package com.cssn.samplesdk;

/**
 * Created by tapasbehera on 5/9/16.
 */

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.acuant.mobilesdk.CardType;
import com.acuant.mobilesdk.util.Utils;
import com.cssn.samplesdk.model.MainActivityModel;
import com.cssn.samplesdk.util.TempImageStore;
import com.cssn.samplesdk.util.Util;

import java.util.HashMap;


/**
 * Created by tapasbehera on 5/9/16.
 */


public class ImageConformationActivity extends Activity {
    Bitmap image;
    ImageView cropImageViewer;
    TextView messageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.image_confirmation_landscape);
        }else {
            setContentView(R.layout.image_confirmation);
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        cropImageViewer = (ImageView) findViewById(R.id.cropImage);
        messageView = (TextView)findViewById(R.id.messageTextView);
        image = TempImageStore.getBitmapImage();
        if(TempImageStore.isCroppingPassed()) {
            cropImageViewer.setImageBitmap(image);

            ImageView titleImg = (ImageView)  findViewById(R.id.titleImg);
            titleImg.setVisibility(View.GONE);

        }else{
            Button confirmButton = (Button) findViewById(R.id.buttonConfirm);
            confirmButton.setVisibility(View.GONE);
            cropImageViewer.getLayoutParams().height = height*5/10;
            cropImageViewer.getLayoutParams().width = width*8/10;
            cropImageViewer.setImageResource(R.drawable.help_screen_tip_1);

            ImageView titleImg = (ImageView)  findViewById(R.id.titleImg);
            titleImg.setVisibility(View.VISIBLE);
            titleImg.getLayoutParams().width = width*8/10;
            titleImg.setImageResource(R.drawable.help_screen_tip_title_1);

        }
        messageView.setText(getMessage());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        image = null;
        cropImageViewer = null;
        messageView = null;
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();

    }

    public void clearImage(){
        if(image!=null){
            image.recycle();
            image = null;
        }
    }

    public void retryButtonPressed(View v) {
        clearImage();
        System.gc();
        System.runFinalization();
        if(TempImageStore.getImageConfirmationListener()!=null) {
            TempImageStore.getImageConfirmationListener().retry();
        }
        finish();
    }

    public void confirmButtonPressed(View v) {
        if(TempImageStore.getImageConfirmationListener()!=null) {
            TempImageStore.getImageConfirmationListener().confimed();
        }
        finish();
    }

    public String getMessage(){
        String retString = "Please make sure all the text on the ID image is readable,otherwise retry.";
        if(TempImageStore.isCroppingPassed()){
            if(TempImageStore.getCardType()== CardType.DRIVERS_LICENSE){
                retString = "Please make sure all the text on the ID image is readable,otherwise retry.";
            }else if(TempImageStore.getCardType()== CardType.PASSPORT){
                retString = "Please make sure all the text on the Passport image is readable,otherwise retry.";
            }else if(TempImageStore.getCardType()== CardType.MEDICAL_INSURANCE){
                retString = "Please make sure all the text on the Insurance Card image is readable,otherwise retry.";
            }
            boolean isSHarp = true;
            HashMap<String,Object> imageMetrics = TempImageStore.getImageMetrics();
            if(imageMetrics!=null && imageMetrics.get("IS_SHARP")!=null) {
                isSHarp = Boolean.parseBoolean(imageMetrics.get("IS_SHARP").toString());
                if(!isSHarp){
                    retString = "The image appears to be blurry. Please retry.";
                }
            }

            if(imageMetrics!=null && imageMetrics.get("HAS_GLARE")!=null) {
                boolean hasGlare = Boolean.parseBoolean(imageMetrics.get("HAS_GLARE").toString());
                if(hasGlare){
                    retString = "The image has glare.";
                }
                if(!isSHarp){
                    retString = "The image appears to be blurry.";
                }

                retString = retString + " Please retry.";
            }

    }else{
            if(TempImageStore.getCardType()== CardType.DRIVERS_LICENSE){
                retString = "Unable to detect ID, please retry.";
            }else if(TempImageStore.getCardType()== CardType.PASSPORT){
                retString = "Unable to detect Passport, please retry.";
            }else if(TempImageStore.getCardType()== CardType.MEDICAL_INSURANCE){
                retString = "Unable to detect Insurance Card, please retry.";
            }
        }
        return retString;
    }

    public static boolean isTabletDevice(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

}
