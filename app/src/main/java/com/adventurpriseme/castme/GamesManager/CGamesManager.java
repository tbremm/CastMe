package com.adventurpriseme.castme.GamesManager;

import android.app.Activity;
import android.util.Log;

import com.adventurpriseme.castme.CommsMgr.IGames2Comms;
import com.adventurpriseme.castme.GameInstanceMgr.IGamesMgr2Game;
import com.adventurpriseme.castme.TriviaGame.CTriviaGame;
import com.adventurpriseme.castme.TriviaGame.ITriviaGame;

/**
 * Manages the game instance and implements communication with the cast interface.
 * <p/>
 * Created by Timothy on 12/27/2014.
 * Copyright 12/27/2014 adventurpriseme.com
 */
public class CGamesManager
	implements IComms2Games, IGame2GamesMgr
	{
	/** Data members */
	private static final String TAG = "Games Manager";
	IGames2Comms   m_iGames2Comms;  // Instance of current communications channel
	IGamesMgr2Game m_GameInstance;  // Instance of currently running game
	private Activity m_activity;    // Parent activity
	// TODO: Present a list of available games to the user
	// TODO: Launch/play game selected by user

	/**
	 * Constructor.
	 *
	 * @param activity (required)  This is the parent activity that is hosting the game session.
	 */
	public CGamesManager (Activity activity)
		{
		m_activity = activity;
		}

	/**
	 * Initialize a game instance.
	 *
	 * @param eGame
	 * 	(required)
	 * 	Determines which game is to be initialized.
	 */
	public void initGame (ESupportedGames eGame)
		{
		switch (eGame)
			{
			case TRIVIA:
				m_GameInstance = new CTriviaGame ((ITriviaGame)m_activity);
				break;
			default:
				Log.e (TAG, "ERROR: Unsupported game selected...");
				break;
			}
		}

	/**
	 * Initialize the manager with a communications manager object
	 * <p/>
	 * Call this after construction, and pass it an implementation of the {@link com.adventurpriseme.castme.GamesManager.IComms2Games} interface. This allows the manager to make
	 * outbound calls to the communications API.
	 *
	 * @param iGames2CommsMgr
	 * 	(required)
	 * 	The communications interface used to make outbound calls to the communications API
	 */
	public void initCommunications (IGames2Comms iGames2CommsMgr)
		{
		m_iGames2Comms = iGames2CommsMgr;
		}

	@Override
	public Activity getActivity ()
		{
		return m_activity;
		}

	public IGamesMgr2Game getGameInstance ()
		{
		return m_GameInstance;
		}
	}
