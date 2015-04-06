package name.gaudat.panda.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by anon on 11/1/2015.
 */
public class Gallery implements Parcelable {
    // The panda gallery
    // Contains pages and various metadata

    // Various metadata
    public String id;
    public String token;
    public String title;
    public String title2;
    public String thumb;
    public int type;
    public String uploader;
    public String uploaded;
    public String language;
    public int raters;
    public float rating;

    public ArrayList<Tag> tags;
    public ArrayList<Page> pages;
    // list indexes corresponds to zero based page number


    // making the gallery with id and token
    public Gallery(String id, String token) {
        this.id = id;
        this.token = token;

    }

    // everything that can be scrapped from list view
    public Gallery(String id, String token, String title, String thumb, String type, String uploader, String uploaded, float rating) {
        this.id = id;
        this.token = token;
        this.title = title;
        this.thumb = thumb;
        this.type = Constants.TYPEMAP.get(type);
        this.uploader = uploader;
        this.uploaded = uploaded;
        this.rating = rating;

    }

    // everything that can be scrapped from thumb view
    public Gallery(String id, String token, String title, String thumb, String type, int pages, float rating) {
        this.id = id;
        this.token = token;
        this.title = title;
        this.thumb = thumb;
        this.type = Constants.TYPEMAP.get(type);
        this.pages = new ArrayList<>(pages);
        for (int i = 0; i < pages; i++) {
            this.pages.add(null);
        }
        // ugly hack
        // we fill the array list with nulls first so it shows the correct size

        this.rating = rating;

    }

    public String getPath() {
        return "/g/" + id + "/" + token;
    }

    // TODO: updater (worker) should update metadata and copy gid to individual pages
    // the pages should have been initialized
    // if not, do it in the worker after getting number of total pages
    public class UpdateRequest {
        // request for updating the gallery
        public Gallery g;
        // zero based page number
        // -1 means all
        public int start; // which page to start updating (including that page)
        public int end; // which page to stop updating (including that page)

        public UpdateRequest(Gallery gallery, int start, int end) {
            this.g = gallery;
            this.start = start;
            this.end = end;
        }


    }

    // getting the gallery and some options to pass to the worker
    public UpdateRequest getUpdateRequest(int start, int end) {
        // only get the overview pages
        // we are not raping the server
        // although we love rape pandas
        return new UpdateRequest(this, start, end);
    }

    // shorthand for only updating that page
    public UpdateRequest getUpdateRequest(int page) {
        return getUpdateRequest(page, page);
    }

    // shorthand for updating the first page
    // used in the first update
    public UpdateRequest getUpdateRequest() {
        return getUpdateRequest(0);
    }

    // TODO: used by downloader (a kind of worker)
    // similar to hath download
    // the actual download calls will be passed to individual pages
    public class DownloadRequest {
        public Gallery g; // a copy of this
        public int start; // which page to start (inclusive)
        public int end; // which page to end (inclusive)

        // page numbers are zero based
        // -1 means all
        public DownloadRequest(Gallery g, int start, int end) {
            this.g = g;
            this.start = start;
            this.end = end;
        }
    }

    // download a range of pages
    public DownloadRequest getDownloadRequest(int start, int end) {
        return new DownloadRequest(this, start, end);
    }

    // download one page
    public DownloadRequest getDownloadRequest(int page) {
        return getDownloadRequest(page, page);
    }

    // download all pages
    public DownloadRequest getDownloadRequest() {
        return getDownloadRequest(-1);
    }


    @Override
    public String toString() {
        return "Gallery{" +
                "id='" + id + '\'' +
                ", token='" + token + '\'' +
                ", title='" + title + '\'' +
                ", title2='" + title2 + '\'' +
                ", thumb='" + thumb + '\'' +
                ", type='" + type + '\'' +
                ", uploader='" + uploader + '\'' +
                ", uploaded='" + uploaded + '\'' +
                ", language='" + language + '\'' +
                ", raters=" + raters +
                ", rating=" + rating +
                ", tags=" + tags +
                ", pages=" + pages +
                '}';
    }


    // Parcelable code from here
    protected Gallery(Parcel in) {
        id = in.readString();
        token = in.readString();
        title = in.readString();
        title2 = in.readString();
        thumb = in.readString();
        type = in.readInt();
        uploader = in.readString();
        uploaded = in.readString();
        language = in.readString();
        raters = in.readInt();
        rating = in.readFloat();
        in.readTypedList(tags, Tag.CREATOR);
        in.readTypedList(pages, Page.CREATOR);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(token);
        dest.writeString(title);
        dest.writeString(title2);
        dest.writeString(thumb);
        dest.writeInt(type);
        dest.writeString(uploader);
        dest.writeString(uploaded);
        dest.writeString(language);
        dest.writeInt(raters);
        dest.writeDouble(rating);
        dest.writeTypedList(tags);
        dest.writeTypedList(pages);
    }

    public static final Parcelable.Creator<Gallery> CREATOR = new Parcelable.Creator<Gallery>() {
        @Override
        public Gallery createFromParcel(Parcel in) {
            return new Gallery(in);
        }

        @Override
        public Gallery[] newArray(int size) {
            return new Gallery[size];
        }
    };
}