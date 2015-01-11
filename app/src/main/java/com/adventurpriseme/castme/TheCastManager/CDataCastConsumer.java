package com.adventurpriseme.castme.TheCastManager;

import android.util.Log;

import com.adventurpriseme.castme.TriviaGame.CTriviaGame;
import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.common.api.Status;
import com.google.sample.castcompanionlibrary.cast.callbacks.DataCastConsumerImpl;

/**
 * Created by Timothy on 1/7/2015.
 * Copyright 1/7/2015 adventurpriseme.com
 */
public class CDataCastConsumer extends DataCastConsumerImpl
	{
	private static final String TAG = "CDataCastConsumer";
	private CTriviaGame m_cTriviaGame;

	@Override
	public void onMessageReceived (CastDevice castDevice, String namespace, String message)
		{
		m_cTriviaGame.onMessageIn (message);
		}

	@Override
	public void onMessageSendFailed(Status status)
		{
		Log.e (TAG, "onMessageSendFailed with message: " + status.getStatusMessage ());
		}

	@Override
	public void onApplicationConnected(ApplicationMetadata appMetadata, String applicationStatus, String sessionId, boolean wasLaunched)
		{

		}
	}
