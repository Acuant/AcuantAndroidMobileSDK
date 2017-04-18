/*
 *
 */
package com.cssn.samplesdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.acuant.mobilesdk.AcuantAndroidMobileSDKController;
import com.acuant.mobilesdk.AcuantErrorListener;
import com.acuant.mobilesdk.Card;
import com.acuant.mobilesdk.CardCroppingListener;
import com.acuant.mobilesdk.CardType;
import com.acuant.mobilesdk.ConnectWebserviceListener;
import com.acuant.mobilesdk.DriversLicenseCard;
import com.acuant.mobilesdk.ErrorType;
import com.acuant.mobilesdk.FacialData;
import com.acuant.mobilesdk.FacialRecognitionListener;
import com.acuant.mobilesdk.LicenseActivationDetails;
import com.acuant.mobilesdk.LicenseDetails;
import com.acuant.mobilesdk.MedicalCard;
import com.acuant.mobilesdk.PassportCard;
import com.acuant.mobilesdk.Permission;
import com.acuant.mobilesdk.ProcessImageRequestOptions;
import com.acuant.mobilesdk.Region;
import com.acuant.mobilesdk.WebServiceListener;
import com.acuant.mobilesdk.util.Utils;
import com.cssn.mobilesdk.utilities.AcuantUtil;
import com.cssn.samplesdk.model.MainActivityModel;
import com.cssn.samplesdk.model.MainActivityModel.State;
import com.cssn.samplesdk.util.ConfirmationListener;
import com.cssn.samplesdk.util.DataContext;
import com.cssn.samplesdk.util.TempImageStore;
import com.cssn.samplesdk.util.Util;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 */
public class MainActivity extends Activity implements WebServiceListener,ConnectWebserviceListener, CardCroppingListener, AcuantErrorListener, FacialRecognitionListener {

    private static final String TAG = MainActivity.class.getName();
    private  final String IS_SHOWING_DIALOG_KEY = "isShowingDialog";
    private  final String IS_PROCESSING_DIALOG_KEY = "isProcessing";
    private  final String IS_CROPPING_DIALOG_KEY = "isCropping";
    private  final String IS_VALIDATING_DIALOG_KEY = "isValidating";
    private  final String IS_ACTIVATING_DIALOG_KEY = "isActivating";
    private  final String IS_SHOWDUPLEXDIALOG_DIALOG_KEY = "isShowDuplexDialog";
    public  String sPdf417String = "";
    AcuantAndroidMobileSDKController acuantAndroidMobileSdkControllerInstance = null;
    public ImageView frontImageView;
    public ImageView backImageView;
    private TextView txtTapToCaptureFront;
    private TextView txtTapToCaptureBack;
    public Button processCardButton;
    private Button buttonMedical;
    private RelativeLayout layoutFrontImage;
    private RelativeLayout layoutBackImage;
    private LinearLayout layoutCards;
    private EditText editTextLicense;
    private TextView textViewCardInfo;
    public MainActivityModel mainActivityModel = null;
    private Button activateLicenseButton;
    private  ProgressDialog progressDialog;
    private  AlertDialog showDuplexAlertDialog;
    private  AlertDialog alertDialog;
    private  boolean isShowErrorAlertDialog;
    private  boolean isProcessing;
    private  boolean isValidating;
    private  boolean isActivating;
    private  boolean isCropping;
    private  boolean isBackSide;
    private  boolean isShowDuplexDialog;
    private  boolean isProcessingFacial;
    private  boolean isFacialFlow;
    private MainActivity mainActivity;
    private int cardRegion;
    private Bitmap originalImage;
    private Card processedCardInformation;
    private FacialData processedFacialData;

    private String assureIDUsername;
    private String assureIDPassword;
    private String assureIDSubscription;
    private String assureIDURL;
    private boolean isConnect = false;
    private boolean isFirstLoad = true;
    /**
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (Util.LOG_ENABLED) {
            Utils.appendLog(TAG, "protected void onCreate(Bundle savedInstanceState)");
        }

        // load the model
        if (savedInstanceState == null) {
            if (Util.LOG_ENABLED) {
                Utils.appendLog(TAG, "if (savedInstanceState == null)");
            }
            mainActivityModel = new MainActivityModel();
        } else {
            if (Util.LOG_ENABLED) {
                Utils.appendLog(TAG, "if (savedInstanceState != null)");
            }
            mainActivityModel = DataContext.getInstance().getMainActivityModel();
            // if coming from background and kill the app, restart the model
            if (mainActivityModel == null) {
                mainActivityModel = new MainActivityModel();
            }
        }
        DataContext.getInstance().setContext(getApplicationContext());
        setContentView(R.layout.activity_main);

        layoutCards = (LinearLayout) findViewById(R.id.cardImagesLayout);
        layoutBackImage = (RelativeLayout) findViewById(R.id.relativeLayoutBackImage);
        layoutFrontImage = (RelativeLayout) findViewById(R.id.relativeLayoutFrontImage);

        frontImageView = (ImageView) findViewById(R.id.frontImageView);
        backImageView = (ImageView) findViewById(R.id.backImageView);

        editTextLicense = (EditText) findViewById(R.id.editTextLicenceKey);
        editTextLicense.setText(DataContext.getInstance().getLicenseKey());

        textViewCardInfo = (TextView) findViewById(R.id.textViewCardInfo);

        buttonMedical = (Button)findViewById(R.id.buttonMedical);

        txtTapToCaptureFront = (TextView) findViewById(R.id.txtTapToCaptureFront);
        txtTapToCaptureBack = (TextView) findViewById(R.id.txtTapToCaptureBack);

        activateLicenseButton = (Button) findViewById(R.id.activateLicenseButton);

        processCardButton = (Button) findViewById(R.id.processCardButton);
        processCardButton.setVisibility(View.INVISIBLE);

        editTextLicense.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                validateLicenseKey(editTextLicense.getText().toString());
                DataContext.getInstance().setLicenseKey(editTextLicense.getText().toString());
                return true;
            }
        });

        // it is necessary to use a post UI call, because of the previous set text on 'editTextLicense'
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                editTextLicense.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        mainActivityModel.setState(State.NO_VALIDATED);
                        updateActivateLicenseButtonFromModel();
                    }

                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }
                });
            }
        });

        editTextLicense.setOnFocusChangeListener(new OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideVirtualKeyboard();
                }
            }
        });

        initializeSDK();
        // update the UI from the model
        updateUI();
        if (Utils.LOG_ENABLED) {
            Utils.appendLog(TAG, "getScreenOrientation()=" + Util.getScreenOrientation(this));
        }
    }

    private void initializeSDK(){
        String licenseKey = DataContext.getInstance().getLicenseKey();
        isConnect = isConnectWS();

        // load the controller instance
        Util.lockScreen(this);
        if(isConnect){
            if(buttonMedical!=null){
                buttonMedical.setEnabled(false);
            }
            if(textViewCardInfo!=null){
                textViewCardInfo.setVisibility(View.GONE);
            }
            if(editTextLicense!=null){
                editTextLicense.setVisibility(View.GONE);
            }
            if(activateLicenseButton!=null){
                activateLicenseButton.setVisibility(View.GONE);
            }
            acuantAndroidMobileSdkControllerInstance = AcuantAndroidMobileSDKController.getInstance(this,assureIDUsername,assureIDPassword,assureIDSubscription,assureIDURL);
            acuantAndroidMobileSdkControllerInstance.setConnectWebServiceListener(this);

        }else {
            if(buttonMedical!=null){
                buttonMedical.setEnabled(true);
            }
            if(textViewCardInfo!=null){
                textViewCardInfo.setVisibility(View.VISIBLE);
            }
            if(editTextLicense!=null){
                editTextLicense.setVisibility(View.VISIBLE);
            }
            if(activateLicenseButton!=null){
                activateLicenseButton.setVisibility(View.VISIBLE);
            }
            acuantAndroidMobileSdkControllerInstance = AcuantAndroidMobileSDKController.getInstance(this, licenseKey);
            acuantAndroidMobileSdkControllerInstance.setCloudUrl("cssnwebservices.com");
            //acuantAndroidMobileSdkControllerInstance.setCloudUrl("cssnwebservicestest.com");

        }
        Util.lockScreen(this);
        if (!Util.isTablet(this)) {
            acuantAndroidMobileSdkControllerInstance.setPdf417BarcodeImageDrawable(getResources().getDrawable(R.drawable.barcode));
        }


        acuantAndroidMobileSdkControllerInstance.setWebServiceListener(this);
        acuantAndroidMobileSdkControllerInstance.setWatermarkText("Powered By Acuant", 0, 0, 30, 0);
        acuantAndroidMobileSdkControllerInstance.setFacialRecognitionTimeoutInSeconds(20);
        DisplayMetrics metrics = this.getResources().getDisplayMetrics();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        int minLength = (int) (Math.min(width,height)*0.9);
        int maxLength = (int) (minLength*1.5);
        final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        Typeface currentTypeFace =   textPaint.getTypeface();
        Typeface bold = Typeface.create(currentTypeFace, Typeface.BOLD);
        textPaint.setColor(Color.WHITE);
        if (!Util.isTablet(this)) {
            Display display = getWindowManager().getDefaultDisplay();
            if(display.getWidth()<1000 || display.getHeight()<1000){
                textPaint.setTextSize(30);
            }else {
                textPaint.setTextSize(50);
            }

        }else{
            textPaint.setTextSize(30);
        }
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(bold);

        Paint subtextPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        subtextPaint.setColor(Color.RED);
        if (!Util.isTablet(this)) {
            Display display = getWindowManager().getDefaultDisplay();
            if(display.getWidth()<1000 || display.getHeight()<1000){
                textPaint.setTextSize(20);
            }else {
                subtextPaint.setTextSize(40);
            }
        }else{
            subtextPaint.setTextSize(25);
        }
        subtextPaint.setTextAlign(Paint.Align.LEFT);
        subtextPaint.setTypeface(Typeface.create(subtextPaint.getTypeface(), Typeface.BOLD));



        final String instrunctionStr = "Get closer until Red Rectangle appears and Blink";
        final String subInstString = "Analyzing...";
        Rect bounds = new Rect();
        textPaint.getTextBounds(instrunctionStr, 0, instrunctionStr.length(), bounds);
        int top = (int)(height*0.05);
        if (Util.isTablet(this)) {
            top = top - 20;
        }
        int left = (width-bounds.width())/2;

        textPaint.getTextBounds(subInstString, 0, subInstString.length(), bounds);
        int subLeft = (width-bounds.width())/2;

        acuantAndroidMobileSdkControllerInstance.setInstructionText(instrunctionStr, left,top,textPaint);
        if (!Util.isTablet(this)) {
            Display display = getWindowManager().getDefaultDisplay();
            if(display.getWidth()<1000 || display.getHeight()<1000){
                acuantAndroidMobileSdkControllerInstance.setSubInstructionText(subInstString, subLeft,top+15,subtextPaint);
            }else {
                acuantAndroidMobileSdkControllerInstance.setSubInstructionText(subInstString, subLeft,top+60,subtextPaint);
            }
        }else{
            acuantAndroidMobileSdkControllerInstance.setSubInstructionText(subInstString, subLeft,top+30,subtextPaint);
        }


        //acuantAndroidMobileSdkControllerInstance.setShowActionBar(false);
        //acuantAndroidMobileSdkControllerInstance.setShowStatusBar(false);
        acuantAndroidMobileSdkControllerInstance.setFlashlight(false);
        //acuantAndroidMobileSdkControllerInstance.setFlashlight(0,0,50,0);
        //acuantAndroidMobileSdkControllerInstance.setFlashlightImageDrawable(getResources().getDrawable(R.drawable.lighton), getResources().getDrawable(R.drawable.lightoff));
        //acuantAndroidMobileSdkControllerInstance.setShowInitialMessage(true);
        //acuantAndroidMobileSdkControllerInstance.setCropBarcode(true);
        //acuantAndroidMobileSdkControllerInstance.setCropBarcodeOnCancel(true);
        //acuantAndroidMobileSdkControllerInstance.setPdf417BarcodeDialogWaitingBarcode("AcuantAndroidMobileSampleSDK","ALIGN AND TAP", 10, "Try Again", "Yes");
        //acuantAndroidMobileSdkControllerInstance.setCanShowBracketsOnTablet(true);
        // load several member variables

        acuantAndroidMobileSdkControllerInstance.setCardCroppingListener(this);
        acuantAndroidMobileSdkControllerInstance.setAcuantErrorListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void SaveImage(Bitmap finalBitmap) {

        String fname = "/sdcard/cropped.jpg";
        File file = new File (fname);
        if (file.exists ()) file.delete ();
        try {
            file.createNewFile();
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isConnectWS(){
        boolean retValue = false;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        assureIDSubscription = sharedPref.getString("AssureID_Subscription","");
        assureIDUsername = sharedPref.getString("AssureID_Username","");
        assureIDPassword = sharedPref.getString("AssureID_Password","");
        assureIDURL = sharedPref.getString("AssureID_Cloud_URL","");
        boolean enabled = sharedPref.getBoolean("AssureID_Enable",false);


        if(enabled && assureIDSubscription!=null && !assureIDSubscription.trim().equalsIgnoreCase("")
                && assureIDUsername!=null && !assureIDUsername.trim().equalsIgnoreCase("")
                && assureIDPassword!=null && !assureIDPassword.trim().equalsIgnoreCase("")
                && assureIDURL!=null && !assureIDURL.trim().equalsIgnoreCase("")){

            retValue = true;

        }

        return retValue;
    }


    /**
     *
     */
    private void validateLicenseKey(String licenseKey) {
        Util.lockScreen(MainActivity.this);
        DataContext.getInstance().setLicenseKey(editTextLicense.getText().toString());
        if(progressDialog!=null && progressDialog.isShowing()){
            Util.dismissDialog(progressDialog);
        }
        isValidating = true;
        acuantAndroidMobileSdkControllerInstance.setLicensekey(licenseKey);
        hideVirtualKeyboard();
    }

    /**
     *
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Util.LOG_ENABLED) {
            Utils.appendLog(TAG, "protected void onActivityResult(int requestCode, int resultCode, Intent data)");
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        updateUI();
    }

    @Override
    public void onCardCroppingStart(Activity activity) {
        if (Utils.LOG_ENABLED) {
            Utils.appendLog(TAG, "public void onCardCroppingStart(Activity activity)");
        }
        cardRegion = DataContext.getInstance().getCardRegion();
        if(progressDialog!=null && progressDialog.isShowing()) {
            Util.dismissDialog(progressDialog);
        }
        Util.lockScreen(this);
        progressDialog = Util.showProgessDialog(activity, "Cropping image...");
        isCropping = true;
    }

    /**
     * Result from the CSSNMobileSDKController.showCameraInterface method when
     * popover == true
     */
    @Override
    public void onCardCroppingFinish(final Bitmap bitmap) {
        if(progressDialog!=null && progressDialog.isShowing()) {
            Util.dismissDialog(progressDialog);
            progressDialog = null;
        }
        TempImageStore.setBitmapImage(bitmap);
        TempImageStore.setImageConfirmationListener(new ConfirmationListener() {
            @Override
            public void confimed() {
                if (Util.LOG_ENABLED) {
                    Utils.appendLog("appendLog", "public void onCardCroppedFinish(final Bitmap bitmap) - begin");
                }
                if (bitmap != null) {
                    updateModelAndUIFromCroppedCard(bitmap);
                }else{
                    // set an error to be shown in the onResume method.
                    mainActivityModel.setErrorMessage("Unable to detect the card. Please try again.");
                    updateModelAndUIFromCroppedCard(originalImage);
                }
                Util.unLockScreen(MainActivity.this);

                if (Util.LOG_ENABLED) {
                    Utils.appendLog("appendLog", "public void onCardCroppedFinish(final Bitmap bitmap) - end");
                }
                isCropping = false;
            }

            @Override
            public void retry() {
                showCameraInterface();
            }
        });

        Intent imageConfirmationIntent = new Intent(this, ImageConformationActivity.class);
        if(bitmap==null){
            TempImageStore.setCroppingPassed(false);
        }else{
            TempImageStore.setCroppingPassed(true);
        }
        TempImageStore.setCardType(mainActivityModel.getCurrentOptionType());
        startActivity(imageConfirmationIntent);

    }

    /**
     * Result from the CSSNMobileSDKController.showCameraInterface method when
     * popover == true
     */
    @Override
    public void onCardCroppingFinish(final Bitmap bitmap, final boolean scanBackSide) {
        if(progressDialog!=null && progressDialog.isShowing()) {
            Util.dismissDialog(progressDialog);
            progressDialog = null;
        }
        TempImageStore.setBitmapImage(bitmap);
        TempImageStore.setImageConfirmationListener(new ConfirmationListener() {
            @Override
            public void confimed() {
                presentCameraForBackSide(bitmap,scanBackSide);
            }

            @Override
            public void retry() {
                if ((cardRegion == Region.REGION_UNITED_STATES || cardRegion == Region.REGION_CANADA)
                        &&  mainActivityModel.getCurrentOptionType()==CardType.DRIVERS_LICENSE
                        && isBackSide) {
                    acuantAndroidMobileSdkControllerInstance.setInitialMessageDescriptor(R.layout.tap_to_focus);
                    acuantAndroidMobileSdkControllerInstance.showCameraInterfacePDF417(mainActivity, CardType.DRIVERS_LICENSE, cardRegion);
                } else {
                    showCameraInterface();
                }
            }
        });
        Intent imageConfirmationIntent = new Intent(this, ImageConformationActivity.class);
        if(bitmap==null){
            TempImageStore.setCroppingPassed(false);
        }else{
            TempImageStore.setCroppingPassed(true);
        }
        TempImageStore.setCardType(mainActivityModel.getCurrentOptionType());
        startActivity(imageConfirmationIntent);
    }

    public void presentCameraForBackSide(final Bitmap bitmap, boolean scanBackSide) {

        if (Util.LOG_ENABLED) {
            Utils.appendLog("appendLog", "public void onCardCroppedFinish(final Bitmap bitmap) - begin");
        }
        cardRegion = DataContext.getInstance().getCardRegion();
        if (bitmap != null) {
            isBackSide = scanBackSide;
            if (isBackSide) {
                mainActivityModel.setCardSideSelected(MainActivityModel.CardSide.FRONT);
            } else {
                mainActivityModel.setCardSideSelected(MainActivityModel.CardSide.BACK);
            }

            if (mainActivityModel.getCurrentOptionType() == CardType.DRIVERS_LICENSE && isBackSide) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showDuplexDialog();
                    }
                }, 100);
            }
            updateModelAndUIFromCroppedCard(bitmap);
        } else {
            // set an error to be shown in the onResume method.
            mainActivityModel.setErrorMessage("Unable to detect the card. Please try again.");
            updateModelAndUIFromCroppedCard(originalImage);
        }

        Util.unLockScreen(this);

        if (Util.LOG_ENABLED) {
            Utils.appendLog("appendLog", "public void onCardCroppedFinish(final Bitmap bitmap) - end");
        }
        isCropping = false;
    }

    private void showDuplexDialog() {
        mainActivity = this;
        cardRegion = DataContext.getInstance().getCardRegion();
        Util.dismissDialog(showDuplexAlertDialog);
        Util.dismissDialog(alertDialog);
        showDuplexAlertDialog = new AlertDialog.Builder(this).create();
        showDuplexAlertDialog = Util.showDialog(this, getString(R.string.dl_duplex_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (cardRegion == Region.REGION_UNITED_STATES || cardRegion == Region.REGION_CANADA) {
                    acuantAndroidMobileSdkControllerInstance.setInitialMessageDescriptor(R.layout.tap_to_focus);
                    acuantAndroidMobileSdkControllerInstance.showCameraInterfacePDF417(mainActivity, CardType.DRIVERS_LICENSE, cardRegion);
                } else {
                    acuantAndroidMobileSdkControllerInstance.showManualCameraInterface(mainActivity, CardType.DRIVERS_LICENSE, cardRegion, isBackSide);
                }
                dialog.dismiss();
                isShowDuplexDialog = false;
            }
        });
        isShowDuplexDialog = true;
    }


    private void showFacialDialog() {
        try {
            new AlertDialog.Builder(this)
                    .setCancelable(false)
                    .setMessage(getString(R.string.facial_instruction_dialog))
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            acuantAndroidMobileSdkControllerInstance.setFacialListener(MainActivity.this);
                            isProcessingFacial = acuantAndroidMobileSdkControllerInstance.showManualFacialCameraInterface(MainActivity.this);
                            dialog.dismiss();
                        }
                    }).show();
        }catch(Exception e){
            e.printStackTrace();
        }

    }


    @Override
    public void onPDF417Finish(String result) {
        sPdf417String = result;
    }

    @Override
    public void onOriginalCapture(Bitmap bitmap) {
        originalImage = bitmap;
    }

    @Override
    public void onCancelCapture(Bitmap croppedImage,Bitmap originalImage) {
        Utils.appendLog("Acuant", "onCancelCapture");
        if(croppedImage!=null || originalImage!=null) {
            final Bitmap cImage = croppedImage;
            final Bitmap oImage = originalImage;
            if (cImage != null) {
                mainActivityModel.setBackSideCardImage(cImage);
            } else if (oImage != null) {
                mainActivityModel.setBackSideCardImage(oImage);
            }
        }
    }

    @Override
    public void onBarcodeTimeOut(Bitmap croppedImage,Bitmap originalImage) {
        final Bitmap cImage = croppedImage;
        final Bitmap oImage = originalImage;
        acuantAndroidMobileSdkControllerInstance.pauseScanningBarcodeCamera();
        AlertDialog.Builder builder = new AlertDialog.Builder(acuantAndroidMobileSdkControllerInstance.getBarcodeCameraContext());
        // barcode Dialog "ignore" option
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                if(cImage!=null){
                    mainActivityModel.setBackSideCardImage(cImage);
                }else if (oImage != null) {
                    mainActivityModel.setBackSideCardImage(oImage);
                }
                acuantAndroidMobileSdkControllerInstance.finishScanningBarcodeCamera();
                dialog.dismiss();
            }
        });
        // barcode Dialog "retry" option
        builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                acuantAndroidMobileSdkControllerInstance.resumeScanningBarcodeCamera();
                dialog.dismiss();
            }
        });
        //barcode Dialog title and main message
        builder.setMessage("Unable to scan the barcode?");
        builder.setTitle("AcuantMobileSDK");
        builder.create().show();

    }

    /**
     * Updates the model, and the ui. Called after acquiring a cropped card.
     */
    private void updateModelAndUIFromCroppedCard(final Bitmap bitmap) {
        switch (mainActivityModel.getCardSideSelected()) {
            case FRONT:
                mainActivityModel.setFrontSideCardImage(bitmap);
                break;

            case BACK:
                mainActivityModel.setBackSideCardImage(bitmap);
                break;

            default:
                throw new IllegalStateException("This method is bad implemented, there is not processing for the cardSide '"
                        + mainActivityModel.getCardSideSelected() + "'");
        }
        updateUI();
    }

    /**
     * @param v
     */
    public void frontSideCapturePressed(View v) {
        if (Util.LOG_ENABLED) {
            Utils.appendLog(TAG, "public void frontSideCapturePressed(View v)");
        }
        isBackSide = false;

        mainActivityModel.clearImages();

        mainActivityModel.setCardSideSelected(MainActivityModel.CardSide.FRONT);

        showCameraInterface();
    }

    /**
     * @param v
     */
    public void backSideCapturePressed(View v) {
        if (Util.LOG_ENABLED) {
            Utils.appendLog(TAG, "public void backSideCapturePressed(View v)");
        }
        isBackSide = true;

        //mainActivityModel.clearImages();

        mainActivityModel.setCardSideSelected(MainActivityModel.CardSide.BACK);
        showCameraInterface();
    }

    /**
     *
     */
    private void showCameraInterface() {
        final int currentOptionType = mainActivityModel.getCurrentOptionType();
        cardRegion = DataContext.getInstance().getCardRegion();
        alertDialog = new AlertDialog.Builder(this).create();
        LicenseDetails license_details = DataContext.getInstance().getCssnLicenseDetails();
        if (currentOptionType == CardType.PASSPORT) {
            acuantAndroidMobileSdkControllerInstance.setWidth(AcuantUtil.DEFAULT_CROP_PASSPORT_WIDTH);
        }else if (currentOptionType == CardType.MEDICAL_INSURANCE) {
            acuantAndroidMobileSdkControllerInstance.setWidth(AcuantUtil.DEFAULT_CROP_MEDICAL_INSURANCE);
        } else {
            if(license_details!=null && license_details.isAssureIDAllowed()) {
                acuantAndroidMobileSdkControllerInstance.setWidth(AcuantUtil.DEFAULT_CROP_DRIVERS_LICENSE_WIDTH_FOR_AUTHENTICATION);
            }else {
                acuantAndroidMobileSdkControllerInstance.setWidth(AcuantUtil.DEFAULT_CROP_DRIVERS_LICENSE_WIDTH);
            }
        }
        acuantAndroidMobileSdkControllerInstance.setInitialMessageDescriptor(R.layout.align_and_tap);
        acuantAndroidMobileSdkControllerInstance.setFinalMessageDescriptor(R.layout.hold_steady);
        acuantAndroidMobileSdkControllerInstance.showManualCameraInterface(this, currentOptionType, cardRegion, isBackSide);
    }
    /**
     * Called after a tap in the driver's card button.
     *
     * @param v
     */
    public void driverCardWithFacialButtonPressed(View v){
        // update the model
        processedCardInformation = null;
        processedFacialData=null;
        mainActivityModel.setCurrentOptionType(CardType.DRIVERS_LICENSE);
        mainActivityModel.clearImages();
        isProcessing=false;
        isProcessingFacial=false;
        if(DataContext.getInstance().getCssnLicenseDetails()!=null && DataContext.getInstance().getCssnLicenseDetails().isFacialAllowed()) {
            isFacialFlow = true;
        }else{
            isFacialFlow = false;
        }
        Intent regionList = new Intent(this, RegionList.class);
        this.startActivity(regionList);
        updateUI();
    }

    /**
     * Called after a tap in the passport card button.
     *
     * @param v
     */

    public void passportCardWithFacialButtonPressed(View v) {
        processedCardInformation = null;
        processedFacialData=null;
        mainActivityModel.setCurrentOptionType(CardType.PASSPORT);
        isProcessing=false;
        isProcessingFacial=false;
        if(DataContext.getInstance().getCssnLicenseDetails()!=null && DataContext.getInstance().getCssnLicenseDetails().isFacialAllowed()) {
            isFacialFlow = true;
        }else{
            isFacialFlow = false;
        }
        mainActivityModel.clearImages();

        updateUI();
    }

    /**
     * Called after a tap in the medical card button.
     *
     * @param v
     */
    public void medicalCardButtonPressed(View v) {
        processedCardInformation = null;
        processedFacialData=null;
        mainActivityModel.setCurrentOptionType(CardType.MEDICAL_INSURANCE);
        mainActivityModel.clearImages();

        updateUI();
    }

    /**
     * calculate the width and height of the front side card image and resize them
     */
    private void resizeImageFrames(int cardType) {
        double aspectRatio = AcuantUtil.getAspectRatio(cardType);

        int height = (int) (layoutFrontImage.getLayoutParams().width * aspectRatio);
        int width = layoutFrontImage.getLayoutParams().width;

        layoutFrontImage.getLayoutParams().height = height;
        layoutFrontImage.getLayoutParams().width = width;

        layoutFrontImage.setLayoutParams(layoutFrontImage.getLayoutParams());

        if (cardType == CardType.MEDICAL_INSURANCE) {
            layoutBackImage.getLayoutParams().height = height;
            layoutBackImage.getLayoutParams().width = width;

            layoutBackImage.setLayoutParams(layoutBackImage.getLayoutParams());
        }
    }

    /**
     * Updates the card's frame layout, shows/hides the back side card frame,
     * highlights the selected option, and load the card images in the view.
     */
    public void updateUI() {
        if (Utils.LOG_ENABLED) {
            Utils.appendLog(TAG, "private void updateUI()");
        }

        if (mainActivityModel.getErrorMessage() != null) {
            Util.dismissDialog(alertDialog);

            alertDialog = Util.showDialog(this, mainActivityModel.getErrorMessage(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mainActivityModel.setErrorMessage(null);
                    isShowErrorAlertDialog = false;
                }
            });
            isShowErrorAlertDialog = true;
        }

        // change orientation issues
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            layoutCards.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            layoutCards.setOrientation(LinearLayout.VERTICAL);
        }

        // enable/disable the activate button
        updateActivateLicenseButtonFromModel();

        if (mainActivityModel.getCurrentOptionType() == -1) {
            // do not do any extra processing
            return;
        }

        // calculate the width and height of the front side card image and resize them
        resizeImageFrames(mainActivityModel.getCurrentOptionType());

        // show/hide the front and back views in the layout
        switch (mainActivityModel.getCurrentOptionType()) {
            case CardType.PASSPORT:
                txtTapToCaptureFront.setText(getResources().getString(R.string.tap_to_capture));
                showFrontSideCardImage();
                hideBackSideCardImage();
                break;

            case CardType.DRIVERS_LICENSE:
                showFrontSideCardImage();
                if (mainActivityModel.getBackSideCardImage() != null){
                    showBackSideCardImage();
                }else{
                    hideBackSideCardImage();
                }
                break;

            case CardType.MEDICAL_INSURANCE:
                txtTapToCaptureFront.setText(R.string.tap_to_capture_front_side);
                showFrontSideCardImage();
                showBackSideCardImage();
                break;
            case CardType.FACIAL_RECOGNITION:
                showFrontSideCardImage();
                if (mainActivityModel.getBackSideCardImage() != null){
                    showBackSideCardImage();
                }else{
                    hideBackSideCardImage();
                }
                break;

            default:
                throw new IllegalArgumentException(
                        "This method is wrong implemented, there is not processing for the card type '" + mainActivityModel.getCurrentOptionType() + "'");

        }

        // update card in front image view
        frontImageView.setImageBitmap( Util.getRoundedCornerBitmap(mainActivityModel.getFrontSideCardImage(), this.getApplicationContext()));

        if (mainActivityModel.getFrontSideCardImage() != null) {
            hideFrontImageText();
        } else {
            showFrontImageText();
        }

        // update card in back image view
        backImageView.setImageBitmap(Util.getRoundedCornerBitmap(mainActivityModel.getBackSideCardImage(), this.getApplicationContext()));

        if (mainActivityModel.getBackSideCardImage() != null) {
            hideBackImageText();
        } else {
            showBackImageText();
        }

        // update the process button
        if (mainActivityModel.getFrontSideCardImage() != null) {
            processCardButton.setVisibility(View.VISIBLE);
        } else {
            processCardButton.setVisibility(View.GONE);
        }

        highlightCurrentCardOption();

    }

    /**
     * Highlights the current option: drivers card, medical or passport.
     */
    private void highlightCurrentCardOption() {
        int buttonId = 0;

        switch (mainActivityModel.getCurrentOptionType()) {

            case CardType.DRIVERS_LICENSE:
                buttonId = R.id.buttonDriver;

                buttonId = R.id.buttonDriver;

                break;

            case CardType.PASSPORT:
                buttonId = R.id.buttonPassport;

                buttonId = R.id.buttonPassport;

                break;

            case CardType.MEDICAL_INSURANCE:

                buttonId = R.id.buttonMedical;

                break;
            case CardType.FACIAL_RECOGNITION:
                if(processedCardInformation instanceof DriversLicenseCard) {
                    buttonId = R.id.buttonDriver;
                }else if(processedCardInformation instanceof PassportCard){
                    buttonId = R.id.buttonPassport;
                }

                break;

            default:
                throw new IllegalArgumentException(
                        "This method is wrong implemented, there is not processing for the card type '"
                                + mainActivityModel.getCurrentOptionType() + "'");

        }

        ((Button) findViewById(R.id.buttonDriver)).setTypeface(null, Typeface.NORMAL);
        ((Button) findViewById(R.id.buttonPassport)).setTypeface(null, Typeface.NORMAL);
        ((Button) findViewById(R.id.buttonMedical)).setTypeface(null, Typeface.NORMAL);

        ((Button) findViewById(buttonId)).setTypeface(null, Typeface.BOLD);
    }

    /**
     * Called by the process Button
     *
     * @param v
     */
    public void processCard(View v) {
        if(isFacialFlow) {
            if (mainActivityModel.getCurrentOptionType() == CardType.FACIAL_RECOGNITION || mainActivityModel.getCurrentOptionType() == CardType.PASSPORT || mainActivityModel.getCurrentOptionType() == CardType.DRIVERS_LICENSE) {
                isProcessingFacial = true;
                showFacialDialog();
            }
        }
        if(!isProcessingFacial) {
            if(progressDialog!=null && progressDialog.isShowing()){
                Util.dismissDialog(progressDialog);
            }
            progressDialog = Util.showProgessDialog(MainActivity.this, "Capturing data ...");
            Util.lockScreen(this);
        }
        if ((!isProcessing && processedCardInformation==null) || mainActivityModel.getCurrentOptionType()==CardType.MEDICAL_INSURANCE) {
            isProcessing = true;
            // check for the internet connection
            if (!Utils.isNetworkAvailable(this)) {
                String msg = getString(R.string.no_internet_message);
                Utils.appendLog(TAG, msg);
                Util.dismissDialog(alertDialog);
                alertDialog = Util.showDialog(this, msg,new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isShowErrorAlertDialog = false;
                    }
                });
                isShowErrorAlertDialog = true;
                return;
            }

            // process the card
            //progressDialog = Util.showProgessDialog(MainActivity.this, "Capturing data ...");

            //Util.lockScreen(this);

            ProcessImageRequestOptions options = ProcessImageRequestOptions.getInstance();
            options.autoDetectState = true;
            options.stateID = -1;
            options.reformatImage = true;
            options.reformatImageColor = 0;
            options.DPI = 150;
            options.cropImage = false;
            options.faceDetec = true;
            options.signDetec = true;
            options.imageSource = 101;
            options.iRegion = DataContext.getInstance().getCardRegion();
            options.acuantCardType = mainActivityModel.getCurrentOptionType();

            if(isConnect){
                acuantAndroidMobileSdkControllerInstance.callProcessImageConnectServices(mainActivityModel.getFrontSideCardImage(), mainActivityModel.getBackSideCardImage(), sPdf417String, this, options);
            }else {

                acuantAndroidMobileSdkControllerInstance.callProcessImageServices(mainActivityModel.getFrontSideCardImage(), mainActivityModel.getBackSideCardImage(), sPdf417String, this, options);

            }
            resetPdf417String();
        }
    }


    /**
     * Called after card processing is over.
     *
     * @param
     */
    public void processImageValidation(Bitmap faceImage,Bitmap idCropedFaceImage) {
        if(processedCardInformation!=null){
            isProcessingFacial=false;
        }
        mainActivityModel.setCurrentOptionType(CardType.FACIAL_RECOGNITION);
        if (!Utils.isNetworkAvailable(this)) {
            String msg = getString(R.string.no_internet_message);
            Utils.appendLog(TAG, msg);
            Util.dismissDialog(alertDialog);
            alertDialog = Util.showDialog(this, msg,new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isShowErrorAlertDialog = false;
                }
            });
            isShowErrorAlertDialog = true;
            return;
        }

        //Util.lockScreen(this);

        ProcessImageRequestOptions options = ProcessImageRequestOptions.getInstance();
        options.acuantCardType = CardType.FACIAL_RECOGNITION;
        acuantAndroidMobileSdkControllerInstance.callProcessImageServices(faceImage, idCropedFaceImage, null, this, options);
    }

    private void resetPdf417String() {
        sPdf417String = "";
    }

    /**
     * @param v
     */
    public void activateLicenseKey(View v) {
        hideVirtualKeyboard();

        String key = editTextLicense.getText().toString().trim();
        if (!key.equals("")) {
            Util.lockScreen(MainActivity.this);
            if(progressDialog!=null && progressDialog.isShowing()){
                Util.dismissDialog(progressDialog);
            }
            progressDialog = Util.showProgessDialog(MainActivity.this, "Activating License ..");
            isActivating = true;
            acuantAndroidMobileSdkControllerInstance.callActivateLicenseKeyService(key);

        } else {
            Util.dismissDialog(alertDialog);
            alertDialog = Util.showDialog(this, "The license key cannot be empty.",new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isShowErrorAlertDialog = false;
                }
            });
            isShowErrorAlertDialog = true;
        }
    }

    /**
     *
     */
    @Override
    public void processImageServiceCompleted(Card card) {
        if (Util.LOG_ENABLED) {
            Utils.appendLog(TAG, "public void processImageServiceCompleted(CSSNCard card, int status, String errorMessage)");
        }

        if(mainActivityModel.getCurrentOptionType()!=CardType.FACIAL_RECOGNITION) {
            isProcessing = false;
            processedCardInformation = card;
        }else{
            isProcessingFacial=false;
            processedFacialData = (FacialData) card;
        }

        presentResults(processedCardInformation,processedFacialData);

    }


    public void presentResults(Card card,FacialData facialData){
        if(!isProcessing && !isProcessingFacial) {
            Util.dismissDialog(progressDialog);
            showData(card,facialData);
        }

    }

    public void showData(Card card,FacialData facialData){
        String dialogMessage = null;
        try {
            DataContext.getInstance().setCardType(mainActivityModel.getCurrentOptionType());

            if (card == null || card.isEmpty()) {
                dialogMessage = "No data found for this license card!";
            } else {

                switch (mainActivityModel.getCurrentOptionType()) {
                    case CardType.DRIVERS_LICENSE:
                        DataContext.getInstance().setProcessedLicenseCard((DriversLicenseCard) card);
                        break;

                    case CardType.MEDICAL_INSURANCE:
                        DataContext.getInstance().setProcessedMedicalCard((MedicalCard) card);
                        break;

                    case CardType.PASSPORT:
                        DataContext.getInstance().setProcessedPassportCard((PassportCard) card);
                        break;
                    case CardType.FACIAL_RECOGNITION:
                        if( processedCardInformation instanceof DriversLicenseCard) {
                            DriversLicenseCard dlCard = (DriversLicenseCard)processedCardInformation;
                            DataContext.getInstance().setProcessedLicenseCard(dlCard);
                            DataContext.getInstance().setCardType(CardType.DRIVERS_LICENSE);
                        }else if(processedCardInformation instanceof PassportCard) {
                            PassportCard passportCard = (PassportCard) processedCardInformation;
                            DataContext.getInstance().setProcessedPassportCard(passportCard);
                            DataContext.getInstance().setCardType(CardType.PASSPORT);
                        }
                        DataContext.getInstance().setProcessedFacialData(processedFacialData);
                        break;
                    default:
                        throw new IllegalStateException("There is not implementation for processing the card type '"
                                + mainActivityModel.getCurrentOptionType() + "'");
                }

                Util.unLockScreen(MainActivity.this);
                Intent showDataActivityIntent = new Intent(this, ShowDataActivity.class);
                showDataActivityIntent.putExtra("FACIAL",isFacialFlow);
                this.startActivity(showDataActivityIntent);
            }


        } catch (Exception e) {
            Utils.appendLog(TAG, e.getMessage());
            dialogMessage = "Sorry! Internal error has occurred, please contact us!";

        }

        if (dialogMessage != null) {
            Util.dismissDialog(alertDialog);
            alertDialog = Util.showDialog(this, dialogMessage, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isShowErrorAlertDialog = false;
                }
            });
            isShowErrorAlertDialog = true;
        }
    }

    /**
     *
     */
    @Override
    public void activateLicenseKeyCompleted(LicenseActivationDetails cssnLicenseActivationDetails) {
        Util.dismissDialog(progressDialog);
        Util.unLockScreen(MainActivity.this);
        isActivating = false;

        String msg="";

        if (cssnLicenseActivationDetails != null) {
            msg = cssnLicenseActivationDetails.getIsLicenseKeyActivatedDescscription();
        }

        Util.lockScreen(this);
        alertDialog =  Util.showDialog(this, msg, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Util.dismissDialog((Dialog) dialog);
                Util.unLockScreen(MainActivity.this);
                // validation if there was not error in the activation, because the
                // validation is done before, every time the license key text
                // changes.
                validateLicenseKey(editTextLicense.getText().toString());
                isShowErrorAlertDialog = false;
            }
        });
        isShowErrorAlertDialog = true;

    }

    /**
     *
     */
    @Override
    public void validateLicenseKeyCompleted(LicenseDetails details) {
        isFirstLoad = false;
        Util.dismissDialog(progressDialog);
        Util.unLockScreen(MainActivity.this);

        LicenseDetails cssnLicenseDetails = DataContext.getInstance().getCssnLicenseDetails();
        DataContext.getInstance().setCssnLicenseDetails(details);

        // update model
        mainActivityModel.setState(State.VALIDATED);
        if (cssnLicenseDetails != null && cssnLicenseDetails.isLicenseKeyActivated()) {
            mainActivityModel.setValidatedStateActivation(State.ValidatedStateActivation.ACTIVATED);
        } else {
            mainActivityModel.setValidatedStateActivation(State.ValidatedStateActivation.NO_ACTIVATED);
        }
        updateActivateLicenseButtonFromModel();
        // message dialogs
        acuantAndroidMobileSdkControllerInstance.enableLocationAuthentication(this);
        isValidating = false;

    }

    /**
     */
    private void showFrontSideCardImage() {
        layoutFrontImage.setClickable(true);
        layoutFrontImage.setVisibility(View.VISIBLE);
    }

    /**
     *
     */
    private void hideFrontSideCardImage() {
        layoutFrontImage.setClickable(false);
        layoutFrontImage.setVisibility(View.GONE);
    }

    /**
     *
     */
    private void showBackSideCardImage() {
        layoutBackImage.setClickable(true);
        layoutBackImage.setVisibility(View.VISIBLE);
    }

    /**
     *
     */
    private void hideBackSideCardImage() {
        layoutBackImage.setClickable(false);
        layoutBackImage.setVisibility(View.GONE);
    }

    /**
     *
     */
    private void showFrontImageText() {
        txtTapToCaptureFront.setVisibility(View.VISIBLE);
    }

    /**
     *
     */
    private void hideFrontImageText() {
        txtTapToCaptureFront.setVisibility(View.GONE);
    }

    /**
     *
     */
    private void showBackImageText() {
        txtTapToCaptureBack.setVisibility(View.VISIBLE);
    }

    /**
     *
     */
    private void hideBackImageText() {
        txtTapToCaptureBack.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean wschanged = sharedPref.getBoolean("WSCHANGED",false);
        if(wschanged && !isFirstLoad){
            initializeSDK();
        }
        if (Util.LOG_ENABLED) {
            Utils.appendLog(TAG, "protected void onResume()");
        }
        editTextLicense.clearFocus();
        frontImageView.requestFocus();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //spdf417 = savedInstanceState.get(PDF417_STRING_KEY) != null ? (String) savedInstanceState.get(PDF417_STRING_KEY) : "";
        isShowErrorAlertDialog = savedInstanceState.getBoolean(IS_SHOWING_DIALOG_KEY, false);
        isProcessing = savedInstanceState.getBoolean(IS_PROCESSING_DIALOG_KEY, false);
        isCropping = savedInstanceState.getBoolean(IS_CROPPING_DIALOG_KEY, false);
        isValidating = savedInstanceState.getBoolean(IS_VALIDATING_DIALOG_KEY, false);
        isActivating = savedInstanceState.getBoolean(IS_ACTIVATING_DIALOG_KEY, false);
        isShowDuplexDialog = savedInstanceState.getBoolean(IS_SHOWDUPLEXDIALOG_DIALOG_KEY, false);
        if(progressDialog!=null && progressDialog.isShowing()){
            Util.dismissDialog(progressDialog);
        }
        if (isShowDuplexDialog) {
            showDuplexDialog();
        }
        if (isProcessing) {
            progressDialog = Util.showProgessDialog(MainActivity.this, "Capturing data ...");
        }
        if (isCropping){
            progressDialog = Util.showProgessDialog(MainActivity.this, "Cropping image...");
        }
        if (isValidating){
            progressDialog = Util.showProgessDialog(MainActivity.this, "Validating License ..");
        }
        if (isActivating){
            progressDialog = Util.showProgessDialog(MainActivity.this, "Activating License ..");
        }
        if (isShowErrorAlertDialog){
            alertDialog.show();
        }
        updateUI();
    }

    /**
     *
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (Util.LOG_ENABLED) {
            Utils.appendLog(TAG, "protected void onSaveInstanceState(Bundle outState)");
        }

        DataContext.getInstance().setMainActivityModel(mainActivityModel);
        //outState.putString(PDF417_STRING_KEY, this.pdf417);
        outState.putBoolean(IS_SHOWING_DIALOG_KEY, isShowErrorAlertDialog);
        outState.putBoolean(IS_PROCESSING_DIALOG_KEY, isProcessing);
        outState.putBoolean(IS_CROPPING_DIALOG_KEY, isCropping);
        outState.putBoolean(IS_ACTIVATING_DIALOG_KEY, isActivating);
        outState.putBoolean(IS_VALIDATING_DIALOG_KEY, isValidating);
        outState.putBoolean(IS_SHOWDUPLEXDIALOG_DIALOG_KEY, isShowDuplexDialog);
    }

    /**
     *
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (Utils.LOG_ENABLED) {
            Utils.appendLog(TAG, "protected void onPause()");
        }
    }

    /**
     *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        TempImageStore.cleanup();
        acuantAndroidMobileSdkControllerInstance.cleanup();
        if (Util.LOG_ENABLED) {
            Utils.appendLog(TAG, "protected void onDestroy()");
        }
    }

    /**
     * @param bitmap
     * @return
     */
    private boolean saveBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("dd-mm-yyyy");
            String formattedDate = df.format(c.getTime());

            File file = new File("sdcard/CSSNCardCropped" + formattedDate + ".png");
            FileOutputStream fOutputStream = null;

            try {
                fOutputStream = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOutputStream);

                fOutputStream.flush();
                fOutputStream.close();

                MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
            } catch (FileNotFoundException e) {
                if (Util.LOG_ENABLED) {
                    Utils.appendLog(TAG, e.getMessage());
                }
                Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
                return false;
            } catch (IOException e) {
                if (Util.LOG_ENABLED) {
                    Utils.appendLog(TAG, e.getMessage());
                }
                Toast.makeText(this, "Save Failed", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     */
    private void hideVirtualKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editTextLicense.getWindowToken(), 0);
    }

    /**
     *
     */
    private void updateActivateLicenseButtonFromModel() {
        activateLicenseButton.setEnabled(
                mainActivityModel.getState() == State.NO_VALIDATED || (mainActivityModel.getState() == State.VALIDATED && mainActivityModel.getValidatedStateActivation() == State.ValidatedStateActivation.NO_ACTIVATED));
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public void didFailWithError(int code, String message) {
        Utils.appendLog("didFailWithError", "didFailWithError:" + code + "message" + message);
        Util.dismissDialog(progressDialog);
        Util.unLockScreen(MainActivity.this);
        String msg = message;
        if (code == ErrorType.AcuantErrorCouldNotReachServer) {
            msg = getString(R.string.no_internet_message);
        }else if (code == ErrorType.AcuantErrorUnableToCrop){
            updateModelAndUIFromCroppedCard(originalImage);
        }
        alertDialog = Util.showDialog(this, msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isShowErrorAlertDialog = false;
            }
        });
        isShowErrorAlertDialog = true;
        if (Util.LOG_ENABLED) {
            Utils.appendLog(TAG, "didFailWithError:" + message);
        }
        // message dialogs
        isValidating = false;
        isProcessing = false;
        isActivating = false;
    }

    @Override
    public void onFacialRecognitionTimedOut(final Bitmap bitmap) {
        isProcessingFacial=false;
        onFacialRecognitionCompleted(bitmap);
    }

    @Override
    public void onFacialRecognitionCompleted(final Bitmap bitmap) {
        if(isShowErrorAlertDialog){
            return;
        }
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                Util.lockScreen(MainActivity.this);
                if(progressDialog!=null && progressDialog.isShowing()){
                    Util.dismissDialog(progressDialog);
                }
                progressDialog = Util.showProgessDialog(MainActivity.this, "Capturing data ...");
                new Thread(new Runnable()
                {
                    @Override
                    public void run() {
                        while(isProcessing){
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        // process the card
                        if(processedCardInformation instanceof DriversLicenseCard) {
                            DriversLicenseCard dlCard = (DriversLicenseCard)processedCardInformation;
                            if(dlCard!=null) {
                                processImageValidation(bitmap, dlCard.getFaceImage());
                            }else{
                                processImageValidation(bitmap, null);
                            }
                        }else if(processedCardInformation instanceof PassportCard) {
                            PassportCard passportCard = (PassportCard) processedCardInformation;
                            if(passportCard!=null) {
                                processImageValidation(bitmap, passportCard.getFaceImage());
                            }else{
                                processImageValidation(bitmap, null);
                            }
                        }
                    }
                }).start();

            }
        });


    }

    @Override
    public void onFacialRecognitionCanceled(){
        isProcessingFacial=false;
    }


    //Override this only for API 23 and Above
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Permission.PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showCameraInterface();

                } else {
                    // permission denied
                    Util.showDialog(this,"Denied permission.Please give camera permission to proceed.");
                }
                return;
            }

            case Permission.PERMISSION_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    acuantAndroidMobileSdkControllerInstance.enableLocationAuthentication(this);
                } else {
                    // permission denied
                    Util.showDialog(this,"Denied permission.Please give location permission to proceed.");
                }
                return;
            }
        }
    }

    @Override
    public void processImageConnectServiceCompleted(String jsonString) {
        DataContext.getInstance().setCardType(mainActivityModel.getCurrentOptionType());
        if(progressDialog!=null && progressDialog.isShowing()){
            Util.dismissDialog(progressDialog);
        }
        String front_image_Loc="";
        String back_image_Loc="";
        String signature_image_loc="";
        String face_image_loc="";
        String dateOfBirth="";
        String dateofExpiry="";
        String documentNumber="";
        String documentType="";
        try {
            JSONObject jsonResponse = new JSONObject(jsonString);
            JSONArray images = jsonResponse.getJSONArray("Images");
            for(int i =0;i<images.length();i++){
                JSONObject imageDict = images.getJSONObject(i);
                if(imageDict.has("Side")){
                    int side = imageDict.getInt("Side");
                    if(side==0 && imageDict.has("Uri")){
                        front_image_Loc = imageDict.getString("Uri");
                    }else if(side==1 && imageDict.has("Uri")){
                        back_image_Loc = imageDict.getString("Uri");
                    }
                }
            }

            JSONArray fields = jsonResponse.getJSONArray("Fields");
            for(int i=0;i<fields.length();i++){
                JSONObject dict = fields.getJSONObject(i);
                if(dict.has("IsImage")){
                    boolean IsImage = dict.getBoolean("IsImage");
                    if(IsImage && dict.has("Value")){
                        if(dict.has("Key")){
                            if(dict.getString("Key").equalsIgnoreCase("Signature")){
                                signature_image_loc = dict.getString("Value");
                            }else if(dict.getString("Key").equalsIgnoreCase("Photo")){
                                face_image_loc = dict.getString("Value");
                            }
                        }
                    }
                }
                if(dict.has("Key") && dict.has("Type") && dict.getString("Type").equalsIgnoreCase("datetime") &&
                        dict.getString("Key").equalsIgnoreCase("Birth Date")|| (dict.getString("Key").equalsIgnoreCase("Expiration Date"))){
                    String dateStr = dict.getString("Value");
                    if(dateStr!=null && !dateStr.trim().equalsIgnoreCase("")) {
                        dateStr = dateStr.replace("Date","");
                        dateStr = dateStr.replace(")","");
                        dateStr = dateStr.replace("(","");
                        dateStr = dateStr.replace("/","");
                        if(dateStr!=null) {
                            Long longDate = Long.parseLong(dateStr);
                            if (longDate != null) {
                                Date date = new Date(longDate);
                                SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yy");
                                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
                                if (dict.getString("Key").equalsIgnoreCase("Birth Date")) {
                                    dateOfBirth = sdf.format(date);
                                } else if (dict.getString("Key").equalsIgnoreCase("Expiration Date")) {
                                    dateofExpiry = sdf.format(date);
                                }
                            }
                        }
                    }

                }
                if(dict.has("Key") && dict.has("Type") && dict.getString("Type").equalsIgnoreCase("string") &&
                        dict.getString("Key").equalsIgnoreCase("Document Number")){
                    documentNumber = dict.getString("Value");
                }

                if(dict.has("Key") && dict.has("Type") && dict.getString("Type").equalsIgnoreCase("string") &&
                        dict.getString("Key").equalsIgnoreCase("Document Class Name")){
                    documentType = dict.getString("Value");
                }
            }
            //String instanceID = jsonResponse.getString("InstanceId");
            Intent showDataActivityIntent = new Intent(this, ShowConnectDataActivity.class);
            showDataActivityIntent.putExtra("FACEIMAGEURL",face_image_loc);
            showDataActivityIntent.putExtra("SIGNATUREIMAGEURL",signature_image_loc);
            showDataActivityIntent.putExtra("FRONTIMAGEURL",front_image_Loc);
            showDataActivityIntent.putExtra("BACKIMAGEURL",back_image_Loc);
            showDataActivityIntent.putExtra("RESPONSE",jsonString);
            showDataActivityIntent.putExtra("DOB",dateOfBirth);
            showDataActivityIntent.putExtra("DOE",dateofExpiry);
            showDataActivityIntent.putExtra("DOCNUM",documentNumber);
            showDataActivityIntent.putExtra("DOCTYPE",documentType);
            showDataActivityIntent.putExtra("USERNAME",assureIDUsername);
            showDataActivityIntent.putExtra("PASSWORD",assureIDPassword);
            this.startActivity(showDataActivityIntent);
            //acuantAndroidMobileSdkControllerInstance.deleteInstanceConnectServices(this,instanceID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void processImageConnectServiceFailed(int code, String message) {
        Log.d(TAG,message);
        if(progressDialog!=null && progressDialog.isShowing()){
            Util.dismissDialog(progressDialog);
        }
    }

    @Override
    public void deleteImageConnectServiceCompleted(String instanceID) {
        Log.d(TAG,instanceID);
        if(progressDialog!=null && progressDialog.isShowing()){
            Util.dismissDialog(progressDialog);
        }
    }

    @Override
    public void deleteImageConnectServiceFailed(int code, String message) {
        Log.d(TAG,message);
        if(progressDialog!=null && progressDialog.isShowing()){
            Util.dismissDialog(progressDialog);
        }
    }

}