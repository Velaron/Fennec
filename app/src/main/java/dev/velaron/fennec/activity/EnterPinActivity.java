package dev.velaron.fennec.activity;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import dev.velaron.fennec.R;
import dev.velaron.fennec.fragment.EnterPinFragment;
import dev.velaron.fennec.util.Utils;

/**
 * Created by ruslan.kolbasa on 30-May-16.
 * mobilebankingandroid
 */
public class EnterPinActivity extends NoMainActivity {

    public static Class getClass(Context context){
        return Utils.is600dp(context) ? EnterPinActivity.class : EnterPinActivityPortraitOnly.class;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, EnterPinFragment.newInstance())
                    .commit();
        }
    }
}
