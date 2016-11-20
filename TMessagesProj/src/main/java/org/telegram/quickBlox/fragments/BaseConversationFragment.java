package org.telegram.quickBlox.fragments;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtc.QBRTCSession;
import com.quickblox.videochat.webrtc.QBRTCTypes;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.MessagesStorage;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.messenger.VideoEditedInfo;
import org.telegram.quickBlox.SessionManager;
import org.telegram.quickBlox.activities.CallActivity1;
import org.telegram.quickBlox.activities.CallActivity1Tablet;
import org.telegram.quickBlox.adapters.OpponentsAdapter;
import org.telegram.quickBlox.definitions.Consts;
import org.telegram.quickBlox.holder.DataHolder;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.QuickAckDelegate;
import org.telegram.tgnet.RequestDelegate;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ChatActivityEnterView;
import org.telegram.ui.LaunchActivity;
import org.telegram.ui.ProfileActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.telegram.messenger.R;

/**
 * Created by tereha on 15.07.15.
 */
public abstract class BaseConversationFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = BaseConversationFragment.class.getSimpleName();

    protected List<Integer> opponents;
    public static QBRTCTypes.QBConferenceType qbConferenceType;
    protected int startReason;
    protected String callerName;
    public String callerPhone;
    String phoneNumber;

    private HashMap<Integer, MessageObject> unsentMessages = new HashMap<>();


    private ImageButton dynamicToggleVideoCall;
    private ImageButton micToggleVideoCall;
    private ImageButton handUpVideoCall;
    private TextView opponentNameView;
    private boolean isAudioEnabled = true;
    private boolean isMessageProcessed;
    private IntentFilter intentFilter;
    private AudioStreamReceiver audioStreamReceiver;
    private Integer callerID;
    private ArrayList<QBUser> opponentsList;
    private OpponentsAdapter opponentsAdapter;
    int coun =0;
    private HashMap<Integer, TLRPC.Message> sendingMessages = new HashMap<>();
    private int swith = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getContentView(), container, false);

        if( AndroidUtilities.isTablet()) {
            ((CallActivity1Tablet) getActivity()).initActionBarWithTimer();
        }else{
            ((CallActivity1) getActivity()).initActionBarWithTimer();
        }

        if (getArguments() != null) {
            startReason = getArguments().getInt(Consts.CALL_DIRECTION_TYPE_EXTRAS);
        }
        initCallData();
        initViews(view);

        return view;
    }
    public void setOpponentsList(ArrayList<QBUser> qbUsers) {
        this.opponentsList = qbUsers;
    }

    protected void initCallData() {
        QBRTCSession session = SessionManager.getCurrentSession();
        if (session != null){
            opponents = session.getOpponents();
            callerID = session.getCallerID();
            callerName = DataHolder.getUserNameByID(callerID);
           // callerName = DataHolder.getUserNameByID(callerID);
            //callerPhone = DataHolder.getUserPhoneByID(callerID);
            //phoneNumber = PhoneNumberUtils.stripSeparators(callerPhone);

            //getContactName(callerPhone);
            qbConferenceType = session.getConferenceType();
        }
    }


    protected abstract int getContentView();

    public void actionButtonsEnabled(boolean enability) {

        micToggleVideoCall.setEnabled(enability);
        dynamicToggleVideoCall.setEnabled(enability);

        // inactivate toggle buttons
        micToggleVideoCall.setActivated(enability);
        dynamicToggleVideoCall.setActivated(enability);
    }


    @Override
    public void onStart() {
        getActivity().registerReceiver(audioStreamReceiver, intentFilter);

        super.onStart();
        QBRTCSession session = SessionManager.getCurrentSession();
        if (!isMessageProcessed && session != null) {
            if (startReason == Consts.CALL_DIRECTION_TYPE.INCOMING.ordinal()) {
                coun =1;
                Log.d(TAG, "acceptCall() from " + TAG);
                session.acceptCall(session.getUserInfo());
                if ( ProfileActivity.statuscall == 2) {
                    onClick(dynamicToggleVideoCall);
                }
            } else {
                coun =2;
                Log.d(TAG, "startCall() from " + TAG);
                session.startCall(session.getUserInfo());
                if ( ProfileActivity.statuscall == 2) {
                    onClick(dynamicToggleVideoCall);
                }
            }
            isMessageProcessed = true;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intentFilter = new IntentFilter();

        intentFilter.addAction(AudioManager.ACTION_HEADSET_PLUG);
        intentFilter.addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        audioStreamReceiver = new AudioStreamReceiver();

    }

    protected void initViews(View view) {
        dynamicToggleVideoCall = (ImageButton) view.findViewById(R.id.dynamicToggleVideoCall);
        dynamicToggleVideoCall.setOnClickListener(this);




        micToggleVideoCall = (ImageButton) view.findViewById(R.id.micToggleVideoCall);
        micToggleVideoCall.setOnClickListener(this);



        handUpVideoCall = (ImageButton) view.findViewById(R.id.handUpVideoCall);
        handUpVideoCall.setOnClickListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(coun ==2) {
           // processSendingText(getResources().getString(R.string.call));

        }
        if (!QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(BaseConversationFragment.qbConferenceType)) {
            AudioConversationFragment.notificationManager2.cancel(IncomeCallFragment.NOTIFY_ID + 2);
            //AudioConversationFragment.notificationManager2.cancelAll();
        }

        Intent intent = new Intent(ApplicationLoader.applicationContext.getApplicationContext(), LaunchActivity.class);
        startActivity(intent);
        //ProfileActivity.userName = null;
        //IncomeCallFragment.callName = null;
        getActivity().unregisterReceiver(audioStreamReceiver);
        //AudioConversationFragment.notificationManager2.cancel(IncomeCallFragment.NOTIFY_ID + 2);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dynamicToggleVideoCall:
                if (SessionManager.getCurrentSession() != null) {
                    Log.d(TAG, "Dynamic switched");

                    if (swith ==0){
                        dynamicToggleVideoCall.setBackgroundResource(R.drawable.ic_dynamic_off);
                        swith = 2;
                    } else if(swith ==2){
                        dynamicToggleVideoCall.setBackgroundResource(R.drawable.ic_dynamic_on);
                        swith = 0;
                    }

                    SessionManager.getCurrentSession().switchAudioOutput();
                    ProfileActivity.statuscall = 0;
                }
                break;
            case R.id.micToggleVideoCall:
                if (SessionManager.getCurrentSession() != null) {
                    if (isAudioEnabled) {
                        micToggleVideoCall.setBackgroundResource(R.drawable.ic_mic_off);
                        Log.d(TAG, "Mic is off");
                        SessionManager.getCurrentSession().setAudioEnabled(false);
                        isAudioEnabled = false;
                    } else {
                        micToggleVideoCall.setBackgroundResource(R.drawable.ic_mic_on);
                        Log.d(TAG, "Mic is on");
                        SessionManager.getCurrentSession().setAudioEnabled(true);
                        isAudioEnabled = true;
                    }
                }
                break;
            case R.id.handUpVideoCall:
                actionButtonsEnabled(false);
                handUpVideoCall.setEnabled(false);
                Log.d(TAG, "Call is stopped");

                ProfileActivity.userName = null;
                IncomeCallFragment.callName = null;

                if (SessionManager.getCurrentSession() != null) {
                    SessionManager.getCurrentSession().hangUp(new HashMap<String, String>());
                }
                if (!QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO.equals(qbConferenceType)) {
                    AudioConversationFragment.notificationManager2.cancel(IncomeCallFragment.NOTIFY_ID + 2);
                }
                Intent intent = new Intent(ApplicationLoader.applicationContext.getApplicationContext(), LaunchActivity.class);
                startActivity(intent);
                handUpVideoCall.setEnabled(false);
                handUpVideoCall.setActivated(false);
                break;
            default:
                break;
        }
    }

    public boolean processSendingText(String text) {
        text = getTrimmedString(text);
        if (text.length() != 0) {
            int count = (int) Math.ceil(text.length() / 4096.0f);
            for (int a = 0; a < count; a++) {
                String mess = text.substring(a * 4096, Math.min((a + 1) * 4096, text.length()));
               // SendMessagesHelper.getInstance().sendMessage(mess,null, ChatActivity.dialog_id, ChatActivityEnterView.replyingMessageObject, ChatActivityEnterView.messageWebPage, ChatActivityEnterView.messageWebPageSearch, asAdmin());
                SendMessagesHelper.getInstance().sendMessage(mess.toString(),  ChatActivity.dialog_id, null, null, true, null, null, null);

            }
            return true;
        }
        return false;
    }
    public boolean asAdmin() {
        return ChatActivityEnterView.isAsAdmin;
    }

    public void sendMessage(String message, long peer, MessageObject reply_to_msg, TLRPC.WebPage webPage, boolean searchLinks, boolean asAdmin) {
        sendMessage(message, null, null, null, null, null, null, null, null, null, peer, false, null, reply_to_msg, webPage, searchLinks, asAdmin);
    }

    private void sendMessage(String message, TLRPC.MessageMedia location, TLRPC.TL_photo photo, TLRPC.TL_video_layer45 video, VideoEditedInfo videoEditedInfo, MessageObject msgObj, TLRPC.User user, TLRPC.TL_document document, TLRPC.TL_audio_layer45 audio, String originalPath, long peer, boolean retry, String path, MessageObject reply_to_msg, TLRPC.WebPage webPage, boolean searchLinks, boolean asAdmin) {
        if (peer == 0) {
            return;
        }

        TLRPC.Message newMsg = null;
        int type = -1;
        int lower_id = (int) peer;
        int high_id = (int) (peer >> 32);
        TLRPC.EncryptedChat encryptedChat = null;
        TLRPC.InputPeer sendToPeer = lower_id != 0 ? MessagesController.getInputPeer(lower_id) : null;
        ArrayList<TLRPC.InputUser> sendToPeers = null;
        if (message != null) {
            newMsg = new TLRPC.TL_message();

            if (encryptedChat != null || webPage == null) {
                newMsg.media = new TLRPC.TL_messageMediaEmpty();
            }
            type = 0;
            newMsg.message = message;
        }

        newMsg.local_id = newMsg.id = UserConfig.getNewMessageId();
        newMsg.flags |= TLRPC.MESSAGE_FLAG_OUT;

        newMsg.from_id = UserConfig.getClientUserId();
        newMsg.flags |= TLRPC.MESSAGE_FLAG_HAS_FROM_ID;

        UserConfig.saveConfig(false);

        if (newMsg.random_id == 0) {
            newMsg.random_id = getNextRandomId();
        }
        newMsg.date = ConnectionsManager.getInstance().getCurrentTime();
        newMsg.flags |= TLRPC.MESSAGE_FLAG_HAS_MEDIA;


        newMsg.flags |= TLRPC.MESSAGE_FLAG_UNREAD;

        newMsg.dialog_id = peer;

        if (lower_id != 0) {

            newMsg.to_id = MessagesController.getPeer(lower_id);
            if (lower_id > 0) {
                TLRPC.User sendToUser = MessagesController.getInstance().getUser(lower_id);
                if (sendToUser == null) {
                    processSentMessage(newMsg.id);
                    return;
                }
                if ((sendToUser.flags & TLRPC.USER_FLAG_BOT) != 0) {
                    newMsg.flags &= ~TLRPC.MESSAGE_FLAG_UNREAD;
                }
            }

        }

        MessageObject newMsgObj = new MessageObject(newMsg, null, true);
        newMsgObj.replyMessageObject = reply_to_msg;
        newMsgObj.messageOwner.send_state = MessageObject.MESSAGE_SEND_STATE_SENDING;


        newMsg.message = "Звонок";

        ArrayList<MessageObject> objArr = new ArrayList<>();
        objArr.add(newMsgObj);
        ArrayList<TLRPC.Message> arr = new ArrayList<>();
        arr.add(newMsg);
        MessagesStorage.getInstance().putMessages(arr, false, true, false, 0);
        //newMsg.message = "Входящий звонок";

        MessagesController.getInstance().updateInterfaceWithMessages(peer, objArr);
        NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);

        TLRPC.TL_messages_sendMessage reqSend = new TLRPC.TL_messages_sendMessage();
        reqSend.message = message;
        reqSend.peer = sendToPeer;
        reqSend.random_id = newMsg.random_id;

        performSendMessageRequest(reqSend, newMsgObj.messageOwner, null);

    }

    private void performSendMessageRequest(final TLObject req, final TLRPC.Message newMsgObj, final String originalPath) {
        putToSendingMessages(newMsgObj);
        ConnectionsManager.getInstance().sendRequest(req, new RequestDelegate() {
            @Override
            public void run(TLObject response, final TLRPC.TL_error error) {
                boolean isSentError = false;
                if (error == null) {
                    final int oldId = newMsgObj.id;
                    final boolean isBroadcast = req instanceof TLRPC.TL_messages_sendBroadcast;
                    final ArrayList<TLRPC.Message> sentMessages = new ArrayList<>();
                    final String attachPath = newMsgObj.attachPath;
                    if (response instanceof TLRPC.TL_updateShortSentMessage) {
                        TLRPC.TL_updateShortSentMessage res = (TLRPC.TL_updateShortSentMessage) response;
                        newMsgObj.local_id = newMsgObj.id = res.id;
                        newMsgObj.date = res.date;
                        newMsgObj.entities = res.entities;
                        if (res.media != null) {
                            newMsgObj.media = res.media;
                            newMsgObj.flags |= TLRPC.MESSAGE_FLAG_HAS_MEDIA;
                        }
                        if (!newMsgObj.entities.isEmpty()) {
                            newMsgObj.flags |= TLRPC.MESSAGE_FLAG_HAS_ENTITIES;
                        }
                        MessagesController.getInstance().processNewDifferenceParams(-1, res.pts, res.date, res.pts_count);
                        sentMessages.add(newMsgObj);
                    } else if (response instanceof TLRPC.Updates) {
                        boolean ok = false;
                        for (TLRPC.Update update : ((TLRPC.Updates) response).updates) {
                            if (update instanceof TLRPC.TL_updateNewMessage) {
                                TLRPC.TL_updateNewMessage newMessage = (TLRPC.TL_updateNewMessage) update;
                                sentMessages.add(newMessage.message);
                                newMsgObj.id = newMessage.message.id;
                                processSentMessage(newMsgObj, newMessage.message, originalPath);
                                MessagesController.getInstance().processNewDifferenceParams(-1, newMessage.pts, -1, newMessage.pts_count);
                                ok = true;
                                break;
                            } else if (update instanceof TLRPC.TL_updateNewChannelMessage) {
                                TLRPC.TL_updateNewChannelMessage newMessage = (TLRPC.TL_updateNewChannelMessage) update;
                                sentMessages.add(newMessage.message);
                                newMsgObj.id = newMessage.message.id;
                                processSentMessage(newMsgObj, newMessage.message, originalPath);
                                MessagesController.getInstance().processNewChannelDifferenceParams(newMessage.pts, newMessage.pts_count, newMessage.message.to_id.channel_id);
                                ok = true;
                                break;
                            }
                        }
                        if (!ok) {
                            isSentError = true;
                        }
                    }

                    if (!isSentError) {
                        MessagesStorage.getInstance().getStorageQueue().postRunnable(new Runnable() {
                            @Override
                            public void run() {
                                MessagesStorage.getInstance().updateMessageStateAndId(newMsgObj.random_id, oldId, (isBroadcast ? oldId : newMsgObj.id), 0, false, newMsgObj.to_id.channel_id);
                                MessagesStorage.getInstance().putMessages(sentMessages, true, false, isBroadcast, 0);
                                if (isBroadcast) {
                                    ArrayList<TLRPC.Message> currentMessage = new ArrayList<>();
                                    currentMessage.add(newMsgObj);
                                    newMsgObj.send_state = MessageObject.MESSAGE_SEND_STATE_SENT;
                                    MessagesStorage.getInstance().putMessages(currentMessage, true, false, false, 0);
                                }
                                AndroidUtilities.runOnUIThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        newMsgObj.send_state = MessageObject.MESSAGE_SEND_STATE_SENT;
                                        if (isBroadcast) {
                                            for (TLRPC.Message message : sentMessages) {
                                                ArrayList<MessageObject> arr = new ArrayList<>();
                                                MessageObject messageObject = new MessageObject(message, null, false);
                                                arr.add(messageObject);
                                                MessagesController.getInstance().updateInterfaceWithMessages(messageObject.getDialogId(), arr, true);
                                            }
                                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.dialogsNeedReload);
                                        }
                                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageReceivedByServer, oldId, (isBroadcast ? oldId : newMsgObj.id), newMsgObj, newMsgObj.dialog_id);
                                        processSentMessage(oldId);
                                        removeFromSendingMessages(oldId);
                                    }
                                });
                                if (newMsgObj.media instanceof TLRPC.TL_messageMediaVideo_layer45) {
                                   // stopVideoService(attachPath);
                                }
                            }
                        });
                    }
                } else {
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            if (error.text.equals("PEER_FLOOD")) {
                                NotificationCenter.getInstance().postNotificationName(NotificationCenter.spamErrorReceived, 0);
                            }
                        }
                    });
                    isSentError = true;
                }
                if (isSentError) {
                    MessagesStorage.getInstance().markMessageAsSendError(newMsgObj);
                    AndroidUtilities.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            newMsgObj.send_state = MessageObject.MESSAGE_SEND_STATE_SEND_ERROR;
                            NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageSendError, newMsgObj.id);
                            processSentMessage(newMsgObj.id);
                            if (newMsgObj.media instanceof TLRPC.TL_messageMediaVideo_layer45) {
                                //stopVideoService(newMsgObj.attachPath);
                            }
                            removeFromSendingMessages(newMsgObj.id);
                        }
                    });
                }
            }
        }, new QuickAckDelegate() {
            @Override
            public void run() {
                final int msg_id = newMsgObj.id;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        newMsgObj.send_state = MessageObject.MESSAGE_SEND_STATE_SENT;
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.messageReceivedByAck, msg_id);
                    }
                });
            }
        }, ConnectionsManager.RequestFlagCanCompress | ConnectionsManager.RequestFlagInvokeAfter | (req instanceof TLRPC.TL_messages_sendMessage ? ConnectionsManager.RequestFlagNeedQuickAck : 0));
    }


    protected void putToSendingMessages(TLRPC.Message message) {
        sendingMessages.put(message.id, message);
    }

    protected void removeFromSendingMessages(int mid) {
        sendingMessages.remove(mid);
    }

    protected long getNextRandomId() {
        long val = 0;
        while (val == 0) {
            val = Utilities.random.nextLong();
        }
        return val;
    }
    private void processSentMessage(TLRPC.Message newMsg, TLRPC.Message sentMessage, String originalPath) {
        if (sentMessage == null) {
            return;
        }
        if (sentMessage.media instanceof TLRPC.TL_messageMediaPhoto && sentMessage.media.photo != null && newMsg.media instanceof TLRPC.TL_messageMediaPhoto && newMsg.media.photo != null) {
            MessagesStorage.getInstance().putSentFile(originalPath, sentMessage.media.photo, 0);

            for (TLRPC.PhotoSize size : sentMessage.media.photo.sizes) {
                if (size == null || size instanceof TLRPC.TL_photoSizeEmpty || size.type == null) {
                    continue;
                }
                for (TLRPC.PhotoSize size2 : newMsg.media.photo.sizes) {
                    if (size2 == null || size2.location == null || size2.type == null) {
                        continue;
                    }
                    if (size2.location.volume_id == Integer.MIN_VALUE && size.type.equals(size2.type) || size.w == size2.w && size.h == size2.h) {
                        String fileName = size2.location.volume_id + "_" + size2.location.local_id;
                        String fileName2 = size.location.volume_id + "_" + size.location.local_id;
                        if (fileName.equals(fileName2)) {
                            break;
                        }
                        File cacheFile = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName + ".jpg");
                        File cacheFile2;
                        if (sentMessage.media.photo.sizes.size() == 1 || size.w > 90 || size.h > 90) {
                            cacheFile2 = FileLoader.getPathToAttach(size);
                        } else {
                            cacheFile2 = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName2 + ".jpg");
                        }
                        cacheFile.renameTo(cacheFile2);
                        ImageLoader.getInstance().replaceImageInCache(fileName.toString(), fileName2.toString(), size.location,false);
                        size2.location = size.location;
                        break;
                    }
                }
            }
            sentMessage.message = newMsg.message;
            sentMessage.attachPath = newMsg.attachPath;
            newMsg.media.photo.id = sentMessage.media.photo.id;
            newMsg.media.photo.access_hash = sentMessage.media.photo.access_hash;
        } else if (sentMessage.media instanceof TLRPC.TL_messageMediaVideo_layer45 && sentMessage.media.video_unused != null && newMsg.media instanceof TLRPC.TL_messageMediaVideo_layer45 && newMsg.media.video_unused != null) {
            MessagesStorage.getInstance().putSentFile(originalPath, sentMessage.media.video_unused, 2);

            TLRPC.PhotoSize size2 = newMsg.media.video_unused.thumb;
            TLRPC.PhotoSize size = sentMessage.media.video_unused.thumb;
            if (size2.location != null && size2.location.volume_id == Integer.MIN_VALUE && size.location != null && !(size instanceof TLRPC.TL_photoSizeEmpty) && !(size2 instanceof TLRPC.TL_photoSizeEmpty)) {
                String fileName = size2.location.volume_id + "_" + size2.location.local_id;
                String fileName2 = size.location.volume_id + "_" + size.location.local_id;
                if (!fileName.equals(fileName2)) {
                    File cacheFile = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName + ".jpg");
                    File cacheFile2 = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName2 + ".jpg");
                    cacheFile.renameTo(cacheFile2);
                    ImageLoader.getInstance().replaceImageInCache(fileName, fileName2, size.location,false);
                    size2.location = size.location;
                }
            }

            sentMessage.message = newMsg.message;
            newMsg.media.video_unused.dc_id = sentMessage.media.video_unused.dc_id;
            newMsg.media.video_unused.id = sentMessage.media.video_unused.id;
            newMsg.media.video_unused.access_hash = sentMessage.media.video_unused.access_hash;

            if (newMsg.attachPath != null && newMsg.attachPath.startsWith(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE).getAbsolutePath())) {
                File cacheFile = new File(newMsg.attachPath);
                File cacheFile2 = FileLoader.getPathToAttach(newMsg.media.video_unused);
                if (!cacheFile.renameTo(cacheFile2)) {
                    sentMessage.attachPath = newMsg.attachPath;
                }
            } else {
                sentMessage.attachPath = newMsg.attachPath;
            }
        } else if (sentMessage.media instanceof TLRPC.TL_messageMediaDocument && sentMessage.media.document != null && newMsg.media instanceof TLRPC.TL_messageMediaDocument && newMsg.media.document != null) {
            MessagesStorage.getInstance().putSentFile(originalPath, sentMessage.media.document, 1);

            TLRPC.PhotoSize size2 = newMsg.media.document.thumb;
            TLRPC.PhotoSize size = sentMessage.media.document.thumb;
            if (size2.location != null && size2.location.volume_id == Integer.MIN_VALUE && size.location != null && !(size instanceof TLRPC.TL_photoSizeEmpty) && !(size2 instanceof TLRPC.TL_photoSizeEmpty)) {
                String fileName = size2.location.volume_id + "_" + size2.location.local_id;
                String fileName2 = size.location.volume_id + "_" + size.location.local_id;
                if (!fileName.equals(fileName2)) {
                    File cacheFile = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName + ".jpg");
                    File cacheFile2 = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName2 + ".jpg");
                    cacheFile.renameTo(cacheFile2);
                    ImageLoader.getInstance().replaceImageInCache(fileName, fileName2, size.location,false);
                    size2.location = size.location;
                }
            } else if (MessageObject.isStickerMessage(sentMessage) && size2.location != null) {
                size.location = size2.location;
            }

            newMsg.media.document.dc_id = sentMessage.media.document.dc_id;
            newMsg.media.document.id = sentMessage.media.document.id;
            newMsg.media.document.access_hash = sentMessage.media.document.access_hash;
            newMsg.media.document.attributes = sentMessage.media.document.attributes;

            if (newMsg.attachPath != null && newMsg.attachPath.startsWith(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE).getAbsolutePath())) {
                File cacheFile = new File(newMsg.attachPath);
                File cacheFile2 = FileLoader.getPathToAttach(sentMessage.media.document);
                if (!cacheFile.renameTo(cacheFile2)) {
                    sentMessage.attachPath = newMsg.attachPath;
                    sentMessage.message = newMsg.message;
                } else {
                    newMsg.attachPath = "";
                    if (originalPath != null && originalPath.startsWith("http")) {
                        MessagesStorage.getInstance().addRecentLocalFile(originalPath, cacheFile2.toString(),null);
                    }
                }
            } else {
                sentMessage.attachPath = newMsg.attachPath;
                sentMessage.message = newMsg.message;
            }
        } else if (sentMessage.media instanceof TLRPC.TL_messageMediaAudio_layer45 && sentMessage.media.audio_unused != null && newMsg.media instanceof TLRPC.TL_messageMediaAudio_layer45 && newMsg.media.audio_unused != null) {
            sentMessage.message = newMsg.message;

            String fileName = newMsg.media.audio_unused.dc_id + "_" + newMsg.media.audio_unused.id + ".ogg";
            newMsg.media.audio_unused.dc_id = sentMessage.media.audio_unused.dc_id;
            newMsg.media.audio_unused.id = sentMessage.media.audio_unused.id;
            newMsg.media.audio_unused.access_hash = sentMessage.media.audio_unused.access_hash;
            String fileName2 = sentMessage.media.audio_unused.dc_id + "_" + sentMessage.media.audio_unused.id + ".ogg";
            if (!fileName.equals(fileName2)) {
                File cacheFile = new File(FileLoader.getInstance().getDirectory(FileLoader.MEDIA_DIR_CACHE), fileName);
                File cacheFile2 = FileLoader.getPathToAttach(sentMessage.media.audio_unused);
                if (!cacheFile.renameTo(cacheFile2)) {
                    sentMessage.attachPath = newMsg.attachPath;
                }
            }
        } else if (sentMessage.media instanceof TLRPC.TL_messageMediaContact && newMsg.media instanceof TLRPC.TL_messageMediaContact) {
            newMsg.media = sentMessage.media;
        }
    }

    protected void processSentMessage(int id) {
        int prevSize = unsentMessages.size();
        unsentMessages.remove(id);
        if (prevSize != 0 && unsentMessages.size() == 0) {
            checkUnsentMessages();
        }
    }

    public void checkUnsentMessages() {
        MessagesStorage.getInstance().getUnsentMessages(1000);
    }

    private String getTrimmedString(String src) {
        String result = src.trim();
        if (result.length() == 0) {
            return result;
        }
        while (src.startsWith("\n")) {
            src = src.substring(1);
        }
        while (src.endsWith("\n")) {
            src = src.substring(0, src.length() - 1);
        }
        return src;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class AudioStreamReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(AudioManager.ACTION_HEADSET_PLUG)){
                Log.d(TAG, "ACTION_HEADSET_PLUG " + intent.getIntExtra("state", -1));
            } else if (intent.getAction().equals(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)){
                Log.d(TAG, "ACTION_SCO_AUDIO_STATE_UPDATED " + intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -2));
            }

            if (intent.getIntExtra("state", -1) == 0 /*|| intent.getIntExtra("EXTRA_SCO_AUDIO_STATE", -1) == 0*/){
                dynamicToggleVideoCall.setBackgroundResource(R.drawable.ic_dynamic_off);
            } else if (intent.getIntExtra("state", -1) == 1) {
                dynamicToggleVideoCall.setBackgroundResource(R.drawable.ic_dynamic_on);
            }



            //dynamicToggleVideoCall.invalidate();
        }
    }
}

