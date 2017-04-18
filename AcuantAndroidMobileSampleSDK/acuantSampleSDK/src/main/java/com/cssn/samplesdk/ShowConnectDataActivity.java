package com.cssn.samplesdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

public class ShowConnectDataActivity extends Activity {

    private String data = null;
    private String frontImageURL = null;
    private String backImageURL = null;
    private String signatureImageURL = null;
    private String faceImageURL = null;
    private String username=null;
    private String password=null;
    private String dateOfBirth="";
    private String dateOfExpiry="";
    private String documentNumber="";
    private String documentType="";

    private ImageView faceImageView = null;
    private ImageView frontImageView = null;
    private ImageView backImageView = null;
    private ImageView signatureImageView = null;
    private TextView textView = null;
    Button nfcScanningBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_connect_data);

        data = getIntent().getStringExtra("RESPONSE");
        frontImageURL = getIntent().getStringExtra("FRONTIMAGEURL");
        backImageURL = getIntent().getStringExtra("BACKIMAGEURL");
        signatureImageURL = getIntent().getStringExtra("SIGNATUREIMAGEURL");
        faceImageURL = getIntent().getStringExtra("FACEIMAGEURL");
        username = getIntent().getStringExtra("USERNAME");
        password = getIntent().getStringExtra("PASSWORD");
        dateOfBirth = getIntent().getStringExtra("DOB");
        dateOfExpiry= getIntent().getStringExtra("DOE");
        documentNumber =getIntent().getStringExtra("DOCNUM");
        documentType = getIntent().getStringExtra("DOCTYPE");


        faceImageView = (ImageView) findViewById(R.id.connectFaceImage);
        frontImageView = (ImageView) findViewById(R.id.connectFrontSideCardImage);
        backImageView = (ImageView) findViewById(R.id.connectBackSideCardImage);
        signatureImageView = (ImageView) findViewById(R.id.connectSignatureImage);
        textView = (TextView) findViewById(R.id.connectTextViewLicenseCardInfo);
        nfcScanningBtn = (Button) findViewById(R.id.connectButtonNFC);

        if (documentType.equalsIgnoreCase("Passport")){
            nfcScanningBtn.setVisibility(View.VISIBLE);
        }else{
            nfcScanningBtn.setVisibility(View.GONE);
        }

        if(data!=null) {
            textView.setText(data);
        }

        displayImage(frontImageURL,frontImageView);
        displayImage(backImageURL,backImageView);
        displayImage(signatureImageURL,signatureImageView);
        displayImage(faceImageURL,faceImageView);
    }

    public void displayImage(final String stringURL,final ImageView imageView)
    {
        if(username!=null && password!=null && stringURL!=null && !stringURL.trim().equalsIgnoreCase("") && imageView!=null) {
            try {
                Thread thread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try  {
                            Authenticator.setDefault(new Authenticator(){
                                protected PasswordAuthentication getPasswordAuthentication() {
                                    return new PasswordAuthentication(username,password.toCharArray());
                                }});
                            HttpURLConnection c = (HttpURLConnection) new URL(stringURL).openConnection();
                            c.setUseCaches(false);
                            c.connect();
                            InputStream is = c.getInputStream();
                            final Bitmap bmImg = BitmapFactory.decodeStream(is);
                            if(bmImg!=null){
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setImageBitmap(bmImg);

                                    }
                                });
                            }else{
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        imageView.setVisibility(View.GONE);
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            imageView.setVisibility(View.GONE);
        }
    }

    public void nfcPressed(View v){
        Intent confirmNFCDataActivity = new Intent(this, NFCConfirmationActivity.class);
        confirmNFCDataActivity.putExtra("DOB",dateOfBirth);
        confirmNFCDataActivity.putExtra("DOE",dateOfExpiry);
        confirmNFCDataActivity.putExtra("DOCNUMBER",documentNumber);
        this.startActivity(confirmNFCDataActivity);
    }

}
