package dev.velaron.fennec.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collection;

import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.activity.CreatePinActivity;
import dev.velaron.fennec.crypt.KeyLocationPolicy;
import dev.velaron.fennec.db.Stores;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.settings.ISettings;
import dev.velaron.fennec.settings.SecuritySettings;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.AssertUtils;

public class SecurityPreferencesFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private SwitchPreference mUsePinForSecurityPreference;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.security_settings);

        mUsePinForSecurityPreference = findPreference(SecuritySettings.KEY_USE_PIN_FOR_SECURITY);
        mUsePinForSecurityPreference.setOnPreferenceChangeListener(this);

        Preference changePinPreference = findPreference(SecuritySettings.KEY_CHANGE_PIN);
        changePinPreference.setOnPreferenceClickListener(preference -> {
            startActivityForResult(new Intent(requireActivity(), CreatePinActivity.class), REQUEST_CHANGE_PIN);
            return true;
        });

        Preference clearKeysPreference = findPreference(SecuritySettings.KEY_DELETE_KEYS);
        AssertUtils.requireNonNull(clearKeysPreference);
        clearKeysPreference.setOnPreferenceClickListener(preference -> {
            onClearKeysClick();
            return true;
        });

        findPreference("encryption_terms_of_use").setOnPreferenceClickListener(preference -> {
            View view = View.inflate(requireActivity(), R.layout.content_encryption_terms_of_use, null);
            new MaterialAlertDialogBuilder(requireActivity())
                    .setView(view)
                    .setTitle(R.string.phoenix_encryption)
                    .setNegativeButton(R.string.button_cancel, null)
                    .show();
            return true;
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));
    }

    private void onClearKeysClick() {
        String[] items = {getString(R.string.for_the_current_account), getString(R.string.for_all_accounts)};
        new MaterialAlertDialogBuilder(requireActivity())
                .setItems(items, (dialog, which) -> onClearKeysClick(which == 1))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    private void onClearKeysClick(boolean allAccount) {
        if (allAccount) {
            Collection<Integer> accountIds = Settings.get()
                    .accounts()
                    .getRegistered();

            for (Integer accountId : accountIds) {
                removeKeysFor(accountId);
            }
        } else {
            int currentAccountId = Settings.get()
                    .accounts()
                    .getCurrent();

            if (ISettings.IAccountsSettings.INVALID_ID != currentAccountId) {
                removeKeysFor(currentAccountId);
            }
        }

        Toast.makeText(requireActivity(), R.string.deleted, Toast.LENGTH_LONG).show();
    }

    private void removeKeysFor(int accountId) {
        Stores.getInstance()
                .keys(KeyLocationPolicy.PERSIST)
                .deleteAll(accountId)
                .blockingAwait();

        Stores.getInstance()
                .keys(KeyLocationPolicy.RAM)
                .deleteAll(accountId)
                .blockingAwait();
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings);
            actionBar.setSubtitle(R.string.security);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_SETTINGS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    private static final int REQUEST_CREATE_PIN = 1786;
    private static final int REQUEST_CHANGE_PIN = 1787;

    private void startCreatePinActivity() {
        startActivityForResult(new Intent(requireActivity(), CreatePinActivity.class), REQUEST_CREATE_PIN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_PIN && resultCode == Activity.RESULT_OK) {
            int[] values = CreatePinFragment.extractValueFromIntent(data);
            Settings.get()
                    .security()
                    .setPin(values);
            mUsePinForSecurityPreference.setChecked(true);
        }

        if (requestCode == REQUEST_CHANGE_PIN && resultCode == Activity.RESULT_OK) {
            int[] values = CreatePinFragment.extractValueFromIntent(data);
            Settings.get()
                    .security()
                    .setPin(values);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        switch (preference.getKey()) {
            case SecuritySettings.KEY_USE_PIN_FOR_SECURITY:
                Boolean usePinForSecurity = (Boolean) newValue;
                if (usePinForSecurity) {
                    if (!Settings.get().security().hasPinHash()) {
                        startCreatePinActivity();
                        return false;
                    } else {
                        // при вызове mUsePinForSecurityPreference.setChecked(true) мы опять попадем в этот блок
                        return true;
                    }
                } else {
                    Settings.get().security().setPin(null);
                    return true;
                }
        }

        return false;
    }
}