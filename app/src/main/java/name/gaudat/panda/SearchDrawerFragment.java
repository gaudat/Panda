package name.gaudat.panda;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import name.gaudat.panda.data.SearchQuery;

/**
 * Fragment used for managing interactions for and presentation of a Search drawer.
 * See the <a href="https://developer.android.com/design/patterns/Search-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class SearchDrawerFragment extends Fragment {

    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private SearchDrawerCallbacks mCallbacks;

    public static SearchDrawerFragment newInstance(SearchQuery sq) {
        // set arguments here

        SearchDrawerFragment fragment = new SearchDrawerFragment();
        Bundle args = new Bundle();
        args.putParcelable("query", sq);
        fragment.setArguments(args);
        return fragment;
    }

    public SearchDrawerFragment() {
    }

    SearchQuery sq;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // get arguments here
        if (getArguments() != null) {
            this.sq = getArguments().getParcelable("query");
        }
        // if nothing is passed make a new query
        if (sq == null) this.sq = new SearchQuery();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_drawer, container, false);
        Spinner sp = (Spinner) v.findViewById(R.id.minRating);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(),
                R.array.ratings, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        sp.setAdapter(adapter);
        setQuery(sq, v);
        return v;
    }


    public void setQuery(SearchQuery sq, View v) {
        ImageButton search1 = (ImageButton) v.findViewById(R.id.search1);
        ImageButton search2 = (ImageButton) v.findViewById(R.id.search2);
        ImageButton search3 = (ImageButton) v.findViewById(R.id.search3);
        ImageButton search4 = (ImageButton) v.findViewById(R.id.search4);
        ImageButton search5 = (ImageButton) v.findViewById(R.id.search5);
        ImageButton search6 = (ImageButton) v.findViewById(R.id.search6);
        ImageButton search7 = (ImageButton) v.findViewById(R.id.search7);
        ImageButton search8 = (ImageButton) v.findViewById(R.id.search8);
        ImageButton search9 = (ImageButton) v.findViewById(R.id.search9);
        ImageButton search10 = (ImageButton) v.findViewById(R.id.search10);

        EditText query = (EditText) v.findViewById(R.id.searchQuery);
        CheckBox searchName = (CheckBox) v.findViewById(R.id.searchName);
        CheckBox searchTags = (CheckBox) v.findViewById(R.id.searchTags);
        CheckBox searchDesc = (CheckBox) v.findViewById(R.id.searchDesc);
        CheckBox searchLowPower = (CheckBox) v.findViewById(R.id.searchLowPower);
        CheckBox searchDownvoted = (CheckBox) v.findViewById(R.id.searchDownvoted);
        CheckBox searchExpunged = (CheckBox) v.findViewById(R.id.searchExpunged);
        CheckBox minStars = (CheckBox) v.findViewById(R.id.minStars);
        Spinner minRating = (Spinner) v.findViewById(R.id.minRating);
        //whew

        search1.setAlpha(sq.type.get(R.id.search1));
        search2.setAlpha(sq.type.get(R.id.search2));
        search3.setAlpha(sq.type.get(R.id.search3));
        search4.setAlpha(sq.type.get(R.id.search4));
        search5.setAlpha(sq.type.get(R.id.search5));
        search6.setAlpha(sq.type.get(R.id.search6));
        search7.setAlpha(sq.type.get(R.id.search7));
        search8.setAlpha(sq.type.get(R.id.search8));
        search9.setAlpha(sq.type.get(R.id.search9));
        search10.setAlpha(sq.type.get(R.id.search10));

        query.setText(sq.query);
        searchName.setChecked(sq.searchName);
        searchTags.setChecked(sq.searchTags);
        searchDesc.setChecked(sq.searchDesc);
        searchLowPower.setChecked(sq.searchLowPower);
        searchDownvoted.setChecked(sq.searchDownvoted);
        searchExpunged.setChecked(sq.searchExpunged);
        minStars.setChecked(sq.searchMinStars);

        minRating.setSelection(sq.searchMinRating - 1);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (SearchDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement SearchDrawerCallbacks.");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }


    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public interface SearchDrawerCallbacks {
        /**
         * Called when an item in the Search drawer is selected.
         */
    }
}
