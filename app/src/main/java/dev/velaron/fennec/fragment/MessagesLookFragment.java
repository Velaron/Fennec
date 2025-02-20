package dev.velaron.fennec.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.activity.ActivityFeatures;
import dev.velaron.fennec.activity.ActivityUtils;
import dev.velaron.fennec.activity.SendAttachmentsActivity;
import dev.velaron.fennec.adapter.MessagesAdapter;
import dev.velaron.fennec.fragment.base.PlaceSupportMvpFragment;
import dev.velaron.fennec.listener.EndlessRecyclerOnScrollListener;
import dev.velaron.fennec.model.FwdMessages;
import dev.velaron.fennec.model.LastReadId;
import dev.velaron.fennec.model.LoadMoreState;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.mvp.presenter.MessagesLookPresenter;
import dev.velaron.fennec.mvp.view.IMessagesLookView;
import dev.velaron.fennec.util.Logger;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.view.LoadMoreFooterHelper;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

/**
 * Created by ruslan.kolbasa on 03.10.2016.
 * phoenix
 */
public class MessagesLookFragment extends PlaceSupportMvpFragment<MessagesLookPresenter, IMessagesLookView>
        implements IMessagesLookView, MessagesAdapter.OnMessageActionListener {

    private static final String TAG = MessagesLookFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private MessagesAdapter mMessagesAdapter;

    public static Bundle buildArgs(int accountId, int peerId, int focusMesssageId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.PEER_ID, peerId);
        args.putInt(Extra.FOCUS_TO, focusMesssageId);
        return args;
    }

    public static MessagesLookFragment newInstance(Bundle args) {
        MessagesLookFragment fragment = new MessagesLookFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private View mHeaderView;
    private View mFooterView;

    private LoadMoreFooterHelper mHeaderHelper;
    private LoadMoreFooterHelper mFooterHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_messages_lookup, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, true);

        mRecyclerView = root.findViewById(R.id.recycleView);
        mRecyclerView.setLayoutManager(layoutManager);

        mHeaderView = inflater.inflate(R.layout.footer_load_more, mRecyclerView, false);
        mFooterView = inflater.inflate(R.layout.footer_load_more, mRecyclerView, false);
        mHeaderHelper = LoadMoreFooterHelper.createFrom(mHeaderView, this::onHeaderLoadMoreClick);
        mFooterHelper = LoadMoreFooterHelper.createFrom(mFooterView, this::onFooterLoadMoreClick);

        mEndlessRecyclerOnScrollListener = new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                Logger.d(TAG, "onScrollToLastElement");
            }

            @Override
            public void onScrollToFirstElement() {
                Logger.d(TAG, "onScrollToFirstElement");
            }
        };

        mRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
        return root;
    }

    @Override
    public void showDeleteForAllDialog(ArrayList<Integer> ids) {
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.confirmation)
                .setMessage(R.string.messages_delete_for_all_question_message)
                .setNeutralButton(R.string.button_cancel, null)
                .setPositiveButton(R.string.button_for_all, (dialog, which) -> getPresenter().fireDeleteForAllClick(ids))
                .setNegativeButton(R.string.button_for_me, (dialog, which) -> getPresenter().fireDeleteForMeClick(ids))
                .show();
    }

    private EndlessRecyclerOnScrollListener mEndlessRecyclerOnScrollListener;

    private void onFooterLoadMoreClick() {
        getPresenter().fireFooterLoadMoreClick();
    }

    private void onHeaderLoadMoreClick() {
        getPresenter().fireHeaderLoadMoreClick();
    }

    @Override
    public void displayMessages(@NonNull List<Message> messages, @NonNull LastReadId lastReadId) {
        mMessagesAdapter = new MessagesAdapter(requireActivity(), messages, lastReadId, this);
        mMessagesAdapter.setOnMessageActionListener(this);
        mMessagesAdapter.addFooter(mHeaderView);
        mMessagesAdapter.addHeader(mFooterView);
        mRecyclerView.setAdapter(mMessagesAdapter);
    }

    @Override
    public void focusTo(int index) {
        mRecyclerView.removeOnScrollListener(mEndlessRecyclerOnScrollListener);
        mRecyclerView.scrollToPosition(index + 1); // +header
        mRecyclerView.addOnScrollListener(mEndlessRecyclerOnScrollListener);
    }

    @Override
    public void notifyMessagesUpAdded(int startPosition, int count) {
        if(Objects.nonNull(mMessagesAdapter)){
            mMessagesAdapter.notifyItemRangeInserted(startPosition + 1, count); //+header
        }
    }

    @Override
    public void notifyMessagesDownAdded(int count) {
        if(Objects.nonNull(mMessagesAdapter)){
            mMessagesAdapter.notifyItemRemoved(0);
            mMessagesAdapter.notifyItemRangeInserted(0, count + 1); //+header
        }
    }

    @Override
    public void configNowVoiceMessagePlaying(int id, float progress, boolean paused, boolean amin) {
        // TODO: 09.10.2016
    }

    @Override
    public void bindVoiceHolderById(int holderId, boolean play, boolean paused, float progress, boolean amin) {
        // TODO: 09.10.2016
    }

    @Override
    public void disableVoicePlaying() {
        // TODO: 09.10.2016
    }

    private ActionMode mActionMode;
    private ActionModeCallback mActionModeCallback = new ActionModeCallback();

    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.delete:
                    getPresenter().fireActionModeDeleteClick();
                    break;
                case R.id.copy:
                    getPresenter().fireActionModeCopyClick();
                    break;
                case R.id.forward:
                    getPresenter().fireForwardClick();
                    break;
            }

            mode.finish();
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.messages_menu, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            getPresenter().fireActionModeDestroy();
            mActionMode = null;
        }
    }

    @Override
    public void showActionMode(String title, Boolean canEdit, Boolean canPin) {
        if (Objects.isNull(mActionMode)) {
            mActionMode = ((AppCompatActivity) requireActivity()).startSupportActionMode(mActionModeCallback);
        }

        if(Objects.nonNull(mActionMode)){
            mActionMode.setTitle(title);
            mActionMode.invalidate();
        }
    }

    @Override
    public void finishActionMode() {
        if(Objects.nonNull(mActionMode)){
            mActionMode.finish();
            mActionMode = null;
        }
    }

    @Override
    public void notifyDataChanged() {
        if(Objects.nonNull(mMessagesAdapter)){
            mMessagesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setupHeaders(@LoadMoreState int upHeaderState, @LoadMoreState int downHeaderState) {
        if(Objects.nonNull(mHeaderHelper)){
            mHeaderHelper.switchToState(upHeaderState);
        }

        if(Objects.nonNull(mFooterHelper)){
            mFooterHelper.switchToState(downHeaderState);
        }
    }

    @Override
    public void forwardMessages(int accountId, @NonNull ArrayList<Message> messages) {
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, new FwdMessages(messages));
    }

    @Override
    public IPresenterFactory<MessagesLookPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int aid = requireArguments().getInt(Extra.ACCOUNT_ID);
            int peerId = requireArguments().getInt(Extra.PEER_ID);
            Integer focusTo = requireArguments().containsKey(Extra.FOCUS_TO) ? requireArguments().getInt(Extra.FOCUS_TO) : null;
            return new MessagesLookPresenter(aid, peerId, focusTo, saveInstanceState);
        };
    }

    @Override
    public void onAvatarClick(@NonNull Message message, int userId) {
        if(Objects.nonNull(mActionMode)){
            getPresenter().fireMessageClick(message);
        } else {
            getPresenter().fireOwnerClick(userId);
        }
    }

    @Override
    public void onRestoreClick(@NonNull Message message, int position) {
        getPresenter().fireMessageRestoreClick(message, position);
    }

    @Override
    public boolean onMessageLongClick(@NonNull Message message) {
        getPresenter().fireMessageLongClick(message);
        return true;
    }

    @Override
    public void onMessageClicked(@NonNull Message message) {
        getPresenter().fireMessageClick(message);
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if(Objects.nonNull(actionBar)){
            actionBar.setTitle(R.string.viewing_messages);
            actionBar.setSubtitle(null);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }
}
