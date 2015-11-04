package edu.uoregon.casls.aris_android;

import android.app.ActivityOptions;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.uoregon.casls.aris_android.Utilities.AppUtils;
import edu.uoregon.casls.aris_android.Utilities.Config;
import edu.uoregon.casls.aris_android.Utilities.Dispatcher;
import edu.uoregon.casls.aris_android.Utilities.ResponseHandler;
import edu.uoregon.casls.aris_android.Utilities.Services;
import edu.uoregon.casls.aris_android.data_objects.Game;
import edu.uoregon.casls.aris_android.data_objects.Media;
import edu.uoregon.casls.aris_android.data_objects.User;
import edu.uoregon.casls.aris_android.models.MediaModel;
import edu.uoregon.casls.aris_android.models.UsersModel;

public class GamePlayActivity extends AppCompatActivity // <-- was ActionBarActivity
		implements GamePlayNavDrawerFragment.NavigationDrawerCallbacks, GamePlayMapFragment.OnFragmentInteractionListener {

// Todo 9.29.15: Need to see what happens now when the game tries to load, and then set about setting up the cyclic app status calls

	private final static String TAG_SERVER_SUCCESS = "success";
	public Bundle mTransitionAnimationBndl;
	public User mPlayer; // Sanity note: Now that the game is "playing" we will refer to the logged in User as "Player"
	public Game mGame;
	public Dispatcher mDispatch;
	public Services mServices;
	public ResponseHandler mResposeHandler;
	public MediaModel mMediaModel;
	public UsersModel mUsersModel;
//	public GamesModel mGamesModel; // todo: needed for Android?
	private View mProgressView; // todo: install a progress spinner for server delays
	public JSONObject mJsonAuth;
	public Map<Long, Media> mGameMedia = new LinkedHashMap<>();
	public Map<String, User> mGameUsers = new LinkedHashMap<>();

	/**
	 * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
	 */
	private GamePlayNavDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_play);

		mNavigationDrawerFragment = (GamePlayNavDrawerFragment)
				getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		Gson gson = new Gson();

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mPlayer = new User(extras.getString("user")); // we're now a "Player", BTW.
			//GSON (Slow in debug mode. Ok in regular run mode)
			mGame = gson.fromJson(extras.getString("game"), Game.class);
			mGame.initContext(this); // to allow upward visibility to activities various game/player objects

			try {
				mJsonAuth = new JSONObject(extras.getString("json_auth"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		// tell transitioning activities how to slide. eg: makeCustomAnimation(ctx, howNewMovesIn, howThisMovesOut) -sem
		mTransitionAnimationBndl = ActivityOptions.makeCustomAnimation(getApplicationContext(),
				R.animator.slide_in_from_right, R.animator.slide_out_to_left).toBundle();

		mDispatch = new Dispatcher(); // Centralized place for object to object messaging
		mServices = new Services(); // Centralized place for server calls.
		mResposeHandler = new ResponseHandler(); // Where calls to server return for landing.
		mMediaModel = new MediaModel(this);
		mUsersModel = new UsersModel(this);
		// Set up the drawer. todo: move this to processServerResponse() for call getTabsForPlayer
		mNavigationDrawerFragment.setUp(
				R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
	}

	@Override
	public void onResume() {
		super.onResume();
		mDispatch.initContext(this); // initialize contexts
		mServices.initContext(this);
		mServices.mJsonAuth = this.mJsonAuth; // pass it on
		mGame.initContext(this);
		mResposeHandler.initContext(this);
		// initialize game object's inner classes and variables.
		// Having arrived here in this activity is tantamount to the
		//   "LoadingViewController.gameChosen->RootViewController.startLoading" call hierarchy as in iOS
		mGame.getReadyToPlay();
		// Start barrage of game related server requests
		if (!mGame.hasLatestDownload())
			mGame.requestGameData(); // ... like this. To align with the iOS version
		else { // todo: code in the "restoreGameData" process. See LoadingViewController.startLoading
//			[_MODEL_ restoreGameData];
//			[self gameDataLoaded];
		}
	}

	public void dropItem(long item_id, long qty) {

	}

	private void pollServer(final String requestApi, JSONObject jsonMain) {
//		showProgress(true);
		RequestParams rqParams = new RequestParams();

		final Context context = this;
		String request_url = Config.SERVER_URL_MOBILE + requestApi;

		mPlayer.location = AppUtils.getGeoLocation(context);

		rqParams.put("request", requestApi);
		StringEntity entity;
		entity = null;
		JSONObject jsonAuth = new JSONObject();

		try {
			// place the auth block.
			jsonMain.put("auth", mJsonAuth);
			//place additional required params
//			switch (requestApi) {
//				case (HTTP_GET_NEARBY_GAMES_REQ_API):
//					break;
//				case (HTTP_GET_POPULAR_GAMES_REQ_API):
//					//sample: {"interval":"WEEK","longitude":"-89.409260","user_id":"1","latitude":"43.073128","page":0,"auth":{"user_id":1,"key":"F7...X4"}}
//					break;
//				case (HTTP_GET_PLAYER_GAMES_REQ_API):
//				case (HTTP_GET_RECENT_GAMES_REQ_API): // get player and get recent use the same Req param set.
//					break;
//				case (HTTP_GET_SEARCH_GAMES_REQ_API):
//					break;
//				case (HTTP_GET_FULL_GAME_REQ_API):
//					break;
//				default:
//					break;
//			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		try {
			entity = new StringEntity(jsonMain.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}


		// Post the request
		// 	post data should look like this: {"auth":{"user_id":1,"key":"F7...yzX4"},"game_id":"6"}
		if (AppUtils.isNetworkAvailable(getApplicationContext())) {
			AsyncHttpClient client = new AsyncHttpClient();

			Log.d(Config.LOGTAG,  getClass().getSimpleName() + "AsyncHttpClient Sending Req: " + request_url);
			Log.d(Config.LOGTAG,  getClass().getSimpleName() + "AsyncHttpClient Params for Req: " + jsonMain.toString());
			client.post(context, request_url, entity, "application/json", new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, Header[] headers, JSONObject jsonReturn) {
//					showProgress(false);
					try {
//						processJsonHttpResponse(requestApi, TAG_SERVER_SUCCESS, jsonReturn);
						mResposeHandler.processJsonHttpResponse(requestApi, TAG_SERVER_SUCCESS, jsonReturn);
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
				@Override
				public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
					Log.w(Config.LOGTAG, getClass().getSimpleName() + "AsyncHttpClient failed server call. ", throwable);
//					showProgress(false);
					Toast t = Toast.makeText(getApplicationContext(), "There was a problem receiving data from the server. Please try again, later.",
							Toast.LENGTH_SHORT);
					t.setGravity(Gravity.CENTER, 0, 0);
					t.show();
					super.onFailure(statusCode, headers, responseString, throwable);
				}
			});
		}
		else {
			Toast t = Toast.makeText(getApplicationContext(), "You are not connected to the internet currently. Please try again later.",
					Toast.LENGTH_SHORT);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
		}

	}

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		switch (position) {
			case 0:
				fragmentManager.beginTransaction()
						.replace(R.id.container, GamePlayQuestsFragment.newInstance(position + 1))
						.commit();
				break;
			case 1:
				fragmentManager.beginTransaction()
						.replace(R.id.container, GamePlayMapFragment.newInstance(position + 1))
						.commit();
				break;
		}
	}

	@Override
	public void onNavigationDrawerItemSelected(String itemName) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();

		if (itemName.equals("Quests")) {
			fragmentManager.beginTransaction()
					.replace(R.id.container, GamePlayQuestsFragment.newInstance(itemName))
					.commit();
		}
		else if (itemName.equals("Map")) {
				fragmentManager.beginTransaction()
						.replace(R.id.container, GamePlayMapFragment.newInstance(itemName))
						.commit();
		}
	}

	public void onSectionAttached(int number) {
		switch (number) {
			case 1:
				mTitle = getString(R.string.title_section1);
				break;
			case 2:
				mTitle = getString(R.string.title_section2);
				break;
			case 3:
				mTitle = getString(R.string.title_section3);
				break;
		}
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			// Only show items in the action bar relevant to this screen
			// if the drawer is not showing. Otherwise, let the drawer
			// decide what to show in the action bar.
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onFragmentInteraction(Uri uri) {
		Uri u = uri;
	}

	@Override
	public void onSecondFragButtonClick(String message) {
		String gotit = message;
	}

	public void fetchNoteById(long object_id) { // why is this here?? todo: get this out of here.
		JSONObject jsonAddlData = new JSONObject();
		try {
			jsonAddlData.put("note_id", mGame.game_id);
		} catch (JSONException e) {
			e.printStackTrace();
		}

//		mServices.pollServer(Calls.HTTP_GET_NOTE, jsonAddlData);
	}

//	@Override
//	public void onFragmentInteraction(Uri uri) {
//		Uri u = uri;
//	}

//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_game_play);
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.menu_game_play, menu);
//		return true;
//	}
//
//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//
//		//noinspection SimplifiableIfStatement
//		if (id == R.id.action_settings) {
//			return true;
//		}
//
//		return super.onOptionsItemSelected(item);
//	}
//
//	@Override
//	public void onNavigationDrawerItemSelected(int position) {
//
//	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		Gson gson = new Gson();
		String jsonGame = gson.toJson(mGame);
		savedInstanceState.putString("mGame", jsonGame);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
		Gson gson = new Gson();

		mGame = gson.fromJson(savedInstanceState.getString("mGame"), Game.class); // restore game from stored json in savedInstanceState
		mGame.initContext(this); // reset context
		mGame.initModelContexts(); // re-initialize all the embedded objects' references to the Activity context.
	}

	// Stubs from iOS RootViewController. May be unnecessary in Android vers. but included while developing App just in case they become useful.
	public void gameBegan() { // stub for potential use later to duplicate RootViewController behaviours as exist in iOS vs.
		// in iOS, initializes View Controller. Not much else.
	}
	public void gameChosen() { // stub for potential use later to duplicate RootViewController behaviours as exist in iOS vs.
		// in iOS, starts the game loading sequence.
	}
	public void gameLeft() {
		// in iOS RootViewController, nulls all values kills current gameplayview and returns view to GamesList.
		// pretty much default behaviour in Android Activity stack "back" action. Not needed here;
	}
}
