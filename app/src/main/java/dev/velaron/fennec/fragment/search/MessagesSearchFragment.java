package dev.velaron.fennec.fragment.search;

import android.os.Bundle;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.adapter.MessagesAdapter;
import dev.velaron.fennec.fragment.search.criteria.MessageSeachCriteria;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.mvp.presenter.search.MessagesSearchPresenter;
import dev.velaron.fennec.mvp.view.search.IMessagesSearchView;
import dev.velaron.fennec.place.PlaceFactory;
import biz.dealnote.mvp.core.IPresenterFactory;

/**
 * Created by admin on 28.06.2016.
 * phoenix
 */
public class MessagesSearchFragment extends AbsSearchFragment<MessagesSearchPresenter, IMessagesSearchView, Message>
        implements MessagesAdapter.OnMessageActionListener, IMessagesSearchView {

    public static MessagesSearchFragment newInstance(int accountId, @Nullable MessageSeachCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        MessagesSearchFragment fragment = new MessagesSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    void setAdapterData(RecyclerView.Adapter adapter, List<Message> data) {
        ((MessagesAdapter) adapter).setItems(data);
    }

    @Override
    RecyclerView.Adapter createAdapter(List<Message> data) {
        MessagesAdapter adapter = new MessagesAdapter(requireActivity(), data, this);
        //adapter.setOnHashTagClickListener(this);
        adapter.setOnMessageActionListener(this);
        return adapter;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity());
    }

    @Override
    public void onAvatarClick(@NonNull Message message, int userId) {
        getPresenter().fireOwnerClick(userId);
    }

    @Override
    public void onRestoreClick(@NonNull Message message, int position) {
        // delete is not supported
    }

    @Override
    public boolean onMessageLongClick(@NonNull Message message) {
        return false;
    }

    @Override
    public void onMessageClicked(@NonNull Message message) {
        getPresenter().fireMessageClick(message);
    }

    @Override
    public IPresenterFactory<MessagesSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            MessageSeachCriteria c = getArguments().getParcelable(Extra.CRITERIA);
            return new MessagesSearchPresenter(accountId, c, saveInstanceState);
        };
    }

    @Override
    public void goToMessagesLookup(int accountId, int peerId, int messageId) {
        PlaceFactory.getMessagesLookupPlace(accountId, peerId, messageId).tryOpenWith(requireActivity());
    }
}
