package dev.velaron.fennec.db;

import android.os.Parcel;
import android.os.Parcelable;

public class DatabaseIdRange implements Parcelable {

    private final int first;
    private final int last;

    private DatabaseIdRange(int first, int last){
        this.first = first;
        this.last = last;
    }

    protected DatabaseIdRange(Parcel in) {
        first = in.readInt();
        last = in.readInt();
    }

    public int getFirst() {
        return first;
    }

    public int getLast() {
        return last;
    }

    public static final Creator<DatabaseIdRange> CREATOR = new Creator<DatabaseIdRange>() {
        @Override
        public DatabaseIdRange createFromParcel(Parcel in) {
            return new DatabaseIdRange(in);
        }

        @Override
        public DatabaseIdRange[] newArray(int size) {
            return new DatabaseIdRange[size];
        }
    };

    public static DatabaseIdRange create(int first, int last){
        return new DatabaseIdRange(first, last);
    }

    @Override
    public String toString() {
        return "DatabaseIdRange{" +
                "first=" + first +
                ", last=" + last +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(first);
        dest.writeInt(last);
    }
}