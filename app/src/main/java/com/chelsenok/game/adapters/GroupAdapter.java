package com.chelsenok.game.adapters;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chelsenok.game.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private GroupAdapter.ViewHolder mConnectingViewHolder;
    private final List<WifiP2pDevice> mDevices;
    private final View.OnClickListener mListener;
    private static final Map<Integer, String> mStatusStringMap = new HashMap<>();

    static {
        mStatusStringMap.put(0, "Connected");
        mStatusStringMap.put(1, "Invited");
        mStatusStringMap.put(2, "Failed");
        mStatusStringMap.put(3, "Available");
        mStatusStringMap.put(4, "Unavailable");
    }

    public GroupAdapter(@Nullable View.OnClickListener listener) {
        mDevices = new ArrayList<>();
        if (listener == null) {
            listener = v -> {
            };
        }
        mListener = listener;
    }

    public void add(final WifiP2pDevice device) {
        if ((device.status == WifiP2pDevice.AVAILABLE || device.status == WifiP2pDevice.CONNECTED)
                && !contains(device)) {
            mDevices.add(device);
            notifyDataSetChanged();
        }
    }

    public void onFailure() {
        remove(mConnectingViewHolder.getWifiP2pDevice());
        mConnectingViewHolder = null;
    }

    private boolean contains(final WifiP2pDevice device) {
        for (final WifiP2pDevice p2pDevice :
                mDevices) {
            if (device.deviceAddress.equals(p2pDevice.deviceAddress)) {
                return true;
            }
        }
        return false;
    }

    public void remove(final WifiP2pDevice device) {
        for (final WifiP2pDevice p2pDevice :
                mDevices) {
            if (p2pDevice.deviceAddress.equals(device.deviceAddress)) {
                mDevices.remove(p2pDevice);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public boolean isEmpty() {
        return mDevices.isEmpty();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private WifiP2pDevice wifiP2pDevice;
        private final TextView deviceNameTextView;
        private final TextView deviceStatusTextView;

        private final View itemView;

        public ViewHolder(final View itemView) {
            super(itemView);
            this.itemView = itemView;
            deviceNameTextView = (TextView) itemView.findViewById(R.id.tv_name);
            deviceStatusTextView = (TextView) itemView.findViewById(R.id.tv_status);
        }

        public void setWifiP2pDevice(final WifiP2pDevice wifiP2pDevice) {
            this.wifiP2pDevice = wifiP2pDevice;
        }

        public void setOnClickListener(final View.OnClickListener onClickListener) {
            itemView.setOnClickListener(onClickListener);
        }

        public void setName(final CharSequence name) {
            this.deviceNameTextView.setText(name);
        }

        public void setStatus(final CharSequence status) {
            this.deviceStatusTextView.setText(status);
        }

        public WifiP2pDevice getWifiP2pDevice() {
            return wifiP2pDevice;
        }

    }

    @Override
    public GroupAdapter.ViewHolder onCreateViewHolder(final ViewGroup parent,
                                                      final int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);
        final View wifiP2pDeviceView = inflater.inflate(R.layout.item_wifi_group, parent, false);
        final GroupAdapter.ViewHolder viewHolder = new ViewHolder(wifiP2pDeviceView);
        viewHolder.setOnClickListener(v -> {
            if (mConnectingViewHolder == null) {
                mListener.onClick(v);
                viewHolder.setStatus("CONNECTING");
                mConnectingViewHolder = viewHolder;
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final GroupAdapter.ViewHolder viewHolder, final int position) {
        final WifiP2pDevice wifiP2pDevice = mDevices.get(position);
        viewHolder.setWifiP2pDevice(wifiP2pDevice);
        viewHolder.setName(wifiP2pDevice.deviceName);
        viewHolder.setStatus(mStatusStringMap.get(wifiP2pDevice.status));
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }
}