package com.adventurpriseme.castme.ChromeCastMgr;

import android.content.Context;
import android.util.Log;

import com.adventurpriseme.castme.PlayTriviaActivity;
import com.adventurpriseme.castme.R;
import com.google.android.gms.cast.Cast;
import com.google.android.gms.cast.CastDevice;

/**
 * Created by Timothy on 11/18/2014.
 * Copyright 11/18/2014 adventurpriseme.com
 */
public class CCastChannel
	extends PlayTriviaActivity
	implements Cast.MessageReceivedCallback
	{
	private final String TAG = "Trivia Cast Channel";
	private Context            m_context;
	private IChromeCastMessage m_CastMsg;

	public CCastChannel ()
		{
		}

	/**
	 * Manages the
	 *
	 * @param ICMsgCallback
	 */
	public CCastChannel (IChromeCastMessage ICMsgCallback)
		{
		m_CastMsg = ICMsgCallback;
		}

	public String getNamespace ()
		{
		return getString(R.string.cast_namespace);
		}

	@Override
	public void onMessageReceived (CastDevice castDevice, String namespace, String message)
		{
		Log.d (TAG, "onMessageReceived: " + message);
		m_CastMsg.onReceiveCallback (message);
		// TODO: set an IChromeCastMessage here to be read in the parent
		}
	}
