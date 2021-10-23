package dev.velaron.fennec.domain.impl;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.Constants;
import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.CommentsDto;
import dev.velaron.fennec.api.model.VKApiPhoto;
import dev.velaron.fennec.api.model.VKApiPost;
import dev.velaron.fennec.api.model.VKApiTopic;
import dev.velaron.fennec.api.model.VKApiVideo;
import dev.velaron.fennec.api.model.response.NewsfeedCommentsResponse;
import dev.velaron.fennec.domain.INewsfeedInteractor;
import dev.velaron.fennec.domain.IOwnersRepository;
import dev.velaron.fennec.domain.mappers.Dto2Model;
import dev.velaron.fennec.model.Comment;
import dev.velaron.fennec.model.Commented;
import dev.velaron.fennec.model.IOwnersBundle;
import dev.velaron.fennec.model.NewsfeedComment;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.PhotoWithOwner;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.Topic;
import dev.velaron.fennec.model.TopicWithOwner;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.model.VideoWithOwner;
import dev.velaron.fennec.util.Pair;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.util.VKOwnIds;
import io.reactivex.Single;

import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.nonEmpty;

/**
 * Created by admin on 08.05.2017.
 * phoenix
 */
public class NewsfeedInteractor implements INewsfeedInteractor {

    private final INetworker networker;
    private final IOwnersRepository ownersRepository;

    public NewsfeedInteractor(INetworker networker, IOwnersRepository ownersRepository) {
        this.networker = networker;
        this.ownersRepository = ownersRepository;
    }

    @Override
    public Single<Pair<List<NewsfeedComment>, String>> getNewsfeedComments(int accountId, int count, String startFrom, String filter) {
        return networker.vkDefault(accountId)
                .newsfeed()
                .getComments(count, filter, null, null, null,
                        1, startFrom, Constants.MAIN_OWNER_FIELDS)
                .flatMap(response -> {
                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    VKOwnIds ownIds = new VKOwnIds();

                    List<NewsfeedCommentsResponse.Dto> dtos = Utils.listEmptyIfNull(response.items);

                    for (NewsfeedCommentsResponse.Dto dto : dtos) {
                        if (dto instanceof NewsfeedCommentsResponse.PostDto) {
                            VKApiPost post = ((NewsfeedCommentsResponse.PostDto) dto).post;
                            ownIds.append(post);
                            ownIds.append(post.comments);
                        } else if (dto instanceof NewsfeedCommentsResponse.PhotoDto) {
                            ownIds.append(((NewsfeedCommentsResponse.PhotoDto) dto).photo.owner_id);
                            ownIds.append(((NewsfeedCommentsResponse.PhotoDto) dto).photo.comments);
                        } else if (dto instanceof NewsfeedCommentsResponse.TopicDto) {
                            VKApiTopic topic = ((NewsfeedCommentsResponse.TopicDto) dto).topic;
                            ownIds.append(topic.owner_id);
                            ownIds.append(topic.comments);
                        } else if (dto instanceof NewsfeedCommentsResponse.VideoDto) {
                            ownIds.append(((NewsfeedCommentsResponse.VideoDto) dto).video.owner_id);
                            ownIds.append(((NewsfeedCommentsResponse.VideoDto) dto).video.comments);
                        } else {
                            // TODO: 08.05.2017
                        }
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ownIds.getAll(), IOwnersRepository.MODE_ANY, owners)
                            .map(bundle -> {
                                List<NewsfeedComment> comments = new ArrayList<>(dtos.size());
                                for (NewsfeedCommentsResponse.Dto dto : dtos) {
                                    NewsfeedComment comment = createFrom(dto, bundle);
                                    if (nonNull(comment)) {
                                        comments.add(comment);
                                    }
                                }

                                return Pair.Companion.create(comments, response.nextFrom);
                            });
                });
    }

    private static Comment oneCommentFrom(Commented commented, CommentsDto dto, IOwnersBundle bundle) {
        if (nonNull(dto) && nonEmpty(dto.list)) {
            return Dto2Model.buildComment(commented, dto.list.get(dto.list.size() - 1), bundle);
        }

        return null;
    }

    private static NewsfeedComment createFrom(NewsfeedCommentsResponse.Dto dto, IOwnersBundle bundle) {
        if (dto instanceof NewsfeedCommentsResponse.PhotoDto) {
            VKApiPhoto photoDto = ((NewsfeedCommentsResponse.PhotoDto) dto).photo;
            Photo photo = Dto2Model.transform(photoDto);
            Commented commented = Commented.from(photo);

            Owner photoOwner = bundle.getById(photo.getOwnerId());
            return new NewsfeedComment(new PhotoWithOwner(photo, photoOwner))
                    .setComment(oneCommentFrom(commented, photoDto.comments, bundle));
        }

        if (dto instanceof NewsfeedCommentsResponse.VideoDto) {
            VKApiVideo videoDto = ((NewsfeedCommentsResponse.VideoDto) dto).video;
            Video video = Dto2Model.transform(videoDto);
            Commented commented = Commented.from(video);

            Owner videoOwner = bundle.getById(video.getOwnerId());
            return new NewsfeedComment(new VideoWithOwner(video, videoOwner))
                    .setComment(oneCommentFrom(commented, videoDto.comments, bundle));
        }

        if (dto instanceof NewsfeedCommentsResponse.PostDto) {
            VKApiPost postDto = ((NewsfeedCommentsResponse.PostDto) dto).post;
            Post post = Dto2Model.transform(postDto, bundle);
            Commented commented = Commented.from(post);
            return new NewsfeedComment(post).setComment(oneCommentFrom(commented, postDto.comments, bundle));
        }

        if (dto instanceof NewsfeedCommentsResponse.TopicDto) {
            VKApiTopic topicDto = ((NewsfeedCommentsResponse.TopicDto) dto).topic;
            Topic topic = Dto2Model.transform(topicDto, bundle);

            if(nonNull(topicDto.comments)){
                topic.setCommentsCount(topicDto.comments.count);
            }

            Commented commented = Commented.from(topic);
            Owner owner = bundle.getById(topic.getOwnerId());
            return new NewsfeedComment(new TopicWithOwner(topic, owner)).setComment(oneCommentFrom(commented, topicDto.comments, bundle));
        }

        return null;
    }
}