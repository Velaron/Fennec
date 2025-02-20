package dev.velaron.fennec.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import dev.velaron.fennec.api.model.VkApiPrivacy;

/**
 * Created by ruslan.kolbasa on 30.11.2016.
 * phoenix
 */
public class SimplePrivacy implements Parcelable {

    private final String type;
    private final List<Entry> entries;

    public SimplePrivacy(String type, List<Entry> entries) {
        this.type = type;
        this.entries = entries;
    }

    protected SimplePrivacy(Parcel in) {
        type = in.readString();
        entries = in.createTypedArrayList(Entry.CREATOR);
    }

    public static final Creator<SimplePrivacy> CREATOR = new Creator<SimplePrivacy>() {
        @Override
        public SimplePrivacy createFromParcel(Parcel in) {
            return new SimplePrivacy(in);
        }

        @Override
        public SimplePrivacy[] newArray(int size) {
            return new SimplePrivacy[size];
        }
    };

    public String getType() {
        return type;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(type);
        parcel.writeTypedList(entries);
    }

    public static class Entry implements Parcelable {

        public static final int TYPE_USER = 1;
        public static final int TYPE_FRIENDS_LIST = 2;

        private int type;
        private int id;
        private boolean allowed;

        public int getType() {
            return type;
        }

        public int getId() {
            return id;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public Entry(int type, int id, boolean allowed) {
            this.type = type;
            this.id = id;
            this.allowed = allowed;
        }

        protected Entry(Parcel in) {
            type = in.readInt();
            id = in.readInt();
            allowed = in.readByte() != 0;
        }

        public static final Creator<Entry> CREATOR = new Creator<Entry>() {
            @Override
            public Entry createFromParcel(Parcel in) {
                return new Entry(in);
            }

            @Override
            public Entry[] newArray(int size) {
                return new Entry[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(type);
            dest.writeInt(id);
            dest.writeByte((byte) (allowed ? 1 : 0));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VkApiPrivacy.Entry entry = (VkApiPrivacy.Entry) o;
            return type == entry.type && id == entry.id && allowed == entry.allowed;
        }

        @Override
        public int hashCode() {
            int result = type;
            result = 31 * result + id;
            result = 31 * result + (allowed ? 1 : 0);
            return result;
        }
    }
}