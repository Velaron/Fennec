package dev.velaron.fennec.activity;

import dev.velaron.fennec.model.SelectProfileCriteria;
import dev.velaron.fennec.model.User;

public interface ProfileSelectable {

    void select(User user);

    SelectProfileCriteria getAcceptableCriteria();
}