package com.befriender.lib.listener.base;

public interface SocialNetworkListener {
    public void onError(int socialNetworkID, String requestID, String errorMessage, Object data);
}
