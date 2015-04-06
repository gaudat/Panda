package name.gaudat.panda.data;

import android.content.Context;
import name.gaudat.panda.R;

import java.util.HashMap;

/**
 * Created by anon on 10/1/2015.
 */
public class Constants {
    // Ignore static inner class errors
    // These will be dealt with when moving back to individual java files

    public static final String NORMAL_SITEROOT = "http://g.e-hentai.org";
    public static final String FJORD_SITEROOT = "http://exhentai.org";
    public static final int IMG_PER_PAGE = 40;
    public static final HashMap<String, Integer> TYPEMAP = new HashMap<>();

    static {
        TYPEMAP.put("doujinshi", 1);
        TYPEMAP.put("manga", 2);
        TYPEMAP.put("artistcg", 3);
        TYPEMAP.put("gamecg", 4);
        TYPEMAP.put("western", 5);
        TYPEMAP.put("non-h", 6);
        TYPEMAP.put("imageset", 7);
        TYPEMAP.put("cosplay", 8);
        TYPEMAP.put("asianporn", 9);
        TYPEMAP.put("misc", 10);
    }

    public static final HashMap<Integer, Integer> COLORMAP = new HashMap<>();

    static {
        COLORMAP.put(1, R.color.c_doujinshi);
        COLORMAP.put(2, R.color.c_manga);
        COLORMAP.put(3, R.color.c_artistcg);
        COLORMAP.put(4, R.color.c_gamecg);
        COLORMAP.put(5, R.color.c_western);
        COLORMAP.put(6, R.color.c_nonh);
        COLORMAP.put(7, R.color.c_imageset);
        COLORMAP.put(8, R.color.c_cosplay);
        COLORMAP.put(9, R.color.c_asianporn);
        COLORMAP.put(10, R.color.c_misc);
    }

    public static final String GUEST_ACC = "Guest";
    public static final int LOAD_THUMB = 1;
    public static final float BUTTON_INACTIVE_ALPHA = 0.5f;
    public static final float BUTTON_ACTIVE_ALPHA = 1.0f;
    public static final int RESULT_ITEMS_PER_PAGE = 25;
    public static final String SEARCH_QUERY_FILE_NAME = "Panda image search";
    public static final int UPDATE_RESULT_THRESHOLD = 5;

    public static Context applicationContext;
    public static String FOLDER_PREFIX = "Panda";
    // prefix to download the images into

    public static final int SHOW_AS_LIST = 1;
    public static final int SHOW_AS_GRID = 2;
    public static final int SHOW_AS_PAGER = 3;
    public static final String GUEST_PW = "";
    public static final int SEARCH_WITH_LIST_VIEW = 1;
    public static final int SEARCH_WITH_THUMB_VIEW = 2;
    public static final String LOGIN_SITE = "https://forums.e-hentai.org/index.php?act=Login&CODE=01";

    // delete if in production copy
    public static final String TEST_ACC = "dtwai";
    public static final String TEST_PW = "dontaskwhoami";

    public static Account CURRENT_ACCOUNT;

}



