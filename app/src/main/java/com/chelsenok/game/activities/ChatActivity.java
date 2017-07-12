package com.chelsenok.game.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.widget.Button;

import com.chelsenok.game.R;
import com.chelsenok.wifidirect.MessageShaper;
import com.chelsenok.wifidirect.WifiDirectManager;

import java.util.Random;

import static com.chelsenok.game.communication.AccessTypes.BROADCAST;
import static com.chelsenok.game.communication.ContentTypes.COLOR_UPDATER;

public class ChatActivity extends Activity implements Handler.Callback {

    private Button mButtonColor;
    private WifiDirectManager gWifiDirectManager;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        gWifiDirectManager = WifiDirectManager.getInstance(this);
        gWifiDirectManager.updateHandler(new Handler(this));
        mButtonColor = getButtonColor();
    }

    private Button getButtonColor() {
        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(v -> {
            final Integer color = new Random().nextInt();
            updateColor(color);
            gWifiDirectManager.sendMessage(MessageShaper.recycle(BROADCAST, COLOR_UPDATER, color));
        });
        return button;
    }

    @Override
    public boolean handleMessage(final Message msg) {
        switch (msg.what) {
            case COLOR_UPDATER:
                updateColor((Integer) msg.obj);
                break;
        }
        return false;
    }

    private void updateColor(final int color) {
        mButtonColor.setBackgroundColor(color);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(gWifiDirectManager.getWifiDirectBroadcastReceiver(),
                gWifiDirectManager.getIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(gWifiDirectManager.getWifiDirectBroadcastReceiver());
    }
}
