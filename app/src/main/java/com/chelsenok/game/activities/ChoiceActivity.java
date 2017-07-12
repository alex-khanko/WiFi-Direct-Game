package com.chelsenok.game.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.chelsenok.game.R;
import com.chelsenok.game.activities.group.CreateGroupActivity;
import com.chelsenok.game.activities.group.JoinGroupActivity;
import com.chelsenok.wifidirect.WifiDirectManager;

public class ChoiceActivity extends Activity {

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choice);
        if (!WifiDirectManager.isWifiDirectSupported(this)) {
            showErrorAlertDialog();
        } else {
            showWifiDirectSupported();
        }
    }

    public void onJoinClick(final View view) {
        startGroupActivity(JoinGroupActivity.class);
    }

    public void onCreateGroupClick(final View view) {
        startGroupActivity(CreateGroupActivity.class);
    }

    private void startGroupActivity(final Class cls) {
        startActivity(new Intent(this, cls));
        finish();
    }

    private void showWifiDirectSupported() {
        Toast.makeText(this,
                "WiFi Direct is supported", Toast.LENGTH_SHORT).show();
    }

    private void showErrorAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("Wifi-Direct is not supported on your device\n" +
                        "Wait for online version of the product :)")
                .setCancelable(false)
                .setNegativeButton("Quit", (dialog, which) -> finish())
                .create()
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
