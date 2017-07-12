package com.chelsenok.wifidirect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private final WifiP2pManager manager;
    private final Channel channel;
    private final ConnectionInfoListener infoListener;

    public WifiDirectBroadcastReceiver(final WifiP2pManager manager,
                                       final Channel channel,
                                       final ConnectionInfoListener infoListener) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.infoListener = infoListener;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager != null) {
                final NetworkInfo networkInfo = intent
                        .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    manager.requestConnectionInfo(channel, infoListener);
                } else {
                    // It's a disconnect
                }
            }
        }
    }
}
