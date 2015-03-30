package com.befriender.lib.listener;

import com.befriender.lib.SocialPerson;
import com.befriender.lib.listener.base.SocialNetworkListener;

public interface OnRequestSocialPersonCompleteListener extends SocialNetworkListener {
    public void onRequestSocialPersonSuccess(int socialNetworkID, SocialPerson socialPerson);
}
