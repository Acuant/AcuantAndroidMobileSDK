package com.cssn.samplesdk.util;

import android.graphics.Bitmap;

import com.acuant.mobilesdk.CardType;

/**
 * Created by tapasbehera on 5/11/16.
 */
public class TempImageStore {
    private static Bitmap bitmapImage;
    private static ConfirmationListener imageConfirmationListener;
    private static boolean croppingPassed = false;
    private static int cardType;

    public static int getCardType() {
        return cardType;
    }

    public static void setCardType(int cardType) {
        TempImageStore.cardType = cardType;
    }

    public static boolean isCroppingPassed() {
        return croppingPassed;
    }

    public static void setCroppingPassed(boolean croppingPassed) {
        TempImageStore.croppingPassed = croppingPassed;
    }

    public static ConfirmationListener getImageConfirmationListener() {
        return imageConfirmationListener;
    }

    public static void setImageConfirmationListener(ConfirmationListener imageConfirmationListener) {
        TempImageStore.imageConfirmationListener = imageConfirmationListener;
    }

    public static Bitmap getBitmapImage() {
        return bitmapImage;
    }
    public static void setBitmapImage(Bitmap bitmapImage) {
        TempImageStore.bitmapImage = bitmapImage;
    }

    public static void cleanup(){
        if(bitmapImage!=null){
            bitmapImage.recycle();
            bitmapImage=null;
        }
        TempImageStore.imageConfirmationListener = null;
    }
}
