package dev.velaron.fennec.domain.mappers;

import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.api.model.AttachmentsTokenCreator;
import dev.velaron.fennec.api.model.IAttachmentToken;
import dev.velaron.fennec.model.AbsModel;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.model.Link;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.Video;

/**
 * Created by Ruslan Kolbasa on 05.09.2017.
 * phoenix
 */
public class Model2Dto {

    /*public static List<IAttachmentToken> createTokens(Attachments attachments){
        List<IAttachmentToken> tokens = new ArrayList<>(nonNull(attachments) ? attachments.size() : 0);

        if(nonNull(attachments)){
            tokens.addAll(createTokens(attachments.getAudios()));
            tokens.addAll(createTokens(attachments.getPhotos()));
            tokens.addAll(createTokens(attachments.getDocs()));
            tokens.addAll(createTokens(attachments.getVideos()));
            tokens.addAll(createTokens(attachments.getPosts()));
            tokens.addAll(createTokens(attachments.getLinks()));
            tokens.addAll(createTokens(attachments.getPolls()));
            tokens.addAll(createTokens(attachments.getPages()));
            tokens.addAll(createTokens(attachments.getVoiceMessages()));
        }

        return tokens;
    }*/

    public static List<IAttachmentToken> createTokens(Collection<? extends AbsModel> models){
        return MapUtil.mapAll(models, Model2Dto::createToken);
    }

    public static IAttachmentToken createToken(AbsModel model){
        if(model instanceof Document){
            Document document = (Document) model;
            return AttachmentsTokenCreator.ofDocument(document.getId(), document.getOwnerId(), document.getAccessKey());
        }

        if(model instanceof Audio){
            Audio audio = (Audio) model;
            return AttachmentsTokenCreator.ofAudio(audio.getId(), audio.getOwnerId(), audio.getAccessKey());
        }

        if(model instanceof Link){
            return AttachmentsTokenCreator.ofLink(((Link) model).getUrl());
        }

        if(model instanceof Photo){
            Photo photo = (Photo) model;
            return AttachmentsTokenCreator.ofPhoto(photo.getId(), photo.getOwnerId(), photo.getAccessKey());
        }

        if(model instanceof Poll){
            Poll poll = (Poll) model;
            return AttachmentsTokenCreator.ofPoll(poll.getId(), poll.getOwnerId());
        }

        if(model instanceof Post){
            Post post = (Post) model;
            return AttachmentsTokenCreator.ofPost(post.getVkid(), post.getOwnerId());
        }

        if(model instanceof Video){
            Video video = (Video) model;
            return AttachmentsTokenCreator.ofVideo(video.getId(), video.getOwnerId(), video.getAccessKey());
        }

        throw new UnsupportedOperationException("Token for class " + model.getClass() + " not supported");
    }
}