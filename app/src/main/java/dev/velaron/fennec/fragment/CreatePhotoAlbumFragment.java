package dev.velaron.fennec.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.fragment.base.BaseMvpFragment;
import dev.velaron.fennec.listener.BackPressCallback;
import dev.velaron.fennec.model.PhotoAlbum;
import dev.velaron.fennec.model.PhotoAlbumEditor;
import dev.velaron.fennec.mvp.presenter.EditPhotoAlbumPresenter;
import dev.velaron.fennec.mvp.view.IEditPhotoAlbumView;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.view.steppers.base.AbsStepHolder;
import dev.velaron.fennec.view.steppers.base.AbsSteppersVerticalAdapter;
import dev.velaron.fennec.view.steppers.base.BaseHolderListener;
import dev.velaron.fennec.view.steppers.impl.CreatePhotoAlbumStep1Holder;
import dev.velaron.fennec.view.steppers.impl.CreatePhotoAlbumStep2Holder;
import dev.velaron.fennec.view.steppers.impl.CreatePhotoAlbumStep3Holder;
import dev.velaron.fennec.view.steppers.impl.CreatePhotoAlbumStep4Holder;
import dev.velaron.fennec.view.steppers.impl.CreatePhotoAlbumStepsHost;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

public class CreatePhotoAlbumFragment extends BaseMvpFragment<EditPhotoAlbumPresenter, IEditPhotoAlbumView>
        implements BackPressCallback, BaseHolderListener, IEditPhotoAlbumView, CreatePhotoAlbumStep4Holder.ActionListener, CreatePhotoAlbumStep3Holder.ActionListener, CreatePhotoAlbumStep2Holder.ActionListener, CreatePhotoAlbumStep1Holder.ActionListener {

    private static final String TAG = CreatePhotoAlbumFragment.class.getSimpleName();
    private static final String EXTRA_EDITOR = "editor";

    private static final int REQUEST_PRIVACY_VIEW = 113;
    private static final int REQUEST_PRIVACY_COMMENT = 135;
    private static final int REQUEST_CREATE = 136;
    private static final int REUQEST_EDIT = 137;

    private RecyclerView mRecyclerView;
    private AbsSteppersVerticalAdapter<CreatePhotoAlbumStepsHost> mAdapter;

    public static Bundle buildArgsForEdit(int aid, @NonNull PhotoAlbum album, @NonNull PhotoAlbumEditor editor) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_EDITOR, editor);
        bundle.putParcelable(Extra.ALBUM, album);
        bundle.putInt(Extra.ACCOUNT_ID, aid);
        return bundle;
    }

    public static Bundle buildArgsForCreate(int aid, int ownerId) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.OWNER_ID, ownerId);
        bundle.putInt(Extra.ACCOUNT_ID, aid);
        return bundle;
    }

    public static CreatePhotoAlbumFragment newInstance(Bundle args) {
        CreatePhotoAlbumFragment fragment = new CreatePhotoAlbumFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_create_photo_album, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mRecyclerView = root.findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        return root;
    }

    /*@Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PRIVACY_VIEW && resultCode == Activity.RESULT_OK && data != null) {
            Privacy privacy = data.getParcelableExtra(Extra.PRIVACY);
            AssertUtils.requireNonNull(privacy);

            mStepsHost.getState().setPrivacyView(privacy);
            mAdapter.notifyItemChanged(CreatePhotoAlbumStepsHost.STEP_PRIVACY_VIEW);
        }

        if (requestCode == REQUEST_PRIVACY_COMMENT && resultCode == Activity.RESULT_OK && data != null) {
            Privacy privacy = data.getParcelableExtra(Extra.PRIVACY);
            AssertUtils.requireNonNull(privacy);

            mStepsHost.getState().setPrivacyComment(privacy);
            mAdapter.notifyItemChanged(CreatePhotoAlbumStepsHost.STEP_PRIVACY_COMMENT);
        }

        if (requestCode == REUQEST_EDIT || requestCode == REQUEST_CREATE) {
            if (resultCode == Activity.RESULT_CANCELED) {
                String error = data.getStringExtra(Extra.ERROR);
                safeSnackbar(error);
                return;
            }

            Bundle resultData = data.getBundleExtra(Extra.RESULT_DATA);
            VKApiPhotoAlbum album = resultData.getParcelable(Extra.ALBUM);
            AssertUtils.requireNonNull(resultData);
            AssertUtils.requireNonNull(album);

            if (requestCode == REQUEST_CREATE) {
                onPhotoAlbumCreated(album);
            }

            if (requestCode == REUQEST_EDIT) {
                onPhotoAlbumEdited(album);
            }
        }
    }*/

    private void onPhotoAlbumEdited(PhotoAlbum album) {
        if (getTargetFragment() != null) {
            Intent data = new Intent();
            data.putExtra(Extra.ALBUM, album);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        }

        goBack();
    }

    @Override
    public void updateStepView(int step) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.notifyItemChanged(step);
        }
    }

    @Override
    public void moveSteppers(int old, int current) {
        if(Objects.nonNull(mRecyclerView) && Objects.nonNull(mAdapter)){
            mRecyclerView.scrollToPosition(current);
            mAdapter.notifyItemChanged(old);
            mAdapter.notifyItemChanged(current);
        }
    }

    @Override
    public void goBack() {
        requireActivity().onBackPressed();
    }

    @Override
    public void hideKeyboard() {
        ActivityUtils.hideSoftKeyboard(requireActivity());
    }

    @Override
    public void updateStepButtonsAvailability(int step) {
        if(Objects.nonNull(mAdapter)){
            mAdapter.updateNextButtonAvailability(step);
        }
    }

    @Override
    public void attachSteppersHost(@NonNull CreatePhotoAlbumStepsHost host) {
        mAdapter = new AbsSteppersVerticalAdapter<CreatePhotoAlbumStepsHost>(host, this) {
            @Override
            public AbsStepHolder<CreatePhotoAlbumStepsHost> createHolderForStep(ViewGroup parent, CreatePhotoAlbumStepsHost host, int step) {
                return createHolder(step, parent);
            }
        };

        mRecyclerView.setAdapter(mAdapter);
    }

    private AbsStepHolder<CreatePhotoAlbumStepsHost> createHolder(int step, ViewGroup parent) {
        switch (step) {
            case CreatePhotoAlbumStepsHost.STEP_TITLE_AND_DESCRIPTION:
                return new CreatePhotoAlbumStep1Holder(parent, this);
            case CreatePhotoAlbumStepsHost.STEP_UPLOAD_AND_COMMENTS:
                return new CreatePhotoAlbumStep2Holder(parent, this);
            case CreatePhotoAlbumStepsHost.STEP_PRIVACY_VIEW:
                return new CreatePhotoAlbumStep3Holder(parent, this);
            case CreatePhotoAlbumStepsHost.STEP_PRIVACY_COMMENT:
                return new CreatePhotoAlbumStep4Holder(parent, this);

            default:
                throw new IllegalArgumentException("Inavalid step index: " + step);
        }
    }

    @Override
    public boolean onBackPressed() {
        return getPresenter().fireBackButtonClick();
    }

    /*private void doRequest() {
        CreatePhotoAlbumStepsHost.PhotoAlbumState state = mStepsHost.getState();

        String title = state.getTitle();
        String desc = state.getDescription();
        Integer groupId = mOwnerId < 0 ? Math.abs(mOwnerId) : null;

        SimplePrivacy privacyView = state.getPrivacyView().toSimple();
        SimplePrivacy privacyComemnt = state.getPrivacyComment().toSimple();

        Request request;
        String dialogTitle;
        int requestCode;

        if (!isEditing()) {
            dialogTitle = getString(R.string.create_an_album);
            requestCode = REQUEST_CREATE;

            request = PhotoRequestFactory.getCreateAlbumRequest(title, groupId, desc, privacyView,
                    privacyComemnt, state.isUploadByAdminsOnly(), state.isCommentsDisabled());
        } else {
            dialogTitle = getString(R.string.album_edit);
            requestCode = REUQEST_EDIT;

            request = PhotoRequestFactory.getEditAlbumRequest(mAlbum.getId(), mAlbum.getOwnerId(), title, desc, privacyView,
                    privacyComemnt, state.isUploadByAdminsOnly(), state.isCommentsDisabled());
        }

        ProgressDialogFragment.newInstance(dialogTitle, title, request, this, requestCode, false)
                .show(getFragmentManager(), "progress");
    }*/

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.create_album);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    /*private void onPhotoAlbumCreated(@NonNull VKApiPhotoAlbum album) {
        if (getTargetFragment() != null) {
            Intent data = new Intent();
            data.putExtra(Extra.ALBUM, album);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
        }

        goBackWithResult();

        PlaceFactory.getVKPhotosAlbumPlace(getAccountId(), album.owner_id, album.id,
                VKPhotosFragment.ACTION_SHOW_PHOTOS)
                .withParcelableExtra(Extra.ALBUM, album)
                .tryOpenWith(requireActivity());
    }*/

    /*@Override
    public void onPrivacyViewClick() {
        Logger.d(TAG, "onPrivacyViewClick");

        PlaceFactory.getPrivacyViewPlace(getAccountId(), mStepsHost.getState().getPrivacyView())
                .targetTo(this, REQUEST_PRIVACY_VIEW)
                .tryOpenWith(getContext());
    }

    @Override
    public void onPrivacyCommentClick() {
        PlaceFactory.getPrivacyViewPlace(getAccountId(), mStepsHost.getState().getPrivacyComment())
                .targetTo(this, REQUEST_PRIVACY_COMMENT)
                .tryOpenWith(getContext());
    }*/

    /*private boolean isEditing() {
        return mAlbum != null;
    }*/

    @Override
    public void onNextButtonClick(int step) {
        getPresenter().fireStepPositiveButtonClick(step);
    }

    @Override
    public void onCancelButtonClick(int step) {
        getPresenter().fireStepNegativeButtonClick(step);
    }

    @Override
    public IPresenterFactory<EditPhotoAlbumPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);

            if (getArguments().containsKey(Extra.ALBUM)) {
                PhotoAlbum abum = getArguments().getParcelable(Extra.ALBUM);
                PhotoAlbumEditor editor = getArguments().getParcelable(EXTRA_EDITOR);
                AssertUtils.requireNonNull(abum);
                AssertUtils.requireNonNull(editor);
                return new EditPhotoAlbumPresenter(accountId, abum, editor, saveInstanceState);
            } else {
                int ownerId = getArguments().getInt(Extra.OWNER_ID);
                return new EditPhotoAlbumPresenter(accountId, ownerId, saveInstanceState);
            }
        };
    }

    @Override
    public void onPrivacyCommentClick() {
        getPresenter().firePrivacyCommentClick();
    }

    @Override
    public void onPrivacyViewClick() {
        getPresenter().firePrivacyViewClick();
    }

    @Override
    public void onUploadByAdminsOnlyChecked(boolean checked) {
        getPresenter().fireUploadByAdminsOnlyChecked(checked);
    }

    @Override
    public void onCommentsDisableChecked(boolean checked) {
        getPresenter().fireDisableCommentsClick(checked);
    }

    @Override
    public void onTitleEdited(CharSequence text) {
        getPresenter().fireTitleEdit(text);
    }

    @Override
    public void onDescriptionEdited(CharSequence text) {

    }
}