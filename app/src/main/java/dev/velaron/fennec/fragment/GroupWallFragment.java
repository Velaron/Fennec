package dev.velaron.fennec.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Constants;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.activity.LoginActivity;
import dev.velaron.fennec.adapter.horizontal.HorizontalOptionsAdapter;
import dev.velaron.fennec.api.PicassoInstance;
import dev.velaron.fennec.fragment.search.SearchContentType;
import dev.velaron.fennec.fragment.search.criteria.PeopleSearchCriteria;
import dev.velaron.fennec.model.Community;
import dev.velaron.fennec.model.GroupSettings;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.ParcelableOwnerWrapper;
import dev.velaron.fennec.model.PostFilter;
import dev.velaron.fennec.model.Token;
import dev.velaron.fennec.mvp.presenter.DocsListPresenter;
import dev.velaron.fennec.mvp.presenter.GroupWallPresenter;
import dev.velaron.fennec.mvp.view.IGroupWallView;
import dev.velaron.fennec.place.PlaceFactory;
import dev.velaron.fennec.util.AssertUtils;
import dev.velaron.fennec.util.RoundTransformation;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.nonEmpty;

/**
 * Created by admin on 23.01.2017.
 * phoenix
 */
public class GroupWallFragment extends AbsWallFragment<IGroupWallView, GroupWallPresenter> implements IGroupWallView {

    private static final int REQUEST_LOGIN_COMMUNITY = 14;
    private GroupHeaderHolder mHeaderHolder;

    @Override
    public void displayBaseCommunityData(Community community) {
        if(isNull(mHeaderHolder)) return;

        mHeaderHolder.tvName.setText(community.getFullName());

        String screenName = nonEmpty(community.getScreenName()) ? "@" + community.getScreenName() : null;
        mHeaderHolder.tvScreenName.setText(screenName);

        String photoUrl = community.getMaxSquareAvatar();
        if (nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl).transform(new RoundTransformation())
                    .into(mHeaderHolder.ivAvatar);
        }
    }

    @Override
    public void displayCommunityCover(boolean enabled, String resource) {
        if (enabled) {
            PicassoInstance.with()
                    .load(resource)
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                            mHeaderHolder.vgCover.setBackground(drawable);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            //do nothing
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                            //do nothing
                        }
                    });
        }
    }

    @Override
    public IPresenterFactory<GroupWallPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int ownerId = requireArguments().getInt(Extra.OWNER_ID);

            ParcelableOwnerWrapper wrapper = requireArguments().getParcelable(Extra.OWNER);
            AssertUtils.requireNonNull(wrapper);

            return new GroupWallPresenter(accountId, ownerId, (Community) wrapper.get(), saveInstanceState);
        };
    }

    @Override
    protected int headerLayout() {
        return R.layout.header_group;
    }

    @Override
    protected void onHeaderInflated(View headerRootView) {
        mHeaderHolder = new GroupHeaderHolder(headerRootView);
    }

    @Override
    public void setupPrimaryButton(@StringRes Integer title) {
        if(nonNull(mHeaderHolder)){
            if(nonNull(title)){
                mHeaderHolder.primaryActionButton.setText(title);
                mHeaderHolder.primaryActionButton.setVisibility(View.VISIBLE);
            } else {
                mHeaderHolder.primaryActionButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setupSecondaryButton(@StringRes Integer title) {
        if(nonNull(mHeaderHolder)){
            if(nonNull(title)){
                mHeaderHolder.secondaryActionButton.setText(title);
                mHeaderHolder.secondaryActionButton.setVisibility(View.VISIBLE);
            } else {
                mHeaderHolder.secondaryActionButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void openTopics(int accoundId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getTopicsPlace(accoundId, ownerId)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void openCommunityMembers(int accoundId, int groupId) {
        PeopleSearchCriteria criteria = new PeopleSearchCriteria("")
                .setGroupId(groupId);

        PlaceFactory.getSingleTabSearchPlace(accoundId, SearchContentType.PEOPLE, criteria).tryOpenWith(requireActivity());
    }

    @Override
    public void openDocuments(int accoundId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getDocumentsPlace(accoundId, ownerId, DocsListPresenter.ACTION_SHOW)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void displayWallFilters(List<PostFilter> filters) {
        if(nonNull(mHeaderHolder)){
            mHeaderHolder.mFiltersAdapter.setItems(filters);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_community_wall, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_community_control){
            getPresenter().fireCommunityControlClick();
            return true;
        }

        if(item.getItemId() == R.id.action_community_messages){
            getPresenter().fireCommunityMessagesClick();
            return true;
        }

        if(item.getItemId() == R.id.action_add_to_bookmarks){
            getPresenter().fireAddToBookmarksClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void notifyWallFiltersChanged() {
        if(nonNull(mHeaderHolder)){
            mHeaderHolder.mFiltersAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityUtils.setToolbarTitle(this, R.string.community);
        ActivityUtils.setToolbarSubtitle(this, null);
    }

    @Override
    public void goToCommunityControl(int accountId, Community community, GroupSettings settings) {
        PlaceFactory.getCommunityControlPlace(accountId, community, settings).tryOpenWith(requireActivity());
    }

    @Override
    public void startLoginCommunityActivity(int groupId) {
        Intent intent = LoginActivity.createIntent(requireActivity(), String.valueOf(Constants.API_ID), "messages,photos,docs,manage", Collections.singletonList(groupId));
        startActivityForResult(intent, REQUEST_LOGIN_COMMUNITY);
    }

    @Override
    public void openCommunityDialogs(int accountId, int groupId, String subtitle) {
        PlaceFactory.getDialogsPlace(accountId, -groupId, subtitle).tryOpenWith(requireActivity());
    }

    @Override
    public void displayCounters(int members, int topics, int docs, int photos, int audio, int video) {
        if(isNull(mHeaderHolder)) return;

        setupCounter(mHeaderHolder.bTopics, topics);
        setupCounter(mHeaderHolder.bMembers, members);
        setupCounter(mHeaderHolder.bDocuments, docs);
        setupCounter(mHeaderHolder.bPhotos, photos);
//        setupCounter(mHeaderHolder.bAudios, audio);
        setupCounter(mHeaderHolder.bVideos, video);
    }

    @Override
    public void onPrepareOptionsMenu(@NotNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        OptionMenuView optionMenuView = new OptionMenuView();
        getPresenter().fireOptionMenuViewCreated(optionMenuView);

        menu.findItem(R.id.action_community_control).setVisible(optionMenuView.controlVisible);
    }

    private static final class OptionMenuView implements IOptionMenuView {

        boolean controlVisible;

        @Override
        public void setControlVisible(boolean visible) {
            this.controlVisible = visible;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_LOGIN_COMMUNITY && resultCode == Activity.RESULT_OK){
            ArrayList<Token> tokens = LoginActivity.extractGroupTokens(data);
            getPresenter().fireGroupTokensReceived(tokens);
        }
    }

    private class GroupHeaderHolder {
        ViewGroup vgCover;
        ImageView ivAvatar;
        TextView tvName;
        TextView tvStatus;
        TextView tvScreenName;

        TextView bTopics;
        TextView bMembers;
        TextView bDocuments;
        TextView bPhotos;
        TextView bAudios;
        TextView bVideos;
        Button primaryActionButton;
        Button secondaryActionButton;

        View fabMessage;
        HorizontalOptionsAdapter<PostFilter> mFiltersAdapter;

        GroupHeaderHolder(@NonNull View root) {
            vgCover = root.findViewById(R.id.profile_data_container);
            ivAvatar = root.findViewById(R.id.header_group_avatar);
            tvName = root.findViewById(R.id.header_group_name);
            tvStatus = root.findViewById(R.id.header_group_status);
            tvScreenName = root.findViewById(R.id.header_group_id);
            bTopics = root.findViewById(R.id.header_group_btopics);
            bMembers = root.findViewById(R.id.header_group_bmembers);
            bDocuments = root.findViewById(R.id.header_group_bdocuments);
            bPhotos = root.findViewById(R.id.header_group_bphotos);
            bVideos = root.findViewById(R.id.header_group_bvideos);
            primaryActionButton = root.findViewById(R.id.header_group_primary_button);
            secondaryActionButton = root.findViewById(R.id.header_group_secondary_button);
            fabMessage = root.findViewById(R.id.header_group_fab_message);

            RecyclerView filterList = root.findViewById(R.id.post_filter_recyclerview);
            filterList.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
            mFiltersAdapter = new HorizontalOptionsAdapter<>(Collections.emptyList());
            mFiltersAdapter.setListener(entry -> getPresenter().fireFilterEntryClick(entry));

            filterList.setAdapter(mFiltersAdapter);

            tvStatus.setOnClickListener(v -> getPresenter().fireHeaderStatusClick());
            fabMessage.setOnClickListener(v -> getPresenter().fireChatClick());
            secondaryActionButton.setOnClickListener(v -> getPresenter().fireSecondaryButtonClick());
            primaryActionButton.setOnClickListener(v -> getPresenter().firePrimaryButtonClick());

            root.findViewById(R.id.header_group_photos_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderPhotosClick());
            root.findViewById(R.id.header_group_videos_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderVideosClick());
            root.findViewById(R.id.header_group_members_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderMembersClick());
            root.findViewById(R.id.header_group_topics_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderTopicsClick());
            root.findViewById(R.id.header_group_documents_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderDocsClick());
        }
    }
}