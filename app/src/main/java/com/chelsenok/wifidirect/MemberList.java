package com.chelsenok.wifidirect;

import java.util.ArrayList;
import java.util.List;

final class MemberList {

    private final List<Member> mMembers;
    private static MemberList ourInstance;

    public static MemberList getInstance() {
        if (ourInstance == null) {
            ourInstance = new MemberList();
        }
        return ourInstance;
    }

    private MemberList() {
        mMembers = new ArrayList<>();
    }

    List<ChatNeedle> getChatNeedles() {
        final List<ChatNeedle> chatNeedles = new ArrayList<>();
        for (final Member member :
                mMembers) {
            chatNeedles.add(member.getDeviceChatNeedle());
        }
        return chatNeedles;
    }

    private Member getMember(final String deviceAddress) {
        for (final Member member :
                mMembers) {
            if (member.getDeviceAddress().equals(deviceAddress)) {
                return member;
            }
        }
        return null;
    }

    void add(final Member member) {
        final String deviceAddress = member.getDeviceAddress();
        if (deviceAddress != null) {
            final Member existingMember = getMember(deviceAddress);
            if (existingMember != null) {
                existingMember.updateChatNeedle(member.getDeviceChatNeedle());
            } else {
                mMembers.add(member);
            }
        }
    }
}
