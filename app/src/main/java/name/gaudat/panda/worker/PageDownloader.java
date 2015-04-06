package name.gaudat.panda.worker;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import name.gaudat.panda.data.Constants;
import name.gaudat.panda.data.Page;

/**
 * Created by anon on 11/1/2015.
 */
public class PageDownloader extends AsyncTask<Void, Void, Void> {
    // Downloads the image referred by a page
    // with options (where to download to)

    Page.DownloadRequest p;
    DownloadManager dm;

    @Override
    protected Void doInBackground(Void... params) {
        // If we are only getting one page
        // We can afford making a dm for every page
        if (dm == null) {
            dm = (DownloadManager) Constants.applicationContext.getSystemService(Context.DOWNLOAD_SERVICE);
        }

        DownloadManager.Request dr = new DownloadManager.Request(Uri.parse(p.img))
                .setDestinationUri(Uri.parse(p.dest));
        dr.allowScanningByMediaScanner();
        dm.enqueue(dr);
        // TODO: make the notification more beautiful

        return null;
    }

    // for downloading individual pages
    public PageDownloader(Page.DownloadRequest p) {
        this.p = p;
    }

    // Use this in GalleryDownloader
    public PageDownloader(Page.DownloadRequest p, DownloadManager dm) {
        // also pass DownloadManager in
        // can help when getting a lot of images
        this.p = p;
        this.dm = dm;
    }
}
