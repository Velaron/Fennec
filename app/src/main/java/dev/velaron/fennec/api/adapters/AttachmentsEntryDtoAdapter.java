package dev.velaron.fennec.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import dev.velaron.fennec.api.model.VKApiAttachment;
import dev.velaron.fennec.api.model.VKApiAudio;
import dev.velaron.fennec.api.model.VKApiGiftItem;
import dev.velaron.fennec.api.model.VKApiLink;
import dev.velaron.fennec.api.model.VKApiPhoto;
import dev.velaron.fennec.api.model.VKApiPhotoAlbum;
import dev.velaron.fennec.api.model.VKApiPoll;
import dev.velaron.fennec.api.model.VKApiPost;
import dev.velaron.fennec.api.model.VKApiSticker;
import dev.velaron.fennec.api.model.VKApiVideo;
import dev.velaron.fennec.api.model.VKApiWikiPage;
import dev.velaron.fennec.api.model.VkApiAttachments;
import dev.velaron.fennec.api.model.VkApiDoc;
import dev.velaron.fennec.util.Objects;

/**
 * Created by admin on 29.03.2017.
 * phoenix
 */
public class AttachmentsEntryDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiAttachments.Entry> {

    @Override
    public VkApiAttachments.Entry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject o = json.getAsJsonObject();

        String type = optString(o, "type");
        VKApiAttachment attachment = parse(type, o, context);

        VkApiAttachments.Entry entry = null;
        if (Objects.nonNull(attachment)) {
            entry = new VkApiAttachments.Entry(type, attachment);
        }

        return entry;
    }

    private VKApiAttachment parse(String type, JsonObject root, JsonDeserializationContext context) {
        JsonElement o = root.get(type);

        //{"type":"photos_list","photos_list":["406536042_456239026"]}

        if (VkApiAttachments.TYPE_PHOTO.equals(type)) {
            return context.deserialize(o, VKApiPhoto.class);
        } else if (VkApiAttachments.TYPE_VIDEO.equals(type)) {
            return context.deserialize(o, VKApiVideo.class);
        } else if (VkApiAttachments.TYPE_AUDIO.equals(type)) {
            return context.deserialize(o, VKApiAudio.class);
        } else if (VkApiAttachments.TYPE_DOC.equals(type)) {
            return context.deserialize(o, VkApiDoc.class);
        } else if (VkApiAttachments.TYPE_POST.equals(type)) {
            return context.deserialize(o, VKApiPost.class);
            //} else if (VkApiAttachments.TYPE_POSTED_PHOTO.equals(type)) {
            //    return context.deserialize(o, VKApiPostedPhoto.class);
        } else if (VkApiAttachments.TYPE_LINK.equals(type)) {
            return context.deserialize(o, VKApiLink.class);
            //} else if (VkApiAttachments.TYPE_NOTE.equals(type)) {
            //    return context.deserialize(o, VKApiNote.class);
            //} else if (VkApiAttachments.TYPE_APP.equals(type)) {
            //    return context.deserialize(o, VKApiApplicationContent.class);
        } else if (VkApiAttachments.TYPE_POLL.equals(type)) {
            return context.deserialize(o, VKApiPoll.class);
        } else if (VkApiAttachments.TYPE_WIKI_PAGE.equals(type)) {
            return context.deserialize(o, VKApiWikiPage.class);
        } else if (VkApiAttachments.TYPE_ALBUM.equals(type)) {
            return context.deserialize(o, VKApiPhotoAlbum.class);
        } else if (VkApiAttachments.TYPE_STICKER.equals(type)) {
            return context.deserialize(o, VKApiSticker.class);
        } else if (VkApiAttachments.TYPE_GIFT.equals(type)) {
            return context.deserialize(o, VKApiGiftItem.class);
        }

        return null;
    }
}
