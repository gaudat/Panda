package name.gaudat.panda;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.FragmentManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import name.gaudat.panda.data.Constants;
import name.gaudat.panda.data.Gallery;
import name.gaudat.panda.data.Page;
import name.gaudat.panda.data.Tag;

import java.util.ArrayList;


public class ViewerActivity extends ActionBarActivity implements PagerFragment.OnFragmentInteractionListener {
    private boolean paneExpanded;

    public ViewerActivity() {
    }

    public ViewerActivity(BroadcastReceiver notificationClicked) {
        NotificationClicked = notificationClicked;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_pager);
        setContentView(R.layout.fragment_gallery_detail);
        ((SlidingUpPanelLayout) findViewById(R.id.slidingpane)).hidePanel();
        if (savedInstanceState == null) {
            if (getIntent().getParcelableExtra("gallery") == null) {
                // not passing a gallery, don't display overview page
                ArrayList<Page> pages = getIntent().getParcelableArrayListExtra("pages");
                Log.i(getClass().toString(), "Created through ArrayList of page");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, PagerFragment.newInstance(
                                pages, 0))
                        .commit();
            } else {
                // passing a gallery
                Log.i(getClass().toString(), "Created through gallery");
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.container, PagerFragment.newInstance(
                                ((Gallery) getIntent().getParcelableExtra("gallery")), 0))
                        .commit();
            }
        }
        Log.d(getClass().toString(),"Created ViewPager");
        imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        DisplayImageOptions o = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
        ImageLoaderConfiguration c = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(o).build();
        ImageLoader.getInstance().init(c);
    }

    public void setGalleryDetails(Gallery g) {
        TextView t = (TextView) findViewById(R.id.title);
        assert t != null;
        t.setText(g.title);
        t = (TextView) findViewById(R.id.titlealt);
        t.setText(g.title2);
        t = (TextView) findViewById(R.id.galleryuploader);
        t.setText(g.language + " " + g.uploaded + "@" + g.pages.size());
        RatingBar r = (RatingBar) findViewById(R.id.galleryrating);
        r.setRating(g.rating);
        t = (TextView) findViewById(R.id.galleryratingtext);
        t.setText(((Float) g.rating).toString() + " (" + ((Integer) g.raters).toString() + ") ");
        ImageView i = (ImageView) findViewById(R.id.gallerycolor);
        i.setBackgroundColor(getResources().getColor(Constants.COLORMAP.get(g.type)));
        new TagListFiller(g.tags).execute();
        ((SlidingUpPanelLayout) findViewById(R.id.slidingpane)).hidePanel();
        paneExpanded = false;

    }


    public class TagListFiller extends AsyncTask<Void, Void, String> {

        ArrayList<Tag> tags;

        public TagListFiller(ArrayList<Tag> tags) {
            this.tags = tags;
        }

        @Override
        protected String doInBackground(Void... params) {
            String s = "";
            s += "<style>" +
                    "div.gt{" +
                    "float:left;" +
                    "font-weight:bold;" +
                    "padding:0px 3px;" +
                    "margin:0 2px 4px 2px;" +
                    "white-space:nowrap;" +
                    "position:relative;" +
                    "border-radius:5px;" +
                    "border:1px solid #989898;" +
                    //"background:#4f535b" +
                    "}" +
                    "div.gtl{" +
                    "float:left;" +
                    "font-weight:bold;" +
                    "padding:0px 3px;" +
                    "margin:0 2px 4px 2px;" +
                    "white-space:nowrap;" +
                    "position:relative;" +
                    "border-radius:5px;" +
                    "border:1px dashed #8c8c8c;" +
                    //"background:#4f535b" +
                    "}" +
                    "</style>";
            Log.d(getClass().toString(), "Appended styles");
            for (Tag t : tags) {
                s += "<a class=\"" + t.attr + "href=\"name.gaudat.panda://search/" +
                        t.namespace + ":" + t.name +
                        "\">" + t.namespace + ":" + t.name +

                        "</a>";
                Log.d(getClass().toString(), "Filled " + t.name);
            }
            Log.i(getClass().toString(), "Filled tags");
            return s;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TextView t = (TextView) findViewById(R.id.taglist);
            t.setText(Html.fromHtml(s));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pager, menu);
        // If we can make it split option bar
        // If it is gallery also add "Download Gallery"
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_search_image:
                // TODO make image search
                return true;
            case R.id.action_download_image:
                dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Request r = new DownloadManager.Request(Uri.parse(currentPage.img))
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "Panda/" + currentPage.gtitle
                                + "-" + currentPage.pn + 1 + "-" + currentPage.fn)
                        .setTitle(currentPage.fn)
                        .setDescription("Download from Panda");
                r.allowScanningByMediaScanner();
                dm.enqueue(r);
                // download images to pictures/panda, keeping notification
                return true;
            case R.id.action_download_gallery:
                //TODO download the WHOLE gallery
                // maybe make a toast telling the user
                // the download manager will be flooded
                return true;
            case R.id.action_update_image:
                //TODO update image, call for nl
                return true;
            case R.id.action_gallery_details:
                if (!paneExpanded) {
                    ((SlidingUpPanelLayout) findViewById(R.id.slidingpane)).expandPanel();
                    paneExpanded = true;
                } else {
                    ((SlidingUpPanelLayout) findViewById(R.id.slidingpane)).hidePanel();
                    paneExpanded = false;
                }
                return true;
            case R.id.action_image_details:
                // TODO: popup for image details
                return true;
            case R.id.action_settings:
                // TODO: start settings activity
                return true;
            case R.id.action_view_gallery:
                // TODO: start another activity using this page's gallery
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toFirstPage(View v) {
        Log.d(getClass().toString(), "toFirstPage called");
        ((ViewPager) findViewById(R.id.pager)).setCurrentItem(0);
    }

    public void toPrevPage(View v) {
        int cp = ((ViewPager) findViewById(R.id.pager)).getCurrentItem();
        Log.d(getClass().toString(), "toPrevPage called, page is " + cp);
        if (cp == 0) {
            Log.i(getClass().toString(), "Not doing anything because it is the first page");
            return;
        }
        ((ViewPager) findViewById(R.id.pager)).setCurrentItem(cp - 1);
    }

    InputMethodManager imm;


    public void changePage(View v) {
        Log.d(getClass().toString(), "changePage called");
        final TextView tv = (TextView) v;
        final EditText et = (EditText) findViewById(R.id.pagechange);
        et.setHint("(1-" + ((ViewPager) findViewById(R.id.pager)).getAdapter().getCount() + ")...");
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                try {
                    int pn = Integer.parseInt(v.getText().toString());
                    Log.d(getClass().toString(), "Enter pressed, page number is " + pn);
                    if (pn < 1 || pn > ((ViewPager) findViewById(R.id.pager)).getAdapter().getCount() - 1) {
                        Log.w(getClass().toString(),"pn out of range");
                        Toast.makeText(getApplicationContext(),"Invalid page number!",Toast.LENGTH_SHORT).show();
                        et.setText("");
                        return true;
                    } else {
                        ((ViewPager) findViewById(R.id.pager)).setCurrentItem(pn - 1);
                        Log.i(getClass().toString(),"Changed page to "  + (pn - 1));
                        imm.hideSoftInputFromWindow(et.getWindowToken(),0);
                        et.setVisibility(View.GONE);
                        tv.setVisibility(View.VISIBLE);
                        return true;
                    }
                } catch (NumberFormatException e) {
                    // the user put shit in the text box
                    Log.d(getClass().toString(),"Nothing is input, nothing is done");
                    imm.hideSoftInputFromWindow(et.getWindowToken(),0);
                    et.setVisibility(View.GONE);
                    tv.setVisibility(View.VISIBLE);
                    return true;
                }

            }
        });
        et.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(getClass().toString(), "hasFocus is " + hasFocus);
                if (!hasFocus) {
                    // return the view to page number
                    Log.d(getClass().toString(), "Blurred, hiding editText");
                    imm.hideSoftInputFromWindow(et.getWindowToken(),0);
                    et.setVisibility(View.GONE);
                    tv.setVisibility(View.VISIBLE);
                    Log.d(getClass().toString(), "tv is visible, et is gone");
                } else {
                    tv.setVisibility(View.GONE);
                    et.setVisibility(View.VISIBLE);
                    imm.showSoftInput(et, 0);
                    Log.d(getClass().toString(),"tv is gone, et is visible");

                }
            }
        });
        tv.setVisibility(View.GONE);
        et.setVisibility(View.VISIBLE);
        imm.showSoftInput(et, 0);
        Log.d(getClass().toString(),"tv is gone, et is visible");
        et.requestFocus();
    }

    public void toNextPage(View v) {
        int cp = ((ViewPager) findViewById(R.id.pager)).getCurrentItem();
        Log.d(getClass().toString(), "toNextPage called, page is " + cp);
        if (cp == ((ViewPager) findViewById(R.id.pager)).getAdapter().getCount() - 1) {
            Log.i(getClass().toString(), "Not doing anything because it is the last page");
            return;
        }
        ((ViewPager) findViewById(R.id.pager)).setCurrentItem(cp + 1);
    }

    public void toLastPage(View v) {
        Log.d(getClass().toString(), "toLastPage called");
        int lp = ((ViewPager) findViewById(R.id.pager)).getAdapter().getCount() - 1;
        ((ViewPager) findViewById(R.id.pager)).setCurrentItem(lp);
    }

    DownloadManager dm;
    Page currentPage;
    private BroadcastReceiver NotificationClicked;

    @Override
    public void onFragmentInteraction(Page p) {
        currentPage = p;
        setTitle(p.gtitle);
        ViewPager vp = (ViewPager) findViewById(R.id.pager);
        ((TextView) findViewById(R.id.pagedetail)).setText(
                "Page " + (vp.getCurrentItem()+1) + " of " + vp.getAdapter().getCount());
    }


}
