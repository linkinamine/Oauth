package com.befriender.lib.listener;

import com.befriender.lib.listener.base.SocialNetworkListener;

public interface OnRequestAddFriendCompleteListener extends SocialNetworkListener {
    public void onRequestAddFriendComplete(int socialNetworkID, String userID);
}
