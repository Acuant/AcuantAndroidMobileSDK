/**
 * 
 */
package com.cssn.samplesdk.model;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import com.cssn.samplesdk.model.MainActivityModel.State.ValidatedStateActivation;

/**
 * Model to be used in the main activity.
 */
public class MainActivityModel
{
    private static final String TAG = MainActivityModel.class.getName();
    
    private int currentOptionType = -1;
    //private Bitmap frontSideCardImage = null;
    //private Bitmap backSideCardImage = null;

    private CardSide cardSideSelected;
    
    /**
     * 
     */
    private State state = null;
    
    /**
     * If the validated state is validatedStateActivation or not.
     */
    private ValidatedStateActivation validatedStateActivation = null;
    
    /**
     * Error message to be shown when once if it is not null. 
     */
    private String errorMessage = null;
    
    /**
     * false indicates that all changes in the model are reflected in the UI. true indicates that the UI needs refreshing. 
     */
    private boolean isDirty = true;
    
    /**
     * @return the validatedStateActivation
     */
    public ValidatedStateActivation getValidatedStateActivation()
    {
        if (state != State.VALIDATED)
        {
            throw new IllegalStateException("Only the state VALIDATED supports validatedStateActivation or not.");
        }
        
        return validatedStateActivation;
    }

    /**
     */
    public void setValidatedStateActivation(ValidatedStateActivation activated)
    {
        if (state != State.VALIDATED)
        {
            throw new IllegalStateException("Only the state VALIDATED supports validatedStateActivation or not.");
        }
        
        this.validatedStateActivation = activated;
    }

    /**
     * @return the currentOptionType
     */
    public int getCurrentOptionType()
    {
        return currentOptionType;
    }

    /**
     * @param currentOptionType
     *            the currentOptionType to set
     */
    public void setCurrentOptionType(int currentOptionType)
    {
        this.currentOptionType = currentOptionType;
    }

    /**
     * @return the frontSideCardImage
     */
    /*public Bitmap getFrontSideCardImage()
    {
        return frontSideCardImage;
    }*/

    /**
     * @param frontSideCardImage
     *            the frontSideCardImage to set
     */
    /*public void setFrontSideCardImage(Bitmap frontSideCardImage)
    {
        this.frontSideCardImage = frontSideCardImage;

    }*/

    /**
     * @return the backSideCardImage
     */
    /*public Bitmap getBackSideCardImage()
    {
        return backSideCardImage;
    }*/

    /**
     * @param backSideCardImage
     *            the backSideCardImage to set
     */
    /*public void setBackSideCardImage(Bitmap backSideCardImage)
    {
        this.backSideCardImage = backSideCardImage;
    }8?

    /**
     * 
     * @return true if the model has no images.
     */
    /*public boolean isEmpty()
    {
        return frontSideCardImage == null && backSideCardImage == null;
    }*/

    /**
     * clears the images in the model
     */
    /*public void clearImages()
    {
        if(frontSideCardImage!=null) {
            frontSideCardImage.recycle();
            frontSideCardImage = null;
        }
        if(backSideCardImage!=null) {
            backSideCardImage.recycle();
            backSideCardImage = null;
        }
    }*/

    /*public void clearBackImage()
    {
        if(backSideCardImage!=null) {
            backSideCardImage.recycle();
            backSideCardImage = null;
        }
    }*/

    /**
     * @return the cardSideSelected
     */
    public CardSide getCardSideSelected()
    {
        return cardSideSelected;
    }

    /**
     * @param cardSideSelected
     *            the cardSideSelected to set
     */
    public void setCardSideSelected(CardSide cardSideSelected)
    {
        this.cardSideSelected = cardSideSelected;
    }

    /**
     * @return the state
     */
    public State getState()
    {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(State state)
    {
        this.state = state;
    }

    /**
     * @return the isDirty
     */
    public boolean isDirty()
    {
        return isDirty;
    }

    /**
     * @param isDirty the isDirty to set
     */
    public void setDirty(boolean isDirty)
    {
        this.isDirty = isDirty;
    }

    /**
     * @return the errorMessage
     */
    public String getErrorMessage()
    {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage)
    {
        this.errorMessage = errorMessage;
    }

    /**
     * Possibles sides of one card
     */
    public static enum CardSide
    {
        FRONT, BACK;
    }
    
    /**
     * 
     */
    public static enum State
    {
        /**
         * The validation was not performed with the current licenseKey.
         */
        NO_VALIDATED
        ,
        /**
         * it is being validated the current license key.
         */
        VALIDATING
        , 
        /**
         * it has had an response from the server about validation.
         */
        VALIDATED;
        
        /**
         * If the validated state is validatedStateActivation or not.
         */
        public static enum ValidatedStateActivation
        {
            ACTIVATED, NO_ACTIVATED;
        };
    };

}
