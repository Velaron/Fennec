package dev.velaron.fennec.db.model.entity;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public final class EntitiesWrapper implements Iterable<Entity> {

    private final List<Entity> entities;

    public EntitiesWrapper(List<Entity> entities) {
        this.entities = entities;
    }

    public List<Entity> get() {
        return entities;
    }

    public static final EntitiesWrapper EMPTY = new EntitiesWrapper(Collections.emptyList());

    public static EntitiesWrapper wrap(List<Entity> entities){
        return entities == null ? EMPTY : new EntitiesWrapper(entities);
    }

    @NonNull
    @Override
    public Iterator<Entity> iterator() {
        return entities.iterator();
    }
}