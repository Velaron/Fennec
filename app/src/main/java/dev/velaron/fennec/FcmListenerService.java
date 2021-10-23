package dev.velaron.fennec;

import android.content.Context;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import dev.velaron.fennec.push.IPushRegistrationResolver;
import dev.velaron.fennec.push.PushType;
import dev.velaron.fennec.push.message.CommentFCMMessage;
import dev.velaron.fennec.push.message.FCMMessage;
import dev.velaron.fennec.push.message.FriendFCMMessage;
import dev.velaron.fennec.push.message.LikeFCMMessage;
import dev.velaron.fennec.push.message.NewPostPushMessage;
import dev.velaron.fennec.push.message.WallPostFCMMessage;
import dev.velaron.fennec.settings.ISettings;
import dev.velaron.fennec.settings.Settings;
import dev.velaron.fennec.util.Logger;
import dev.velaron.fennec.util.PersistentLogger;
import dev.velaron.fennec.util.RxUtils;

import static dev.velaron.fennec.util.Utils.isEmpty;

public class FcmListenerService extends FirebaseMessagingService {

    private static final String TAG = "FcmListenerService";

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Injection.providePushRegistrationResolver()
                .resolvePushRegistration()
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(RxUtils.dummy(), RxUtils.ignore());
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        Context context = getApplicationContext();
        Logger.d(TAG, message.getData().size() > 0 ? "Data-notification" : "Notification-notification");

        String pushType = message.getData().get("type");

        Logger.d(TAG, "onMessage, from: " + message.getFrom() + ", pushType: " + pushType + ", data: " + message.getData());
        if (isEmpty(pushType)) {
            return;
        }

        StringBuilder bundleDump = new StringBuilder();
        for (Map.Entry<String, String> entry : message.getData().entrySet()) {
            try {
                String line = "key: " + entry.getKey() + ", value: " + entry.getValue() + ", class: " + (entry.getValue() == null ? "null" : entry.getValue().getClass());
                Logger.d(TAG, line);
                bundleDump.append("\n").append(line);
            } catch (Exception ignored) {
            }
        }

        int accountId = Settings.get()
                .accounts()
                .getCurrent();

        if (accountId == ISettings.IAccountsSettings.INVALID_ID) {
            return;
        }

        final IPushRegistrationResolver registrationResolver = Injection.providePushRegistrationResolver();

        if (!registrationResolver.canReceivePushNotification()) {
            Logger.d(TAG, "Invalid push registration on VK");
            return;
        }

        try {
            switch (pushType) {
                case PushType.MSG:
                case "chat":
                    FCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.POST:
                    WallPostFCMMessage.fromRemoteMessage(message).nofify(context, accountId);
                    break;
                case PushType.COMMENT:
                    CommentFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.FRIEND:
                    FriendFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                    break;
                case PushType.NEW_POST:
                    new NewPostPushMessage(accountId, message).notifyIfNeed(context);
                    break;
                case PushType.LIKE:
                    new LikeFCMMessage(accountId, message).notifyIfNeed(context);
                    break;

            /*case PushType.REPLY:
                ReplyFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;
            case PushType.WALL_PUBLISH:
                WallPublishFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;

            case PushType.FRIEND_ACCEPTED:
                FriendAcceptedFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;
            case PushType.GROUP_INVITE:
                GroupInviteFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;
            case PushType.BIRTHDAY:
                BirthdayFCMMessage.fromRemoteMessage(message).notify(context, accountId);
                break;

            */
                default:
                    PersistentLogger.logThrowable("Push issues", new Exception("Unespected Push event, collapse_key: " + pushType + ", dump: " + bundleDump));
                    break;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}