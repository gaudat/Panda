package name.gaudat.panda;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import name.gaudat.panda.data.Account;
import name.gaudat.panda.data.Constants;
import name.gaudat.panda.data.Gallery;
import name.gaudat.panda.data.Page;
import name.gaudat.panda.worker.AccountLoginer;
import name.gaudat.panda.worker.GalleryUpdater;
import name.gaudat.panda.worker.PageUpdater;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


public class MainActivity extends ActionBarActivity {

    // App-wide details are defined here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up ImageLoader
        DisplayImageOptions o = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
        ImageLoaderConfiguration c = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(o).build();
        ImageLoader.getInstance().init(c);

        // if the activity is invoked by a uri intent
        String url = getIntent().getDataString();

        //try {
        //    getPage(findViewById(R.id.ll));
        //} catch (ExecutionException | InterruptedException e) {
        //    e.printStackTrace();
        //}
        Constants.applicationContext = this;
        try {
            Constants.CURRENT_ACCOUNT = new AccountLoginer(new Account()).execute(Constants.GUEST_ACC, Constants.GUEST_PW).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        if (url == null) {
            // nothing is passed from the intent...
            // probably the app is started from launcher
            startActivity(new Intent(this, SearchActivity.class));
        } else {
            ((EditText)findViewById(R.id.url)).setText(url);
            try {
                getPage(findViewById(R.id.url));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    Gallery g;
    Page p;


    public void getPage(View view) throws ExecutionException, InterruptedException {
        String url = ((EditText) findViewById(R.id.url)).getText().toString();
        TextView t = (TextView) findViewById(R.id.textView);
        if (url.contains("exhentai.org")) {
            Constants.CURRENT_ACCOUNT = new AccountLoginer(new Account()).execute(Constants.TEST_ACC, Constants.TEST_PW).get();
        } else {
            Constants.CURRENT_ACCOUNT = new AccountLoginer(new Account()).execute(Constants.GUEST_ACC, Constants.GUEST_PW).get();
        }
        if (Constants.CURRENT_ACCOUNT == null) {
            Toast.makeText(this, R.string.network_error, Toast.LENGTH_SHORT).show();
            return;// panda
        }
        if (url.contains("/g/")) {
            // gallery
            String gid = url.split("/g/")[1].split("/")[0];
            String gtoken = url.split("/g/")[1].split("/")[1];
            g = new Gallery(gid, gtoken);
            g = new GalleryUpdater(g.getUpdateRequest(0)).execute().get();
            if (g == null) {
                Toast.makeText(getApplication(), R.string.parse_error, Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(this, ViewerActivity.class);
            intent.putExtra("gallery", g);
            startActivity(intent);
        } else if (url.contains("/s/")) {
            // page
            String ptoken = url.split("/s/")[1].split("/")[0];
            String gid = url.split("/s/")[1].split("/")[1].split("-")[0];
            int pn = Integer.parseInt(url.split("/s/")[1].split("/")[1].split("-")[1]) - 1;
            p = new Page(pn, gid, ptoken);
            p = new PageUpdater(p.getUpdateRequest()).execute().get();
            t.setText(p.toString());
            // Try to load the image
            Intent intent = new Intent(this, ViewerActivity.class);
            ArrayList<Page> pages = new ArrayList<>();
            pages.add(p);
            intent.putExtra("pages", pages);
            startActivity(intent);
        } else {
            Log.w("getPage", url + " is not a valid panda url");
        }
    }
}


