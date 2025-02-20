package dev.velaron.fennec.domain.mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.api.model.Commentable;
import dev.velaron.fennec.api.model.Likeable;
import dev.velaron.fennec.api.model.PhotoSizeDto;
import dev.velaron.fennec.api.model.VKApiAttachment;
import dev.velaron.fennec.api.model.VKApiAudio;
import dev.velaron.fennec.api.model.VKApiCareer;
import dev.velaron.fennec.api.model.VKApiCity;
import dev.velaron.fennec.api.model.VKApiComment;
import dev.velaron.fennec.api.model.VKApiCommunity;
import dev.velaron.fennec.api.model.VKApiCountry;
import dev.velaron.fennec.api.model.VKApiGift;
import dev.velaron.fennec.api.model.VKApiGiftItem;
import dev.velaron.fennec.api.model.VKApiLink;
import dev.velaron.fennec.api.model.VKApiMessage;
import dev.velaron.fennec.api.model.VKApiMilitary;
import dev.velaron.fennec.api.model.VKApiNews;
import dev.velaron.fennec.api.model.VKApiPhoto;
import dev.velaron.fennec.api.model.VKApiPhotoAlbum;
import dev.velaron.fennec.api.model.VKApiPoll;
import dev.velaron.fennec.api.model.VKApiPost;
import dev.velaron.fennec.api.model.VKApiSchool;
import dev.velaron.fennec.api.model.VKApiSticker;
import dev.velaron.fennec.api.model.VKApiStickerSet;
import dev.velaron.fennec.api.model.VKApiTopic;
import dev.velaron.fennec.api.model.VKApiUniversity;
import dev.velaron.fennec.api.model.VKApiUser;
import dev.velaron.fennec.api.model.VKApiVideo;
import dev.velaron.fennec.api.model.VKApiVideoAlbum;
import dev.velaron.fennec.api.model.VKApiWikiPage;
import dev.velaron.fennec.api.model.VkApiAttachments;
import dev.velaron.fennec.api.model.VkApiAudioMessage;
import dev.velaron.fennec.api.model.VkApiConversation;
import dev.velaron.fennec.api.model.VkApiDialog;
import dev.velaron.fennec.api.model.VkApiDoc;
import dev.velaron.fennec.api.model.VkApiPostSource;
import dev.velaron.fennec.api.model.VkApiPrivacy;
import dev.velaron.fennec.api.model.feedback.Copies;
import dev.velaron.fennec.api.model.feedback.VkApiBaseFeedback;
import dev.velaron.fennec.api.model.feedback.VkApiCommentFeedback;
import dev.velaron.fennec.api.model.feedback.VkApiCopyFeedback;
import dev.velaron.fennec.api.model.feedback.VkApiLikeCommentFeedback;
import dev.velaron.fennec.api.model.feedback.VkApiLikeFeedback;
import dev.velaron.fennec.api.model.feedback.VkApiMentionCommentFeedback;
import dev.velaron.fennec.api.model.feedback.VkApiMentionWallFeedback;
import dev.velaron.fennec.api.model.feedback.VkApiReplyCommentFeedback;
import dev.velaron.fennec.api.model.feedback.VkApiUsersFeedback;
import dev.velaron.fennec.api.model.feedback.VkApiWallFeedback;
import dev.velaron.fennec.api.model.response.FavePageResponse;
import dev.velaron.fennec.crypt.CryptHelper;
import dev.velaron.fennec.crypt.MessageType;
import dev.velaron.fennec.db.model.IdPairEntity;
import dev.velaron.fennec.db.model.entity.AudioEntity;
import dev.velaron.fennec.db.model.entity.AudioMessageEntity;
import dev.velaron.fennec.db.model.entity.CareerEntity;
import dev.velaron.fennec.db.model.entity.CityEntity;
import dev.velaron.fennec.db.model.entity.CommentEntity;
import dev.velaron.fennec.db.model.entity.CommunityEntity;
import dev.velaron.fennec.db.model.entity.CopiesEntity;
import dev.velaron.fennec.db.model.entity.CountryEntity;
import dev.velaron.fennec.db.model.entity.DialogEntity;
import dev.velaron.fennec.db.model.entity.DocumentEntity;
import dev.velaron.fennec.db.model.entity.Entity;
import dev.velaron.fennec.db.model.entity.FavePageEntity;
import dev.velaron.fennec.db.model.entity.GiftEntity;
import dev.velaron.fennec.db.model.entity.GiftItemEntity;
import dev.velaron.fennec.db.model.entity.LinkEntity;
import dev.velaron.fennec.db.model.entity.MessageEntity;
import dev.velaron.fennec.db.model.entity.MilitaryEntity;
import dev.velaron.fennec.db.model.entity.NewsEntity;
import dev.velaron.fennec.db.model.entity.OwnerEntities;
import dev.velaron.fennec.db.model.entity.PageEntity;
import dev.velaron.fennec.db.model.entity.PhotoAlbumEntity;
import dev.velaron.fennec.db.model.entity.PhotoEntity;
import dev.velaron.fennec.db.model.entity.PhotoSizeEntity;
import dev.velaron.fennec.db.model.entity.PollEntity;
import dev.velaron.fennec.db.model.entity.PostEntity;
import dev.velaron.fennec.db.model.entity.PrivacyEntity;
import dev.velaron.fennec.db.model.entity.SchoolEntity;
import dev.velaron.fennec.db.model.entity.SimpleDialogEntity;
import dev.velaron.fennec.db.model.entity.StickerEntity;
import dev.velaron.fennec.db.model.entity.StickerSetEntity;
import dev.velaron.fennec.db.model.entity.TopicEntity;
import dev.velaron.fennec.db.model.entity.UniversityEntity;
import dev.velaron.fennec.db.model.entity.UserDetailsEntity;
import dev.velaron.fennec.db.model.entity.UserEntity;
import dev.velaron.fennec.db.model.entity.VideoAlbumEntity;
import dev.velaron.fennec.db.model.entity.VideoEntity;
import dev.velaron.fennec.db.model.entity.feedback.CopyEntity;
import dev.velaron.fennec.db.model.entity.feedback.FeedbackEntity;
import dev.velaron.fennec.db.model.entity.feedback.LikeCommentEntity;
import dev.velaron.fennec.db.model.entity.feedback.LikeEntity;
import dev.velaron.fennec.db.model.entity.feedback.MentionCommentEntity;
import dev.velaron.fennec.db.model.entity.feedback.MentionEntity;
import dev.velaron.fennec.db.model.entity.feedback.NewCommentEntity;
import dev.velaron.fennec.db.model.entity.feedback.PostFeedbackEntity;
import dev.velaron.fennec.db.model.entity.feedback.ReplyCommentEntity;
import dev.velaron.fennec.db.model.entity.feedback.UsersEntity;
import dev.velaron.fennec.model.CommentedType;
import dev.velaron.fennec.model.Message;
import dev.velaron.fennec.model.MessageStatus;
import dev.velaron.fennec.model.feedback.FeedbackType;
import dev.velaron.fennec.util.Utils;

import static dev.velaron.fennec.domain.mappers.MapUtil.calculateConversationAcl;
import static dev.velaron.fennec.domain.mappers.MapUtil.mapAll;
import static dev.velaron.fennec.util.Objects.isNull;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.listEmptyIfNull;
import static dev.velaron.fennec.util.Utils.nonEmpty;

/**
 * Created by Ruslan Kolbasa on 04.09.2017.
 * phoenix
 */
public class Dto2Entity {

    public static FeedbackEntity buildFeedbackDbo(VkApiBaseFeedback feedback) {
        @FeedbackType
        int type = FeedbackEntity2Model.transformType(feedback.type);

        switch (type) {
            case FeedbackType.FOLLOW:
            case FeedbackType.FRIEND_ACCEPTED:
                VkApiUsersFeedback usersNotifcation = (VkApiUsersFeedback) feedback;

                final UsersEntity usersDbo = new UsersEntity(type);
                usersDbo.setOwners(usersNotifcation.users.ids);
                usersDbo.setDate(feedback.date);
                return usersDbo;

            case FeedbackType.MENTION:
                VkApiMentionWallFeedback mentionWallFeedback = (VkApiMentionWallFeedback) feedback;

                final MentionEntity mentionDbo = new MentionEntity(type);
                PostEntity post = mapPost(mentionWallFeedback.post);
                mentionDbo.setWhere(post);

                if (nonNull(feedback.reply)) {
                    mentionDbo.setReply(mapComment(post.getId(), post.getOwnerId(), CommentedType.POST, null, feedback.reply));
                }

                mentionDbo.setDate(feedback.date);
                return mentionDbo;

            case FeedbackType.MENTION_COMMENT_POST:
            case FeedbackType.MENTION_COMMENT_PHOTO:
            case FeedbackType.MENTION_COMMENT_VIDEO:
                VkApiMentionCommentFeedback mentionCommentFeedback = (VkApiMentionCommentFeedback) feedback;
                CEntity entity = createFromCommentable(mentionCommentFeedback.comment_of);

                final MentionCommentEntity mentionCommentDbo = new MentionCommentEntity(type);
                mentionCommentDbo.setDate(feedback.date);
                mentionCommentDbo.setCommented(entity.entity);
                mentionCommentDbo.setWhere(mapComment(entity.id, entity.ownerId, entity.type, entity.accessKey, mentionCommentFeedback.where));

                if (nonNull(feedback.reply)) {
                    mentionCommentDbo.setReply(mapComment(entity.id, entity.ownerId, entity.type, entity.accessKey, feedback.reply));
                }

                return mentionCommentDbo;

            case FeedbackType.WALL:
            case FeedbackType.WALL_PUBLISH:
                VkApiWallFeedback wallFeedback = (VkApiWallFeedback) feedback;
                PostEntity postEntity = mapPost(wallFeedback.post);

                final PostFeedbackEntity postFeedbackEntity = new PostFeedbackEntity(type);
                postFeedbackEntity.setDate(feedback.date);
                postFeedbackEntity.setPost(postEntity);

                if (nonNull(feedback.reply)) {
                    postFeedbackEntity.setReply(mapComment(postEntity.getId(), postEntity.getOwnerId(), CommentedType.POST, null, feedback.reply));
                }

                return postFeedbackEntity;

            case FeedbackType.COMMENT_POST:
            case FeedbackType.COMMENT_PHOTO:
            case FeedbackType.COMMENT_VIDEO:
                VkApiCommentFeedback commentFeedback = (VkApiCommentFeedback) feedback;
                CEntity commented = createFromCommentable(commentFeedback.comment_of);

                final NewCommentEntity commentEntity = new NewCommentEntity(type);
                commentEntity.setComment(mapComment(commented.id, commented.ownerId, commented.type, commented.accessKey, commentFeedback.comment));
                commentEntity.setCommented(commented.entity);
                commentEntity.setDate(feedback.date);

                if (nonNull(feedback.reply)) {
                    commentEntity.setReply(mapComment(commented.id, commented.ownerId, commented.type, commented.accessKey, feedback.reply));
                }

                return commentEntity;

            case FeedbackType.REPLY_COMMENT:
            case FeedbackType.REPLY_COMMENT_PHOTO:
            case FeedbackType.REPLY_COMMENT_VIDEO:
            case FeedbackType.REPLY_TOPIC:
                VkApiReplyCommentFeedback replyCommentFeedback = (VkApiReplyCommentFeedback) feedback;
                CEntity c = createFromCommentable(replyCommentFeedback.comments_of);

                final ReplyCommentEntity replyCommentEntity = new ReplyCommentEntity(type);
                replyCommentEntity.setDate(feedback.date);
                replyCommentEntity.setCommented(c.entity);
                replyCommentEntity.setFeedbackComment(mapComment(c.id, c.ownerId, c.type, c.accessKey, replyCommentFeedback.feedback_comment));

                if (nonNull(replyCommentFeedback.own_comment)) {
                    replyCommentEntity.setOwnComment(mapComment(c.id, c.ownerId, c.type, c.accessKey, replyCommentFeedback.own_comment));
                }

                if (nonNull(feedback.reply)) {
                    replyCommentEntity.setReply(mapComment(c.id, c.ownerId, c.type, c.accessKey, feedback.reply));
                }

                return replyCommentEntity;

            case FeedbackType.LIKE_POST:
            case FeedbackType.LIKE_PHOTO:
            case FeedbackType.LIKE_VIDEO:
                VkApiLikeFeedback likeFeedback = (VkApiLikeFeedback) feedback;

                final LikeEntity likeEntity = new LikeEntity(type);
                likeEntity.setLiked(createFromLikeable(likeFeedback.liked));
                likeEntity.setLikesOwnerIds(likeFeedback.users.ids);
                likeEntity.setDate(feedback.date);
                return likeEntity;

            case FeedbackType.LIKE_COMMENT_POST:
            case FeedbackType.LIKE_COMMENT_PHOTO:
            case FeedbackType.LIKE_COMMENT_VIDEO:
            case FeedbackType.LIKE_COMMENT_TOPIC:
                VkApiLikeCommentFeedback likeCommentFeedback = (VkApiLikeCommentFeedback) feedback;
                CEntity ce = createFromCommentable(likeCommentFeedback.commented);

                final LikeCommentEntity likeCommentEntity = new LikeCommentEntity(type);
                likeCommentEntity.setCommented(ce.entity);
                likeCommentEntity.setLiked(mapComment(ce.id, ce.ownerId, ce.type, ce.accessKey, likeCommentFeedback.comment));
                likeCommentEntity.setDate(feedback.date);
                likeCommentEntity.setLikesOwnerIds(likeCommentFeedback.users.ids);
                return likeCommentEntity;

            case FeedbackType.COPY_POST:
            case FeedbackType.COPY_PHOTO:
            case FeedbackType.COPY_VIDEO:
                VkApiCopyFeedback copyFeedback = (VkApiCopyFeedback) feedback;

                final CopyEntity copyEntity = new CopyEntity(type);
                copyEntity.setDate(feedback.date);

                if (type == FeedbackType.COPY_POST) {
                    copyEntity.setCopied(mapPost((VKApiPost) copyFeedback.what));
                } else if (type == FeedbackType.COPY_PHOTO) {
                    copyEntity.setCopied(mapPhoto((VKApiPhoto) copyFeedback.what));
                } else {
                    copyEntity.setCopied(mapVideo((VKApiVideo) copyFeedback.what));
                }

                List<Copies.IdPair> copyPairs = listEmptyIfNull(copyFeedback.copies.pairs);

                CopiesEntity copiesEntity = new CopiesEntity();
                copiesEntity.setPairDbos(new ArrayList<>(copyPairs.size()));

                for (Copies.IdPair idPair : copyPairs) {
                    copiesEntity.getPairDbos().add(new IdPairEntity(idPair.id, idPair.owner_id));
                }

                copyEntity.setCopies(copiesEntity);
                return copyEntity;
        }

        throw new UnsupportedOperationException("Unsupported feedback type: " + feedback.type);
    }

    private static final class CEntity {

        final int id;
        final int ownerId;
        final int type;
        final String accessKey;
        final Entity entity;

        private CEntity(int id, int ownerId, int type, String accessKey, Entity entity) {
            this.id = id;
            this.ownerId = ownerId;
            this.type = type;
            this.accessKey = accessKey;
            this.entity = entity;
        }
    }

    private static Entity createFromLikeable(Likeable likeable) {
        if (likeable instanceof VKApiPost) {
            return mapPost((VKApiPost) likeable);
        }

        if (likeable instanceof VKApiPhoto) {
            return mapPhoto((VKApiPhoto) likeable);
        }

        if (likeable instanceof VKApiVideo) {
            return mapVideo((VKApiVideo) likeable);
        }

        throw new UnsupportedOperationException("Unsupported commentable type: " + likeable);
    }

    private static CEntity createFromCommentable(Commentable commentable) {
        if (commentable instanceof VKApiPost) {
            PostEntity entity = mapPost((VKApiPost) commentable);
            return new CEntity(entity.getId(), entity.getOwnerId(), CommentedType.POST, null, entity);
        }

        if (commentable instanceof VKApiPhoto) {
            PhotoEntity entity = mapPhoto((VKApiPhoto) commentable);
            return new CEntity(entity.getId(), entity.getOwnerId(), CommentedType.PHOTO, entity.getAccessKey(), entity);
        }

        if (commentable instanceof VKApiVideo) {
            VideoEntity entity = mapVideo((VKApiVideo) commentable);
            return new CEntity(entity.getId(), entity.getOwnerId(), CommentedType.VIDEO, entity.getAccessKey(), entity);
        }

        if (commentable instanceof VKApiTopic) {
            TopicEntity entity = buildTopicDbo((VKApiTopic) commentable);
            return new CEntity(entity.getId(), entity.getOwnerId(), CommentedType.TOPIC, null, entity);
        }

        throw new UnsupportedOperationException("Unsupported commentable type: " + commentable);
    }

    public static VideoAlbumEntity buildVideoAlbumDbo(VKApiVideoAlbum dto) {
        return new VideoAlbumEntity(dto.id, dto.owner_id)
                .setUpdateTime(dto.updated_time)
                .setCount(dto.count)
                .setPhoto160(dto.photo_160)
                .setPhoto320(dto.photo_320)
                .setTitle(dto.title)
                .setPrivacy(nonNull(dto.privacy) ? mapPrivacy(dto.privacy) : null);
    }

    public static TopicEntity buildTopicDbo(VKApiTopic dto) {
        return new TopicEntity(dto.id, dto.owner_id)
                .setTitle(dto.title)
                .setCreatedTime(dto.created)
                .setCreatorId(dto.created_by)
                .setLastUpdateTime(dto.updated)
                .setUpdatedBy(dto.updated_by)
                .setClosed(dto.is_closed)
                .setFixed(dto.is_fixed)
                .setCommentsCount(nonNull(dto.comments) ? dto.comments.count : 0)
                .setFirstComment(dto.first_comment)
                .setLastComment(dto.last_comment)
                .setPoll(null);
    }

    public static PhotoAlbumEntity buildPhotoAlbumDbo(VKApiPhotoAlbum dto) {
        return new PhotoAlbumEntity(dto.id, dto.owner_id)
                .setTitle(dto.title)
                .setSize(dto.size)
                .setDescription(dto.description)
                .setCanUpload(dto.can_upload)
                .setUpdatedTime(dto.updated)
                .setCreatedTime(dto.created)
                .setSizes(nonNull(dto.photo) ? mapPhotoSizes(dto.photo) : null)
                .setCommentsDisabled(dto.comments_disabled)
                .setUploadByAdminsOnly(dto.upload_by_admins_only)
                .setPrivacyView(nonNull(dto.privacy_view) ? mapPrivacy(dto.privacy_view) : null)
                .setPrivacyComment(nonNull(dto.privacy_comment) ? mapPrivacy(dto.privacy_comment) : null);
    }

    public static OwnerEntities mapOwners(List<VKApiUser> users, List<VKApiCommunity> communities) {
        return new OwnerEntities(mapUsers(users), mapCommunities(communities));
    }

    public static List<CommunityEntity> mapCommunities(List<VKApiCommunity> communities) {
        return mapAll(communities, Dto2Entity::mapCommunity);
    }

    public static List<UserEntity> mapUsers(List<VKApiUser> users) {
        return mapAll(users, Dto2Entity::mapUser, true);
    }

    public static CommunityEntity mapCommunity(VKApiCommunity community) {
        return new CommunityEntity(community.id)
                .setName(community.name)
                .setScreenName(community.screen_name)
                .setClosed(community.is_closed)
                .setAdmin(community.is_admin)
                .setAdminLevel(community.admin_level)
                .setMember(community.is_member)
                .setMemberStatus(community.member_status)
                .setType(community.type)
                .setPhoto50(community.photo_50)
                .setPhoto100(community.photo_100)
                .setPhoto200(community.photo_200);
    }

    public static FavePageEntity mapFavePage(FavePageResponse favePage) {
        int id = 0;
        if (favePage.user != null) {
            id = favePage.user.id;
        }
        if (favePage.group != null) {
            id = -favePage.group.id;
        }

        return new FavePageEntity(id)
                .setDescription(favePage.description)
                .setUpdateDate(favePage.updated_date)
                .setFaveType(favePage.type);
    }

    public static UserEntity mapUser(VKApiUser user) {
        return new UserEntity(user.id)
                .setFirstName(user.first_name)
                .setLastName(user.last_name)
                .setOnline(user.online)
                .setOnlineMobile(user.online_mobile)
                .setOnlineApp(user.online_app)
                .setPhoto50(user.photo_50)
                .setPhoto100(user.photo_100)
                .setPhoto200(user.photo_200)
                .setLastSeen(user.last_seen)
                .setPlatform(user.platform)
                .setStatus(user.status)
                .setSex(user.sex)
                .setDomain(user.domain)
                .setFriend(user.is_friend)
                .setFriendStatus(user.friend_status);
    }

    public static UserDetailsEntity mapUserDetails(VKApiUser user) {
        UserDetailsEntity dbo = new UserDetailsEntity();

        try {
            if (nonEmpty(user.photo_id)) {
                int dividerIndex = user.photo_id.indexOf("_");
                if (dividerIndex != -1) {
                    int photoId = Integer.parseInt(user.photo_id.substring(dividerIndex + 1));
                    dbo.setPhotoId(new IdPairEntity(photoId, user.id));
                }
            }
        } catch (Exception ignored) {

        }

        dbo.setStatusAudio(nonNull(user.status_audio) ? mapAudio(user.status_audio) : null);
        dbo.setBdate(user.bdate);
        dbo.setCity(isNull(user.city) ? null : mapCity(user.city));
        dbo.setCountry(isNull(user.country) ? null : mapCountry(user.country));
        dbo.setHomeTown(user.home_town);
        dbo.setPhone(user.mobile_phone);
        dbo.setHomePhone(user.home_phone);
        dbo.setSkype(user.skype);
        dbo.setInstagram(user.instagram);
        dbo.setFacebook(user.facebook);
        dbo.setTwitter(user.twitter);

        VKApiUser.Counters counters = user.counters;

        if (nonNull(counters)) {
            dbo.setFriendsCount(counters.friends)
                    .setOnlineFriendsCount(counters.online_friends)
                    .setMutualFriendsCount(counters.mutual_friends)
                    .setFollowersCount(counters.followers)
                    .setGroupsCount(counters.groups)
                    .setPhotosCount(counters.photos)
                    .setAudiosCount(counters.audios)
                    .setVideosCount(counters.videos)
                    .setAllWallCount(counters.all_wall)
                    .setOwnWallCount(counters.owner_wall)
                    .setPostponedWallCount(counters.postponed_wall);
        }

        dbo.setMilitaries(mapAll(user.militaries, Dto2Entity::mapMilitary));
        dbo.setCareers(mapAll(user.careers, Dto2Entity::mapCareer));
        dbo.setUniversities(mapAll(user.universities, Dto2Entity::mapUniversity));
        dbo.setSchools(mapAll(user.schools, Dto2Entity::mapSchool));
        dbo.setRelatives(mapAll(user.relatives, Dto2Entity::mapUserRelative));

        dbo.setRelation(user.relation);
        dbo.setRelationPartnerId(user.relation_partner == null ? 0 : user.relation_partner.id);
        dbo.setLanguages(user.langs);

        dbo.setPolitical(user.political);
        dbo.setPeopleMain(user.people_main);
        dbo.setLifeMain(user.life_main);
        dbo.setSmoking(user.smoking);
        dbo.setAlcohol(user.alcohol);
        dbo.setInspiredBy(user.inspired_by);
        dbo.setReligion(user.religion);
        dbo.setSite(user.site);
        dbo.setInterests(user.interests);
        dbo.setMusic(user.music);
        dbo.setActivities(user.activities);
        dbo.setMovies(user.movies);
        dbo.setTv(user.tv);
        dbo.setGames(user.games);
        dbo.setQuotes(user.quotes);
        dbo.setAbout(user.about);
        dbo.setBooks(user.books);
        return dbo;
    }

    public static UserDetailsEntity.RelativeEntity mapUserRelative(VKApiUser.Relative relative) {
        return new UserDetailsEntity.RelativeEntity()
                .setId(relative.id)
                .setType(relative.type)
                .setName(relative.name);
    }

    public static SchoolEntity mapSchool(VKApiSchool dto) {
        return new SchoolEntity()
                .setCityId(dto.city_id)
                .setClazz(dto.clazz)
                .setCountryId(dto.country_id)
                .setFrom(dto.year_from)
                .setTo(dto.year_to)
                .setYearGraduated(dto.year_graduated)
                .setId(dto.id)
                .setName(dto.name);
    }

    public static UniversityEntity mapUniversity(VKApiUniversity dto) {
        return new UniversityEntity()
                .setId(dto.id)
                .setCityId(dto.city_id)
                .setCountryId(dto.country_id)
                .setName(dto.name)
                .setStatus(dto.education_status)
                .setForm(dto.education_form)
                .setFacultyId(dto.faculty)
                .setFacultyName(dto.faculty_name)
                .setChairId(dto.chair)
                .setChairName(dto.chair_name)
                .setGraduationYear(dto.graduation);
    }

    public static MilitaryEntity mapMilitary(VKApiMilitary dto) {
        return new MilitaryEntity()
                .setCountryId(dto.country_id)
                .setFrom(dto.from)
                .setUnit(dto.unit)
                .setUnitId(dto.unit_id)
                .setUntil(dto.until);
    }

    public static CareerEntity mapCareer(VKApiCareer dto) {
        return new CareerEntity()
                .setCityId(dto.city_id)
                .setCompany(dto.company)
                .setCountryId(dto.country_id)
                .setFrom(dto.from)
                .setUntil(dto.until)
                .setPosition(dto.position)
                .setGroupId(dto.group_id);
    }

    public static CountryEntity mapCountry(VKApiCountry dto) {
        return new CountryEntity(dto.id, dto.title);
    }

    public static CityEntity mapCity(VKApiCity dto) {
        return new CityEntity()
                .setArea(dto.area)
                .setId(dto.id)
                .setImportant(dto.important)
                .setTitle(dto.title)
                .setRegion(dto.region);
    }

    public static NewsEntity mapNews(VKApiNews news) {
        NewsEntity entity = new NewsEntity()
                .setType(news.type)
                .setSourceId(news.source_id)
                .setDate(news.date)
                .setPostId(news.post_id)
                .setPostType(news.post_type)
                .setFinalPost(news.final_post)
                .setCopyOwnerId(news.copy_owner_id)
                .setCopyPostId(news.copy_post_id)
                .setCopyPostDate(news.copy_post_date)
                .setText(news.text)
                .setCanEdit(news.can_edit)
                .setCanDelete(news.can_delete)
                .setCommentCount(news.comment_count)
                .setCanPostComment(news.comment_can_post)
                .setLikesCount(news.like_count)
                .setUserLikes(news.user_like)
                .setCanLike(news.can_like)
                .setCanPublish(news.can_publish)
                .setRepostCount(news.reposts_count)
                .setUserReposted(news.user_reposted)
                .setGeoId(nonNull(news.geo) ? news.geo.id : 0)
                .setFriendsTags(news.friends)
                .setViews(news.views);

        if (news.hasAttachments()) {
            entity.setAttachments(mapAttachemntsList(news.attachments));
        } else {
            entity.setAttachments(Collections.emptyList());
        }

        entity.setCopyHistory(mapAll(news.copy_history, Dto2Entity::mapPost, false));
        return entity;
    }

    public static CommentEntity mapComment(int sourceId, int sourceOwnerId, int sourceType, String sourceAccessKey, VKApiComment comment) {
        List<Entity> attachmentsEntities;

        if (nonNull(comment.attachments)) {
            attachmentsEntities = mapAttachemntsList(comment.attachments);
        } else {
            attachmentsEntities = Collections.emptyList();
        }

        return new CommentEntity(sourceId, sourceOwnerId, sourceType, sourceAccessKey, comment.id)
                .setFromId(comment.from_id)
                .setDate(comment.date)
                .setText(comment.text)
                .setReplyToUserId(comment.reply_to_user)
                .setReplyToComment(comment.reply_to_comment)
                .setLikesCount(comment.likes)
                .setUserLikes(comment.user_likes)
                .setCanLike(comment.can_like)
                .setCanEdit(comment.can_edit)
                .setDeleted(false)
                .setAttachmentsCount(comment.getAttachmentsCount())
                .setAttachments(attachmentsEntities);
    }

    public static SimpleDialogEntity mapConversation(VkApiConversation dto) {
        SimpleDialogEntity entity = new SimpleDialogEntity(dto.peer.id)
                .setInRead(dto.inRead)
                .setOutRead(dto.outRead)
                .setUnreadCount(dto.unreadCount)
                .setLastMessageId(dto.lastMessageId)
                .setAcl(calculateConversationAcl(dto));

        if (nonNull(dto.settings)) {
            entity.setTitle(dto.settings.title);

            if (nonNull(dto.settings.pinnedMesage)) {
                entity.setPinned(mapMessage(dto.settings.pinnedMesage));
            }

            if (nonNull(dto.settings.photo)) {
                entity.setPhoto50(dto.settings.photo.photo50)
                        .setPhoto100(dto.settings.photo.photo100)
                        .setPhoto200(dto.settings.photo.photo200);
            }
        }

        return entity;
    }

    public static DialogEntity mapDialog(VkApiDialog dto) {
        MessageEntity messageEntity = mapMessage(dto.lastMessage);

        DialogEntity entity = new DialogEntity(messageEntity.getPeerId())
                .setLastMessageId(messageEntity.getId())
                .setMessage(messageEntity)
                .setInRead(dto.conversation.inRead)
                .setOutRead(dto.conversation.outRead)
                .setUnreadCount(dto.conversation.unreadCount)
                .setAcl(calculateConversationAcl(dto.conversation));

        if (nonNull(dto.conversation.settings)) {
            entity.setTitle(dto.conversation.settings.title);
            entity.setGroupChannel(dto.conversation.settings.is_group_channel);

            if (nonNull(dto.conversation.settings.pinnedMesage)) {
                entity.setPinned(mapMessage(dto.conversation.settings.pinnedMesage));
            }

            if (nonNull(dto.conversation.settings.photo)) {
                entity.setPhoto50(dto.conversation.settings.photo.photo50)
                        .setPhoto100(dto.conversation.settings.photo.photo100)
                        .setPhoto200(dto.conversation.settings.photo.photo200);
            }
        }

        return entity;
    }

    public static List<Entity> mapAttachemntsList(VkApiAttachments attachments) {
        List<VkApiAttachments.Entry> entries = attachments.entryList();

        if (entries.isEmpty()) {
            return Collections.emptyList();
        }

        if (entries.size() == 1) {
            return Collections.singletonList(mapAttachment(entries.get(0).attachment));
        }

        List<Entity> entities = new ArrayList<>(entries.size());

        for (VkApiAttachments.Entry entry : entries) {
            if (isNull(entry)) {
                // TODO: 04.10.2017
                continue;
            }

            entities.add(mapAttachment(entry.attachment));
        }

        return entities;
    }

    public static Entity mapAttachment(VKApiAttachment dto) {
        if (dto instanceof VKApiPhoto) {
            return mapPhoto((VKApiPhoto) dto);
        }

        if (dto instanceof VKApiVideo) {
            return mapVideo((VKApiVideo) dto);
        }

        if (dto instanceof VkApiDoc) {
            return mapDoc((VkApiDoc) dto);
        }

        if (dto instanceof VKApiLink) {
            return mapLink((VKApiLink) dto);
        }

        if (dto instanceof VKApiWikiPage) {
            return mapWikiPage((VKApiWikiPage) dto);
        }

        if (dto instanceof VKApiSticker) {
            return mapSticker((VKApiSticker) dto);
        }

        if (dto instanceof VKApiPost) {
            return mapPost((VKApiPost) dto);
        }

        if (dto instanceof VKApiPoll) {
            return buildPollEntity((VKApiPoll) dto);
        }

        if (dto instanceof VKApiAudio) {
            return mapAudio((VKApiAudio) dto);
        }

        if (dto instanceof VkApiAudioMessage) {
            return mapAudioMessage((VkApiAudioMessage) dto);
        }

        if (dto instanceof VKApiGiftItem) {
            return mapGiftItem((VKApiGiftItem) dto);
        }


        throw new UnsupportedOperationException("Unsupported attachment, class: " + dto.getClass());
    }

    public static AudioEntity mapAudio(VKApiAudio dto) {
        return new AudioEntity(dto.id, dto.owner_id)
                .setArtist(dto.artist)
                .setTitle(dto.title)
                .setDuration(dto.duration)
                .setUrl(dto.url)
                .setLyricsId(dto.lyrics_id)
                .setAlbumId(dto.album_id)
                .setGenre(dto.genre)
                .setAccessKey(dto.access_key);
    }

    public static PollEntity.Answer mapPollAnswer(VKApiPoll.Answer dto) {
        return new PollEntity.Answer(dto.id, dto.text, dto.votes, dto.rate);
    }

    public static PollEntity buildPollEntity(VKApiPoll dto) {
        return new PollEntity(dto.id, dto.owner_id)
                .setAnonymous(dto.anonymous)
                .setAnswers(mapAll(dto.answers, Dto2Entity::mapPollAnswer))
                .setBoard(dto.is_board)
                .setCreationTime(dto.created)
                .setMyAnswerIds(dto.answer_ids)
                .setVoteCount(dto.votes)
                .setQuestion(dto.question)
                .setClosed(dto.closed)
                .setAuthorId(dto.author_id)
                .setCanVote(dto.can_vote)
                .setCanEdit(dto.can_edit)
                .setCanReport(dto.can_report)
                .setCanShare(dto.can_share)
                .setEndDate(dto.end_date)
                .setMultiple(dto.multiple);
    }

    public static PostEntity mapPost(VKApiPost dto) {
        PostEntity dbo = new PostEntity(dto.id, dto.owner_id)
                .setFromId(dto.from_id)
                .setDate(dto.date)
                .setText(dto.text)
                .setReplyOwnerId(dto.reply_owner_id)
                .setReplyPostId(dto.reply_post_id)
                .setFriendsOnly(dto.friends_only)
                .setCommentsCount(nonNull(dto.comments) ? dto.comments.count : 0)
                .setCanPostComment(nonNull(dto.comments) && dto.comments.canPost)
                .setLikesCount(dto.likes_count)
                .setUserLikes(dto.user_likes)
                .setCanLike(dto.can_like)
                .setCanEdit(dto.can_edit)
                .setCanPublish(dto.can_publish)
                .setRepostCount(dto.reposts_count)
                .setUserReposted(dto.user_reposted)
                .setPostType(dto.post_type)
                .setAttachmentsCount(dto.getAttachmentsCount())
                .setSignedId(dto.signer_id)
                .setCreatedBy(dto.created_by)
                .setCanPin(dto.can_pin)
                .setPinned(dto.is_pinned)
                .setDeleted(false) // cant be deleted
                .setViews(dto.views);

        VkApiPostSource source = dto.post_source;
        if (nonNull(source)) {
            dbo.setSource(new PostEntity.SourceDbo(source.type, source.platform, source.data, source.url));
        }

        if (dto.hasAttachments()) {
            dbo.setAttachments(mapAttachemntsList(dto.attachments));
        } else {
            dbo.setAttachments(Collections.emptyList());
        }

        if (dto.hasCopyHistory()) {
            dbo.setCopyHierarchy(mapAll(dto.copy_history, Dto2Entity::mapPost));
        } else {
            dbo.setCopyHierarchy(Collections.emptyList());
        }

        return dbo;
    }

    public static StickerEntity mapSticker(VKApiSticker sticker) {
        return new StickerEntity(sticker.sticker_id)
                .setImages(mapAll(sticker.images, Dto2Entity::mapStickerImage))
                .setImagesWithBackground(mapAll(sticker.images_with_background, Dto2Entity::mapStickerImage))
                .setAnimationUrl(sticker.animation_url);
    }

    public static StickerSetEntity mapStikerSet(VKApiStickerSet.Product dto) {
        return new StickerSetEntity(dto.id)
                .setTitle(dto.title)
                .setPromoted(dto.promoted)
                .setActive(dto.active)
                .setPurchased(dto.purchased)
                .setPhoto35("https://vk.com/images/store/stickers/" + dto.id + "/cover_35b.png")
                .setPhoto70("https://vk.com/images/store/stickers/" + dto.id + "/cover_70b.png")
                .setPhoto140("https://vk.com/images/store/stickers/" + dto.id + "/cover_140b.png")
                .setStickers(mapAll(dto.stickers, Dto2Entity::mapSticker));
    }

    public static StickerEntity.Img mapStickerImage(VKApiSticker.Image dto) {
        return new StickerEntity.Img(dto.url, dto.width, dto.height);
    }

    public static PageEntity mapWikiPage(VKApiWikiPage dto) {
        return new PageEntity(dto.id, dto.owner_id)
                .setCreatorId(dto.creator_id)
                .setTitle(dto.title)
                .setSource(dto.source)
                .setEditionTime(dto.edited)
                .setCreationTime(dto.created)
                .setParent(dto.parent)
                .setParent2(dto.parent2)
                .setViews(dto.views)
                .setViewUrl(dto.view_url);
    }

    public static LinkEntity mapLink(VKApiLink link) {
        return new LinkEntity(link.url)
                .setCaption(link.caption)
                .setDescription(link.description)
                .setTitle(link.title)
                .setPhoto(nonNull(link.photo) ? mapPhoto(link.photo) : null);
    }

    public static AudioMessageEntity mapAudioMessage(VkApiAudioMessage dto) {
        return new AudioMessageEntity(dto.id, dto.owner_id)
                .setAccessKey(dto.access_key)
                .setDuration(dto.duration)
                .setLinkMp3(dto.linkMp3)
                .setLinkOgg(dto.linkOgg)
                .setWaveform(dto.waveform);
    }

    public static DocumentEntity mapDoc(VkApiDoc dto) {
        DocumentEntity dbo = new DocumentEntity(dto.id, dto.ownerId)
                .setTitle(dto.title)
                .setSize(dto.size)
                .setExt(dto.ext)
                .setUrl(dto.url)
                .setDate(dto.date)
                .setType(dto.type)
                .setAccessKey(dto.accessKey);

        if (nonNull(dto.preview)) {
            if (nonNull(dto.preview.photo) && nonNull(dto.preview.photo.sizes)) {
                dbo.setPhoto(mapPhotoSizes(dto.preview.photo.sizes));
            }

            if (nonNull(dto.preview.video)) {
                VkApiDoc.Video video = dto.preview.video;
                dbo.setVideo(new DocumentEntity.VideoPreviewDbo(video.src, video.width, video.height, video.fileSize));
            }

            if (nonNull(dto.preview.graffiti)) {
                VkApiDoc.Graffiti graffiti = dto.preview.graffiti;
                dbo.setGraffiti(new DocumentEntity.GraffitiDbo(graffiti.src, graffiti.width, graffiti.height));
            }
        }

        return dbo;
    }

    public static MessageEntity mapMessage(VKApiMessage dto) {
        boolean encrypted = CryptHelper.analizeMessageBody(dto.body) == MessageType.CRYPTED;

        int randomId = 0;
        try {
            randomId = Integer.parseInt(dto.random_id);
        } catch (NumberFormatException ignored) {
        }

        MessageEntity entity = new MessageEntity(dto.id, dto.peer_id, dto.from_id)
                .setDate(dto.date)
                .setOut(dto.out)
                .setBody(dto.body)
                .setEncrypted(encrypted)
                .setImportant(dto.important)
                .setDeleted(dto.deleted)
                .setDeletedForAll(false) // cant be deleted for all?
                .setForwardCount(Utils.safeCountOf(dto.fwd_messages))
                .setHasAttachmens(nonNull(dto.attachments) && !dto.attachments.isEmpty())
                .setStatus(MessageStatus.SENT) // only sent can be
                .setOriginalId(dto.id)
                .setAction(Message.fromApiChatAction(dto.action))
                .setActionMemberId(dto.action_mid)
                .setActionEmail(dto.action_email)
                .setActionText(dto.action_text)
                .setPhoto50(dto.action_photo_50)
                .setPhoto100(dto.action_photo_100)
                .setPhoto200(dto.action_photo_200)
                .setRandomId(randomId)
                .setUpdateTime(dto.update_time);

        if (entity.isHasAttachmens()) {
            entity.setAttachments(mapAttachemntsList(dto.attachments));
        } else {
            entity.setAttachments(Collections.emptyList());
        }

        if (nonEmpty(dto.fwd_messages)) {
            if (dto.fwd_messages.size() == 1) {
                entity.setForwardMessages(Collections.singletonList(mapMessage(dto.fwd_messages.get(0))));
            } else {
                List<MessageEntity> fwds = new ArrayList<>(dto.fwd_messages.size());

                for (VKApiMessage f : dto.fwd_messages) {
                    fwds.add(mapMessage(f));
                }

                entity.setForwardMessages(fwds);
            }
        } else {
            entity.setForwardMessages(Collections.emptyList());
        }

        return entity;
    }

    public static VideoEntity mapVideo(VKApiVideo dto) {
        return new VideoEntity(dto.id, dto.owner_id)
                .setAlbumId(dto.album_id)
                .setTitle(dto.title)
                .setDescription(dto.description)
                .setDuration(dto.duration)
                .setLink(dto.link)
                .setDate(dto.date)
                .setAddingDate(dto.adding_date)
                .setViews(dto.views)
                .setPlayer(dto.player)
                .setPhoto130(dto.photo_130)
                .setPhoto320(dto.photo_320)
                .setPhoto800(dto.photo_800)
                .setAccessKey(dto.access_key)
                .setCommentsCount(isNull(dto.comments) ? 0 : dto.comments.count)
                .setCanComment(dto.can_comment)
                .setCanRepost(dto.can_repost)
                .setUserLikes(dto.user_likes)
                .setRepeat(dto.repeat)
                .setLikesCount(dto.likes)
                .setPrivacyView(nonNull(dto.privacy_view) ? mapPrivacy(dto.privacy_view) : null)
                .setPrivacyComment(nonNull(dto.privacy_comment) ? mapPrivacy(dto.privacy_comment) : null)
                .setMp4link240(dto.mp4_240)
                .setMp4link360(dto.mp4_360)
                .setMp4link480(dto.mp4_480)
                .setMp4link720(dto.mp4_720)
                .setMp4link1080(dto.mp4_1080)
                .setExternalLink(dto.external)
                .setHls(dto.hls)
                .setLive(dto.live)
                .setPlatform(dto.platform)
                .setCanEdit(dto.can_edit)
                .setCanAdd(dto.can_add);
    }

    public static PrivacyEntity mapPrivacy(VkApiPrivacy dto) {
        return new PrivacyEntity(dto.category, mapAll(dto.entries, orig -> new PrivacyEntity.Entry(orig.type, orig.id, orig.allowed)));
    }

    public static GiftEntity mapGift(VKApiGift dto) {
        return new GiftEntity(dto.id)
                .setFromId(dto.from_id)
                .setMessage(dto.message)
                .setDate(dto.date)
                .setGiftItem(mapGiftItem(dto.giftItem))
                .setPrivacy(dto.privacy);
    }

    public static GiftItemEntity mapGiftItem(VKApiGiftItem dto) {
        return new GiftItemEntity(dto.id)
                .setThumb48(dto.thumb_48)
                .setThumb96(dto.thumb_96)
                .setThumb256(dto.thumb_256);
    }

    public static PhotoEntity mapPhoto(VKApiPhoto dto) {
        return new PhotoEntity(dto.id, dto.owner_id)
                .setAlbumId(dto.album_id)
                .setWidth(dto.width)
                .setHeight(dto.height)
                .setText(dto.text)
                .setDate(dto.date)
                .setUserLikes(dto.user_likes)
                .setCanComment(dto.can_comment)
                .setLikesCount(dto.likes)
                .setCommentsCount(nonNull(dto.comments) ? dto.comments.count : 0)
                .setTagsCount(dto.tags)
                .setAccessKey(dto.access_key)
                .setPostId(dto.post_id)
                .setDeleted(false) //cant bee deleted
                .setSizes(mapPhotoSizes(dto.sizes));
    }

    public static PhotoSizeEntity.Size mapPhotoSize(PhotoSizeDto dto) {
        return new PhotoSizeEntity.Size()
                .setH(dto.height)
                .setW(dto.width)
                .setUrl(dto.url);
    }

    public static PhotoSizeEntity mapPhotoSizes(List<PhotoSizeDto> dtos) {
        PhotoSizeEntity sizes = new PhotoSizeEntity();

        if (nonNull(dtos)) {
            for (PhotoSizeDto dto : dtos) {
                switch (dto.type) {
                    case PhotoSizeDto.Type.S:
                        sizes.setS(mapPhotoSize(dto));
                        break;

                    case PhotoSizeDto.Type.M:
                        sizes.setM(mapPhotoSize(dto));
                        break;

                    case PhotoSizeDto.Type.X:
                        sizes.setX(mapPhotoSize(dto));
                        break;

                    case PhotoSizeDto.Type.Y:
                        sizes.setY(mapPhotoSize(dto));
                        break;

                    case PhotoSizeDto.Type.Z:
                        sizes.setZ(mapPhotoSize(dto));
                        break;

                    case PhotoSizeDto.Type.W:
                        sizes.setW(mapPhotoSize(dto));
                        break;

                    case PhotoSizeDto.Type.O:
                        sizes.setO(mapPhotoSize(dto));
                        break;

                    case PhotoSizeDto.Type.P:
                        sizes.setP(mapPhotoSize(dto));
                        break;

                    case PhotoSizeDto.Type.Q:
                        sizes.setQ(mapPhotoSize(dto));
                        break;

                    case PhotoSizeDto.Type.R:
                        sizes.setR(mapPhotoSize(dto));
                        break;
                }
            }
        }

        return sizes;
    }
}