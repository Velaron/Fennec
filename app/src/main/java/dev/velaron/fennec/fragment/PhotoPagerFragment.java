package dev.velaron.fennec.fragment;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.activity.SendAttachmentsActivity;
import dev.velaron.fennec.domain.ILikesInteractor;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.model.Commented;
import dev.velaron.fennec.model.EditingPostType;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.PhotoSize;
import dev.velaron.fennec.model.TmpSource;
import dev.velaron.fennec.mvp.presenter.photo.FavePhotoPagerPresenter;
import dev.velaron.fennec.mvp.presenter.photo.PhotoAlbumPagerPresenter;
import dev.velaron.fennec.mvp.presenter.photo.PhotoPagerPresenter;
import dev.velaron.fennec.mvp.presenter.photo.SimplePhotoPresenter;
import dev.velaron.fennec.mvp.presenter.photo.TmpGalleryPagerPresenter;
import dev.velaron.fennec.mvp.view.IPhotoPagerView;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.place.PlaceUtil;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.view.CircleCounterButton;
import dev.velaron.fennec.view.pager.AbsImageDisplayHolder;
import dev.velaron.fennec.view.pager.AbsPagerAdapter;
import dev.velaron.fennec.view.pager.CloseOnFlingListener;
import dev.velaron.fennec.view.pager.GoBackCallback;
import dev.velaron.fennec.view.pager.WeakGoBackAnimationAdapter;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.nonEmpty;

/**
 * Created by admin on 24.09.2016.
 * phoenix
 */
public class PhotoPagerFragment extends BaseMvpFragment<PhotoPagerPresenter, IPhotoPagerView>
        implements IPhotoPagerView, GoBackCallback {

    private static final String EXTRA_FOCUS_PHOTO_ID = "focus_photo_id";

    private static final String TAG = PhotoPagerFragment.class.getSimpleName();

    private static final String EXTRA_PHOTOS = "photos";
    private static final String EXTRA_NEED_UPDATE = "need_update";
    private static final int REQUEST_PERMISSION_WRITE_STORAGE = 9020;

    private static final SparseIntArray SIZES = new SparseIntArray();

    private static final int DEFAULT_PHOTO_SIZE = PhotoSize.Z;

    static {
        SIZES.put(1, PhotoSize.X);
        SIZES.put(2, PhotoSize.Y);
        SIZES.put(3, PhotoSize.Z);
        SIZES.put(4, PhotoSize.W);
    }

    private ViewPager mViewPager;
    private CircleCounterButton mButtonLike;
    private CircleCounterButton mButtonComments;
    private ProgressBar mLoadingProgressBar;
    private Toolbar mToolbar;
    private View mButtonsRoot;
    private Button mButtonRestore;
    private Adapter mPagerAdapter;
    private boolean mCanSaveYourself;
    private boolean mCanDelete;
    private WeakGoBackAnimationAdapter mGoBackAnimationAdapter = new WeakGoBackAnimationAdapter(this);

    public static Bundle buildArgsForSimpleGallery(int aid, int index, ArrayList<Photo> photos,
                                                   boolean needUpdate) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, aid);
        args.putParcelableArrayList(EXTRA_PHOTOS, photos);
        args.putInt(Extra.INDEX, index);
        args.putBoolean(EXTRA_NEED_UPDATE, needUpdate);
        return args;
    }

    public static Bundle buildArgsForAlbum(int aid, int albumId, int ownerId, Integer focusPhotoId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, aid);
        args.putInt(Extra.OWNER_ID, ownerId);
        args.putInt(Extra.ALBUM_ID, albumId);
        if (focusPhotoId != null) {
            args.putInt(EXTRA_FOCUS_PHOTO_ID, focusPhotoId);
        }

        return args;
    }

    public static Bundle buildArgsForFave(int aid, @NonNull ArrayList<Photo> photos, int index) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, aid);
        args.putParcelableArrayList(EXTRA_PHOTOS, photos);
        args.putInt(Extra.INDEX, index);
        return args;
    }

    public static PhotoPagerFragment newInstance(int placeType, Bundle args) {
        Bundle targetArgs = new Bundle();
        targetArgs.putAll(args);
        targetArgs.putInt(Extra.PLACE_TYPE, placeType);
        PhotoPagerFragment fragment = new PhotoPagerFragment();
        fragment.setArguments(targetArgs);
        return fragment;
    }

    private static void addPhotoSizeToMenu(PopupMenu menu, int id, int size, int selectedItem) {
        menu.getMenu()
                .add(0, id, 0, getTitleForPhotoSize(size))
                .setChecked(selectedItem == size);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_photo_pager_new, container, false);

        mLoadingProgressBar = root.findViewById(R.id.loading_progress_bar);

        mButtonRestore = root.findViewById(R.id.button_restore);
        mButtonsRoot = root.findViewById(R.id.buttons);
        mToolbar = root.findViewById(R.id.toolbar);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(mToolbar);

        mViewPager = root.findViewById(R.id.view_pager);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getPresenter().firePageSelected(position);
            }
        });

        mButtonLike = root.findViewById(R.id.like_button);
        mButtonLike.setOnClickListener(v -> getPresenter().fireLikeClick());

        mButtonLike.setOnLongClickListener(v -> {
            getPresenter().fireLikeLongClick();
            return false;
        });

        mButtonComments = root.findViewById(R.id.comments_button);
        mButtonComments.setOnClickListener(v -> getPresenter().fireCommentsButtonClick());

        CircleCounterButton buttonShare = root.findViewById(R.id.share_button);
        buttonShare.setOnClickListener(v -> getPresenter().fireShareButtonClick());

        mButtonRestore.setOnClickListener(v -> getPresenter().fireButtonRestoreClick());

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.vkphoto_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.photo_size:
                onPhotoSizeClicked();
                break;
            case R.id.save_on_drive:
                if (isPresenterPrepared()) getPresenter().fireSaveOnDriveClick();
                return true;
            case R.id.save_yourself:
                if (isPresenterPrepared()) getPresenter().fireSaveYourselfClick();
                break;
            case R.id.action_delete:
                if (isPresenterPrepared()) getPresenter().fireDeleteClick();
                break;
            case R.id.info:
                if (isPresenterPrepared()) getPresenter().fireInfoButtonClick();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void goToLikesList(int accountId, int ownerId, int photoId) {
        PlaceFactory.getLikesCopiesPlace(accountId, "photo", ownerId, photoId, ILikesInteractor.FILTER_LIKES).tryOpenWith(requireActivity());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.save_yourself).setVisible(mCanSaveYourself);
        menu.findItem(R.id.action_delete).setVisible(mCanDelete);

        int imageSize = getPhotoSizeFromPrefs();
        menu.findItem(R.id.photo_size).setTitle(getTitleForPhotoSize(imageSize));
    }

    private void onPhotoSizeClicked() {
        View view = requireActivity().findViewById(R.id.photo_size);

        int current = getPhotoSizeFromPrefs();

        PopupMenu popupMenu = new PopupMenu(requireActivity(), view);
        for (int i = 0; i < SIZES.size(); i++) {
            int key = SIZES.keyAt(i);
            int value = SIZES.get(key);

            addPhotoSizeToMenu(popupMenu, key, value, current);
        }

        popupMenu.getMenu().setGroupCheckable(0, true, true);

        popupMenu.setOnMenuItemClickListener(item -> {
            int key = item.getItemId();

            //noinspection ResourceType
            Settings.get()
                    .main()
                    .setPrefDisplayImageSize(SIZES.get(key));

            onImageDisplayedImageSizeChanged();
            requireActivity().invalidateOptionsMenu();
            return true;
        });

        popupMenu.show();
    }

    private static String getTitleForPhotoSize(int size) {
        switch (size) {
            case PhotoSize.X:
                return 604 + "px";
            case PhotoSize.Y:
                return 807 + "px";
            case PhotoSize.Z:
                return 1024 + "px";
            case PhotoSize.W:
                return 2048 + "px";
            default:
                throw new IllegalArgumentException("Unsupported size");
        }
    }

    private void onImageDisplayedImageSizeChanged() {
        if (nonNull(mPagerAdapter)) {
            mPagerAdapter.rebindHolders();
        }
    }

    @Override
    public void displayAccountNotSupported() {

    }

    @Override
    public IPresenterFactory<PhotoPagerPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int placeType = requireArguments().getInt(Extra.PLACE_TYPE);
            int aid = requireArguments().getInt(Extra.ACCOUNT_ID);

            switch (placeType) {
                case Place.SIMPLE_PHOTO_GALLERY:
                    int index = requireArguments().getInt(Extra.INDEX);
                    boolean needUpdate = requireArguments().getBoolean(EXTRA_NEED_UPDATE);
                    ArrayList<Photo> photos = requireArguments().getParcelableArrayList(EXTRA_PHOTOS);
                    AssertUtils.requireNonNull(photos);
                    return new SimplePhotoPresenter(photos, index, needUpdate, aid, saveInstanceState);

                case Place.VK_PHOTO_ALBUM_GALLERY:
                    int ownerId = requireArguments().getInt(Extra.OWNER_ID);
                    int albumId = requireArguments().getInt(Extra.ALBUM_ID);
                    Integer focusTo = requireArguments().containsKey(EXTRA_FOCUS_PHOTO_ID)
                            ? requireArguments().getInt(EXTRA_FOCUS_PHOTO_ID) : null;
                    return new PhotoAlbumPagerPresenter(aid, ownerId, albumId, focusTo, saveInstanceState);

                case Place.FAVE_PHOTOS_GALLERY:
                    int findex = requireArguments().getInt(Extra.INDEX);
                    ArrayList<Photo> favePhotos = requireArguments().getParcelableArrayList(EXTRA_PHOTOS);
                    AssertUtils.requireNonNull(favePhotos);
                    return new FavePhotoPagerPresenter(favePhotos, findex, aid, saveInstanceState);

                case Place.VK_PHOTO_TMP_SOURCE:
                    TmpSource source = requireArguments().getParcelable(Extra.SOURCE);
                    AssertUtils.requireNonNull(source);
                    return new TmpGalleryPagerPresenter(aid, source, requireArguments().getInt(Extra.INDEX), saveInstanceState);
            }

            throw new UnsupportedOperationException();
        };
    }

    @Override
    public void setupLikeButton(boolean like, int likes) {
        if (nonNull(mButtonLike)) {
            mButtonLike.setActive(like);
            mButtonLike.setCount(likes);
            mButtonLike.setIcon(like ? R.drawable.heart_filled : R.drawable.heart);
        }
    }

    @Override
    public void setupCommentsButton(boolean visible, int count) {
        if (nonNull(mButtonComments)) {
            mButtonComments.setVisibility(visible ? View.VISIBLE : View.GONE);
            mButtonComments.setCount(count);
        }
    }

    @Override
    public void displayPhotos(@NonNull List<Photo> photos, int initialIndex) {
        if (nonNull(mViewPager)) {
            mPagerAdapter = new Adapter(photos);
            mViewPager.setAdapter(mPagerAdapter);
            mViewPager.setCurrentItem(initialIndex);
        }
    }

    @Override
    public void setToolbarTitle(String title) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setSubtitle(subtitle);
        }
    }

    @Override
    public void sharePhoto(int accountId, @NonNull Photo photo) {
        String[] items = new String[]{
                getString(R.string.share_link),
                getString(R.string.repost_send_message),
                getString(R.string.repost_to_wall)
        };

        new AlertDialog.Builder(requireActivity())
                .setItems(items, (dialogInterface, i) -> {
                    switch (i) {
                        case 0:
                            Utils.shareLink(requireActivity(), photo.generateWebLink(), photo.getText());
                            break;
                        case 1:
                            SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, photo);
                            break;
                        case 2:
                            getPresenter().firePostToMyWallClick();
                            break;
                    }
                })
                .setCancelable(true)
                .setTitle(R.string.share_photo_title)
                .show();
    }

    @Override
    public void postToMyWall(@NonNull Photo photo, int accountId) {
        PlaceUtil.goToPostCreation(requireActivity(), accountId, accountId, EditingPostType.TEMP, Collections.singletonList(photo));
    }

    @Override
    public void requestWriteToExternalStoragePermission() {
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_WRITE_STORAGE);
    }

    @Override
    public void setButtonRestoreVisible(boolean visible) {
        if (nonNull(mButtonRestore)) {
            mButtonRestore.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setupOptionMenu(boolean canSaveYourself, boolean canDelete) {
        mCanSaveYourself = canSaveYourself;
        mCanDelete = canDelete;
        requireActivity().invalidateOptionsMenu();
    }

    @Override
    public void goToComments(int aid, @NonNull Commented commented) {
        PlaceFactory.getCommentsPlace(aid, commented, null).tryOpenWith(requireActivity());
    }

    @Override
    public void displayPhotoListLoading(boolean loading) {
        if (nonNull(mLoadingProgressBar)) {
            mLoadingProgressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setButtonsBarVisible(boolean visible) {
        if (nonNull(mButtonsRoot)) {
            mButtonsRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setToolbarVisible(boolean visible) {
        if (nonNull(mToolbar)) {
            mToolbar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void rebindPhotoAt(int position) {
        if (nonNull(mPagerAdapter)) {
            mPagerAdapter.rebindHolderAt(position);
        }
    }

    @Override
    public void showPhotoInfo(String time, String info) {
        new AlertDialog.Builder(requireActivity())
                .setTitle("Uploaded: " + time)
                .setMessage(info)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_WRITE_STORAGE && isPresenterPrepared()) {
            getPresenter().fireWriteExternalStoragePermissionResolved();
        }
    }

    @Override
    public void onDestroyView() {
        if (nonNull(mPagerAdapter)) {
            mPagerAdapter.release();
        }

        super.onDestroyView();
    }

    @Override
    public void goBack() {
        if (isAdded() && canGoBack()) {
            requireActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(false, false)
                .build()
                .apply(requireActivity());
    }

    private boolean canGoBack() {
        return requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 1;
    }

    @PhotoSize
    public int getPhotoSizeFromPrefs() {
        return Settings.get()
                .main()
                .getPrefDisplayImageSize(DEFAULT_PHOTO_SIZE);
    }

    private class Holder extends AbsImageDisplayHolder implements Callback {

        Holder(int adapterPosition, View root) {
            super(adapterPosition, root);
            // TODO: 10.01.2017 каким образом root.getContext() получается NULL ?

            mPhotoView.setOnSingleFlingListener(new CloseOnFlingListener(root.getContext()) {
                @Override
                public boolean onVerticalFling(float distanceByY) {
                    if (canGoBack()) {
                        animateImageViewAndGoBack(distanceByY);
                        return true;
                    }

                    return false;
                }
            });

            mPhotoView.setOnPhotoTapListener((view, x, y) -> callPresenter(PhotoPagerPresenter::firePhotoTap));
        }

        private void animateImageViewAndGoBack(float distance) {
            ObjectAnimator objectAnimatorPosition = ObjectAnimator.ofFloat(mItemView, "translationY", -distance);
            ObjectAnimator objectAnimatorAlpha = ObjectAnimator.ofFloat(mItemView, View.ALPHA, 1, 0);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(objectAnimatorPosition, objectAnimatorAlpha);
            animatorSet.setDuration(200);
            animatorSet.addListener(mGoBackAnimationAdapter);
            animatorSet.start();
        }

        void bindTo(@NonNull Photo photo) {
            int size = getPhotoSizeFromPrefs();

            String url = photo.getUrlForSize(size, true);

            if (nonEmpty(url)) {
                loadImage(url);
            } else {
                // waiting for image valid URL
                //showError(R.string.image_load_error);
            }
        }

        @Override
        protected int idOfImageView() {
            return R.id.image_view;
        }

        @Override
        protected int idOfProgressBar() {
            return R.id.progress_bar;
        }
    }

    private class Adapter extends AbsPagerAdapter<Holder> {

        final List<Photo> mPhotos;

        Adapter(List<Photo> data) {
            mPhotos = data;
        }

        @Override
        public int getCount() {
            return mPhotos.size();
        }

        @Override
        protected Holder createHolder(int adapterPosition, ViewGroup container) {
            return new Holder(adapterPosition, LayoutInflater.from(container.getContext())
                    .inflate(R.layout.content_photo_page, container, false));
        }

        @Override
        protected void bindHolder(@NonNull Holder holder, int position) {
            Photo photo = mPhotos.get(position);
            holder.bindTo(photo);
        }
    }
}