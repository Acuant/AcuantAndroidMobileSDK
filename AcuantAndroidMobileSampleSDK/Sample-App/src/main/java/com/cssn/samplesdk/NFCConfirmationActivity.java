package com.cssn.samplesdk;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.acuant.mobilesdk.AcuantAndroidMobileSDKController;
import com.acuant.mobilesdk.AcuantNFCCardDetails;
import com.acuant.mobilesdk.AcuantTagReadingListener;
import com.acuant.mobilesdk.PassportCard;
import com.acuant.mobilesdk.util.Utils;
import com.cssn.samplesdk.util.DataContext;
import com.cssn.samplesdk.util.NFCStore;
import com.cssn.samplesdk.util.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class NFCConfirmationActivity extends Activity implements AcuantTagReadingListener {

    private static final String TAG = NFCConfirmationActivity.class.getName();


    Button nfcScanningBtn;

    NfcAdapter nfcAdapter;

    String documentNumber= null;
    String dob = null;
    String doe = null;

    private EditText mrzDocNumber = null;
    private TextView mrzDOB = null;
    private TextView mrzDOE = null;


    private static AlertDialog alertDialog;

    public void DateDialog(final TextView tv, int year, int month, int day){
        DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
            {

                tv.setText(Util.getInMMddyyFormat(year,(monthOfYear),dayOfMonth));

            }};

        DatePickerDialog dpDialog=new DatePickerDialog(this, listener, year, month, day);
        dpDialog.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nfcconfirmation);

        nfcScanningBtn = (Button) findViewById(R.id.buttonNFC);

        documentNumber = getIntent().getStringExtra("DOCNUMBER");
        dob = getIntent().getStringExtra("DOB");
        doe = getIntent().getStringExtra("DOE");

        mrzDocNumber = (EditText) findViewById(R.id.mrzDocumentNumber);
        mrzDOB = (TextView) findViewById(R.id.mrzDOB);
        mrzDOE = (TextView) findViewById(R.id.mrzDOE);

        mrzDocNumber.setText(documentNumber);
        mrzDOB.setText(dob);
        mrzDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                if(mrzDOB.getText()!=null) {
                    String[] dateComps = mrzDOB.getText().toString().split("-");
                    if(dateComps.length==3) {
                        day = Integer.parseInt(dateComps[1]);
                        month = Integer.parseInt(dateComps[0])-1;
                        year = Integer.parseInt(dateComps[2]);
                    }
                }
                if(year<20){
                    year = 2000+year;
                }else {
                    year = Util.get4DigitYear(year, month, day);
                }
                DateDialog(mrzDOB,year,month,day);
            }
        });

        mrzDOE.setText(doe);
        mrzDOE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                if(mrzDOE.getText()!=null) {
                    String[] dateComps = mrzDOE.getText().toString().split("-");
                    if(dateComps.length==3) {
                        day = Integer.parseInt(dateComps[1]);
                        month = Integer.parseInt(dateComps[0])-1;
                        year = Integer.parseInt(dateComps[2]);
                    }
                }
                if(year<50){
                    year = 2000+year;
                }else {
                    year = Util.get4DigitYear(year, month, day);
                }
                DateDialog(mrzDOE,year,month,day);
            }
        });

        if(nfcAdapter==null){
            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.nfcAdapter != null) {
            this.nfcAdapter.disableForegroundDispatch(this);
        }
    }

    public void nfcPressed(View v){
       if(nfcAdapter!=null) {
            ensureSensorIsOn();
            AcuantAndroidMobileSDKController acuantAndroidMobileSdkControllerInstance = AcuantAndroidMobileSDKController.getInstance();
            if (acuantAndroidMobileSdkControllerInstance != null) {
                acuantAndroidMobileSdkControllerInstance.listenNFC(this, nfcAdapter);
                if (alertDialog != null && alertDialog.isShowing()) {
                    Util.dismissDialog(alertDialog);
                }
                alertDialog = Util.showDialog(this, "Searching for passport chip...\n\nTap and place the phone on top of passport chip.");
                alertDialog.setCancelable(false);
            }
        }else{
            new AlertDialog.Builder(this)
                    .setTitle("NFC error!")
                    .setMessage("NFC is not available for this device")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            dialogInterface.dismiss();
                        }
                    })
                    .show();
        }
    }

    private void ensureSensorIsOn()
    {
        if(this.nfcAdapter!=null && !this.nfcAdapter.isEnabled())
        {
            // Alert the user that NFC is off
            new AlertDialog.Builder(this)
                    .setTitle("NFC Sensor Turned Off")
                    .setMessage("In order to use this application, the NFC sensor must be turned on. Do you wish to turn it on?")
                    .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            // Send the user to the settings page and hope they turn it on
                            if (android.os.Build.VERSION.SDK_INT >= 16)
                            {
                                startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                            }
                            else
                            {
                                startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                            }
                        }
                    })
                    .setNegativeButton("Do Nothing", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            // Do nothing
                        }
                    })
                    .show();
        }else if(this.nfcAdapter==null){


        }

    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if(alertDialog!=null && alertDialog.isShowing()){
            Util.dismissDialog(alertDialog);
        }
        alertDialog = Util.showProgessDialog(this, "Reading passport chip...\n\nPlease don't move passport or phone.");
        AcuantAndroidMobileSDKController acuantAndroidMobileSdkControllerInstance = AcuantAndroidMobileSDKController.getInstance();
        if(acuantAndroidMobileSdkControllerInstance!=null){
            acuantAndroidMobileSdkControllerInstance.setAcuantTagReadingListener(this);
            String docNumber = mrzDocNumber.getText().toString().trim();
            String dateOfBirth = getFromattedStringFromDateString(mrzDOB.getText().toString().trim());
            String dateOfExpiry = getFromattedStringFromDateString(mrzDOE.getText().toString().trim());
            if(docNumber != null && !docNumber.trim().equals("") && dateOfBirth!=null && dateOfBirth.length()==6 && dateOfExpiry!=null && dateOfExpiry.length()==6) {
                acuantAndroidMobileSdkControllerInstance.readNFCTag(intent, docNumber, dateOfBirth, dateOfExpiry);
            }
        }

    }

    @Override
    public void tagReadSucceeded(AcuantNFCCardDetails cardDetails, Bitmap image, Bitmap sign_image) {
        Log.d(TAG,"NFC Tag successfully read");
        if(alertDialog!=null && alertDialog.isShowing()){
            Util.dismissDialog(alertDialog);
        }
        Intent intent = new Intent(getBaseContext(), NFCResultActivity.class);
        NFCStore.image = image;
        NFCStore.signature_image=sign_image;
        NFCStore.cardDetails = cardDetails;
        startActivity(intent);
        if (this.nfcAdapter != null)
        {
            this.nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    public void tagReadFailed(String message) {
        Log.d(TAG,message);
        if(alertDialog!=null && alertDialog.isShowing()){
            Util.dismissDialog(alertDialog);
        }
        alertDialog = Util.showDialog(this, message);
        if (this.nfcAdapter != null)
        {
            try {
                this.nfcAdapter.disableForegroundDispatch(this);
            }catch(Exception e){

            }
        }

    }


    public static String getFromattedStringFromDateString(String dateString){
        String retString = null;
        if(dateString!=null && !dateString.trim().equals("")) {
            String[] dateComps = dateString.split("-");
            if(dateComps.length==3) {
                int year = Integer.parseInt(dateComps[2]);
                int day = Integer.parseInt(dateComps[1]);
                int month = Integer.parseInt(dateComps[0])-1;
                Date date = new Date(year, month, day);
                SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd");
                retString = sdf.format(date);
            }
        }
        return retString;
    }

}
