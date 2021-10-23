package dev.velaron.fennec.settings;

import java.util.List;

import dev.velaron.fennec.model.ProxyConfig;
import dev.velaron.fennec.util.Optional;
import io.reactivex.Observable;

/**
 * Created by admin on 10.07.2017.
 * phoenix
 */
public interface IProxySettings {
    void put(String address, int port);
    void put(String address, int port, String username, String pass);

    Observable<ProxyConfig> observeAdding();
    Observable<ProxyConfig> observeRemoving();
    Observable<Optional<ProxyConfig>> observeActive();

    List<ProxyConfig> getAll();

    ProxyConfig getActiveProxy();

    void setActive(ProxyConfig config);

    void delete(ProxyConfig config);
}