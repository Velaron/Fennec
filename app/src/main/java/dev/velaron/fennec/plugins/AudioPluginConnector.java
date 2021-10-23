package dev.velaron.fennec.plugins;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.IdPair;
import dev.velaron.fennec.util.Utils;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;

import static dev.velaron.fennec.util.Objects.isNull;

/**
 * Created by admin on 2/3/2018.
 * Phoenix-for-VK
 */
public class AudioPluginConnector implements IAudioPluginConnector {

    private static final String AUTHORITY = "biz.dealnote.phoenix.AudioProvider";

    private final Context app;

    public AudioPluginConnector(Context context) {
        this.app = context.getApplicationContext();
    }

    protected static String join(Collection<IdPair> audios, String delimiter) {
        if (isNull(audios)) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (IdPair pair : audios) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }

            sb.append(pair.ownerId + "_" + pair.id);
        }

        return sb.toString();
    }

    @Override
    public Single<List<Audio>> get(int ownerId, int offset) {
        return Single.create(emitter -> {
            Uri uri = new Uri.Builder()
                    .scheme("content")
                    .authority(AUTHORITY)
                    .path("audios")
                    .appendQueryParameter("request", "get")
                    .appendQueryParameter("owner_id", String.valueOf(ownerId))
                    .appendQueryParameter("offset", String.valueOf(offset))
                    .build();

            parseAndInsertToDb(emitter, uri);
        });
    }

    @Override
    public Single<List<Audio>> getById(List<IdPair> audios) {
        return Single.create(emitter -> {
            Uri uri = new Uri.Builder()
                    .scheme("content")
                    .authority(AUTHORITY)
                    .path("audios")
                    .appendQueryParameter("request", "getById")
                    .appendQueryParameter("audios", join(audios, ","))
                    .build();

            parseAndInsertToDb(emitter, uri);
        });
    }

    @Override
    public Single<List<Audio>> getPopular(int foreign, int genre) {
        return Single.create(emitter -> {
            Uri uri = new Uri.Builder()
                    .scheme("content")
                    .authority(AUTHORITY)
                    .path("audios")
                    .appendQueryParameter("request", "getPopular")
                    .appendQueryParameter("foreign", String.valueOf(foreign))
                    .appendQueryParameter("genre", String.valueOf(genre))
                    .build();

            parseAndInsertToDb(emitter, uri);
        });
    }

    @Override
    public Single<List<Audio>> search(String query, boolean own, int offset) {
        return Single.create(emitter -> {
            Uri uri = new Uri.Builder()
                    .scheme("content")
                    .authority(AUTHORITY)
                    .path("search")
                    .appendQueryParameter("own", String.valueOf(own))
                    .appendQueryParameter("query", query)
                    .appendQueryParameter("offset", String.valueOf(offset))
                    .build();
            parseAndInsertToDb(emitter, uri);
        });
    }

    private static void checkAudioPluginError(Cursor cursor) throws AudioPluginException {
        if (cursor.getPosition() == 0) {
            int errorCodeIndex = cursor.getColumnIndex("error_code");

            if (errorCodeIndex >= 0) {
                int code = cursor.getInt(errorCodeIndex);
                String message = cursor.getString(cursor.getColumnIndex("error_message"));
                throw new AudioPluginException(code, message);
            }
        }
    }

    @Override
    public boolean isPluginAvailable() {
        Uri uri = new Uri.Builder()
                .scheme("content")
                .authority(AUTHORITY)
                .path("availability")
                .build();

        boolean available = false;

        try {
            Cursor cursor = app.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.moveToNext()) {
                    available = cursor.getInt(cursor.getColumnIndex("available")) == 1;
                }
                cursor.close();
            }
        } catch (Exception ignored) {

        }

        return available;
    }

    private void parseAndInsertToDb(SingleEmitter<List<Audio>> emitter, Uri uri) {
        Cursor cursor = app.getContentResolver().query(uri, null, null, null, null);

        List<Audio> audios = new ArrayList<>(Utils.safeCountOf(cursor));

        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (emitter.isDisposed()) {
                    break;
                }

                try {
                    checkAudioPluginError(cursor);
                } catch (Exception e) {
                    cursor.close();
                    emitter.onError(e);
                    return;
                }

                int audioId = cursor.getInt(cursor.getColumnIndex("audio_id"));
                int ownerId1 = cursor.getInt(cursor.getColumnIndex("owner_id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String artist = cursor.getString(cursor.getColumnIndex("artist"));
                int duration = cursor.getInt(cursor.getColumnIndex("duration"));
                String url = cursor.getString(cursor.getColumnIndex("url"));
                String cover = cursor.getString(cursor.getColumnIndex("cover_url"));
                String bigCover = cursor.getString(cursor.getColumnIndex("cover_url_big"));
                int isHq = cursor.getInt(cursor.getColumnIndex("is_hq"));

                Audio audio = new Audio()
                        .setArtist(artist)
                        .setDuration(duration)
                        .setId(audioId)
                        .setOwnerId(ownerId1)
                        .setTitle(title)
                        .setUrl(url)
                        .setBigCover(bigCover)
                        .setCover(cover)
                        .setHq(isHq == 1);

                audios.add(audio);
            }
            cursor.close();
        }
        emitter.onSuccess(audios);
    }

    public static final class AudioPluginException extends Exception {

        final int code;

        AudioPluginException(int code, String message) {
            super(message);
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}