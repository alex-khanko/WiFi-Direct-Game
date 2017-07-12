package com.chelsenok.game.activities.group;

import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.chelsenok.game.R;
import com.chelsenok.game.activities.ChatActivity;
import com.chelsenok.game.adapters.GroupAdapter;

public class JoinGroupActivity extends GroupActivity {

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_group);
        mPeersAdapter = getPeersAdapter(v -> {
            final GroupAdapter.ViewHolder viewHolder =
                    (GroupAdapter.ViewHolder) mPeersRecyclerView.getChildViewHolder(v);
            mWifiDirectManager.join(viewHolder.getWifiP2pDevice());
        });
        mPeersRecyclerView = getPeersRecyclerView(mPeersAdapter);
        mWifiDirectManager.updateJoinListener(new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // TODO: 5/27/17 MembersActivity
                mWifiDirectManager.updateJoinListener(null);
                mWifiDirectManager.stopDiscovery();
                startActivity(new Intent(getActivity(), ChatActivity.class));
                finish();
            }

            @Override
            public void onFailure(final int reason) {
                mPeersAdapter.onFailure();
            }
        });
        mWifiDirectManager.updateWifiP2pDeviceObservable(
                wifiP2pDevice -> mPeersAdapter.add(wifiP2pDevice)
        );
        mWifiDirectManager.startDiscovery();
    }
}
