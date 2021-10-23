package dev.velaron.fennec.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.fragment.DocsFragment;
import dev.velaron.fennec.fragment.VideosFragment;
import dev.velaron.fennec.fragment.VideosTabsFragment;
import dev.velaron.fennec.model.Types;
import dev.velaron.fennec.mvp.presenter.DocsListPresenter;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceProvider;

public class AttachmentsActivity extends NoMainActivity implements PlaceProvider {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Fragment fragment = null;

            int type = getIntent().getExtras().getInt(Extra.TYPE);
            int accountId = getIntent().getExtras().getInt(Extra.ACCOUNT_ID);

            switch (type){
                case Types.DOC:
                    fragment = DocsFragment.newInstance(accountId, accountId, DocsListPresenter.ACTION_SELECT);
                    break;

                case Types.VIDEO:
                    fragment = VideosTabsFragment.newInstance(accountId, accountId, VideosFragment.ACTION_SELECT);
                    break;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    public static Intent createIntent(Context context, int accountId, int type){
        return new Intent(context, AttachmentsActivity.class)
                .putExtra(Extra.TYPE, type)
                .putExtra(Extra.ACCOUNT_ID, accountId);
    }

    @Override
    public void openPlace(Place place) {
        if(place.type == Place.VIDEO_ALBUM){
            Fragment fragment = VideosFragment.newInstance(place.getArgs());
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .addToBackStack("video_album")
                    .commit();
        }
    }
}