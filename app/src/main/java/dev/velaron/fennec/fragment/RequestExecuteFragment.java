package dev.velaron.fennec.fragment;

import android.Manifest;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.listener.TextWatcherAdapter;
import dev.velaron.fennec.mvp.presenter.RequestExecutePresenter;
import dev.velaron.fennec.mvp.view.IRequestExecuteView;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

/**
 * Created by Ruslan Kolbasa on 05.07.2017.
 * phoenix
 */
public class RequestExecuteFragment extends BaseMvpFragment<RequestExecutePresenter, IRequestExecuteView> implements IRequestExecuteView {

    private static final int REQUEST_PERMISSION_WRITE = 14;

    public static RequestExecuteFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        RequestExecuteFragment fragment = new RequestExecuteFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private EditText mResposeBody;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_request_executor, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mResposeBody = root.findViewById(R.id.response_body);

        EditText methodEditText = root.findViewById(R.id.method);
        methodEditText.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireMethodEdit(s);
            }
        });

        EditText bodyEditText = root.findViewById(R.id.body);
        bodyEditText.addTextChangedListener(new TextWatcherAdapter(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireBodyEdit(s);
            }
        });

        root.findViewById(R.id.button_copy).setOnClickListener(v -> getPresenter().fireCopyClick());
        root.findViewById(R.id.button_save).setOnClickListener(v -> getPresenter().fireSaveClick());
        root.findViewById(R.id.button_execute).setOnClickListener(v -> getPresenter().fireExecuteClick());
        return root;
    }

    @Override
    public IPresenterFactory<RequestExecutePresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new RequestExecutePresenter(requireArguments().getInt(Extra.ACCOUNT_ID), saveInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(actionBar != null){
            actionBar.setTitle(R.string.request_executor_title);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void displayBody(String body) {
        safelySetText(mResposeBody, body);
    }

    @Override
    public void hideKeyboard() {
        ActivityUtils.hideSoftKeyboard(requireActivity());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_WRITE){
            getPresenter().fireWritePermissionResolved();
        }
    }

    @Override
    public void requestWriteExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE);
    }
}