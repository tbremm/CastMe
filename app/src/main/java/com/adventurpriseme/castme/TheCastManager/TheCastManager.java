package com.adventurpriseme.castme.TheCastManager;

import android.content.Context;

/**
 * Our base class that extends the Chromecast Companion Library (CCL) BaseCastManager.
 *
 * Created by Timothy on 1/7/2015.
 * Copyright 1/7/2015 adventurpriseme.com
 */
public class TheCastManager
	{
	private static TheCastManager m_instance = null;    // This is a singleton class
	public static TheCastManager getInstance (Context context, String applicationId)
		{
		if (m_instance == null)
			{
			m_instance = new TheCastManager (context, applicationId);
			}
		return m_instance;
		}

	private TheCastManager (Context context, String applicationId)
		{

		}
	}
