package com.cssn.samplesdk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

class AssureIDResult{
    public static final int Unknown = 0;
    public static final int Passed = 1;
    public static final int Failed = 2;
    public static final int Skipped = 3;
    public static final int Caution = 4;
    public static final int Attention = 5;
}

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
    private TextView parsedTextView = null;
    private TextView facialTextView = null;
    Button nfcScanningBtn;

    private boolean failedCroppingFaceImage = false;

    private boolean isFacialMatch = false;
    private String facialMatchConfidenceRating = "0";
    private boolean livelinessDetected = false;

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
        failedCroppingFaceImage = getIntent().getBooleanExtra("FACECROPFAILED",false);

        facialMatchConfidenceRating= getIntent().getStringExtra("FACIALMATCHCONFIDENCERATING");
        isFacialMatch = getIntent().getBooleanExtra("FACIALMATCH", false);
        livelinessDetected = getIntent().getBooleanExtra("LIVELINESS", false);


        faceImageView = (ImageView) findViewById(R.id.connectFaceImage);
        frontImageView = (ImageView) findViewById(R.id.connectFrontSideCardImage);
        backImageView = (ImageView) findViewById(R.id.connectBackSideCardImage);
        signatureImageView = (ImageView) findViewById(R.id.connectSignatureImage);
        facialTextView = (TextView)findViewById(R.id.facialDataInfo);
        nfcScanningBtn = (Button) findViewById(R.id.connectButtonNFC);

        parsedTextView = (TextView) findViewById(R.id.connectTextViewParsedLicenseCardInfo);



        /*if (documentType.equalsIgnoreCase("Passport")){
            nfcScanningBtn.setVisibility(View.VISIBLE);
        }else{
            nfcScanningBtn.setVisibility(View.GONE);
        }*/

        if(!failedCroppingFaceImage){
            String facialStr = "Facial Match :"+isFacialMatch+"\nFacial Match Confident Rating :"+facialMatchConfidenceRating+"\nLiveliness Detected :"+livelinessDetected;
            facialTextView.setText(facialStr);
        }else{
            facialTextView.setText("Could not crop face image from the ID.");
        }

        String parsedData = getParsedString(data);

        if(parsedTextView!=null){
            parsedTextView.setText(parsedData);
        }


        displayImage(frontImageURL,frontImageView);
        displayImage(backImageURL,backImageView);
        displayImage(signatureImageURL,signatureImageView);
        displayImage(faceImageURL,faceImageView);
    }

    private String getParsedString(String data) {
        String retStr = "";
        try {
            final JSONObject obj = new JSONObject(data);

            JSONArray Alerts = obj.getJSONArray("Alerts");
            int Result = obj.getInt("Result");
            JSONArray Fields = obj.getJSONArray("Fields");

            if(Result==AssureIDResult.Passed || Result==AssureIDResult.Failed ){
                if(Result==AssureIDResult.Passed) {
                    retStr = "Authentication Result : Passed" + "\n";
                }else if(Result==AssureIDResult.Failed) {
                    retStr = "Authentication Result : Failed" + "\n";
                }
            }else{
                if(Alerts.length()>0){
                    for(int i=0;i<Alerts.length();i++){
                        JSONObject alertObject = (JSONObject) Alerts.get(i);
                        if(alertObject.getInt("Result")==Result) {
                            retStr = retStr + "Alert : " + alertObject.getString("Key") + "\n";
                        }
                    }
                }
            }

            for(int i=0;i<Fields.length();i++){
                JSONObject info = (JSONObject)Fields.get(i);
                String key = info.getString("Name");
                String value = info.getString("Value");
                if(!key.equalsIgnoreCase("Face Image") && !key.equalsIgnoreCase("Signature") && !key.equalsIgnoreCase("Photo")){
                    retStr = retStr + key + " : " + value + "\n";
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return retStr;
    }


    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }

    

    public synchronized void displayImage(final String stringURL,final ImageView imageView)
    {
        Log.d("Iamge","Front :"+stringURL);
        synchronized (this) {
            if (username != null && password != null && stringURL != null && !stringURL.trim().equalsIgnoreCase("") && imageView != null) {
                try {
                    Thread thread = new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                Authenticator.setDefault(new Authenticator() {
                                    protected PasswordAuthentication getPasswordAuthentication() {
                                        return new PasswordAuthentication(username, password.toCharArray());
                                    }
                                });
                                HttpURLConnection c = (HttpURLConnection) new URL(stringURL).openConnection();
                                c.setUseCaches(false);
                                c.connect();
                                final Bitmap bmImg = BitmapFactory.decodeStream(c.getInputStream());
                                if (bmImg != null) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            imageView.setImageBitmap(bmImg);
                                        }
                                    });
                                } else {
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
            } else {
                imageView.setVisibility(View.GONE);
            }
        }
    }


}
