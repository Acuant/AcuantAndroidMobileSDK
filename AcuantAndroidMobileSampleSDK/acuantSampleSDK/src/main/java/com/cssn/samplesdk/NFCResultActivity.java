package com.cssn.samplesdk;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.acuant.mobilesdk.AcuantNFCCardDetails;
import com.acuant.mobilesdk.Card;
import com.acuant.mobilesdk.PassportCard;
import com.cssn.samplesdk.util.NFCStore;

import java.util.ArrayList;

public class NFCResultActivity extends Activity{

    RelativeLayout resultLayout = null;
    ImageView imageView = null;
    ImageView signImageView = null;
    int currentId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nfcresult);

        this.resultLayout = (RelativeLayout) findViewById(R.id.dataLayout);
        this.imageView = (ImageView)findViewById(R.id.photo);
        this.signImageView = (ImageView)findViewById(R.id.signaturePhoto);

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Intent intent = getIntent();
        if (intent != null) {
            AcuantNFCCardDetails cardDetails = NFCStore.cardDetails;
            if(cardDetails!=null){
                setData(cardDetails);
                if(NFCStore.acuantCard!=null){
                    setAcuantData(NFCStore.acuantCard);
                }
                Bitmap image = NFCStore.image;
                if(image!=null){
                    imageView.setImageBitmap(image);
                }
                if(NFCStore.signature_image!=null){
                    signImageView.setImageBitmap(NFCStore.signature_image);
                }

                currentId = signImageView.getId();
            }else{
                String message =  intent.getStringExtra("DATA");
                addField("Error",message);
            }


        }

    }



    private void setData(AcuantNFCCardDetails data){

        String key;
        String value;

        if(data.getLdsVersion()!=null){
            key = "LDS Version";
            value = data.getLdsVersion();
            addField(key,value);
        }

        if(data.getPrimaryIdentifier()!=null){
            key = "Primary Identifier";
            value = data.getPrimaryIdentifier();
            addField(key,value);
        }

        if(data.getSecondaryIdentifier()!=null){
            key = "Secondary Identifier";
            value = data.getSecondaryIdentifier();
            int i=value.indexOf("<");
            if(i>0){
                value = value.substring(0,i);
            }
            addField(key,value);
        }

        if(data.getGender()!=null){
            key = "Gender";
            value = data.getGender()+"";
            addField(key,value);
        }

        if(data.getDateOfBirth()!=null){
            key = "Date of Birth";
            value = data.getDateOfBirth();
            addField(key,value);
        }

        if(data.getNationality()!=null){
            key = "Nationality";
            value = data.getNationality();
            addField(key,value);
        }

        if(data.getDateOfExpiry()!=null){
            key = "Date of Expiry";
            value = data.getDateOfExpiry();
            addField(key,value);
        }

        if(data.getDocumentCode()!=null){
            key = "Document Code";
            value = data.getDocumentCode();
            addField(key,value);
        }

        key = "Document Type";
        value = data.getDocumentType()+"";
        addField(key,value);

        if(data.getIssuingState()!=null){
            key = "Issuing State";
            value = data.getIssuingState();
            addField(key,value);
        }

        if(data.getDocumentNumber()!=null){
            key = "Document Number";
            value = data.getDocumentNumber();
            addField(key,value);
        }

        if(data.getPersonalNumber()!=null){
            key = "Personal Number";
            value = data.getPersonalNumber();
            addField(key,value);
        }

        if(data.getOptionalData1()!=null){
            key = "OptionalData1";
            value = data.getOptionalData1();
            addField(key,value);
        }

        if(data.getOptionalData2()!=null){
            key = "OptionalData2";
            value = data.getOptionalData2();
            addField(key,value);
        }

        key = "Supported Authentications";
        value = data.supportedMethodsString();
        if(value!=null && !value.equals("")) {
            addField(key, value);
        }

        key = "Unsupported Authentications";
        value = data.notSupportedMethodsString();
        if(value!=null && !value.equals("")) {
            addField(key, value);
        }

        if(data.getDocSignerValidity()!=null){
            key = "Document Signer Validity";
            value = data.getDocSignerValidity();
            addField(key,value);
        }


        if(data.isBacSupported()) {
            key = "BAC Aunthentication";
            addBooleanField(key, data.isBacAunthenticated());
        }

        key = "Group Hash Aunthentication";
        addBooleanField(key, data.isAuthenticDataGroupHashes());

        key = "Document Signer";
        addBooleanField(key, data.isAuthenticDocSignature());

        if(data.isAaSupported()) {
            key = "Active Aunthentication";
            addBooleanField(key, data.isAaAunthenticated());
        }

        if(data.isCaSupported()) {
            key = "Chip Aunthentication";
            addBooleanField(key, data.isCaAunthenticated());
        }

        if(data.isTaSupported()) {
            key = "Terminal Aunthentication";
            addBooleanField(key, data.isTaAunthenticated());
        }

    }

    private void setAcuantData(Card acuantCard) {
        if(acuantCard!=null) {
            PassportCard passportCard = (PassportCard) acuantCard;

            String authResult = passportCard.getAuthenticationResult();
            ArrayList<String> authSummary = null;
            if(authResult!=null && authResult.equalsIgnoreCase("passed")){
                addBooleanField("Assure ID Authentication  ",true);
            }else if(authResult!=null && authResult.equalsIgnoreCase("failed")){
                addBooleanField("Assure ID Authentication  ",false);
            }else if(authResult!=null){
                authSummary = passportCard.getAuthenticationResultSummaryList();
                String summary = "";
                for(int i=0;i<authSummary.size();i++){
                    if(i==0) {
                        summary = authSummary.get(i);
                    }else{
                        summary = summary +","+ authSummary.get(i);
                    }
                }
                addField("Assure ID "+authResult,summary);
            }
        }
    }

    private void addField(String key,String value){

        RelativeLayout field = new RelativeLayout(this);
        RelativeLayout.LayoutParams fieldparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        fieldparams.addRule(RelativeLayout.BELOW,currentId);
        field.setLayoutParams(fieldparams);

        ShapeDrawable rectShapeDrawable = new ShapeDrawable(); // pre defined class

        // get paint
        Paint paint = rectShapeDrawable.getPaint();

        // set border color, stroke and stroke width
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2); // you can change the value of 5
        field.setBackgroundDrawable(rectShapeDrawable);

        RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lparams.setMarginStart(20);

        TextView tvKey=new TextView(this);
        tvKey.setGravity(Gravity.CENTER_VERTICAL);
        tvKey.setLayoutParams(lparams);
        tvKey.setText(key);
        field.addView(tvKey);
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();

        RelativeLayout.LayoutParams rparams = new RelativeLayout.LayoutParams(width/2, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rparams.setMarginEnd(10);
        TextView valueKey=new TextView(this);
        valueKey.setLayoutParams(rparams);
        valueKey.setText(value);
        valueKey.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
        valueKey.setGravity(Gravity.CENTER_VERTICAL);
        field.addView(valueKey);

        resultLayout.addView(field);
        currentId = View.generateViewId();
        field.setId(currentId);
    }

    private void addBooleanField(String key,boolean value){

        RelativeLayout field = new RelativeLayout(this);
        RelativeLayout.LayoutParams fieldparams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        fieldparams.addRule(RelativeLayout.BELOW,currentId);
        field.setLayoutParams(fieldparams);

        ShapeDrawable rectShapeDrawable = new ShapeDrawable(); // pre defined class

        // get paint
        Paint paint = rectShapeDrawable.getPaint();

        // set border color, stroke and stroke width
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2); // you can change the value of 5
        field.setBackgroundDrawable(rectShapeDrawable);

        RelativeLayout.LayoutParams lparams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lparams.setMarginStart(20);

        TextView tvKey=new TextView(this);
        tvKey.setLayoutParams(lparams);
        tvKey.setGravity(Gravity.CENTER_VERTICAL);
        tvKey.setText(key);
        field.addView(tvKey);

        RelativeLayout.LayoutParams rparams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rparams.setMarginEnd(20);
        ImageView valueKey=new ImageView(this);
        valueKey.setLayoutParams(rparams);
        valueKey.getLayoutParams().height = 60;
        valueKey.getLayoutParams().width = 60;
        valueKey.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        if(value) {
            valueKey.setImageResource(R.drawable.greencheckmark);
        }else{
            valueKey.setImageResource(R.drawable.redcheckmark);
        }
        field.addView(valueKey);

        resultLayout.addView(field);
        currentId = View.generateViewId();
        field.setId(currentId);
    }


}

