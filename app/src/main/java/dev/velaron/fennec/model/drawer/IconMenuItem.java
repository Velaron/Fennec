package dev.velaron.fennec.model.drawer;

import android.os.Parcel;
import android.os.Parcelable;

public class IconMenuItem extends SectionMenuItem implements Parcelable {

    private int icon;

    public static Creator<IconMenuItem> CREATOR = new Creator<IconMenuItem>() {
        public IconMenuItem createFromParcel(Parcel source) {
            return new IconMenuItem(source);
        }

        public IconMenuItem[] newArray(int size) {
            return new IconMenuItem[size];
        }
    };

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(icon);
    }

    public IconMenuItem(int section, int icon, int title) {
        super(TYPE_ICON, section, title);
        this.icon = icon;
    }

    public IconMenuItem(Parcel in) {
        super(in);
        this.icon = in.readInt();
    }
}
