package dev.velaron.fennec.longpoll;

import android.content.Context;

import dev.velaron.fennec.R;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.settings.ISettings;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.Logger;

import static dev.velaron.fennec.util.Utils.hasFlag;
import static dev.velaron.fennec.util.Utils.isEmpty;

public class LongPollNotificationHelper {

    public static final String TAG = LongPollNotificationHelper.class.getSimpleName();

    /**
     * Действие при добавлении нового сообщения в диалог или чат
     *
     * @param message нотификация с сервера
     */
    public static void notifyAbountNewMessage(Context context, final Message message) {
        if (message.isOut()) {
            return;
        }

        //if (message.isRead()) {
        //    return;
        //}

        //boolean needSendNotif = needNofinicationFor(message.getAccountId(), message.getPeerId());
        //if(!needSendNotif){
        //    return;
        //}

        String messageText = isEmpty(message.getDecryptedBody()) ? (isEmpty(message.getBody())
                ? context.getString(R.string.attachments) : message.getBody()) : message.getDecryptedBody();

        notifyAbountNewMessage(context, message.getAccountId(), messageText, message.getPeerId(), message.getId(), message.getDate());
    }

    private static void notifyAbountNewMessage(Context context, int accountId, String body, int peerId, int messageId, long date){
        int mask = Settings.get().notifications().getNotifPref(accountId, peerId);
        if (!hasFlag(mask, ISettings.INotificationSettings.FLAG_SHOW_NOTIF)) {
            return;
        }

        if (Settings.get().accounts().getCurrent() != accountId) {
            Logger.d(TAG, "notifyAbountNewMessage, Attempting to send a notification does not in the current account!!!");
            return;
        }

        NotificationHelper.notifNewMessage(context, accountId, body, peerId, messageId, date);
    }
}