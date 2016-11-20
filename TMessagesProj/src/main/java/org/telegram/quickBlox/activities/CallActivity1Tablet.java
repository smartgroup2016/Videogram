package org.telegram.quickBlox.activities;


import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBSettings;
import com.quickblox.videochat.webrtc.QBRTCClient;
import com.quickblox.videochat.webrtc.QBRTCConfig;
import com.quickblox.videochat.webrtc.QBRTCException;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import org.jivesoftware.smack.SmackException;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildVars;
import org.telegram.quickBlox.SessionManager;
import org.telegram.quickBlox.definitions.Consts;
import org.telegram.quickBlox.fragments.AudioConversationFragment;
import org.telegram.quickBlox.fragments.BaseConversationFragment;
import org.telegram.quickBlox.fragments.IncomeCallFragment;
import org.telegram.quickBlox.fragments.VideoConversationFragment;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProfileActivity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.telegram.messenger.R;

/**
 *
 * Created by tereha on 16.02.15.
 *
 */
public class CallActivity1Tablet extends BaseActivity {


    private static final String TAG = CallActivity1.class.getSimpleName();
    public static final String INCOME_CALL_FRAGMENT = "income_call_fragment";
    public static final String CONVERSATION_CALL_FRAGMENT = "conversation_call_fragment";

    private Runnable showIncomingCallWindowTask;
    private Handler showIncomingCallWindowTaskHandler;
    private boolean closeByWifiStateAllow = true;
    private String hangUpReason;
    private boolean isInCommingCall;
    private Consts.CALL_DIRECTION_TYPE call_direction_type;
    private QBRTCTypes.QBConferenceType call_type;
    private List<Integer> opponentsList;
    private MediaPlayer ringtone;
    private BroadcastReceiver callBroadcastReceiver;
    public static Notification notification1;
    public static NotificationManager notificationManager1;

    public static void start(Context context, QBRTCTypes.QBConferenceType qbConferenceType,
                             List<Integer> opponentsIds, Map<String, String> userInfo,
                             Consts.CALL_DIRECTION_TYPE callDirectionType){
        Intent intent = new Intent(context, CallActivity1Tablet.class);
        intent.putExtra(Consts.CALL_DIRECTION_TYPE_EXTRAS, callDirectionType);
        intent.putExtra(Consts.CALL_TYPE_EXTRAS, qbConferenceType);
        intent.putExtra(Consts.USER_INFO_EXTRAS, (Serializable) userInfo);
        intent.putExtra(Consts.OPPONENTS_LIST_EXTRAS, (Serializable) opponentsIds);
        if (callDirectionType == Consts.CALL_DIRECTION_TYPE.INCOMING) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerCallbackListener();

        if (getIntent().getExtras() != null) {
            parseIntentExtras(getIntent().getExtras());
        }
        if (call_direction_type == Consts.CALL_DIRECTION_TYPE.INCOMING){

            isInCommingCall = true;
            addIncomeCallFragment(SessionManager.getCurrentSession());

        } else if (call_direction_type == Consts.CALL_DIRECTION_TYPE.OUTGOING){
            initNotificationOut();
            isInCommingCall = false;
            addConversationFragment(opponentsList, call_type, call_direction_type);
        }

    }

    public void initNotificationOut(){

        Context context = ApplicationLoader.applicationContext.getApplicationContext();

        Intent notificationIntent = new Intent(context, CallActivity1Tablet.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        String tmp = null;
        if (ProfileActivity.userName != null){
            tmp =ProfileActivity.userName;
        } else if(IncomeCallFragment.callName !=null){
            tmp =ProfileActivity.userName;
        }
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_call)
                // большая картинка
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
                //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setTicker("Call "+ProfileActivity.userName)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentTitle("TelegramPro")
                //.setContentText(res.getString(R.string.notifytext))
                .setContentText("Call "+tmp); // Текст уведомления

        notification1 = builder.getNotification(); // до API 16
        //Notification notification = builder.build();

        notificationManager1 = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notification1.flags = notification1.flags | Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        notificationManager1.notify(IncomeCallFragment.NOTIFY_ID + 4, notification1);


    }

    private void parseIntentExtras(Bundle extras) {
        call_direction_type = (Consts.CALL_DIRECTION_TYPE) extras.getSerializable(
                Consts.CALL_DIRECTION_TYPE_EXTRAS);
        call_type = (QBRTCTypes.QBConferenceType) extras.getSerializable(Consts.CALL_TYPE_EXTRAS);
        opponentsList = (List<Integer>) extras.getSerializable(Consts.OPPONENTS_LIST_EXTRAS);
    }

    @Override
    void processCurrentConnectionState(boolean isConnected) {
        if (!isConnected) {
            Log.d(TAG, "Internet is turned off");
            if (closeByWifiStateAllow) {
                if (SessionManager.getCurrentSession() != null) {
                    Log.d(TAG, "currentSession NOT null");
                    // Close session safely
                    disableConversationFragmentButtons();
                    stopOutBeep();

                    hangUpCurrentSession();

                    hangUpReason = Consts.WIFI_DISABLED;
                } else {
                    Log.d(TAG, "Call finish() on activity");
                    finish();
                }
            }
        } else {
            Log.d(TAG, "Internet is turned on");
        }
    }

    private void disableConversationFragmentButtons() {
        BaseConversationFragment fragment = (BaseConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
        if (fragment != null) {
            fragment.actionButtonsEnabled(false);
        }
    }

    private void initIncommingCallTask() {
        showIncomingCallWindowTaskHandler = new Handler(Looper.myLooper());
        showIncomingCallWindowTask = new Runnable() {
            @Override
            public void run() {
                IncomeCallFragment incomeCallFragment = (IncomeCallFragment) getFragmentManager().findFragmentByTag(INCOME_CALL_FRAGMENT);
                if (incomeCallFragment == null) {
                    BaseConversationFragment conversationFragment = (BaseConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
                    if (conversationFragment != null) {
                        disableConversationFragmentButtons();
                        stopOutBeep();
                        hangUpCurrentSession();
                    }
                } else {
                    rejectCurrentSession();
                    finish();
                }
            }
        };
    }

    public void rejectCurrentSession() {
        if (SessionManager.getCurrentSession() != null) {
            Map<String, String> params = new HashMap<>();
            params.put("reason", "manual");
            SessionManager.getCurrentSession().rejectCall(params);
        }
    }

    public void hangUpCurrentSession() {
        if (SessionManager.getCurrentSession() != null) {
            SessionManager.getCurrentSession().hangUp(new HashMap<String, String>());
        }
        finish();
    }

    private void startIncomeCallTimer() {
        showIncomingCallWindowTaskHandler.postAtTime(showIncomingCallWindowTask, SystemClock.uptimeMillis() + TimeUnit.SECONDS.toMillis(QBRTCConfig.getAnswerTimeInterval()));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private void stopIncomeCallTimer() {
        Log.d(TAG, "stopIncomeCallTimer");
        showIncomingCallWindowTaskHandler.removeCallbacks(showIncomingCallWindowTask);
    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopOutBeep();
        if (call_direction_type == Consts.CALL_DIRECTION_TYPE.OUTGOING) {
            notificationManager1.cancel(IncomeCallFragment.NOTIFY_ID + 4);
            notificationManager1.cancel(IncomeCallFragment.NOTIFY_ID + 1);
        }
    }

    private void forbidenCloseByWifiState() {
        closeByWifiStateAllow = false;
    }


    // ---------------Chat callback methods implementation  ----------------------//

    public void onReceiveNewSession(){
    }

    public void onUserNotAnswer(Integer userID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast(R.string.noAnswer);

                Intent intent5 = new Intent(ApplicationLoader.applicationContext.getApplicationContext(), LaunchActivity.class);
                startActivity(intent5);
                finish();
            }
        });
    }

    public void onStartConnectToUser(Integer userID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //  showToast(R.string.checking);
                stopOutBeep();
            }
        });
    }

    public void onCallRejectByUser (Integer userID, Map<String, String> userInfo) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // showToast(R.string.rejected);
                stopOutBeep();
                Intent intent5 = new Intent(ApplicationLoader.applicationContext.getApplicationContext(), LaunchActivity.class);
                startActivity(intent5);
                finish();
            }
        });
    }

    public void onConnectionClosedForUser(Integer userID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // showToast(R.string.closed);
                // Close app after session close of network was disabled
                Log.d(TAG, "onConnectionClosedForUser()");
                if (hangUpReason != null && hangUpReason.equals(Consts.WIFI_DISABLED)) {
                    Intent returnIntent = new Intent();
                    setResult(Consts.CALL_ACTIVITY_CLOSE_WIFI_DISABLED, returnIntent);
                    finish();
                }
            }
        });
    }

    public void onConnectedToUser(final Integer userID) {
        forbidenCloseByWifiState();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isInCommingCall) {
                    stopIncomeCallTimer();
                }
                // showToast(R.string.connected);
                startTimer();

                BaseConversationFragment fragment = (BaseConversationFragment) getFragmentManager().findFragmentByTag(CONVERSATION_CALL_FRAGMENT);
                if (fragment != null) {
                    fragment.actionButtonsEnabled(true);
                }

                // test code
                //
                try {
                    // create a message
                    QBChatMessage chatMessage = new QBChatMessage();
                    chatMessage.setProperty("param1", "value1");
                    chatMessage.setProperty("param2", "value2");
                    chatMessage.setBody("system body");

                    chatMessage.setRecipientId(QBChatService.getInstance().getUser().getId());

                    QBChatService.getInstance().getSystemMessagesManager().sendSystemMessage(chatMessage);
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                } catch (IllegalStateException ee){
                    ee.printStackTrace();
                }
                //
                //
            }
        });
    }

    public void onDisconnectedTimeoutFromUser(Integer userID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //  showToast(R.string.time_out);
                hangUpCurrentSession();
            }
        });
    }

    public void onConnectionFailedWithUser(Integer userID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //showToast(R.string.failed);
            }
        });
    }

    public void onError(QBRTCException e) {
    }

    public void onSessionClosed() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isInCommingCall) {
                    stopIncomeCallTimer();
                    Log.d(TAG, "isInCommingCall - " + isInCommingCall);
                }

                SessionManager.setCurrentSession(null);

                Log.d(TAG, "Stop session");

                stopTimer();
                closeByWifiStateAllow = true;
                finish();
            }
        });
    }

    public void onSessionStartClose() {
        stopOutBeep();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disableConversationFragmentButtons();
            }
        });
    }

    public void onDisconnectedFromUser(Integer userID) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //showToast(R.string.disconnected);
            }
        });
    }

    public void onReceiveHangUpFromUser(final Integer userID) {
        // TODO update view of this user
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //showToast(R.string.hungUp);
            }
        });
        finish();
    }

    private void addIncomeCallFragment(QBRTCSession session) {
        if(session != null) {
            initIncommingCallTask();
            startIncomeCallTimer();
            Fragment fragment = new IncomeCallFragment();
            getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, INCOME_CALL_FRAGMENT).commit();
        } else {
            Log.d(TAG, "SKIP addIncomeCallFragment method");
            finish();
        }
    }

    public void addConversationFragment (List<Integer> opponents,
                                         QBRTCTypes.QBConferenceType qbConferenceType,
                                         Consts.CALL_DIRECTION_TYPE callDirectionType){

        initNotification();
        if (SessionManager.getCurrentSession() == null && callDirectionType == Consts.CALL_DIRECTION_TYPE.OUTGOING){
            startOutBeep();
            try {
                QBSettings.getInstance().fastConfigInit(BuildVars.QB_Application_ID, BuildVars.QB_Authorization_key, BuildVars.QB_Authorization_secret);

                QBRTCSession newSessionWithOpponents = QBRTCClient.getInstance().createNewSessionWithOpponents(opponents, qbConferenceType);
                SessionManager.setCurrentSession(newSessionWithOpponents);
                Log.d(TAG, "addConversationFragmentStartCall. Set session " + newSessionWithOpponents);

            } catch (IllegalStateException e) {
                // Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        BaseConversationFragment fragment;
        if (QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(qbConferenceType)) {
            ProfileActivity.statuscall = 1;
            fragment = new VideoConversationFragment();
            notificationManager1.cancel(IncomeCallFragment.NOTIFY_ID+4);
            notificationManager1.cancel(IncomeCallFragment.NOTIFY_ID+1);
        }
        else {
            ProfileActivity.statuscall = 2;
            fragment = new AudioConversationFragment();
            notificationManager1.cancel(IncomeCallFragment.NOTIFY_ID+4);
            notificationManager1.cancel(IncomeCallFragment.NOTIFY_ID+1);
        }

        Bundle bundle = new Bundle();
        bundle.putInt(Consts.CALL_DIRECTION_TYPE_EXTRAS, callDirectionType.ordinal());
        fragment.setArguments(bundle);

        getFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, CONVERSATION_CALL_FRAGMENT).commit();
    }

    public void initNotification(){

        Context context = ApplicationLoader.applicationContext.getApplicationContext();

        Intent notificationIntent = new Intent(context, CallActivity1Tablet.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);

        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_call)
                // большая картинка
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
                //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setTicker("Begin call")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentTitle("TelegramPro");
        //.setContentText(res.getString(R.string.notifytext))
        // Текст уведомления

        notification1 = builder.getNotification(); // до API 16
        //Notification notification = builder.build();

        notificationManager1 = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notification1.flags = notification1.flags | Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        notificationManager1.notify(IncomeCallFragment.NOTIFY_ID + 1, notification1);


    }

    private void startOutBeep() {
        try {
            ringtone = MediaPlayer.create(this, R.raw.beep);
            ringtone.setLooping(true);
            ringtone.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stopOutBeep() {
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!closeByWifiStateAllow) {
            if (SessionManager.getCurrentSession() != null) {
                if (SessionManager.getCurrentSession().getState() == QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_ACTIVE) {
                    hangUpCurrentSession();
                } else if (SessionManager.getCurrentSession().getState() == QBRTCSession.QBRTCSessionState.QB_RTC_SESSION_NEW) {
                    rejectCurrentSession();
                }
            }
        }
        SessionManager.setCurrentSession(null);
        unregisterReceiver(callBroadcastReceiver);
    }

    @Override
    public void onAttachedToWindow() {
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
    }

    @Override
    public void onBackPressed() {
    }

    private void registerCallbackListener(){
        callBroadcastReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Consts.CALL_RESULT)){

                    int callTask = intent.getIntExtra(Consts.CALL_ACTION_VALUE, 0);
                    final Integer userID = intent.getIntExtra(Consts.USER_ID, 0);
                    final Map<String, String> userInfo = (Map<String, String>) intent
                            .getSerializableExtra(Consts.USER_INFO_EXTRAS);
                    final QBRTCException exception = (QBRTCException) intent
                            .getSerializableExtra(Consts.QB_EXCEPTION_EXTRAS);

                    switch (callTask) {
                        case Consts.RECEIVE_NEW_SESSION:
                            onReceiveNewSession();
                            break;
                        case Consts.USER_NOT_ANSWER:
                            onUserNotAnswer(userID);
                            break;
                        case Consts.CALL_REJECT_BY_USER:
                            onCallRejectByUser(userID, userInfo);
                            break;
                        case Consts.RECEIVE_HANG_UP_FROM_USER:
                            onReceiveHangUpFromUser(userID);
                            break;
                        case Consts.SESSION_CLOSED:
                            onSessionClosed();
                            break;
                        case Consts.SESSION_START_CLOSE:
                            onSessionStartClose();
                            break;
                        case Consts.START_CONNECT_TO_USER:
                            onStartConnectToUser(userID);
                            break;
                        case Consts.CONNECTED_TO_USER:
                            onConnectedToUser(userID);
                            break;
                        case Consts.CONNECTION_CLOSED_FOR_USER:
                            onConnectionClosedForUser(userID);
                            break;
                        case Consts.DISCONNECTED_FROM_USER:
                            onDisconnectedFromUser(userID);
                            break;
                        case Consts.DISCONNECTED_TIMEOUT_FROM_USER:
                            onDisconnectedTimeoutFromUser(userID);
                            break;
                        case Consts.CONNECTION_FAILED_WITH_USER:
                            onConnectionFailedWithUser(userID);
                            break;
                        case Consts.ERROR:
                            onError(exception);
                            break;
                    }
                }

            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Consts.CALL_RESULT);
        registerReceiver(callBroadcastReceiver, intentFilter);
    }
}


