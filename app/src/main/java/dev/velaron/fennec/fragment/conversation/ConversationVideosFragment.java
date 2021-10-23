package dev.velaron.fennec.fragment.conversation;

import android.os.Bundle;

import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.VideosAdapter;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.mvp.presenter.history.ChatAttachmentVideoPresenter;
import dev.velaron.fennec.mvp.view.IChatAttachmentVideoView;
import dev.velaron.fennec.mvp.core.IPresenterFactory;

public class ConversationVideosFragment extends AbsChatAttachmentsFragment<Video, ChatAttachmentVideoPresenter, IChatAttachmentVideoView>
        implements VideosAdapter.VideoOnClickListener, IChatAttachmentVideoView {

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        int columns = getContext().getResources().getInteger(R.integer.videos_column_count);
        return new StaggeredGridLayoutManager(columns, StaggeredGridLayoutManager.VERTICAL);
    }

    @Override
    public RecyclerView.Adapter createAdapter() {
        VideosAdapter adapter = new VideosAdapter(requireActivity(), Collections.emptyList());
        adapter.setVideoOnClickListener(this);
        return adapter;
    }

    @Override
    public void onVideoClick(int position, Video video) {
        getPresenter().fireVideoClick(video);
    }

    @Override
    public void displayAttachments(List<Video> data) {
        VideosAdapter adapter = (VideosAdapter) getAdapter();
        adapter.setData(data);
    }

    @Override
    public IPresenterFactory<ChatAttachmentVideoPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            int peerId = getArguments().getInt(Extra.PEER_ID);
            return new ChatAttachmentVideoPresenter(peerId, accountId, saveInstanceState);
        };
    }
}