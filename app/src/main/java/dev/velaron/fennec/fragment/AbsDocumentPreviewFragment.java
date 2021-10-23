package dev.velaron.fennec.fragment;

import android.Manifest;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Collections;

import androidx.annotation.NonNull;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.SendAttachmentsActivity;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.model.EditingPostType;
import dev.velaron.fennec.mvp.presenter.BaseDocumentPresenter;
import dev.velaron.fennec.mvp.view.IBasicDocumentView;
import dev.velaron.fennec.place.PlaceUtil;
import dev.velaron.fennec.util.Utils;

/**
 * Created by ruslan.kolbasa on 11.10.2016.
 * phoenix
 */
public abstract class AbsDocumentPreviewFragment<P extends BaseDocumentPresenter<V>, V
        extends IBasicDocumentView> extends BaseMvpFragment<P, V> implements IBasicDocumentView {

    private static final int REQUEST_WRITE_PERMISSION = 160;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_WRITE_PERMISSION && isPresenterPrepared()){
            getPresenter().fireWritePermissionResolved();
        }
    }

    @Override
    public void requestWriteExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
    }

    @Override
    public void shareDocument(int accountId, @NonNull Document document) {
        String[] items = new String[]{
                getString(R.string.share_link),
                getString(R.string.repost_send_message),
                getString(R.string.repost_to_wall)
        };

        new MaterialAlertDialogBuilder(requireActivity())
                .setItems(items, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            Utils.shareLink(requireActivity(), document.generateWebLink(), document.getTitle());
                            break;
                        case 1:
                            SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, document);
                            break;
                        case 2:
                            PlaceUtil.goToPostCreation(requireActivity(), accountId, accountId, EditingPostType.TEMP, Collections.singletonList(document));
                            break;
                    }
                })
                .setCancelable(true)
                .setTitle(R.string.share_document_title)
                .show();
    }
}