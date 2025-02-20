package dev.velaron.fennec.media.voice;

import androidx.annotation.NonNull;
import dev.velaron.fennec.model.VoiceMessage;

/**
 * Created by r.kolbasa on 27.11.2017.
 * Phoenix-for-VK
 */
public class AudioEntry {

    private int id;
    private VoiceMessage audio;

    public AudioEntry(int id, @NonNull VoiceMessage audio) {
        this.id = id;
        this.audio = audio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AudioEntry entry = (AudioEntry) o;
        return id == entry.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    public int getId() {
        return id;
    }

    public VoiceMessage getAudio() {
        return audio;
    }
}