package dev.velaron.fennec.activity;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import dev.velaron.fennec.R;
import dev.velaron.fennec.fragment.FileManagerFragment;
import dev.velaron.fennec.listener.BackPressCallback;
import dev.velaron.fennec.settings.CurrentTheme;
import dev.velaron.fennec.settings.Settings;

public class FileManagerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(Settings.get().ui().getMainTheme());

        setContentView(R.layout.activity_no_main);
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(CurrentTheme.getStatusBarColor(this));

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (toolbar != null) {
            toolbar.setNavigationIcon(R.drawable.close);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        if (savedInstanceState == null) {
            attachFragment();
        }
    }

    private void attachFragment() {
        FileManagerFragment ignoredFragment = new FileManagerFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment, ignoredFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fragment);
        if (fragment != null && fragment instanceof BackPressCallback) {
            if (((BackPressCallback) fragment).onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }
}
