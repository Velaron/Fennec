package dev.velaron.fennec.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.Constants;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.adapter.AttachmentsViewBinder;
import dev.velaron.fennec.adapter.feedback.FeedbackAdapter;
import dev.velaron.fennec.dialog.FeedbackLinkDialog;
import dev.velaron.fennec.fragment.base.PlaceSupportMvpFragment;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.listener.OnSectionResumeCallback;
import dev.velaron.fennec.listener.PicassoPauseOnScrollListener;
import dev.velaron.fennec.model.LoadMoreState;
import dev.velaron.fennec.model.feedback.Feedback;
import dev.velaron.fennec.mvp.presenter.FeedbackPresenter;
import dev.velaron.fennec.mvp.view.IFeedbackView;
import dev.velaron.fennec.place.Place;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.ViewUtils;
import dev.velaron.fennec.view.LoadMoreFooterHelper;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

import static dev.velaron.fennec.util.Objects.nonNull;

public class FeedbackFragment extends PlaceSupportMvpFragment<FeedbackPresenter, IFeedbackView> implements SwipeRefreshLayout.OnRefreshListener,
        IFeedbackView, FeedbackAdapter.ClickListener, AttachmentsViewBinder.OnAttachmentsActionCallback {

    private static final String TAG = FeedbackFragment.class.getSimpleName();

    private FeedbackAdapter mAdapter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mEmptyText;
    private LoadMoreFooterHelper mLoadMoreHelper;

    public static Bundle buildArgs(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static FeedbackFragment newInstance(int accountId) {
        return newInstance(buildArgs(accountId));
    }

    public static FeedbackFragment newInstance(Bundle args) {
        FeedbackFragment feedsFragment = new FeedbackFragment();
        feedsFragment.setArguments(args);
        return feedsFragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_feedback, container, false);
        ((AppCompatActivity)requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mEmptyText = root.findViewById(R.id.fragment_feedback_empty_text);
        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout, true);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(requireActivity());
        RecyclerView recyclerView = root.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToLast();
            }
        });

        View footerView = inflater.inflate(R.layout.footer_load_more, recyclerView, false);
        mLoadMoreHelper = LoadMoreFooterHelper.createFrom(footerView, getPresenter()::fireLoadMoreClick);
        mLoadMoreHelper.switchToState(LoadMoreState.INVISIBLE);
        mLoadMoreHelper.setEndOfListText("• • • • • • • •");

        mAdapter = new FeedbackAdapter(requireActivity(), Collections.emptyList(), this);
        mAdapter.addFooter(footerView);
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

        resolveEmptyTextVisibility();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.NOTIFICATIONS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.drawer_feedback);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_FEEDBACK);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void displayData(List<Feedback> data) {
        if(nonNull(mAdapter)){
            mAdapter.setItems(data);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void showLoading(boolean loading) {
        if(nonNull(mSwipeRefreshLayout)){
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(loading));
        }
    }

    private void resolveEmptyTextVisibility() {
        if(nonNull(mEmptyText) && nonNull(mAdapter)){
            mEmptyText.setVisibility(mAdapter.getRealItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void notifyDataAdding(int position, int count) {
        if(nonNull(mAdapter)){
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)){
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void configLoadMore(@LoadMoreState int loadmoreState) {
        if (nonNull(mLoadMoreHelper)){
            mLoadMoreHelper.switchToState(loadmoreState);
        }
    }

    @Override
    public void showLinksDialog(int accountId, @NonNull Feedback notification) {
        FeedbackLinkDialog.newInstance(accountId, notification).show(requireFragmentManager(), "feedback_links");
    }

    @Override
    public IPresenterFactory<FeedbackPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FeedbackPresenter(requireArguments().getInt(Extra.ACCOUNT_ID),saveInstanceState);
    }

    @Override
    public void onNotificationClick(Feedback notification) {
        getPresenter().fireItemClick(notification);
    }

    @Override
    public void onRefresh() {
        getPresenter().fireRefresh();
    }
}