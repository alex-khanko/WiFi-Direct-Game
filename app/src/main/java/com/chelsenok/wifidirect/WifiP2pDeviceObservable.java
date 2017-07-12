package com.chelsenok.wifidirect;

import android.net.wifi.p2p.WifiP2pDevice;

public interface WifiP2pDeviceObservable {

    void notifyObserver(WifiP2pDevice wifiP2pDevice);

}
