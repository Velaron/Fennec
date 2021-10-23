package dev.velaron.fennec.domain;

import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.IdPair;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.annotations.Nullable;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public interface IAudioInteractor {
    Single<Audio> add(int accountId, Audio audio, Integer groupId, Integer albumId);
    Completable delete(int accountId, int audioId, int ownerId);
    Completable restore(int accountId, int audioId, int ownerId);

    Completable sendBroadcast(int accountId, int audioOwnerId, int audioId, @Nullable Collection<Integer> targetIds);

    Single<List<Audio>> get(int ownerId, int offset);

    Single<List<Audio>> getById(List<IdPair> audios);

    Single<List<Audio>> getPopular(int foreign, int genre);

    Single<List<Audio>> search(String query, boolean own, int offset);
//    Single<String> findAudioUrl(int audioId, int ownerId);

    boolean isAudioPluginAvailable();
}