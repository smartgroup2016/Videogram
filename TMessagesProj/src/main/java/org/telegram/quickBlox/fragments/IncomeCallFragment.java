package org.telegram.quickBlox.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.quickBlox.SessionManager;
import org.telegram.quickBlox.activities.CallActivity1;
import org.telegram.quickBlox.activities.CallActivity1Tablet;
import org.telegram.quickBlox.definitions.Consts;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProfileActivity;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.telegram.messenger.R;

/**
 * Created by tereha on 16.02.15.
 */
public class IncomeCallFragment extends Fragment implements Serializable {

    private static final String TAG = IncomeCallFragment.class.getSimpleName();
    private TextView typeIncCallView;
    private TextView callerName;
    private ImageView avatarview;
    private TextView otherIncUsers;
    public static ImageButton rejectBtn;
    public ImageButton takeBtn;
    public static final int NOTIFY_ID = 101;

    public static List<Integer> opponents;
    private List<QBUser> opponentsFromCall = new ArrayList<>();
    private MediaPlayer ringtone;
    public static Vibrator vibrator;
    private ImageView image;
    public static QBRTCTypes.QBConferenceType conferenceType;
    private View view;
    private boolean isVideoCall;
    private Map<String, String> userInfo;
    public static String callName;
    TLRPC.FileLocation photoBig = null;
    public String url;
    ProgressDialog progress;
    public Notification notification;
    public static NotificationManager notificationManager;
    File f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState == null) {

            view = inflater.inflate(R.layout.fragment_income_call, container, false);

            if(AndroidUtilities.isTablet()){
                ((CallActivity1Tablet) getActivity()).initActionBar();
            }else {
                ((CallActivity1) getActivity()).initActionBar();
            }
            initCallData();
            initUI(view);
            //initNotification();
            initButtonsListener();

        }

        return view;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        if (grantResults.length > 0){
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    ShowPermissionDenied();
                }
            }
        }else{

            ShowPermissionDenied();

        }
    }
    private void ShowPermissionDenied(){
        AlertDialog.Builder dialog = new AlertDialog.Builder( getActivity());
        dialog.setTitle(LocaleController.getString("AppName", R.string.AppName));
        //dialog.setMessage(LocaleController.getString("premission_denied", R.string.premission_denied));
        dialog.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        });
        dialog.show();
    }
    private void CheckForPermissions(String perm,int i){
        if (Build.VERSION.SDK_INT >= 23 && ApplicationLoader.applicationContext.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{perm},i);
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setRetainInstance(true);
        CheckForPermissions(Manifest.permission.CAMERA,0);
        CheckForPermissions(Manifest.permission.RECORD_AUDIO,1);
        CheckForPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,2);
        CheckForPermissions(Manifest.permission.MODIFY_AUDIO_SETTINGS,3);
        Log.d(TAG, "onCreate() from IncomeCallFragment");
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume(){
        super.onResume();
        initNotification();
    }
    @Override
    public void onStart() {
        super.onStart();
        startCallNotification();
    }

    private void initCallData(){
        QBRTCSession currentSession = SessionManager.getCurrentSession();
        if ( currentSession != null) {
            opponents = currentSession.getOpponents();
            conferenceType = currentSession.getConferenceType();
            userInfo = currentSession.getUserInfo();
            int callerID = currentSession.getCallerID();

            final List<QBUser> users1 = new LinkedList<>();
            List<Integer> Id = new LinkedList<>();
            Id.add(callerID);
            final QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
            requestBuilder.setPerPage(100);

            QBUsers.getUsersByIDs(Id, requestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                    ArrayList<QBUser> resultList = new ArrayList<>(qbUsers.size());
                    resultList.addAll(qbUsers);
                    QBUser user = new QBUser();
                    users1.add(resultList.get(0));
                    user = resultList.get(0);
                    String tmp = users1.get(0).getExternalId();
                    int tmpIn = Integer.valueOf(tmp);
                    final TLRPC.User userTel = MessagesController.getInstance().getUser(tmpIn);
                    if (userTel.last_name != null) {
                        callName = userTel.first_name + " " + userTel.last_name;

                    } else {
                        callName = userTel.first_name;
                    }

                   /* if (userTel.photo != null && userTel.photo.photo_big != null) {

                        f = FileLoader.getPathToAttach(userTel.photo.photo_big, userTel.id != 0);
                        Uri uri = Uri.fromFile(f);
                        avatarview.setImageURI(uri);
                    }*/



                    callerName.setText(callName);
                    callerName.setBackgroundResource(R.drawable.rectangle_rounded_spring_bud);
                    initNotification();

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(List<String> list) {

                }
            });
        }



    }




    public void initNotification(){

        Context context = ApplicationLoader.applicationContext.getApplicationContext();

        Intent notificationIntent = new Intent(context, CallActivity1.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);
        String incCall;
        try {
             incCall = getResources().getString(R.string.inccall);
        } catch (Exception e){
            incCall = "";
        }
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_call)
                        // большая картинка
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
                        //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setTicker(incCall)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                        //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentTitle("TelegramPro")
                        //.setContentText(res.getString(R.string.notifytext))
                .setContentText(getResources().getString(R.string.call)+callName); // Текст уведомления

        notification = builder.getNotification(); // до API 16
        //Notification notification = builder.build();

        notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags = notification.flags | Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        notificationManager.notify(NOTIFY_ID, notification);


    }

     /* @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private Notification createNotification() {

    }*/


    private void initUI(View view) {
        isVideoCall = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(conferenceType);

        typeIncCallView = (TextView) view.findViewById(R.id.typeIncCallView);
        typeIncCallView.setText(isVideoCall ? R.string.incoming_video_call : R.string.incoming_audio_call);

        callerName = (TextView) view.findViewById(R.id.callerName);
        avatarview = (ImageView) view.findViewById(R.id.avatarview);




        rejectBtn = (ImageButton) view.findViewById(R.id.rejectBtn);
        takeBtn = (ImageButton) view.findViewById(R.id.takeBtn);
    }

    private void initButtonsListener() {
        rejectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rejectBtn.setClickable(false);
                takeBtn.setClickable(false);
                Log.d(TAG, "Call is rejected");
                stopCallNotification();
                ProfileActivity.userName = null;
                IncomeCallFragment.callName = null;

                if (SessionManager.getCurrentSession() != null) {
                    Map<String, String> params = new HashMap<>();
                    params.put("reason", "manual");
                    SessionManager.getCurrentSession().rejectCall(params);
                }
                if (ProfileActivity.counter != 0) {
                    Intent intent3 = new Intent(ApplicationLoader.applicationContext.getApplicationContext(), LaunchActivity.class);
                    startActivity(intent3);
                    notificationManager.cancel(NOTIFY_ID);
                    //CallActivity1.notificationManager1.cancel(IncomeCallFragment.NOTIFY_ID+1);
                    //AudioConversationFragment.notificationManager2.cancel(IncomeCallFragment.NOTIFY_ID + 2);

                } else {
                    notificationManager.cancel(NOTIFY_ID);
                    Intent intent3 = new Intent(ApplicationLoader.applicationContext.getApplicationContext(), LaunchActivity.class);
                    startActivity(intent3);
                    //CallActivity1.notificationManager1.cancel(IncomeCallFragment.NOTIFY_ID+1);
                    //AudioConversationFragment.notificationManager2.cancel(IncomeCallFragment.NOTIFY_ID+2);
                    //getActivity().finish();
                }
            }
        });

        takeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeBtn.setClickable(false);
                rejectBtn.setClickable(false);
                stopCallNotification();
                if(AndroidUtilities.isTablet()){
                    ((CallActivity1Tablet) getActivity())
                            .addConversationFragment(
                                    opponents, conferenceType, Consts.CALL_DIRECTION_TYPE.INCOMING);
                }else {
                    ((CallActivity1) getActivity())
                            .addConversationFragment(
                                    opponents, conferenceType, Consts.CALL_DIRECTION_TYPE.INCOMING);
                }
                Log.d(TAG, "Call is started");
                //CallActivity1.notificationManager1.cancel(IncomeCallFragment.NOTIFY_ID + 1);
                //AudioConversationFragment.notificationManager2.cancel(IncomeCallFragment.NOTIFY_ID + 2);
                notificationManager.cancel(NOTIFY_ID);

            }
        });
    }


    public void startCallNotification() {
try {
    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
    ringtone = MediaPlayer.create(getActivity(), notification);
    ringtone.start();

}catch (Exception e){
    try {
        ringtone = MediaPlayer.create(getActivity(), R.raw.beep);
        ringtone.setLooping(true);
        ringtone.start();
    }catch (Exception e1){
        e1.printStackTrace();
    }
}



        /*
        try {
            vibrator = (Vibrator) ApplicationLoader.applicationContext.getSystemService(Context.VIBRATOR_SERVICE);
            long[] vibrationCycle = {0, 1000, 1000};
            if (vibrator.hasVibrator()) {
                vibrator.vibrate(vibrationCycle, 1);
            }
        } catch (Exception e){
            FileLog.e("tmessages", e);
        }*/





    }

    public void stopCallNotification() {
        if (ringtone != null) {
            try {
                ringtone.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            ringtone.release();
            ringtone = null;
        }

        /*if (vibrator != null) {
            vibrator.cancel();
        }*/
    }

    private String getOtherIncUsersNames(List<Integer> opponents) {
        List<Integer> otherOpponents = new ArrayList<>(opponents);
        StringBuffer s = new StringBuffer("");
        //opponentsFromCall.addAll(DataHolder.getUsersList());
        otherOpponents.remove(QBChatService.getInstance().getUser().getId());

        for (Integer i : otherOpponents) {
            for (QBUser usr : opponentsFromCall) {
                if (usr.getId().equals(i)) {
                    if (otherOpponents.indexOf(i) == (otherOpponents.size() - 1)) {
                        s.append(usr.getFullName() + " ");
                        break;
                    } else {
                        s.append(usr.getFullName() + ", ");
                    }
                }
            }
        }
        return s.toString();
    }

    public void onStop() {

        stopCallNotification();
        notificationManager.cancel(NOTIFY_ID);
     //   Intent intent = new Intent(ApplicationLoader.applicationContext.getApplicationContext(), LaunchActivity.class);
     //   startActivity(intent);
        super.onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

