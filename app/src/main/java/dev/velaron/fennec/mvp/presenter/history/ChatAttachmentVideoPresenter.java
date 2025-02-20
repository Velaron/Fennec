package dev.velaron.fennec.mvp.presenter.history;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.safeCountOf;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.R;
import dev.velaron.fennec.api.Apis;
import dev.velaron.fennec.api.model.VKApiVideo;
import dev.velaron.fennec.api.model.response.AttachmentsHistoryResponse;
import dev.velaron.fennec.domain.mappers.Dto2Model;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.mvp.view.IChatAttachmentVideoView;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Single;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public class ChatAttachmentVideoPresenter extends BaseChatAttachmentsPresenter<Video, IChatAttachmentVideoView> {

    public ChatAttachmentVideoPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(peerId, accountId, savedInstanceState);
    }

    @Override
    void onDataChanged() {
        super.onDataChanged();
        resolveToolbar();
    }

    @Override
    Single<Pair<String, List<Video>>> requestAttachments(int peerId, String nextFrom) {
        return Apis.get().vkDefault(getAccountId())
                .messages()
                .getHistoryAttachments(peerId, "video", nextFrom, 50, null)
                .map(response -> {
                    List<Video> videos = new ArrayList<>(safeCountOf(response.items));

                    if (nonNull(response.items)) {
                        for (AttachmentsHistoryResponse.One one : response.items) {
                            if (nonNull(one) && nonNull(one.entry) && one.entry.attachment instanceof VKApiVideo) {
                                VKApiVideo dto = (VKApiVideo) one.entry.attachment;
                                videos.add(Dto2Model.transform(dto));
                            }
                        }
                    }

                    return Pair.Companion.create(response.next_from, videos);
                });
    }

    @OnGuiCreated
    private void resolveToolbar() {
        if (isGuiReady()) {
            getView().setToolbarTitle(getString(R.string.attachments_in_chat));
            getView().setToolbarSubtitle(getString(R.string.videos_count, safeCountOf(data)));
        }
    }
}