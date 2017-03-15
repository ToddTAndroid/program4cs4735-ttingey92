package edu.cs4730.battleclientvr;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GVRFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link GVRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GVRFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;


    //gvr variables
    CardboardOverlayView overlayView;
    myStereoRenderer render;


    public GVRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment GVRFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static GVRFragment newInstance(String param1, String param2) {
        GVRFragment fragment = new GVRFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_gvr, container, false);

        GvrView gvrView = (GvrView) myView.findViewById(R.id.cardboard_view);
        String str[] = MainActivity.token(mParam1); //seti[ pid, xsize, ysize, numberofbots
        render = new myStereoRenderer(Integer.parseInt(str[1]), Float.parseFloat(str[2]), Float.parseFloat(str[3]));
        gvrView.setRenderer(render);
        ((GvrActivity) getActivity()).setGvrView(gvrView);

        //this is overlay code from google, that allows us to put text on the "screen" easily.
        overlayView = (CardboardOverlayView) myView.findViewById(R.id.overlay);
        overlayView.show3DToast("Welcome to the demo.");


        return myView;
    }

    public void setme(float x, float y) {
        //this is my location, so change render camera view.
        if (render != null)  //so it appears the status get called sometimes, before the vr has setup!
            render.move(x, y);
    }

    public void SetInfo(ArrayList ScanInfo) {
        //do something...
        if (render != null)
            render.SetObjects(ScanInfo);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String cmd);
    }
}
