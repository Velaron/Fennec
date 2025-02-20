package dev.velaron.fennec.model;

import dev.velaron.fennec.model.criteria.Criteria;

/**
 * Created by ruslan.kolbasa on 14.12.2016.
 * phoenix
 */
public class FeedSourceCriteria extends Criteria {

    private final int accountId;

    public FeedSourceCriteria(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }
}
