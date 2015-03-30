package com.befriender.lib.listener;

import com.befriender.lib.listener.base.SocialNetworkListener;

public interface OnRequestRemoveFriendCompleteListener extends SocialNetworkListener {
    public void onRequestRemoveFriendComplete(int socialNetworkID, String userID);
}
