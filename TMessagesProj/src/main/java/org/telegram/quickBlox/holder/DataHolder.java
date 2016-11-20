package org.telegram.quickBlox.holder;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DataHolder {

    public static ArrayList<QBUser> usersList;
    public static final String PASSWORD = "x6Bt0VDy5";
    public static String callerPhone;

    public static String contactName;
    public static String callName;


    public static String getUserNameByID(Integer callerID) {
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
                }else {callName = userTel.first_name;}

            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(List<String> list) {

            }
        });
        /*for (QBUser user : getUsersList()) {
            if (user.getId().equals(callerID)) {
                return user.getFullName();
            }
        }*/
        //return "User_name_unused";


         return callName;
    }

    public static String getContactName(String phoneNumber) {
        ContentResolver cr = ApplicationLoader.applicationContext.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }
        String contactName = null;
        if(cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }
    /*public static String getUserPhoneByID(Integer callerID){
        QBUsers user = new QBUser(callerID);
        return  user.getLogin();
    }*/
   /* public static String getUserNameByLogin(String login) {
        for (QBUser user : getUsersList()) {
            if (user.getLogin().equals(login)) {
                return user.getFullName();
            }
        }
        return "User_name_unused";
    }

    public static int getUserIndexByID(Integer callerID) {
        for (QBUser user : getUsersList()) {
            if (user.getId().equals(callerID)) {
                return usersList.indexOf(user);
            }
        }
        return -1;
    }

    public static ArrayList<Integer> getIdsAiiUsers (){
        ArrayList<Integer> ids = new ArrayList<>();
        for (QBUser user : getUsersList()){
            ids.add(user.getId());
        }
        return ids;
    }*/
}
