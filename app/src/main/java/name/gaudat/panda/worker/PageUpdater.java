package name.gaudat.panda.worker;

import android.os.AsyncTask;
import android.util.Log;
import name.gaudat.panda.data.Constants;
import name.gaudat.panda.data.Page;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

/**
 * Created by anon on 11/1/2015.
 */
public class PageUpdater extends AsyncTask<Void, Void, Page> {
    // Update a page by visiting its uri and fill up any info
    // no options
    Page.UpdateRequest ur;

    public PageUpdater(Page.UpdateRequest ur) {
        this.ur = ur;
    }


    @Override
    protected Page doInBackground(Void... params) {
        Log.d(getClass().toString(), "Clicked is " + ur.p.clicked);

        // update normal way
        if (!ur.p.clicked) {
            Log.d(getClass().toString(), "Populating page");
            // updatePageDetails(Page)
            Document d = null;
            try {
                Log.d(getClass().toString(), "Getting document " + ur.p.toPath());
                Connection.Response r = Jsoup.connect(Constants.CURRENT_ACCOUNT.siteRoot + ur.p.toPath()).cookies(Constants.CURRENT_ACCOUNT.cookie).execute();
                Constants.CURRENT_ACCOUNT.cookie = (java.util.HashMap<String, String>) r.cookies();
                d = r.parse();
                Log.d(getClass().toString(), "Got response " + r);
                // TODO: check if new cookie is written back

            } catch (IOException e) {
                e.printStackTrace();
            }
            // Parse image page
            if (d == null) return null;
            String details = d.select("#i2 div").get(2).text();
            Log.d(getClass().toString(), "Got page details");
            ur.p.gtitle = d.select("h1").text();
            ur.p.gtoken = d.select("#i5 .sb a").attr("href").split("/")[5];
            ur.p.fn = details.split(" :: ")[0];
            ur.p.firstp = d.select("#i2 .sn a").get(0).attr("onclick").split("'")[1];
            ur.p.prevp = d.select("#i2 .sn a").get(0).attr("onclick").split("'")[1];
            ur.p.nextp = d.select("#i2 .sn a").get(0).attr("onclick").split("'")[1];
            ur.p.lastp = d.select("#i2 .sn a").get(0).attr("onclick").split("'")[1];
            ur.p.width = Integer.parseInt(details.split(" :: ")[1].split(" x ")[0]);
            ur.p.height = Integer.parseInt(details.split(" :: ")[1].split(" x ")[1]);
            ur.p.size = details.split(" :: ")[2];
            ur.p.img = d.select("#img").attr("src");
            ur.p.nl = d.select("#i6 a").last().attr("onclick").split("nl\\(")[1].split("\\)")[0];
            ur.p.hash = d.select("#i6 a").first().attr("href").split("f_shash=")[1].split("&fs_from")[0];
            ur.p.clicked = true;
            return ur.p;
        } else if (ur.p.nl != null) {
            // only update big image
            Log.d(getClass().toString(), "Updating nl");
            Document d = null;
            try {
                Log.d(getClass().toString(), "Getting document " + ur.p.toPath());
                Connection.Response r = Jsoup.connect(Constants.CURRENT_ACCOUNT.siteRoot + ur.p.toPath() + "/?nl=" + ur.p.nl).cookies(Constants.CURRENT_ACCOUNT.cookie).execute();
                Constants.CURRENT_ACCOUNT.cookie = (java.util.HashMap<String, String>) r.cookies();
                d = r.parse();
                Log.d(getClass().toString(), "Got response " + r);
                // TODO: check if new cookie is written back

            } catch (IOException e) {
                e.printStackTrace();
            }
            if (d == null) return null;
            ur.p.img = d.select("#img").attr("src");
            ur.p.nl = d.select("#i6 a").last().attr("onclick").split("nl\\(")[1].split("\\)")[0];
            return ur.p;
        }
        // something is wrong so return null
        return null;
    }
}
