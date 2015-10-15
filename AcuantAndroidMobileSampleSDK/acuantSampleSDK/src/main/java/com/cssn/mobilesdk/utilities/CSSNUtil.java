/*
 * 
 */
package com.cssn.mobilesdk.utilities;

import com.acuant.mobilesdk.CardType;

/**
 * General utilities about CSSN mobile 
 */
public class CSSNUtil
{
    public static final int DEFAULT_CROP_PASSPORT_WIDTH = 1478;
    public static final int DEFAULT_CROP_PASSPORT_HEIGHT = 1000;
    
    public static final int DEFAULT_CROP_DRIVERS_LICENSE_WIDTH = 1009;
    public static final int DEFAULT_CROP_DRIVERS_LICENSE_HEIGHT = 637;
    
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
