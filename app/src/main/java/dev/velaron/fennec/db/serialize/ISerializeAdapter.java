package dev.velaron.fennec.db.serialize;

/**
 * Created by Ruslan Kolbasa on 20.06.2017.
 * phoenix
 */
public interface ISerializeAdapter<T> {
    T deserialize(String raw);
    String serialize(T data);
}