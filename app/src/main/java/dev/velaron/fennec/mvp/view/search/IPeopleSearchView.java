package dev.velaron.fennec.mvp.view.search;

import dev.velaron.fennec.model.User;

/**
 * Created by admin on 02.05.2017.
 * phoenix
 */
public interface IPeopleSearchView extends IBaseSearchView<User> {
    void openUserWall(int accountId, User user);
}