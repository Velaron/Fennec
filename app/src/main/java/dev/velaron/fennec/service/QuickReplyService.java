package dev.velaron.fennec.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.RemoteInput;
import dev.velaron.fennec.Extra;
import dev.velaron.fennec.domain.IMessagesRepository;
import dev.velaron.fennec.domain.Repository;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.model.SaveMessageBuilder;

public class QuickReplyService extends IntentService {

    public static final String ACTION_ADD_MESSAGE = "SendService.ACTION_ADD_MESSAGE";

    public QuickReplyService() {
        super(QuickReplyService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null && ACTION_ADD_MESSAGE.equals(intent.getAction()) && intent.getExtras() != null) {
            int accountId = intent.getExtras().getInt(Extra.ACCOUNT_ID);
            int peerId = intent.getExtras().getInt(Extra.PEER_ID);
            Bundle msg = RemoteInput.getResultsFromIntent(intent);

            if (msg != null) {
                CharSequence body = msg.getCharSequence(Extra.BODY);
                addMessage(accountId, peerId, body == null ? null : body.toString());
            }
        }
    }

    private void addMessage(int accountId, int peerId, String body) {
        final IMessagesRepository messagesInteractor = Repository.INSTANCE.getMessages();
        SaveMessageBuilder builder = new SaveMessageBuilder(accountId, peerId).setBody(body);

        Message message = messagesInteractor.put(builder).blockingGet();

        Repository.INSTANCE.getMessages().runSendingQueue();
    }

    public static Intent intentForAddMessage(Context context, int accountId, int peerId) {
        Intent intent = new Intent(context, QuickReplyService.class);
        intent.setAction(ACTION_ADD_MESSAGE);
        intent.putExtra(Extra.ACCOUNT_ID, accountId);
        intent.putExtra(Extra.PEER_ID, peerId);
        return intent;
    }
}