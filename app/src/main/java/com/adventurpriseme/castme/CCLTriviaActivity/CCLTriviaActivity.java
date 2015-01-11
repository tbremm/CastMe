package com.adventurpriseme.castme.CCLTriviaActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adventurpriseme.castme.CAboutDialog;
import com.adventurpriseme.castme.CTriviaPlayer;
import com.adventurpriseme.castme.GamesManager.CGamesManager;
import com.adventurpriseme.castme.R;
import com.adventurpriseme.castme.TheCastManager.CDataCastConsumer;
import com.adventurpriseme.castme.TriviaGame.CTriviaGame;
import com.adventurpriseme.castme.TriviaGame.ETriviaGameStates;
import com.adventurpriseme.castme.TriviaGame.ITriviaGame;
import com.adventurpriseme.castme.TriviaGame.TriviaPrefsActivity;
import com.google.sample.castcompanionlibrary.cast.DataCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

import java.io.IOException;
import java.util.ArrayList;

public class CCLTriviaActivity
	extends ActionBarActivity
	implements ITriviaGame
	{
	private static DataCastManager   sm_dataCastManager;
	private        CDataCastConsumer m_cDataCastConsumer;
	private        CGamesManager     m_GamesMgr;
	private        CTriviaGame       m_cTriviaGame;
	private        CTriviaPlayer     m_cTriviaPlayer;
	private        SharedPreferences m_sharedPreferences;

	@Override
	protected void onCreate (Bundle savedInstanceState)
		{
		super.onCreate (savedInstanceState);
		initCastManager ();
		sm_dataCastManager.reconnectSessionIfPossible ();   // Default is 10 seconds
		m_cDataCastConsumer = new CDataCastConsumer ();
		setContentView (R.layout.activity_ccltrivia);
		}

	@Override
	protected void onPostCreate (Bundle savedInstanceState)
		{
		super.onPostCreate (savedInstanceState);
		m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences (this);
		m_cTriviaPlayer = new CTriviaPlayer (this);
		// FIXME: Fix game creation so that it is dependent on which game the user selects
		m_cTriviaGame = new CTriviaGame (this);
		}

	private void initCastManager ()
		{
		sm_dataCastManager = DataCastManager.initialize (getApplicationContext (), getString (R.string.cast_app_id), getString (R.string.cast_namespace));
		sm_dataCastManager.enableFeatures (DataCastManager.FEATURE_DEBUGGING |
		                                  DataCastManager.FEATURE_WIFI_RECONNECT);
		}

	public static DataCastManager getDataCastManager ()
		{
		if (sm_dataCastManager == null)
			{
			throw new IllegalStateException ("Application has not been started");
			}
		return sm_dataCastManager;
		}

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
		{
		super.onCreateOptionsMenu (menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater ().inflate (R.menu.menu_ccltrivia, menu);
		sm_dataCastManager.addMediaRouterButton (menu, R.id.media_route_menu_item); // This returns a pointer to the MenuItem that represents the Cast button
		return true;
		}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
		{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId ();
		//noinspection SimplifiableIfStatement
		switch (id)
			{
			case R.id.action_settings:
				onSettingsSelected ();
				return true;

			case R.id.home:
				// This ID represents the Home or Up button. In the case of this
				// activity, the Up button is shown. Use NavUtils to allow users
				// to navigate up one level in the application structure. For
				// more details, see the Navigation pattern on Android Design:
				//
				// http://developer.android.com/design/patterns/navigation.html#up-vs-back
				//
				NavUtils.navigateUpFromSameTask (this);
				return true;

			case R.id.action_about:
				// Show an about dialog box with version info
				CAboutDialog.Show (CCLTriviaActivity.this);
				break;
			}
		return super.onOptionsItemSelected (item);
		}

	/**
	 * Action bar settings menu entry
	 * <p/>
	 * This will load the settings view.
	 */
	private void onSettingsSelected ()
		{
		Intent intent = new Intent (this, TriviaPrefsActivity.class);
		startActivity (intent);
		}

	@Override
	public void onResume ()
		{
		super.onResume ();
		getDataCastManager ();
		sm_dataCastManager.incrementUiCounter ();
		}

	@Override
	public void onPause ()
		{
		super.onPause ();
		sm_dataCastManager.decrementUiCounter ();
		}
	/*********************************************************************
	 * Game play code
	 *********************************************************************/
	public void updateUI (ETriviaGameStates state)
		{
		// Get all of our GUI elements
		TextView tvPlayTitle = (TextView) findViewById (R.id.tvPlayTitle);
		TextView tvQuestion = (TextView) findViewById (R.id.tvQuestion);
		Button btnBeginNewRound = (Button) findViewById (R.id.btn_begin_new_round);
		RadioGroup rgAnswers = (RadioGroup) findViewById (R.id.radio_group_answers);
		// TODO Add an onCheckedChangeListener to handle button graphical state?
		switch (state)
			{
			case GET_CONFIG:
				break;
			case WAITING:
				break;
			case READY:
				break;
			case CONNECTED:
				// Clear the display of UI elements
				setAllUiElements_Visibility (View.INVISIBLE);
				if (m_cTriviaPlayer.getWillHost ())
					{
					// Create the "host game" button
					btnBeginNewRound.setText (getString (R.string.btn_text_host_game));
					btnBeginNewRound.setOnClickListener (new View.OnClickListener ()
					{
					@Override
					public void onClick (View view)
						{
						m_cTriviaGame.requestHost ();
						}
					});
					AddButtonLayout (btnBeginNewRound, RelativeLayout.CENTER_IN_PARENT); // Put button in the center of the screen
					btnBeginNewRound.setVisibility (View.VISIBLE);
					}
				else
					{
					m_cTriviaPlayer.setIsHosting (false);
					tvQuestion.setText (getString (R.string.waiting_for_host));
					tvQuestion.setVisibility (View.VISIBLE);
					}
				m_cTriviaGame.sendMessage (m_cTriviaGame.getOutMsg ());
				break;
			case HOSTING:
				// Clear the display of UI elements
				setAllUiElements_Visibility (View.INVISIBLE);
				m_cTriviaPlayer.setIsHosting (true);
				btnBeginNewRound.setText (getString (R.string.start_the_game));
				btnBeginNewRound.setOnClickListener (new View.OnClickListener ()
				{
				@Override
				public void onClick (View view)
					{
					m_cTriviaGame.beginNewRound ();
					}
				});
				AddButtonLayout (btnBeginNewRound, RelativeLayout.CENTER_IN_PARENT); // Put button in the center of the screen
				btnBeginNewRound.setVisibility (View.VISIBLE);
				break;
			case HOSTED:    // The game is already hosted, waiting to start
				// Clear the display of UI elements
				setAllUiElements_Visibility (View.INVISIBLE);
				m_cTriviaPlayer.setIsHosting (false);
				tvPlayTitle.setText (getString (R.string.waiting_on_host));
				tvPlayTitle.setVisibility (View.VISIBLE);
				break;
			case GOT_Q_AND_A:
				setAllUiElements_Visibility (View.INVISIBLE);       // Clear the display of UI elements
				tvPlayTitle.setText (getString (R.string.select_an_answer));       // Create the title text
				tvPlayTitle.setVisibility (View.VISIBLE);           // Make it visible
				tvQuestion.setText (m_cTriviaGame.getQuestion ());   // Create the question text
				tvQuestion.setVisibility (View.VISIBLE);            // Make it visible
				ArrayList<String> answers = m_cTriviaGame.getAnswers ();
				rgAnswers.removeAllViews ();                        // Remove any pre-existing radio buttons
				for (int i = 0; i < answers.size (); ++i)            // Add a radio button for each available answer
					{
					rgAnswers.addView (new CAnswerRadioButton (this, i, answers.get (i)));
					}
				rgAnswers.setVisibility (View.VISIBLE);         // Make sure we see the buttons
				if (m_cTriviaPlayer.isHosting ())               // Host gets to short-circuit the question
					{
					btnBeginNewRound.setText (getString (R.string.finish_round));
					btnBeginNewRound.setVisibility (View.VISIBLE);
					btnBeginNewRound.setOnClickListener (new View.OnClickListener ()    // TODO: Investigate if we have to remove old listeners here (possible leak?)
					{
					@Override
					public void onClick (View view)
						{
						m_cTriviaGame.endRound ();
						}
					});
					AddButtonLayout (btnBeginNewRound, RelativeLayout.ALIGN_BASELINE); // Put button at the beginning of the screen
					}
				break;
			case ROUND_WIN:
				// Clear the display of UI elements
				setAllUiElements_Visibility (View.INVISIBLE);
				tvPlayTitle.setText (getString (R.string.you_win));
				tvPlayTitle.setVisibility (View.VISIBLE);
				if (m_cTriviaPlayer.isHosting ())               // Host gets to short-circuit the question
					{
					btnBeginNewRound.setText (getString (R.string.btn_new_round));
					btnBeginNewRound.setVisibility (View.VISIBLE);
					btnBeginNewRound.setOnClickListener (new View.OnClickListener ()    // TODO: Investigate if we have to remove old listeners here (possible leak?)
					{
					@Override
					public void onClick (View view)
						{
						m_cTriviaGame.beginNewRound ();
						}
					});
					AddButtonLayout (btnBeginNewRound, RelativeLayout.ALIGN_BASELINE); // Put button at the bottom of the screen
					}
				break;
			case ROUND_LOSE:
				// Clear the display of UI elements
				setAllUiElements_Visibility (View.INVISIBLE);
				tvPlayTitle.setText (getString (R.string.you_lose));
				tvPlayTitle.setVisibility (View.VISIBLE);
				if (m_cTriviaPlayer.isHosting ())               // Host gets to short-circuit the question
					{
					btnBeginNewRound.setText (getString (R.string.btn_new_round));
					btnBeginNewRound.setVisibility (View.VISIBLE);
					btnBeginNewRound.setOnClickListener (new View.OnClickListener ()    // TODO: Investigate if we have to remove old listeners here (possible leak?)
					{
					@Override
					public void onClick (View view)
						{
						m_cTriviaGame.beginNewRound ();
						}
					});
					AddButtonLayout (btnBeginNewRound, RelativeLayout.ALIGN_BASELINE); // Put button at the bottom of the screen
					}
				break;
			case QUIT:
				break;
			case ERROR:
				// TODO: Error handling
				sendMessage ("error|msg=");
			default:
				break;
			}
		}

	/**
	 * Set the visibility of all children of the main relative layout.
	 *
	 * @param visibility
	 * 	(required)  This should be View.VISIBLE, INVISIBLE, or GONE
	 */
	private void setAllUiElements_Visibility (int visibility)
		{
		RelativeLayout relativeLayout = (RelativeLayout) findViewById (R.id.rl_trivia_main_on);
		int numViewElements = relativeLayout.getChildCount ();
		for (int i = 0; i < numViewElements; ++i)
			{
			relativeLayout.getChildAt (i)
				.setVisibility (visibility);
			}
		}

	/**
	 * Set layout of a button within a relative layout.
	 *
	 * @param button
	 * @param centerInParent
	 * 	RelativeLayout.ALIGN_PARENT_BOTTOM, etc
	 */
	private void AddButtonLayout (Button button, int centerInParent)
		{
		// Just call the other AddButtonLayout Method with Margin 0
		AddButtonLayout (button, centerInParent, 0, 0, 0, 0);
		}

	// public - so CTriviaGame can interact with this function
	public void sendMessage (final String message)
		{
		try
			{
			sm_dataCastManager.sendDataMessage (message, getString (R.string.cast_namespace));
			}
		catch (IOException e)
			{
			e.printStackTrace ();
			}
		catch (TransientNetworkDisconnectionException e)
			{
			e.printStackTrace ();
			}
		catch (NoConnectionException e)
			{
			e.printStackTrace ();
			}
		}

	/**
	 * Apply a layout to a button within a relative layout.
	 *
	 * @param button
	 * @param centerInParent
	 * @param marginLeft
	 * @param marginTop
	 * @param marginRight
	 * @param marginBottom
	 */
	private void AddButtonLayout (Button button, int centerInParent, int marginLeft, int marginTop, int marginRight, int marginBottom)
		{
		// Defining the layout parameters of the Button
		RelativeLayout.LayoutParams buttonLayoutParameters = new RelativeLayout.LayoutParams (RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		// Add Margin to the LayoutParameters
		buttonLayoutParameters.setMargins (marginLeft, marginTop, marginRight, marginBottom);
		// Add Rule to Layout
		buttonLayoutParameters.addRule (centerInParent);
		// Setting the parameters on the Button
		button.setLayoutParams (buttonLayoutParameters);
		}

	public String getPlayerName ()
		{
		return PreferenceManager.getDefaultSharedPreferences (this)
			       .getString ("pref_player_name_text", "Player");
		}

	public boolean getRoundTimerEnable ()
		{
		SharedPreferences temp = PreferenceManager.getDefaultSharedPreferences (this); // helpful for debug
		return PreferenceManager.getDefaultSharedPreferences (this)
			       .getBoolean ("pref_host_checkbox_round_timer", true);
		}

	public boolean getPostRoundTimerEnable ()
		{
		return PreferenceManager.getDefaultSharedPreferences (this)
			       .getBoolean ("pref_host_checkbox_postround_timer", true);
		}

	public CTriviaPlayer getTriviaPlayer ()
		{
		return m_cTriviaPlayer;
		}

	/*********************************************************************
	 * Helper classes and functions
	 *********************************************************************/
	/**
	 * Creates a radio button customized for a trivia answer
	 */
	private class CAnswerRadioButton
		extends RadioButton
		{
		/**
		 * Constructor for the button.
		 * <p/>
		 * This sets the button's properties so that its text matches the correct answer,
		 * and has the "radio" selector hidden so it looks like the whole button is selected.
		 * Size is to wrap_content, and its onClick listener sets the player's answer and
		 * sends it to the game server.
		 *
		 * @param context
		 * 	(required)  Typically "this"
		 * @param index
		 * 	(required)  Index of the answer to assign this button to
		 */
		public CAnswerRadioButton (Context context, int index, String buttonText)
			{
			super (context);
			// Populate the button with our settings
			setId (index);
			setText (buttonText);
			setChecked (false);  // Default to no selection to remove bias
			// keep button visible for now. TODO. setButtonDrawable (R.drawable.null_selector);
			setVisibility (View.VISIBLE);
			setOnClickListener (new View.OnClickListener ()
			{
			@Override
			public void onClick (View view)
				{
				//TODO: Update graphics to let user know they've clicked something
				((RadioGroup) view.getParent ()).check (view.getId ());                     // Check the radio button
				m_cTriviaPlayer.setAnswer (((RadioButton) view).getText ()
					                           .toString ());    // Save off the answer
				m_cTriviaGame.sendMessage (m_cTriviaPlayer.getAnswer ());                          // Send the answer to the game server
				}
			});
			}
		}
	}
