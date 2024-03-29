package com.adventurpriseme.castme.TriviaGame;

import android.util.Log;

import com.adventurpriseme.castme.GameInstanceMgr.IGamesMgr2Game;

import java.util.ArrayList;
import java.util.Arrays;

import static com.adventurpriseme.castme.TriviaGame.EConfigKeys.MSG_KEY_ANSWER;
import static com.adventurpriseme.castme.TriviaGame.EConfigKeys.MSG_KEY_PLAYER_NAME;
import static com.adventurpriseme.castme.TriviaGame.EConfigKeys.MSG_KEY_POSTROUND_TIMER;
import static com.adventurpriseme.castme.TriviaGame.EConfigKeys.MSG_KEY_QUESTION;
import static com.adventurpriseme.castme.TriviaGame.EConfigKeys.MSG_KEY_ROUND_TIMER;
import static com.adventurpriseme.castme.TriviaGame.EConfigKeys.MSG_KEY_UID;
import static com.adventurpriseme.castme.TriviaGame.EErrorMessages.MSG_ERROR;
import static com.adventurpriseme.castme.TriviaGame.EErrorMessages.MSG_ERROR_MSG;
import static com.adventurpriseme.castme.TriviaGame.EErrorMessages.MSG_ERROR_RECEIVED_EMPTY_MSG;
import static com.adventurpriseme.castme.TriviaGame.EMessageDelimiters.MSG_SPLIT_DATA;
import static com.adventurpriseme.castme.TriviaGame.EMessageDelimiters.MSG_SPLIT_KEY_VALUE;
import static com.adventurpriseme.castme.TriviaGame.ETriviaGameStates.CONNECTED;
import static com.adventurpriseme.castme.TriviaGame.ETriviaGameStates.GET_CONFIG;
import static com.adventurpriseme.castme.TriviaGame.ETriviaGameStates.GOT_Q_AND_A;
import static com.adventurpriseme.castme.TriviaGame.ETriviaGameStates.HOSTED;
import static com.adventurpriseme.castme.TriviaGame.ETriviaGameStates.HOSTING;
import static com.adventurpriseme.castme.TriviaGame.ETriviaGameStates.ROUND_LOSE;
import static com.adventurpriseme.castme.TriviaGame.ETriviaGameStates.ROUND_WIN;
import static com.adventurpriseme.castme.TriviaGame.ETriviaGameStates.WAITING;
import static com.adventurpriseme.castme.TriviaGame.ETriviaMessagesToServer.MSG_BEGIN_ROUND;
import static com.adventurpriseme.castme.TriviaGame.ETriviaMessagesToServer.MSG_CONFIG;
import static com.adventurpriseme.castme.TriviaGame.ETriviaMessagesToServer.MSG_END_ROUND;
import static com.adventurpriseme.castme.TriviaGame.ETriviaMessagesToServer.MSG_REQUEST_HOST;

/**
 * This represents a trivia game.
 * <p/>
 * This class maintains the game state machine and player control.
 * <p/>
 * Created by Timothy on 12/11/2014.
 * Copyright 12/11/2014 adventurpriseme.com
 */
public class CTriviaGame
	implements IGamesMgr2Game
	{
	private static final String[] Q_AND_A_STRING_ARRAY = new String[5];   // TODO: Don't hardcode this size
	// Private data members
	private final        String   TAG                  = "CTriviaGame";
	// The caller's context
	private ITriviaGame m_activity;
	private String             m_strMsgIn;
	private ArrayList<String>  m_strMsgOut;
	private String             m_question;
	private ArrayList<String>  m_answers;
	private ETriviaGameStates m_eTriviaGameState;

	public CTriviaGame (ITriviaGame iTriviaGame)
		{
		m_strMsgOut = new ArrayList<String> ();    // TODO: Make this dependent on the type of question expected
		m_activity = iTriviaGame;
		m_question = "";
		m_answers = new ArrayList<String> ();
		setGameState (WAITING);
		}

	public ETriviaGameStates getGameState ()
		{
		return m_eTriviaGameState;
		}

	private void setGameState (ETriviaGameStates state)
		{
		m_eTriviaGameState = state;
		}

	/**
	 * Handler for when we receive a message from the web.
	 * <p/>
	 * Preserves the message and sets the game state to process it.
	 *
	 * @param strMsgIn
	 * 	String content of the received message
	 */
	public void onMessageIn (String strMsgIn)
		{
		m_strMsgIn = strMsgIn.toLowerCase ();   // TODO: Move this to just-in-time conditioning, in case we want to preserve case on a string that's displayed directly
		processMessage (strMsgIn);
		}

	/**
	 * Process messages received from the game server.
	 * <p/>
	 * Sets the response string, if any, based upon the received string.
	 * Sets the state according to the received message.
	 */
	private void processMessage (String strMsgIn)
		{
		// Break apart the message into its parts
		ArrayList<String> inMsgSplit = new ArrayList<String> (Arrays.asList (strMsgIn.split ("\\" + MSG_SPLIT_DATA)));
		m_strMsgOut.clear ();   // Clear out the output array
		// Error handling
		if (inMsgSplit.size () < 1)
			{
			m_strMsgOut.add (MSG_ERROR.toString ());
			m_strMsgOut.add (MSG_ERROR_MSG.toString () + MSG_SPLIT_KEY_VALUE.toString () + MSG_ERROR_RECEIVED_EMPTY_MSG.toString ());
			m_activity.sendMessage (formatMessage (m_strMsgOut));
			m_activity.updateUI (ETriviaGameStates.ERROR);
			return;
			}
		ETriviaCommandsFromServer command = ETriviaCommandsFromServer.getEnumFromString (inMsgSplit.get (0));
		// Process the message
		switch (command)
			{
			case MSG_CONNECTED:
				{
				// Send config values and currently known UID. This UID will be updated.
				m_strMsgOut.addAll (getPlayerConfigKeyValues ());
				// We expect a unique player ID here (it is currently our senderID)
				if (inMsgSplit.size () > 1)
					{
					String[] dataSplit = inMsgSplit.get (1)
						                     .split (MSG_SPLIT_KEY_VALUE.toString ());
					if (dataSplit.length == 2 && dataSplit[0].equals (MSG_KEY_UID.toString ()))
						{
						m_activity.getTriviaPlayer ()
							.setUID (dataSplit[1]);
						}
					}
				m_activity.updateUI (CONNECTED);
				break;
				}
			case MSG_HOST:
			{
			m_activity.updateUI (HOSTING);
			break;
			}
			case MSG_HOSTED:
			{
			m_strMsgOut.addAll (getPlayerConfigKeyValues ());
			m_activity.updateUI (HOSTED);
			break;
			}
			case MSG_REQUEST_CONFIG:
			{
			// The server is asking for config data
			// TODO: Make this smarter (get all player info and send in as key-value pairs)
			m_strMsgOut.addAll (getPlayerConfigKeyValues ());
			m_activity.updateUI (GET_CONFIG);
			break;
			}
			case MSG_Q_AND_A:
			{
			if (inMsgSplit.size () == 6)                         // command, question, answers 1-4
				{
				m_question = "";    // clear question
				m_answers.clear ();  // clear answers
				int answerIndex = 0;
				for (int i = 0; i < inMsgSplit.size (); ++i)     // Get and strip the question and answer values
					{
					String[] key_value_split = inMsgSplit.get (i)
						                           .split (MSG_SPLIT_KEY_VALUE.toString ());
					if (key_value_split[0].equals (MSG_KEY_ANSWER.toString ()))       // Check for the answer key
						{
						m_answers.add (inMsgSplit.get (i)
							               .split (MSG_SPLIT_KEY_VALUE.toString ())[1]);    // Get the answer value
						}
					else if (key_value_split[0].equals (MSG_KEY_QUESTION.toString ()))
						{
						m_question = inMsgSplit.get (i)
							             .split (MSG_SPLIT_KEY_VALUE.toString ())[1];
						}
					}
				}
			m_activity.updateUI (GOT_Q_AND_A);
			break;
				}
			case MSG_WIN:
			{
			m_activity.updateUI (ROUND_WIN);
			break;
			}
			case MSG_LOSE:
			{
			m_activity.updateUI (ROUND_LOSE);
			break;
			}
			case MSG_ERROR:
				// TODO: Handle error messages received from the server
				break;
			default:
			{
			// TODO: Handle unexpected command
			Log.e (TAG, "Unhandled message received: " + inMsgSplit.get (0));
			break;
			}
			}
		}

	private String formatMessage (ArrayList<String> list)
		{
		String ret = "";
		for (int i = 0; i < list.size (); i++)
			{
			ret += list.get (i);
			if (i < (list.size () - 1))
				{
				ret += MSG_SPLIT_DATA.toString ();
				}
			}
		return ret;
		}

	private ArrayList<String> getPlayerConfigKeyValues ()
		{
		ArrayList<String> starOut = new ArrayList<String> ();
		starOut.add (MSG_CONFIG.toString ());
		starOut.add (MSG_KEY_PLAYER_NAME.toString () + MSG_SPLIT_KEY_VALUE.toString () + m_activity.getPlayerName ());      // Player name
		starOut.add (MSG_KEY_UID.toString () + MSG_SPLIT_KEY_VALUE.toString () + m_activity.getTriviaPlayer ()
			                                                                         .getUID ());  // Player Unique ID
		return starOut;
		}

	/**
	 * Send the user's strMessage to the web server.
	 *
	 * @param strMessage
	 * 	(required) String  The player's strMessage to send
	 */
	public void sendMessage (ArrayList<String> strMessage)
		{
		m_activity.sendMessage (formatMessage (strMessage));
		}

	// TODO: same function call for beginning a game as starting a new round.
	// may want to separate calls (ie. one with config data - one without)
	public void beginNewRound ()
		{
		// TODO: Should wrap all configuration into a single object to be passed around -GN
		// Set the round timer
		String enableRoundTimer = MSG_KEY_ROUND_TIMER.toString () + MSG_SPLIT_KEY_VALUE.toString () +
		                          String.valueOf (m_activity.getRoundTimerEnable ());
		// Set the post round timer
		String enablePostRoundTimer = MSG_KEY_POSTROUND_TIMER.toString () + MSG_SPLIT_KEY_VALUE.toString () +
		                              String.valueOf (m_activity.getPostRoundTimerEnable ());
		// Set the player's name
		String playerName = MSG_KEY_PLAYER_NAME.toString () + MSG_SPLIT_KEY_VALUE.toString () +
		                    String.valueOf (m_activity.getPlayerName ());
		// Send the game start packet
		m_activity.sendMessage (MSG_BEGIN_ROUND.toString () + MSG_SPLIT_DATA.toString () + enableRoundTimer +
		                        MSG_SPLIT_DATA.toString () + enablePostRoundTimer +
		                        MSG_SPLIT_DATA.toString () + playerName);
		}

	public void endRound ()
		{
		m_activity.sendMessage (MSG_END_ROUND.toString ());
		}

	/**
	 * Send the user's strMessage to the web server.
	 *
	 * @param strMessage
	 * 	(required) String  The player's strMessage to send
	 */
	public void sendMessage (String strMessage)
		{
		m_activity.sendMessage (formatMessage (strMessage));
		}

	/**
	 * Formats a user's strMessage so that the web server can parse it.
	 * <p/>
	 * Current strMessage message format is:
	 * "a|strMessage"
	 *
	 * @param strMessage
	 *
	 * @return String  Formatted strMessage
	 */
	public String formatMessage (String strMessage)
		{
		return (MSG_KEY_ANSWER.toString () + MSG_SPLIT_DATA.toString () + strMessage);
		}

	public void requestHost ()
		{
		m_activity.sendMessage (MSG_REQUEST_HOST.toString ());
		}

	public String getQuestion ()
		{
		return m_question;
		}

	public ArrayList<String> getAnswers ()
		{
		return m_answers;
		}

	public ArrayList<String> getOutMsg ()
		{
		return m_strMsgOut;
		}
	}
