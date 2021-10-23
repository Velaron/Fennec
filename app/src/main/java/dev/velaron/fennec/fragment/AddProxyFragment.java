package dev.velaron.fennec.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import dev.velaron.fennec.R;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.TextWatcherAdapter;
import dev.velaron.fennec.mvp.presenter.AddProxyPresenter;
import dev.velaron.fennec.mvp.view.IAddProxyView;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by Ruslan Kolbasa on 11.07.2017.
 * phoenix
 */
public class AddProxyFragment extends BaseMvpFragment<AddProxyPresenter, IAddProxyView> implements IAddProxyView {

    public static AddProxyFragment newInstance() {
        Bundle args = new Bundle();
        AddProxyFragment fragment = new AddProxyFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private EditText mAddress;
    private EditText mPort;
    private CheckBox mAuth;
    private EditText mUsername;
    private EditText mPassword;

    private View mAuthFieldsRoot;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_proxy_add, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mAuthFieldsRoot = root.findViewById(R.id.auth_fields_root);

        mAddress = root.findViewById(R.id.address);
        mAddress.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireAddressEdit(s);
            }
        });

        mPort = root.findViewById(R.id.port);
        mPort.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().firePortEdit(s);
            }
        });

        mAuth = root.findViewById(R.id.authorization);
        mAuth.setOnCheckedChangeListener((buttonView, isChecked) -> getPresenter().fireAuthChecked(isChecked));

        mUsername = root.findViewById(R.id.username);
        mUsername.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireUsernameEdit(s);
            }
        });

        mPassword = root.findViewById(R.id.password);
        mPassword.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().firePassEdit(s);
            }
        });

        root.findViewById(R.id.button_save).setOnClickListener(v -> getPresenter().fireSaveClick());
        return root;
    }

    @Override
    public void setAuthFieldsEnabled(boolean enabled) {
        if(nonNull(mAuthFieldsRoot)){
            mAuthFieldsRoot.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setAuthChecked(boolean checked) {
        if(nonNull(mAuth)){
            mAuth.setChecked(checked);
        }
    }

    @Override
    public void goBack() {
        requireActivity().onBackPressed();
    }

    @Override
    public IPresenterFactory<AddProxyPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AddProxyPresenter(saveInstanceState);
    }
}