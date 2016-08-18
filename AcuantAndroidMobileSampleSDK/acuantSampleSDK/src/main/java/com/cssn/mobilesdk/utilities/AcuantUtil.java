/*
 * 
 */
package com.cssn.mobilesdk.utilities;

import com.acuant.mobilesdk.CardType;

/**
 * General utilities about CSSN mobile 
 */
public class AcuantUtil
{
    public static final int DEFAULT_CROP_PASSPORT_WIDTH = 1478;

    public static final int DEFAULT_CROP_DRIVERS_LICENSE_WIDTH = 1250;

    public static final int DEFAULT_CROP_DRIVERS_LICENSE_WIDTH_FOR_AUTHENTICATION = 2024;

    public static final int DEFAULT_CROP_MEDICAL_INSURANCE = 1500;

    /**
     * 
     * @param cardType
     * @return the aspect ratio for different card types.
     */
    public static double getAspectRatio(int cardType)
    {
        double retVal = -1;
        
        switch (cardType)
        {
            case CardType.DRIVERS_LICENSE:
            case CardType.FACIAL_RECOGNITION:
            case CardType.MEDICAL_INSURANCE:
                
                retVal = 0.637;
                break;

            case CardType.PASSPORT:
                
                retVal = 0.677;
                break;

            default:
                throw new IllegalStateException("This method is bad implemented, there is not processing for the cardtype '"
                        + cardType + "'");
        }
        
        return retVal;
    }
}
