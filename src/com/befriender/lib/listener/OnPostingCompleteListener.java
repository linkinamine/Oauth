package com.befriender.lib.listener;

import com.befriender.lib.listener.base.SocialNetworkListener;

public interface OnPostingCompleteListener extends SocialNetworkListener {
    public void onPostSuccessfully(int socialNetworkID);
}
