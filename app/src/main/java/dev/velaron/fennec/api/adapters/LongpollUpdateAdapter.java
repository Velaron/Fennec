package dev.velaron.fennec.api.adapters;

import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.hasFlag;
import static dev.velaron.fennec.util.Utils.nonEmpty;

import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import dev.velaron.fennec.api.model.VKApiMessage;
import dev.velaron.fennec.api.model.longpoll.AbsLongpollEvent;
import dev.velaron.fennec.api.model.longpoll.AddMessageUpdate;
import dev.velaron.fennec.api.model.longpoll.BadgeCountChangeUpdate;
import dev.velaron.fennec.api.model.longpoll.InputMessagesSetReadUpdate;
import dev.velaron.fennec.api.model.longpoll.MessageFlagsResetUpdate;
import dev.velaron.fennec.api.model.longpoll.MessageFlagsSetUpdate;
import dev.velaron.fennec.api.model.longpoll.OutputMessagesSetReadUpdate;
import dev.velaron.fennec.api.model.longpoll.UserIsOfflineUpdate;
import dev.velaron.fennec.api.model.longpoll.UserIsOnlineUpdate;
import dev.velaron.fennec.api.model.longpoll.WriteTextInDialogUpdate;
import dev.velaron.fennec.api.util.VKStringUtils;
import dev.velaron.fennec.model.Peer;

/**
 * Created by admin on 25.12.2016.
 * phoenix
 */
public class LongpollUpdateAdapter extends AbsAdapter implements JsonDeserializer<AbsLongpollEvent> {

    @Override
    public AbsLongpollEvent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray array = json.getAsJsonArray();
        int action = array.get(0).getAsInt();
        return deserialize(action, array);
    }

    @Nullable
    private AbsLongpollEvent deserialize(int action, JsonArray array){
        switch (action){
            case AbsLongpollEvent.ACTION_MESSAGE_ADDED:
                return deserializeAddMessageUpdate(array);

            case AbsLongpollEvent.ACTION_USER_WRITE_TEXT_IN_DIALOG:
                WriteTextInDialogUpdate w = new WriteTextInDialogUpdate();
                w.user_id = optInt(array, 1);
                w.flags = optInt(array, 2);
                return w;

            case AbsLongpollEvent.ACTION_USER_IS_ONLINE:
                UserIsOnlineUpdate u = new UserIsOnlineUpdate();
                u.user_id = -optInt(array, 1);
                u.extra = optInt(array, 2);
                return u;

            case AbsLongpollEvent.ACTION_USER_IS_OFFLINE:
                UserIsOfflineUpdate u1 = new UserIsOfflineUpdate();
                u1.user_id = -optInt(array, 1);
                u1.flags = optInt(array, 2);
                return u1;

            case AbsLongpollEvent.ACTION_MESSAGES_FLAGS_RESET: {
                MessageFlagsResetUpdate update = new MessageFlagsResetUpdate();
                update.message_id = optInt(array, 1);
                update.mask = optInt(array, 2);
                update.peer_id = optInt(array, 3);
                return update.peer_id != 0 && update.message_id != 0 ? update : null;
            }

            case AbsLongpollEvent.ACTION_MESSAGES_FLAGS_SET: {
                MessageFlagsSetUpdate update = new MessageFlagsSetUpdate();
                update.message_id = optInt(array, 1);
                update.mask = optInt(array, 2);
                update.peer_id = optInt(array, 3);
                return update.peer_id != 0 && update.message_id != 0 ? update : null;
            }

            case AbsLongpollEvent.ACTION_COUNTER_UNREAD_WAS_CHANGED:
                BadgeCountChangeUpdate c = new BadgeCountChangeUpdate();
                c.count = optInt(array, 1);
                return c;

            case AbsLongpollEvent.ACTION_SET_INPUT_MESSAGES_AS_READ: {
                InputMessagesSetReadUpdate update = new InputMessagesSetReadUpdate();
                update.peer_id = optInt(array, 1);
                update.local_id = optInt(array, 2);
                update.unread_count = optInt(array, 3); // undocumented
                return update.peer_id != 0 ? update : null;
            }
            case AbsLongpollEvent.ACTION_SET_OUTPUT_MESSAGES_AS_READ:{
                OutputMessagesSetReadUpdate update = new OutputMessagesSetReadUpdate();
                update.peer_id = optInt(array, 1);
                update.local_id = optInt(array, 2);
                update.unread_count = optInt(array, 3); // undocumented
                return update.peer_id != 0 ? update : null;
            }
        }

        return null;
    }

    private AddMessageUpdate deserializeAddMessageUpdate(JsonArray array){
        AddMessageUpdate update = new AddMessageUpdate();

        int flags = optInt(array, 2);

        update.message_id = optInt(array, 1);
        update.peer_id = optInt(array, 3);
        update.timestamp = optLong(array, 4);
        update.text = VKStringUtils.unescape(optString(array, 5));
        update.outbox = hasFlag(flags, VKApiMessage.FLAG_OUTBOX);
        update.unread = hasFlag(flags, VKApiMessage.FLAG_UNREAD);
        update.important = hasFlag(flags, VKApiMessage.FLAG_IMPORTANT);
        update.deleted = hasFlag(flags, VKApiMessage.FLAG_DELETED);

        JsonObject extra = (JsonObject) opt(array, 6);
        if(nonNull(extra)){
            update.from = optInt(extra, "from");
            update.subject = optString(extra, "title");
            update.sourceText = optString(extra, "source_text");
            update.sourceAct = optString(extra, "source_act");
            update.sourceMid = optInt(extra, "source_mid");
        }

        JsonObject attachments = (JsonObject) opt(array, 7);
        if(nonNull(attachments)){
            update.hasMedia = attachments.has("attach1_type");
            String fwd = optString(attachments, "fwd");
            if(nonEmpty(fwd)){
                update.fwds = parseLineWithSeparators(fwd, ",");
            }
        }

        update.random_id = optString(array, 8); // ok

        if(update.from == 0 && !Peer.isGroupChat(update.peer_id) && !update.outbox){
            update.from = update.peer_id;
        }

        return update.message_id != 0 ? update : null;
    }

    private static ArrayList<String> parseLineWithSeparators(String line, String separator) {
        if (isNull(line) || line.isEmpty()) {
            return null;
        }

        String[] tokens = line.split(separator);
        ArrayList<String> ids = new ArrayList<>();
        Collections.addAll(ids, tokens);
        return ids;
    }
}