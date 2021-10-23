package dev.velaron.fennec.db.impl;

import static dev.velaron.fennec.util.Utils.safeCountOf;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import dev.velaron.fennec.db.MessengerContentProvider;
import dev.velaron.fennec.db.column.CountriesColumns;
import dev.velaron.fennec.db.interfaces.IDatabaseStore;
import dev.velaron.fennec.db.model.entity.CountryEntity;
import dev.velaron.fennec.util.Objects;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 20.09.2017.
 * phoenix
 */
public class DatabaseStorage extends AbsStorage implements IDatabaseStore {

    DatabaseStorage(@NonNull AppStorages base) {
        super(base);
    }

    @Override
    public Completable storeCountries(int accountId, List<CountryEntity> dbos) {
        return Completable.create(emitter -> {
            Uri uri = MessengerContentProvider.getCountriesContentUriFor(accountId);

            ArrayList<ContentProviderOperation> operations = new ArrayList<>(dbos.size() + 1);
            operations.add(ContentProviderOperation.newDelete(uri).build());

            for (CountryEntity dbo : dbos) {
                ContentValues cv = new ContentValues();
                cv.put(CountriesColumns._ID, dbo.getId());
                cv.put(CountriesColumns.NAME, dbo.getTitle());

                operations.add(ContentProviderOperation.newInsert(uri)
                        .withValues(cv)
                        .build());
            }

            getContentResolver().applyBatch(MessengerContentProvider.AUTHORITY, operations);
            emitter.onComplete();
        });
    }

    @Override
    public Single<List<CountryEntity>> getCountries(int accountId) {
        return Single.create(emitter -> {
            Uri uri = MessengerContentProvider.getCountriesContentUriFor(accountId);

            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            List<CountryEntity> dbos = new ArrayList<>(safeCountOf(cursor));

            if(Objects.nonNull(cursor)){
                while (cursor.moveToNext()){
                    if(emitter.isDisposed()){
                        break;
                    }

                    int id = cursor.getInt(cursor.getColumnIndex(CountriesColumns._ID));
                    String title = cursor.getString(cursor.getColumnIndex(CountriesColumns.NAME));
                    dbos.add(new CountryEntity(id, title));
                }

                cursor.close();
            }

            emitter.onSuccess(dbos);
        });
    }
}