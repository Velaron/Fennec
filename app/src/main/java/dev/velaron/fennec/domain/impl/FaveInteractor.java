package dev.velaron.fennec.domain.impl;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.FaveLinkDto;
import dev.velaron.fennec.api.model.VKApiPhoto;
import dev.velaron.fennec.api.model.VKApiPost;
import dev.velaron.fennec.api.model.VKApiVideo;
import dev.velaron.fennec.api.model.response.FavePageResponse;
import dev.velaron.fennec.db.column.UserColumns;
import dev.velaron.fennec.db.interfaces.IStorages;
import dev.velaron.fennec.db.model.entity.CommunityEntity;
import dev.velaron.fennec.db.model.entity.FaveLinkEntity;
import dev.velaron.fennec.db.model.entity.FavePageEntity;
import dev.velaron.fennec.db.model.entity.OwnerEntities;
import dev.velaron.fennec.db.model.entity.PhotoEntity;
import dev.velaron.fennec.db.model.entity.PostEntity;
import dev.velaron.fennec.db.model.entity.UserEntity;
import dev.velaron.fennec.db.model.entity.VideoEntity;
import dev.velaron.fennec.domain.IFaveInteractor;
import dev.velaron.fennec.domain.IOwnersRepository;
import dev.velaron.fennec.domain.mappers.Dto2Entity;
import dev.velaron.fennec.domain.mappers.Dto2Model;
import dev.velaron.fennec.domain.mappers.Entity2Model;
import dev.velaron.fennec.model.EndlessData;
import dev.velaron.fennec.model.FaveLink;
import dev.velaron.fennec.model.FavePage;
import dev.velaron.fennec.model.FavePageType;
import dev.velaron.fennec.model.Owner;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.Post;
import dev.velaron.fennec.model.Video;
import dev.velaron.fennec.model.criteria.FavePhotosCriteria;
import dev.velaron.fennec.model.criteria.FavePostsCriteria;
import dev.velaron.fennec.model.criteria.FaveVideosCriteria;
import dev.velaron.fennec.util.Utils;
import dev.velaron.fennec.util.VKOwnIds;
import io.reactivex.Completable;
import io.reactivex.Single;

import static dev.velaron.fennec.domain.mappers.MapUtil.mapAll;
import static dev.velaron.fennec.util.Objects.nonNull;
import static dev.velaron.fennec.util.Utils.listEmptyIfNull;
import static dev.velaron.fennec.util.Utils.safeCountOf;

/**
 * Created by Ruslan Kolbasa on 14.07.2017.
 * phoenix
 */
public class FaveInteractor implements IFaveInteractor {

    private final INetworker networker;
    private final IStorages cache;
    private final IOwnersRepository ownersRepository;

    public FaveInteractor(INetworker networker, IStorages cache, IOwnersRepository ownersRepository) {
        this.networker = networker;
        this.cache = cache;
        this.ownersRepository = ownersRepository;
    }

    private static FaveLink createLinkFromEntity(FaveLinkEntity entity) {
        return new FaveLink(entity.getId())
                .setDescription(entity.getDescription())
                .setPhoto50(entity.getPhoto50())
                .setPhoto100(entity.getPhoto100())
                .setTitle(entity.getTitle())
                .setUrl(entity.getUrl());
    }

    private static FaveLinkEntity createLinkEntityFromDto(FaveLinkDto dto) {
        return new FaveLinkEntity(dto.id, dto.url)
                .setDescription(dto.description)
                .setTitle(dto.title)
                .setPhoto50(dto.photo_50)
                .setPhoto100(dto.photo_100);
    }

    @Override
    public Single<List<Post>> getPosts(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getPosts(offset, count, true)
                .flatMap(response -> {
                    List<VKApiPost> dtos = listEmptyIfNull(response.posts);

                    List<Owner> owners = Dto2Model.transformOwners(response.profiles, response.groups);

                    VKOwnIds ids = new VKOwnIds();
                    for (VKApiPost dto : dtos) {
                        ids.append(dto);
                    }

                    final OwnerEntities ownerEntities = Dto2Entity.mapOwners(response.profiles, response.groups);

                    final List<PostEntity> dbos = new ArrayList<>(safeCountOf(response.posts));
                    if (nonNull(response.posts)) {
                        for (VKApiPost dto : response.posts) {
                            dbos.add(Dto2Entity.mapPost(dto));
                        }
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY, owners)
                            .map(bundle -> Dto2Model.transformPosts(dtos, bundle))
                            .flatMap(posts -> cache.fave()
                                    .storePosts(accountId, dbos, ownerEntities, offset == 0)
                                    .andThen(Single.just(posts)));
                });
    }

    @Override
    public Single<List<Post>> getCachedPosts(int accountId) {
        return cache.fave().getFavePosts(new FavePostsCriteria(accountId))
                .flatMap(postDbos -> {
                    VKOwnIds ids = new VKOwnIds();
                    for (PostEntity dbo : postDbos) {
                        Entity2Model.fillPostOwnerIds(ids, dbo);
                    }

                    return ownersRepository.findBaseOwnersDataAsBundle(accountId, ids.getAll(), IOwnersRepository.MODE_ANY)
                            .map(owners -> {
                                List<Post> posts = new ArrayList<>();
                                for (PostEntity dbo : postDbos) {
                                    posts.add(Entity2Model.buildPostFromDbo(dbo, owners));
                                }
                                return posts;
                            });
                });
    }

    @Override
    public Single<List<Photo>> getCachedPhotos(int accountId) {
        FavePhotosCriteria criteria = new FavePhotosCriteria(accountId);
        return cache.fave()
                .getPhotos(criteria)
                .map(photoDbos -> {
                    List<Photo> photos = new ArrayList<>(photoDbos.size());
                    for (PhotoEntity dbo : photoDbos) {
                        photos.add(Entity2Model.map(dbo));
                    }
                    return photos;
                });
    }

    @Override
    public Single<List<Photo>> getPhotos(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getPhotos(offset, count)
                .flatMap(items -> {
                    List<VKApiPhoto> dtos = listEmptyIfNull(items.getItems());

                    List<PhotoEntity> dbos = new ArrayList<>(dtos.size());
                    List<Photo> photos = new ArrayList<>(dtos.size());

                    for (VKApiPhoto dto : dtos) {
                        dbos.add(Dto2Entity.mapPhoto(dto));
                        photos.add(Dto2Model.transform(dto));
                    }

                    return cache.fave().storePhotos(accountId, dbos, offset == 0)
                            .map(ints -> photos);
                });
    }

    @Override
    public Single<List<Video>> getCachedVideos(int accountId) {
        FaveVideosCriteria criteria = new FaveVideosCriteria(accountId);

        return cache.fave()
                .getVideos(criteria)
                .map(videoDbos -> {
                    List<Video> videos = new ArrayList<>(videoDbos.size());
                    for (VideoEntity dbo : videoDbos) {
                        videos.add(Entity2Model.buildVideoFromDbo(dbo));
                    }
                    return videos;
                });
    }

    @Override
    public Single<List<Video>> getVideos(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getVideos(offset, count, false)
                .flatMap(items -> {
                    List<VKApiVideo> dtos = listEmptyIfNull(items.getItems());

                    List<VideoEntity> dbos = new ArrayList<>(dtos.size());
                    List<Video> videos = new ArrayList<>(dtos.size());

                    for (VKApiVideo dto : dtos) {
                        dbos.add(Dto2Entity.mapVideo(dto));
                        videos.add(Dto2Model.transform(dto));
                    }

                    return cache.fave().storeVideos(accountId, dbos, offset == 0)
                            .map(ints -> videos);
                });
    }

    @Override
    public Single<List<FavePage>> getCachedPages(int accountId) {
        return cache.fave()
                .getFaveUsers(accountId)
                .map(Entity2Model::buildFaveUsersFromDbo);
    }

    @Override
    public Single<EndlessData<FavePage>> getPages(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getPages(offset, count, UserColumns.API_FIELDS)
                .flatMap(items -> {
                    boolean hasNext = count + offset < items.count;

                    List<FavePageResponse> dtos = listEmptyIfNull(items.getItems());

                    List<UserEntity> userEntities = new ArrayList<>();
                    List<CommunityEntity> communityEntities = new ArrayList<>();
                    for (FavePageResponse item : dtos) {
                        switch (item.type) {
                            case FavePageType.USER:
                                userEntities.add(Dto2Entity.mapUser(item.user));
                                break;
                            case FavePageType.COMMUNITY:
                                communityEntities.add(Dto2Entity.mapCommunity(item.group));
                                break;
                        }
                    }

                    List<FavePageEntity> entities = mapAll(dtos, Dto2Entity::mapFavePage, true);
                    List<FavePage> pages = mapAll(dtos, Dto2Model::transformFaveUser, true);

                    return cache.fave()
                            .storePages(accountId, entities, offset == 0)
                            .andThen(cache.owners().storeOwnerEntities(accountId, new OwnerEntities(userEntities, communityEntities)))
                            .andThen(Single.just(EndlessData.create(pages, hasNext)));
                });
    }

    @Override
    public Single<List<FaveLink>> getCachedLinks(int accountId) {
        return cache.fave()
                .getFaveLinks(accountId)
                .map(entities -> {
                    List<FaveLink> links = new ArrayList<>(entities.size());

                    for (FaveLinkEntity entity : entities) {
                        links.add(createLinkFromEntity(entity));
                    }

                    return links;
                });
    }

    @Override
    public Single<EndlessData<FaveLink>> getLinks(int accountId, int count, int offset) {
        return networker.vkDefault(accountId)
                .fave()
                .getLinks(offset, count)
                .flatMap(items -> {
                    boolean hasNext = offset + count < items.count;
                    List<FaveLinkDto> dtos = Utils.listEmptyIfNull(items.getItems());
                    List<FaveLink> links = new ArrayList<>(dtos.size());
                    List<FaveLinkEntity> entities = new ArrayList<>(dtos.size());

                    for (FaveLinkDto dto : dtos) {
                        FaveLinkEntity entity = createLinkEntityFromDto(dto);
                        links.add(createLinkFromEntity(entity));
                        entities.add(entity);
                    }

                    return cache.fave()
                            .storeLinks(accountId, entities, offset == 0)
                            .andThen(Single.just(EndlessData.create(links, hasNext)));
                });
    }

    @Override
    public Completable removeLink(int accountId, String id) {
        return networker.vkDefault(accountId)
                .fave()
                .removeLink(id)
                .flatMapCompletable(ignore -> cache.fave()
                        .removeLink(accountId, id));
    }

    @Override
    public Completable addPage(int accountId, int ownerId) {
        return networker.vkDefault(accountId)
                .fave()
                .addPage(ownerId > 0 ? ownerId : null, ownerId < 0 ? Math.abs(ownerId) : null)
                .ignoreElement();
    }

    @Override
    public Completable removePage(int accountId, int ownerId) {
        return networker.vkDefault(accountId)
                .fave()
                .removePage(ownerId > 0 ? ownerId : null, ownerId < 0 ? Math.abs(ownerId) : null)
                .flatMapCompletable(ignored -> cache.fave().removePage(accountId, ownerId));
    }
}