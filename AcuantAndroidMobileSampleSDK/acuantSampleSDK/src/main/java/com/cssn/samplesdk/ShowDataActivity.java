/*
 *
 */
package com.cssn.samplesdk;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.acuant.mobilesdk.AcuantAndroidMobileSDKController;
import com.acuant.mobilesdk.CardType;
import com.acuant.mobilesdk.DriversLicenseCard;
import com.acuant.mobilesdk.FacialData;
import com.acuant.mobilesdk.LocationVerificationResult;
import com.acuant.mobilesdk.MedicalCard;
import com.acuant.mobilesdk.PassportCard;
import com.acuant.mobilesdk.Region;
import com.acuant.mobilesdk.util.Constants;
import com.acuant.mobilesdk.util.Utils;
import com.cssn.samplesdk.util.DataContext;
import com.cssn.samplesdk.util.Util;

import java.util.ArrayList;

/**
 *
 *
 */
public class ShowDataActivity extends Activity
{
    private static final String TAG = ShowDataActivity.class.getName();

    public Boolean isError = false;
    //private static AlertDialog alertDialog;
    private boolean isFacialFlow = false;
    ImageView imgFaceViewer;
    ImageView imgSignatureViewer;
    ImageView frontSideCardImageView;
    ImageView backSideCardImageView;

    TextView textViewCardInfo;
    Button nfcScanningBtn;

    int nFields = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            setContentView(R.layout.activity_show_data_landscape);
        }else {
            setContentView(R.layout.activity_show_data);
        }

        /*if (isTablet == false)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }*/


        frontSideCardImageView = (ImageView) findViewById(R.id.frontSideCardImage);
        backSideCardImageView = (ImageView) findViewById(R.id.backSideCardImage);
        imgFaceViewer = (ImageView) findViewById(R.id.faceImage);
        imgSignatureViewer = (ImageView) findViewById(R.id.signatureImage);
        textViewCardInfo = (TextView) findViewById(R.id.textViewLicenseCardInfo);
        nfcScanningBtn = (Button) findViewById(R.id.buttonNFC);

        isFacialFlow = getIntent().getBooleanExtra("FACIAL",false);

        if (DataContext.getInstance().getCardType() == CardType.PASSPORT){
            nfcScanningBtn.setVisibility(View.VISIBLE);
        }else{
            nfcScanningBtn.setVisibility(View.GONE);
        }
        loadResult();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }

    public void nfcPressed(View v){
        Intent confirmNFCDataActivity = new Intent(this, NFCConfirmationActivity.class);
        PassportCard processedPassportCard = DataContext.getInstance().getProcessedPassportCard();
        confirmNFCDataActivity.putExtra("DOB",processedPassportCard.getDateOfBirth());
        confirmNFCDataActivity.putExtra("DOE",processedPassportCard.getExpirationDate());
        confirmNFCDataActivity.putExtra("DOCNUMBER",processedPassportCard.getPassportNumber());
        this.startActivity(confirmNFCDataActivity);
    }

    private void loadResult()
    {
        if (DataContext.getInstance().getCardType() == CardType.DRIVERS_LICENSE)
        {
            if (DataContext.getInstance().getProcessedLicenseCard() != null)
            {
                setResultFields();
            } else
            {
                Toast.makeText(ShowDataActivity.this, Constants.ERROR_RESULT, Toast.LENGTH_LONG).show();
            }
        } else if (DataContext.getInstance().getCardType() == CardType.MEDICAL_INSURANCE)
        {
            if (DataContext.getInstance().getProcessedMedicalCard() != null)
            {
                setResultFields();
            } else
            {
                Toast.makeText(ShowDataActivity.this, Constants.ERROR_RESULT, Toast.LENGTH_LONG).show();
            }
        } else if (DataContext.getInstance().getCardType() == CardType.PASSPORT)
        {
            if (DataContext.getInstance().getProcessedPassportCard() != null)
            {
                setResultFields();
            } else
            {
                Toast.makeText(ShowDataActivity.this, Constants.ERROR_RESULT, Toast.LENGTH_LONG).show();
            }
        } else
        {
            Toast.makeText(ShowDataActivity.this, Constants.ERROR_RESULT, Toast.LENGTH_LONG).show();
        }

    }

    private void setResultFields()
    {
        try
        {
            backSideCardImageView.setVisibility(View.INVISIBLE);

            switch (DataContext.getInstance().getCardType())
            {
                case CardType.DRIVERS_LICENSE:

                    setResultsForDriversLicenseCard();
                    break;

                case CardType.MEDICAL_INSURANCE:

                    setResultsForMedicalCard();
                    break;

                case CardType.PASSPORT:

                    setResultsForPassportCard();
                    break;

                default:
                    Utils.appendLog(TAG,"Invalid card type. This method is bad implemented or DataContext.getInstance().getCardType() has an invalid card type.");
                    break;

            }

        } catch (Exception e)
        {
            Utils.appendLog(TAG, e.getMessage());
        }

    }

    /**
     *
     */
    private void setResultsForPassportCard()
    {
        PassportCard processedPassportCard = DataContext.getInstance().getProcessedPassportCard();
        FacialData processedFacialData = DataContext.getInstance().getProcessedFacialData();

        StringBuilder info = new StringBuilder();

        // First Name
        info.append(("First Name").concat(" - "))
                .append(processedPassportCard.getNameFirst()).append("<br/>");
        // Middle Name
        info.append(("Middle Name").concat(" - "))
                .append(processedPassportCard.getNameMiddle()).append("<br/>");
        // Last Name
        info.append(("Last Name").concat(" - "))
                .append(processedPassportCard.getNameLast()).append("<br/>");
        // Passport Number
        info.append(("Passport Number").concat(" - "))
                .append(processedPassportCard.getPassportNumber()).append("<br/>");
        // Personal Number
        info.append(("Personal Number").concat(" - "))
                .append(processedPassportCard.getPersonalNumber()).append("<br/>");
        // Sex
        info.append(("Sex").concat(" - ")).append(processedPassportCard.getSex())
                .append("<br/>");
        // Country Long
        info.append(("Country Long").concat(" - "))
                .append(processedPassportCard.getCountryLong()).append("<br/>");
        // Nationality Long
        info.append(("Nationality Long").concat(" - "))
                .append(processedPassportCard.getNationalityLong()).append("<br/>");
        // DOB Long
        info.append(("DOB Long").concat(" - "))
                .append(processedPassportCard.getDateOfBirth4()).append("<br/>");
        // Issue Date
        info.append(("Issue Date Long").concat(" - "))
                .append(processedPassportCard.getIssueDate4()).append("<br/>");
        // Long Expiration
        info.append(("Expiration Date Long").concat(" - "))
                .append(processedPassportCard.getExpirationDate4()).append("<br/>");
        // Place of Birth
        info.append(("Place of Birth").concat(" - "))
                .append(processedPassportCard.getEnd_POB()).append("<br/>");
        if(processedPassportCard.getAuthenticationResult()!=null) {
            info.append("Document Authentication".concat(" - ")).append(processedPassportCard.getAuthenticationResult()).append("<br/>");
        }
        if(processedPassportCard.getAuthenticationResultSummaryList()!=null && processedPassportCard.getAuthenticationResultSummaryList().size()>0) {
            info.append("Document Authentication".concat(" - ")).append(getStringFromList(processedPassportCard.getAuthenticationResultSummaryList())).append("<br/>");
        }

        if(processedPassportCard.idLocationCountryTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location Country Test".concat(" - ")).append(processedPassportCard.idLocationCountryTestResult).append("<br/>");
        }

        if(processedPassportCard.idLocationStateTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location State Test".concat(" - ")).append(processedPassportCard.idLocationStateTestResult).append("<br/>");
        }

        if(processedPassportCard.idLocationCityTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location City Test".concat(" - ")).append(processedPassportCard.idLocationCityTestResult).append("<br/>");
        }

        if(processedPassportCard.idLocationZipcodeTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location Zipcode Test".concat(" - ")).append(processedPassportCard.idLocationZipcodeTestResult).append("<br/>");
        }

        AcuantAndroidMobileSDKController instance = AcuantAndroidMobileSDKController.getInstance();

        if(instance.getDeviceCountryName()!= null) {
            info.append("Device Country".concat(" - ")).append(instance.getDeviceCountryName()).append("<br/>");
        }

        if(instance.getDeviceCountryCode()!= null) {
            info.append("Device Country Code".concat(" - ")).append(instance.getDeviceCountryCode()).append("<br/>");
        }

        if(instance.getDeviceState()!= null) {
            info.append("Device State".concat(" - ")).append(instance.getDeviceState()).append("<br/>");
        }

        if(instance.getDeviceCity()!= null) {
            info.append("Device City".concat(" - ")).append(instance.getDeviceCity()).append("<br/>");
        }

        if(instance.getDeviceArea()!= null) {
            info.append("Device Area".concat(" - ")).append(instance.getDeviceArea()).append("<br/>");
        }

        if(instance.getDeviceZipCode()!= null) {
            info.append("Device Zip Code".concat(" - ")).append(instance.getDeviceZipCode()).append("<br/>");
        }


        info.append("TID".concat(" - ")).append(processedPassportCard.getTransactionId()).append("<br/>");
        if(processedFacialData!=null && isFacialFlow) {
            info.append("FTID".concat(" - ")).append(processedFacialData.getTransactionId()).append("<br/>");
            info.append("Facial Matched".concat(" - ")).append(processedFacialData.getFacialMatch()).append("<br/>");
            info.append("Facial Match Confidence Rating".concat(" - ")).append(processedFacialData.getFacialMatchConfidenceRating()).append("<br/>");
            info.append("Live Face Detected".concat(" - ")).append(processedFacialData.getFaceLivelinessDetection()).append("<br/>");
        }

        textViewCardInfo.setText(Html.fromHtml(info.toString()));

        frontSideCardImageView.setImageBitmap(Util.getRoundedCornerBitmap(processedPassportCard
                .getReformattedImage(), this.getApplicationContext()));
        imgFaceViewer.setImageBitmap(processedPassportCard.getFaceImage());
        imgSignatureViewer.setImageBitmap(processedPassportCard.getSignImage());
    }



    /**
     *
     */
    private void setResultsForMedicalCard()
    {
        MedicalCard processedMedicalCard = DataContext.getInstance().getProcessedMedicalCard();

        StringBuilder info = new StringBuilder();

        // First Name
        info.append(("First Name").concat(" - "))
                .append(processedMedicalCard.getFirstName()).append("<br/>");
        // Last Name
        info.append(("Last Name").concat(" - "))
                .append(processedMedicalCard.getLastName()).append("<br/>");
        // MemberID
        info.append(("MemberID").concat(" - ")).append(processedMedicalCard.getMemberId())
                .append("<br/>");
        // Group No
        info.append(("Group No.").concat(" - "))
                .append(processedMedicalCard.getGroupNumber()).append("<br/>");
        // Copay ER
        info.append(("Copay ER").concat(" - ")).append(processedMedicalCard.getCopayEr())
                .append("<br/>");
        // Copay OV
        info.append(("Copay OV").concat(" - ")).append(processedMedicalCard.getCopayOv())
                .append("<br/>");
        // Copay SP
        info.append(("Copay SP").concat(" - ")).append(processedMedicalCard.getCopaySp())
                .append("<br/>");
        // Copay UC
        info.append(("Copay UC").concat(" - ")).append(processedMedicalCard.getCopayUc())
                .append("<br/>");
        // Coverage
        info.append(("Coverage").concat(" - ")).append(processedMedicalCard.getCoverage())
                .append("<br/>");
        // Date of Birth
        info.append(("Date of Birth").concat(" - "))
                .append(processedMedicalCard.getDateOfBirth()).append("<br/>");
        // Deductible
        info.append(("Deductible").concat(" - "))
                .append(processedMedicalCard.getDeductible()).append("<br/>");
        // Effective Date
        info.append(("Effective Date").concat(" - "))
                .append(processedMedicalCard.getEffectiveDate()).append("<br/>");
        // Employer
        info.append(("Employer").concat(" - ")).append(processedMedicalCard.getEmployer())
                .append("<br/>");
        // Expire Date
        info.append(("Expire Date").concat(" - "))
                .append(processedMedicalCard.getExpirationDate()).append("<br/>");
        // Group Name
        info.append(("Group Name").concat(" - "))
                .append(processedMedicalCard.getGroupName()).append("<br/>");
        // Issuer Number
        info.append(("Issuer Number").concat(" - "))
                .append(processedMedicalCard.getIssuerNumber()).append("<br/>");
        // Other
        info.append(("Other").concat(" - ")).append(processedMedicalCard.getOther())
                .append("<br/>");
        // Payer ID
        info.append(("Payer ID").concat(" - ")).append(processedMedicalCard.getPayerId())
                .append("<br/>");
        // Plan Admin
        info.append(("Plan Admin").concat(" - "))
                .append(processedMedicalCard.getPlanAdmin()).append("<br/>");
        // Plan Provider
        info.append(("Plan Provider").concat(" - "))
                .append(processedMedicalCard.getPlanProvider()).append("<br/>");
        // Plan Type
        info.append(("Plan Type").concat(" - "))
                .append(processedMedicalCard.getPlanType()).append("<br/>");
        // RX Bin
        info.append(("RX Bin").concat(" - ")).append(processedMedicalCard.getRxBin())
                .append("<br/>");
        // RX Group
        info.append(("RX Group").concat(" - ")).append(processedMedicalCard.getRxGroup())
                .append("<br/>");
        // RX ID
        info.append(("RX ID").concat(" - ")).append(processedMedicalCard.getRxId())
                .append("<br/>");
        // RX PCN
        info.append(("RX PCN").concat(" - ")).append(processedMedicalCard.getRxPcn())
                .append("<br/>");
        // Telephone
        info.append(("Telephone").concat(" - "))
                .append(processedMedicalCard.getPhoneNumber()).append("<br/>");
        // Web
        info.append(("Web").concat(" - ")).append(processedMedicalCard.getWebAddress())
                .append("<br/>");
        // Email
        info.append(("Email").concat(" - ")).append(processedMedicalCard.getEmail())
                .append("<br/>");
        // Address
        info.append(("Address").concat(" - "))
                .append(processedMedicalCard.getFullAddress()).append("<br/>");
        // City
        info.append(("City").concat(" - ")).append(processedMedicalCard.getCity())
                .append("<br/>");
        // Zip
        info.append(("Zip").concat(" - ")).append(processedMedicalCard.getZip())
                .append("<br/>");
        // State
        info.append(("State").concat(" - ")).append(processedMedicalCard.getState())
                .append("<br/>");
        info.append("TID".concat(" - ")).append(processedMedicalCard.getTransactionId()).append("<br/>");

        if(processedMedicalCard.idLocationCountryTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location Country Test".concat(" - ")).append(processedMedicalCard.idLocationCountryTestResult).append("<br/>");
        }

        if(processedMedicalCard.idLocationStateTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location State Test".concat(" - ")).append(processedMedicalCard.idLocationStateTestResult).append("<br/>");
        }

        if(processedMedicalCard.idLocationCityTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location City Test".concat(" - ")).append(processedMedicalCard.idLocationCityTestResult).append("<br/>");
        }

        if(processedMedicalCard.idLocationZipcodeTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location Zipcode Test".concat(" - ")).append(processedMedicalCard.idLocationZipcodeTestResult).append("<br/>");
        }

        AcuantAndroidMobileSDKController instance = AcuantAndroidMobileSDKController.getInstance();

        if(instance.getDeviceCountryName()!= null) {
            info.append("Device Country".concat(" - ")).append(instance.getDeviceCountryName()).append("<br/>");
        }

        if(instance.getDeviceCountryCode()!= null) {
            info.append("Device Country Code".concat(" - ")).append(instance.getDeviceCountryCode()).append("<br/>");
        }

        if(instance.getDeviceState()!= null) {
            info.append("Device State".concat(" - ")).append(instance.getDeviceState()).append("<br/>");
        }

        if(instance.getDeviceCity()!= null) {
            info.append("Device City".concat(" - ")).append(instance.getDeviceCity()).append("<br/>");
        }

        if(instance.getDeviceArea()!= null) {
            info.append("Device Area".concat(" - ")).append(instance.getDeviceArea()).append("<br/>");
        }

        if(instance.getDeviceZipCode()!= null) {
            info.append("Device Zip Code".concat(" - ")).append(instance.getDeviceZipCode()).append("<br/>");
        }

        textViewCardInfo.setText(Html.fromHtml(info.toString()));

        frontSideCardImageView.setImageBitmap(Util.getRoundedCornerBitmap(processedMedicalCard
                .getReformattedImage(), this.getApplicationContext()));

        if (processedMedicalCard.getReformattedImageTwo() != null)
        {
            backSideCardImageView.setVisibility(View.VISIBLE);
            backSideCardImageView.setImageBitmap(Util.getRoundedCornerBitmap(processedMedicalCard
                    .getReformattedImageTwo(), this.getApplicationContext()));
        }
    }

    /**
     *
     */
    private void setResultsForDriversLicenseCard()
    {
        DriversLicenseCard processedLicenseCard = DataContext.getInstance().getProcessedLicenseCard();
        FacialData processedFacialData = DataContext.getInstance().getProcessedFacialData();
        StringBuilder info = new StringBuilder();
        // first name
        info.append(("First Name").concat(" - "))
                .append(processedLicenseCard.getNameFirst()).append("<br/>");
        // middle name
        info.append(("Middle Name").concat(" - "))
                .append(processedLicenseCard.getNameMiddle()).append("<br/>");
        // last name
        info.append(("Last Name").concat(" - "))
                .append(processedLicenseCard.getNameLast()).append("<br/>");
        // name suffix
        info.append(("Name Suffix").concat(" - "))
                .append(processedLicenseCard.getNameSuffix()).append("<br/>");
        // license id
        info.append(("ID").concat(" - ")).append(processedLicenseCard.getLicenceID())
                .append("<br/>");
        // license
        info.append(("License").concat(" - ")).append(processedLicenseCard.getLicense())
                .append("<br/>");
        // date of birth long
        info.append(("DOB Long").concat(" - "))
                .append(processedLicenseCard.getDateOfBirth4()).append("<br/>");
        // date of birth short
        info.append(("DOB Short").concat(" - "))
                .append(processedLicenseCard.getDateOfBirth()).append("<br/>");
        // date of birth local
        info.append(("Date Of Birth Local").concat(" - "))
                .append(processedLicenseCard.getDateOfBirthLocal()).append("<br/>");
        // issue date long
        info.append(("Issue Date Long").concat(" - "))
                .append(processedLicenseCard.getIssueDate4()).append("<br/>");
        // issue date short
        info.append(("Issue Date Short").concat(" - "))
                .append(processedLicenseCard.getIssueDate()).append("<br/>");
        // issue date local
        info.append(("Issue Date Local").concat(" - "))
                .append(processedLicenseCard.getIssueDateLocal()).append("<br/>");

        // expiration date long
        info.append(("Expiration Date Long").concat(" - "))
                .append(processedLicenseCard.getExpirationDate4()).append("<br/>");
        // expiration date short
        info.append(("Expiration Date Short").concat(" - "))
                .append(processedLicenseCard.getExpirationDate()).append("<br/>");

        // eye color
        info.append(("EyeColor").concat(" - ")).append(processedLicenseCard.getEyeColor())
                .append("<br/>");
        // hair color
        info.append(("HairColor").concat(" - ")).append(processedLicenseCard.getHair())
                .append("<br/>");
        // height
        info.append(("Height").concat(" - ")).append(processedLicenseCard.getHeight())
                .append("<br/>");
        // weight
        info.append(("Weight").concat(" - ")).append(processedLicenseCard.getWeight())
                .append("<br/>");

        // address
        info.append(("Address").concat(" - ")).append(processedLicenseCard.getAddress())
                .append("<br/>");
        // address 2
        info.append(("Address 2").concat(" - "))
                .append(processedLicenseCard.getAddress2()).append("<br/>");
        // address 3
        info.append(("Address 3").concat(" - "))
                .append(processedLicenseCard.getAddress3()).append("<br/>");
        // address 4
        info.append(("Address 4").concat(" - "))
                .append(processedLicenseCard.getAddress4()).append("<br/>");
        // address 5
        info.append(("Address 5").concat(" - "))
                .append(processedLicenseCard.getAddress5()).append("<br/>");
        // address 6
        info.append(("Address 6").concat(" - "))
                .append(processedLicenseCard.getAddress6()).append("<br/>");

        // city
        info.append(("City").concat(" - ")).append(processedLicenseCard.getCity())
                .append("<br/>");
        // zip
        info.append(("Zip").concat(" - ")).append(processedLicenseCard.getZip())
                .append("<br/>");
        // state
        info.append(("State").concat(" - ")).append(processedLicenseCard.getState())
                .append("<br/>");
        // country
        info.append(("Country").concat(" - ")).append(processedLicenseCard.getCounty())
                .append("<br/>");
        // country short
        info.append(("Country short").concat(" - "))
                .append(processedLicenseCard.getCountryShort()).append("<br/>");
        // country long
        info.append(("Country long").concat(" - "))
                .append(processedLicenseCard.getIdCountry()).append("<br/>");

        // license class
        info.append(("Class").concat(" - "))
                .append(processedLicenseCard.getLicenceClass()).append("<br/>");
        // restriction
        info.append(("Restriction").concat(" - "))
                .append(processedLicenseCard.getRestriction()).append("<br/>");
        // sex
        info.append(("Sex").concat(" - ")).append(processedLicenseCard.getSex())
                .append("<br/>");
        // audit
        info.append(("Audit").concat(" - ")).append(processedLicenseCard.getAudit())
                .append("<br/>");
        // Endorsements
        info.append(("Endorsements").concat(" - "))
                .append(processedLicenseCard.getEndorsements()).append("<br/>");
        // Fee
        info.append(("Fee").concat(" - ")).append(processedLicenseCard.getFee())
                .append("<br/>");
        // CSC
        info.append(("CSC").concat(" - ")).append(processedLicenseCard.getCSC())
                .append("<br/>");
        // SigNum
        info.append(("SigNum").concat(" - ")).append(processedLicenseCard.getSigNum())
                .append("<br/>");
        // Text1
        info.append(("Text1").concat(" - ")).append(processedLicenseCard.getText1())
                .append("<br/>");
        // Text2
        info.append(("Text2").concat(" - ")).append(processedLicenseCard.getText2())
                .append("<br/>");
        // Text3
        info.append(("Text3").concat(" - ")).append(processedLicenseCard.getText3())
                .append("<br/>");
        // Type
        info.append(("Type").concat(" - ")).append(processedLicenseCard.getType())
                .append("<br/>");
        // Doc Type
        info.append(("Doc Type").concat(" - ")).append(processedLicenseCard.getDocType())
                .append("<br/>");
        // Father Name
        info.append(("Father Name").concat(" - "))
                .append(processedLicenseCard.getFatherName()).append("<br/>");
        // Mother Name
        info.append(("Mother Name").concat(" - "))
                .append(processedLicenseCard.getMotherName()).append("<br/>");
        // NameFirst_NonMRZ
        info.append(("NameFirst_NonMRZ").concat(" - "))
                .append(processedLicenseCard.getNameFirst_NonMRZ()).append("<br/>");
        // NameFirst_NonMRZ
        info.append(("NameLast_NonMRZ").concat(" - "))
                .append(processedLicenseCard.getNameLast_NonMRZ()).append("<br/>");
        // NameLast1
        info.append(("NameLast1").concat(" - "))
                .append(processedLicenseCard.getNameLast1()).append("<br/>");
        // NameLast2
        info.append(("NameLast2").concat(" - "))
                .append(processedLicenseCard.getNameLast2()).append("<br/>");
        // NameMiddle_NonMRZ
        info.append(("NameMiddle_NonMRZ").concat(" - "))
                .append(processedLicenseCard.getNameMiddle_NonMRZ()).append("<br/>");
        // NameSuffix_NonMRZ
        info.append(("NameSuffix_NonMRZ").concat(" - "))
                .append(processedLicenseCard.getNameSuffix_NonMRZ()).append("<br/>");
        // DocumentDetectName
        info.append(("Document Detectd Name").concat(" - "))
                .append(processedLicenseCard.getDocumentDetectedName()).append("<br/>");
        // DocumentDetectNameShort
        info.append(("Document Detectd Name Short").concat(" - "))
                .append(processedLicenseCard.getDocumentDetectedNameShort()).append("<br/>");

        // Nationality
        info.append(("Nationality").concat(" - "))
                .append(processedLicenseCard.getNationality()).append("<br/>");
        // Original
        info.append(("Original").concat(" - ")).append(processedLicenseCard.getOriginal())
                .append("<br/>");
        // PlaceOfBirth
        info.append(("Place Of Birth").concat(" - "))
                .append(processedLicenseCard.getPlaceOfBirth()).append("<br/>");
        // PlaceOfIssue
        info.append(("Place Of Issue").concat(" - ")).append(processedLicenseCard.getPlaceOfIssue()).append("<br/>");
        // Social Security
        info.append(("Social Security").concat(" - ")).append(processedLicenseCard.getSocialSecurity()).append("<br/>");
        //info.append("IsAddressCorrected ".concat(" - ")).append(processedLicenseCard.isAddressCorrected()).append("<br/>");
        //IsAddressVerifiedinfo.append("IsAddressVerified ".concat(" - ")).append(processedLicenseCard.isAddressVerified()).append("<br/>");

        if (processedLicenseCard.getRegion() == Region.REGION_CANADA || processedLicenseCard.getRegion() == Region.REGION_UNITED_STATES) {
            info.append("Is Barcode Read ".concat(" - ")).append(processedLicenseCard.getIsBarcodeRead()).append("<br/>");
            info.append("Is ID Verified ".concat(" - ")).append(processedLicenseCard.getIsIDVerified()).append("<br/>");
            info.append("Is Ocr Read ".concat(" - ")).append(processedLicenseCard.getIsOcrRead()).append("<br/>");
        }

        info.append("Document Verification Confidence Rating".concat(" - ")).append(processedLicenseCard.getDocumentVerificationConfidenceRating()).append("<br/>");
        if(processedLicenseCard.getAuthenticationResult()!=null) {
            info.append("Document Authentication".concat(" - ")).append(processedLicenseCard.getAuthenticationResult()).append("<br/>");
        }
        if(processedLicenseCard.getAuthenticationResultSummaryList()!=null && processedLicenseCard.getAuthenticationResultSummaryList().size()>0) {
            info.append("Document Authentication".concat(" - ")).append(getStringFromList(processedLicenseCard.getAuthenticationResultSummaryList())).append("<br/>");
        }

        if(processedLicenseCard.idLocationCountryTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location Country Test".concat(" - ")).append(processedLicenseCard.idLocationCountryTestResult).append("<br/>");
        }

        if(processedLicenseCard.idLocationStateTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location State Test".concat(" - ")).append(processedLicenseCard.idLocationStateTestResult).append("<br/>");
        }

        if(processedLicenseCard.idLocationCityTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location City Test".concat(" - ")).append(processedLicenseCard.idLocationCityTestResult).append("<br/>");
        }

        if(processedLicenseCard.idLocationZipcodeTestResult!= LocationVerificationResult.NOT_AVAILABLE) {
            info.append("ID Location Zipcode Test".concat(" - ")).append(processedLicenseCard.idLocationZipcodeTestResult).append("<br/>");
        }


        AcuantAndroidMobileSDKController instance = AcuantAndroidMobileSDKController.getInstance();

        if(instance.getDeviceCountryName()!= null) {
            info.append("Device Country".concat(" - ")).append(instance.getDeviceCountryName()).append("<br/>");
        }

        if(instance.getDeviceCountryCode()!= null) {
            info.append("Device Country Code".concat(" - ")).append(instance.getDeviceCountryCode()).append("<br/>");
        }

        if(instance.getDeviceState()!= null) {
            info.append("Device State".concat(" - ")).append(instance.getDeviceState()).append("<br/>");
        }

        if(instance.getDeviceCity()!= null) {
            info.append("Device City".concat(" - ")).append(instance.getDeviceCity()).append("<br/>");
        }

        if(instance.getDeviceArea()!= null) {
            info.append("Device Area".concat(" - ")).append(instance.getDeviceArea()).append("<br/>");
        }

        if(instance.getDeviceZipCode()!= null) {
            info.append("Device Zip Code".concat(" - ")).append(instance.getDeviceZipCode()).append("<br/>");
        }



        info.append("TID".concat(" - ")).append(processedLicenseCard.getTransactionId()).append("<br/>");
        if(processedFacialData!=null && isFacialFlow) {
            info.append("FTID".concat(" - ")).append(processedFacialData.getTransactionId()).append("<br/>");
            info.append("Facial Matched".concat(" - ")).append(processedFacialData.getFacialMatch()).append("<br/>");
            info.append("Facial Match Confidence Rating".concat(" - ")).append(processedFacialData.getFacialMatchConfidenceRating()).append("<br/>");
            info.append("Live Face Detected".concat(" - ")).append(processedFacialData.getFaceLivelinessDetection()).append("<br/>");
        }

        textViewCardInfo.setText(Html.fromHtml(info.toString()));
        if (processedLicenseCard.getReformatImage() != null) {
            frontSideCardImageView.setVisibility(View.VISIBLE);
            frontSideCardImageView.setImageBitmap(Util.getRoundedCornerBitmap(processedLicenseCard.getReformatImage(), this.getApplicationContext()));
        }else {
            frontSideCardImageView.setVisibility(View.GONE);
        }
        if (processedLicenseCard.getFaceImage() != null) {
            imgFaceViewer.setVisibility(View.VISIBLE);
            imgFaceViewer.setImageBitmap(processedLicenseCard.getFaceImage());
        }else {
            frontSideCardImageView.setVisibility(View.GONE);
        }
        if (processedLicenseCard.getSignImage() != null) {
            imgSignatureViewer.setVisibility(View.VISIBLE);
            imgSignatureViewer.setImageBitmap(processedLicenseCard.getSignImage());
        }else {
            imgSignatureViewer.setVisibility(View.GONE);
        }
        if (processedLicenseCard.getReformatImageTwo() != null){
            backSideCardImageView.setVisibility(View.VISIBLE);
            backSideCardImageView.setImageBitmap(Util.getRoundedCornerBitmap(processedLicenseCard.getReformatImageTwo(), this.getApplicationContext()));
        }else {
            backSideCardImageView.setVisibility(View.GONE);
        }
    }

    private String getStringFromList(ArrayList list){
        String retStr = null;

        for(Object str:list){
            if(retStr==null){
                retStr = (String)str;
            }else{
                retStr = retStr + ", "+str;
            }

        }

        return retStr;
    }

}
