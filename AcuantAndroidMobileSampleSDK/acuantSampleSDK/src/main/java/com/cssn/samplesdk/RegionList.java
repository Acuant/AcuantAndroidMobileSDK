package com.cssn.samplesdk;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import com.acuant.mobilesdk.Region;
import com.cssn.samplesdk.model.MainActivityModel;
import com.cssn.samplesdk.util.DataContext;

/**
 * Created by DiegoArena on 7/10/15.
 */
public class RegionList extends Activity {
    private MainActivityModel mainActivityModel = null;

    /**
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // load several member variables
        setContentView(R.layout.activity_regionlist);
        mainActivityModel = DataContext.getInstance().getMainActivityModel();
    }

    public void selectedRegion(View v){
        int region;
        switch (v.getId()){
            case R.id.USA:
                region = Region.REGION_UNITED_STATES;
                break;
            case R.id.Canada:
                region = Region.REGION_CANADA;
                break;
            case R.id.Europe:
                region = Region.REGION_EUROPE;
                break;
            case R.id.Africa:
                region = Region.REGION_AFRICA;
                break;
            case R.id.Asia:
                region = Region.REGION_ASIA;
                break;
            case R.id.America:
                region = Region.REGION_AMERICA;
                break;
            case R.id.Australia:
                region = Region.REGION_AUSTRALIA;
                break;
            default:
                region = Region.REGION_UNITED_STATES;
                break;
        }
        DataContext.getInstance().setCardRegion(region);
        finish();
    }
}
