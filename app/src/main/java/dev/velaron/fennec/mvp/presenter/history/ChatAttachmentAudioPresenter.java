package dev.velaron.fennec.mvp.presenter.history;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.safeCountOf;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.mvp.reflect.OnGuiCreated;
import dev.velaron.fennec.R;
import dev.velaron.fennec.api.Apis;
import dev.velaron.fennec.api.model.VKApiAudio;
import dev.velaron.fennec.api.model.response.AttachmentsHistoryResponse;
import dev.velaron.fennec.domain.mappers.Dto2Model;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.mvp.view.IChatAttachmentAudiosView;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Single;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public class ChatAttachmentAudioPresenter extends BaseChatAttachmentsPresenter<Audio, IChatAttachmentAudiosView> {

    public ChatAttachmentAudioPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(peerId, accountId, savedInstanceState);
    }

    @Override
    void onDataChanged() {
        super.onDataChanged();
        resolveToolbar();
    }

    @Override
    Single<Pair<String, List<Audio>>> requestAttachments(int peerId, String nextFrom) {
        return Apis.get().vkDefault(getAccountId())
                .messages()
                .getHistoryAttachments(peerId, "audio", nextFrom, 50, null)
                .map(response -> {
                    List<Audio> audios = new ArrayList<>(safeCountOf(response.items));

                    if (nonNull(response.items)) {
                        for (AttachmentsHistoryResponse.One one : response.items) {
                            if (nonNull(one) && nonNull(one.entry) && one.entry.attachment instanceof VKApiAudio) {
                                VKApiAudio dto = (VKApiAudio) one.entry.attachment;
                                audios.add(Dto2Model.transform(dto));
                            }
                        }
                    }

                    return Pair.Companion.create(response.next_from, audios);
                });
    }

    @SuppressWarnings("unused")
    public void fireAudioPlayClick(int position, Audio audio){
        super.fireAudioPlayClick(position, new ArrayList<>(data));
    }

    @OnGuiCreated
    private void resolveToolbar() {
        if (isGuiReady()) {
            getView().setToolbarTitle(getString(R.string.attachments_in_chat));
            getView().setToolbarSubtitle(getString(R.string.audios_count, safeCountOf(data)));
        }
    }
}