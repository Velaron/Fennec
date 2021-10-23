package dev.velaron.fennec.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.PhotosActivity;
import dev.velaron.fennec.adapter.BigVkPhotosAdapter;
import dev.velaron.fennec.dialog.ImageSizeAlertDialog;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.listener.PicassoPauseOnScrollListener;
import dev.velaron.fennec.model.LocalPhoto;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.ParcelableOwnerWrapper;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.PhotoAlbum;
import dev.velaron.fennec.model.wrappers.SelectablePhotoWrapper;
import dev.velaron.fennec.mvp.presenter.VkPhotosPresenter;
import dev.velaron.fennec.mvp.view.IVkPhotosView;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.upload.Upload;
import dev.velaron.fennec.util.AppPerms;
import dev.velaron.fennec.util.ViewUtils;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.nonEmpty;

public class VKPhotosFragment extends BaseMvpFragment<VkPhotosPresenter, IVkPhotosView>
        implements BigVkPhotosAdapter.PhotosActionListener, BigVkPhotosAdapter.UploadActionListener, IVkPhotosView {

    private static final String TAG = VKPhotosFragment.class.getSimpleName();
    private static final int REQUEST_PERMISSION_READ_EXTARNAL_STORAGE = 14;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private BigVkPhotosAdapter mAdapter;
    private TextView mEmptyText;
    private FloatingActionButton mFab;
    private String mAction;

    public static Bundle buildArgs(int accountId, int ownerId, int albumId, String action) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putInt(Extra.ALBUM_ID, albumId);
        args.putString(Extra.ACTION, action);
        return args;
    }

    public static VKPhotosFragment newInstance(Bundle args) {
        VKPhotosFragment fragment = new VKPhotosFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static VKPhotosFragment newInstance(int accountId, int ownerId, int albumId, String action) {
        return newInstance(buildArgs(accountId, ownerId, albumId, action));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAction = requireArguments().getString(Extra.ACTION, ACTION_SHOW_PHOTOS);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        int columnCount = getResources().getInteger(R.integer.local_gallery_column_count);
        RecyclerView.LayoutManager manager = new GridLayoutManager(requireActivity(), columnCount);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout, true);

        RecyclerView mRecyclerView = root.findViewById(R.id.list);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(TAG));
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mEmptyText = root.findViewById(R.id.empty);

        mFab = root.findViewById(R.id.fr_photo_gallery_attach);
        mFab.setOnClickListener(v -> onFabClicked());

        mAdapter = new BigVkPhotosAdapter(requireActivity(), Collections.emptyList(), Collections.emptyList(), TAG);
        mAdapter.setPhotosActionListener(this);
        mAdapter.setUploadActionListener(this);
        mRecyclerView.setAdapter(mAdapter);
        return root;
    }

    private void resolveEmptyTextVisibility() {
        if (nonNull(mEmptyText) && nonNull(mAdapter)) {
            mEmptyText.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    private void resolveFabVisibility(boolean anim, boolean show) {
        if (!isAdded() || mFab == null) return;

        if (mFab.isShown() && !show) {
            mFab.hide();
        }

        if (!mFab.isShown() && show) {
            mFab.show();
        }
    }

    private void onFabClicked() {
        if(isSelectionMode()){
            getPresenter().fireSelectionCommitClick();
        } else {
            getPresenter().fireAddPhotosClick();
        }
    }

    private boolean isSelectionMode() {
        return ACTION_SELECT_PHOTOS.equals(mAction);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_UPLOAD_LOCAL_PHOTO && resultCode == Activity.RESULT_OK) {
            ArrayList<LocalPhoto> photos = data.getParcelableArrayListExtra(Extra.PHOTOS);

            if (nonEmpty(photos)) {
                onPhotosForUploadSelected(photos);
            }
        }
    }

    private void onPhotosForUploadSelected(@NonNull final List<LocalPhoto> photos) {
        ImageSizeAlertDialog.showUploadPhotoSizeIfNeed(requireActivity(), size -> doUploadPhotosToAlbum(photos, size));
    }

    private void doUploadPhotosToAlbum(@NonNull List<LocalPhoto> photos, int size) {
        getPresenter().firePhotosForUploadSelected(photos, size);
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onDestroyView() {
        mAdapter.cleanup();
        super.onDestroyView();
    }

    @Override
    public void onPhotoClick(BigVkPhotosAdapter.PhotoViewHolder holder, SelectablePhotoWrapper wrapper) {
        if(isSelectionMode()){
            getPresenter().firePhotoSelectionChanged(wrapper);
            mAdapter.updatePhotoHoldersSelectionAndIndexes();
        } else {
            getPresenter().firePhotoClick(wrapper);
        }
    }

    @Override
    public void onUploadRemoveClicked(Upload upload) {
        getPresenter().fireUploadRemoveClick(upload);
    }

    @Override
    public void displayData(List<SelectablePhotoWrapper> photos, List<Upload> uploads) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(BigVkPhotosAdapter.DATA_TYPE_UPLOAD, uploads);
            mAdapter.setData(BigVkPhotosAdapter.DATA_TYPE_PHOTO, photos);
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyPhotosAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count, BigVkPhotosAdapter.DATA_TYPE_PHOTO);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void notifyUploadAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count, BigVkPhotosAdapter.DATA_TYPE_UPLOAD);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyUploadRemoved(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(index, BigVkPhotosAdapter.DATA_TYPE_UPLOAD);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void setButtonAddVisible(boolean visible, boolean anim) {
        if (nonNull(mFab)) {
            resolveFabVisibility(anim, visible);
        }
    }

    @Override
    public void notifyUploadItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(index, BigVkPhotosAdapter.DATA_TYPE_UPLOAD);
        }
    }

    @Override
    public void notifyUploadProgressChanged(int id, int progress) {
        if (nonNull(mAdapter)) {
            mAdapter.updateUploadHoldersProgress(id, true, progress);
        }
    }

    @Override
    public void displayGallery(int accountId, int albumId, int ownerId, Integer focusToId) {
        PlaceFactory.getPhotoAlbumGalleryPlace(accountId, albumId, ownerId, focusToId).tryOpenWith(requireActivity());
    }

    @Override
    public void displayDefaultToolbarTitle() {
        super.setToolbarTitle(getString(R.string.photos));
    }

    @Override
    public void setDrawerPhotosSelected(boolean selected) {
        if (requireActivity() instanceof OnSectionResumeCallback) {
            if (selected) {
                ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_PHOTOS);
            } else {
                ((OnSectionResumeCallback) requireActivity()).onClearSelection();
            }
        }
    }

    @Override
    public void returnSelectionToParent(List<Photo> selected) {
        Intent intent = new Intent();
        intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, new ArrayList<>(selected));
        requireActivity().setResult(Activity.RESULT_OK, intent);
        requireActivity().finish();
    }

    @Override
    public void showSelectPhotosToast() {
        Toast.makeText(requireActivity(), getString(R.string.select_attachments), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void startLocalPhotosSelection() {
        if (!AppPerms.hasReadStoragePermision(requireActivity())) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION_READ_EXTARNAL_STORAGE);
            return;
        }

        startLocalPhotosSelectionActibity();
    }

    private static final int REQUEST_UPLOAD_LOCAL_PHOTO = 121;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(REQUEST_PERMISSION_READ_EXTARNAL_STORAGE == requestCode){
            getPresenter().fireReadStoragePermissionChanged();
        }
    }

    private void startLocalPhotosSelectionActibity(){
        Intent intent = new Intent(requireActivity(), PhotosActivity.class);
        intent.putExtra(PhotosActivity.EXTRA_MAX_SELECTION_COUNT, Integer.MAX_VALUE);
        startActivityForResult(intent, REQUEST_UPLOAD_LOCAL_PHOTO);
    }

    @Override
    public void startLocalPhotosSelectionIfHasPermission() {
        if (AppPerms.hasReadStoragePermision(requireActivity())) {
            startLocalPhotosSelectionActibity();
        }
    }

    @Override
    public IPresenterFactory<VkPhotosPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            ParcelableOwnerWrapper ownerWrapper = requireArguments().getParcelable(Extra.OWNER);
            Owner owner = nonNull(ownerWrapper) ? ownerWrapper.get() : null;
            PhotoAlbum album = requireArguments().getParcelable(Extra.ALBUM);

            return new VkPhotosPresenter(
                    requireArguments().getInt(Extra.ACCOUNT_ID),
                    requireArguments().getInt(Extra.OWNER_ID),
                    requireArguments().getInt(Extra.ALBUM_ID),
                    requireArguments().getString(Extra.ACTION, ACTION_SHOW_PHOTOS),
                    owner,
                    album,
                    saveInstanceState
            );
        };
    }
}