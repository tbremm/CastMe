package com.adventurpriseme.castme;

import android.app.Application;
import android.util.Log;

import com.adventurpriseme.castme.Settings.CastPreference;
import com.google.sample.castcompanionlibrary.cast.DataCastManager;
import com.google.sample.castcompanionlibrary.utils.Utils;

/**
 * Created by Timothy on 1/10/2015.
 * Copyright 1/10/2015 adventurpriseme.com
 */
public class MainApplication extends Application
	{
	private static String            TAG = "Main Application";
	private static String            sm_strApplicationId;           // App ID registered with Google
	private static DataCastManager   sm_dataCastManager = null;     // Our cast manager
	public static final double VOLUME_INCREMENT = 0.05;             // For when we implement volume controls

	@Override
	public void onCreate ()
		{
		super.onCreate ();
		sm_strApplicationId = getString (R.string.cast_app_id_debug);
		initCastManager ();
		Utils.saveFloatToPreference(getApplicationContext (), DataCastManager.PREFS_KEY_VOLUME_INCREMENT, (float) VOLUME_INCREMENT);
		}

	private void initCastManager ()
		{
		sm_dataCastManager = DataCastManager.initialize (getApplicationContext (), sm_strApplicationId, getString (R.string.cast_namespace));
		sm_dataCastManager.enableFeatures (DataCastManager.FEATURE_DEBUGGING | DataCastManager.FEATURE_WIFI_RECONNECT);
		String destroyOnExitStr = Utils.getStringFromPreference(getApplicationContext(),
		                                                        CastPreference.TERMINATION_POLICY_KEY);
		sm_dataCastManager.setStopOnDisconnect(null != destroyOnExitStr
		                             && CastPreference.STOP_ON_DISCONNECT.equals(destroyOnExitStr));
		}

	/**
	 * Wraps _dataCastManager() in a try/catch block.
	 *
	 * @return DataCastManager
	 *
	 * @throws java.lang.IllegalStateException
	 */
	public static DataCastManager getDataCastManager ()
		{
		try
			{
			return _getDataCastManager ();
			}
		catch (IllegalStateException e)
			{
			Log.e (TAG, e.getMessage ());
			throw e;
			}
		}

	/**
	 * Gets the data cast manager if it exists. Throws otherwise.
	 *
	 * @return DataCastManager
	 * @throws IllegalStateException "Application has not been started"
	 */
	private static DataCastManager _getDataCastManager ()
		{
		if (sm_dataCastManager == null)
			{
			throw new IllegalStateException ("Application has not been started");
			}
		return sm_dataCastManager;
		}
	}
