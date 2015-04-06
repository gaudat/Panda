package name.gaudat.panda;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import name.gaudat.panda.data.Constants;
import name.gaudat.panda.data.Gallery;
import name.gaudat.panda.data.Tag;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GalleryDetailFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GalleryDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GalleryDetailFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "gallery";

    private Gallery mParam1;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment GalleryDetailFragment.
     */

    // Always pass a finished gallery to this
    public static GalleryDetailFragment newInstance(Gallery param1) {
        GalleryDetailFragment fragment = new GalleryDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    public GalleryDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getParcelable(ARG_PARAM1);
        }
        Log.d("GalleryDetailFragment", "Got gallery parcel");
        setGalleryDetails(mParam1);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gallery_detail, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // There should be no
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }


    public void setGalleryDetails(Gallery g) {
        TextView t = (TextView) getView().findViewById(R.id.title);
        assert t != null;
        t.setText(g.title);
        t = (TextView) getView().findViewById(R.id.titlealt);
        t.setText(g.title2);
        t = (TextView) getView().findViewById(R.id.galleryuploader);
        t.setText(g.language + " " + g.uploaded + "@" + g.pages.size());
        RatingBar r = (RatingBar) getView().findViewById(R.id.galleryrating);
        r.setRating(g.rating);
        t = (TextView) getView().findViewById(R.id.galleryratingtext);
        t.setText(((Float) g.rating).toString() + " (" + ((Integer) g.raters).toString() + ") ");
        ImageView i = (ImageView) getView().findViewById(R.id.gallerycolor);
        i.setBackgroundColor(Constants.COLORMAP.get(Constants.TYPEMAP.get(g.type)));
        new TagListFiller(g.tags).execute();
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
            Log.d("TaglistFiller", "Appended styles");
            for (Tag t : tags) {
                s += "<div class=\"" +
                        t.attr +
                        "\"><a href=\"name.gaudat.panda://search/" +
                        t.namespace + ":" + t.name +
                        "\">" + t.namespace + ":" + t.name +

                        "</div>";
                Log.d(getClass().toString(), "Filled" + t.name);
            }
            Log.i(getClass().toString(), "Filled tags");
            return s;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            TextView t = (TextView) getView().findViewById(R.id.taglist);
            t.setText(Html.fromHtml(s));
        }
    }
}
