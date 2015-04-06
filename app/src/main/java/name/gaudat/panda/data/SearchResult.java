package name.gaudat.panda.data;

import java.util.HashMap;

/**
 * Created by anon on 11/1/2015.
 */
public class SearchResult {

    // search results
    public HashMap<Integer, Gallery> result = new HashMap<>();
    public int totalResult;
    public SearchQuery query;
    public int lastitem;

    public int getPages() {
        return totalResult / Constants.RESULT_ITEMS_PER_PAGE;
    }


    public SearchResult() {
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "totalResult=" + totalResult +
                ", query=" + query +
                '}';
    }
}
