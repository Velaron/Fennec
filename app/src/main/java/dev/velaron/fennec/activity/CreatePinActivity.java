package dev.velaron.fennec.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import dev.velaron.fennec.R;
import dev.velaron.fennec.fragment.CreatePinFragment;

/**
 * Created by ruslan.kolbasa on 30-May-16.
 * mobilebankingandroid
 */
public class CreatePinActivity extends NoMainActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, CreatePinFragment.newInstance())
                    .commit();
        }
    }
}