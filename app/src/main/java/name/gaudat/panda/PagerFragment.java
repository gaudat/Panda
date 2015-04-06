package name.gaudat.panda;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.polites.android.GestureImageView;
import name.gaudat.panda.data.Gallery;
import name.gaudat.panda.data.Page;
import name.gaudat.panda.worker.GalleryUpdater;
import name.gaudat.panda.worker.PageUpdater;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;


// Image class, used in view pagers

public class PagerFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static PagerFragment newInstance(ArrayList<Page> pages, int pagenumber) {
        PagerFragment fragment = new PagerFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList("pages", pages);
        args.putInt("pn", pagenumber);
        fragment.setArguments(args);
        return fragment;
    }

    public static PagerFragment newInstance(Gallery gallery, int pagenumber) {
        // this is used if gallery is passed
        PagerFragment fragment = new PagerFragment();
        Bundle args = new Bundle();
        args.putParcelable("gallery", gallery);
        args.putInt("pn", pagenumber);
        fragment.setArguments(args);
        return fragment;
    }


    public PagerFragment() {
        // Required empty public constructor
    }

    DisplayImageOptions o;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            p = getArguments().getParcelableArrayList("pages");
            if (p == null) {
                Log.d(getClass().toString(), "Passed gallery");
                g = getArguments().getParcelable("gallery");
                p = g.pages;
            }
            pn = getArguments().getInt("pn");
        }
        // update the gallery if it is not visited
        if (g.title2 == null) {
            try {
                Log.i(getClass().toString(), "Gallery needs to update as it is not clicked");
                g = new GalleryUpdater(g.getUpdateRequest()).execute().get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        // Make config of image loader
        o = new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // hide download gallery item if not loaded by gallery
        if (g == null) {
            menu.findItem(R.id.action_download_gallery).setVisible(false);
        } else {
            // hide display gallery item if it is loaded from gallery
            menu.findItem(R.id.action_view_gallery).setVisible(false);
        }
    }

    Gallery g;
    ArrayList<Page> p;
    int pn; // which page (array list index) to start at

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_pager, container, false);
        ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
        pager.setAdapter(new ImageViewerAdapter());
        pager.setCurrentItem(pn); // Zero based, corresponds to page number
        return rootView;
    }


    public class ImageViewerAdapter extends android.support.v4.view.PagerAdapter {

        ImageViewerAdapter() {
            inflater = LayoutInflater.from(getActivity());
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // This should take care of bitmap recycling
            container.removeView((View) object);
        }

        LayoutInflater inflater;

        @Override
        public int getCount() {
            if (p == null) return 1;
            return p.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, final int position) {
            View imageLayout = inflater.inflate(R.layout.fragment_viewer, view, false);
            assert imageLayout != null;
            onPageLoaded(position);
            final ProgressBar progress = (ProgressBar) imageLayout.findViewById(R.id.loading);
            final ImageView thumb = (ImageView) imageLayout.findViewById(R.id.thumb);
            final GestureImageView image = (GestureImageView) imageLayout.findViewById(R.id.image);
            progress.setVisibility(ProgressBar.VISIBLE);
            image.setVisibility(ImageView.VISIBLE);
            thumb.setVisibility(ImageView.VISIBLE);

            if (p == null || p.get(position) == null) {
                // update the gallery
                try {
                    g = new GalleryUpdater(g.getUpdateRequest(position)).execute().get();
                    mListener.setGalleryDetails(g);
                    p = g.pages;
                    notifyDataSetChanged();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            if (!p.get(position).clicked) {
                try {
                    p.set(position, new PageUpdater(p.get(position).getUpdateRequest()).execute().get());
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            // thumb is null if the pages are not from a gallery
            if (p.get(position).thumb != null) {
                ImageLoader.getInstance().loadImage(p.get(position).thumb, o, new SimpleImageLoadingListener() {
                    @Override
                    public void onLoadingCancelled(String imageUri, View view) {
                        thumb.setVisibility(ImageView.GONE);
                        Log.d(getClass().toString(), "Cancelled loading thumb " + imageUri);
                    }

                    @Override
                    public void onLoadingStarted(String imageUri, View view) {
                        progress.setVisibility(ProgressBar.VISIBLE);
                        Log.d(getClass().toString(), "Start loading thumb " + imageUri);
                    }

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                        thumb.setVisibility(ImageView.VISIBLE);
                        Bitmap bm = Bitmap.createBitmap(loadedImage, p.get(position).thoffset, 0,
                                p.get(position).twidth, p.get(position).theight);
                        thumb.setImageBitmap(bm);
                        Log.i(getClass().toString(), "Loading thumb complete for " + imageUri);
                    }

                });
            }
            ImageLoader.getInstance().displayImage(p.get(position).img, image, o, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {
                    progress.setVisibility(ProgressBar.VISIBLE);
                    Log.d(getClass().toString(), "Start loading image " + imageUri);
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    progress.setVisibility(ProgressBar.GONE);
                    thumb.setVisibility(ImageView.GONE);
                    image.setVisibility(ImageView.VISIBLE);
                    image.setImageBitmap(loadedImage);
                    Log.i(getClass().toString(), "Loading image complete for " + imageUri);
                    ImageLoader.getInstance().cancelDisplayTask(thumb);
                    onPageLoaded(position);

                }

            }, new ImageLoadingProgressListener() {
                @Override
                public void onProgressUpdate(String imageUri, View view, int current, int total) {
                    progress.setMax(total);
                    progress.setProgress(current);
                }
            });
            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }
    }


    public void onPageLoaded(int position) {
        // Pass
        if (p == null) {
            // There's nothing yet, better show "Loading"
            return;
        }
        if (mListener != null) {
            mListener.onFragmentInteraction(p.get(position));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
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
        public void onFragmentInteraction(Page p);

        public void setGalleryDetails(Gallery g);
    }

}
