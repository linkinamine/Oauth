package com.befriender.lib.listener;

import com.befriender.lib.listener.base.SocialNetworkListener;

public interface OnLoginCompleteListener extends SocialNetworkListener {
	public void onLoginSuccess(int socialNetworkID);

	public void onLogOutSuccess(int socialNetworkID);
}
