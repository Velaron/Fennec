package dev.velaron.fennec.view.steppers.impl;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import dev.velaron.fennec.R;
import dev.velaron.fennec.view.steppers.base.AbsStepHolder;
import dev.velaron.fennec.view.steppers.base.BaseHolderListener;

public class CreatePhotoAlbumStep2Holder extends AbsStepHolder<CreatePhotoAlbumStepsHost> {

    private CheckBox mUploadByAdminsOnly;
    private CheckBox mDisableComments;

    private ActionListener mActionListener;

    public CreatePhotoAlbumStep2Holder(@NonNull ViewGroup parent, @NonNull ActionListener actionListener) {
        super(parent, R.layout.content_create_photo_album_step_2, CreatePhotoAlbumStepsHost.STEP_UPLOAD_AND_COMMENTS);
        mActionListener = actionListener;
    }

    public interface ActionListener extends BaseHolderListener {
        void onUploadByAdminsOnlyChecked(boolean checked);
        void onCommentsDisableChecked(boolean checked);
    }

    @Override
    public void initInternalView(View contentView) {
        mUploadByAdminsOnly = contentView.findViewById(R.id.upload_only_admins);
        mDisableComments = contentView.findViewById(R.id.disable_comments);

        CompoundButton.OnCheckedChangeListener uploadByAdminsOnlyListener = (compoundButton, b) -> mActionListener.onUploadByAdminsOnlyChecked(b);
        CompoundButton.OnCheckedChangeListener disableCommentsListener = (compoundButton, b) -> mActionListener.onCommentsDisableChecked(b);

        mUploadByAdminsOnly.setOnCheckedChangeListener(uploadByAdminsOnlyListener);
        mDisableComments.setOnCheckedChangeListener(disableCommentsListener);
    }

    @Override
    protected void bindViews(CreatePhotoAlbumStepsHost host) {
        mDisableComments.setChecked(host.getState().isCommentsDisabled());
        mUploadByAdminsOnly.setChecked(host.getState().isUploadByAdminsOnly());

        mDisableComments.setEnabled(host.isAdditionalOptionsEnable());
        mUploadByAdminsOnly.setEnabled(host.isAdditionalOptionsEnable());
    }
}
