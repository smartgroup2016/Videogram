package org.telegram.quickBlox.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.telegram.messenger.ContactsController;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ProfileActivity;

/**
 * Created by radga on 19.11.2015.
 */
public class MyDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = "Невозможно выполнить звонок";
        String message = "К сожалению "+ ProfileActivity.userName+ " не использует TelegramPro! Но вы можете предложить этому пользователю скачать TelegramPro";
        String button1String = "Ок, вот козел!";
        String button2String = "Предложить TelegramPro";

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);  // заголовок
        builder.setMessage(message); // сообщение
        builder.setPositiveButton(button1String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
               getActivity().finish();
            }
        });
        builder.setNegativeButton(button2String, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, ContactsController.getInstance().getInviteText());
                    startActivityForResult(Intent.createChooser(intent, LocaleController.getString("InviteFriends", R.string.InviteFriends)), 500);
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
                getActivity().finish();
            }
        });
        builder.setCancelable(true);

        return builder.create();
    }
}
