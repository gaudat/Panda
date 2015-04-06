package name.gaudat.panda.worker;

import android.app.DownloadManager;
import android.content.Context;
import android.os.AsyncTask;
import name.gaudat.panda.data.Constants;
import name.gaudat.panda.data.Gallery;

/**
 * Created by anon on 11/1/2015.
 */
public class GalleryDownloader extends AsyncTask<Gallery.DownloadRequest, Void, Void> {
    // Downloads a gallery with account
    // with options

    Gallery.DownloadRequest g;
    DownloadManager dm;

    // constructor with options
    public GalleryDownloader() {
        this.dm = (DownloadManager) Constants.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    // constructor with dm
    // may not be of use
    public GalleryDownloader(DownloadManager dm) {
        this.dm = dm;
    }

    @Override
    protected Void doInBackground(Gallery.DownloadRequest... params) {
        g = params[0];
        // start and end are inclusive
        for (int i = g.start; i <= g.end; i++) {
            new PageDownloader(g.g.pages.get(i).getDownloadRequest(), dm).execute();
        }
        return null;
    }


}
