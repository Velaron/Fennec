package dev.velaron.fennec.db.impl;

import static dev.velaron.fennec.util.Objects.nonNull;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.crypt.AesKeyPair;
import dev.velaron.fennec.db.MessengerContentProvider;
import dev.velaron.fennec.db.column.KeyColumns;
import dev.velaron.fennec.db.interfaces.IKeysStorage;
import dev.velaron.fennec.exception.DatabaseException;
import dev.velaron.fennec.util.Optional;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;

/**
 * Created by ruslan.kolbasa on 20.10.2016.
 * phoenix
 */
class KeysPersistStorage extends AbsStorage implements IKeysStorage {

    KeysPersistStorage(@NonNull AppStorages context) {
        super(context);
    }

    private AesKeyPair map(Cursor cursor){
        return new AesKeyPair()
                .setVersion(cursor.getInt(cursor.getColumnIndex(KeyColumns.VERSION)))
                .setPeerId(cursor.getInt(cursor.getColumnIndex(KeyColumns.PEER_ID)))
                .setSessionId(cursor.getLong(cursor.getColumnIndex(KeyColumns.SESSION_ID)))
                .setDate(cursor.getLong(cursor.getColumnIndex(KeyColumns.DATE)))
                .setStartMessageId(cursor.getInt(cursor.getColumnIndex(KeyColumns.START_SESSION_MESSAGE_ID)))
                .setEndMessageId(cursor.getInt(cursor.getColumnIndex(KeyColumns.END_SESSION_MESSAGE_ID)))
                .setHisAesKey(cursor.getString(cursor.getColumnIndex(KeyColumns.IN_KEY)))
                .setMyAesKey(cursor.getString(cursor.getColumnIndex(KeyColumns.OUT_KEY)));
    }

    @Override
    public Completable saveKeyPair(@NonNull AesKeyPair pair) {
        return Completable.create(e -> {
            AesKeyPair alreaadyExist = findKeyPairFor(pair.getAccountId(), pair.getSessionId())
                    .blockingGet();

            if(nonNull(alreaadyExist)){
                e.onError(new DatabaseException("Key pair with the session ID is already in the database"));
                return;
            }

            ContentValues cv = new ContentValues();
            cv.put(KeyColumns.VERSION, pair.getVersion());
            cv.put(KeyColumns.PEER_ID, pair.getPeerId());
            cv.put(KeyColumns.SESSION_ID, pair.getSessionId());
            cv.put(KeyColumns.DATE, pair.getDate());
            cv.put(KeyColumns.START_SESSION_MESSAGE_ID, pair.getStartMessageId());
            cv.put(KeyColumns.END_SESSION_MESSAGE_ID, pair.getEndMessageId());
            cv.put(KeyColumns.OUT_KEY, pair.getMyAesKey());
            cv.put(KeyColumns.IN_KEY, pair.getHisAesKey());

            Uri uri = MessengerContentProvider.getKeysContentUriFor(pair.getAccountId());
            getContext().getContentResolver().insert(uri, cv);

            e.onComplete();
        });
    }

    @Override
    public Single<List<AesKeyPair>> getAll(int accountId) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getKeysContentUriFor(accountId);
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, KeyColumns._ID);

            List<AesKeyPair> pairs = new ArrayList<>(Utils.safeCountOf(cursor));
            if(nonNull(cursor)){
                while (cursor.moveToNext()){
                    if(e.isDisposed()) {
                        break;
                    }

                    pairs.add(map(cursor).setAccountId(accountId));
                }

                cursor.close();
            }

            e.onSuccess(pairs);
        });
    }

    @Override
    public Single<List<AesKeyPair>> getKeys(int accountId, int peerId) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getKeysContentUriFor(accountId);
            Cursor cursor = getContext().getContentResolver()
                    .query(uri, null, KeyColumns.PEER_ID + " = ?", new String[]{String.valueOf(peerId)}, KeyColumns._ID);

            List<AesKeyPair> pairs = new ArrayList<>(Utils.safeCountOf(cursor));
            if(nonNull(cursor)){
                while (cursor.moveToNext()){
                    if(e.isDisposed()) {
                        break;
                    }

                    pairs.add(map(cursor).setAccountId(accountId));
                }
                cursor.close();
            }

            e.onSuccess(pairs);
        });
    }

    @Override
    public Single<Optional<AesKeyPair>> findLastKeyPair(int accountId, int peerId) {
        return Single.create(e -> {
            Uri uri = MessengerContentProvider.getKeysContentUriFor(accountId);
            Cursor cursor = getContext().getContentResolver()
                    .query(uri, null, KeyColumns.PEER_ID + " = ?",
                            new String[]{String.valueOf(peerId)}, KeyColumns._ID + " DESC LIMIT 1");

            AesKeyPair pair = null;
            if(nonNull(cursor)){
                if(cursor.moveToNext()){
                    pair = map(cursor).setAccountId(accountId);
                }

                cursor.close();
            }

            e.onSuccess(Optional.wrap(pair));
        });
    }

    @Override
    public Maybe<AesKeyPair> findKeyPairFor(int accountId, long sessionId) {
        return Maybe.create(e -> {
            Uri uri = MessengerContentProvider.getKeysContentUriFor(accountId);
            Cursor cursor = getContext().getContentResolver()
                    .query(uri, null, KeyColumns.SESSION_ID + " = ?",
                            new String[]{String.valueOf(sessionId)}, null);

            AesKeyPair pair = null;
            if(nonNull(cursor)){
                if(cursor.moveToNext()){
                    pair = map(cursor).setAccountId(accountId);
                }

                cursor.close();
            }

            if(nonNull(pair)){
                e.onSuccess(pair);
            }

            e.onComplete();
        });
    }


    @Override
    public Completable deleteAll(int accountId) {
        return Completable.create(e -> {
            Uri uri = MessengerContentProvider.getKeysContentUriFor(accountId);
            getContext().getContentResolver().delete(uri, null, null);
            e.onComplete();
        });
    }
}
