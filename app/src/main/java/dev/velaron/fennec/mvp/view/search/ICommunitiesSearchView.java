package dev.velaron.fennec.mvp.view.search;

import dev.velaron.fennec.model.Community;

/**
 * Created by admin on 19.09.2017.
 * phoenix
 */
public interface ICommunitiesSearchView extends IBaseSearchView<Community> {
    void openCommunityWall(int accountId, Community community);
}