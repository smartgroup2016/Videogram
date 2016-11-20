package org.telegram.quickBlox.fragments;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.quickBlox.activities.CallActivity1;
import org.telegram.ui.ProfileActivity;

import org.telegram.messenger.R;

/**
 * Created by tereha on 15.07.15.
 */
public class AudioConversationFragment extends BaseConversationFragment {
    public static Notification notification1;
    public static NotificationManager notificationManager2;
    private ImageView avatarview;

    @Override
    protected int getContentView() {
        return R.layout.fragment_conversation_base;
    }

    /* @Override
     protected void initViews(View view){
         avatarview = (ImageView) view.findViewById(R.id.avatarview);
         File f = ProfileActivity.user_photo_big;
         if(f!=null) {
             Uri uri = Uri.fromFile(f);
             avatarview.setImageURI(uri);
         }
     }*/
    @Override
    public void onResume(){

        super.onResume();
        initNotification();
    }

    public void initNotification(){

        Context context = ApplicationLoader.applicationContext.getApplicationContext();

        Intent notificationIntent = new Intent(context, CallActivity1.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);
        String tmp = null;
        if (ProfileActivity.userName != null){
            tmp =ProfileActivity.userName;
        } else if(IncomeCallFragment.callName !=null){
            tmp =IncomeCallFragment.callName;
        }

        String talk = getResources().getString(R.string.talkwith);
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_call)
                        // большая картинка
                .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_launcher))
                        //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                        //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                .setContentTitle("TelegramPro")
                        //.setContentText(res.getString(R.string.notifytext))
                .setContentText(talk+tmp); // Текст уведомления

        notification1 = builder.getNotification(); // до API 16
        //Notification notification = builder.build();

        notificationManager2 = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notification1.flags = notification1.flags | Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
        notificationManager2.notify(IncomeCallFragment.NOTIFY_ID + 2, notification1);


    }

}
