package dev.velaron.fennec.model.criteria;

import dev.velaron.fennec.db.DatabaseIdRange;

/**
 * Created by admin on 09.01.2017.
 * phoenix
 */
public class FaveVideosCriteria extends Criteria {

    private final int accountId;

    private DatabaseIdRange range;

    public FaveVideosCriteria(int accountId) {
        this.accountId = accountId;
    }

    public FaveVideosCriteria setRange(DatabaseIdRange range) {
        this.range = range;
        return this;
    }

    public DatabaseIdRange getRange() {
        return range;
    }

    public int getAccountId() {
        return accountId;
    }
}
