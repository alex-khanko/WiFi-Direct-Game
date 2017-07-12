package com.chelsenok.wifidirect;

public class Member {

    private final String mWifiP2pDeviceAddress;
    private ChatNeedle mChatNeedle;

    Member(final String wifiP2pDeviceAddress, final ChatNeedle chatNeedle) {
        mWifiP2pDeviceAddress = wifiP2pDeviceAddress;
        mChatNeedle = chatNeedle;
    }

    public Member(final Member member) {
        mWifiP2pDeviceAddress = member.getDeviceAddress();
        mChatNeedle = member.getDeviceChatNeedle();
    }

    String getDeviceAddress() {
        return mWifiP2pDeviceAddress;
    }

    ChatNeedle getDeviceChatNeedle() {
        return mChatNeedle;
    }

    void updateChatNeedle(final ChatNeedle deviceChatNeedle) {
        mChatNeedle = deviceChatNeedle;
    }
}
