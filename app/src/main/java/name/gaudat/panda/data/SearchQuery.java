package name.gaudat.panda.data;


import android.os.Parcel;
import android.os.Parcelable;
import name.gaudat.panda.R;

import java.util.HashMap;

public class SearchQuery implements Parcelable {

    // sample search link
    // (displays everything)
    // http://g.e-hentai.org/?f_doujinshi=1&f_manga=1&f_artistcg=1&f_gamecg=1
    // &f_western=1&f_non-h=1&f_imageset=1&f_cosplay=1&f_asianporn=1&f_misc=1
    // &f_search=Search+Keywords&f_apply=Apply+Filter&advsearch=1&f_sname=on
    // &f_stags=on&f_sdesc=on&f_sdt1=on&f_sdt2=on&f_sh=on&f_sr=on&f_srdd=3

    public String query = "";
    public HashMap<Integer, Integer> type = new HashMap<>();
    // include which kind of gallery

    public boolean searchName = true;
    public boolean searchTags = true;
    public boolean searchDesc = false;
    public boolean searchLowPower = false;
    public boolean searchExpunged = false;
    public boolean searchDownvoted = false;
    public boolean searchMinStars = false;
    public int searchMinRating = 2;

    // file search url (page)
    // http://g.e-hentai.org/?f_shash=c7ea697aeb3e19bdf51b27f63464aab28fed8b3c
    // ;3c9eb386a194359b0aabcf9ca789e4f7d3ffa10b
    // &fs_from=C87Pod_Luck_Life0001.jpg+from+%28C87%29%5BPod+Luck+Life%28%E3
    // %81%9F%E3%81%AC%E3%81%8D%E3%81%A1%29%5D%E3%81%AE%E3%82%93%E3%81%9F%E3
    // %81%AC%E3%81%A8%E3%82%AD%E3%83%84%E3%83%8D%E2%80%95%E3%83%81%E3%82%AB

    // file hash
    // file source (arbitary)
    // search cover, should be nothing instead of true
    // also search expunged
    public String hash = "";
    public String imageSearchName = "";
    public boolean imageSearchCoversOnly = false;
    public boolean imageSearchExpunged = false;


    // file search url (upload)
    // http://g.e-hentai.org/?f_shash=30b5caf1d980b11605bcd9d7bd4ab00b3139151d
    // &fs_from=23781890.png&fs_similar=1
    // f_shash is sha1 hash of the uploaded file
    public int imageSearchSimilar = 1;
    // 0 to disable similarity search


    // hybrid search
    // http://g.e-hentai.org/?f_doujinshi=1&f_manga=1&f_artistcg=1&f_gamecg=1
    // &f_western=1&f_non-h=1&f_imageset=1&f_cosplay=1&f_asianporn=1&f_misc=1
    // &f_search=&f_apply=Apply+Filter
    // &f_shash=30b5caf1d980b11605bcd9d7bd4ab00b3139151d
    // &fs_from=23781890.png&fs_similar=0

    public int searchMode = Constants.SEARCH_WITH_LIST_VIEW;
    public int whichPage = 0; // get which page

    public SearchQuery() {
        // search nothing, display everything
        type.put(R.id.search1, 1);
        type.put(R.id.search2, 1);
        type.put(R.id.search3, 1);
        type.put(R.id.search4, 1);
        type.put(R.id.search5, 1);
        type.put(R.id.search6, 1);
        type.put(R.id.search7, 1);
        type.put(R.id.search8, 1);
        type.put(R.id.search9, 1);
        type.put(R.id.search10, 1);
    }

    public String toPath() {
        return "/?f_doujinshi=" + type.get(R.id.search1)
                + "&f_manga=" + type.get(R.id.search2)
                + "&f_artistcg=" + type.get(R.id.search3)
                + "&f_gamecg=" + type.get(R.id.search4)
                + "&f_western=" + type.get(R.id.search5)
                + "&f_non-h=" + type.get(R.id.search6)
                + "&f_imageset=" + type.get(R.id.search7)
                + "&f_cosplay=" + type.get(R.id.search8)
                + "&f_asianporn=" + type.get(R.id.search9)
                + "&f_misc=" + type.get(R.id.search10)
                + "&f_search=" + query
                + (searchName ? "&f_sname=on" : "")
                + (searchTags ? "&f_stags=on" : "")
                + (searchDesc ? "&f_sdesc=on" : "")
                + (searchExpunged ? "&f_sh=on" : "")
                + (searchDownvoted ? "&f_sdt2=on" : "")
                + (searchLowPower ? "&f_sdt1=on" : "")
                + (searchMinStars ? "&f_sr=on&f_srdd=" + searchMinRating : "")
                + (hash.isEmpty() ? "" : "&f_shash=" + hash)
                + (imageSearchCoversOnly ? "&fs_covers=on" : "")
                + (imageSearchExpunged ? "&fs_exp=on" : "")
                + "&fs_similar=" + imageSearchSimilar
                ;
        // whew
    }

    public SearchQuery(String name, String hash, boolean coversOnly, boolean doExpunged, int similar) {
        // from page
        this.imageSearchName = name;
        this.hash = hash;
        this.imageSearchCoversOnly = coversOnly;
        this.imageSearchExpunged = doExpunged;
        this.imageSearchSimilar = similar;
    }

    @Override
    public String toString() {
        return "SearchQuery{" +
                "query='" + query + '\'' +
                ", type=" + type +
                ", searchName=" + searchName +
                ", searchTags=" + searchTags +
                ", searchDesc=" + searchDesc +
                ", searchLowPower=" + searchLowPower +
                ", searchExpunged=" + searchExpunged +
                ", searchDownvoted=" + searchDownvoted +
                ", searchMinStars=" + searchMinStars +
                ", searchMinRating=" + searchMinRating +
                ", hash='" + hash + '\'' +
                ", imageSearchName='" + imageSearchName + '\'' +
                ", imageSearchCoversOnly=" + imageSearchCoversOnly +
                ", imageSearchExpunged=" + imageSearchExpunged +
                ", imageSearchSimilar=" + imageSearchSimilar +
                ", whichPage=" + whichPage +
                ", searchMode=" + searchMode +
                '}';
    }


    protected SearchQuery(Parcel in) {
        query = in.readString();
        type = (HashMap) in.readValue(HashMap.class.getClassLoader());
        searchName = in.readByte() != 0x00;
        searchTags = in.readByte() != 0x00;
        searchDesc = in.readByte() != 0x00;
        searchLowPower = in.readByte() != 0x00;
        searchExpunged = in.readByte() != 0x00;
        searchDownvoted = in.readByte() != 0x00;
        searchMinStars = in.readByte() != 0x00;
        searchMinRating = in.readInt();
        hash = in.readString();
        imageSearchName = in.readString();
        imageSearchCoversOnly = in.readByte() != 0x00;
        imageSearchExpunged = in.readByte() != 0x00;
        imageSearchSimilar = in.readInt();
        searchMode = in.readInt();
        whichPage = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(query);
        dest.writeValue(type);
        dest.writeByte((byte) (searchName ? 0x01 : 0x00));
        dest.writeByte((byte) (searchTags ? 0x01 : 0x00));
        dest.writeByte((byte) (searchDesc ? 0x01 : 0x00));
        dest.writeByte((byte) (searchLowPower ? 0x01 : 0x00));
        dest.writeByte((byte) (searchExpunged ? 0x01 : 0x00));
        dest.writeByte((byte) (searchDownvoted ? 0x01 : 0x00));
        dest.writeByte((byte) (searchMinStars ? 0x01 : 0x00));
        dest.writeInt(searchMinRating);
        dest.writeString(hash);
        dest.writeString(imageSearchName);
        dest.writeByte((byte) (imageSearchCoversOnly ? 0x01 : 0x00));
        dest.writeByte((byte) (imageSearchExpunged ? 0x01 : 0x00));
        dest.writeInt(imageSearchSimilar);
        dest.writeInt(searchMode);
        dest.writeInt(whichPage);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SearchQuery> CREATOR = new Parcelable.Creator<SearchQuery>() {
        @Override
        public SearchQuery createFromParcel(Parcel in) {
            return new SearchQuery(in);
        }

        @Override
        public SearchQuery[] newArray(int size) {
            return new SearchQuery[size];
        }
    };
}