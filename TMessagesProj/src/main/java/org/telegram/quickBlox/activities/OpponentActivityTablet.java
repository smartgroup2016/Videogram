package org.telegram.quickBlox.activities;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.quickBlox.adapters.OpponentsAdapter;
import org.telegram.quickBlox.definitions.Consts;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProfileActivity;
import org.telegram.util.Prefs;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class OpponentActivityTablet extends BaseActivity {


    private static final String TAG = OpponentsActivity.class.getSimpleName();
    private OpponentsAdapter opponentsAdapter;
    private Button btnAudioCall;
    private Button btnVideoCall;
    private ProgressDialog progressDialog;
    private ListView opponentsListView;
    private ArrayList<QBUser> opponentsList;
    private boolean isWifiConnected;
    public static int usIs;
    private HashMap<Integer, MessageObject> unsentMessages = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opponents);



        //initActionBar();
        initUI();
        initProgressDialog();
        initOpponentListAdapter();
    }


    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this) {
            @Override
            public void onBackPressed() {
                //  Toast.makeText(OpponentsActivity.this, getString(R.string.wait_until_loading_finish), Toast.LENGTH_SHORT).show();
            }
        };
        progressDialog.setMessage(getString(R.string.load_opponents));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void initOpponentListAdapter() {
        final ListView opponentsList = (ListView) findViewById(R.id.opponentsList);
        List<QBUser> users = getOpponentsList();
        String user_login = ProfileActivity.userIds;

        user_login=md5(user_login);
        List<String> login = new LinkedList<>();
        login.add(user_login);

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPerPage(1);
        if (users == null) {
            QBUsers.getUsersByLogins(login, requestBuilder, new QBEntityCallback<ArrayList<QBUser>>() {
                @Override
                public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
                    ArrayList<QBUser> orderedUsers = reorderUsersByName(qbUsers);
                    setOpponentsList(orderedUsers);
                    prepareUserList(opponentsList, orderedUsers);
                    hideProgressDialog();


                    if (orderedUsers.size() == 0) {
                        try {
                           int user_opponent_dialog=Prefs.getValue("user_opponent_dialog",0);
                            String tmp = LocaleController.getString("InviteText", R.string.InviteText);
                            //SendMessagesHelper.getInstance().sendMessage(tmp,null, ChatActivity.dialog_id, ChatActivityEnterView.replyingMessageObject, ChatActivityEnterView.messageWebPage, ChatActivityEnterView.messageWebPageSearch, false);
                            SendMessagesHelper.getInstance().sendMessage(tmp.toString(), user_opponent_dialog, null, null, true, null, null, null);
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                        String title =LocaleController.getString("unable_call", R.string.unable_call); ;
                        String message =LocaleController.getString("unable_body", R.string.unable_body); ;
                        String button1String =LocaleController.getString("ok", R.string.ok); getResources().getString(R.string.ok);

                        AlertDialog.Builder builder = new AlertDialog.Builder(OpponentActivityTablet.this);
                        builder.setTitle(title);  // заголовок
                        builder.setMessage(message); // сообщение
                        builder.setNegativeButton(button1String, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {

                                finish();
                            }
                        });
                        builder.show();
                    }

                }


                @Override
                public void onSuccess () {

                }

                @Override
                public void onError (List < String > list) {

                }
            });
        }else {
            ArrayList<QBUser> userList = getOpponentsList();
            prepareUserList(opponentsList, userList);
            hideProgressDialog();
        }
    }
    public void setOpponentsList(ArrayList<QBUser> qbUsers) {
        this.opponentsList = qbUsers;
    }

    public ArrayList<QBUser> getOpponentsList() {
        return opponentsList;
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }

    }
    private void hideProgressDialog() {
        if (progressDialog != null){
            progressDialog.dismiss();
        }
    }




    private void prepareUserList(ListView opponentsList, List<QBUser> users) {
        QBUser currentUser = QBChatService.getInstance().getUser();

        ArrayList <QBUser> nonAppUsers = new ArrayList<>();
        for (QBUser nonAppUser : users){

            nonAppUsers.add(nonAppUser);
            onClick();

        }

        //if (users.contains(currentUser)) {
        //     users.remove(currentUser);
        //  }

        //if (users.containsAll(nonAppUsers)) {
        //     users.removeAll(nonAppUsers);
        //  }

        // Prepare users list for simple adapter.
        opponentsAdapter = new OpponentsAdapter(this, users);
        opponentsList.setAdapter(opponentsAdapter);
        opponentsList.setSelection(0);
    }



    private void initUI() {

        //btnAudioCall = (Button) findViewById(R.id.btnAudioCall);
        //btnVideoCall = (Button) findViewById(R.id.btnVideoCall);

        //btnAudioCall = new Button(getBaseContext());

        //btnAudioCall.setOnClickListener(this);
        //btnVideoCall.setOnClickListener(this);
        //opponentsListView = new ListView(getBaseContext());
        opponentsListView = (ListView) findViewById(R.id.opponentsList);
        opponentsListView.setVisibility(View.GONE);
    }


    public void onClick() {


        QBRTCTypes.QBConferenceType qbConferenceType = null;

        //Init conference type
        switch (ProfileActivity.counter) {
            //case R.id.btnAudioCall:
            case 1:
                qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;
                //  setActionButtonsClickable(false);
                ProfileActivity.counter =0;
                break;

            case 2:
                qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;
                // setActionButtonsClickable(false);
                ProfileActivity.counter=0;
                break;
        }

        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("any_custom_data", "some data");
        userInfo.put("my_avatar_url", "avatar_reference");

        Log.d(TAG, "QBChatService.getInstance().isLoggedIn() = " + String.valueOf(QBChatService.getInstance().isLoggedIn()));

        List<QBUser> users = getOpponentsList();

            /*if (!isWifiConnected){
                showToast(R.string.internet_not_connected);
                //setActionButtonsClickable(true);
            } else if (!QBChatService.getInstance().isLoggedIn()){
                showToast(R.string.initializing_in_chat);
               // setActionButtonsClickable(true);
            }else if (isWifiConnected && QBChatService.getInstance().isLoggedIn()) {*/
        CallActivity1Tablet.start(this, qbConferenceType, getOpponentsIds(users),
                userInfo, Consts.CALL_DIRECTION_TYPE.OUTGOING);


    }

   /* private void setActionButtonsClickable(boolean isClickable) {
        btnAudioCall.setClickable(isClickable);
        btnVideoCall.setClickable(isClickable);
    }*/

    public static ArrayList<Integer> getOpponentsIds(List<QBUser> opponents){
        ArrayList<Integer> ids = new ArrayList<>();
        for(QBUser user : opponents){
            ids.add(user.getId());
        }
        return ids;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (OpponentsAdapter.i > 0){
            opponentsListView.setSelection(OpponentsAdapter.i);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        setActionButtonsClickable(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(progressDialog != null && progressDialog.isShowing()) {
            hideProgressDialog();
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                showLogOutDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private ArrayList<QBUser> reorderUsersByName(ArrayList<QBUser> qbUsers) {
        // Make clone collection to avoid modify input param qbUsers
        ArrayList<QBUser> resultList = new ArrayList<>(qbUsers.size());
        resultList.addAll(qbUsers);

        // Rearrange list by user IDs
        Collections.sort(resultList, new Comparator<QBUser>() {
            @Override
            public int compare(QBUser firstUsr, QBUser secondUsr) {
                if (firstUsr.getId().equals(secondUsr.getId())) {
                    return 0;
                } else if (firstUsr.getId() < secondUsr.getId()) {
                    return -1;
                } else {
                    return 1;
                }
            }
        });
        return resultList;
    }
    @Override
    public void onBackPressed() {
        Intent intent2 = new Intent(this, LaunchActivity.class);
        startActivity(intent2);
        //minimizeApp();
    }

    private void showLogOutDialog(){
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(this);
        quitDialog.setTitle(R.string.log_out_dialog_title);
        quitDialog.setMessage(R.string.log_out_dialog_message);

        quitDialog.setPositiveButton(R.string.positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                OpponentsAdapter.i = 0;
                stopIncomeCallListenerService();
                clearUserDataFromPreferences();
                startListUsersActivity();
                finish();
            }
        });

        quitDialog.setNegativeButton(R.string.negative_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        quitDialog.show();
    }

    @Override
    void processCurrentConnectionState(boolean isConncted) {
        if (!isConncted) {
            Log.d(TAG, "Internet is turned off");
            isWifiConnected = false;
        } else {
            Log.d(TAG, "Internet is turned on");
            isWifiConnected = true;
        }
    }


}


