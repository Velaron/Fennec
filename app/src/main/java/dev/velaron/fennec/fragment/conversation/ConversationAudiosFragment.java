package dev.velaron.fennec.fragment.conversation;

import android.os.Bundle;

import java.util.Collections;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.adapter.AudioRecyclerAdapter;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.mvp.presenter.history.ChatAttachmentAudioPresenter;
import dev.velaron.fennec.mvp.view.IChatAttachmentAudiosView;
import biz.dealnote.mvp.core.IPresenterFactory;

public class ConversationAudiosFragment extends AbsChatAttachmentsFragment<Audio, ChatAttachmentAudioPresenter, IChatAttachmentAudiosView>
        implements AudioRecyclerAdapter.ClickListener, IChatAttachmentAudiosView {

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
    }

    @Override
    public RecyclerView.Adapter createAdapter() {
        AudioRecyclerAdapter audioRecyclerAdapter = new AudioRecyclerAdapter(requireActivity(), Collections.emptyList());
        audioRecyclerAdapter.setClickListener(this);
        return audioRecyclerAdapter;
    }

    @Override
    public void onClick(int position, Audio audio) {
        getPresenter().fireAudioPlayClick(position, audio);
    }

    @Override
    public void displayAttachments(List<Audio> data) {
        ((AudioRecyclerAdapter)getAdapter()).setData(data);
    }

    @Override
    public IPresenterFactory<ChatAttachmentAudioPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ChatAttachmentAudioPresenter(
                getArguments().getInt(Extra.PEER_ID),
                getArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }
}
