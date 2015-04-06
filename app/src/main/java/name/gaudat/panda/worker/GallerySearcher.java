package name.gaudat.panda.worker;

import android.os.AsyncTask;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;
import name.gaudat.panda.SearchActivity;
import name.gaudat.panda.data.Constants;
import name.gaudat.panda.data.Gallery;
import name.gaudat.panda.data.SearchQuery;
import name.gaudat.panda.data.SearchResult;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class GallerySearcher extends AsyncTask<View, Void, SearchResult> {
    //TODO
    // Search with account and tags
    // Normal queries are treated as tags with misc namespace
    // with SearchSettings option

    // sample search link
    // (displays everything)
    // http://g.e-hentai.org/?f_doujinshi=1&f_manga=1&f_artistcg=1&f_gamecg=1
    // &f_western=1&f_non-h=1&f_imageset=1&f_cosplay=1&f_asianporn=1&f_misc=1
    // &f_search=Search+Keywords&f_apply=Apply+Filter&advsearch=1&f_sname=on
    // &f_stags=on&f_sdesc=on&f_sdt1=on&f_sdt2=on&f_sh=on&f_sr=on&f_srdd=3

    // search type
    int f_doujinshi = 1;
    int f_manga = 1;
    int f_artistcg = 1;
    int f_gamecg = 1;
    int f_western = 1;
    int f_nonh = 1; // should be "f_non-h" but java doesn't allow hyphens
    int f_imageset = 1;
    int f_cosplay = 1;
    int f_asianporn = 1;
    int f_misc = 1;

    String f_search = ""; // Search query

    int f_sname = 1; // search name
    int f_stags = 1; // search tags
    int f_sdesc = 1; // search desc
    boolean f_sh = true; // search expunged, nothing if not checked
    boolean f_sdt2 = true; // search downvoted
    boolean f_sdt1 = true; // search low power
    boolean f_sr = true; // search min rating
    int f_srdd = 2; // min rating

    // file search url (page)
    // http://g.e-hentai.org/?f_shash=c7ea697aeb3e19bdf51b27f63464aab28fed8b3c
    // ;3c9eb386a194359b0aabcf9ca789e4f7d3ffa10b
    // &fs_from=C87Pod_Luck_Life0001.jpg+from+%28C87%29%5BPod+Luck+Life%28%E3
    // %81%9F%E3%81%AC%E3%81%8D%E3%81%A1%29%5D%E3%81%AE%E3%82%93%E3%81%9F%E3
    // %81%AC%E3%81%A8%E3%82%AD%E3%83%84%E3%83%8D%E2%80%95%E3%83%81%E3%82%AB

    String f_shash = ""; // file hash
    String fs_from = ""; // file source (arbitary)
    boolean fs_covers = false; // search cover, should be nothing instead of true
    boolean fs_exp = false; // also search expunged

    // file search url (upload)
    // http://g.e-hentai.org/?f_shash=30b5caf1d980b11605bcd9d7bd4ab00b3139151d
    // &fs_from=23781890.png&fs_similar=1
    // f_shash is sha1 hash of the uploaded file
    int fs_similar = 1; // 0 to disable similarity search

    // hybrid search
    // http://g.e-hentai.org/?f_doujinshi=1&f_manga=1&f_artistcg=1&f_gamecg=1
    // &f_western=1&f_non-h=1&f_imageset=1&f_cosplay=1&f_asianporn=1&f_misc=1
    // &f_search=&f_apply=Apply+Filter
    // &f_shash=30b5caf1d980b11605bcd9d7bd4ab00b3139151d
    // &fs_from=23781890.png&fs_similar=0

    SearchQuery sq;
    SearchActivity sa;
    SearchResult sr;

    boolean forceupdate = true;
    private boolean update;

    public GallerySearcher(SearchQuery sq, SearchActivity sa) {
        Log.d(getClass().toString(), "Created by query");
        this.sq = sq;
        this.sa = sa;
        this.sr = new SearchResult();
    }

    public GallerySearcher(SearchQuery sq, SearchActivity sa, boolean forceupdate) {
        Log.d(getClass().toString(), "Created by query");
        this.sq = sq;
        this.sa = sa;
        this.sr = new SearchResult();
        this.forceupdate = forceupdate;
    }

    public GallerySearcher(SearchResult sr, SearchActivity sa) {
        Log.d(getClass().toString(), "Created by result");
        this.sq = sr.query;
        this.sa = sa;
        this.sr = sr;
    }

    @Override
    protected SearchResult doInBackground(View... params) {

        if (forceupdate) {
            if (params[0] instanceof FrameLayout) {
                // it is a container
                Log.d(getClass().toString(), "Passed a container");
                update = false;
            } else if (params[0] instanceof ListView) {
                // it is an update
                Log.d(getClass().toString(), "Passed a ListView");
                update = true;
            } else if (params[0] instanceof GridView) {
                // it is an update
                Log.d(getClass().toString(), "Passed a GridView");
                update = true;
            } else if (params[0] instanceof ViewPager) {
                // it is an update
                Log.d(getClass().toString(), "Passed a ViewPager");
                update = true;
            } else {
                Log.e(getClass().toString(), params[0] + " is not a Searcher target.");
                return null;
            }
        } else {
            update = false;
        }
        Log.d(getClass().toString(),"Update is " + update);
        Log.d(getClass().toString(),"ForceUpdate is " + forceupdate);
        Document d = null;
        try {
            Log.d(getClass().toString(), "Getting document " + sq.toPath()
                    + "&page=" + sq.whichPage + "&inline_set="
                    + (sq.searchMode == Constants.SEARCH_WITH_LIST_VIEW ?
                    "dm_l" : "dm_t"));
            // possible: inline_set is custom settings
            Connection.Response r = Jsoup.connect(Constants.CURRENT_ACCOUNT.siteRoot + sq.toPath()
                    + "&page=" + sq.whichPage + "&inline_set="
                    + (sq.searchMode == Constants.SEARCH_WITH_LIST_VIEW ?
                    "dm_l" : "dm_t"))
                    .cookies(Constants.CURRENT_ACCOUNT.cookie).execute();
            Constants.CURRENT_ACCOUNT.cookie = (java.util.HashMap<String, String>) r.cookies();
            d = r.parse();
            Log.d(getClass().toString(), "Got response " + r);
            // TODO: check if new cookie is written back

        } catch (IOException e) {
            e.printStackTrace();
        }
        if (d == null) {
            // something is wrong so return null
            return null;
        }
        Log.i(getClass().toString(), "Got search document");
        sr.query = sq;
        sr.totalResult = Integer.parseInt(d.select("body > div.ido > div:nth-child(2) > p").text()
                .split("of ")[1].replace(",", "")); // remove commas
        Log.d(getClass().toString(), "Total number of results is " + sr.totalResult);
        assert sq.whichPage <= sr.totalResult / Constants.RESULT_ITEMS_PER_PAGE;
        // make sure we don't move past the end
        Log.d(getClass().toString(), "Page number is " + sq.whichPage);
        Log.d(getClass().toString(), "Display mode is " + sq.searchMode);
        switch (sq.searchMode) {
            case Constants.SEARCH_WITH_LIST_VIEW:
                Elements table = d.select("table.itg tr");
                int j = 0;
                for (Element tr : table) {
                    if (!tr.className().startsWith("gtr")) {
                        continue;
                    }
                    String link = tr.select("div.it5 > a").attr("href");
                    String id = link.replaceAll(".*/(.*?)/.*?/", "$1");
                    String token = link.replaceAll(".*/(.*?)/", "$1");
                    // regex madness!
                    String title = tr.select("div.it5 a").text();
                    // preloading thumb, regex to get the actual url
                    String thumb = tr.select("div.it2 img").attr("src");
                    if (thumb == "") {
                        // thumb preloading is off so we have to crack the thumb ourselves
                        thumb = tr.select("div.it2").text().replaceAll(".*?~(.*?)~(.*?)~.*", "http://$1/$2");
                        Log.d(getClass().toString(), "Result " + (sq.whichPage * Constants.RESULT_ITEMS_PER_PAGE + j)
                                + "has thumb preloading off, actual thumb is " + thumb);
                    }
                    String type = tr.select("td.itdc img").attr("alt");
                    String uploader = tr.select("td.itu a").text();
                    String uploaded = tr.select("td.itd").first().text().replaceAll("(.*?) init.*", "$1");
                    String r = tr.select("div.it4 > div").attr("style");
                    int rx = Integer.parseInt(r.replaceAll(".*?:(.*?)px.*", "$1"));
                    int ry = Integer.parseInt(r.replaceAll(".*?px (.*?)px.*", "$1"));
                    float rating = 0.0f;
                    switch (rx) {
                        case 0:
                            rating = 5;
                            break;
                        case -16:
                            rating = 4;
                            break;
                        case -32:
                            rating = 3;
                            break;
                        case -48:
                            rating = 2;
                            break;
                        case -64:
                            rating = 1;
                            break;
                    }
                    assert rating != 0.0f; // there should be no zero rating
                    if (ry == -21) {
                        rating -= 0.5;
                    } else {
                        assert ry == -1; // the only 2 possible values for ry
                    }
                    Gallery g = new Gallery(id, token, title, thumb, type, uploader, uploaded, rating);
                    Log.d(getClass().toString(), "Result " + (sq.whichPage * Constants.RESULT_ITEMS_PER_PAGE + j) + " is "
                            + g.toString());
                    // so that the search result will keep appending to this hashmap
                    sr.result.put(sq.whichPage * Constants.RESULT_ITEMS_PER_PAGE + j, g);
                    j += 1;
                }
                sr.lastitem = (sq.whichPage * Constants.RESULT_ITEMS_PER_PAGE) + j;
                assert j == Constants.RESULT_ITEMS_PER_PAGE;
                break;
            case Constants.SEARCH_WITH_THUMB_VIEW:
                Elements thumbs = d.select("div.id1");
                j = 0;
                for (Element t : thumbs) {
                    if (j >= Constants.RESULT_ITEMS_PER_PAGE) break;
                    String id = t.select("div.id2 > a").attr("href").replaceAll(".*/(.*?)/.*?/", "$1");
                    String token = t.select("div.id2 > a").attr("href").replaceAll(".*/(.*?)/", "$1");
                    String title = t.select("div.id2 > a").text();
                    String thumb = t.select("div.id3 > a > img").attr("src");
                    String type = t.select("div.id41").attr("title").toLowerCase();
                    if (type.equals("image sets")) type = "imageset"; // english to program speak
                    if (type.equals("artist cg sets")) type = "artistcg";
                    if (type.equals("game cg sets")) type = "gamecg";
                    if (type.equals("asian porn")) type = "asianporn";

                    Log.d(getClass().toString(), "Type is " + type);
                    int pages = Integer.parseInt(t.select("div.id42").text().replaceAll("(.*?) file.*", "$1"));
                    String r = t.select("div.id43").attr("style");
                    int rx = Integer.parseInt(r.replaceAll(".*?:(.*?)px.*", "$1"));
                    int ry = Integer.parseInt(r.replaceAll(".*?px (.*?)px.*", "$1"));
                    float rating = 0.0f;
                    switch (rx) {
                        case 0:
                            rating = 5;
                            break;
                        case -16:
                            rating = 4;
                            break;
                        case -32:
                            rating = 3;
                            break;
                        case -48:
                            rating = 2;
                            break;
                        case -64:
                            rating = 1;
                            break;
                    }
                    assert rating != 0.0f; // there should be no zero rating
                    if (ry == -21) {
                        rating -= 0.5;
                    } else {
                        assert ry == -1; // the only 2 possible values for ry
                    }
                    Gallery g = new Gallery(id, token, title, thumb, type, pages, rating);
                    Log.d(getClass().toString(), "Result " + (sq.whichPage * Constants.RESULT_ITEMS_PER_PAGE + j) + " is "
                            + g.toString());
                    // so that the search result will keep appending to this hashmap
                    sr.result.put(sq.whichPage * Constants.RESULT_ITEMS_PER_PAGE + j, g);
                    j += 1;
                }
                sr.lastitem = (sq.whichPage * Constants.RESULT_ITEMS_PER_PAGE) + j - 1;
                break;
        }


        return sr;
    }

    @Override
    protected void onPostExecute(SearchResult sr) {
        Log.d(getClass().toString(), "onPostExecute called");
        sa.onSearchComplete(sr, update);
    }

}
