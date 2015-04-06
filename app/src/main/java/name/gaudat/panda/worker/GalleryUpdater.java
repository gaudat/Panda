package name.gaudat.panda.worker;

import android.os.AsyncTask;
import android.util.Log;
import name.gaudat.panda.data.Constants;
import name.gaudat.panda.data.Gallery;
import name.gaudat.panda.data.Page;
import name.gaudat.panda.data.Tag;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by anon on 11/1/2015.
 */
public class GalleryUpdater extends AsyncTask<Void, Void, Gallery> {
    // Update a gallery by visiting its uri and fill up any info
    // also try to fill its pages by the info from overview page
    // with options

    Gallery.UpdateRequest ur;

    private String[][] jsoupToTable(Elements table) {
        Elements trs = table.select("tr");
        String[][] trtd = new String[trs.size()][];
        for (int i = 0; i < trs.size(); i++) {
            Elements tds = trs.get(i).select("td");
            trtd[i] = new String[tds.size()];
            for (int j = 0; j < tds.size(); j++) {
                trtd[i][j] = tds.get(j).text();
            }
        }
        return trtd;
    }

    public GalleryUpdater(Gallery.UpdateRequest ur) {
        this.ur = ur;
    }

    @Override
    protected Gallery doInBackground(Void... params) {
        Log.d(getClass().toString(), "Started job");

        // snap to page boundaries
        int startp;
        int endp;
        if (ur.start == -1) {
            // all pages
            startp = 0;
        } else {
            // snap to start of page
            startp = ur.start / Constants.IMG_PER_PAGE;
        }

        Document d = null;
        try {
            Log.d(getClass().toString(), "Getting document " + ur.g.getPath());
            Connection.Response r = Jsoup.connect(Constants.CURRENT_ACCOUNT.siteRoot + ur.g.getPath() + "/?p=" + startp).cookies(Constants.CURRENT_ACCOUNT.cookie).execute();
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
        Log.d(getClass().toString(), "Got gallery document");

        if (ur.g.title2 == null) {
            ur.g.title = d.select("#gn").text();
            if (ur.g.title.equals("")) {
                Log.e(getClass().toString(), "Something is wrong with the link!");
                return null;
            }
            ur.g.title2 = d.select("#gj").text();
            ur.g.thumb = d.select("#gd1 img").attr("src");
            ur.g.type = Constants.TYPEMAP.get(d.select("#gdc img").attr("alt"));
            ur.g.uploader = d.select("#gdn a").text().trim();
            String[][] table = jsoupToTable(d.select("#gdd table"));
            ur.g.uploaded = table[0][1];
            ur.g.language = table[5][1];
            ur.g.raters = Integer.parseInt(d.select("#rating_count").text());
            ur.g.rating = Float.parseFloat(d.select("#rating_label").text().split(":")[1]);
            Log.d(getClass().toString(), "Got basic metadata");
            ur.g.tags = new ArrayList<>();
            Log.d(getClass().toString(), "Gallery has " + Integer.parseInt(table[1][1].split(" @")[0]) + " pages");
            ur.g.pages = new ArrayList<>(Integer.parseInt(table[1][1].split(" @")[0]));
            for (int i = 0; i < Integer.parseInt(table[1][1].split(" @")[0]); i++) {
                ur.g.pages.add(null);
            }
            // ugly hack
            // we fill the array list with nulls first so it shows the correct size

            // Parse tags
            Elements tagTable = d.select("#taglist table td > div");
            for (int i = 0; i < tagTable.size(); i++) {
                Tag temp = new Tag();
                if (tagTable.get(i).id().replace("td_", "").contains(":")) {
                    temp.namespace = tagTable.get(i).id().replace("td_", "").split(":")[0];
                    temp.name = tagTable.get(i).id().replace("td_", "").split(":")[1];
                } else {
                    temp.name = tagTable.get(i).id().replace("td_", "");
                }
                temp.attr = tagTable.get(i).className();
                ur.g.tags.add(temp);
            }
            Log.d(getClass().toString(), "Got tags");
        }
        // if gallery is clicked we only update the thumbnails
        // Parse thumbnails
        if (ur.end == -1) {
            // All pages
            endp = Integer.parseInt(d.select(".ip").text().split(" of ")[1].split(" im")[0]) - 1;
        } else {
            // Snap to end of page
            endp = ur.end / Constants.IMG_PER_PAGE;
        }

        Log.d(getClass().toString(), "Thumbnails start is " + startp + ", end is " + endp);
        for (int i = startp; i <= endp; i++) {
            if (i != startp) {
                // skip the first time
                try {
                    Log.d(getClass().toString(), "Getting document " + ur.g.getPath());
                    Connection.Response r = Jsoup.connect(Constants.CURRENT_ACCOUNT.siteRoot
                            + ur.g.getPath() + "/?p=" + i).cookies(Constants.CURRENT_ACCOUNT.cookie).execute();
                    Constants.CURRENT_ACCOUNT.cookie = (java.util.HashMap<String, String>) r.cookies();
                    d = r.parse();
                    Log.d(getClass().toString(), "Got response " + r);
                    // TODO: check if new cookie is written back

                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(getClass().toString(), "Got page " + i);
            }
            Elements thumbs = d.select("#gdt .gdtm");
            Log.d(getClass().toString(), "Got thumbnails of page " + i + " total " + thumbs.size() + " images");
            for (int j = 0; j < thumbs.size(); j++) {
                Element t = thumbs.get(j);
                int pn = i * Constants.IMG_PER_PAGE + j;
                assert pn == Integer.parseInt(t.select("img").attr("alt")) - 1;

                // IMPORTANT: thumb is 20 pages stitched together
                String thumb = t.select("div").get(1).attr("style").split(" url\\(")[1].split("\\) ")[0];
                int twidth = Integer.parseInt(t.select("div img").get(0).attr("style").split("width:")[1].split("px")[0]);
                int theight = Integer.parseInt(t.select("div img").get(0).attr("style").split("height:")[1].split("px")[0]);
                String[] temp = t.select("div").get(1).attr("style").split("\\) -");
                int thoffset = 0;
                if (temp.length == 1) {
                    thoffset = 0;
                } else {
                    thoffset = Integer.parseInt(t.select("div").get(1).attr("style").split("\\) -")[1].split("px")[0]);
                }
                String ptoken = t.select("a").attr("href").split("/")[4];
                //TODO change page title based on sharedPrefs
                // pn is zero based
                ur.g.pages.set(pn, new Page(
                        pn, ur.g.id, ur.g.title, thumb, ptoken, twidth, theight, thoffset
                ));

            }
        }
        Log.d(getClass().toString(), "Got thumbnails");


        return ur.g;
    }
}
