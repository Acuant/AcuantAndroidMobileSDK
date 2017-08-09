/*
 *
 */
package com.cssn.samplesdk;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acuant.mobilesdk.AcuantAndroidMobileSDKController;
import com.acuant.mobilesdk.AcuantErrorListener;
import com.acuant.mobilesdk.Card;
import com.acuant.mobilesdk.CardCroppingListener;
import com.acuant.mobilesdk.CardType;
import com.acuant.mobilesdk.ConnectWebserviceListener;
import com.acuant.mobilesdk.FacialData;
import com.acuant.mobilesdk.LicenseActivationDetails;
import com.acuant.mobilesdk.LicenseDetails;
import com.acuant.mobilesdk.ProcessImageRequestOptions;
import com.acuant.mobilesdk.WebServiceListener;
import com.acuant.mobilesdk.util.Utils;
import com.cssn.samplesdk.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 */

public class MainActivity extends Activity implements WebServiceListener, ConnectWebserviceListener, CardCroppingListener, AcuantErrorListener{

    private static final String TAG = MainActivity.class.getName();
    public ImageView frontImageView;
    public ImageView backImageView;
    public Button processCardButton;
    AcuantAndroidMobileSDKController assureId_Instance = null;
    private TextView txtTapToCaptureFront;
    private TextView txtTapToCaptureBack;
    private RelativeLayout layoutFrontImage;
    private RelativeLayout layoutBackImage;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private boolean isShowErrorAlertDialog;
    private MainActivity mainActivity;

    //set credentials
    private String assureIDUsername = "xxxxxxxxxxx";
    private String assureIDPassword = "xxxxxxxxxxx";
    private String assureIDSubscription = "xxxxxxxxxxx";
    private String assureIDURL = "https://devconnect.assureid.net/AssureIDService";


    private boolean wasLicenseValided = false;
    private int cardType = CardType.DRIVERS_LICENSE;

    private int cardSide = 0; //0 front, 1 back

    private Bitmap faceImage = null;


    // Variables for web service results
    String front_image_Loc = "";
    String back_image_Loc = "";
    String signature_image_loc = "";
    String face_image_loc = "";
    String dateOfBirth = "";
    String dateofExpiry = "";
    String documentNumber = "";
    String documentType = "";
    String cardDatajsonString="";


    private void resetAllData(){
        front_image_Loc = "";
        back_image_Loc = "";
        signature_image_loc = "";
        face_image_loc = "";
        dateOfBirth = "";
        dateofExpiry = "";
        documentNumber = "";
        documentType = "";
        cardDatajsonString="";
    }

    private void resetUI(){
        frontImageView.setImageBitmap(null);
        backImageView.setImageBitmap(null);
        hideProcessButton();
        hideBackImageText();
        hideFrontImageText();
        hideFrontSideCardImage();
        hideBackSideCardImage();
    }

    private AcuantAndroidMobileSDKController getAssureIDInstance(){
        assureId_Instance = AcuantAndroidMobileSDKController.getInstance(this, assureIDUsername, assureIDPassword, assureIDSubscription, assureIDURL);
        assureId_Instance.setConnectWebServiceListener(this);
        assureId_Instance.setWebServiceListener(this);
        assureId_Instance.setCardCroppingListener(this);
        assureId_Instance.setAcuantErrorListener(this);
        return assureId_Instance;
    }

    private void showResult(String face_image_loc,String signature_image_loc,String front_image_Loc,String back_image_Loc,
                            String jsonString,String dateOfBirth,String dateofExpiry,String documentNumber,String documentType,FacialData fd){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog!=null && progressDialog.isShowing()){
                    Util.dismissDialog(progressDialog);
                }
            }
        });
        Intent showDataActivityIntent = new Intent(this, ShowConnectDataActivity.class);
        showDataActivityIntent.putExtra("FACEIMAGEURL", face_image_loc);
        showDataActivityIntent.putExtra("SIGNATUREIMAGEURL", signature_image_loc);
        showDataActivityIntent.putExtra("FRONTIMAGEURL", front_image_Loc);
        showDataActivityIntent.putExtra("BACKIMAGEURL", back_image_Loc);
        showDataActivityIntent.putExtra("RESPONSE", jsonString);
        showDataActivityIntent.putExtra("DOB", dateOfBirth);
        showDataActivityIntent.putExtra("DOE", dateofExpiry);
        showDataActivityIntent.putExtra("DOCNUM", documentNumber);
        showDataActivityIntent.putExtra("DOCTYPE", documentType);
        showDataActivityIntent.putExtra("USERNAME", assureIDUsername);
        showDataActivityIntent.putExtra("PASSWORD", assureIDPassword);
        if(fd!=null) {
            showDataActivityIntent.putExtra("FACIALMATCHCONFIDENCERATING", fd.facialMatchConfidenceRating);
            showDataActivityIntent.putExtra("FACIALMATCH", fd.facialMatch);
            showDataActivityIntent.putExtra("LIVELINESS", fd.faceLivelinessDetection);
        }else{
            showDataActivityIntent.putExtra("FACECROPFAILED",true);
        }
        this.startActivityForResult(showDataActivityIntent,100);
    }


    /**
     *
     */
    private void showCameraInterface() {
        alertDialog = new AlertDialog.Builder(this).create();
        assureId_Instance.setInitialMessageDescriptor(R.layout.align_and_tap);
        assureId_Instance.setFinalMessageDescriptor(R.layout.hold_steady);
        assureId_Instance.showManualCameraInterface(this, 0, 0, true);
    }

    /**
     * Called by the process Button
     *
     * @param v
     */
    public void processCard(View v) {
        progressDialog = Util.showProgessDialog(mainActivity,"Capturing data...");
        ProcessImageRequestOptions options = ProcessImageRequestOptions.getInstance();
        options.acuantCardType = cardType;
        BitmapDrawable frontdrawable = (BitmapDrawable) frontImageView.getDrawable();
        Bitmap frontImage = frontdrawable.getBitmap();
        BitmapDrawable backdrawable = (BitmapDrawable) backImageView.getDrawable();
        Bitmap backImage = null;
        if(backdrawable!=null) {
            backImage = backdrawable.getBitmap();
        }
        assureId_Instance.setCloudUrl(assureIDURL);
        assureId_Instance.callProcessImageConnectServices(frontImage, backImage, "", this, options);
    }



    private void initializeSDK() {
        // load the controller instance
        Util.lockScreen(this);
        if(wasLicenseValided==false) {
            progressDialog = Util.showProgessDialog(mainActivity,"Initializing...");
            assureId_Instance = getAssureIDInstance();

        }

    }

    // Android lifecycle methods

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
        } else {
            if (Util.LOG_ENABLED) {
                Utils.appendLog(TAG, "if (savedInstanceState != null)");
            }
        }
        setContentView(R.layout.activity_main);

        layoutBackImage = (RelativeLayout) findViewById(R.id.relativeLayoutBackImage);
        layoutFrontImage = (RelativeLayout) findViewById(R.id.relativeLayoutFrontImage);

        frontImageView = (ImageView) findViewById(R.id.frontImageView);
        backImageView = (ImageView) findViewById(R.id.backImageView);

        txtTapToCaptureFront = (TextView) findViewById(R.id.txtTapToCaptureFront);
        txtTapToCaptureBack = (TextView) findViewById(R.id.txtTapToCaptureBack);


        processCardButton = (Button) findViewById(R.id.processCardButton);
        processCardButton.setVisibility(View.INVISIBLE);

        mainActivity = this;

    }


    @Override
    protected void onResume() {
        super.onResume();
        initializeSDK();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(progressDialog!=null && progressDialog.isShowing()){
            Util.dismissDialog(progressDialog);
        }
        resetUI();
        resetAllData();
        super.onActivityResult(requestCode, resultCode, data);

    }
    /**
     *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(assureId_Instance!=null) {
            assureId_Instance.cleanup();
        }
        if (Util.LOG_ENABLED) {
            Utils.appendLog(TAG, "protected void onDestroy()");
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

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

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    // User Actions

    public void captureButtonPressed(View v){
        showCameraInterface();
        hideProcessButton();
        cardSide = 0;
    }

    public void backSideCapturePressed(View v){
        showCameraInterface();
        hideProcessButton();
        cardSide = 1;
    }

    public void frontSideCapturePressed(View v){
        showCameraInterface();
        hideProcessButton();
        cardSide=0;
    }

    // Image Cropping callbacks
    @Override
    public void onCardImageCaptured() {

    }

    @Override
    public void onCardCroppingStart(Activity activity) {
        if (Utils.LOG_ENABLED) {
            Utils.appendLog(TAG, "public void onCardCroppingStart(Activity activity)");
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            Util.dismissDialog(progressDialog);
        }
        Util.lockScreen(this);
        progressDialog = Util.showProgessDialog(activity, "Cropping image...");
    }

    /**
     * Result from the CSSNMobileSDKController.showCameraInterface method when
     * popover == true
     */
    @Override
    public void onCardCroppingFinish(final Bitmap bitmap, int detectedCardType) {
        //SaveImage(bitmap);
        cardType = detectedCardType;
        if(detectedCardType==CardType.PASSPORT){
            hideBackSideCardImage();
            hideFrontImageText();
            hideBackImageText();
            showFrontSideCardImage();
            if(bitmap!=null) {
                frontImageView.setImageBitmap(bitmap);
                showProcessButton();
            }
        }
    }

    /**
     * Result from the CSSNMobileSDKController.showCameraInterface method when
     * popover == true
     */
    @Override
    public void onCardCroppingFinish(final Bitmap bitmap, final boolean scanBackSide, int detectedCardType) {
        //SaveImage(bitmap);
        cardType = detectedCardType;
        if(detectedCardType==CardType.DRIVERS_LICENSE){
            if(cardSide==0) {
                hideFrontImageText();
                hideBackImageText();
                showFrontSideCardImage();
                showBackSideCardImage();
                if (bitmap != null) {
                    frontImageView.setImageBitmap(bitmap);
                }
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setMessage("Please scan the back side of the drivers license.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                cardSide = 1;
                                dialog.dismiss();
                                showCameraInterface();
                            }
                        }).show();
            }else{
                if (bitmap != null) {
                    backImageView.setImageBitmap(bitmap);
                    showProcessButton();
                }
            }
        }
    }


    //license validation callback
    @Override
    public void validateLicenseKeyCompleted(LicenseDetails details) {
        assureId_Instance.setCaptureOriginalCapture(true);
        wasLicenseValided= true;
        Util.dismissDialog(progressDialog);
        Util.unLockScreen(MainActivity.this);
    }

    // Following callback methods need not to have any action.
    @Override
    public void onPDF417Finish(String result) {

    }

    @Override
    public void onOriginalCapture(Bitmap bitmap) {
       //SaveImage(bitmap);
    }

    @Override
    public void onCancelCapture(Bitmap croppedImage, Bitmap originalImage) {

    }

    @Override
    public void onBarcodeTimeOut(Bitmap bitmap, Bitmap bitmap1) {

    }

    /**
     *
     */
    @Override
    public void processImageServiceCompleted(final Card card) {

    }

    @Override
    public void activateLicenseKeyCompleted(LicenseActivationDetails licenseActivationDetails) {

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

    private void showProcessButton(){
        processCardButton.setClickable(true);
        processCardButton.setVisibility(View.VISIBLE);
    }

    private void hideProcessButton(){
        processCardButton.setClickable(false);
        processCardButton.setVisibility(View.GONE);
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


    // SDK Error Callback
    @Override
    public void didFailWithError(int code, String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog!=null && progressDialog.isShowing()){
                    Util.dismissDialog(progressDialog);
                }
            }
        });
        Utils.showDialog(mainActivity,message);
    }


    //AssureID Web Services Callbacks

    @Override
    public void processImageConnectServiceCompleted(final String jsonString) {
        cardDatajsonString = jsonString;
         try {
            JSONObject jsonResponse = new JSONObject(jsonString);
            JSONArray images = jsonResponse.getJSONArray("Images");
            for (int i = 0; i < images.length(); i++) {
                JSONObject imageDict = images.getJSONObject(i);
                if (imageDict.has("Side")) {
                    int side = imageDict.getInt("Side");
                    if (side == 0 && imageDict.has("Uri")) {
                        front_image_Loc = imageDict.getString("Uri");
                    } else if (side == 1 && imageDict.has("Uri")) {
                        back_image_Loc = imageDict.getString("Uri");
                    }
                }
            }

            JSONArray fields = jsonResponse.getJSONArray("Fields");
            for (int i = 0; i < fields.length(); i++) {
                JSONObject dict = fields.getJSONObject(i);
                if (dict.has("IsImage")) {
                    boolean IsImage = dict.getBoolean("IsImage");
                    if (IsImage && dict.has("Value")) {
                        if (dict.has("Key")) {
                            if (dict.getString("Key").equalsIgnoreCase("Signature")) {
                                signature_image_loc = dict.getString("Value");
                            } else if (dict.getString("Key").equalsIgnoreCase("Photo")) {
                                face_image_loc = dict.getString("Value");
                            }
                        }
                    }
                }
                if (dict.has("Key") && dict.has("Type") && dict.getString("Type").equalsIgnoreCase("datetime") &&
                        dict.getString("Key").equalsIgnoreCase("Birth Date") || (dict.getString("Key").equalsIgnoreCase("Expiration Date"))) {
                    String dateStr = dict.getString("Value");
                    if (dateStr != null && !dateStr.trim().equalsIgnoreCase("")) {
                        dateStr = dateStr.replace("Date", "");
                        dateStr = dateStr.replace(")", "");
                        dateStr = dateStr.replace("(", "");
                        dateStr = dateStr.replace("/", "");
                        if (dateStr != null) {
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
                if (dict.has("Key") && dict.has("Type") && dict.getString("Type").equalsIgnoreCase("string") &&
                        dict.getString("Key").equalsIgnoreCase("Document Number")) {
                    documentNumber = dict.getString("Value");
                }

                if (dict.has("Key") && dict.has("Type") && dict.getString("Type").equalsIgnoreCase("string") &&
                        dict.getString("Key").equalsIgnoreCase("Document Class Name")) {
                    documentType = dict.getString("Value");
                }
            }
            //String instanceID = jsonResponse.getString("InstanceId");
             runOnUiThread(new Runnable() {
                 @Override
                 public void run() {
                     if(progressDialog!=null && progressDialog.isShowing()) {
                         Util.dismissDialog(progressDialog);
                     }
                     showResult(face_image_loc,signature_image_loc,front_image_Loc,back_image_Loc,jsonString,dateOfBirth,dateofExpiry,documentNumber,documentType,null);
                 }
             });
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void processImageConnectServiceFailed(int code, String message) {
        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog!=null && progressDialog.isShowing()){
                    Util.dismissDialog(progressDialog);
                }
            }
        });
    }

    @Override
    public void deleteImageConnectServiceCompleted(String instanceID) {
        Log.d(TAG, instanceID);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog!=null && progressDialog.isShowing()){
                    Util.dismissDialog(progressDialog);
                }
            }
        });
    }

    @Override
    public void deleteImageConnectServiceFailed(int code, String message) {
        Log.d(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(progressDialog!=null && progressDialog.isShowing()){
                    Util.dismissDialog(progressDialog);
                }
            }
        });
    }
}