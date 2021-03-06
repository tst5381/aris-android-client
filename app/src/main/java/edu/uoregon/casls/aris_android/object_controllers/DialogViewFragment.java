package edu.uoregon.casls.aris_android.object_controllers;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Collection;

import edu.uoregon.casls.aris_android.ARISWebView;
import edu.uoregon.casls.aris_android.GamePlayActivity;
import edu.uoregon.casls.aris_android.R;
import edu.uoregon.casls.aris_android.Utilities.AppConfig;
import edu.uoregon.casls.aris_android.data_objects.Dialog;
import edu.uoregon.casls.aris_android.data_objects.DialogCharacter;
import edu.uoregon.casls.aris_android.data_objects.DialogOption;
import edu.uoregon.casls.aris_android.data_objects.DialogScript;
import edu.uoregon.casls.aris_android.data_objects.Instance;
import edu.uoregon.casls.aris_android.data_objects.Media;
import edu.uoregon.casls.aris_android.data_objects.Tab;
import edu.uoregon.casls.aris_android.models.DialogsModel;
import edu.uoregon.casls.aris_android.models.MediaModel;

/**

 */
public class DialogViewFragment extends Fragment {

	public Dialog dialog;
	public DialogsModel dialogsModel;
	public DialogScript dialogScript;
	public DialogCharacter dialogCharacter;
	public Media dialogCharMedia;

	public Instance instance;
	public Tab tab;
	public transient GamePlayActivity mGamePlayAct;

	ScrollView slideUpDialogScriptAndOptionsPanel;
	View       fragView;

	private OnFragmentInteractionListener mListener;

	public DialogViewFragment() {
		// local convenience reference to Parent activity
	}

	public void initWithInstance(Instance i) {
		instance = i;
		dialog = mGamePlayAct.mGame.dialogsModel.dialogForId(i.object_id);
		dialogsModel = mGamePlayAct.mGame.dialogsModel; // convenience ref
//		delegate = d;
//		loadViewElements(dialog.intro_dialog_script_id); // move into onCreateView. Fragment isn't inflated here, so it will die.
	}

	public void initWithTab(Tab tab) {
		instance = mGamePlayAct.mGame.instancesModel.instanceForId(0);
		instance.object_type = tab.type;
		instance.object_id = tab.content_id;
		dialogsModel = mGamePlayAct.mGame.dialogsModel; // convenience ref
		dialog = mGamePlayAct.mGame.dialogsModel.dialogForId(instance.object_id);
//		delegate = d;
		loadViewElements(dialog.intro_dialog_script_id);
	}

	public void initContext(GamePlayActivity gamePlayActivity) {
		mGamePlayAct = gamePlayActivity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mGamePlayAct = (GamePlayActivity) getActivity();
		if (getArguments() != null) {
//			mParam1 = getArguments().getString(ARG_PARAM1);
//			mParam2 = getArguments().getString(ARG_PARAM2);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		fragView = inflater.inflate(R.layout.fragment_dialog_view, container, false);
		loadViewElements(dialog.intro_dialog_script_id);
		return fragView;
	}

	// expects that the dialog Script
	private void loadViewElements(long dialogScriptId) {
		dialogScript = dialogsModel.scriptForId(dialogScriptId); // get Script To Display
		if (dialog.intro_dialog_script_id == 0) dialogScript.dialog_id = dialog.dialog_id; //the 'null script'
		dialogCharacter = dialogsModel.characterForId(dialogScript.dialog_character_id); // get character
		// todo: the media might already be loaded on the device. if it is, use it instead of downloading from remote URL

		// set title
		TextView tvDialogTitle = (TextView) fragView.findViewById(R.id.tv_dialog_title);
		tvDialogTitle.setText(dialogCharacter.title);
		WebView wvCharImage = (WebView) fragView.findViewById(R.id.wv_character_image);

		// set character media (image); iOS = DialogScriptViewController.loadScript
		if (dialogScript.dialog_character_id == 0) { // 0 = you
//		if (dialogCharacter.media_id == 0 || Integer.getInteger(mGamePlayAct.mPlayer.media_id) == 0) { // zero id means there's no custom media for this; use default icon.
			wvCharImage.setBackgroundColor(0x00000000);
			// if player has a stored pic use it
			int playerMediaId = Integer.parseInt(mGamePlayAct.mPlayer.media_id);
			if (playerMediaId != 0) {
				// retrieve stored pic
				Media youChar = mGamePlayAct.mMediaModel.mediaForId(playerMediaId);
				if (youChar != null && youChar.mediaCD != null && youChar.mediaCD.localURL != null) { // todo: Android doesn't do the player pic thing yet. Logic will default to generic icon.
//todo: modularize this webview setup since it's duplicated multiple times.
								// set dialog character image
					wvCharImage.getSettings().setJavaScriptEnabled(true);
					wvCharImage.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
					wvCharImage.getSettings().setLoadWithOverviewMode(true); // causes the content (image) to fit into webview's window size.
					wvCharImage.getSettings().setUseWideViewPort(true); // constrain the image horizontally
					// attempt to fit image to whole screen width
					DisplayMetrics displaymetrics = new DisplayMetrics();
					mGamePlayAct.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
					int screenWidth = displaymetrics.widthPixels;
					int screenHeight = displaymetrics.heightPixels;
					// from: http://stackoverflow.com/a/10395972
					String data = "<html><head><body><center><img width=" + screenWidth + " src=\"" + youChar.localURL().toString() + "\" /></center></body></html>";
					wvCharImage.loadData(data, "text/html", null);
				}
				else {
					wvCharImage.setBackgroundResource(R.drawable.default_character);
				}
			} // fixme: generic icon not appearing in webview
			else {
				wvCharImage.setBackgroundResource(R.drawable.default_character);
			}
		}
		else {
			if (dialogCharacter.media_id != 0) {
				// get custom character media pic.
				MediaModel mm = new MediaModel(mGamePlayAct);
				// get mediaCD (from database)
				dialogCharMedia = mm.mediaForId(dialogCharacter.media_id);

				// set dialog character image
				wvCharImage.getSettings().setJavaScriptEnabled(true);
				wvCharImage.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
				wvCharImage.getSettings().setLoadWithOverviewMode(true); // causes the content (image) to fit into webview's window size.
				wvCharImage.getSettings().setUseWideViewPort(true); // constrain the image horizontally
				// attempt to fit image to whole screen width
				DisplayMetrics displaymetrics = new DisplayMetrics();
				mGamePlayAct.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
				int screenWidth = displaymetrics.widthPixels;
				int screenHeight = displaymetrics.heightPixels;
				// from: http://stackoverflow.com/a/10395972
				String data = "<html><head><body><center><img width=" + screenWidth + " src=\"" + dialogCharMedia.mediaCD.remoteURL.toString() + "\" /></center></body></html>";
				wvCharImage.loadData(data, "text/html", null);
			}
			else {
				wvCharImage.setBackgroundResource(R.drawable.default_character);
			}
		}

		slideUpDialogScriptAndOptionsPanel = (ScrollView) fragView.findViewById(R.id.scrlv_dialog_text_w_options);
		slideUpDialogScriptAndOptionsPanel.setVisibility(View.INVISIBLE); // hide to start.
		ImageButton backBtn = (ImageButton) fragView.findViewById(R.id.ib_dialog_back);
		backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackButtonClick(fragView);
			}
		});

		// set Dialog Script Text:
//		TextView tvScriptText = (TextView) fragView.findViewById(R.id.tv_dialog_script_text);
//		tvScriptText.setText(dialogScript.text);
		ARISWebView awvScriptPrompt = (ARISWebView) fragView.findViewById(R.id.awv_script_prompt);
		awvScriptPrompt.initContextAndInjectJavaScript(mGamePlayAct);
		awvScriptPrompt.loadHTMLString(dialogScript.text);

		// build Dialog Options List
		populateDialogOptionsList(fragView);
		toggleSlidingDialogPanel();

	}

	private void populateDialogOptionsList(View fragView) {
		LinearLayout llDialogOptionsListLayout = (LinearLayout) fragView.findViewById(R.id.ll_dialog_options_list);
		llDialogOptionsListLayout.removeAllViews(); // refresh visible views so they don't accumulate

//		String[] tempText = {"This is the first dialog option, but it is a really long one <i>with some tags also</i>. This will help me align the web layout so long stuff <b>fits properly</b>.",
//				"This is the <b>second</b> diolog option",
//				"<font color=\"red\">This is the third diolog option</font>"};
		Collection<DialogOption> dialogOptions =  dialogsModel.dialogOptions.values();
		// loop through set of all dialog options for this dialog; add them to list.

		int listPosition = 0;
		for (DialogOption dialogOption : dialogOptions) {
			if (dialogOption.parent_dialog_script_id == dialogScript.dialog_script_id) {
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				final View dialogOptionItemView = inflater.inflate(R.layout.dialog_option_list_item, null);

				// set webview to display dialog option prompt
				ARISWebView awvDialogOptionPrompt = (ARISWebView) dialogOptionItemView.findViewById(R.id.awv_dialog_option);
				awvDialogOptionPrompt.initContextAndInjectJavaScript(mGamePlayAct);
				awvDialogOptionPrompt.loadHTMLString(dialogOption.prompt);

//				WebView wvDialogOptionPrompt = (WebView) dialogOptionItemView.findViewById(R.id.wv_dialog_option_prompt);
//				wvDialogOptionPrompt.getSettings().setJavaScriptEnabled(true);
//				wvDialogOptionPrompt.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
//				wvDialogOptionPrompt.getSettings().setLoadWithOverviewMode(true); // causes the content (image) to fit into webview's window size.
////		    	wvDialogOptionPrompt.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
//				String dialogOptionHtml = "<html><body>" + dialogOption.prompt + "</body></html>";
//				wvDialogOptionPrompt.loadData(dialogOptionHtml, "text/html", null);

				final int dialogOptionId = (int)dialogOption.dialog_option_id;
				dialogOptionItemView.setId(dialogOptionId);
				dialogOptionItemView.setTag(dialogOptionId); // redundant but I might find a use for this later.

				awvDialogOptionPrompt.disableUserInteraction();
				dialogOptionItemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						dialogOptionSelected(dialogOptionId);
					}
				});
				// add this dialog option item to the list layout
				llDialogOptionsListLayout.addView(dialogOptionItemView, listPosition++);

			}
		}
	}

	private void dialogOptionSelected(long dialogOptionId) {
		// if the option is another dialog script, just repave .
		DialogOption op = dialogsModel.dialogOptions.get(dialogOptionId);
		Log.d(AppConfig.LOGTAG+AppConfig.LOGTAG_D1, "DialogViewFragment.dialogOptionSelected; link_type: " + op.link_type);
		if (op.link_type.contentEquals("DIALOG_SCRIPT")) {
			this.dialogScriptChosen(dialogsModel.scriptForId((long) op.link_id));
		}
		else if (op.link_type.contentEquals("EXIT")) {
			this.dismissSelf();
		}
		else if (op.link_type.contentEquals("EXIT_TO_PLAQUE")) {
			mGamePlayAct.mGame.displayQueueModel.enqueueObject(mGamePlayAct.mGame.plaquesModel.plaqueForId(op.link_id));
			this.dismissSelf();
		}
		else if (op.link_type.contentEquals("EXIT_TO_ITEM")) {
			mGamePlayAct.mGame.displayQueueModel.enqueueObject(mGamePlayAct.mGame.itemsModel.itemForId(op.link_id));
			this.dismissSelf();
		}
		else if (op.link_type.contentEquals("EXIT_TO_WEB_PAGE")) {
			mGamePlayAct.mGame.displayQueueModel.enqueueObject(mGamePlayAct.mGame.webPagesModel.webPageForId(op.link_id));
			this.dismissSelf();
		}
		else if (op.link_type.contentEquals("EXIT_TO_DIALOG")) {
			// Optimized: reuse the same controllers, just switch it to a new dialog
//			[delegate dialogScriptChosen:[_MODEL_DIALOGS_ scriptForId:[_MODEL_DIALOGS_ dialogForId:op.link_id].intro_dialog_script_id]];
			Dialog d = mGamePlayAct.mGame.dialogsModel.dialogForId(op.link_id);
			this.dialogScriptChosen(dialogsModel.scriptForId(d.intro_dialog_script_id));
		}
		else if (op.link_type.contentEquals("EXIT_TO_TAB")) {
			this.dismissSelf();
			mGamePlayAct.mGame.displayQueueModel.enqueueTab(mGamePlayAct.mGame.tabsModel.tabForId(op.link_id));
		}
//		else
//			mListener.onOtherDialogOptionSelected(dialogOptionId);
	}

	private void dismissSelf() {
		if (tab != null) {
			this.showNav();
		} else if (mListener != null) {
//			Instance i = mGamePlayAct.dialogViewFragment.instance;
			mListener.fragmentDialogDismiss();
		}
	}

	private void showNav() {
		mListener.gamePlayTabBarViewControllerRequestsNav();
	}

	private void dialogScriptChosen(DialogScript chosenScript) {
		// switch dialogs if necessary
		if (chosenScript.dialog_id != dialog.dialog_id) {
			// since Android doesn't have a separate DialogScriptViewController we will just repurpose the view elements
			// here to the new dialog.
			dialog = dialogsModel.dialogForId(chosenScript.dialog_id);
		}

		if (chosenScript.event_package_id > 0)
			mGamePlayAct.mGame.eventsModel.runEventPackageId(chosenScript.event_package_id);
		// Tell server dialog was viewed; response will trigger UI update;
		mGamePlayAct.mGame.logsModel.playerViewedContent("DIALOG_SCRIPT", chosenScript.dialog_script_id);
		if (chosenScript.dialog_character_id == 0) { // "0" indicated this is "you" speaking
			// todo: consider callback to Activity to create whole new DialogFragment
			loadViewElements(chosenScript.dialog_script_id); // more like a "reload..." in this case.
			// note that the character image for "you" is the player Pic. that was presumable taken upon entry.
			//    todo: Do we do that in Android?  if not
			// since Android doesn't have a separate DialogScriptViewController we will just repurpose the view elements
			// here to the new dialog.
			// ignoring you/them distinction in Android for now.
		}
		else { // this is a "them" character speaking (in iOS this really just changes the scroll animation from r-l vs l-r
			loadViewElements(chosenScript.dialog_script_id); // more like a "reload..." in this case.
		}
	}

	public void onBackButtonClick(View v) {
		// todo: finish this
	}

	public void toggleSlidingDialogPanel() {
		if (slideUpDialogScriptAndOptionsPanel.getVisibility() == View.VISIBLE) {
			// hide it
			Animation slideAnim = AnimationUtils.loadAnimation(getContext(),
					R.anim.bottom_down);
			slideUpDialogScriptAndOptionsPanel.startAnimation(slideAnim);
			slideUpDialogScriptAndOptionsPanel.setVisibility(View.INVISIBLE);
		}
		else {
			// show it
			Animation slideAnim = AnimationUtils.loadAnimation(getContext(),
					R.anim.bottom_up);
			slideUpDialogScriptAndOptionsPanel.startAnimation(slideAnim);
			slideUpDialogScriptAndOptionsPanel.setVisibility(View.VISIBLE);
		}
	}


	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnFragmentInteractionListener) {
			mListener = (OnFragmentInteractionListener) context;
		}
		else {
			throw new RuntimeException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
	}

	public interface OnFragmentInteractionListener {
		 void onOtherDialogOptionSelected(long dialogOptionId);

		 void fragmentDialogDismiss();
		 void gamePlayTabBarViewControllerRequestsNav();
	}
	@Override
	public void onDetach() {
		super.onDetach();
//		mListener = null;
	}

}
