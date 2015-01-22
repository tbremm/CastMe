package com.adventurpriseme.castme.TriviaDataCastConsumer;

import android.util.Log;

import com.adventurpriseme.castme.R;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.common.api.Status;
import com.google.sample.castcompanionlibrary.cast.callbacks.DataCastConsumerImpl;

/**
 * Created by Timothy on 1/7/2015.
 * Copyright 1/7/2015 adventurpriseme.com
 */
public class CDataCastConsumer
	extends DataCastConsumerImpl
	{
	private static final String            TAG                = "CDataCastConsumer";
	private              eConnectionStates m_eConnectionState = eConnectionStates.E_CONNECTION_STATE_UNKNOWN;    // Maintain our state so that we know if we want to update the layout

	public enum eConnectionStates
		{
			E_CONNECTION_STATE_UNKNOWN,
			E_CONNECTION_STATE_DISCONNECTED,
			E_CONNECTION_STATE_CONNECTED
		}

	public eConnectionStates getConnectionState ()
		{
		return m_eConnectionState;
		}

	private void setConnectionState (eConnectionStates eConnectionState)
		{
		if (m_eConnectionState != eConnectionState)
			{
			m_eConnectionState = eConnectionState;
			updateUIState ();
			}
		}

	/**
	 * Override this to set the activity's content view
	 */
	public void updateUIState ()
		{
		// Override me!
		}

	@Override
	public void onApplicationConnected (ApplicationMetadata appMetadata, String applicationStatus, String sessionId, boolean wasLaunched)
		{
		// Set our content view
		setConnectionState (eConnectionStates.E_CONNECTION_STATE_CONNECTED);
		}

	@Override
	public void onConnected ()
		{
		// Set our content view
		setConnectionState (eConnectionStates.E_CONNECTION_STATE_CONNECTED);
		}

	@Override
	public void onApplicationDisconnected (int errorCode)
		{
		// Set our content view
		setConnectionState (eConnectionStates.E_CONNECTION_STATE_DISCONNECTED);
		}

	@Override
	public void onDisconnected ()
		{
		// Set our content view
		setConnectionState (eConnectionStates.E_CONNECTION_STATE_DISCONNECTED);
		}

	@Override
	public void onMessageSendFailed (Status status)
		{
		Log.e (TAG, "onMessageSendFailed with message: " + status.getStatusMessage ());
		}

	@Override
	public void onFailed (int resourceId, int statusCode)
		{
		Log.e (TAG, "onFailed() called with status code: " + statusCode);
		// TODO: Handle errors
		}

	@Override
	public void onConnectionSuspended (int cause)
		{
		// FIXME: Should just dim things out or display a wait signal or something
		// Set our content view
		setConnectionState (eConnectionStates.E_CONNECTION_STATE_DISCONNECTED);
		Log.d (TAG, "onConnectionSuspended() was called with cause: " + cause);
		}

	@Override
	public void onConnectivityRecovered ()
		{
		// Set our content view
		setConnectionState (eConnectionStates.E_CONNECTION_STATE_CONNECTED);
		Log.d (TAG, "onConnectivityRecovered() was called: " + R.string.connection_recovered);
		}

	/* When we show the user the cast button for the first time, this is where we do it
	@Override
	public void onCastDeviceDetected (final MediaRouter.RouteInfo info)
		{
		if (!CastPreference.isFtuShown (CCLTriviaActivity.this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
			CastPreference.setFtuShown (CCLTriviaActivity.this);
			Log.d (TAG, "Route is visible: " + info);
			new Handler ().postDelayed (new Runnable ()
			{
			@Override
			public void run ()
				{
				if (m_mediaRouteMenuItem.isVisible ())
					{
					Log.d (TAG, "Cast Icon is visible:  " + info.getName ());
					showFtu();    // Show First-Time-User (ftu)
					}
				}
			}, 1000);
			}
		}
		*/

	@Override
	public void onReconnectionStatusChanged (int status)
		{
		Log.d (TAG, "onReconnectionStatusChanged(): " + status);
		}
	}
