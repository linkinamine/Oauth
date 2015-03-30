package com.befriender.lib.listener;

import com.befriender.lib.listener.base.SocialNetworkListener;

public interface OnCheckIsFriendCompleteListener extends SocialNetworkListener {
    public void onCheckIsFriendComplete(int socialNetworkID, String userID, boolean isFriend);
}
