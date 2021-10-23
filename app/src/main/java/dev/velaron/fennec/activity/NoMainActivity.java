package dev.velaron.fennec.activity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import dev.velaron.fennec.R;
import dev.velaron.fennec.listener.BackPressCallback;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.Objects;

/**
 * Created by admin on 03.10.2016.
 * phoenix
 */
public abstract class NoMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Settings.get().ui().getMainTheme());
        setContentView(R.layout.activity_no_main);

        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(CurrentTheme.getStatusBarColor(this));
        w.setNavigationBarColor(CurrentTheme.getNavigationBarColor(this));

        getSupportFragmentManager().addOnBackStackChangedListener(mBackStackListener);
    }

    @IdRes
    protected int getMainContainerViewId() {
        return R.id.fragment;
    }

    private FragmentManager.OnBackStackChangedListener mBackStackListener = this::resolveToolbarNavigationIcon;

    private Toolbar mToolbar;

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        mToolbar = toolbar;
        resolveToolbarNavigationIcon();
    }

    private void resolveToolbarNavigationIcon() {
        if (Objects.isNull(mToolbar)) return;

        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 1) {
            mToolbar.setNavigationIcon(R.drawable.phoenix_round);
        } else {
            mToolbar.setNavigationIcon(R.drawable.close);
        }

        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        Fragment front = getSupportFragmentManager().findFragmentById(getMainContainerViewId());
        if (front instanceof BackPressCallback) {
            if (!(((BackPressCallback) front).onBackPressed())) {
                return;
            }
        }

        if (fm.getBackStackEntryCount() > 1) {
            super.onBackPressed();
        } else {
            supportFinishAfterTransition();
        }
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(mBackStackListener);
        super.onDestroy();
    }
}