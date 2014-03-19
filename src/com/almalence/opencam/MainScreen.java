/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is collection of files collectively known as Open Camera.

The Initial Developer of the Original Code is Almalence Inc.
Portions created by Initial Developer are Copyright (C) 2013 
by Almalence Inc. All Rights Reserved.
*/

/* <!-- +++
package com.almalence.opencam_plus;
+++ --> */
// <!-- -+-
package com.almalence.opencam;
//-+- -->


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Area;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.Size;
import android.media.AudioManager;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.almalence.util.AppWidgetNotifier;
import com.almalence.util.Util;

//<!-- -+-
import com.almalence.opencam.billing.IabHelper;
import com.almalence.opencam.billing.IabResult;
import com.almalence.opencam.billing.Inventory;
import com.almalence.opencam.billing.Purchase;
import com.almalence.opencam.ui.AlmalenceGUI;
import com.almalence.opencam.ui.GLLayer;
import com.almalence.opencam.ui.GUI;
import com.almalence.util.AppRater;
//-+- -->
/* <!-- +++
import com.almalence.opencam_plus.ui.AlmalenceGUI;
import com.almalence.opencam_plus.ui.GLLayer;
import com.almalence.opencam_plus.ui.GUI;
+++ --> */

/***
 * MainScreen - main activity screen with camera functionality
 * 
 * Passes all main events to PluginManager
 ***/

public class MainScreen extends Activity implements View.OnClickListener,
		View.OnTouchListener, SurfaceHolder.Callback, Camera.PictureCallback,
		Camera.AutoFocusCallback, Handler.Callback, Camera.ErrorCallback,
		Camera.PreviewCallback, Camera.ShutterCallback {
	// >>Description
	// section with different global parameters available for everyone
	//
	// Camera parameters and possibly access to camera instance
	//
	// Global defines and others
	//
	// Description<<
	public static MainScreen thiz;
	public static Context mainContext;
	public static Handler H;

	public static boolean isHALv3 = true;

	private Object syncObject = new Object();

	private static final int MSG_RETURN_CAPTURED = -1;

	// public static boolean FramesShot = false;

	public static File ForceFilename = null;

//	private static Camera camera = null;
//	private static Camera.Parameters cameraParameters = null;
	
	//Interface to HALv3 camera and Old style camera
	public static CameraController cameraController = null;
	
	//HALv3 camera's objects
//	CameraManager manager = null;
//	private static CameraCharacteristics camCharacter=null;
//	cameraAvailableListener availListener = null;
//	private static CameraDevice camDevice = null;
//	CaptureRequest.Builder previewRequestBuilder = null;
	private static ImageReader mImageReaderYUV;
	private static ImageReader mImageReaderJPEG;
//	String[] cameraIdList={""};
//	
//	public static boolean cameraConfigured = false;
	

	public static GUI guiManager = null;

	// OpenGL layer. May be used to allow capture plugins to draw overlaying
	// preview, such as night vision or panorama frames.
	private static GLLayer glView;

	public boolean mPausing = false;

	Bundle msavedInstanceState;
	// private. if necessary?!?!?
	public SurfaceHolder surfaceHolder;
	public SurfaceView preview;
	private Surface mCameraSurface = null;
	private OrientationEventListener orientListener;
	private boolean landscapeIsNormal = false;
	private boolean surfaceJustCreated = false;
	private boolean surfaceCreated = false;
	public byte[] pviewBuffer;

	// shared between activities
	public static int surfaceWidth, surfaceHeight;
	private static int imageWidth, imageHeight;
	public static int previewWidth, previewHeight;
	private static int saveImageWidth, saveImageHeight;
//	public static PowerManager pm = null;

	private CountDownTimer ScreenTimer = null;
	private boolean isScreenTimerRunning = false;

//	public static int CameraIndex = 0;
//	private static boolean CameraMirrored = false;
	private static boolean wantLandscapePhoto = false;
	public static int orientationMain = 0;
	public static int orientationMainPrevious = 0;

	private SoundPlayer shutterPlayer = null;

//	// Flags to know which camera feature supported at current device
//	public boolean mEVSupported = false;
//	public boolean mSceneModeSupported = false;
//	public boolean mWBSupported = false;
//	public boolean mFocusModeSupported = false;
//	public boolean mFlashModeSupported = false;
//	public boolean mISOSupported = false;
//	public boolean mCameraChangeSupported = false;
//	
//	public boolean mVideoStabilizationSupported = false;
//
//	public static byte[] supportedSceneModes;
//	public static byte[] supportedWBModes;
//	public static byte[] supportedFocusModes;
//	public static byte[] supportedFlashModes;
//	public static byte[] supportedISOModes;

	// Common preferences
	public static String ImageSizeIdxPreference;
	public static boolean ShutterPreference = true;
	public static boolean ShotOnTapPreference = false;
	
	public static boolean showHelp = false;
	// public static boolean FullMediaRescan;
	public static final String SavePathPref = "savePathPref";

	private boolean keepScreenOn = false;
	
	public static String SaveToPath;
	public static boolean SaveInputPreference;
	public static String SaveToPreference;
	public static boolean SortByDataPreference;
	
	public static boolean MaxScreenBrightnessPreference;

	// Camera resolution variables and lists
//	public static final int MIN_MPIX_SUPPORTED = 1280 * 960;
	// public static final int MIN_MPIX_PREVIEW = 600*400;

//	public static int CapIdx;
//
//	public static List<Long> ResolutionsMPixList;
//	public static List<String> ResolutionsIdxesList;
//	public static List<String> ResolutionsNamesList;
//
//	public static List<Long> ResolutionsMPixListIC;
//	public static List<String> ResolutionsIdxesListIC;
//	public static List<String> ResolutionsNamesListIC;
//
//	public static List<Long> ResolutionsMPixListVF;
//	public static List<String> ResolutionsIdxesListVF;
//	public static List<String> ResolutionsNamesListVF;
//
//	public static final int FOCUS_STATE_IDLE = 0;
//	public static final int FOCUS_STATE_FOCUSED = 1;
//	public static final int FOCUS_STATE_FAIL = 3;
//	public static final int FOCUS_STATE_FOCUSING = 4;
//
//	public static final int CAPTURE_STATE_IDLE = 0;
//	public static final int CAPTURE_STATE_CAPTURING = 1;
//
//	private static int mFocusState = FOCUS_STATE_IDLE;
//	private static int mCaptureState = CAPTURE_STATE_IDLE;
	
	private static boolean mAFLocked = false;

	// >>Description
	// section with initialize, resume, start, stop procedures, preferences
	// access
	//
	// Initialize, stop etc depends on plugin type.
	//
	// Create main GUI controls and plugin specific controls.
	//
	// Description<<

	public static boolean isCreating = false;
	public static boolean mApplicationStarted = false;
	public static long startTime = 0;
	
	public static final String EXTRA_ITEM = "WidgetModeID"; //Clicked mode id from widget.
	public static final String EXTRA_TORCH = "WidgetTorchMode";
	
	public static final String EXTRA_SHOP = "WidgetGoShopping";
	
	public static boolean launchTorch = false;
	public static boolean goShopping = false;

	public static final int VOLUME_FUNC_SHUTTER = 0;
	public static final int VOLUME_FUNC_ZOOM 	= 1;
	public static final int VOLUME_FUNC_EXPO 	= 2;
	public static final int VOLUME_FUNC_NONE	= 3;
	
	public static String deviceSS3_01;
	public static String deviceSS3_02;
	public static String deviceSS3_03;
	public static String deviceSS3_04;
	public static String deviceSS3_05;
	public static String deviceSS3_06;
	public static String deviceSS3_07;
	public static String deviceSS3_08;
	public static String deviceSS3_09;
	public static String deviceSS3_10;
	public static String deviceSS3_11;
	public static String deviceSS3_12;
	public static String deviceSS3_13;
	
	public static List<Area> mMeteringAreaMatrix5 = new ArrayList<Area>();	
	public static List<Area> mMeteringAreaMatrix4 = new ArrayList<Area>();	
	public static List<Area> mMeteringAreaMatrix1 = new ArrayList<Area>();	
	public static List<Area> mMeteringAreaCenter = new ArrayList<Area>();	
	public static List<Area> mMeteringAreaSpot = new ArrayList<Area>();
	
//	public static String meteringModeMatrix = "Matrix";
//	public static String meteringModeCenter = "Center-weighted";
//	public static String meteringModeSpot = "Spot";
//	public static String meteringModeAuto = "Auto";
	
	public static int meteringModeAuto = 0;
	public static int meteringModeMatrix = 1;	
	public static int meteringModeCenter = 2;
	public static int meteringModeSpot = 3;
	
	
	public static int currentMeteringMode = -1;
	
	
	public final static String sEvPref = "EvCompensationValue";
	public final static String sSceneModePref = "SceneModeValue";
	public final static String sWBModePref = "WBModeValue";
	public final static String sFrontFocusModePref = "FrontFocusModeValue";
	public final static String sRearFocusModePref = "RearFocusModeValue";
	public final static String sFlashModePref = "FlashModeValue";
	public final static String sISOPref = "ISOValue";
	public final static String sMeteringModePref = "MeteringModeValue";
	
	public static int sDefaultValue = CameraCharacteristics.CONTROL_MODE_AUTO;
	public static int sDefaultFocusValue = CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
	public static int sDefaultFlashValue = CameraCharacteristics.FLASH_MODE_OFF;

//	public final static String isoParam = "iso";
//	public final static String isoParam2 = "iso-speed";
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		deviceSS3_01 = getResources().getString(R.string.device_name_ss3_01);
		deviceSS3_02 = getResources().getString(R.string.device_name_ss3_02);
		deviceSS3_03 = getResources().getString(R.string.device_name_ss3_03);
		deviceSS3_04 = getResources().getString(R.string.device_name_ss3_04);
		deviceSS3_05 = getResources().getString(R.string.device_name_ss3_05);
		deviceSS3_06 = getResources().getString(R.string.device_name_ss3_06);
		deviceSS3_07 = getResources().getString(R.string.device_name_ss3_07);
		deviceSS3_08 = getResources().getString(R.string.device_name_ss3_08);
		deviceSS3_09 = getResources().getString(R.string.device_name_ss3_09);
		deviceSS3_10 = getResources().getString(R.string.device_name_ss3_10);
		deviceSS3_11 = getResources().getString(R.string.device_name_ss3_11);
		deviceSS3_12 = getResources().getString(R.string.device_name_ss3_12);
		deviceSS3_13 = getResources().getString(R.string.device_name_ss3_13);

		Intent intent = this.getIntent();
		String mode = intent.getStringExtra(EXTRA_ITEM);
		launchTorch = intent.getBooleanExtra(EXTRA_TORCH, false);
		goShopping = intent.getBooleanExtra(EXTRA_SHOP, false);
		
		startTime = System.currentTimeMillis();
		msavedInstanceState = savedInstanceState;
		mainContext = this.getBaseContext();
		H = new Handler(this);
		thiz = this;

		mApplicationStarted = false;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// ensure landscape orientation
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		// set to fullscreen
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// set some common view here
		setContentView(R.layout.opencamera_main_layout);
		
		//reset or save settings
		ResetOrSaveSettings();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
		
		if(null != mode)
			prefs.edit().putString("defaultModeName", mode).commit();
		
		if(launchTorch)
			prefs.edit().putString(sFlashModePref, getResources().getString(R.string.flashTorchSystem)).commit();
		
		// <!-- -+-
		
		/**** Billing *****/
		if (true == prefs.contains("unlock_all_forever")) {
			unlockAllPurchased = prefs.getBoolean("unlock_all_forever", false);
		}
		if (true == prefs.contains("plugin_almalence_hdr")) {
			hdrPurchased = prefs.getBoolean("plugin_almalence_hdr", false);
		}
		if (true == prefs.contains("plugin_almalence_panorama")) {
			panoramaPurchased = prefs.getBoolean("plugin_almalence_panorama", false);
		}
		if (true == prefs.contains("plugin_almalence_moving_burst")) {
			objectRemovalBurstPurchased = prefs.getBoolean("plugin_almalence_moving_burst", false);
		}
		if (true == prefs.contains("plugin_almalence_groupshot")) {
			groupShotPurchased = prefs.getBoolean("plugin_almalence_groupshot", false);
		}
		
		createBillingHandler();
		/**** Billing *****/
		
		//application rating helper
		AppRater.app_launched(this);
		//-+- -->
		
		AppWidgetNotifier.app_launched(this);
		
		
		
		
		cameraController = CameraController.getInstance();
		cameraController.onCreate();	
		
		
		
		// set preview, on click listener and surface buffers
		preview = (SurfaceView) this.findViewById(R.id.SurfaceView01);
		preview.setZOrderMediaOverlay(true);
		preview.setOnClickListener(this);
		preview.setOnTouchListener(this);
		preview.setKeepScreenOn(true);

		surfaceHolder = preview.getHolder();
		//surfaceHolder.addCallback(this);
		//surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		orientListener = new OrientationEventListener(this) {
			@Override
			public void onOrientationChanged(int orientation) {
				// figure landscape or portrait
				if (MainScreen.thiz.landscapeIsNormal) {
					// Log.e("MainScreen",
					// "landscapeIsNormal = true. Orientation " + orientation +
					// "+90");
					orientation += 90;
				}
				// else
				// Log.e("MainScreen", "landscapeIsNormal = false. Orientation "
				// + orientation);

				if ((orientation < 45)
						|| (orientation > 315 && orientation < 405)
						|| ((orientation > 135) && (orientation < 225))) {
					if (MainScreen.wantLandscapePhoto == true) {
						MainScreen.wantLandscapePhoto = false;
						// Log.e("MainScreen", "Orientation = " + orientation);
						// Log.e("MainScreen","Orientation Changed. wantLandscapePhoto = "
						// + String.valueOf(MainScreen.wantLandscapePhoto));
						//PluginManager.getInstance().onOrientationChanged(false);
					}
				} else {
					if (MainScreen.wantLandscapePhoto == false) {
						MainScreen.wantLandscapePhoto = true;
						// Log.e("MainScreen", "Orientation = " + orientation);
						// Log.e("MainScreen","Orientation Changed. wantLandscapePhoto = "
						// + String.valueOf(MainScreen.wantLandscapePhoto));
						//PluginManager.getInstance().onOrientationChanged(true);
					}
				}

				// orient properly for video
				if ((orientation > 135) && (orientation < 225))
					orientationMain = 270;
				else if ((orientation < 45) || (orientation > 315))
					orientationMain = 90;
				else if ((orientation < 325) && (orientation > 225))
					orientationMain = 0;
				else if ((orientation < 135) && (orientation > 45))
					orientationMain = 180;
				
				if(orientationMain != orientationMainPrevious)
				{
					orientationMainPrevious = orientationMain;
					//PluginManager.getInstance().onOrientationChanged(orientationMain);
				}
			}
		};

		//pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		keepScreenOn = prefs.getBoolean("keepScreenOn", false);
		// prevent power drain
//		if (!keepScreenOn)
		{
			ScreenTimer = new CountDownTimer(180000, 180000) {
				public void onTick(long millisUntilFinished) {
				}
	
				public void onFinish() {
					boolean isVideoRecording = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext).getBoolean("videorecording", false);
					if (isVideoRecording || keepScreenOn)
					{
						//restart timer
						ScreenTimer.start();
						isScreenTimerRunning = true;
						preview.setKeepScreenOn(true);
						return;
					}
					preview.setKeepScreenOn(false);
					isScreenTimerRunning = false;
				}
			};
			ScreenTimer.start();
			isScreenTimerRunning = true;
		}

		PluginManager.getInstance().setupDefaultMode();
		// Description
		// init gui manager
		guiManager = new AlmalenceGUI();
		guiManager.createInitialGUI();
		this.findViewById(R.id.mainLayout1).invalidate();
		this.findViewById(R.id.mainLayout1).requestLayout();
		guiManager.onCreate();

		// init plugin manager
		PluginManager.getInstance().onCreate();

		if (this.getIntent().getAction() != null) {
			if (this.getIntent().getAction()
					.equals(MediaStore.ACTION_IMAGE_CAPTURE)) {
				try {
					MainScreen.ForceFilename = new File(
							((Uri) this.getIntent().getExtras()
									.getParcelable(MediaStore.EXTRA_OUTPUT))
									.getPath());
					if (MainScreen.ForceFilename.getAbsolutePath().equals(
							"/scrapSpace")) {
						MainScreen.ForceFilename = new File(Environment
								.getExternalStorageDirectory()
								.getAbsolutePath()
								+ "/mms/scrapSpace/.temp.jpg");
						new File(MainScreen.ForceFilename.getParent()).mkdirs();
					}
				} catch (Exception e) {
					MainScreen.ForceFilename = null;
				}
			} else {
				MainScreen.ForceFilename = null;
			}
		} else {
			MainScreen.ForceFilename = null;
		}
		
		// <!-- -+-
		if(goShopping)
		{
			MainScreen.thiz.showUnlock = true;
			if (MainScreen.thiz.titleUnlockAll == null || MainScreen.thiz.titleUnlockAll.endsWith("check for sale"))
			{
				Toast.makeText(MainScreen.mainContext, "Error connecting to Google Play. Check internet connection.", Toast.LENGTH_LONG).show();
				return;
			}
			Intent shopintent = new Intent(MainScreen.thiz, Preferences.class);
			MainScreen.thiz.startActivity(shopintent);
		}
		//-+- -->
	}

	public void onPreferenceCreate(PreferenceFragment prefActivity)
	{
		CharSequence[] entries;
		CharSequence[] entryValues;

		if (CameraController.ResolutionsIdxesList != null) {
			entries = CameraController.ResolutionsNamesList
					.toArray(new CharSequence[CameraController.ResolutionsNamesList.size()]);
			entryValues = CameraController.ResolutionsIdxesList
					.toArray(new CharSequence[CameraController.ResolutionsIdxesList.size()]);

			
			ListPreference lp = (ListPreference) prefActivity
					.findPreference("imageSizePrefCommonBack");
			ListPreference lp2 = (ListPreference) prefActivity
					.findPreference("imageSizePrefCommonFront");
			
			if(CameraController.CameraIndex == 0 && lp2 != null && lp != null)
			{
				prefActivity.getPreferenceScreen().removePreference(lp2);
				lp.setEntries(entries);
				lp.setEntryValues(entryValues);
			}
			else if(lp2 != null && lp != null)
			{
				prefActivity.getPreferenceScreen().removePreference(lp);
				lp2.setEntries(entries);
				lp2.setEntryValues(entryValues);
			}
			else
				return;

			// set currently selected image size
			int idx;
			for (idx = 0; idx < CameraController.ResolutionsIdxesList.size(); ++idx) {
				if (Integer.parseInt(CameraController.ResolutionsIdxesList.get(idx)) == CameraController.CapIdx) {
					break;
				}
			}
			if (idx < CameraController.ResolutionsIdxesList.size()) {
				if(CameraController.CameraIndex == 0)
					lp.setValueIndex(idx);
				else
					lp2.setValueIndex(idx);
			}
			if(CameraController.CameraIndex == 0)
			lp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				// @Override
				public boolean onPreferenceChange(Preference preference,
						Object newValue) {
					int value = Integer.parseInt(newValue.toString());
					CameraController.CapIdx = value;
					return true;
				}
			});
			else
				lp2.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
					// @Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						int value = Integer.parseInt(newValue.toString());
						CameraController.CapIdx = value;
						return true;
					}
				});
				
		}
	}

	public void queueGLEvent(final Runnable runnable)
	{
		final GLSurfaceView surfaceView = glView;

		if (surfaceView != null && runnable != null) {
			surfaceView.queueEvent(runnable);
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		MainScreen.cameraController.onStart();
		MainScreen.guiManager.onStart();
		PluginManager.getInstance().onStart();
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		mApplicationStarted = false;
		orientationMain = 0;
		orientationMainPrevious = 0;
		MainScreen.guiManager.onStop();
		PluginManager.getInstance().onStop();
		MainScreen.cameraController.onStop();
		
		if(isHALv3)
		{
			// IamgeReader should be closed
			if (mImageReaderYUV != null)
			{
				mImageReaderYUV.close();
				mImageReaderYUV = null;
			}
			if (mImageReaderJPEG != null)
			{
				mImageReaderJPEG.close();
				mImageReaderJPEG = null;
			}
		}
	}

	@Override
	protected void onDestroy()
	{	
		super.onDestroy();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
		if(launchTorch && prefs.getString(sFlashModePref, "").contains(getResources().getString(R.string.flashTorchSystem)))
		{
			prefs.edit().putString(sFlashModePref, getResources().getString(R.string.flashAutoSystem)).commit();
		}
		MainScreen.guiManager.onDestroy();
		PluginManager.getInstance().onDestroy();
		MainScreen.cameraController.onDestroy();

		// <!-- -+-
		/**** Billing *****/
		destroyBillingHandler();
		/**** Billing *****/
		//-+- -->
		
		glView = null;
	}

	@Override
	protected void onResume()
	{		
		super.onResume();
		
		if (!isCreating)
		{			
			if(!isHALv3)
			{
				new CountDownTimer(50, 50) 
				{
					public void onTick(long millisUntilFinished)
					{
					}
	
					public void onFinish()
					{
						onResumeMain(false);
					}
				}.start();
			}
			else
			{
				onResumeMain(true);
			}
		}
	}
	
	private void onResumeMain(boolean isHALv3)
	{
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(MainScreen.mainContext);
		CameraController.CameraIndex = prefs.getBoolean("useFrontCamera", false) == false ? 0
				: 1;
		ShutterPreference = prefs.getBoolean("shutterPrefCommon",
				false);
		ShotOnTapPreference = prefs.getBoolean("shotontapPrefCommon",
				false);
		ImageSizeIdxPreference = prefs.getString(CameraController.CameraIndex == 0 ?
				"imageSizePrefCommonBack" : "imageSizePrefCommonFront", "-1");
		Log.e("MainScreen", "ImageSizeIdxPreference = " + ImageSizeIdxPreference);
		// FullMediaRescan = prefs.getBoolean("mediaPref", true);
		SaveToPath = prefs.getString(SavePathPref, Environment
				.getExternalStorageDirectory().getAbsolutePath());
		SaveInputPreference = prefs.getBoolean("saveInputPref",
				false);
		SaveToPreference = prefs.getString("saveToPref", "0");
		SortByDataPreference = prefs.getBoolean("sortByDataPref",
				false);
		
		MaxScreenBrightnessPreference = prefs.getBoolean("maxScreenBrightnessPref", false);
		setScreenBrightness(MaxScreenBrightnessPreference);

		MainScreen.guiManager.onResume();
		PluginManager.getInstance().onResume();
		MainScreen.thiz.mPausing = false;
		
		MainScreen.thiz.findViewById(R.id.mainLayout2).setVisibility(View.VISIBLE);
		
		if(isHALv3)
		{
			setupCamera(null);
		}
		else
		{
			if (surfaceCreated && (CameraController.camera == null)) {
				MainScreen.thiz.findViewById(R.id.mainLayout2)
						.setVisibility(View.VISIBLE);
				setupCamera(surfaceHolder);				
			}			
		}

		if (glView != null && CameraController.camDevice != null)
			glView.onResume();
		
		PluginManager.getInstance().onGUICreate();
		MainScreen.guiManager.onGUICreate();
		orientListener.enable();
		

		shutterPlayer = new SoundPlayer(this.getBaseContext(), getResources()
				.openRawResourceFd(R.raw.plugin_capture_tick));

		if (ScreenTimer != null) {
			if (isScreenTimerRunning)
				ScreenTimer.cancel();
			ScreenTimer.start();
			isScreenTimerRunning = true;
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		mApplicationStarted = false;
		CameraController.cameraConfigured = false;

		MainScreen.guiManager.onPause();
		PluginManager.getInstance().onPause(true);

		orientListener.disable();

		// initiate full media rescan
		// if (FramesShot && FullMediaRescan)
		// {
		// // using MediaScannerConnection.scanFile(this, paths, null, null);
		// instead
		// sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
		// Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
		// FramesShot = false;
		// }

		if (ShutterPreference) {
			AudioManager mgr = (AudioManager) MainScreen.thiz
					.getSystemService(MainScreen.mainContext.AUDIO_SERVICE);
			mgr.setStreamMute(AudioManager.STREAM_SYSTEM, false);
		}

		this.mPausing = true;

		if (glView != null) {
			glView.onPause();
		}

		if (ScreenTimer != null) {
			if (isScreenTimerRunning)
				ScreenTimer.cancel();
			isScreenTimerRunning = false;
		}

		//reset torch
		if(!isHALv3)
		{
			try 
	    	{
	    		 Camera.Parameters p = getCameraParameters();
	    		 if (p != null && isFlashModeSupported())
	        	 {	
	    			 p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
	    			 setCameraParameters(p);
	        	 }
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("MainScreen", "Torch exception: " + e.getMessage());
			}		
		
			if (CameraController.camera != null)
			{
				CameraController.camera.setPreviewCallback(null);
				CameraController.camera.stopPreview();
				CameraController.camera.release();
				CameraController.camera = null;
				CameraController.cameraParameters = null;
			}
		}
		else
		{
			// HALv3 code -----------------------------------------
			if ((cameraController.availListener != null) && (cameraController.manager != null))
				cameraController.manager.removeAvailabilityListener(cameraController.availListener);
			
			if (CameraController.camDevice != null)
			{
				CameraController.camDevice.close();
				CameraController.camDevice = null;
			}
		}
		
		this.findViewById(R.id.mainLayout2).setVisibility(View.INVISIBLE);

		if (shutterPlayer != null) {
			shutterPlayer.release();
			shutterPlayer = null;
		}
	}

	public void PauseMain() {
		onPause();
	}

	public void StopMain() {
		onStop();
	}

	public void StartMain() {
		onStart();
	}

	public void ResumeMain() {
		onResume();
	}

	@Override
	public void surfaceChanged(final SurfaceHolder holder, final int format,
			final int width, final int height) {

		if (!isCreating)
		{
//			new CountDownTimer(50, 50) {
//				public void onTick(long millisUntilFinished) {
//
//				}
//
//				public void onFinish() {
//					SharedPreferences prefs = PreferenceManager
//							.getDefaultSharedPreferences(MainScreen.mainContext);
//					CameraIndex = prefs.getBoolean("useFrontCamera", false) == false ? 0
//							: 1;
//					ShutterPreference = prefs.getBoolean("shutterPrefCommon",
//							false);
//					ShotOnTapPreference = prefs.getBoolean("shotontapPrefCommon",
//							false);
//					ImageSizeIdxPreference = prefs.getString(CameraIndex == 0 ?
//							"imageSizePrefCommonBack" : "imageSizePrefCommonFront", "-1");
//					// FullMediaRescan = prefs.getBoolean("mediaPref", true);
//
//					if (!MainScreen.thiz.mPausing && surfaceCreated
//							&& (camera == null)) {
//						surfaceWidth = width;
//						surfaceHeight = height;
//						MainScreen.thiz.findViewById(R.id.mainLayout2)
//								.setVisibility(View.VISIBLE);
//						
//						H.sendEmptyMessage(PluginManager.MSG_SURFACE_READY);
////						setupCamera(holder);
////						PluginManager.getInstance().onGUICreate();
////						MainScreen.guiManager.onGUICreate();
//					}
//				}
//			}.start();
			
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(MainScreen.mainContext);
			CameraController.CameraIndex = prefs.getBoolean("useFrontCamera", false) == false ? 0
					: 1;
			ShutterPreference = prefs.getBoolean("shutterPrefCommon",
					false);
			ShotOnTapPreference = prefs.getBoolean("shotontapPrefCommon",
					false);
			ImageSizeIdxPreference = prefs.getString(CameraController.CameraIndex == 0 ?
					"imageSizePrefCommonBack" : "imageSizePrefCommonFront", "-1");
			// FullMediaRescan = prefs.getBoolean("mediaPref", true);

			if (!MainScreen.thiz.mPausing && surfaceCreated
					&& (CameraController.camera == null)) {
				surfaceWidth = width;
				surfaceHeight = height;
				MainScreen.thiz.findViewById(R.id.mainLayout2)
						.setVisibility(View.VISIBLE);
				
				H.sendEmptyMessage(PluginManager.MSG_SURFACE_READY);
//				setupCamera(holder);
//				PluginManager.getInstance().onGUICreate();
//				MainScreen.guiManager.onGUICreate();
			}
		}
		else {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(MainScreen.mainContext);
			CameraController.CameraIndex = prefs.getBoolean("useFrontCamera", false) == false ? 0
					: 1;
			ShutterPreference = prefs.getBoolean("shutterPrefCommon", false);
			ShotOnTapPreference = prefs.getBoolean("shotontapPrefCommon",false);
			ImageSizeIdxPreference = prefs.getString(CameraController.CameraIndex == 0 ?
					"imageSizePrefCommonBack" : "imageSizePrefCommonFront",
					"-1");
			// FullMediaRescan = prefs.getBoolean("mediaPref", true);

			if (!MainScreen.thiz.mPausing && surfaceCreated && (CameraController.camera == null)) {
				surfaceWidth = width;
				surfaceHeight = height;
			}
		}
	}


//	@TargetApi(9)
//	protected void openCameraFrontOrRear() {
//		if (Camera.getNumberOfCameras() > 0) {
//			camera = Camera.open(CameraController.CameraIndex);
//		}
//
//		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
//		Camera.getCameraInfo(CameraIndex, cameraInfo);
//
//		if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
//			CameraMirrored = true;
//		else
//			CameraMirrored = false;
//	}

	public void setupCamera(SurfaceHolder surface) {
//		if (camera == null) {
//			try {
//				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//					openCameraFrontOrRear();
//				} else {
//					camera = Camera.open();
//				}
//			} catch (RuntimeException e) {
//				camera = null;
//			}
//
//			if (camera == null) {
//				Toast.makeText(MainScreen.thiz, "Unable to start camera", Toast.LENGTH_LONG).show();
//				return;
//			}
//		}
//		
//		cameraParameters = camera.getParameters(); //Initialize of camera parameters variable





		// HALv3 open camera -----------------------------------------------------------------
		try
		{
			cameraController.manager.openCamera (cameraController.cameraIdList[CameraController.CameraIndex], new openListener(), null);
		}
		catch (CameraAccessException e)
		{
			Log.d("MainScreen", "manager.openCamera failed");
			e.printStackTrace();
		}
		
		// find suitable image sizes for preview and capture
		try	{
			CameraController.camCharacter = cameraController.manager.getCameraCharacteristics(cameraController.cameraIdList[CameraController.CameraIndex]);
		} catch (CameraAccessException e) {
			Log.d("MainScreen", "getCameraCharacteristics failed");
			e.printStackTrace();
		}
		
		if (CameraController.camCharacter.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT)
			CameraController.CameraMirrored = true;
		else
			CameraController.CameraMirrored = false;
		
		// Add an Availability Listener as Cameras become available or unavailable
		cameraController.availListener = cameraController.new cameraAvailableListener();
		cameraController.manager.addAvailabilityListener(cameraController.availListener, null);
		
		cameraController.mVideoStabilizationSupported = CameraController.camCharacter.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES) == null? false : true;
		
		// check that full hw level is supported
		if (CameraController.camCharacter.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL) != CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL) 
			H.sendEmptyMessage(PluginManager.MSG_NOT_LEVEL_FULL);		
		// ^^ HALv3 open camera -----------------------------------------------------------------		
		
		
		
		
		
		
//		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH)
//	    	mVideoStabilizationSupported = isVideoStabilizationSupported();

		PluginManager.getInstance().SelectDefaults();

		// screen rotation
//		try {
//			camera.setDisplayOrientation(90);
//		} catch (RuntimeException e) {
//			e.printStackTrace();
//		}
//
//		try {
//			camera.setPreviewDisplay(holder);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//		if (MainScreen.camera == null)
//			return;
		//Camera.Parameters cp = MainScreen.cameraParameters;
		
		PopulateCameraDimensions();
		CameraController.ResolutionsMPixListIC = CameraController.ResolutionsMPixList;
		CameraController.ResolutionsIdxesListIC = CameraController.ResolutionsIdxesList;
		CameraController.ResolutionsNamesListIC = CameraController.ResolutionsNamesList;

		PluginManager.getInstance().SelectImageDimension(); // updates SX, SY
															// values

		
		//surfaceHolder.setFixedSize(MainScreen.imageWidth, MainScreen.imageHeight);
		surfaceHolder.setFixedSize(1280, 720);
		surfaceHolder.addCallback(this);
		
		// HALv3 code -------------------------------------------------------------------
//		if (mImageReader == null)
//		{
//	        mImageReader = ImageReader.newInstance(MainScreen.imageWidth, MainScreen.imageHeight, ImageFormat.YUV_420_888, 2);
//			mImageReader.setOnImageAvailableListener(new imageAvailableListener(), null);
//		}
		
		mImageReaderYUV = ImageReader.newInstance(MainScreen.imageWidth, MainScreen.imageHeight, ImageFormat.YUV_420_888, 2);
		mImageReaderYUV.setOnImageAvailableListener(new imageAvailableListener(), null);
		
		mImageReaderJPEG = ImageReader.newInstance(MainScreen.imageWidth, MainScreen.imageHeight, ImageFormat.JPEG, 2);
		mImageReaderJPEG.setOnImageAvailableListener(new imageAvailableListener(), null);
		
		// prepare list of surfaces to be used in capture requests
//		List<Surface> sfl = new ArrayList<Surface>();
//		
//		sfl.add(mCameraSurface);				// surface for viewfinder preview		
//		sfl.add(mImageReader.getSurface());		// surface for image capture
//
//		// configure camera with all the surfaces to be ever used
//		try {
//			camDevice.configureOutputs(sfl);
//		} catch (CameraAccessException e)	{
//			Log.d("MainScreen", "setting up configureOutputs failed");
//			e.printStackTrace();
//		}
//		
//		try
//		{
//			previewRequestBuilder = camDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//			previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//			previewRequestBuilder.addTarget(mCameraSurface);			
//		}
//		catch (CameraAccessException e)
//		{
//			Log.d("MainScreen", "setting up preview failed");
//			e.printStackTrace();
//		}
//		// ^^ HALv3 code -------------------------------------------------------------------
//		
//		
//		
//		// ----- Select preview dimensions with ratio correspondent to full-size
//		// image
////		PluginManager.getInstance().SetCameraPreviewSize(cameraParameters);
////
////		guiManager.setupViewfinderPreviewSize(cameraParameters);
//
////		Size previewSize = cameraParameters.getPreviewSize();
//
//		if (PluginManager.getInstance().isGLSurfaceNeeded()) {
//			if (glView == null) {
//				glView = new GLLayer(MainScreen.mainContext);// (GLLayer)findViewById(R.id.SurfaceView02);
//				glView.setLayoutParams(new LayoutParams(
//						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
//				((RelativeLayout) findViewById(R.id.mainLayout2)).addView(
//						glView, 1);
//				glView.setZOrderMediaOverlay(true);
//				glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
//			}
//		} else {
//			((RelativeLayout) findViewById(R.id.mainLayout2))
//					.removeView(glView);
//			glView = null;
//		}
//
//		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) preview
//				.getLayoutParams();
//		if (glView != null) {
//			glView.setVisibility(View.VISIBLE);
//			glView.setLayoutParams(lp);
//		} else {
//			if (glView != null)
//				glView.setVisibility(View.GONE);
//		}
//
////		pviewBuffer = new byte[previewSize.width
////				* previewSize.height
////				* ImageFormat.getBitsPerPixel(cameraParameters
////						.getPreviewFormat()) / 8];
//
////		camera.setErrorCallback(MainScreen.thiz);
//
//		supportedSceneModes = getSupportedSceneModes();
//		supportedWBModes = getSupportedWhiteBalance();
//		supportedFocusModes = getSupportedFocusModes();
//		supportedFlashModes = getSupportedFlashModes();
//		supportedISOModes = getSupportedISO();
//
//		PluginManager.getInstance().SetCameraPictureSize();
//		PluginManager.getInstance().SetupCameraParameters();
//		//cp = cameraParameters;
//
////		try {
////			//Log.i("CameraTest", Build.MODEL);
////			if (Build.MODEL.contains("Nexus 5"))
////			{
////				cameraParameters.setPreviewFpsRange(7000, 30000);
////				setCameraParameters(cameraParameters);
////			}
////			
////			//Log.i("CameraTest", "fps ranges "+range.size()+" " + range.get(0)[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] + " " + range.get(0)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
////			//cameraParameters.setPreviewFpsRange(range.get(0)[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], range.get(0)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
////			//cameraParameters.setPreviewFpsRange(7000, 30000);
////			// an obsolete but much more reliable way of setting preview to a reasonable fps range
////			// Nexus 5 is giving preview which is too dark without this
////			//cameraParameters.setPreviewFrameRate(30);
////		
////			
////		} catch (RuntimeException e) {
////			Log.e("CameraTest", "MainScreen.setupCamera unable setParameters "
////					+ e.getMessage());
////		}
////
////		previewWidth = cameraParameters.getPreviewSize().width;
////		previewHeight = cameraParameters.getPreviewSize().height;
//
////		Util.initialize(mainContext);
////		Util.initializeMeteringMatrix();
////		
////		prepareMeteringAreas();
//
//		guiManager.onCameraCreate();
//		PluginManager.getInstance().onCameraParametersSetup();
//		guiManager.onPluginsInitialized();
//
//		// ----- Start preview and setup frame buffer if needed
//
//		// ToDo: call camera release sequence from onPause somewhere ???
//		new CountDownTimer(10, 10) {
//			@Override
//			public void onFinish() {
////				try // exceptions sometimes happen here when resuming after
////					// processing
////				{
////					camera.startPreview();
////				} catch (RuntimeException e) {
////					Toast.makeText(MainScreen.thiz, "Unable to start camera", Toast.LENGTH_LONG).show();
////					return;
////				}
////
////				camera.setPreviewCallbackWithBuffer(MainScreen.thiz);
////				camera.addCallbackBuffer(pviewBuffer);
//
//				PluginManager.getInstance().onCameraSetup();
//				guiManager.onCameraSetup();
//				MainScreen.mApplicationStarted = true;
//			}
//
//			@Override
//			public void onTick(long millisUntilFinished) {
//			}
//		}.start();
	}
	
	
	public void configureCamera()
	{
		Log.e("MainScreen", "configureCamera()");
		// prepare list of surfaces to be used in capture requests
		List<Surface> sfl = new ArrayList<Surface>();
		
		sfl.add(mCameraSurface);				// surface for viewfinder preview		
		sfl.add(mImageReaderYUV.getSurface());		// surface for yuv image capture
		sfl.add(mImageReaderJPEG.getSurface());		// surface for jpeg image capture

		// configure camera with all the surfaces to be ever used
		try {
			CameraController.camDevice.configureOutputs(sfl);
		} catch (CameraAccessException e)	{
			Log.e("MainScreen", "configureOutputs failed. CameraAccessException");
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) 
		{
			Log.e("MainScreen", "configureOutputs failed. IllegalArgumentException");
			e.printStackTrace();
		}
		catch (IllegalStateException e) 
		{
			Log.e("MainScreen", "configureOutputs failed. IllegalStateException");
			e.printStackTrace();
		}
		
		try
		{
			cameraController.previewRequestBuilder = CameraController.camDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
			cameraController.previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
			cameraController.previewRequestBuilder.addTarget(mCameraSurface);
			try {
				CameraController.camDevice.setRepeatingRequest(cameraController.previewRequestBuilder.build(), null, null);
			} catch (CameraAccessException e) {
				e.printStackTrace();
			}
		}
		catch (CameraAccessException e)
		{
			Log.d("MainScreen", "setting up preview failed");
			e.printStackTrace();
		}
		// ^^ HALv3 code -------------------------------------------------------------------
		
		
		
		// ----- Select preview dimensions with ratio correspondent to full-size
		// image
//				PluginManager.getInstance().SetCameraPreviewSize(cameraParameters);
//
//				guiManager.setupViewfinderPreviewSize(cameraParameters);

//				Size previewSize = cameraParameters.getPreviewSize();

		if (PluginManager.getInstance().isGLSurfaceNeeded()) {
			if (glView == null) {
				glView = new GLLayer(MainScreen.mainContext);// (GLLayer)findViewById(R.id.SurfaceView02);
				glView.setLayoutParams(new LayoutParams(
						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				((RelativeLayout) findViewById(R.id.mainLayout2)).addView(
						glView, 1);
				glView.setZOrderMediaOverlay(true);
				glView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
			}
		} else {
			((RelativeLayout) findViewById(R.id.mainLayout2))
					.removeView(glView);
			glView = null;
		}

		RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) preview
				.getLayoutParams();
		if (glView != null) {
			glView.setVisibility(View.VISIBLE);
			glView.setLayoutParams(lp);
		} else {
			if (glView != null)
				glView.setVisibility(View.GONE);
		}

//				pviewBuffer = new byte[previewSize.width
//						* previewSize.height
//						* ImageFormat.getBitsPerPixel(cameraParameters
//								.getPreviewFormat()) / 8];

//				camera.setErrorCallback(MainScreen.thiz);

		CameraController.supportedSceneModes = getSupportedSceneModes();
		CameraController.supportedWBModes = getSupportedWhiteBalance();
		CameraController.supportedFocusModes = getSupportedFocusModes();
		CameraController.supportedFlashModes = getSupportedFlashModes();
		CameraController.supportedISOModes = getSupportedISO();

		PluginManager.getInstance().SetCameraPictureSize();
		PluginManager.getInstance().SetupCameraParameters();
		//cp = cameraParameters;

//				try {
//					//Log.i("CameraTest", Build.MODEL);
//					if (Build.MODEL.contains("Nexus 5"))
//					{
//						cameraParameters.setPreviewFpsRange(7000, 30000);
//						setCameraParameters(cameraParameters);
//					}
//					
//					//Log.i("CameraTest", "fps ranges "+range.size()+" " + range.get(0)[Camera.Parameters.PREVIEW_FPS_MIN_INDEX] + " " + range.get(0)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
//					//cameraParameters.setPreviewFpsRange(range.get(0)[Camera.Parameters.PREVIEW_FPS_MIN_INDEX], range.get(0)[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
//					//cameraParameters.setPreviewFpsRange(7000, 30000);
//					// an obsolete but much more reliable way of setting preview to a reasonable fps range
//					// Nexus 5 is giving preview which is too dark without this
//					//cameraParameters.setPreviewFrameRate(30);
//				
//					
//				} catch (RuntimeException e) {
//					Log.e("CameraTest", "MainScreen.setupCamera unable setParameters "
//							+ e.getMessage());
//				}
//
////				previewWidth = cameraParameters.getPreviewSize().width;
////				previewHeight = cameraParameters.getPreviewSize().height;
//				previewWidth = cameraParameters.getPreviewSize().width;
//				previewHeight = cameraParameters.getPreviewSize().height;

//				Util.initialize(mainContext);
//				Util.initializeMeteringMatrix();
//				
//				prepareMeteringAreas();

		guiManager.onCameraCreate();
		PluginManager.getInstance().onCameraParametersSetup();
		guiManager.onPluginsInitialized();

		// ----- Start preview and setup frame buffer if needed

		// ToDo: call camera release sequence from onPause somewhere ???
		new CountDownTimer(10, 10) {
			@Override
			public void onFinish() {
//						try // exceptions sometimes happen here when resuming after
//							// processing
//						{
//							camera.startPreview();
//						} catch (RuntimeException e) {
//							Toast.makeText(MainScreen.thiz, "Unable to start camera", Toast.LENGTH_LONG).show();
//							return;
//						}
//
//						camera.setPreviewCallbackWithBuffer(MainScreen.thiz);
//						camera.addCallbackBuffer(pviewBuffer);

				PluginManager.getInstance().onCameraSetup();
				guiManager.onCameraSetup();
				MainScreen.mApplicationStarted = true;
				CameraController.cameraConfigured = true;
			}

			@Override
			public void onTick(long millisUntilFinished) {
			}
		}.start();		
	}
	
	
	// HALv3 ------------------------------------------------ camera-related listeners

		// Note: never received onCameraAvailable notifications, only onCameraUnavailable
		private class cameraAvailableListener extends CameraManager.AvailabilityListener
		{
			@Override
			public void onCameraAvailable(java.lang.String cameraId)
			{
				// should we call this?
				super.onCameraAvailable(cameraId);
				
				Log.d("MainScreen", "CameraManager.AvailabilityListener.onCameraAvailable");
			}
			
			@Override
			public void onCameraUnavailable(java.lang.String cameraId)
			{
				// should we call this?
				super.onCameraUnavailable(cameraId);
				
				Log.d("MainScreen", "CameraManager.AvailabilityListener.onCameraUnavailable");
			}
		}

		private class openListener extends CameraDevice.StateListener
		{
			@Override
			public void onDisconnected(CameraDevice arg0) {
				Log.d("MainScreen", "CameraDevice.StateListener.onDisconnected");
			}

			@Override
			public void onError(CameraDevice arg0, int arg1) {
				Log.d("MainScreen", "CameraDevice.StateListener.onError: "+arg1);
			}

			@Override
			public void onOpened(CameraDevice arg0)
			{
				Log.d("MainScreen", "CameraDevice.StateListener.onOpened");

				CameraController.camDevice = arg0;
				
				H.sendEmptyMessage(PluginManager.MSG_CAMERA_OPENED);

				//dumpCameraCharacteristics();
			}
		}

		// Note: there other onCaptureXxxx methods in this listener which we do not implement
		private class captureListener extends CameraDevice.CaptureListener
		{
			@Override
			public void onCaptureCompleted(CameraDevice camera, CaptureRequest request, CaptureResult result)
			{
				Log.d("MainScreen", "CameraDevice.CaptureListener.onCaptureCompleted");
				
				// Note: result arriving here is just image metadata, not the image itself
				// good place to extract sensor gain and other parameters

				// Note: not sure which units are used for exposure time (ms?)
//				currentExposure = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
//				currentSensitivity = result.get(CaptureResult.SENSOR_SENSITIVITY);
				
				//dumpCaptureResult(result);
				try {
					CameraController.camDevice.setRepeatingRequest(cameraController.previewRequestBuilder.build(), null, null);
				} catch (CameraAccessException e) {
					e.printStackTrace();
				}
			}
		}
		
		private class imageAvailableListener implements ImageReader.OnImageAvailableListener
		{
			@Override
			public void onImageAvailable(ImageReader ir)
			{
				Log.e("MainScreen", "ImageReader.OnImageAvailableListener.onImageAvailable");
				
				// Contrary to what is written in Aptina presentation acquireLatestImage is not working as described
				// Google: Also, not working as described in android docs (should work the same as acquireNextImage in our case, but it is not)
				//Image im = ir.acquireLatestImage();
				Image im = ir.acquireNextImage();
				
				PluginManager.getInstance().onImageAvailable(im);
				CameraController.mCaptureState = CameraController.CAPTURE_STATE_IDLE;
				
//				if (nFrame<NUM_FRAMES)
//					ExtractCentralCrop(im, nFrame);
//				else
//					ExtractDigitalZoomImage(im, nFrame);
//				
//				++nFrame;
//				
//				// dump image data as-is to file
//				//dumpImageData(im);
//				
//				// Image should be closed after we are done with it
				im.close();
//				
//				if (nFrame==NUM_FRAMES+1)
//					H.sendEmptyMessage(MSG_PROCESS);
//				//else
//				//	H.sendEmptyMessage(MSG_CAPTURE_NEXT);
			}
		}
	// ^^ HALv3 code -------------------------------------------------------------- camera-related listeners
		
		
		
		
	
	private void prepareMeteringAreas()
	{
		Rect centerRect = Util.convertToDriverCoordinates(new Rect(previewWidth/4, previewHeight/4, previewWidth - previewWidth/4, previewHeight - previewHeight/4));
		Rect topLeftRect = Util.convertToDriverCoordinates(new Rect(0, 0, previewWidth/2, previewHeight/2));
		Rect topRightRect = Util.convertToDriverCoordinates(new Rect(previewWidth/2, 0, previewWidth, previewHeight/2));
		Rect bottomRightRect = Util.convertToDriverCoordinates(new Rect(previewWidth/2, previewHeight/2, previewWidth, previewHeight));
		Rect bottomLeftRect = Util.convertToDriverCoordinates(new Rect(0, previewHeight/2, previewWidth/2, previewHeight));
		Rect spotRect = Util.convertToDriverCoordinates(new Rect(previewWidth/2 - 10, previewHeight/2 - 10, previewWidth/2 + 10, previewHeight/2 + 10));
		
		mMeteringAreaMatrix5.clear();
		mMeteringAreaMatrix5.add(new Area(centerRect, 600));
		mMeteringAreaMatrix5.add(new Area(topLeftRect, 200));
		mMeteringAreaMatrix5.add(new Area(topRightRect, 200));
		mMeteringAreaMatrix5.add(new Area(bottomRightRect, 200));
		mMeteringAreaMatrix5.add(new Area(bottomLeftRect, 200));
		
		mMeteringAreaMatrix4.clear();
		mMeteringAreaMatrix4.add(new Area(topLeftRect, 250));
		mMeteringAreaMatrix4.add(new Area(topRightRect, 250));
		mMeteringAreaMatrix4.add(new Area(bottomRightRect, 250));
		mMeteringAreaMatrix4.add(new Area(bottomLeftRect, 250));
		
		mMeteringAreaMatrix1.clear();
		mMeteringAreaMatrix1.add(new Area(centerRect, 1000));
		
		mMeteringAreaCenter.clear();
		mMeteringAreaCenter.add(new Area(centerRect, 1000));
		
		mMeteringAreaSpot.clear();
		mMeteringAreaSpot.add(new Area(spotRect, 1000));
	}

	public static void PopulateCameraDimensions() {
		CameraController.ResolutionsMPixList = new ArrayList<Long>();
		CameraController.ResolutionsIdxesList = new ArrayList<String>();
		CameraController.ResolutionsNamesList = new ArrayList<String>();

		////For debug file
//		File saveDir = PluginManager.getInstance().GetSaveDir();
//		File file = new File(
//        		saveDir, 
//        		"!!!ABC_DEBUG_COMMON.txt");
//		if (file.exists())
//		    file.delete();
//		try {
//			String data = "";
//			data = cp.flatten();
//			FileOutputStream out;
//			out = new FileOutputStream(file);
//			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
//			outputStreamWriter.write(data);
//			outputStreamWriter.write(data);
//			outputStreamWriter.flush();
//			outputStreamWriter.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		
		//List<Camera.Size> cs;
		//int MinMPIX = MIN_MPIX_SUPPORTED;
		//cs = cp.getSupportedPictureSizes();
		
		int MinMPIX = CameraController.MIN_MPIX_SUPPORTED;
		CameraCharacteristics params = MainScreen.thiz.getCameraParameters2();
    	Size[] cs = params.get(CameraCharacteristics.SCALER_AVAILABLE_PROCESSED_SIZES);

		CharSequence[] RatioStrings = { " ", "4:3", "3:2", "16:9", "1:1" };

		int iHighestIndex = 0;
		Size sHighest = cs[iHighestIndex];

//		/////////////////////////		
//		try {
//			File saveDir2 = PluginManager.getInstance().GetSaveDir();
//			File file2 = new File(
//	        		saveDir2, 
//	        		"!!!ABC_DEBUG.txt");
//			if (file2.exists())
//			    file2.delete();
//			FileOutputStream out2;
//			out2 = new FileOutputStream(file2);
//			OutputStreamWriter outputStreamWriter2 = new OutputStreamWriter(out2);
//
//			for (int ii = 0; ii < cs.size(); ++ii) {
//				Size s = cs.get(ii);
//					String data = "cs.size() = "+cs.size()+ " " +"size "+ ii + " = " + s.width + "x" +s.height;
//					outputStreamWriter2.write(data);
//					outputStreamWriter2.flush();
//			}
//			outputStreamWriter2.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		/////////////////////////
		
		int ii = 0;
		for(Size s : cs)
		{
//		for (int ii = 0; ii < cs.size(); ++ii) {
//			Size s = cs.get(ii);

			if ((long) s.getWidth() * s.getHeight() > (long) sHighest.getWidth()
					* sHighest.getHeight()) {
				sHighest = s;
				iHighestIndex = ii;
			}

			if ((long) s.getWidth() * s.getHeight() < MinMPIX)
				continue;

			Long lmpix = (long) s.getWidth() * s.getHeight();
			float mpix = (float) lmpix / 1000000.f;
			float ratio = (float) s.getWidth() / s.getHeight();

			// find good location in a list
			int loc;
			for (loc = 0; loc < CameraController.ResolutionsMPixList.size(); ++loc)
				if (CameraController.ResolutionsMPixList.get(loc) < lmpix)
					break;

			int ri = 0;
			if (Math.abs(ratio - 4 / 3.f) < 0.1f)
				ri = 1;
			if (Math.abs(ratio - 3 / 2.f) < 0.12f)
				ri = 2;
			if (Math.abs(ratio - 16 / 9.f) < 0.15f)
				ri = 3;
			if (Math.abs(ratio - 1) == 0)
				ri = 4;

			CameraController.ResolutionsNamesList.add(loc,
					String.format("%3.1f Mpix  " + RatioStrings[ri], mpix));
			CameraController.ResolutionsIdxesList.add(loc, String.format("%d", ii));
			CameraController.ResolutionsMPixList.add(loc, lmpix);
			
			ii++;
		}

		if (CameraController.ResolutionsNamesList.size() == 0) {
			Size s = cs[iHighestIndex];

			Long lmpix = (long) s.getWidth() * s.getHeight();
			float mpix = (float) lmpix / 1000000.f;
			float ratio = (float) s.getWidth() / s.getHeight();

			int ri = 0;
			if (Math.abs(ratio - 4 / 3.f) < 0.1f)
				ri = 1;
			if (Math.abs(ratio - 3 / 2.f) < 0.12f)
				ri = 2;
			if (Math.abs(ratio - 16 / 9.f) < 0.15f)
				ri = 3;
			if (Math.abs(ratio - 1) == 0)
				ri = 4;

			CameraController.ResolutionsNamesList.add(0,
					String.format("%3.1f Mpix  " + RatioStrings[ri], mpix));
			CameraController.ResolutionsIdxesList.add(0, String.format("%d", 0));
			CameraController.ResolutionsMPixList.add(0, lmpix);
		}

		return;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// ----- Find 'normal' orientation of the device

		Display display = ((WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getRotation();
		if ((rotation == Surface.ROTATION_90)
				|| (rotation == Surface.ROTATION_270))
			landscapeIsNormal = true; // false; - if landscape view orientation
										// set for MainScreen
		else
			landscapeIsNormal = false;

		surfaceCreated = true;
		surfaceJustCreated = true;
		
		mCameraSurface = surfaceHolder.getSurface();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		surfaceCreated = false;
		surfaceJustCreated = false;
	}

	@TargetApi(14)
	public boolean isFaceDetectionAvailable(Camera.Parameters params) {
		if (params.getMaxNumDetectedFaces() > 0)
			return true;
		else
			return false;
	}

	public Size getPreviewSize() {
		LayoutParams lp = preview.getLayoutParams();
		if (lp == null)
			return null;

		return new Size(lp.width, lp.height);		
	}

	public int getPreviewWidth() {
		LayoutParams lp = preview.getLayoutParams();
		if (lp == null)
			return 0;

		return lp.width;

	}

	public int getPreviewHeight() {
		LayoutParams lp = preview.getLayoutParams();
		if (lp == null)
			return 0;

		return lp.height;
	}

	@Override
	public void onError(int error, Camera camera) {
	}

	/*
	 * CAMERA parameters access functions
	 * 
	 * Camera.Parameters get/set Camera scene modes getSupported/set Camera
	 * white balance getSupported/set Camera focus modes getSupported/set Camera
	 * flash modes getSupported/set
	 * 
	 * For API14 Camera focus areas get/set Camera metering areas get/set
	 */
	public boolean isFrontCamera() {
		return CameraController.CameraMirrored;
	}

	public Camera getCamera() {
		return CameraController.camera;
	}
	
	public CameraDevice getCamera2() {
		return CameraController.camDevice;
	}
	
	public ImageReader getImageReaderYUV() {
		return mImageReaderYUV;
	}
	
	public ImageReader getImageReaderJPEG() {
		return mImageReaderJPEG;
	}

	public void setCamera(Camera cam) {
		CameraController.camera = cam;
	}

	public Camera.Parameters getCameraParameters() {
		if (CameraController.camera != null && CameraController.cameraParameters != null)
			return CameraController.cameraParameters;

		return null;
	}
	
	public CameraCharacteristics getCameraParameters2() {
		if (CameraController.camCharacter != null)
			return CameraController.camCharacter;

		return null;
	}

	public boolean setCameraParameters(Camera.Parameters params) {
		if (params != null && CameraController.camera != null)
		{			
			try
			{
				CameraController.camera.setParameters(params);
				CameraController.cameraParameters = params;
				//cameraParameters = camera.getParameters();
			}
			catch (Exception e) {
				e.printStackTrace();
				Log.e("MainScreen", "setCameraParameters exception: " + e.getMessage());
				return false;
			}
			
			return true;
		}
		
		return false;		
	}
	
	@TargetApi(15)
	public void setVideoStabilization(boolean stabilization)
	{
		if(CameraController.cameraParameters != null && CameraController.cameraParameters.isVideoStabilizationSupported())
		{
			CameraController.cameraParameters.setVideoStabilization(stabilization);
			this.setCameraParameters(CameraController.cameraParameters);
		}
	}
	
	@TargetApi(15)
	public boolean isVideoStabilizationSupported()
	{
		if(CameraController.cameraParameters != null)
			return CameraController.cameraParameters.isVideoStabilizationSupported();
		
		return false;
	}
	
	public boolean isExposureLockSupported() {
//		if (camera != null && cameraParameters != null) {
//			if (cameraParameters.isAutoExposureLockSupported())
//				return true;
//			else
//				return false;
//		} else
//			return false;
		return true;
	}
	
	public boolean isWhiteBalanceLockSupported() {
//		if (camera != null && cameraParameters != null) {
//			if (cameraParameters.isAutoWhiteBalanceLockSupported())
//				return true;
//			else
//				return false;
//		} else
//			return false;
		return true;
	}

	public boolean isExposureCompensationSupported() {
//		if (camera != null && cameraParameters != null) {
//			if (cameraParameters.getMinExposureCompensation() == 0
//					&& cameraParameters.getMaxExposureCompensation() == 0)
//				return false;
//			else
//				return true;
//		} else
//			return false;
		
		if(CameraController.camCharacter != null)
		{
			int expRange[] = CameraController.camCharacter.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
			return expRange[1] == expRange[0] ? false : true;
		}
		
		return false;
	}

	public int getMinExposureCompensation() {
//		if (camera != null && cameraParameters != null)
//			return cameraParameters.getMinExposureCompensation();
//		else
//			return 0;
		if(CameraController.camCharacter != null)
		{
			int expRange[] = CameraController.camCharacter.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
			return expRange[0];
		}
		
		return 0;
	}

	public int getMaxExposureCompensation() {
//		if (camera != null && cameraParameters != null)
//			return cameraParameters.getMaxExposureCompensation();
//		else
//			return 0;
		
		if(CameraController.camCharacter != null)
		{
			int expRange[] = CameraController.camCharacter.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE);
			return expRange[1];
		}
		
		return 0;
	}

	public float getExposureCompensationStep() {
//		if (camera != null && cameraParameters != null)
//			return cameraParameters.getExposureCompensationStep();
//		else
//			return 0;
		
		if(CameraController.camCharacter != null)
		{
			float step = CameraController.camCharacter.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP).toFloat();
			return step;
		}
		
		return 0;
	}

	public float getExposureCompensation() {
//		if (camera != null && cameraParameters != null)
//			return cameraParameters.getExposureCompensation()
//					* cameraParameters.getExposureCompensationStep();
//		else
//			return 0;
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
		float currEv = prefs.getFloat(MainScreen.sEvPref, 0);
		
		return currEv;
	}

	public void resetExposureCompensation() {
//		if (camera != null) {
//			if (!isExposureCompensationSupported())
//				return;
//			Camera.Parameters params = cameraParameters;
//			params.setExposureCompensation(0);
//			setCameraParameters(params);
//		}
		if(cameraController.previewRequestBuilder != null && CameraController.camDevice != null)
		{		
			cameraController.previewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
			try 
			{
				CameraController.camDevice.setRepeatingRequest(cameraController.previewRequestBuilder.build(), null, null);
			}
			catch (CameraAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

	public boolean isSceneModeSupported() {
//		List<String> supported_scene = getSupportedSceneModes();
//		if (supported_scene != null && supported_scene.size() > 0)
//			return true;
//		else
//			return false;
		
		if(CameraController.camCharacter != null)
		{
			byte scenes[]  = CameraController.camCharacter.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
			if(scenes.length > 0 && scenes[0] != CameraCharacteristics.CONTROL_SCENE_MODE_UNSUPPORTED)
				return true;				
		}
		
		return false;
	}

	public byte[] getSupportedSceneModes() {
//		if (camera != null)
//			return cameraParameters.getSupportedSceneModes();
//
//		return null;
		
		if(CameraController.camCharacter != null)
		{
			byte scenes[]  = CameraController.camCharacter.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES);
			if(scenes.length > 0 && scenes[0] != CameraCharacteristics.CONTROL_SCENE_MODE_UNSUPPORTED)
				return scenes;				
		}
		
		return null;
	}

	public boolean isWhiteBalanceSupported() {
//		List<String> supported_wb = getSupportedWhiteBalance();
//		if (supported_wb != null && supported_wb.size() > 0)
//			return true;
//		else
//			return false;
		
		if(CameraController.camCharacter != null)
		{
			byte wbs[]  = CameraController.camCharacter.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
			if(wbs.length > 0)
				return true;				
		}
		
		return false;
	}

	public byte[] getSupportedWhiteBalance() {
//		if (camera != null)
//			return cameraParameters.getSupportedWhiteBalance();
		
		if(CameraController.camCharacter != null)
		{
			byte wbs[]  = CameraController.camCharacter.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES);
			if(wbs.length > 0)
				return wbs;				
		}

		return null;
	}

	public boolean isFocusModeSupported() {
//		List<String> supported_focus = getSupportedFocusModes();
//		if (supported_focus != null && supported_focus.size() > 0)
//			return true;
//		else
//			return false;
		
		if(CameraController.camCharacter != null)
		{
			byte afs[]  = CameraController.camCharacter.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
			if(afs.length > 0)
				return true;				
		}
		
		return false;
	}

	public byte[] getSupportedFocusModes() {
//		if (camera != null)
//			return cameraParameters.getSupportedFocusModes();
//
//		return null;
		
		if(CameraController.camCharacter != null)
		{
			byte afs[]  = CameraController.camCharacter.get(CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES);
			if(afs.length > 0)
				return afs;				
		}

		return null;
	}

	public boolean isFlashModeSupported() {
//		List<String> supported_flash = getSupportedFlashModes();
//		if (supported_flash != null && supported_flash.size() > 0)
//			return true;
//		else
//			return false;
		
		if(CameraController.camCharacter != null)
		{
			return CameraController.camCharacter.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == 1? true : false;						
		}
		
		return false;
	}

	public byte[] getSupportedFlashModes() {		
//		if (camera != null)
//			return cameraParameters.getSupportedFlashModes();
//
//		return null;
		
		if(isFlashModeSupported())
		{
			byte flash[] = new byte[3];
			flash[0] = CameraCharacteristics.FLASH_MODE_OFF;
			flash[1] = CameraCharacteristics.FLASH_MODE_SINGLE;
			flash[2] = CameraCharacteristics.FLASH_MODE_TORCH;
			return flash;
		}

		return null;		
	}

	public boolean isISOSupported() {
//		List<String> supported_iso = getSupportedISO();
//		String isoSystem = MainScreen.thiz.getCameraParameters().get("iso");
//		String isoSystem2 = MainScreen.thiz.getCameraParameters().get("iso-speed");
//		if ((supported_iso != null && supported_iso.size() > 0) || isoSystem != null || isoSystem2 != null)
//			return true;
//		else
//			return false;
		return false;
	}

	public byte[] getSupportedISO()
	{
//		if (camera != null)
//		{
//			Camera.Parameters camParams = MainScreen.cameraParameters;
//			String supportedIsoValues = camParams.get("iso-values");
//			String supportedIsoValues2 = camParams.get("iso-speed-values");
//			String supportedIsoValues3 = camParams.get("iso-mode-values");
//			//String iso = camParams.get("iso");
//			
//			String delims = "[,]+";
//			String[] ISOs = null;
//			
//			if (supportedIsoValues != "" && supportedIsoValues != null)
//				ISOs = supportedIsoValues.split(delims);
//			else if(supportedIsoValues2 != "" && supportedIsoValues2 != null)
//				ISOs = supportedIsoValues2.split(delims);
//			else if(supportedIsoValues3 != "" && supportedIsoValues3 != null)
//				ISOs = supportedIsoValues3.split(delims);
//			
//			if(ISOs != null)
//			{
//				List<String> isoList = new ArrayList<String>();				
//				for (int i = 0; i < ISOs.length; i++)
//					isoList.add(ISOs[i]);
//
//				return isoList;
//			}
//		}

		return null;
	}	
	
	
	public int getMaxNumMeteringAreas()
	{
//		if(camera != null)
//		{
//			Camera.Parameters camParams = MainScreen.cameraParameters;
//			return camParams.getMaxNumMeteringAreas();
//		}
//		
//		return 0;
		if(CameraController.camCharacter != null)
		{
			return CameraController.camCharacter.get(CameraCharacteristics.CONTROL_MAX_REGIONS);						
		}
		
		return 0;
	}
	
	
	public static boolean isModeAvailable(byte[] modeList, int mode)
	{
		boolean isAvailable = false;
		for(int currMode : modeList)
		{
			if(currMode == mode)
			{
				isAvailable = true;
				break;
			}
		}
		return isAvailable;
	}
	
	

	public String getSceneMode() {
		if (CameraController.camera != null) {
			Camera.Parameters params = CameraController.cameraParameters;
			if (params != null)
				return params.getSceneMode();
		}

		return null;
	}

	public String getWBMode() {
		if (CameraController.camera != null) {
			Camera.Parameters params = CameraController.cameraParameters;
			if (params != null)
				return params.getWhiteBalance();
		}

		return null;
	}

	public String getFocusMode() {
		
		try {
			if (CameraController.camera != null) {
				Camera.Parameters params = CameraController.cameraParameters;
				if (params != null)
					return params.getFocusMode();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.e("MainScreen", "getFocusMode exception: " + e.getMessage());
		}

		return null;
	}

	public String getFlashMode() {
		if (CameraController.camera != null) {
			Camera.Parameters params = CameraController.cameraParameters;
			if (params != null)
				return params.getFlashMode();
		}

		return null;
	}

	public String getISOMode() {
		if (CameraController.camera != null) {
			Camera.Parameters params = CameraController.cameraParameters;			
			if (params != null)
			{
				String iso = null;
				iso = params.get("iso");
				if(iso == null)
					iso = params.get("iso-speed");
				
				return iso;
			}
		}

		return null;
	}

	public void setCameraSceneMode(int mode) {
//		if (camera != null) {
//			Camera.Parameters params = cameraParameters;
//			if (params != null) {
//				params.setSceneMode(mode);
//				setCameraParameters(params);
//			}
//		}
		
		if(cameraController.previewRequestBuilder != null && CameraController.camDevice != null)
		{		
			cameraController.previewRequestBuilder.set(CaptureRequest.CONTROL_SCENE_MODE, mode);
			try 
			{
				CameraController.camDevice.setRepeatingRequest(cameraController.previewRequestBuilder.build(), null, null);
			}
			catch (CameraAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void setCameraWhiteBalance(int mode) {
//		if (camera != null) {
//			Camera.Parameters params = cameraParameters;
//			if (params != null) {
//				params.setWhiteBalance(mode);
//				setCameraParameters(params);
//			}
//		}
		
		if(cameraController.previewRequestBuilder != null && CameraController.camDevice != null)
		{		
			cameraController.previewRequestBuilder.set(CaptureRequest.CONTROL_AWB_MODE, mode);
			try 
			{
				CameraController.camDevice.setRepeatingRequest(cameraController.previewRequestBuilder.build(), null, null);
			}
			catch (CameraAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void setCameraFocusMode(int mode) {
//		if (camera != null) {
//			Camera.Parameters params = cameraParameters;
//			if (params != null) {
//				params.setFocusMode(mode);
//				setCameraParameters(params);
//				mAFLocked = false;
//			}
//		}
		if(cameraController.previewRequestBuilder != null && CameraController.camDevice != null)
		{		
			cameraController.previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, mode);
			try 
			{
				CameraController.camDevice.setRepeatingRequest(cameraController.previewRequestBuilder.build(), null, null);
			}
			catch (CameraAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void setCameraFlashMode(int mode) {
//		if (camera != null) {
//			Camera.Parameters params = cameraParameters;
//			if (params != null && mode != "") {
//				params.setFlashMode(mode);
//				setCameraParameters(params);
//			}
//		}
		if(cameraController.previewRequestBuilder != null && CameraController.camDevice != null)
		{		
			cameraController.previewRequestBuilder.set(CaptureRequest.FLASH_MODE, mode);
			try 
			{
				CameraController.camDevice.setRepeatingRequest(cameraController.previewRequestBuilder.build(), null, null);
			}
			catch (CameraAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void setCameraISO(String mode) {
		if (CameraController.camera != null) {
			Camera.Parameters params = CameraController.cameraParameters;
			if (params != null) {
				if(params.get("iso") != null)
					params.set("iso", mode);
				else if(params.get("iso-speed") != null)
					params.set("iso-speed", mode);
				
				setCameraParameters(params);
			}
		}
	}
	
	public void setCameraMeteringMode(int mode)
	{
//		if (camera != null)
//		{
//			Camera.Parameters params = cameraParameters;
//			if(meteringModeAuto.contains(mode))
//				setCameraMeteringAreas(null);
//			else if(meteringModeMatrix.contains(mode))
//			{				
//				int maxAreasCount = params.getMaxNumMeteringAreas();
//				if(maxAreasCount > 4)
//					setCameraMeteringAreas(mMeteringAreaMatrix5);
//				else if(maxAreasCount > 3)
//					setCameraMeteringAreas(mMeteringAreaMatrix4);
//				else if(maxAreasCount > 0)
//					setCameraMeteringAreas(mMeteringAreaMatrix1);
//				else
//					setCameraMeteringAreas(null);					
//			}
//			else if(meteringModeCenter.contains(mode))
//				setCameraMeteringAreas(mMeteringAreaCenter);
//			else if(meteringModeSpot.contains(mode))
//				setCameraMeteringAreas(mMeteringAreaSpot);
//			
//			currentMeteringMode = mode;
//		}
		
		if(meteringModeAuto == mode)
			setCameraMeteringAreas(null);
		else if(meteringModeMatrix == mode)
		{				
			int maxAreasCount = MainScreen.thiz.getMaxNumMeteringAreas();
			if(maxAreasCount > 4)
				setCameraMeteringAreas(mMeteringAreaMatrix5);
			else if(maxAreasCount > 3)
				setCameraMeteringAreas(mMeteringAreaMatrix4);
			else if(maxAreasCount > 0)
				setCameraMeteringAreas(mMeteringAreaMatrix1);
			else
				setCameraMeteringAreas(null);					
		}
		else if(meteringModeCenter == mode)
			setCameraMeteringAreas(mMeteringAreaCenter);
		else if(meteringModeSpot == mode)
			setCameraMeteringAreas(mMeteringAreaSpot);
		
		currentMeteringMode = mode;
	}

	public void setCameraExposureCompensation(int iEV) {
//		if (camera != null) {
//			Camera.Parameters params = cameraParameters;
//			if (params != null) {
//				params.setExposureCompensation(iEV);
//				setCameraParameters(params);
//			}
//		}
		if(cameraController.previewRequestBuilder != null && CameraController.camDevice != null)
		{		
			cameraController.previewRequestBuilder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, iEV);
			try 
			{
				CameraController.camDevice.setRepeatingRequest(cameraController.previewRequestBuilder.build(), null, null);
			}
			catch (CameraAccessException e)
			{
				e.printStackTrace();
			}
		}
	}

	/*
	 * CAMERA PARAMETERS SECTION Supplementary methods for those plugins that
	 * need an icons of supported camera parameters (scene, iso, wb, flash,
	 * focus) Methods return id of drawable icon
	 */
	public int getSceneIcon(int sceneMode) {
		return guiManager.getSceneIcon(sceneMode);
	}

	public int getWBIcon(int wb) {
		return guiManager.getWBIcon(wb);
	}

	public int getFocusIcon(int focusMode) {
		return guiManager.getFocusIcon(focusMode);
	}

	public int getFlashIcon(int flashMode) {
		return guiManager.getFlashIcon(flashMode);
	}

	public int getISOIcon(String isoMode) {
		return guiManager.getISOIcon(isoMode);
	}

	public void updateCameraFeatures() {
		cameraController.mEVSupported = isExposureCompensationSupported();
		cameraController.mSceneModeSupported = isSceneModeSupported();
		cameraController.mWBSupported = isWhiteBalanceSupported();
		cameraController.mFocusModeSupported = isFocusModeSupported();
		cameraController.mFlashModeSupported = isFlashModeSupported();
		cameraController.mISOSupported = isISOSupported();
	}

	public void setCameraFocusAreas(List<Area> focusAreas) {
		if (CameraController.camera != null) {
			try {
				Camera.Parameters params = CameraController.cameraParameters;
				if (params != null) {
					params.setFocusAreas(focusAreas);
					setCameraParameters(params);
				}
			} catch (RuntimeException e) {
				Log.e("SetFocusArea", e.getMessage());
			}
		}
	}

	public void setCameraMeteringAreas(List<Area> meteringAreas) {
		if (CameraController.camera != null) {
			try {
				Camera.Parameters params = CameraController.cameraParameters;
				if (params != null) {
//					Rect rect = meteringAreas.get(0).rect;
//					Log.e("MainScreen", "Metering area: " + rect.left + ", " + rect.top + " - " + rect.right + ", " + rect.bottom);
					if(meteringAreas != null)
					{
						params.setMeteringAreas(null);
						setCameraParameters(params);
					}
					params.setMeteringAreas(meteringAreas);
					setCameraParameters(params);
				}
			} catch (RuntimeException e) {
				Log.e("SetMeteringArea", e.getMessage());
			}
		}
	}

	/*
	 * 
	 * CAMERA parameters access function ended
	 */

	// >>Description
	// section with user control procedures and main capture functions
	//
	// all events translated to PluginManager
	// Description<<

	public static boolean takePicture() {
		synchronized (MainScreen.thiz.syncObject) {
			if (CameraController.camera != null && getFocusState() != CameraController.FOCUS_STATE_FOCUSING) 
			{
				CameraController.mCaptureState = CameraController.CAPTURE_STATE_CAPTURING;
				// Log.e("", "mFocusState = " + getFocusState());
				CameraController.camera.setPreviewCallback(null);
				CameraController.camera.takePicture(null, null, null, MainScreen.thiz);
				return true;
			}

			// Log.e("", "takePicture(). FocusState = FOCUS_STATE_FOCUSING ");
			return false;
		}
	}
	
	public static void captureImage(int nFrames, int fm)
	{
		// stop preview
		try {
			CameraController.camDevice.stopRepeating();
		} catch (CameraAccessException e1) {
			Log.e("MainScreen", "Can't stop preview");
			e1.printStackTrace();
		}
		
		// create capture requests for the burst of still images
		CaptureRequest.Builder stillRequestBuilder = null;
		try
		{
			stillRequestBuilder = CameraController.camDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
			stillRequestBuilder.set(CaptureRequest.EDGE_MODE, CaptureRequest.EDGE_MODE_OFF);
			stillRequestBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_OFF);
			// no re-focus needed, already focused in preview, so keeping the same focusing mode for snapshot
			stillRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
			// Google: note: CONTROL_AF_MODE_OFF causes focus to move away from current position 
			//stillRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
			if(fm == ImageFormat.JPEG)
				stillRequestBuilder.addTarget(mImageReaderJPEG.getSurface());
			else
				stillRequestBuilder.addTarget(mImageReaderYUV.getSurface());

			// Google: throw: "Burst capture implemented yet", when to expect implementation?
			/*
			List<CaptureRequest> requests = new ArrayList<CaptureRequest>();
			for (int n=0; n<NUM_FRAMES; ++n)
				requests.add(stillRequestBuilder.build());
			
			camDevice.captureBurst(requests, new captureListener() , null);
			*/
			
			// requests for SZ input frames
			for (int n=0; n<nFrames; ++n)
				CameraController.camDevice.capture(stillRequestBuilder.build(), MainScreen.thiz.new captureListener() , null);
			
			// One more capture for comparison with a standard frame
//			stillRequestBuilder.set(CaptureRequest.EDGE_MODE, CaptureRequest.EDGE_MODE_HIGH_QUALITY);
//			stillRequestBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY);
//			// set crop area for the scaler to have interpolation applied by camera HW
//			stillRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoomCrop);
//			camDevice.capture(stillRequestBuilder.build(), new captureListener() , null);
		}
		catch (CameraAccessException e)
		{
			Log.e("MainScreen", "setting up still image capture request failed");
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	public static boolean autoFocus(Camera.AutoFocusCallback listener) {
		synchronized (MainScreen.thiz.syncObject) {
			if (CameraController.camera != null) {
				if (CameraController.mCaptureState != CameraController.CAPTURE_STATE_CAPTURING) {
					setFocusState(CameraController.FOCUS_STATE_FOCUSING);
					try {
						CameraController.camera.autoFocus(listener);
					}catch (Exception e) {
						e.printStackTrace();
						Log.e("MainScreen autoFocus(listener) failed", "autoFocus: " + e.getMessage());
						return false;
					}
					return true;
				}
			}
			return false;
		}
	}

	public static boolean autoFocus() {
		synchronized (MainScreen.thiz.syncObject) {
			if (CameraController.camera != null) {
				if (CameraController.mCaptureState != CameraController.CAPTURE_STATE_CAPTURING) {
					String fm = thiz.getFocusMode();
					// Log.e("", "mCaptureState = " + mCaptureState);
					setFocusState(CameraController.FOCUS_STATE_FOCUSING);
					try {
						CameraController.camera.autoFocus(MainScreen.thiz);
					}catch (Exception e) {
						e.printStackTrace();
						Log.e("MainScreen autoFocus() failed", "autoFocus: " + e.getMessage());
						return false;
					}					
					return true;
				}
			}
			return false;
		}
	}

	public static void cancelAutoFocus() {
		if (CameraController.camera != null) {
			setFocusState(CameraController.FOCUS_STATE_IDLE);
			try
			{
				CameraController.camera.cancelAutoFocus();
			}
			catch(RuntimeException exp)
			{
				Log.e("MainScreen", "cancelAutoFocus failed. Message: " + exp.getMessage());
			}
		}
	}
	
	public static void setAutoFocusLock(boolean locked)
	{
		mAFLocked = locked;
	}
	
	public static boolean getAutoFocusLock()
	{
		return mAFLocked;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!mApplicationStarted)
			return true;

		if (keyCode == KeyEvent.KEYCODE_MENU) {
			menuButtonPressed();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_CAMERA
				|| keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			MainScreen.guiManager.onHardwareShutterButtonPressed();
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_FOCUS) {
			MainScreen.guiManager.onHardwareFocusButtonPressed();
			return true;
		}
		
		//check if volume button has some functions except Zoom-ing
		if ( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP )
		{
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(MainScreen.mainContext);
			int buttonFunc = Integer.parseInt(prefs.getString("volumeButtonPrefCommon", "0"));
			if (buttonFunc == VOLUME_FUNC_SHUTTER)
			{
				MainScreen.guiManager.onHardwareFocusButtonPressed();
				MainScreen.guiManager.onHardwareShutterButtonPressed();
				return true;
			}
			else if (buttonFunc == VOLUME_FUNC_EXPO)
			{
				MainScreen.guiManager.onVolumeBtnExpo(keyCode);
				return true;
			}
			else if (buttonFunc == VOLUME_FUNC_NONE)
				return true;
		}
		
		
		if (PluginManager.getInstance().onKeyDown(true, keyCode, event))
			return true;
		if (guiManager.onKeyDown(true, keyCode, event))
			return true;

		// <!-- -+-
		if (keyCode == KeyEvent.KEYCODE_BACK)
    	{
    		if (AppRater.showRateDialogIfNeeded(this))
    		{
    			return true;
    		}
    		if (AppWidgetNotifier.showNotifierDialogIfNeeded(this))
    		{
    			return true;
    		}
    	}
		//-+- -->
		
		if (super.onKeyDown(keyCode, event))
			return true;
		return false;
	}

	@Override
	public void onClick(View v) {
		if (mApplicationStarted)
			MainScreen.guiManager.onClick(v);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (mApplicationStarted)
			return MainScreen.guiManager.onTouch(view, event);
		return true;
	}

	public boolean onTouchSuper(View view, MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public void onButtonClick(View v) {
		MainScreen.guiManager.onButtonClick(v);
	}
	
	@Override
	public void onShutter()
	{
		PluginManager.getInstance().onShutter();
	}

	@Override
	public void onPictureTaken(byte[] paramArrayOfByte, Camera paramCamera) 
	{
		
		CameraController.camera.setPreviewCallbackWithBuffer(MainScreen.thiz);
		CameraController.camera.addCallbackBuffer(pviewBuffer);
		
		PluginManager.getInstance().onPictureTaken(paramArrayOfByte,
				paramCamera);
		CameraController.mCaptureState = CameraController.CAPTURE_STATE_IDLE;
	}

	@Override
	public void onAutoFocus(boolean focused, Camera paramCamera) {
		Log.e("", "onAutoFocus call");
		PluginManager.getInstance().onAutoFocus(focused, paramCamera);
		if (focused)
			setFocusState(CameraController.FOCUS_STATE_FOCUSED);
		else
			setFocusState(CameraController.FOCUS_STATE_FAIL);
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera paramCamera) {
		PluginManager.getInstance().onPreviewFrame(data, paramCamera);
		CameraController.camera.addCallbackBuffer(pviewBuffer);
	}

	// >>Description
	// message processor
	//
	// processing main events and calling active plugin procedures
	//
	// possible some additional plugin dependent events.
	//
	// Description<<
	@Override
	public boolean handleMessage(Message msg) {

		switch(msg.what)
		{
			case MSG_RETURN_CAPTURED:
				this.setResult(RESULT_OK);
				this.finish();
				break;		
			case PluginManager.MSG_CAMERA_OPENED:
			case PluginManager.MSG_SURFACE_READY:
	
					// if both surface is created and camera device is opened
					// - ready to set up preview and other things
					if (surfaceCreated && (CameraController.camDevice != null))
					{
						configureCamera();
						PluginManager.getInstance().onGUICreate();
						MainScreen.guiManager.onGUICreate();
					}
					break;			
			default:
			PluginManager.getInstance().handleMessage(msg); break;
		}

		return true;
	}

	public void menuButtonPressed() {
		PluginManager.getInstance().menuButtonPressed();
	}

	public void disableCameraParameter(GUI.CameraParameter iParam,
			boolean bDisable, boolean bInitMenu) {
		guiManager.disableCameraParameter(iParam, bDisable, bInitMenu);
	}

	public void showOpenGLLayer() {
		if (glView != null && glView.getVisibility() == View.GONE) {
			glView.setVisibility(View.VISIBLE);
			glView.onResume();
		}
	}

	public void hideOpenGLLayer() {
		if (glView != null && glView.getVisibility() == View.VISIBLE) {
			glView.setVisibility(View.GONE);
			glView.onPause();
		}
	}

	public void PlayShutter(int sound) {
		if (!MainScreen.ShutterPreference) {
			MediaPlayer mediaPlayer = MediaPlayer
					.create(MainScreen.thiz, sound);
			mediaPlayer.start();
		}
	}

	public void PlayShutter() {
		if (!MainScreen.ShutterPreference) {
			if (shutterPlayer != null)
				shutterPlayer.play();
		}
	}

	// set TRUE to mute and FALSE to unmute
	public void MuteShutter(boolean mute) {
		if (MainScreen.ShutterPreference) {
			AudioManager mgr = (AudioManager) MainScreen.thiz
					.getSystemService(MainScreen.mainContext.AUDIO_SERVICE);
			mgr.setStreamMute(AudioManager.STREAM_SYSTEM, mute);
		}
	}

	public static int getImageWidth() {
		return imageWidth;
	}

	public static void setImageWidth(int setImageWidth) {
		imageWidth = setImageWidth;
	}

	public static int getImageHeight() {
		return imageHeight;
	}

	public static void setImageHeight(int setImageHeight) {
		imageHeight = setImageHeight;
	}

	public static int getSaveImageWidth() {
		return saveImageWidth;
	}

	public static void setSaveImageWidth(int setSaveImageWidth) {
		saveImageWidth = setSaveImageWidth;
	}

	public static int getSaveImageHeight() {
		return saveImageHeight;
	}

	public static void setSaveImageHeight(int setSaveImageHeight) {
		saveImageHeight = setSaveImageHeight;
	}

	public static boolean getCameraMirrored() {
		return CameraController.CameraMirrored;
	}

	public static void setCameraMirrored(boolean setCameraMirrored) {
		CameraController.CameraMirrored = setCameraMirrored;
	}

	public static boolean getWantLandscapePhoto() {
		return wantLandscapePhoto;
	}

	public static void setWantLandscapePhoto(boolean setWantLandscapePhoto) {
		wantLandscapePhoto = setWantLandscapePhoto;
	}

	public static void setFocusState(int state) {
		if (state != CameraController.FOCUS_STATE_IDLE
				&& state != CameraController.FOCUS_STATE_FOCUSED
				&& state != CameraController.FOCUS_STATE_FAIL)
			return;

		CameraController.mFocusState = state;

		Message msg = new Message();
		msg.what = PluginManager.MSG_BROADCAST;
		msg.arg1 = PluginManager.MSG_FOCUS_STATE_CHANGED;
		H.sendMessage(msg);
	}

	public static int getFocusState() {
		return CameraController.mFocusState;
	}
	
	public void setScreenBrightness(boolean setMax)
	{
		//ContentResolver cResolver = getContentResolver();
		Window window = getWindow();
		
		WindowManager.LayoutParams layoutpars = window.getAttributes();
		
        //Set the brightness of this window	
		if(setMax)
			layoutpars.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL;
		else
			layoutpars.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;

        //Apply attribute changes to this window
        window.setAttributes(layoutpars);
	}

	/*******************************************************/
	/************************ Billing ************************/
// <!-- -+-
	IabHelper mHelper;
	
	private boolean bOnSale = false;

	private boolean unlockAllPurchased = false;
	private boolean hdrPurchased = false;
	private boolean panoramaPurchased = false;
	private boolean objectRemovalBurstPurchased = false;
	private boolean groupShotPurchased = false;

	public boolean isUnlockedAll()
	{
		return unlockAllPurchased;
	}
	
	private void createBillingHandler() 
	{
		try {
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
			if ((isInstalled("com.almalence.hdr_plus")) || (isInstalled("com.almalence.pixfix")))
			{
				hdrPurchased = true;
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_hdr", true);
				prefsEditor.commit();
			}
			if (isInstalled("com.almalence.panorama.smoothpanorama"))
			{
				panoramaPurchased = true;
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_panorama", true);
				prefsEditor.commit();
			}
	
			String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnztuXLNughHjGW55Zlgicr9r5bFP/K5DBc3jYhnOOo1GKX8M2grd7+SWeUHWwQk9lgQKat/ITESoNPE7ma0ZS1Qb/VfoY87uj9PhsRdkq3fg+31Q/tv5jUibSFrJqTf3Vmk1l/5K0ljnzX4bXI0p1gUoGd/DbQ0RJ3p4Dihl1p9pJWgfI9zUzYfvk2H+OQYe5GAKBYQuLORrVBbrF/iunmPkOFN8OcNjrTpLwWWAcxV5k0l5zFPrPVtkMZzKavTVWZhmzKNhCvs1d8NRwMM7XMejzDpI9A7T9egl6FAN4rRNWqlcZuGIMVizJJhvOfpCLtY971kQkYNXyilD40fefwIDAQAB";
			// Create the helper, passing it our context and the public key to
			// verify signatures with
			Log.v("Main billing", "Creating IAB helper.");
			mHelper = new IabHelper(this, base64EncodedPublicKey);
	
			mHelper.enableDebugLogging(true);
	
			Log.v("Main billing", "Starting setup.");
			mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
				public void onIabSetupFinished(IabResult result) 
				{
					try {
						Log.v("Main billing", "Setup finished.");
		
						if (!result.isSuccess()) {
							Log.v("Main billing", "Problem setting up in-app billing: "
									+ result);
							return;
						}
		
						List<String> additionalSkuList = new ArrayList<String>();
						additionalSkuList.add("plugin_almalence_hdr");
						additionalSkuList.add("plugin_almalence_panorama");
						additionalSkuList.add("unlock_all_forever");
						additionalSkuList.add("plugin_almalence_moving_burst");
						additionalSkuList.add("plugin_almalence_groupshot");
						
						//for sale
						additionalSkuList.add("abc_sale_controller1");
						additionalSkuList.add("abc_sale_controller2");
		
						Log.v("Main billing", "Setup successful. Querying inventory.");
						mHelper.queryInventoryAsync(true, additionalSkuList,
								mGotInventoryListener);
					} catch (Exception e) {
						e.printStackTrace();
						Log.e("Main billing",
								"onIabSetupFinished exception: " + e.getMessage());
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Main billing",
					"createBillingHandler exception: " + e.getMessage());
		}
	}

	private void destroyBillingHandler() {
		try {
			if (mHelper != null)
				mHelper.dispose();
			mHelper = null;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Main billing",
					"destroyBillingHandler exception: " + e.getMessage());
		}
	}

	public String titleUnlockAll = "$6.95";
	public String titleUnlockHDR = "$2.99";
	public String titleUnlockPano = "$2.99";
	public String titleUnlockMoving = "$2.99";
	public String titleUnlockGroup = "$2.99";
	
	public String summaryUnlockAll = "";
	public String summaryUnlockHDR = "";
	public String summaryUnlockPano = "";
	public String summaryUnlockMoving = "";
	public String summaryUnlockGroup = "";
	
	IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
		public void onQueryInventoryFinished(IabResult result,
				Inventory inventory) {
			if (inventory == null)
			{
				Log.e("Main billing", "mGotInventoryListener inventory null ");
				return;
			}

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(MainScreen.mainContext);
			
			if (inventory.hasPurchase("plugin_almalence_hdr")) {
				hdrPurchased = true;
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_hdr", true);
				prefsEditor.commit();
			}
			if (inventory.hasPurchase("plugin_almalence_panorama")) {
				panoramaPurchased = true;
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_panorama", true);
				prefsEditor.commit();
			}
			if (inventory.hasPurchase("unlock_all_forever")) {
				unlockAllPurchased = true;
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("unlock_all_forever", true);
				prefsEditor.commit();
			}
			if (inventory.hasPurchase("plugin_almalence_moving_burst")) {
				objectRemovalBurstPurchased = true;
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_moving_burst", true);
				prefsEditor.commit();
			}
			if (inventory.hasPurchase("plugin_almalence_groupshot")) {
				groupShotPurchased = true;
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_groupshot", true);
				prefsEditor.commit();
			}
			
			try{
			//if (inventory.hasPurchase("abc_sale_controller1") && inventory.hasPurchase("abc_sale_controller2")) {
				
				String[] separated = inventory.getSkuDetails("abc_sale_controller1").getPrice().split(",");
				int price1 = Integer.valueOf(separated[0]);
				String[] separated2 = inventory.getSkuDetails("abc_sale_controller2").getPrice().split(",");
				int price2 = Integer.valueOf(separated2[0]);
				
				if(price1<price2)
					bOnSale = true;
				else
					bOnSale = false;
				
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("bOnSale", bOnSale);
				prefsEditor.commit();
				
				Log.e("Main billing SALE", "Sale status is " + bOnSale);
			}
			catch(Exception e)
			{
				Log.e("Main billing SALE", "No sale data available");
				bOnSale = false;
			}
			
			titleUnlockAll = inventory.getSkuDetails("unlock_all_forever").getPrice();
			titleUnlockHDR = inventory.getSkuDetails("plugin_almalence_hdr").getPrice();
			titleUnlockPano = inventory.getSkuDetails("plugin_almalence_panorama").getPrice();
			titleUnlockMoving = inventory.getSkuDetails("plugin_almalence_moving_burst").getPrice();
			titleUnlockGroup = inventory.getSkuDetails("plugin_almalence_groupshot").getPrice();
			
			summaryUnlockAll = inventory.getSkuDetails("unlock_all_forever").getDescription();
			summaryUnlockHDR = inventory.getSkuDetails("plugin_almalence_hdr").getDescription();
			summaryUnlockPano = inventory.getSkuDetails("plugin_almalence_panorama").getDescription();
			summaryUnlockMoving = inventory.getSkuDetails("plugin_almalence_moving_burst").getDescription();
			summaryUnlockGroup = inventory.getSkuDetails("plugin_almalence_groupshot").getDescription();
		}
	};

	private int HDR_REQUEST = 100;
	private int PANORAMA_REQUEST = 101;
	private int ALL_REQUEST = 102;
	private int OBJECTREM_BURST_REQUEST = 103;
	private int GROUPSHOT_REQUEST = 104;
	Preference hdrPref, panoramaPref, allPref, objectremovalPref,
			groupshotPref;

	public void onBillingPreferenceCreate(final PreferenceFragment prefActivity) {
		allPref = prefActivity.findPreference("purchaseAll");
		
		if (titleUnlockAll!=null && titleUnlockAll != "")
		{
			String title = getResources().getString(R.string.Pref_Upgrde_All_Preference_Title) + ": " + titleUnlockAll;
			allPref.setTitle(title);
		}
		if (summaryUnlockAll!=null && summaryUnlockAll != "")
		{
			String summary = summaryUnlockAll + " " + getResources().getString(R.string.Pref_Upgrde_All_Preference_Summary);
			allPref.setSummary(summary);
		}
		
		allPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// generate payload to identify user....????
				String payload = "";
				try {
					mHelper.launchPurchaseFlow(MainScreen.thiz,
							"unlock_all_forever", ALL_REQUEST,
							mPreferencePurchaseFinishedListener, payload);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Main billing", "Purchase result " + e.getMessage());
					Toast.makeText(MainScreen.thiz,
							"Error during purchase " + e.getMessage(),
							Toast.LENGTH_LONG).show();
				}

				prefActivity.getActivity().finish();
				Preferences.closePrefs();
				return true;
			}
		});
		if (unlockAllPurchased) {
			allPref.setEnabled(false);
			allPref.setSummary(R.string.already_unlocked);

			hdrPurchased = true;
			panoramaPurchased = true;
			objectRemovalBurstPurchased = true;
			groupShotPurchased = true;
		}

		hdrPref = prefActivity.findPreference("hdrPurchase");
		
		if (titleUnlockHDR!=null && titleUnlockHDR != "")
		{
			String title = getResources().getString(R.string.Pref_Upgrde_HDR_Preference_Title) + ": " + titleUnlockHDR;
			hdrPref.setTitle(title);
		}
		if (summaryUnlockHDR!=null && summaryUnlockHDR != "")
		{
			String summary = summaryUnlockHDR + " " + getResources().getString(R.string.Pref_Upgrde_HDR_Preference_Summary);
			hdrPref.setSummary(summary);
		}
		
		hdrPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				// generate payload to identify user....????
				String payload = "";
				try {
					mHelper.launchPurchaseFlow(MainScreen.thiz,
							"plugin_almalence_hdr", HDR_REQUEST,
							mPreferencePurchaseFinishedListener, payload);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Main billing", "Purchase result " + e.getMessage());
					Toast.makeText(MainScreen.thiz,
							"Error during purchase " + e.getMessage(),
							Toast.LENGTH_LONG).show();
				}

				prefActivity.getActivity().finish();
				Preferences.closePrefs();
				return true;
			}
		});

		if (hdrPurchased) {
			hdrPref.setEnabled(false);
			hdrPref.setSummary(R.string.already_unlocked);
		}

		panoramaPref = prefActivity.findPreference("panoramaPurchase");
		
		if (titleUnlockPano!=null && titleUnlockPano != "")
		{
			String title = getResources().getString(R.string.Pref_Upgrde_Panorama_Preference_Title) + ": " + titleUnlockPano;
			panoramaPref.setTitle(title);
		}
		if (summaryUnlockPano!=null && summaryUnlockPano != "")
		{
			String summary = summaryUnlockPano + " " + getResources().getString(R.string.Pref_Upgrde_Panorama_Preference_Summary) ;
			panoramaPref.setSummary(summary);
		}
		
		panoramaPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						// generate payload to identify user....????
						String payload = "";
						try {
							mHelper.launchPurchaseFlow(MainScreen.thiz,
									"plugin_almalence_panorama",
									PANORAMA_REQUEST,
									mPreferencePurchaseFinishedListener,
									payload);
						} catch (Exception e) {
							e.printStackTrace();
							Log.e("Main billing",
									"Purchase result " + e.getMessage());
							Toast.makeText(MainScreen.thiz,
									"Error during purchase " + e.getMessage(),
									Toast.LENGTH_LONG).show();
						}

						prefActivity.getActivity().finish();
						Preferences.closePrefs();
						return true;
					}
				});
		if (panoramaPurchased) {
			panoramaPref.setEnabled(false);
			panoramaPref.setSummary(R.string.already_unlocked);
		}

		objectremovalPref = prefActivity.findPreference("movingPurchase");
		
		if (titleUnlockMoving!=null && titleUnlockMoving != "")
		{
			String title = getResources().getString(R.string.Pref_Upgrde_Moving_Preference_Title) + ": " + titleUnlockMoving;
			objectremovalPref.setTitle(title);
		}
		if (summaryUnlockMoving!=null && summaryUnlockMoving != "")
		{
			String summary = summaryUnlockMoving + " " + getResources().getString(R.string.Pref_Upgrde_Moving_Preference_Summary);
			objectremovalPref.setSummary(summary);
		}
		
		objectremovalPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						// generate payload to identify user....????
						String payload = "";
						try {
							mHelper.launchPurchaseFlow(MainScreen.thiz,
									"plugin_almalence_moving_burst",
									OBJECTREM_BURST_REQUEST,
									mPreferencePurchaseFinishedListener,
									payload);
						} catch (Exception e) {
							e.printStackTrace();
							Log.e("Main billing",
									"Purchase result " + e.getMessage());
							Toast.makeText(MainScreen.thiz,
									"Error during purchase " + e.getMessage(),
									Toast.LENGTH_LONG).show();
						}

						prefActivity.getActivity().finish();
						Preferences.closePrefs();
						return true;
					}
				});
		if (objectRemovalBurstPurchased) {
			objectremovalPref.setEnabled(false);
			objectremovalPref.setSummary(R.string.already_unlocked);
		}

		groupshotPref = prefActivity.findPreference("groupPurchase");
		
		if (titleUnlockGroup!=null && titleUnlockGroup != "")
		{
			String title = getResources().getString(R.string.Pref_Upgrde_Groupshot_Preference_Title) + ": " + titleUnlockGroup;
			groupshotPref.setTitle(title);
		}
		if (summaryUnlockGroup!=null && summaryUnlockGroup != "")
		{
			String summary = summaryUnlockGroup + " " + getResources().getString(R.string.Pref_Upgrde_Groupshot_Preference_Summary);
			groupshotPref.setSummary(summary);
		}
		
		groupshotPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						String payload = "";
						try {
							mHelper.launchPurchaseFlow(MainScreen.thiz,
									"plugin_almalence_groupshot",
									GROUPSHOT_REQUEST,
									mPreferencePurchaseFinishedListener,
									payload);
						} catch (Exception e) {
							e.printStackTrace();
							Log.e("Main billing",
									"Purchase result " + e.getMessage());
							Toast.makeText(MainScreen.thiz,
									"Error during purchase " + e.getMessage(),
									Toast.LENGTH_LONG).show();
						}

						prefActivity.getActivity().finish();
						Preferences.closePrefs();
						return true;
					}
				});
		if (groupShotPurchased) {
			groupshotPref.setEnabled(false);
			groupshotPref.setSummary(R.string.already_unlocked);
		}
	}

	public boolean showUnlock = false;
	// Callback for when purchase from preferences is finished
	IabHelper.OnIabPurchaseFinishedListener mPreferencePurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.v("Main billing", "Purchase finished: " + result
					+ ", purchase: " + purchase);
			if (result.isFailure()) {
				Log.v("Main billing", "Error purchasing: " + result);
				new CountDownTimer(100, 100) {
					public void onTick(long millisUntilFinished) {
					}

					public void onFinish() {
						showUnlock = true;
						Intent intent = new Intent(MainScreen.thiz,
								Preferences.class);
						startActivity(intent);
					}
				}.start();
				return;
			}

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(MainScreen.mainContext);
			
			Log.v("Main billing", "Purchase successful.");

			if (purchase.getSku().equals("plugin_almalence_hdr")) {
				Log.v("Main billing", "Purchase HDR.");

				hdrPurchased = true;
				hdrPref.setEnabled(false);
				hdrPref.setSummary(R.string.already_unlocked);
				
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_hdr", true);
				prefsEditor.commit();
			}
			if (purchase.getSku().equals("plugin_almalence_panorama")) {
				Log.v("Main billing", "Purchase Panorama.");

				panoramaPurchased = true;
				panoramaPref.setEnabled(false);
				panoramaPref.setSummary(R.string.already_unlocked);
				
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_panorama", true);
				prefsEditor.commit();
			}
			if (purchase.getSku().equals("unlock_all_forever")) {
				Log.v("Main billing", "Purchase all.");

				unlockAllPurchased = true;
				allPref.setEnabled(false);
				allPref.setSummary(R.string.already_unlocked);

				groupshotPref.setEnabled(false);
				groupshotPref.setSummary(R.string.already_unlocked);

				objectremovalPref.setEnabled(false);
				objectremovalPref.setSummary(R.string.already_unlocked);

				panoramaPref.setEnabled(false);
				panoramaPref.setSummary(R.string.already_unlocked);

				hdrPref.setEnabled(false);
				hdrPref.setSummary(R.string.already_unlocked);
				
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("unlock_all_forever", true);
				prefsEditor.commit();
			}
			if (purchase.getSku().equals("plugin_almalence_moving_burst")) {
				Log.v("Main billing", "Purchase object removal.");

				objectRemovalBurstPurchased = true;
				objectremovalPref.setEnabled(false);
				objectremovalPref.setSummary(R.string.already_unlocked);
				
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_moving_burst", true);
				prefsEditor.commit();
			}
			if (purchase.getSku().equals("plugin_almalence_groupshot")) {
				Log.v("Main billing", "Purchase groupshot.");

				groupShotPurchased = true;
				groupshotPref.setEnabled(false);
				groupshotPref.setSummary(R.string.already_unlocked);
				
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_groupshot", true);
				prefsEditor.commit();
			}
		}
	};

	public void launchPurchase(String SKU, int requestID) {
		String payload = "";
		try {
			mHelper.launchPurchaseFlow(MainScreen.thiz, SKU, requestID,
					mPurchaseFinishedListener, payload);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Main billing", "Purchase result " + e.getMessage());
			Toast.makeText(this, "Error during purchase " + e.getMessage(),
					Toast.LENGTH_LONG).show();
		}
	}

	IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
		public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
			Log.v("Main billing", "Purchase finished: " + result
					+ ", purchase: " + purchase);
			if (result.isFailure()) {
				Log.v("Main billing", "Error purchasing: " + result);
				return;
			}

			Log.v("Main billing", "Purchase successful.");

			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
			
			if (purchase.getSku().equals("plugin_almalence_hdr")) {
				Log.v("Main billing", "Purchase HDR.");
				hdrPurchased = true;
				
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_hdr", true);
				prefsEditor.commit();
			}
			if (purchase.getSku().equals("plugin_almalence_panorama")) {
				Log.v("Main billing", "Purchase Panorama.");
				panoramaPurchased = true;
				
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_panorama", true);
				prefsEditor.commit();
			}
			if (purchase.getSku().equals("unlock_all_forever")) {
				Log.v("Main billing", "Purchase unlock_all_forever.");
				unlockAllPurchased = true;
				
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("unlock_all_forever", true);
				prefsEditor.commit();
			}
			if (purchase.getSku().equals("plugin_almalence_moving_burst")) {
				Log.v("Main billing", "Purchase plugin_almalence_moving_burst.");
				objectRemovalBurstPurchased = true;
				
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_moving_burst", true);
				prefsEditor.commit();
			}
			if (purchase.getSku().equals("plugin_almalence_groupshot")) {
				Log.v("Main billing", "Purchase plugin_almalence_groupshot.");
				groupShotPurchased = true;
				
				Editor prefsEditor = prefs.edit();
				prefsEditor.putBoolean("plugin_almalence_groupshot", true);
				prefsEditor.commit();
			}

			showUnlock = true;
			Intent intent = new Intent(MainScreen.thiz, Preferences.class);
			startActivity(intent);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.v("Main billing", "onActivityResult(" + requestCode + ","
				+ resultCode + "," + data);

		// Pass on the activity result to the helper for handling
		if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
			// not handled, so handle it ourselves (here's where you'd
			// perform any handling of activity results not related to in-app
			// billing...
			super.onActivityResult(requestCode, resultCode, data);
		} else {
			Log.v("Main billing", "onActivityResult handled by IABUtil.");
		}
	}

	// next methods used to store number of free launches.
	// using files to store this info

	// returns number of launches left
	public int getLeftLaunches(String modeID) {
		String dirPath = getFilesDir().getAbsolutePath() + File.separator
				+ modeID;
		File projDir = new File(dirPath);
		if (!projDir.exists()) {
			projDir.mkdirs();
			WriteLaunches(projDir, 30);
		}
		int left = ReadLaunches(projDir);
		return left;
	}

	// decrements number of launches left
	public void decrementLeftLaunches(String modeID) {
		String dirPath = getFilesDir().getAbsolutePath() + File.separator
				+ modeID;
		File projDir = new File(dirPath);
		if (!projDir.exists()) {
			projDir.mkdirs();
			WriteLaunches(projDir, 30);
		}

		int left = ReadLaunches(projDir);
		if (left > 0)
			WriteLaunches(projDir, left - 1);
	}

	// writes number of launches left into memory
	private void WriteLaunches(File projDir, int left) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(projDir + "/left");
			fos.write(left);
			fos.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// reads number of launches left from memory
	private int ReadLaunches(File projDir) {
		int left = 0;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(projDir + "/left");
			left = fis.read();
			fis.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return left;
	}

	public boolean checkLaunches(Mode mode) {
		// if mode free
		if (mode.SKU == null)
			return true;
		if (mode.SKU.isEmpty())
			return true;

		// if all unlocked
		if (unlockAllPurchased == true)
			return true;

		// if current mode unlocked
		if (mode.SKU.equals("plugin_almalence_hdr")) {
			if (hdrPurchased == true)
				return true;
		} else if (mode.SKU.equals("plugin_almalence_panorama_augmented")) {
			if (panoramaPurchased == true)
				return true;
		} else if (mode.SKU.equals("plugin_almalence_moving_burst")) {
			if (objectRemovalBurstPurchased == true)
				return true;
		} else if (mode.SKU.equals("plugin_almalence_groupshot")) {
			if (groupShotPurchased == true)
				return true;
		}

		// if (!mode.purchased)
		{
			int launchesLeft = MainScreen.thiz.getLeftLaunches(mode.modeID);
			if (0 == launchesLeft)// no more launches left
			{
				// show appstore for this mode
				launchPurchase(mode.SKU, 100);
				return false;
			} else if ((10 == launchesLeft) || (20 == launchesLeft)
					|| (5 >= launchesLeft)) {
				// show appstore button and say that it cost money
				int id = MainScreen.thiz.getResources().getIdentifier(
						mode.modeName, "string",
						MainScreen.thiz.getPackageName());
				String modename = MainScreen.thiz.getResources().getString(id);

				String left = String.format(getResources().getString(R.string.Pref_Billing_Left),
						modename, launchesLeft);
				Toast toast = Toast.makeText(this, left, Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			}
		}
		return true;
	}

	private boolean isInstalled(String packageName) {
		PackageManager pm = getPackageManager();
		boolean installed = false;
		try {
			pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
			installed = true;
		} catch (PackageManager.NameNotFoundException e) {
			installed = false;
		}
		return installed;
	}
	
// -+- -->
	
	/************************ Billing ************************/
	/*******************************************************/
	
// <!-- -+-
	
	//Application rater code
	public static void CallStoreFree(Activity act)
    {
    	try
    	{
        	Intent intent = new Intent(Intent.ACTION_VIEW);
       		intent.setData(Uri.parse("market://details?id=com.almalence.opencam"));
	        act.startActivity(intent);
    	}
    	catch(ActivityNotFoundException e)
    	{
    		return;
    	}
    }
// -+- -->
	
	//widget ad code
	public static void CallStoreWidgetInstall(Activity act)
    {
    	try
    	{
        	Intent intent = new Intent(Intent.ACTION_VIEW);
       		intent.setData(Uri.parse("market://details?id=com.almalence.opencamwidget"));
	        act.startActivity(intent);
    	}
    	catch(ActivityNotFoundException e)
    	{
    		return;
    	}
    }
	
	private void ResetOrSaveSettings()
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainScreen.mainContext);
		Editor prefsEditor = prefs.edit();
		boolean isSaving = prefs.getBoolean("SaveConfiguration_Mode", true);
		if (false == isSaving)
		{
			prefsEditor.putString("defaultModeName", "single");
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_ImageSize", true);
		if (false == isSaving)
		{			
			//general settings - image size
			prefsEditor.putString("imageSizePrefCommonBack", "-1");
			prefsEditor.putString("imageSizePrefCommonFront", "-1");
			//night hi sped image size
			prefsEditor.putString("imageSizePrefNightBack", "-1");
			prefsEditor.putString("pref_plugin_capture_panoramaaugmented_imageheight", "0");
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_SceneMode", true);
		if (false == isSaving)
		{			
			prefsEditor.putInt(sSceneModePref, sDefaultValue);
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_FocusMode", true);
		if (false == isSaving)
		{			
			prefsEditor.putInt("sRearFocusModePref", sDefaultFocusValue);
			prefsEditor.putInt(sFrontFocusModePref, sDefaultFocusValue);
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_WBMode", true);
		if (false == isSaving)
		{			
			prefsEditor.putInt(sWBModePref, sDefaultValue);
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_ISOMode", true);
		if (false == isSaving)
		{			
			prefsEditor.putInt(sISOPref, sDefaultValue);
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_FlashMode", true);
		if (false == isSaving)
		{			
			prefsEditor.putInt(sFlashModePref, sDefaultValue);
			prefsEditor.commit();
		}
		
		isSaving = prefs.getBoolean("SaveConfiguration_FrontRearCamera", true);
		if (false == isSaving)
		{			
			prefsEditor.putBoolean("useFrontCamera", false);
			prefsEditor.commit();
		}
	}
}
