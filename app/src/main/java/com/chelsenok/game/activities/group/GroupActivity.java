package com.chelsenok.game.activities.group;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.chelsenok.game.R;
import com.chelsenok.game.adapters.GroupAdapter;
import com.chelsenok.game.ui.DividerItemDecoration;
import com.chelsenok.wifidirect.WifiDirectManager;

public class GroupActivity extends Activity {

    protected GroupAdapter mPeersAdapter;
    protected RecyclerView mPeersRecyclerView;
    protected WifiDirectManager mWifiDirectManager;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiDirectManager = WifiDirectManager.getInstance(this);
    }

    public GroupAdapter getPeersAdapter(@Nullable final View.OnClickListener listener) {
        final GroupAdapter adapter = new GroupAdapter(listener);
        return adapter;
    }

    public RecyclerView getPeersRecyclerView(final GroupAdapter adapter) {
        final RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        return recyclerView;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mWifiDirectManager.getWifiDirectBroadcastReceiver(),
                mWifiDirectManager.getIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mWifiDirectManager.getWifiDirectBroadcastReceiver());
    }

    protected GroupActivity getActivity() {
        return this;
    }
}
