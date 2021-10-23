package dev.velaron.fennec.domain.impl;

import java.util.Collection;
import java.util.List;

import dev.velaron.fennec.api.interfaces.INetworker;
import dev.velaron.fennec.domain.IAudioInteractor;
import dev.velaron.fennec.model.Audio;
import dev.velaron.fennec.model.IdPair;
import dev.velaron.fennec.plugins.IAudioPluginConnector;
import dev.velaron.fennec.util.Objects;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Created by admin on 07.10.2017.
 * Phoenix-for-VK
 */
public class AudioInteractor implements IAudioInteractor {

    private final INetworker networker;
    private final IAudioPluginConnector audioPluginConnector;

    public AudioInteractor(INetworker networker, IAudioPluginConnector pluginConnector) {
        this.networker = networker;
        this.audioPluginConnector = pluginConnector;
    }

    @Override
    public Single<Audio> add(int accountId, Audio orig, Integer groupId, Integer albumId) {
        return networker.vkDefault(accountId)
                .audio()
                .add(orig.getId(), orig.getOwnerId(), groupId, albumId)
                .map(resultId -> {
                    final int targetOwnerId = Objects.nonNull(groupId) ? -groupId : accountId;
                    //clone
                    return new Audio()
                            .setId(resultId)
                            .setOwnerId(targetOwnerId)
                            .setAlbumId(Objects.nonNull(albumId) ? albumId : 0)
                            .setArtist(orig.getArtist())
                            .setTitle(orig.getTitle())
                            .setUrl(orig.getUrl())
                            .setLyricsId(orig.getLyricsId())
                            .setGenre(orig.getGenre())
                            .setDuration(orig.getDuration());
                });
    }

    @Override
    public Completable delete(int accountId, int audioId, int ownerId) {
        return networker.vkDefault(accountId)
                .audio()
                .delete(audioId, ownerId)
                .ignoreElement();
    }

    @Override
    public Completable restore(int accountId, int audioId, int ownerId) {
        return networker.vkDefault(accountId)
                .audio()
                .restore(audioId, ownerId)
                .ignoreElement();
    }

    @Override
    public Completable sendBroadcast(int accountId, int audioOwnerId, int audioId, Collection<Integer> targetIds) {
        return networker.vkDefault(accountId)
                .audio()
                .setBroadcast(new dev.velaron.fennec.api.model.IdPair(audioId, audioOwnerId), targetIds)
                .ignoreElement();
    }

    @Override
    public Single<List<Audio>> get(int ownerId, int offset) {
        return audioPluginConnector.get(ownerId, offset);
    }

    @Override
    public Single<List<Audio>> getById(List<IdPair> audios) {
        return audioPluginConnector.getById(audios);
    }

    @Override
    public Single<List<Audio>> getPopular(int foreign, int genre) {
        return audioPluginConnector.getPopular(foreign, genre);
    }

    @Override
    public Single<List<Audio>> search(String query, boolean own, int offset) {
        return audioPluginConnector.search(query, own, offset);
    }

//    @Override
//    public Single<String> findAudioUrl(int audioId, int ownerId) {
//        return audioPluginConnector.findAudioUrl(audioId, ownerId);
//    }

    @Override
    public boolean isAudioPluginAvailable() {
        return audioPluginConnector.isPluginAvailable();
    }
}