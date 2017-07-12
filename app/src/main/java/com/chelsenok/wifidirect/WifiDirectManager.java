package com.chelsenok.wifidirect;

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;

import static com.chelsenok.wifidirect.Constants.CHAT_NEEDLE;
import static com.chelsenok.wifidirect.Constants.PEER;

public final class WifiDirectManager {

    private static WifiP2pManager.ActionListener sActivityActionListener;
    private static WifiP2pDeviceObservable sDeviceObservable;
    private static WifiDirectManager sWifiDirectManager;
    private static Handler sHandler;

    private final String SERVICE_INSTANCE = "WifiDirectService";
    private final WifiP2pManager.Channel mChannel;
    private final IntentFilter mIntentFilter;
    private final WifiDirectBroadcastReceiver mWifiDirectBroadcastReceiver;
    private final WifiP2pManager mManager;
    private final int MAX_NET_ID = 32;
    private final int PERIOD = 5000;
    private Status mStatus;

    private final String MAC_ADDRESS;
    private WifiP2pDnsSdServiceRequest mServiceRequest;
    private WifiP2pDnsSdServiceInfo mServiceInfo;
    private final MemberList gMemberList;
    private ChatNeedle mChatNeedle;
    private String mCurrentConnectingDeviceAddress;
    private final String mRealWifiDirectName;
    private final Handler mServiceDiscoveringHandler;
    private final Runnable mServiceDiscoveringRunnable = this::startSearching;
    private final Handler mServiceBroadcastingHandler;
    private final Runnable mServiceBroadcastingRunnable = new Runnable() {

        @Override
        public void run() {
            mManager.discoverPeers(mChannel, null);
            mServiceBroadcastingHandler
                    .postDelayed(mServiceBroadcastingRunnable, PERIOD);
        }
    };

    private WifiDirectManager(final Context context) {
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        MAC_ADDRESS = (((WifiManager) context.getSystemService(Context.WIFI_SERVICE)))
                .getConnectionInfo()
                .getMacAddress();
        mServiceDiscoveringHandler = new Handler();
        mServiceBroadcastingHandler = new Handler();
        mChannel = mManager.initialize(context, context.getMainLooper(), null);
        gMemberList = MemberList.getInstance();
        mRealWifiDirectName = getDeviceName();
        mWifiDirectBroadcastReceiver = new WifiDirectBroadcastReceiver(
                mManager,
                mChannel,
                info -> {
                    final Handler handler = getHandler();
                    if (info.isGroupOwner) {
                        try {
                            new GroupOwnerSocketHandler(handler).start();
                        } catch (final IOException e) {
                            e.printStackTrace();
                            sActivityActionListener.onFailure(e.hashCode());
                            return;
                        }
                    } else {
                        new ClientSocketHandler(handler, info.groupOwnerAddress).start();
                    }
                    sActivityActionListener.onSuccess();
                }
        );
    }

    public static synchronized WifiDirectManager getInstance(final Context context) {
        if (sWifiDirectManager == null) {
            sWifiDirectManager = new WifiDirectManager(context);
        }
        sDeviceObservable = getDeviceObservable(null);
        sHandler = getHandler(null);
        sActivityActionListener = getActionListener(null);
        return sWifiDirectManager;
    }

    public void updateHandler(@Nullable final Handler handler) {
        sHandler = getHandler(handler);
    }

    public void updateWifiP2pDeviceObservable(@Nullable final WifiP2pDeviceObservable observable) {
        sDeviceObservable = getDeviceObservable(observable);
    }

    public void updateJoinListener(@Nullable final WifiP2pManager.ActionListener onJoinListener) {
        sActivityActionListener = getActionListener(onJoinListener);
    }

    private static WifiP2pManager.ActionListener getActionListener(
            final WifiP2pManager.ActionListener actionListener
    ) {
        if (actionListener == null) {
            return new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFailure(final int reason) {
                }
            };
        } else {
            return actionListener;
        }
    }

    public static WifiP2pDeviceObservable getDeviceObservable(final WifiP2pDeviceObservable observable) {
        if (observable == null) {
            return wifiP2pDevice -> {
            };
        } else {
            return observable;
        }
    }

    private static Handler getHandler(final Handler handler) {
        if (handler == null) {
            return new Handler();
        } else {
            return handler;
        }
    }

    public WifiDirectBroadcastReceiver getWifiDirectBroadcastReceiver() {
        return mWifiDirectBroadcastReceiver;
    }

    public IntentFilter getIntentFilter() {
        return mIntentFilter;
    }

    public String getDeviceName() {
        return android.os.Build.MODEL;
    }

    public void formGroup() {
        // TODO: 5/28/17 notify others about that action
        setWifiDirectName(mRealWifiDirectName);
        mStatus = Status.GroupOwner;
        stopLocalService();
    }

    public void stopDiscovery() {
        mManager.removeServiceRequest(mChannel, mServiceRequest, null);
        mServiceDiscoveringHandler.removeCallbacks(mServiceDiscoveringRunnable);
    }

    private void clearRememberedGroups(final WifiP2pManager manager, final WifiP2pManager.Channel channel) {
        try {
            final Method[] methods = WifiP2pManager.class.getMethods();
            for (final Method method : methods) {
                if ("deletePersistentGroup".equals(method.getName())) {
                    for (int netid = 0; netid < MAX_NET_ID; netid++) {
                        method.invoke(manager, channel, netid, null);
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isWifiDirectSupported(final Context context) {
        final PackageManager pm = context.getPackageManager();
        final FeatureInfo[] features = pm.getSystemAvailableFeatures();
        for (final FeatureInfo info : features) {
            if (info != null && info.name != null
                    && "android.hardware.wifi.direct".equalsIgnoreCase(info.name)) {
                return true;
            }
        }
        return false;
    }

    public void startDiscovery() {
        mManager.setDnsSdResponseListeners(
                mChannel,
                (instanceName, registrationType, srcDevice) -> {
                    if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
                        sDeviceObservable.notifyObserver(srcDevice);
                    }
                },
                null
        );
        mServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        startSearching();
    }

    private void startSearching() {
        mManager.removeServiceRequest(
                mChannel,
                mServiceRequest,
                new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        mManager.addServiceRequest(
                                mChannel,
                                mServiceRequest,
                                new WifiP2pManager.ActionListener() {

                                    @Override
                                    public void onSuccess() {
                                        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

                                            @Override
                                            public void onSuccess() {
                                                mServiceDiscoveringHandler.postDelayed(
                                                        mServiceDiscoveringRunnable,
                                                        PERIOD
                                                );
                                            }

                                            @Override
                                            public void onFailure(final int error) {
                                                // react to failure of starting service discovery
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(final int reason) {
                                    }
                                }
                        );
                    }

                    @Override
                    public void onFailure(final int reason) {
                    }
                }
        );
    }

    public void startLocalService() {
        createGroup(getDeviceName());
    }

    public void stopLocalService() {
        mManager.clearLocalServices(mChannel, null);
        mServiceBroadcastingHandler.removeCallbacks(mServiceBroadcastingRunnable);
    }

    public void createGroup(final String name) {
        mStatus = Status.GroupOwner;
        mManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                setWifiDirectName(name);
                mServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(
                        SERVICE_INSTANCE, "_http._tcp", null
                );
                mManager.addLocalService(mChannel, mServiceInfo, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        mServiceBroadcastingHandler.postDelayed(mServiceBroadcastingRunnable, PERIOD);
                    }

                    @Override
                    public void onFailure(final int reason) {

                    }
                });
            }

            @Override
            public void onFailure(final int reason) {

            }
        });
    }

    public static boolean setWifiEnabled(final Context context, final boolean state) {
        return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).setWifiEnabled(state);
    }

    public static boolean getWifiEnabled(final Context context) {
        try {
            return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getWifiState()
                    == WifiManager.WIFI_STATE_ENABLED;
        } catch (final Exception e) {
            return true;
        }
    }

    public void invite(final WifiP2pDevice device) {
        mStatus = Status.GroupOwner;
        connect(device.deviceAddress, mStatus);
    }

    public void join(final WifiP2pDevice groupOwnerDevice) {
        mStatus = Status.Client;
        connect(groupOwnerDevice.deviceAddress, mStatus);
    }

    private void connect(final String deviceAddress, final Status status) {
        mCurrentConnectingDeviceAddress = deviceAddress;
        if (mServiceRequest != null) {
            mManager.removeServiceRequest(mChannel, mServiceRequest, null);
        }
        final WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = status.getIntent();
        mManager.connect(mChannel, config, null);
    }

    private void disconnect() {
        mManager.cancelConnect(mChannel, null);
        mManager.clearLocalServices(mChannel, null);
        mManager.clearServiceRequests(mChannel, null);
        if (mServiceInfo != null) {
            mManager.removeLocalService(mChannel, mServiceInfo, null);
        }
    }

    public void sendMessage(final byte[] recycle) {
        for (final ChatNeedle chatNeedle :
                gMemberList.getChatNeedles()) {
            chatNeedle.write(recycle);
        }
    }

    private void setWifiDirectName(final String deviceName) {
        try {
            final Class[] paramTypes = new Class[3];
            paramTypes[0] = WifiP2pManager.Channel.class;
            paramTypes[1] = String.class;
            paramTypes[2] = WifiP2pManager.ActionListener.class;
            final Method setDeviceName = mManager.getClass().getMethod(
                    "setDeviceName", paramTypes);
            setDeviceName.setAccessible(true);

            final Object[] arglist = new Object[3];
            arglist[0] = mChannel;
            arglist[1] = deviceName;
            setDeviceName.invoke(mManager, arglist);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private Handler getHandler() {
        return new Handler(
                msg -> {
                    if (msg.what == CHAT_NEEDLE) {
                        mChatNeedle = (ChatNeedle) msg.obj;
                        if (mChatNeedle != null) {
                            gMemberList.add(new Member(mCurrentConnectingDeviceAddress, mChatNeedle));
                            mChatNeedle.write(MessageShaper.recycle(0, PEER, MAC_ADDRESS));
                        }
                    } else {
                        final byte[] obj = (byte[]) msg.obj;
                        final Message formedMessage = MessageShaper.getMessage(obj);
                        switch (formedMessage.what) {
                            case PEER:
                                final String address = (String) formedMessage.obj;
                                gMemberList.add(new Member(address, mChatNeedle));
                                if (mStatus == Status.GroupOwner) {
                                    final WifiP2pDevice p2pDevice = new WifiP2pDevice();
                                    p2pDevice.deviceAddress = address;
                                    p2pDevice.status = WifiP2pDevice.CONNECTED;
                                    p2pDevice.deviceName = String.valueOf(System.currentTimeMillis());
                                    sDeviceObservable.notifyObserver(p2pDevice);
                                }
                                break;
                            default:
                                sHandler.sendMessage(formedMessage);
                                if (mStatus == Status.GroupOwner) {
                                    sendMessage(obj);
                                }
                        }
                    }
                    return false;
                }
        );
    }
}
