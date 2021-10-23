package dev.velaron.fennec.mvp.presenter;

import static dev.velaron.fennec.util.Utils.trimmedIsEmpty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import dev.velaron.fennec.Injection;
import dev.velaron.fennec.mvp.presenter.base.RxSupportPresenter;
import dev.velaron.fennec.mvp.view.IAddProxyView;
import dev.velaron.fennec.settings.IProxySettings;
import dev.velaron.fennec.util.ValidationUtil;

/**
 * Created by Ruslan Kolbasa on 11.07.2017.
 * phoenix
 */
public class AddProxyPresenter extends RxSupportPresenter<IAddProxyView> {

    private boolean authEnabled;
    private String address;
    private String port;
    private String userName;
    private String pass;

    private final IProxySettings settings;

    public AddProxyPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        settings = Injection.provideProxySettings();
    }

    @Override
    public void onGuiCreated(@NonNull IAddProxyView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.setAuthFieldsEnabled(authEnabled);
        viewHost.setAuthChecked(authEnabled);
    }

    public void fireAddressEdit(CharSequence s) {
        address = s.toString();
    }

    public void firePortEdit(CharSequence s) {
        port = s.toString();
    }

    public void fireAuthChecked(boolean isChecked) {
        if (authEnabled == isChecked) {
            return;
        }

        this.authEnabled = isChecked;
        getView().setAuthFieldsEnabled(isChecked);
    }

    public void fireUsernameEdit(CharSequence s) {
        userName = s.toString();
    }

    public void firePassEdit(CharSequence s) {
        pass = s.toString();
    }

    private boolean validateData() {
        try {
            try {
                int portInt = Integer.parseInt(port);
                if (portInt <= 0) {
                    throw new Exception("Invalid port");
                }
            } catch (NumberFormatException e) {
                throw new Exception("Invalid port");
            }

            if (!ValidationUtil.isValidIpAddress(address) && !ValidationUtil.isValidURL(address)) {
                throw new Exception("Invalid address");
            }

            if (authEnabled && trimmedIsEmpty(userName)) {
                throw new Exception("Invalid username");
            }

            if (authEnabled && trimmedIsEmpty(pass)) {
                throw new Exception("Invalid password");
            }
        } catch (Exception e) {
            showError(getView(), e);
            return false;
        }

        return true;
    }

    public void fireSaveClick() {
        if (!validateData()) {
            return;
        }

        final String finalAddress = address.trim();
        final int finalPort = Integer.parseInt(port.trim());

        if (authEnabled) {
            settings.put(finalAddress, finalPort, userName, pass);
        } else {
            settings.put(finalAddress, finalPort);
        }

        getView().goBack();
    }
}