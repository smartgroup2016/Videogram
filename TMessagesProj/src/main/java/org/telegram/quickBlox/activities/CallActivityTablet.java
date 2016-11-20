package org.telegram.quickBlox.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.quickblox.chat.QBChatService;

import org.telegram.messenger.R;
import org.telegram.quickBlox.definitions.Consts;
import org.telegram.quickBlox.services.IncomeCallListenerService;
import org.telegram.ui.LaunchActivity;

/**
 * Created by radga on 12.11.2015.
 */
public class CallActivityTablet extends Activity {
    private QBChatService chatService;

    String login =LaunchActivity.logQBlox;
    String password = LaunchActivity.passQBlox;
    Button btn;
    private boolean isWifiConnected;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        initProgressDialog();
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.test_call);

        if (QBChatService.isInitialized() && QBChatService.getInstance().isLoggedIn()) {
            startOpponentsActivity();
            progressDialog.cancel();
            finish();
        } else {
            //Fabric.with(this, new Crashlytics());
            progressDialog.cancel();
            startIncomeCallListenerService(login, password, Consts.LOGIN);
        }
    }
    private void startOpponentsActivity(){
        Intent intent = new Intent(this, OpponentActivityTablet.class);
        startActivity(intent);
    }

    public void startIncomeCallListenerService(String login, String password, int startServiceVariant){

        Intent tempIntent = new Intent(this, IncomeCallListenerService.class);
        PendingIntent pendingIntent = createPendingResult(Consts.LOGIN_TASK_CODE, tempIntent, 0);
        Intent intent = new Intent(this, IncomeCallListenerService.class);
        intent.putExtra(Consts.USER_LOGIN, login);
        intent.putExtra(Consts.USER_PASSWORD, password);
        intent.putExtra(Consts.START_SERVICE_VARIANT, startServiceVariant);
        intent.putExtra(Consts.PARAM_PINTENT, pendingIntent);

        startService(intent);
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this) {
            @Override
            public void onBackPressed() {
                Toast.makeText(CallActivityTablet.this, getString(R.string.wait_until_loading_finish), Toast.LENGTH_SHORT).show();
            }
        };
        progressDialog.setMessage(getString(R.string.load_opponents));
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }


}