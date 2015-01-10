package com.adventurpriseme.castme;

import com.adventurpriseme.castme.ChromeCastMgr.IChromeCastMessage;

/**
 * This class is used to interface with the receiver app via {@link com.adventurpriseme.castme.ChromeCastMgr.CCastChannel}
 * <p/>
 * Created by Timothy on 11/24/2014.
 * Copyright 11/24/2014 adventurpriseme.com
 */
public class CTriviaCastCCMessage
	implements IChromeCastMessage
	{
	/**
	 * Callback for receiving a message from the chromecast.
	 * <p/>
	 * Implement this function to handle a message string coming in from the chromecast.
	 *
	 * @param strMsg
	 */
	@Override
	public void onReceiveCallback (String strMsg)
		{
		}
	// TODO - This class
	}
