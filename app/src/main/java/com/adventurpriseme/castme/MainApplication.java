package com.adventurpriseme.castme;

import android.app.Application;

import com.google.sample.castcompanionlibrary.cast.DataCastManager;

/**
 * Created by Timothy on 1/10/2015.
 * Copyright 1/10/2015 adventurpriseme.com
 */
public class MainApplication extends Application
	{
	private static String            sm_strApplicationId;
	// FIXME: I think we should have a parent "Application" that hosts the root "Activity".
	// It looks like that's how they do it in their video player example, which would explain
	// how they keep their cast manager alive through various activities.
	private static DataCastManager   sm_dataCastManager;

	@Override
	public void onCreate ()
		{
		super.onCreate ();
		sm_strApplicationId = getString (R.string.cast_app_id);
		initCastManager ();

		}

	private void initCastManager ()
		{
		sm_dataCastManager = DataCastManager.initialize (getApplicationContext (), sm_strApplicationId, getString (R.string.cast_namespace));
		getDataCastManager().enableFeatures (DataCastManager.FEATURE_DEBUGGING | DataCastManager.FEATURE_WIFI_RECONNECT);
		}

	public static DataCastManager getDataCastManager ()
		{
		if (sm_dataCastManager == null)
			{
			throw new IllegalStateException ("Application has not been started");
			}
		return sm_dataCastManager;
		}
	}
