package com.adventurpriseme.castme.CommsMgr;

/**
 * Enumerates the supported communications channels.
 * <p/>
 * This provides a list of supported communication channels.
 * The final entry, NUM_SUPPORTED_COMM_TYPES, must be the last item in the list,
 * and tells us how many communication types we support.
 * <p/>
 * Created by Timothy on 12/27/2014.
 * Copyright 12/27/2014 adventurpriseme.com
 */
public enum ECommChannelTypes
	{
		CHROMECAST,
		CCL,
		// Google Chromecast
		NUM_SUPPORTED_COMM_TYPES    // This must always be last in the list
	}
