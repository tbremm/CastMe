package com.adventurpriseme.castme.CCLTriviaActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
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
import com.adventurpriseme.castme.MainApplication;
import com.adventurpriseme.castme.R;
import com.adventurpriseme.castme.TheCastManager.CDataCastConsumer;
import com.adventurpriseme.castme.TriviaGame.CTriviaGame;
import com.adventurpriseme.castme.TriviaGame.ETriviaGameStates;
import com.adventurpriseme.castme.TriviaGame.ITriviaGame;
import com.adventurpriseme.castme.TriviaGame.TriviaPrefsActivity;
import com.google.android.gms.cast.CastDevice;
import com.google.sample.castcompanionlibrary.cast.DataCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;

import java.io.IOException;
import java.util.ArrayList;

public class CCLTriviaActivity
	extends ActionBarActivity
	implements ITriviaGame
	{
	private static final String                              TAG                  = "CCLTriviaActivity";
	private              DataCastManager                     m_dataCastManager    = null;
	private              CDataCastConsumer                   m_dataCastConsumer   = null;
	private              MenuItem                            m_mediaRouteMenuItem = null;
	private              CTriviaGame                         m_cTriviaGame        = null;
	private              CTriviaPlayer                       m_cTriviaPlayer      = null;
	private              SharedPreferences                   m_sharedPreferences  = null;
	private              CDataCastConsumer.eConnectionStates m_eConnectionState   = CDataCastConsumer.eConnectionStates.E_CONNECTION_STATE_UNKNOWN;

	@Override
	protected void onCreate (Bundle savedInstanceState)
		{
		super.onCreate (savedInstanceState);
		DataCastManager.checkGooglePlayServices (this);
		setContentView (R.layout.activity_ccltrivia);   // Init the view... after this, the DataCastConsumer should handle it for us
		m_sharedPreferences = PreferenceManager.getDefaultSharedPreferences (this);
		m_cTriviaPlayer = new CTriviaPlayer (this);
		// FIXME: Fix game creation so that it is dependent on which game the user selects
		m_cTriviaGame = new CTriviaGame (this);
		m_dataCastManager = MainApplication.getDataCastManager ();
		m_dataCastConsumer = new CDataCastConsumer ()
		{
		@Override
		public void updateUIState ()
			{
			// Only update the UI if it is a state change
			if (m_eConnectionState != this.getConnectionState ())
				{
				// Update our state
				m_eConnectionState = this.getConnectionState ();
				// Set our content view
				chooseActivityContentView ();
				}
			}

		@Override
		public void onMessageReceived (CastDevice castDevice, String namespace, String message)
			{
			m_cTriviaGame.onMessageIn (message);
			}
		};
		m_dataCastManager.reconnectSessionIfPossible ();   // Default is 10 seconds
		}

	private void chooseActivityContentView ()
		{
		// Set the activity layout dependant on our connected state
		if (m_dataCastManager == null || !m_dataCastManager.isConnected ())
			{
			setContentView (R.layout.activity_ccltrivia);
			}
		else
			{
			switch (m_eConnectionState)
				{
				case E_CONNECTION_STATE_UNKNOWN:        // Unknown == disconnected as far as we care for now
				case E_CONNECTION_STATE_DISCONNECTED:
					setContentView (R.layout.activity_ccltrivia);
					break;
				case E_CONNECTION_STATE_CONNECTED:
					setContentView (R.layout.activity_play_trivia_on);
					break;
				default:
					Log.e (TAG, "Unhandled connection state...");
					break;
				}
			}
		}

	@Override
	public boolean onCreateOptionsMenu (Menu menu)
		{
		super.onCreateOptionsMenu (menu);
		getMenuInflater ().inflate (R.menu.menu_ccltrivia, menu);
		m_mediaRouteMenuItem = m_dataCastManager.addMediaRouterButton (menu, R.id.media_route_menu_item); // This returns a pointer to the MenuItem that represents the Cast button
		return true;
		}

	@Override
	public boolean onOptionsItemSelected (MenuItem item)
		{
		switch (item.getItemId ())
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
		Log.d (TAG, "Entering onResume()");
		m_dataCastManager = MainApplication.getDataCastManager ();
		m_dataCastManager.addDataCastConsumer (m_dataCastConsumer);
		m_dataCastManager.incrementUiCounter ();
		super.onResume ();
		}

	@Override
	public void onPause ()
		{
		m_dataCastManager.decrementUiCounter ();
		m_dataCastManager.removeDataCastConsumer (m_dataCastConsumer);
		super.onPause ();
		}

	@Override
	protected void onDestroy ()
		{
		Log.d (TAG, "onDestroy is called");
		if (null != m_dataCastManager)
			{
			m_dataCastManager.clearContext (this);
			}
		super.onDestroy ();
		}

	/**
	 * ******************************************************************
	 * Game play code
	 * *******************************************************************
	 */
	public void updateUI (ETriviaGameStates state)
		{
		// TODO Add an onCheckedChangeListener to handle button graphical state?
		// FIXME: There must be a better way to handle the UI state machine.
		// Switch cases have parentheses so that they are individually scoped
		switch (state)
			{
			case GET_CONFIG:
				break;
			case WAITING:
				break;
			case READY:
				break;
			case CONNECTED:
			{
			// Clear the display of UI elements
			setAllUiElements_Visibility (View.INVISIBLE);
			if (m_cTriviaPlayer.getWillHost ())
				{
				Button btnBeginNewRound = (Button) findViewById (R.id.btn_begin_new_round);
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
				TextView tvQuestion = (TextView) findViewById (R.id.tvQuestion);
				tvQuestion.setText (getString (R.string.waiting_for_host));
				tvQuestion.setVisibility (View.VISIBLE);
				}
			m_cTriviaGame.sendMessage (m_cTriviaGame.getOutMsg ());
			break;
			}
			case HOSTING:
			{
			// Clear the display of UI elements
			setAllUiElements_Visibility (View.INVISIBLE);
			m_cTriviaPlayer.setIsHosting (true);
			Button btnBeginNewRound = (Button) findViewById (R.id.btn_begin_new_round);
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
			}
			case HOSTED:    // The game is already hosted, waiting to start
			{
			// Clear the display of UI elements
			setAllUiElements_Visibility (View.INVISIBLE);
			m_cTriviaPlayer.setIsHosting (false);
			TextView tvPlayTitle = (TextView) findViewById (R.id.tvPlayTitle);
			tvPlayTitle.setText (getString (R.string.waiting_on_host));
			tvPlayTitle.setVisibility (View.VISIBLE);
			break;
			}
			case GOT_Q_AND_A:
			{
			// Get all of our GUI elements
			TextView tvPlayTitle = (TextView) findViewById (R.id.tvPlayTitle);
			TextView tvQuestion = (TextView) findViewById (R.id.tvQuestion);
			Button btnBeginNewRound = (Button) findViewById (R.id.btn_begin_new_round);
			RadioGroup rgAnswers = (RadioGroup) findViewById (R.id.radio_group_answers);
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
			}
			case ROUND_WIN:
			{
			// Clear the display of UI elements
			setAllUiElements_Visibility (View.INVISIBLE);
			TextView tvPlayTitle = (TextView) findViewById (R.id.tvPlayTitle);
			tvPlayTitle.setText (getString (R.string.you_win));
			tvPlayTitle.setVisibility (View.VISIBLE);
			if (m_cTriviaPlayer.isHosting ())               // Host gets to short-circuit the question
				{
				Button btnBeginNewRound = (Button) findViewById (R.id.btn_begin_new_round);
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
			}
			case ROUND_LOSE:
			{
			// Clear the display of UI elements
			setAllUiElements_Visibility (View.INVISIBLE);
			TextView tvPlayTitle = (TextView) findViewById (R.id.tvPlayTitle);
			tvPlayTitle.setText (getString (R.string.you_lose));
			tvPlayTitle.setVisibility (View.VISIBLE);
			if (m_cTriviaPlayer.isHosting ())               // Host gets to short-circuit the question
				{
				Button btnBeginNewRound = (Button) findViewById (R.id.btn_begin_new_round);
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
			}
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
		if (relativeLayout != null)
			{
			int numViewElements = relativeLayout.getChildCount ();
			for (int i = 0; i < numViewElements; ++i)
				{
				relativeLayout.getChildAt (i)
					.setVisibility (visibility);
				}
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
			m_dataCastManager.sendDataMessage (message, getString (R.string.cast_namespace));
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
