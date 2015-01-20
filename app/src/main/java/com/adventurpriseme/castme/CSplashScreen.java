package com.adventurpriseme.castme;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.adventurpriseme.castme.CCLTriviaActivity.CCLTriviaActivity;

/**
 * Class used to display a splash screen.
 * <p/>
 * Created by Timothy on 1/19/2015.
 * Copyright 1/19/2015 adventurpriseme.com
 */
public class CSplashScreen
	extends Activity
	{
	private static int SPLASH_TIME_OUT = 3000;  // Splash screen timeout (ms)

	@Override
	protected void onCreate (Bundle savedInstanceState)
		{
		super.onCreate (savedInstanceState);
		setContentView (R.layout.activity_splash_screen);
		// Handle showing the splash screen and then launching the real main activity
		new Handler ().postDelayed (new Runnable ()
		{
		/**
		 * Show the splash screen.
		 */
		@Override
		public void run ()
			{
			// This starts the trivia activity once the timer is up
			Intent intent = new Intent (CSplashScreen.this, CCLTriviaActivity.class);
			startActivity (intent);
			// Close this activity
			finish ();
			}
		}, SPLASH_TIME_OUT);
		}
	}
