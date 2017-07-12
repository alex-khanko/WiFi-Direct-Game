package com.chelsenok.game.activities.group;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;

import com.chelsenok.game.R;
import com.chelsenok.game.activities.ChatActivity;

public class CreateGroupActivity extends GroupActivity {

    private Button mCancelButton;
    private Button mFinishButton;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        mPeersAdapter = getPeersAdapter(null);
        mPeersRecyclerView = getPeersRecyclerView(mPeersAdapter);
        mCancelButton = getCancelButton();
        mFinishButton = getFinishButton();
        mWifiDirectManager.updateWifiP2pDeviceObservable(wifiP2pDevice -> mPeersAdapter.add(wifiP2pDevice));
        mWifiDirectManager.createGroup(mWifiDirectManager.getDeviceName());
    }

    private Button getCancelButton() {
        final Button button = (Button) findViewById(R.id.button_cancel);
        button.setOnClickListener(v -> {
            mWifiDirectManager.formGroup();
        });
        return button;
    }

    private Button getFinishButton() {
        final Button button = (Button) findViewById(R.id.button_finish);
        button.setOnClickListener(v -> {
            if (!mPeersAdapter.isEmpty()) {
                mWifiDirectManager.formGroup();
                mWifiDirectManager.updateJoinListener(null);
                startActivity(new Intent(getActivity(), ChatActivity.class));
                finish();
            }
        });
        return button;
    }
}
