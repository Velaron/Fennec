package dev.velaron.fennec.model.menu;

import androidx.annotation.DrawableRes;
import dev.velaron.fennec.model.Text;

/**
 * Created by admin on 11.06.2017.
 * phoenix
 */
public class Section {

    @DrawableRes
    private Integer icon;

    private final Text title;

    public Section(Text title) {
        this.title = title;
    }

    public Section setIcon(Integer icon) {
        this.icon = icon;
        return this;
    }

    public Text getTitle() {
        return title;
    }

    public Integer getIcon() {
        return icon;
    }
}
