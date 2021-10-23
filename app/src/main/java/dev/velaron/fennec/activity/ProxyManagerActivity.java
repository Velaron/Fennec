package dev.velaron.fennec.activity;

import android.os.Bundle;

import androidx.annotation.Nullable;
import dev.velaron.fennec.fragment.AddProxyFragment;
import dev.velaron.fennec.fragment.ProxyManagerFragment;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceProvider;
import dev.velaron.fennec.util.Objects;

/**
 * Created by admin on 10.07.2017.
 * phoenix
 */
public class ProxyManagerActivity extends NoMainActivity implements PlaceProvider {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(Objects.isNull(savedInstanceState)){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(getMainContainerViewId(), ProxyManagerFragment.newInstance())
                    .addToBackStack("proxy-manager")
                    .commit();
        }
    }

    @Override
    public void openPlace(Place place) {
        if(place.type == Place.PROXY_ADD){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(getMainContainerViewId(), AddProxyFragment.newInstance())
                    .addToBackStack("proxy-add")
                    .commit();
        }
    }
}