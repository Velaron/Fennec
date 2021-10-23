package dev.velaron.fennec.db.interfaces;

import java.util.List;

import dev.velaron.fennec.model.LogEvent;
import io.reactivex.Single;

/**
 * Created by Ruslan Kolbasa on 26.04.2017.
 * phoenix
 */
public interface ILogsStorage {

    Single<LogEvent> add(int type, String tag, String body);

    Single<List<LogEvent>> getAll(int type);
}
