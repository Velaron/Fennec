package dev.velaron.fennec.domain.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.api.model.VKApiPhoto;
import dev.velaron.fennec.api.model.VKApiPhotoAlbum;
import dev.velaron.fennec.db.column.PhotosColumns;
import dev.velaron.fennec.db.interfaces.IStorages;
import dev.velaron.fennec.db.model.PhotoPatch;
import dev.velaron.fennec.db.model.entity.PhotoAlbumEntity;
import dev.velaron.fennec.db.model.entity.PhotoEntity;
import dev.velaron.fennec.domain.IPhotosInteractor;
import dev.velaron.fennec.domain.mappers.Dto2Entity;
import dev.velaron.fennec.domain.mappers.Dto2Model;
import dev.velaron.fennec.domain.mappers.Entity2Model;
import dev.velaron.fennec.exception.NotFoundException;
import dev.velaron.fennec.model.AccessIdPair;
import dev.velaron.fennec.model.Photo;
import dev.velaron.fennec.model.PhotoAlbum;
import dev.velaron.fennec.model.criteria.PhotoAlbumsCriteria;
import dev.velaron.fennec.model.criteria.PhotoCriteria;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Completable;
import io.reactivex.Single;

import static dev.velaron.fennec.domain.mappers.MapUtil.mapAll;

/**
 * Created by Ruslan Kolbasa on 13.07.2017.
 * phoenix
 */
public class PhotosInteractor implements IPhotosInteractor {

    private final INetworker networker;
    private final IStorages cache;

    public PhotosInteractor(INetworker networker, IStorages cache) {
        this.networker = networker;
        this.cache = cache;
    }

    @Override
    public Single<List<Photo>> get(int accountId, int ownerId, int albumId, int count, int offset, boolean rev) {
        return networker.vkDefault(accountId)
                .photos()
                .get(ownerId, String.valueOf(albumId), null, rev, offset, count)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .flatMap(dtos -> {
                    List<Photo> photos = new ArrayList<>(dtos.size());
                    List<PhotoEntity> dbos = new ArrayList<>(dtos.size());

                    for(VKApiPhoto dto : dtos){
                        photos.add(Dto2Model.transform(dto));
                        dbos.add(Dto2Entity.mapPhoto(dto));
                    }

                    return cache.photos()
                            .insertPhotosRx(accountId, ownerId, albumId, dbos, offset == 0)
                            .andThen(Single.just(photos));
                });
    }

    @Override
    public Single<List<Photo>> getAllCachedData(int accountId, int ownerId, int albumId) {
        PhotoCriteria criteria = new PhotoCriteria(accountId).setAlbumId(albumId).setOwnerId(ownerId);

        if (albumId == -15) {
            criteria.setOrderBy(PhotosColumns._ID);
        }

        return cache.photos()
                .findPhotosByCriteriaRx(criteria)
                .map(entities -> mapAll(entities, Entity2Model::map));
    }

    @Override
    public Single<PhotoAlbum> getAlbumById(int accountId, int ownerId, int albumId) {
        return networker.vkDefault(accountId)
                .photos()
                .getAlbums(ownerId, Collections.singletonList(albumId), null, null, true, true)
                .map(items -> Utils.listEmptyIfNull(items.getItems()))
                .map(dtos -> {
                    if(dtos.isEmpty()){
                        throw new NotFoundException();
                    }

                    return Dto2Model.transform(dtos.get(0));
                });
    }

    @Override
    public Single<List<PhotoAlbum>> getCachedAlbums(int accountId, int ownerId) {
        PhotoAlbumsCriteria criteria = new PhotoAlbumsCriteria(accountId, ownerId);

        return cache.photoAlbums()
                .findAlbumsByCriteria(criteria)
                .map(entities -> mapAll(entities, Entity2Model::map));
    }

    @Override
    public Single<List<PhotoAlbum>> getActualAlbums(int accountId, int ownerId, int count, int offset) {
        return networker.vkDefault(accountId)
                .photos()
                .getAlbums(ownerId, null, offset, count, true, true)
                .flatMap(items -> {
                    List<VKApiPhotoAlbum> dtos = Utils.listEmptyIfNull(items.getItems());

                    List<PhotoAlbumEntity> dbos = new ArrayList<>(dtos.size());
                    List<PhotoAlbum> albums = new ArrayList<>(dbos.size());

                    for(VKApiPhotoAlbum dto : dtos){
                        dbos.add(Dto2Entity.buildPhotoAlbumDbo(dto));
                        albums.add(Dto2Model.transform(dto));
                    }

                    return cache.photoAlbums()
                            .store(accountId, ownerId, dbos, offset == 0)
                            .andThen(Single.just(albums));
                });
    }

    @Override
    public Single<Integer> like(int accountId, int ownerId, int photoId, boolean add, String accessKey) {
        Single<Integer> single;

        if(add){
            single = networker.vkDefault(accountId)
                    .likes()
                    .add("photo", ownerId, photoId, accessKey);
        } else {
            single = networker.vkDefault(accountId)
                    .likes()
                    .delete("photo", ownerId, photoId);
        }

        return single.flatMap(count -> {
            final PhotoPatch patch = new PhotoPatch().setLike(new PhotoPatch.Like(count, add));
            return cache.photos()
                    .applyPatch(accountId, ownerId, photoId, patch)
                    .andThen(Single.just(count));
        });
    }

    @Override
    public Single<Integer> copy(int accountId, int ownerId, int photoId, String accessKey) {
        return networker.vkDefault(accountId)
                .photos()
                .copy(ownerId, photoId, accessKey);
    }

    @Override
    public Completable removedAlbum(int accountId, int ownerId, int albumId) {
        return networker.vkDefault(accountId)
                .photos()
                .deleteAlbum(albumId, ownerId < 0 ? Math.abs(ownerId) : null)
                .flatMapCompletable(ignored -> cache.photoAlbums()
                        .removeAlbumById(accountId, ownerId, albumId));
    }

    @Override
    public Completable deletePhoto(int accountId, int ownerId, int photoId) {
        return networker.vkDefault(accountId)
                .photos()
                .delete(ownerId, photoId)
                .flatMapCompletable(ignored -> {
                    PhotoPatch patch = new PhotoPatch().setDeletion(new PhotoPatch.Deletion(true));
                    return cache.photos()
                            .applyPatch(accountId, ownerId, photoId, patch);
                });
    }

    @Override
    public Completable restorePhoto(int accountId, int ownerId, int photoId) {
        return networker.vkDefault(accountId)
                .photos()
                .restore(ownerId, photoId)
                .flatMapCompletable(ignored -> {
                    PhotoPatch patch = new PhotoPatch().setDeletion(new PhotoPatch.Deletion(false));
                    return cache.photos()
                            .applyPatch(accountId, ownerId, photoId, patch);
                });
    }

    @Override
    public Single<List<Photo>> getPhotosByIds(int accountId, Collection<AccessIdPair> ids) {
        List<dev.velaron.fennec.api.model.AccessIdPair> dtoPairs = new ArrayList<>(ids.size());

        for(AccessIdPair pair : ids){
            dtoPairs.add(new dev.velaron.fennec.api.model.AccessIdPair(pair.getId(),
                    pair.getOwnerId(), pair.getAccessKey()));
        }

        return networker.vkDefault(accountId)
                .photos()
                .getById(dtoPairs)
                .map(dtos -> mapAll(dtos, Dto2Model::transform));
    }
}