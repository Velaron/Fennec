package dev.velaron.fennec.db.impl;

import static dev.velaron.fennec.util.Objects.nonNull;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.db.DatabaseIdRange;
import dev.velaron.fennec.db.MessengerContentProvider;
import dev.velaron.fennec.db.column.VideoAlbumsColumns;
import dev.velaron.fennec.db.interfaces.IVideoAlbumsStorage;
import dev.velaron.fennec.db.model.entity.PrivacyEntity;
import dev.velaron.fennec.db.model.entity.VideoAlbumEntity;
import dev.velaron.fennec.model.VideoAlbumCriteria;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 21.11.2016.
 * phoenix
 */
class VideoAlbumsStorage extends AbsStorage implements IVideoAlbumsStorage {

    VideoAlbumsStorage(@NonNull AppStorages base) {
        super(base);
    }

    @Override
    public Single<List<VideoAlbumEntity>> findByCriteria(@NonNull VideoAlbumCriteria criteria) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getVideoAlbumsContentUriFor(criteria.getAccountId());
            String where;
            String[] args;

            DatabaseIdRange range = criteria.getRange();

            if(nonNull(range)){
                where = VideoAlbumsColumns.OWNER_ID + " = ? " +
                        " AND " + VideoAlbumsColumns._ID + " >= ? " +
                        " AND " + VideoAlbumsColumns._ID + " <= ?";
                args = new String[]{String.valueOf(criteria.getOwnerId()),
                        String.valueOf(range.getFirst()),
                        String.valueOf(range.getLast())};
            } else {
                where = VideoAlbumsColumns.OWNER_ID + " = ?";
                args = new String[]{String.valueOf(criteria.getOwnerId())};
            }

            Cursor cursor = getContentResolver().query(uri, null, where, args, null);
            List<VideoAlbumEntity> data = new ArrayList<>(Utils.safeCountOf(cursor));

            if(nonNull(cursor)){
                while (cursor.moveToNext()){
                    if(e.isDisposed()){
                        break;
                    }

                    data.add(mapAlbum(cursor));
                }

                cursor.close();
            }

            e.onSuccess(data);
        });
    }

    @Override
    public Completable insertData(int accountId, int ownerId, @NonNull List<VideoAlbumEntity> data, boolean deleteBeforeInsert) {
        return Completable.create(e -> {
            Uri uri = MessengerContentProvider.getVideoAlbumsContentUriFor(accountId);
            ArrayList<ContentProviderOperation> operations = new ArrayList<>();

            if (deleteBeforeInsert) {
                operations.add(ContentProviderOperation
                        .newDelete(uri)
                        .withSelection(VideoAlbumsColumns.OWNER_ID + " = ?", new String[]{String.valueOf(ownerId)})
                        .build());
            }

            for (VideoAlbumEntity dbo : data) {
                operations.add(ContentProviderOperation
                        .newInsert(uri)
                        .withValues(getCV(dbo))
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            e.onComplete();
        });
    }

    public static ContentValues getCV(VideoAlbumEntity dbo){
        ContentValues cv = new ContentValues();
        cv.put(VideoAlbumsColumns.OWNER_ID, dbo.getOwnerId());
        cv.put(VideoAlbumsColumns.ALBUM_ID, dbo.getId());
        cv.put(VideoAlbumsColumns.TITLE, dbo.getTitle());
        cv.put(VideoAlbumsColumns.PHOTO_160, dbo.getPhoto160());
        cv.put(VideoAlbumsColumns.PHOTO_320, dbo.getPhoto320());
        cv.put(VideoAlbumsColumns.COUNT, dbo.getCount());
        cv.put(VideoAlbumsColumns.UPDATE_TIME, dbo.getUpdateTime());
        cv.put(VideoAlbumsColumns.PRIVACY, nonNull(dbo.getPrivacy()) ? GSON.toJson(dbo.getPrivacy()) : null);
        return cv;
    }

    private VideoAlbumEntity mapAlbum(Cursor cursor){
        final int id = cursor.getInt(cursor.getColumnIndex(VideoAlbumsColumns.ALBUM_ID));
        final int ownerId = cursor.getInt(cursor.getColumnIndex(VideoAlbumsColumns.OWNER_ID));

        PrivacyEntity privacyEntity = null;

        String privacyJson = cursor.getString(cursor.getColumnIndex(VideoAlbumsColumns.PRIVACY));
        if(Utils.nonEmpty(privacyJson)){
            privacyEntity = GSON.fromJson(privacyJson, PrivacyEntity.class);
        }

        return new VideoAlbumEntity(id, ownerId)
                .setTitle(cursor.getString(cursor.getColumnIndex(VideoAlbumsColumns.TITLE)))
                .setUpdateTime(cursor.getLong(cursor.getColumnIndex(VideoAlbumsColumns.UPDATE_TIME)))
                .setCount(cursor.getInt(cursor.getColumnIndex(VideoAlbumsColumns.COUNT)))
                .setPhoto160(cursor.getString(cursor.getColumnIndex(VideoAlbumsColumns.PHOTO_160)))
                .setPhoto320(cursor.getString(cursor.getColumnIndex(VideoAlbumsColumns.PHOTO_320)))
                .setPrivacy(privacyEntity);
    }
}