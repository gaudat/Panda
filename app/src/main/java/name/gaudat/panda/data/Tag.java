package name.gaudat.panda.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anon on 11/1/2015.
 */
public class Tag implements Parcelable {
    // tags
    // used in galleries and search queries
    // TODO: will be passed into search workers
    // TODO: Implement as a textview (TagTextView?)

    public String namespace = "misc"; // actually is nothing but is shown as misc
    public String name; // tag contents
    public String attr; // tag class


    // blank constructor
    public Tag() {
    }

    // general query constructor
    public Tag(String query) {
        this.name = query;
    }

    // to search string query
    public String toQuery() {
        if (namespace.equals("misc")) {
            return "\"" + name + "\"";
        } else {
            return namespace + ":\"" + name + "\"";
        }
    }

    @Override
    public String toString() {
        return "Tag{" +
                "namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                ", attr='" + attr + '\'' +
                '}';
    }

    protected Tag(Parcel in) {
        namespace = in.readString();
        name = in.readString();
        attr = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(namespace);
        dest.writeString(name);
        dest.writeString(attr);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Tag> CREATOR = new Parcelable.Creator<Tag>() {
        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }

        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
}