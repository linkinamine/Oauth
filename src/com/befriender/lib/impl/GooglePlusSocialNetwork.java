package com.befriender.lib.impl;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.befriender.lib.AccessToken;
import com.befriender.lib.MomentUtil;
import com.befriender.lib.SocialNetwork;
import com.befriender.lib.SocialNetworkException;
import com.befriender.lib.SocialPerson;
import com.befriender.lib.listener.OnCheckIsFriendCompleteListener;
import com.befriender.lib.listener.OnLoginCompleteListener;
import com.befriender.lib.listener.OnPostingCompleteListener;
import com.befriender.lib.listener.OnRequestAddFriendCompleteListener;
import com.befriender.lib.listener.OnRequestRemoveFriendCompleteListener;
import com.befriender.lib.listener.OnRequestSocialPersonCompleteListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;

import java.io.File;
import java.util.UUID;

public class GooglePlusSocialNetwork extends SocialNetwork implements
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	public static final int ID = 3;

	private static final String TAG = GooglePlusSocialNetwork.class
			.getSimpleName();
	// max 16 bit to use in startActivityForResult
	private static final int REQUEST_AUTH = UUID.randomUUID().hashCode() & 0xFFFF;
	/**
	 * mPlusClient.isConntected() works really strange, it returs false right
	 * after init and then true, so let's handle state by ourselves
	 */
	private static final String SAVE_STATE_KEY_IS_CONNECTED = "GooglePlusSocialNetwork.SAVE_STATE_KEY_OAUTH_TOKEN";
	private PlusClient mPlusClient;
	private ConnectionResult mConnectionResult;
	private boolean mConnectRequested;
	private Handler mHandler = new Handler();

	public GooglePlusSocialNetwork(Fragment fragment) {
		super(fragment);
	}

	@Override
	public boolean isConnected() {
		return mSharedPreferences
				.getBoolean(SAVE_STATE_KEY_IS_CONNECTED, false);
	}

	@Override
	public AccessToken getAccessToken() {
		throw new SocialNetworkException(
				"Not supported for GooglePlusSocialNetwork");
	}

	@Override
	public void requestLogin(OnLoginCompleteListener onLoginCompleteListener) {
		super.requestLogin(onLoginCompleteListener);

		if (isConnected()) {
			logout();
			onDisconnected();

		} else {
			mConnectRequested = true;

			try {
				mConnectionResult.startResolutionForResult(
						mSocialNetworkManager.getActivity(), REQUEST_AUTH);
			} catch (Exception e) {
				Log.e(TAG, "ERROR", e);
				if (!mPlusClient.isConnecting()) {
					mPlusClient.connect();
				}
			}
		}
	}

	@Override
	public void logout() {
		mConnectRequested = false;

		if (mPlusClient.isConnected()) {
			mSharedPreferences.edit().remove(SAVE_STATE_KEY_IS_CONNECTED)
					.commit();

			mPlusClient.clearDefaultAccount();
			mPlusClient.disconnect();
			// mPlusClient.connect();
		}
	}

	@Override
	public int getID() {
		return ID;
	}

	@Override
	public void requestCurrentPerson(
			OnRequestSocialPersonCompleteListener onRequestSocialPersonCompleteListener) {
		super.requestCurrentPerson(onRequestSocialPersonCompleteListener);

		Person person = mPlusClient.getCurrentPerson();

		if (person == null) {
			if (mLocalListeners.get(REQUEST_GET_CURRENT_PERSON) != null) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mLocalListeners.get(REQUEST_GET_CURRENT_PERSON)
								.onError(getID(), REQUEST_GET_CURRENT_PERSON,
										"Can't get person", null);
					}
				});
			}

			return;
		}

		final SocialPerson socialPerson = new SocialPerson();
		socialPerson.id = person.getId();
		socialPerson.name = person.getDisplayName();

		Person.Image image = person.getImage();
		if (image != null) {
			String imageURL = image.getUrl();

			if (imageURL != null) {
				socialPerson.avatarURL = imageURL;
			}
		}

		socialPerson.nickname = person.getNickname();
		socialPerson.profileURL = person.getUrl();

		if (mLocalListeners.get(REQUEST_GET_CURRENT_PERSON) != null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					((OnRequestSocialPersonCompleteListener) mLocalListeners
							.get(REQUEST_GET_CURRENT_PERSON))
							.onRequestSocialPersonSuccess(getID(), socialPerson);
				}
			});
		}
	}

	@Override
	public void requestSocialPerson(
			String userID,
			OnRequestSocialPersonCompleteListener onRequestSocialPersonCompleteListener) {
		throw new SocialNetworkException(
				"requestSocialPerson isn't allowed for GooglePlusSocialNetwork");
	}

	@Override
	public void requestPostMessage(String message,
			OnPostingCompleteListener onPostingCompleteListener) {
		throw new SocialNetworkException(
				"requestPostMessage isn't allowed for GooglePlusSocialNetwork");
	}

	@Override
	public void requestPostPhoto(File photo, String message,
			OnPostingCompleteListener onPostingCompleteListener) {
		throw new SocialNetworkException(
				"requestPostPhoto isn't allowed for GooglePlusSocialNetwork");
	}

	@Override
	public void requestCheckIsFriend(String userID,
			OnCheckIsFriendCompleteListener onCheckIsFriendCompleteListener) {
		throw new SocialNetworkException(
				"requestCheckIsFriend isn't allowed for GooglePlusSocialNetwork");
	}

	@Override
	public void requestAddFriend(
			String userID,
			OnRequestAddFriendCompleteListener onRequestAddFriendCompleteListener) {
		throw new SocialNetworkException(
				"requestAddFriend isn't allowed for GooglePlusSocialNetwork");
	}

	@Override
	public void requestRemoveFriend(
			String userID,
			OnRequestRemoveFriendCompleteListener onRequestRemoveFriendCompleteListener) {
		throw new SocialNetworkException(
				"requestRemoveFriend isn't allowed for GooglePlusSocialNetwork");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPlusClient = new PlusClient.Builder(
				mSocialNetworkManager.getActivity(), this, this).setActions(
				MomentUtil.ACTIONS).build();
	}

	@Override
	public void onStart() {
		mPlusClient.connect();
	}

	@Override
	public void onStop() {
		if (mPlusClient.isConnected()) {
			mPlusClient.disconnect();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQUEST_AUTH) {
			if (resultCode == Activity.RESULT_OK && !mPlusClient.isConnected()
					&& !mPlusClient.isConnecting()) {
				// This time, connect should succeed.
				mPlusClient.connect();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				if (mLocalListeners.get(REQUEST_LOGIN) != null) {
					mLocalListeners.get(REQUEST_LOGIN).onError(getID(),
							REQUEST_LOGIN, "canceled", null);
				}
			}
		}
	}

	@Override
	public void onConnected(Bundle bundle) {
		if (mConnectRequested) {
			if (mPlusClient.getCurrentPerson() != null) {
				if (mLocalListeners.get(REQUEST_LOGIN) != null) {
					mSharedPreferences.edit()
							.putBoolean(SAVE_STATE_KEY_IS_CONNECTED, true)
							.commit();
					((OnLoginCompleteListener) mLocalListeners
							.get(REQUEST_LOGIN)).onLoginSuccess(getID());
				}

				return;
			}

			if (mLocalListeners.get(REQUEST_LOGIN) != null) {
				mLocalListeners.get(REQUEST_LOGIN).onError(getID(),
						REQUEST_LOGIN, "get person == null", null);
			}
		}

		mConnectRequested = false;
	}

	@Override
	public void onDisconnected() {

	
		mLocalListeners.get(REQUEST_LOGIN).onError(getID(), REQUEST_LOGIN,
				"Successfully logged out", null);
		mConnectRequested = false;
		((OnLoginCompleteListener) mLocalListeners.get(REQUEST_LOGIN))
		.onLogOutSuccess(getID());
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
		mConnectionResult = connectionResult;

		if (mConnectRequested && mLocalListeners.get(REQUEST_LOGIN) != null) {
			mLocalListeners.get(REQUEST_LOGIN).onError(getID(), REQUEST_LOGIN,
					"error: " + connectionResult.getErrorCode(), null);
		}

		mConnectRequested = false;
	}
}
