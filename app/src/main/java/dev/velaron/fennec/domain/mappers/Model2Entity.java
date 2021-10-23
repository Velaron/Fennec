package dev.velaron.fennec.domain.mappers;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.db.model.entity.AudioEntity;
import dev.velaron.fennec.db.model.entity.AudioMessageEntity;
import dev.velaron.fennec.db.model.entity.DocumentEntity;
import dev.velaron.fennec.db.model.entity.Entity;
import dev.velaron.fennec.db.model.entity.GiftItemEntity;
import dev.velaron.fennec.db.model.entity.LinkEntity;
import dev.velaron.fennec.db.model.entity.MessageEntity;
import dev.velaron.fennec.db.model.entity.PageEntity;
import dev.velaron.fennec.db.model.entity.PhotoEntity;
import dev.velaron.fennec.db.model.entity.PhotoSizeEntity;
import dev.velaron.fennec.db.model.entity.PollEntity;
import dev.velaron.fennec.db.model.entity.PostEntity;
import dev.velaron.fennec.db.model.entity.PrivacyEntity;
import dev.velaron.fennec.db.model.entity.StickerEntity;
import dev.velaron.fennec.db.model.entity.VideoEntity;
import dev.velaron.fennec.model.AbsModel;
import dev.velaron.fennec.model.Attachments;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.CryptStatus;
import dev.velaron.fennec.model.Document;
import dev.velaron.fennec.model.GiftItem;
import dev.velaron.fennec.model.Link;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.PhotoSizes;
import dev.velaron.fennec.model.Poll;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.PostSource;
import dev.velaron.fennec.model.SimplePrivacy;
import dev.velaron.fennec.model.Sticker;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.model.VoiceMessage;
import dev.velaron.fennec.model.WikiPage;

import static dev.velaron.fennec.domain.mappers.MapUtil.mapAll;
import static dev.velaron.fennec.domain.mappers.MapUtil.mapAndAdd;
import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;

/**
 * Created by Ruslan Kolbasa on 05.09.2017.
 * phoenix
 */
public class Model2Entity {

    public static MessageEntity buildMessageEntity(Message message) {
        return new MessageEntity(message.getId(), message.getPeerId(), message.getSenderId())
                .setDate(message.getDate())
                .setOut(message.isOut())
                .setBody(message.getBody())
                .setEncrypted(message.getCryptStatus() != CryptStatus.NO_ENCRYPTION)
                .setImportant(message.isImportant())
                .setDeleted(message.isDeleted())
                .setDeletedForAll(message.isDeletedForAll())
                .setForwardCount(message.getForwardMessagesCount())
                .setHasAttachmens(message.isHasAttachments())
                .setStatus(message.getStatus())
                .setOriginalId(message.getOriginalId())
                .setAction(message.getAction())
                .setActionMemberId(message.getActionMid())
                .setActionEmail(message.getActionEmail())
                .setActionText(message.getActionText())
                .setPhoto50(message.getPhoto50())
                .setPhoto100(message.getPhoto100())
                .setPhoto200(message.getPhoto200())
                .setRandomId(message.getRandomId())
                .setExtras(message.getExtras())
                .setAttachments(nonNull(message.getAttachments()) ? buildEntityAttachments(message.getAttachments()) : null)
                .setForwardMessages(mapAll(message.getFwd(), Model2Entity::buildMessageEntity, false))
                .setUpdateTime(message.getUpdateTime());
    }

    public static List<Entity> buildEntityAttachments(Attachments attachments) {
        List<Entity> entities = new ArrayList<>(attachments.size());
        mapAndAdd(attachments.getAudios(), Model2Entity::buildAudioEntity, entities);
        mapAndAdd(attachments.getStickers(), Model2Entity::buildStickerEntity, entities);
        mapAndAdd(attachments.getPhotos(), Model2Entity::buildPhotoEntity, entities);
        mapAndAdd(attachments.getDocs(), Model2Entity::buildDocumentDbo, entities);
        mapAndAdd(attachments.getVoiceMessages(), Model2Entity::mapAudio, entities);
        mapAndAdd(attachments.getVideos(), Model2Entity::buildVideoDbo, entities);
        mapAndAdd(attachments.getPosts(), Model2Entity::buildPostDbo, entities);
        mapAndAdd(attachments.getLinks(), Model2Entity::buildLinkDbo, entities);
        mapAndAdd(attachments.getPolls(), Model2Entity::buildPollDbo, entities);
        mapAndAdd(attachments.getPages(), Model2Entity::buildPageEntity, entities);
        mapAndAdd(attachments.getGifts(), Model2Entity::buildGiftItemEntity, entities);
        return entities;
    }

    public static List<Entity> buildDboAttachments(List<? extends AbsModel> models){
        List<Entity> entities = new ArrayList<>(models.size());

        for(AbsModel model : models){
            if(model instanceof Audio){
                entities.add(buildAudioEntity((Audio) model));
            } else if(model instanceof Sticker){
                entities.add(buildStickerEntity((Sticker) model));
            } else if(model instanceof Photo){
                entities.add(buildPhotoEntity((Photo) model));
            } else if(model instanceof Document){
                entities.add(buildDocumentDbo((Document) model));
            } else if(model instanceof Video){
                entities.add(buildVideoDbo((Video) model));
            } else if(model instanceof Post){
                entities.add(buildPostDbo((Post) model));
            } else if(model instanceof Link){
                entities.add(buildLinkDbo((Link) model));
            } else if(model instanceof Poll){
                entities.add(buildPollDbo((Poll) model));
            } else if (model instanceof WikiPage) {
                entities.add(buildPageEntity((WikiPage) model));
            } else if (model instanceof GiftItem) {
                entities.add(buildGiftItemEntity((GiftItem) model));
            } else {
                throw new UnsupportedOperationException("Unsupported model");
            }
        }

        return entities;
    }

    public static GiftItemEntity buildGiftItemEntity(GiftItem giftItem) {
        return new GiftItemEntity(giftItem.getId())
                .setThumb256(giftItem.getThumb256())
                .setThumb96(giftItem.getThumb96())
                .setThumb48(giftItem.getThumb48());
    }

    public static PageEntity buildPageEntity(WikiPage page) {
        return new PageEntity(page.getId(), page.getOwnerId())
                .setViewUrl(page.getViewUrl())
                .setViews(page.getViews())
                .setParent2(page.getParent2())
                .setParent(page.getParent())
                .setCreationTime(page.getCreationTime())
                .setEditionTime(page.getEditionTime())
                .setCreatorId(page.getCreatorId())
                .setSource(page.getSource());
    }

    public static PollEntity.Answer mapAnswer(Poll.Answer answer) {
        return new PollEntity.Answer(answer.getId(), answer.getText(), answer.getVoteCount(), answer.getRate());
    }

    public static PollEntity buildPollDbo(Poll poll){
        return new PollEntity(poll.getId(), poll.getOwnerId())
                .setAnswers(mapAll(poll.getAnswers(), Model2Entity::mapAnswer, false))
                .setQuestion(poll.getQuestion())
                .setVoteCount(poll.getVoteCount())
                .setMyAnswerIds(poll.getMyAnswerIds())
                .setCreationTime(poll.getCreationTime())
                .setAnonymous(poll.isAnonymous())
                .setBoard(poll.isBoard())
                .setClosed(poll.isClosed())
                .setAuthorId(poll.getAuthorId())
                .setCanVote(poll.isCanVote())
                .setCanEdit(poll.isCanEdit())
                .setCanReport(poll.isCanReport())
                .setCanShare(poll.isCanShare())
                .setEndDate(poll.getEndDate())
                .setMultiple(poll.isMultiple());
    }

    public static LinkEntity buildLinkDbo(Link link){
        return new LinkEntity(link.getUrl())
                .setPhoto(isNull(link.getPhoto()) ? null : buildPhotoEntity(link.getPhoto()))
                .setTitle(link.getTitle())
                .setDescription(link.getDescription())
                .setCaption(link.getCaption());
    }

    public static PostEntity buildPostDbo(Post post){
        PostEntity dbo = new PostEntity(post.getVkid(), post.getOwnerId())
                .setFromId(post.getAuthorId())
                .setDate(post.getDate())
                .setText(post.getText())
                .setReplyOwnerId(post.getReplyOwnerId())
                .setReplyPostId(post.getReplyPostId())
                .setFriendsOnly(post.isFriendsOnly())
                .setCommentsCount(post.getCommentsCount())
                .setCanPostComment(post.isCanPostComment())
                .setLikesCount(post.getLikesCount())
                .setUserLikes(post.isUserLikes())
                .setCanLike(post.isCanLike())
                .setCanEdit(post.isCanEdit())
                .setCanPublish(post.isCanRepost())
                .setRepostCount(post.getRepostCount())
                .setUserReposted(post.isUserReposted())
                .setPostType(post.getPostType())
                .setAttachmentsCount(nonNull(post.getAttachments()) ? post.getAttachments().size() : 0)
                .setSignedId(post.getSignerId())
                .setCreatedBy(post.getCreatorId())
                .setCanPin(post.isCanPin())
                .setPinned(post.isPinned())
                .setDeleted(post.isDeleted())
                .setViews(post.getViewCount())
                .setDbid(post.getDbid());

        PostSource source = post.getSource();
        if(nonNull(source)){
            dbo.setSource(new PostEntity.SourceDbo(source.getType(), source.getPlatform(), source.getData(), source.getUrl()));
        }

        if(nonNull(post.getAttachments())){
            dbo.setAttachments(buildEntityAttachments(post.getAttachments()));
        } else {
            dbo.setAttachments(Collections.emptyList());
        }

        dbo.setCopyHierarchy(mapAll(post.getCopyHierarchy(), Model2Entity::buildPostDbo, false));
        return dbo;
    }

    public static VideoEntity buildVideoDbo(Video video){
        return new VideoEntity(video.getId(), video.getOwnerId())
                .setAlbumId(video.getAlbumId())
                .setTitle(video.getTitle())
                .setDescription(video.getDescription())
                .setLink(video.getLink())
                .setDate(video.getDate())
                .setAddingDate(video.getAddingDate())
                .setViews(video.getViews())
                .setPlayer(video.getPlayer())
                .setPhoto130(video.getPhoto130())
                .setPhoto320(video.getPhoto320())
                .setPhoto800(video.getPhoto800())
                .setAccessKey(video.getAccessKey())
                .setCommentsCount(video.getCommentsCount())
                .setUserLikes(video.isUserLikes())
                .setLikesCount(video.getLikesCount())
                .setMp4link240(video.getMp4link240())
                .setMp4link360(video.getMp4link360())
                .setMp4link480(video.getMp4link480())
                .setMp4link720(video.getMp4link720())
                .setMp4link1080(video.getMp4link1080())
                .setExternalLink(video.getExternalLink())
                .setPlatform(video.getPlatform())
                .setRepeat(video.isRepeat())
                .setDuration(video.getDuration())
                .setPrivacyView(isNull(video.getPrivacyView()) ? null : mapPrivacy(video.getPrivacyView()))
                .setPrivacyComment(isNull(video.getPrivacyComment()) ? null : mapPrivacy(video.getPrivacyComment()))
                .setCanEdit(video.isCanEdit())
                .setCanAdd(video.isCanAdd())
                .setCanComment(video.isCanComment())
                .setCanRepost(video.isCanRepost());
    }

    public static PrivacyEntity mapPrivacy(SimplePrivacy privacy){
        return new PrivacyEntity(privacy.getType(), mapAll(privacy.getEntries(), orig -> new PrivacyEntity.Entry(orig.getType(), orig.getId(), orig.isAllowed())));
    }

    public static AudioMessageEntity mapAudio(VoiceMessage message){
        return new AudioMessageEntity(message.getId(), message.getOwnerId())
                .setWaveform(message.getWaveform())
                .setLinkOgg(message.getLinkOgg())
                .setLinkMp3(message.getLinkMp3())
                .setDuration(message.getDuration())
                .setAccessKey(message.getAccessKey());
    }

    public static DocumentEntity buildDocumentDbo(Document document){
        DocumentEntity dbo = new DocumentEntity(document.getId(), document.getOwnerId())
                .setTitle(document.getTitle())
                .setSize(document.getSize())
                .setExt(document.getExt())
                .setUrl(document.getUrl())
                .setDate(document.getDate())
                .setType(document.getType())
                .setAccessKey(document.getAccessKey());

        if(nonNull(document.getGraffiti())){
            Document.Graffiti graffiti = document.getGraffiti();
            dbo.setGraffiti(new DocumentEntity.GraffitiDbo(graffiti.getSrc(), graffiti.getWidth(), graffiti.getHeight()));
        }

        if(nonNull(document.getVideoPreview())){
            Document.VideoPreview video = document.getVideoPreview();
            dbo.setVideo(new DocumentEntity.VideoPreviewDbo(video.getSrc(), video.getWidth(), video.getHeight(), video.getFileSize()));
        }

        return dbo;
    }

    public static StickerEntity buildStickerEntity(Sticker sticker) {
        return new StickerEntity(sticker.getId())
                .setImagesWithBackground(mapAll(sticker.getImagesWithBackground(), Model2Entity::map))
                .setImagesWithBackground(mapAll(sticker.getImages(), Model2Entity::map));
    }

    public static StickerEntity.Img map(Sticker.Image image){
        return new StickerEntity.Img(image.getUrl(), image.getWidth(), image.getHeight());
    }

    public static AudioEntity buildAudioEntity(Audio audio) {
        return new AudioEntity(audio.getId(), audio.getOwnerId())
                .setArtist(audio.getArtist())
                .setTitle(audio.getTitle())
                .setDuration(audio.getDuration())
                .setUrl(audio.getUrl())
                .setLyricsId(audio.getLyricsId())
                .setAlbumId(audio.getAlbumId())
                .setGenre(audio.getGenre())
                .setAccessKey(audio.getAccessKey())
                .setDeleted(audio.isDeleted());
    }

    public static PhotoEntity buildPhotoEntity(Photo photo) {
        return new PhotoEntity(photo.getId(), photo.getOwnerId())
                .setAlbumId(photo.getAlbumId())
                .setWidth(photo.getWidth())
                .setHeight(photo.getHeight())
                .setText(photo.getText())
                .setDate(photo.getDate())
                .setUserLikes(photo.isUserLikes())
                .setCanComment(photo.isCanComment())
                .setLikesCount(photo.getLikesCount())
                .setCommentsCount(photo.getCommentsCount())
                .setTagsCount(photo.getTagsCount())
                .setAccessKey(photo.getAccessKey())
                .setPostId(photo.getPostId())
                .setDeleted(photo.isDeleted())
                .setSizes(isNull(photo.getSizes()) ? null : buildPhotoSizeEntity(photo.getSizes()));
    }

    private static PhotoSizeEntity.Size model2entityNullable(@Nullable PhotoSizes.Size size) {
        if (nonNull(size)) {
            return new PhotoSizeEntity.Size()
                    .setUrl(size.getUrl())
                    .setW(size.getW())
                    .setH(size.getH());
        }
        return null;
    }

    public static PhotoSizeEntity buildPhotoSizeEntity(PhotoSizes sizes) {
        return new PhotoSizeEntity()
                .setS(model2entityNullable(sizes.getS()))
                .setM(model2entityNullable(sizes.getM()))
                .setX(model2entityNullable(sizes.getX()))
                .setO(model2entityNullable(sizes.getO()))
                .setP(model2entityNullable(sizes.getP()))
                .setQ(model2entityNullable(sizes.getQ()))
                .setR(model2entityNullable(sizes.getR()))
                .setY(model2entityNullable(sizes.getY()))
                .setZ(model2entityNullable(sizes.getZ()))
                .setW(model2entityNullable(sizes.getW()));
    }
}