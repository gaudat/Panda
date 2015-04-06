package name.gaudat.panda;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import name.gaudat.panda.data.Constants;
import name.gaudat.panda.data.Gallery;
import name.gaudat.panda.data.SearchQuery;
import name.gaudat.panda.data.SearchResult;
import name.gaudat.panda.worker.GallerySearcher;


public class SearchActivity extends ActionBarActivity
        implements SearchDrawerFragment.SearchDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private SearchDrawerFragment mSearchDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    // The activity itself, used in AsyncTasks
    private static SearchActivity sa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        if (savedInstanceState == null) {
            // See if a gallery is passed
            sq = getIntent().getParcelableExtra("query");
            if (sq == null) {
                sq = new SearchQuery();
                Log.i(getClass().toString(), "Created through nothing");
            }
            Log.i(getClass().toString(), "Created through query");
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.navigation_drawer, SearchDrawerFragment.newInstance(sq))
                    .commit();
        }


        mSearchDrawerFragment = (SearchDrawerFragment)
                getSupportFragmentManager().findFragmentById(
                        R.id.navigation_drawer);
        mTitle = getTitle();

        // Fuck it, we try to make the result show up first
        //DisplayImageOptions o = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true).build();
        //ImageLoaderConfiguration c = new ImageLoaderConfiguration.Builder(this).defaultDisplayImageOptions(o).build();
        //ImageLoader.getInstance().init(c);

        sa = this;
        // Until we implement shared pref
        viewMode = Constants.SHOW_AS_LIST;
        sq = new SearchQuery();
        setViewMode(viewMode);
    }

    public void setViewMode(int vm) {
        if (vm == Constants.SHOW_AS_LIST) {
            // search with list view
            sq.searchMode = Constants.SEARCH_WITH_LIST_VIEW;
            new GallerySearcher(sq, this, false).execute(findViewById(R.id.container));
        } else {
            // search with thumb view
            sq.searchMode = Constants.SEARCH_WITH_THUMB_VIEW;
            new GallerySearcher(sq, this, false).execute(findViewById(R.id.container));
        }
        this.viewMode = vm;
    }

    SearchQuery sq = null;
    int viewMode;

    public void onSearchComplete(final SearchResult sr, final boolean isUpdate) {
        Log.d(getClass().toString(), "onSearchComplete called");
        Log.d(getClass().toString(), "SearchResult is " + sr.toString());
        Log.d(getClass().toString(), "isUpdate is " + isUpdate);
        ListView lv = (ListView) findViewById(R.id.result_list);
        GridView gv = (GridView) findViewById(R.id.result_grid);
        ViewPager vp = (ViewPager) findViewById(R.id.result_pager);
        if (!isUpdate) {
            switch (viewMode) {
                case Constants.SHOW_AS_LIST:
                    // use list view
                    lv.setVisibility(View.VISIBLE);
                    gv.setVisibility(View.GONE);
                    vp.setVisibility(View.GONE);
                    lv.setAdapter(new ResultListAdapter(sr));
                    break;
                case Constants.SHOW_AS_GRID:
                    // use grid view
                    lv.setVisibility(View.GONE);
                    gv.setVisibility(View.VISIBLE);
                    vp.setVisibility(View.GONE);
                    gv.setAdapter(new ResultGridAdapter(sr));
                    break;
                case Constants.SHOW_AS_PAGER:
                    // tinder style
                    // remember that gif?
                    lv.setVisibility(View.GONE);
                    gv.setVisibility(View.GONE);
                    vp.setVisibility(View.VISIBLE);
                    vp.setAdapter(new ResultPagerAdapter(sr));
                    break;
            }
        } else {
            switch (viewMode) {
                case Constants.SHOW_AS_LIST:
                    // use list view
                    HeaderViewListAdapter adapter = (HeaderViewListAdapter) lv.getAdapter();
                    ((ResultListAdapter) adapter.getWrappedAdapter()).sr = sr;
                    int oldCount = ((ResultListAdapter) adapter.getWrappedAdapter()).count;
                    ((ResultListAdapter) adapter.getWrappedAdapter()).updateCount();
                    assert ((ResultListAdapter) adapter.getWrappedAdapter()).count != oldCount;
                    lv.removeFooterView(((ResultListAdapter) adapter.getWrappedAdapter()).message);
                    break;
                case Constants.SHOW_AS_GRID:
                    // use grid view
                    ((ResultGridAdapter) gv.getAdapter()).updating = false;
                    oldCount = ((ResultGridAdapter) gv.getAdapter()).count;
                    ((ResultGridAdapter) gv.getAdapter()).updateCount();
                    assert ((ResultGridAdapter) gv.getAdapter()).count != oldCount;
                    break;
                case Constants.SHOW_AS_PAGER:
                    // tinder style
                    // remember that gif?
                    ((ResultPagerAdapter) vp.getAdapter()).updating = false;
                    oldCount = ((ResultPagerAdapter) vp.getAdapter()).count;
                    ((ResultPagerAdapter) vp.getAdapter()).updateCount();
                    assert ((ResultPagerAdapter) vp.getAdapter()).count != oldCount;
                    break;
            }
        }
    }

    private class ResultListAdapter extends BaseAdapter {
        // we assume the users always start at the first page
        // don't need to implement "going back"
        public SearchResult sr;
        public int count; // count
        TextView message; // footer view

        public ResultListAdapter(SearchResult sr) {
            this.sr = sr;
            this.count = sr.result.size();
        }

        public boolean updateCount() {
            Log.d(getClass().toString(), "Count was " + count);
            if (count == sr.totalResult) {
                return true;
            } else {
                count = Math.min(count + Constants.RESULT_ITEMS_PER_PAGE, sr.totalResult);
                Log.d(getClass().toString(), "Count is " + count);
                notifyDataSetChanged();
                return isEnd();
            }
        }

        public void showMore() {
            sr.query.whichPage++;
            new GallerySearcher(sr, sa).execute(findViewById(R.id.result_list));
        }

        public boolean isEnd() {
            return count == sr.totalResult;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object getItem(int position) {
            return sr.result.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View v, ViewGroup parent) {
            Log.d(getClass().toString(), "getView called, position = " + position);
            if (v == null) {
                // inflate new view
                Log.d(getClass().toString(), "Need to make new view");
                v = getLayoutInflater().inflate(R.layout.fragment_result_list, null);
            }
            Gallery g = sr.result.get(position);
            ((TextView) v.findViewById(R.id.listtitle)).setText(g.title);
            ((TextView) v.findViewById(R.id.listdetail)).setText(
                    "(" + (position + 1) + ") " + g.uploaded + " " + g.uploader
            );
            v.findViewById(R.id.listtype).setBackgroundResource(
                    Constants.COLORMAP.get(g.type));
            ((RatingBar) v.findViewById(R.id.listrating)).setRating(g.rating);
            ImageLoader.getInstance().displayImage(g.thumb, (ImageView) v.findViewById(R.id.listthumb),
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            ((ImageView) view).setImageBitmap(loadedImage);
                            Log.i(getClass().toString(), "Thumb load done for pos " + position);
                        }

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            super.onLoadingStarted(imageUri, view);
                            Log.d(getClass().toString(), "Thumb load started for pos " + position);
                        }
                    });


            if (position + Constants.UPDATE_RESULT_THRESHOLD
                    >= sr.lastitem) {
                // this is the last item on its page
                // add a footer view "loading" ?
                Log.d(getClass().toString(), "Last item in page reached");
                ListView lv = (ListView) findViewById(R.id.result_list);
                if (lv.getFooterViewsCount() == 0) {
                    message = new TextView(getApplicationContext());
                    message.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Large);
                    message.setTextColor(getResources().getColor(R.color.primary_text_default_material_light));
                    message.setText(R.string.search_loading_more);
                    lv.addFooterView(message);
                    ((ResultListAdapter)
                            ((HeaderViewListAdapter) lv.getAdapter())
                                    .getWrappedAdapter()).showMore();
                } else {
                    Log.i(getClass().toString(), "Already loading a new page... Be patient");
                }
            }

            if ((position + 1) == sr.totalResult) {
                // this is the last item of the whole search
                // add a footer view "fin" ?
                Log.d(getClass().toString(), "Last item in search reached");
                ListView lv = (ListView) findViewById(R.id.result_list);
                message = new TextView(getApplicationContext());
                message.setTextAppearance(getApplicationContext(), android.R.style.TextAppearance_Large);
                message.setText(R.string.search_loading_all);
                lv.addFooterView(message);
            }
            return v;
        }

    }

    private class ResultPagerAdapter extends PagerAdapter {
        public int count;
        public SearchResult sr;
        public boolean updating; // disable loading more pages

        public boolean updateCount() {
            Log.d(getClass().toString(), "Count was " + count);
            if (count == sr.totalResult) {
                return true;
            } else {
                count = Math.min(count + Constants.RESULT_ITEMS_PER_PAGE, sr.totalResult);
                Log.d(getClass().toString(), "Count is " + count);
                notifyDataSetChanged();
                return isEnd();
            }
        }

        public void showMore() {
            sr.query.whichPage++;
            new GallerySearcher(sr, sa).execute(findViewById(R.id.result_pager));
        }

        public boolean isEnd() {
            return count == sr.totalResult;
        }


        public ResultPagerAdapter(SearchResult sr) {
            this.sr = sr;
            this.count = sr.result.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {

            Log.d(getClass().toString(), "getView called, position = " + position);
            final Gallery g = sr.result.get(position);
            View v = getLayoutInflater().inflate(R.layout.fragment_result_pager, container, false);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(sa, ViewerActivity.class);
                    intent.putExtra("gallery", g);
                    startActivity(intent);
                }
            });
            assert v != null;
            ((TextView) v.findViewById(R.id.pagertitle)).setText(g.title);
            ((TextView) v.findViewById(R.id.pagerdesc)).setText(
                    "(" + (position + 1) + ") " + g.pages.size() + " file" + (g.pages.size() == 1 ? "" : "s")
            );
            v.findViewById(R.id.pagertype).setBackgroundResource(
                    Constants.COLORMAP.get(g.type));
            ((RatingBar) v.findViewById(R.id.pagerrating)).setRating(g.rating);
            Log.d(getClass().toString(), "Pos " + position + " has thumb " + g.thumb);
            final ProgressBar progress = ((ProgressBar) v.findViewById(R.id.pagerprogress));
            ImageLoader.getInstance().displayImage(g.thumb, (ImageView) v.findViewById(R.id.pagerthumb),
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            ((ImageView) view).setImageBitmap(loadedImage);
                            progress.setVisibility(View.GONE);
                            Log.i(getClass().toString(), "Thumb load done for pos " + position);
                        }

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            super.onLoadingStarted(imageUri, view);
                            progress.setVisibility(View.VISIBLE);
                            Log.d(getClass().toString(), "Thumb load started for pos " + position);
                        }
                    });


            if (position + Constants.UPDATE_RESULT_THRESHOLD
                    >= sr.lastitem) {
                // this is the last item on its page
                // add a footer view "loading" ?
                if (updating) {
                    Log.i(getClass().toString(), "Already loading a new page... Be patient");
                } else {
                    Log.d(getClass().toString(), "Last item in page reached");
                    ViewPager vp = (ViewPager) findViewById(R.id.result_pager);
                    ((ResultPagerAdapter) vp.getAdapter()).showMore();
                    updating = true;

                }
            }


            if ((position + 1) == sr.totalResult) {
                // this is the last item of the whole search
                // add a footer view "fin" ?
                Log.d(getClass().toString(), "Last item in search reached");
            }

            container.addView(v);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }
    }


    private class ResultGridAdapter extends BaseAdapter {
        // we assume the users always start at the first page
        // don't need to implement "going back"
        public SearchResult sr;
        public int count; // count
        private boolean updating; // disable loading more pages

        public ResultGridAdapter(SearchResult sr) {
            this.sr = sr;
            this.count = sr.result.size();
        }

        public boolean updateCount() {
            Log.d(getClass().toString(), "Count was " + count);
            if (count == sr.totalResult) {
                return true;
            } else {
                count = Math.min(count + Constants.RESULT_ITEMS_PER_PAGE, sr.totalResult);
                Log.d(getClass().toString(), "Count is " + count);
                notifyDataSetChanged();
                return isEnd();
            }
        }

        public void showMore() {
            sr.query.whichPage++;
            new GallerySearcher(sr, sa).execute(findViewById(R.id.result_pager));
        }

        public boolean isEnd() {
            return count == sr.totalResult;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public Object getItem(int position) {
            return sr.result.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View v, ViewGroup parent) {
            Log.d(getClass().toString(), "getView called, position = " + position);
            if (v == null) {
                // inflate new view
                Log.d(getClass().toString(), "Need to make new view");
                v = getLayoutInflater().inflate(R.layout.fragment_result_grid, null);
            }
            Gallery g = sr.result.get(position);
            ((TextView) v.findViewById(R.id.gridtitle)).setText(g.title);
            ((TextView) v.findViewById(R.id.griddesc)).setText(
                    "(" + (position + 1) + ") " + g.pages.size() + " file" + (g.pages.size() == 1 ? "" : "s")
            );
            v.findViewById(R.id.gridtype).setBackgroundResource(
                    Constants.COLORMAP.get(g.type));
            ((RatingBar) v.findViewById(R.id.gridrating)).setRating(g.rating);
            Log.d(getClass().toString(), "Pos " + position + " has thumb " + g.thumb);
            ImageLoader.getInstance().displayImage(g.thumb, (ImageView) v.findViewById(R.id.gridthumb),
                    new SimpleImageLoadingListener() {
                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            super.onLoadingComplete(imageUri, view, loadedImage);
                            ((ImageView) view).setImageBitmap(loadedImage);
                            Log.i(getClass().toString(), "Thumb load done for pos " + position);
                        }

                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            super.onLoadingStarted(imageUri, view);
                            Log.d(getClass().toString(), "Thumb load started for pos " + position);
                        }
                    });


            if (position + Constants.UPDATE_RESULT_THRESHOLD
                    >= sr.lastitem) {
                // this is the last item on its page
                // add a footer view "loading" ?
                if (updating) {
                    Log.i(getClass().toString(), "Already loading a new page... Be patient");
                } else {
                    Log.d(getClass().toString(), "Last item in page reached");
                    ViewPager vp = (ViewPager) findViewById(R.id.result_pager);
                    ((ResultPagerAdapter) vp.getAdapter()).showMore();
                    updating = true;

                }
            }


            if ((position + 1) == sr.totalResult) {
                // this is the last item of the whole search
                // add a footer view "fin" ?
                Log.d(getClass().toString(), "Last item in search reached");
            }
            return v;
        }

    }

    public void toggleButton(View view) {
        if (view.getAlpha() == 1.0f) {
            view.setAlpha(0.5f);
        } else {
            view.setAlpha(1.0f);
        }
    }

    public void toggleCheckbox(View view) {
        Log.d(getClass().toString(), view + " checked? " + ((CheckBox) view).isChecked());
        if (((CheckBox) view).isChecked()) {
            ((CheckBox) view).setChecked(true);
        } else {
            ((CheckBox) view).setChecked(false);
        }
    }


    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    public void doSearch(View v) {
        // TODO Do search here

        // hide drawer
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
        SearchQuery sq = new SearchQuery();

        ImageButton search1 = (ImageButton) findViewById(R.id.search1);
        ImageButton search2 = (ImageButton) findViewById(R.id.search2);
        ImageButton search3 = (ImageButton) findViewById(R.id.search3);
        ImageButton search4 = (ImageButton) findViewById(R.id.search4);
        ImageButton search5 = (ImageButton) findViewById(R.id.search5);
        ImageButton search6 = (ImageButton) findViewById(R.id.search6);
        ImageButton search7 = (ImageButton) findViewById(R.id.search7);
        ImageButton search8 = (ImageButton) findViewById(R.id.search8);
        ImageButton search9 = (ImageButton) findViewById(R.id.search9);
        ImageButton search10 = (ImageButton) findViewById(R.id.search10);

        EditText query = (EditText) findViewById(R.id.searchQuery);
        CheckBox searchName = (CheckBox) findViewById(R.id.searchName);
        CheckBox searchTags = (CheckBox) findViewById(R.id.searchTags);
        CheckBox searchDesc = (CheckBox) findViewById(R.id.searchDesc);
        CheckBox searchLowPower = (CheckBox) findViewById(R.id.searchLowPower);
        CheckBox searchDownvoted = (CheckBox) findViewById(R.id.searchDownvoted);
        CheckBox searchExpunged = (CheckBox) findViewById(R.id.searchExpunged);
        CheckBox minStars = (CheckBox) findViewById(R.id.minStars);
        Spinner minRating = (Spinner) findViewById(R.id.minRating);
        //whew

        sq.query = query.getText().toString();
        Log.d(getClass().toString(), searchName + " checked? " + searchName.isChecked());
        sq.searchName = searchName.isChecked();
        sq.searchTags = searchTags.isChecked();
        sq.searchDesc = searchDesc.isChecked();
        sq.searchLowPower = searchLowPower.isChecked();
        sq.searchDownvoted = searchDownvoted.isChecked();
        sq.searchExpunged = searchExpunged.isChecked();
        sq.searchMinStars = minStars.isChecked();

        sq.searchMinRating = minRating.getSelectedItemPosition() + 1;

        if (search1.getAlpha() == Constants.BUTTON_INACTIVE_ALPHA) {
            sq.type.put(R.id.search1, 0);
        } else {
            sq.type.put(R.id.search1, 1);
        }
        if (search2.getAlpha() == Constants.BUTTON_INACTIVE_ALPHA) {
            sq.type.put(R.id.search2, 0);
        } else {
            sq.type.put(R.id.search2, 1);
        }
        if (search3.getAlpha() == Constants.BUTTON_INACTIVE_ALPHA) {
            sq.type.put(R.id.search3, 0);
        } else {
            sq.type.put(R.id.search3, 1);
        }
        if (search4.getAlpha() == Constants.BUTTON_INACTIVE_ALPHA) {
            sq.type.put(R.id.search4, 0);
        } else {
            sq.type.put(R.id.search4, 1);
        }
        if (search5.getAlpha() == Constants.BUTTON_INACTIVE_ALPHA) {
            sq.type.put(R.id.search5, 0);
        } else {
            sq.type.put(R.id.search5, 1);
        }
        if (search6.getAlpha() == Constants.BUTTON_INACTIVE_ALPHA) {
            sq.type.put(R.id.search6, 0);
        } else {
            sq.type.put(R.id.search6, 1);
        }
        if (search7.getAlpha() == Constants.BUTTON_INACTIVE_ALPHA) {
            sq.type.put(R.id.search7, 0);
        } else {
            sq.type.put(R.id.search7, 1);
        }
        if (search8.getAlpha() == Constants.BUTTON_INACTIVE_ALPHA) {
            sq.type.put(R.id.search8, 0);
        } else {
            sq.type.put(R.id.search8, 1);
        }
        if (search9.getAlpha() == Constants.BUTTON_INACTIVE_ALPHA) {
            sq.type.put(R.id.search9, 0);
        } else {
            sq.type.put(R.id.search9, 1);
        }
        if (search10.getAlpha() == Constants.BUTTON_INACTIVE_ALPHA) {
            sq.type.put(R.id.search10, 0);
        } else {
            sq.type.put(R.id.search10, 1);
        }

        new GallerySearcher(sq, this).execute(findViewById(R.id.container));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
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
            case R.id.action_toggle_search_drawer:
                if (item.getTitle().equals("Show Search")) {
                    // search is hidden, as in default
                    ((DrawerLayout) findViewById(R.id.drawer_layout)).openDrawer(Gravity.LEFT);
                    item.setTitle("Hide Search");
                    Log.i(getClass().toString(), "Hidden search drawer");
                } else {
                    // search is open, title was hide search
                    ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawer(Gravity.LEFT);
                    item.setTitle("Show Search");
                    Log.i(getClass().toString(), "Shown search drawer");
                }
                // TODO in first use also tell the user they can drag from side
                return true;
            case R.id.action_change_sorting:
                if (viewMode == Constants.SHOW_AS_LIST) {
                    // cycle to grid
                    setViewMode(Constants.SHOW_AS_GRID);
                    Log.i(getClass().toString(), "Changed view to grid");
                } else if (viewMode == Constants.SHOW_AS_GRID) {
                    setViewMode(Constants.SHOW_AS_PAGER);
                    Log.i(getClass().toString(), "Changed view to pager");
                } else if (viewMode == Constants.SHOW_AS_PAGER) {
                    setViewMode(Constants.SHOW_AS_LIST);
                    Log.i(getClass().toString(), "Changed view to list");
                } else {
                    Log.w(getClass().toString(), "Unknown viewMode");
                    return false;
                }
                return true;
            case R.id.action_show_favorites:
                // TODO make sqlite backed fav
                return true;
            case R.id.action_go_settings:
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                return true;
            case R.id.action_go_help:
                return true;

        }

        return super.onOptionsItemSelected(item);
    }


}
