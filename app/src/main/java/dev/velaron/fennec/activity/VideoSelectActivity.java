package dev.velaron.fennec.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.fragment.VideosFragment;
import dev.velaron.fennec.fragment.VideosTabsFragment;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceProvider;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.Objects;

/**
 * Created by Ruslan Kolbasa on 17.08.2017.
 * phoenix
 */
public class VideoSelectActivity extends NoMainActivity implements PlaceProvider {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Settings.get().ui().getMainTheme());

        if (Objects.isNull(savedInstanceState)) {
            int accountId = super.getIntent().getExtras().getInt(Extra.ACCOUNT_ID);
            int ownerId = super.getIntent().getExtras().getInt(Extra.OWNER_ID);
            attachInitialFragment(accountId, ownerId);
        }
    }

    /**
     * @param context
     * @param accountId От чьего имени получать
     * @param ownerId   Чьи получать
     * @return
     */
    public static Intent createIntent(Context context, int accountId, int ownerId) {
        return new Intent(context, VideoSelectActivity.class)
                .putExtra(Extra.ACCOUNT_ID, accountId)
                .putExtra(Extra.OWNER_ID, ownerId);
    }

    private void attachInitialFragment(int accountId, int ownerId) {
        VideosTabsFragment fragment = VideosTabsFragment.newInstance(accountId, ownerId, VideosFragment.ACTION_SELECT);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(getMainContainerViewId(), fragment)
                .addToBackStack("video-tabs")
                .commit();
    }

    @Override
    public void openPlace(Place place) {
        if (place.type == Place.VIDEO_ALBUM) {
            Fragment fragment = VideosFragment.newInstance(place.getArgs());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .addToBackStack("video-album")
                    .commit();
        }
    }
}