package dev.velaron.fennec.domain.mappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.api.model.AttachmentsTokenCreator;
import dev.velaron.fennec.api.model.IAttachmentToken;
import dev.velaron.fennec.db.model.entity.AudioEntity;
import dev.velaron.fennec.db.model.entity.DocumentEntity;
import dev.velaron.fennec.db.model.entity.Entity;
import dev.velaron.fennec.db.model.entity.LinkEntity;
import dev.velaron.fennec.db.model.entity.PhotoEntity;
import dev.velaron.fennec.db.model.entity.PollEntity;
import dev.velaron.fennec.db.model.entity.PostEntity;
import dev.velaron.fennec.db.model.entity.VideoEntity;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.safeCountOf;

/**
 * Created by Ruslan Kolbasa on 05.09.2017.
 * phoenix
 */
public class Entity2Dto {

    public static List<IAttachmentToken> createTokens(Collection<? extends Entity> dbos){
        List<IAttachmentToken> tokens = new ArrayList<>(safeCountOf(dbos));

        if(nonNull(dbos)){
            for(Entity entity : dbos){
                tokens.add(createToken(entity));
            }
        }

        return tokens;
    }

    public static IAttachmentToken createToken(Entity entity){
        if(entity instanceof DocumentEntity){
            DocumentEntity document = (DocumentEntity) entity;
            return AttachmentsTokenCreator.ofDocument(document.getId(), document.getOwnerId(), document.getAccessKey());
        }

        if(entity instanceof AudioEntity){
            AudioEntity audio = (AudioEntity) entity;
            return AttachmentsTokenCreator.ofAudio(audio.getId(), audio.getOwnerId(), audio.getAccessKey());
        }

        if(entity instanceof LinkEntity){
            return AttachmentsTokenCreator.ofLink(((LinkEntity) entity).getUrl());
        }

        if(entity instanceof PhotoEntity){
            PhotoEntity photo = (PhotoEntity) entity;
            return AttachmentsTokenCreator.ofPhoto(photo.getId(), photo.getOwnerId(), photo.getAccessKey());
        }

        if(entity instanceof PollEntity){
            PollEntity poll = (PollEntity) entity;
            return AttachmentsTokenCreator.ofPoll(poll.getId(), poll.getOwnerId());
        }

        if(entity instanceof PostEntity){
            PostEntity post = (PostEntity) entity;
            return AttachmentsTokenCreator.ofPost(post.getId(), post.getOwnerId());
        }

        if(entity instanceof VideoEntity){
            VideoEntity video = (VideoEntity) entity;
            return AttachmentsTokenCreator.ofVideo(video.getId(), video.getOwnerId(), video.getAccessKey());
        }

        throw new UnsupportedOperationException("Token for class " + entity.getClass() + " not supported");
    }
}