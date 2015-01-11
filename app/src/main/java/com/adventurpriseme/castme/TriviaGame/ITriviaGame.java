package com.adventurpriseme.castme.TriviaGame;

import com.adventurpriseme.castme.CTriviaPlayer;

/**
 * Created by Timothy on 1/7/2015.
 * Copyright 1/7/2015 adventurpriseme.com
 */
public interface ITriviaGame
	{
	/**
	 * Send a message over the communications channel.
	 *
	 * @param message
	 */
	public void sendMessage (final String message);
	/**
	 * Controls the activity's UI.
	 *
	 * @param state
	 */
	public void updateUI (com.adventurpriseme.castme.TriviaGame.ETriviaGameStates state);

	/**
	 * Get the current trivia player.
	 *
	 * @return CTriviaPlayer
	 */
	public CTriviaPlayer getTriviaPlayer ();

	/**
	 * Returns the players name.
	 *
	 * @return String
	 */
	public String getPlayerName ();

	/**
	 * Get the enabled state of the round timer.
	 *
	 * @return Boolean  True == enabled, False == disabled
	 */
	public boolean getRoundTimerEnable ();

	/**
	 * Get the enabled state of the post-round timer.
	 *
	 * @return Boolean  True == enabled, False == disabled
	 */
	public boolean getPostRoundTimerEnable ();
	}
