package dev.velaron.fennec.fragment.conversation;

import android.os.Bundle;

import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.R;
import dev.velaron.fennec.adapter.fave.FavePhotosAdapter;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.TmpSource;
import dev.velaron.fennec.mvp.presenter.history.ChatAttachmentPhotoPresenter;
import dev.velaron.fennec.mvp.view.IChatAttachmentPhotosView;
import dev.velaron.fennec.place.PlaceFactory;
import biz.dealnote.mvp.core.IPresenterFactory;

public class ConversationPhotosFragment extends AbsChatAttachmentsFragment<Photo, ChatAttachmentPhotoPresenter,
        IChatAttachmentPhotosView> implements FavePhotosAdapter.PhotoSelectionListener, IChatAttachmentPhotosView {

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        int columns = getContext().getResources().getInteger(R.integer.photos_column_count);
        return new GridLayoutManager(requireActivity(), columns);
    }

    @Override
    public RecyclerView.Adapter createAdapter() {
        FavePhotosAdapter apiPhotoFavePhotosAdapter = new FavePhotosAdapter(requireActivity(), Collections.emptyList());
        apiPhotoFavePhotosAdapter.setPhotoSelectionListener(this);
        return apiPhotoFavePhotosAdapter;
    }

    @Override
    public void onPhotoClicked(int position, Photo photo) {
        getPresenter().firePhotoClick(position, photo);
    }

    @Override
    public IPresenterFactory<ChatAttachmentPhotoPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            int peerId = getArguments().getInt(Extra.PEER_ID);
            return new ChatAttachmentPhotoPresenter(peerId, accountId, saveInstanceState);
        };
    }

    @Override
    public void displayAttachments(List<Photo> data) {
        FavePhotosAdapter adapter = (FavePhotosAdapter) getAdapter();
        adapter.setData(data);
    }

    @Override
    public void goToTempPhotosGallery(int accountId, @NonNull TmpSource source, int index) {
        PlaceFactory.getTmpSourceGalleryPlace(accountId, source, index).tryOpenWith(requireActivity());
    }
}