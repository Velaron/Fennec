package dev.velaron.fennec.longpoll;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import dev.velaron.fennec.api.model.longpoll.AddMessageUpdate;
import dev.velaron.fennec.util.Objects;
import dev.velaron.fennec.util.Utils;

public class FullAndNonFullUpdates {

    private List<AddMessageUpdate> full;
    private List<Integer> nonFull;

    @NonNull
    public List<AddMessageUpdate> prepareFull(){
        if(Objects.isNull(full)){
            full = new ArrayList<>(1);
        }

        return full;
    }

    @NonNull
    public List<Integer> prepareNonFull(){
        if(Objects.isNull(nonFull)){
            nonFull = new ArrayList<>(1);
        }

        return nonFull;
    }

    public List<AddMessageUpdate> getFullMessages() {
        return full;
    }

    public List<Integer> getNonFull() {
        return nonFull;
    }

    public boolean hasFullMessages(){
        return !Utils.safeIsEmpty(full);
    }

    public boolean hasNonFullMessages(){
        return !Utils.safeIsEmpty(nonFull);
    }

    @Override
    public String toString() {
        return "FullAndNonFullUpdates[" +
                "full=" + (full == null ? 0 : full.size()) +
                ", nonFull=" + (nonFull == null ? 0 : nonFull.size()) + ']';
    }
}
