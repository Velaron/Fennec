package dev.velaron.fennec.fragment.sheet;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.AttachmentsActivity;
import dev.velaron.fennec.activity.DualTabPhotoActivity;
import dev.velaron.fennec.activity.VideoSelectActivity;
import dev.velaron.fennec.adapter.AttachmentsBottomSheetAdapter;
import dev.velaron.fennec.model.AbsModel;
import dev.velaron.fennec.model.AttachmenEntry;
import dev.velaron.fennec.model.LocalPhoto;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.model.ModelsBundle;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.Types;
import dev.velaron.fennec.model.selection.LocalPhotosSelectableSource;
import dev.velaron.fennec.model.selection.Sources;
import dev.velaron.fennec.model.selection.VkPhotosSelectableSource;
import dev.velaron.fennec.mvp.presenter.MessageEditPresenter;
import dev.velaron.fennec.mvp.view.IMessageEditView;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by admin on 14.04.2017.
 * phoenix
 */
public class MessageEditFragment extends AbsPresenterBottomSheetFragment<MessageEditPresenter,
        IMessageEditView> implements IMessageEditView, AttachmentsBottomSheetAdapter.ActionListener {

    private static final int REQUEST_ADD_VKPHOTO = 17;
    private static final int REQUEST_PERMISSION_CAMERA = 16;
    private static final int REQUEST_PHOTO_FROM_CAMERA = 15;
    private static final int REQUEST_SELECT_ATTACHMENTS = 14;

    public static MessageEditFragment newInstance(int accountId, int messageOwnerId, int messageId, ModelsBundle bundle) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.MESSAGE_ID, messageId);
        args.putInt(Extra.OWNER_ID, messageOwnerId);
        args.putParcelable(Extra.BUNDLE, bundle);
        MessageEditFragment fragment = new MessageEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private AttachmentsBottomSheetAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private View mEmptyView;

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View view = View.inflate(requireActivity(), R.layout.bottom_sheet_attachments, null);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

        mEmptyView = view.findViewById(R.id.empty_root);

        view.findViewById(R.id.button_send).setOnClickListener(v -> {
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
            getDialog().dismiss();
        });

        view.findViewById(R.id.button_hide).setOnClickListener(v -> getDialog().dismiss());
        view.findViewById(R.id.button_video).setOnClickListener(v -> getPresenter().fireButtonVideoClick());
        view.findViewById(R.id.button_doc).setOnClickListener(v -> getPresenter().fireButtonDocClick());
        view.findViewById(R.id.button_camera).setOnClickListener(v -> getPresenter().fireButtonCameraClick());

        dialog.setContentView(view);
        fireViewCreated();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ADD_VKPHOTO && resultCode == Activity.RESULT_OK){
            ArrayList<Photo> vkphotos = data.getParcelableArrayListExtra(Extra.ATTACHMENTS);
            ArrayList<LocalPhoto> localPhotos = data.getParcelableArrayListExtra(Extra.PHOTOS);
            getPresenter().firePhotosSelected(vkphotos, localPhotos);
        }

        if(requestCode == REQUEST_SELECT_ATTACHMENTS && resultCode == Activity.RESULT_OK){
            ArrayList<AbsModel> attachments = data.getParcelableArrayListExtra(Extra.ATTACHMENTS);
            getPresenter().fireAttachmentsSelected(attachments);
        }

        if (requestCode == REQUEST_PHOTO_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            getPresenter().firePhotoMaked();
        }
    }

    @Override
    public IPresenterFactory<MessageEditPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            Message message = getArguments().getParcelable(Extra.MESSAGE);
            AssertUtils.requireNonNull(message);
            return new MessageEditPresenter(accountId, message, saveInstanceState);
        };
    }

    @Override
    public void displayAttachments(List<AttachmenEntry> entries) {
        if(nonNull(mRecyclerView)){
            this.mAdapter = new AttachmentsBottomSheetAdapter(requireActivity(), entries, this);
            this.mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void notifyDataAdded(int positionStart, int count) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(positionStart + 1, count);
        }
    }

    @Override
    public void addPhoto(int accountId, int ownerId) {
        Sources sources = new Sources()
                .with(new LocalPhotosSelectableSource())
                .with(new VkPhotosSelectableSource(accountId, ownerId));

        Intent intent = DualTabPhotoActivity.createIntent(requireActivity(), 10, sources);
        startActivityForResult(intent, REQUEST_ADD_VKPHOTO);
    }

    @Override
    public void notifyEntryRemoved(int index) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRemoved(index + 1);
        }
    }

    @Override
    public void displaySelectUploadPhotoSizeDialog(List<LocalPhoto> photos) {
        int[] values = {Upload.IMAGE_SIZE_800, Upload.IMAGE_SIZE_1200, Upload.IMAGE_SIZE_FULL};
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.select_image_size_title)
                .setItems(R.array.array_image_sizes_names, (dialogInterface, j)
                        -> getPresenter().fireUploadPhotoSizeSelected(photos, values[j]))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void changePercentageSmoothly(int dataPosition, int progress) {
        if(nonNull(mAdapter)){
            mAdapter.changeUploadProgress(dataPosition, progress, true);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemChanged(index + 1);
        }
    }

    @Override
    public void setEmptyViewVisible(boolean visible) {
        if(nonNull(mEmptyView)){
            mEmptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
    }

    @Override
    public void startCamera(@NonNull Uri fileUri) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(takePictureIntent, REQUEST_PHOTO_FROM_CAMERA);
        }
    }

    @Override
    public void startAddDocumentActivity(int accountId) {
        Intent intent = AttachmentsActivity.createIntent(requireActivity(), accountId, Types.DOC);
        startActivityForResult(intent, REQUEST_SELECT_ATTACHMENTS);
    }

    @Override
    public void startAddVideoActivity(int accountId, int ownerId) {
        Intent intent = VideoSelectActivity.createIntent(requireActivity(), accountId, ownerId);
        startActivityForResult(intent, REQUEST_SELECT_ATTACHMENTS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_CAMERA){
            getPresenter().fireCameraPermissionResolved();
        }
    }

    @Override
    public void onAddPhotoButtonClick() {
        getPresenter().fireAddPhotoButtonClick();
    }

    @Override
    public void onButtonRemoveClick(AttachmenEntry entry) {
        getPresenter().fireRemoveClick(entry);
    }

    @Override
    public void showError(String errorText) {
        if(isAdded()){
            Utils.showRedTopToast(requireActivity(), errorText);
        }
    }

    @Override
    public void showError(int titleTes, Object... params) {
        if(isAdded()){
            showError(getString(titleTes, params));
        }
    }
}