package name.gaudat.panda.data;

import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anon on 11/1/2015.
 */
public class Page implements Parcelable {
    // Pages
    // contains the real thing
    public int pn; // zero based
    public String gid; // Not duplicate as it will be useful in generating URL
    public String gtitle; // Used in notification, also can be scraped
    public String fn; // filename
    public String gtoken;
    public String ptoken; // Will populate once cached
    public String firstp; // look like these 4 will not be used
    public String prevp; // a linked list is better
    public String nextp;
    public String lastp;
    // Pages immediately navigable from this page
    public int width;
    public int height;
    public int twidth;
    public int thoffset; // thumbnail offset
    public int theight;
    public String size;
    public String thumb;
    public String img;
    public String nl;
    public String hash;
    public boolean clicked = false; // false if not updated
    public boolean hasthumb = false;

    public Page() {
    }

    // basic constructor, can be scraped from gallery overview page
    public Page(int pn, String gid, String gtitle, String thumb, String ptoken, int twidth, int theight, int thoffset) {
        this.pn = pn;
        this.gid = gid;
        this.gtitle = gtitle;
        this.thumb = thumb;
        this.ptoken = ptoken;
        this.clicked = false;
        this.twidth = twidth;
        this.theight = theight;
        this.thoffset = thoffset;
        this.hasthumb = true;

    }

    // basic constructor, can be scraped from uri
    public Page(int pn, String gid, String ptoken) {
        this.pn = pn;
        this.gid = gid;
        this.ptoken = ptoken;
        this.clicked = false;
        this.hasthumb = false;
    }


    @Override
    public String toString() {
        return "Page{" +
                "pn=" + pn +
                ", gid='" + gid + '\'' +
                ", gtitle='" + gtitle + '\'' +
                ", gtoken='" + gtoken + '\'' +
                ", ptoken='" + ptoken + '\'' +
                ", firstp='" + firstp + '\'' +
                ", prevp='" + prevp + '\'' +
                ", nextp='" + nextp + '\'' +
                ", lastp='" + lastp + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", twidth=" + twidth +
                ", thoffset=" + thoffset +
                ", theight=" + theight +
                ", size='" + size + '\'' +
                ", thumb='" + thumb + '\'' +
                ", img='" + img + '\'' +
                ", nl='" + nl + '\'' +
                ", clicked=" + clicked +
                ", hasthumb=" + hasthumb +
                '}';
    }


    public String getFileExt() {
        return fn.substring(img.lastIndexOf(".") + 1); // the last part is file extension;
    }

    // Update request
    // TODO: updates all metadata
    public class UpdateRequest {
        public Page p;

        public UpdateRequest(Page p) {
            this.p = p;
        }
    }

    public SearchQuery getSearchQuery() {
        return new SearchQuery(Constants.SEARCH_QUERY_FILE_NAME, hash, false, false, 1);
    }


    // constructs update request
    public UpdateRequest getUpdateRequest() {
        return new UpdateRequest(this);
    }

    // Download request
    // TODO: download big image
    public class DownloadRequest {
        public String img;
        public String dest; // where to download to

        public DownloadRequest(Page p) {
            this.img = p.img;
            // Download to picture folders by default
            this.dest = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath() +
                    Constants.FOLDER_PREFIX + "/" + p.gid + "-" + ((Integer) (p.pn + 1)).toString() + "." +
                    p.getFileExt();
            // TODO: check for slashes and correct path
        }

        public DownloadRequest(Page p, String dest) {
            this.img = p.img;
            this.dest = dest + "." + p.getFileExt();
        }
    }

    public String toPath() {
        return "/s/" + ptoken + "/" + gid + "-" + (pn + 1);
    }

    // constructs download request
    public DownloadRequest getDownloadRequest() {
        return new DownloadRequest(this);
    }

    // download request with custom target, does not include file extension
    public DownloadRequest getDownloadRequest(String dest) {
        return new DownloadRequest(this, dest);
    }


    protected Page(Parcel in) {
        pn = in.readInt();
        gid = in.readString();
        gtitle = in.readString();
        fn = in.readString();
        gtoken = in.readString();
        ptoken = in.readString();
        firstp = in.readString();
        prevp = in.readString();
        nextp = in.readString();
        lastp = in.readString();
        width = in.readInt();
        height = in.readInt();
        twidth = in.readInt();
        thoffset = in.readInt();
        theight = in.readInt();
        size = in.readString();
        thumb = in.readString();
        img = in.readString();
        nl = in.readString();
        hash = in.readString();
        clicked = in.readByte() != 0x00;
        hasthumb = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(pn);
        dest.writeString(gid);
        dest.writeString(gtitle);
        dest.writeString(fn);
        dest.writeString(gtoken);
        dest.writeString(ptoken);
        dest.writeString(firstp);
        dest.writeString(prevp);
        dest.writeString(nextp);
        dest.writeString(lastp);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(twidth);
        dest.writeInt(thoffset);
        dest.writeInt(theight);
        dest.writeString(size);
        dest.writeString(thumb);
        dest.writeString(img);
        dest.writeString(nl);
        dest.writeString(hash);
        dest.writeByte((byte) (clicked ? 0x01 : 0x00));
        dest.writeByte((byte) (hasthumb ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Page> CREATOR = new Parcelable.Creator<Page>() {
        @Override
        public Page createFromParcel(Parcel in) {
            return new Page(in);
        }

        @Override
        public Page[] newArray(int size) {
            return new Page[size];
        }
    };
}