package dev.velaron.fennec.model.drawer;

import android.os.Parcel;
import android.os.Parcelable;

public class AbsMenuItem implements Parcelable {

    public static final int TYPE_ICON = 0;
    public static final int TYPE_WITHOUT_ICON = 1;
    public static final int TYPE_DIVIDER = 2;
    public static final int TYPE_RECENT_CHAT = 3;

    private boolean selected;

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public static Creator<AbsMenuItem> CREATOR = new Creator<AbsMenuItem>() {
        public AbsMenuItem createFromParcel(Parcel source) {
            return new AbsMenuItem(source);
        }

        public AbsMenuItem[] newArray(int size) {
            return new AbsMenuItem[size];
        }
    };

    private int type;

    public int getType() {
        return type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeInt(selected ? 1 : 0);
    }

    public AbsMenuItem(int type) {
        this.type = type;
    }

    public AbsMenuItem(Parcel in) {
        this.type = in.readInt();
        this.selected = in.readInt() == 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbsMenuItem that = (AbsMenuItem) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return type;
    }
}