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
import dev.velaron.fennec.api.model.VKApiAttachment;
import dev.velaron.fennec.api.model.VkApiDoc;
import dev.velaron.fennec.api.model.response.AttachmentsHistoryResponse;
import dev.velaron.fennec.domain.mappers.Dto2Model;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.mvp.view.IChatAttachmentDocsView;
import dev.velaron.fennec.util.Pair;
import io.reactivex.Single;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public class ChatAttachmentDocsPresenter extends BaseChatAttachmentsPresenter<Document, IChatAttachmentDocsView> {

    public ChatAttachmentDocsPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(peerId, accountId, savedInstanceState);
    }

    @Override
    void onDataChanged() {
        super.onDataChanged();
        resolveToolbar();
    }

    @Override
    Single<Pair<String, List<Document>>> requestAttachments(int peerId, String nextFrom) {
        return Apis.get().vkDefault(getAccountId())
                .messages()
                .getHistoryAttachments(peerId, VKApiAttachment.TYPE_DOC, nextFrom, 50, null)
                .map(response -> {
                    List<Document> docs = new ArrayList<>(safeCountOf(response.items));

                    if (nonNull(response.items)) {
                        for (AttachmentsHistoryResponse.One one : response.items) {
                            if (nonNull(one) && nonNull(one.entry) && one.entry.attachment instanceof VkApiDoc) {
                                VkApiDoc dto = (VkApiDoc) one.entry.attachment;
                                docs.add(Dto2Model.transform(dto));
                            }
                        }
                    }

                    return Pair.Companion.create(response.next_from, docs);
                });
    }

    @OnGuiCreated
    private void resolveToolbar() {
        if (isGuiReady()) {
            getView().setToolbarTitle(getString(R.string.attachments_in_chat));
            getView().setToolbarSubtitle(getString(R.string.documents_count, safeCountOf(data)));
        }
    }
}