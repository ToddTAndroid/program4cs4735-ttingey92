package edu.cs4730.battleclientvr;

// Code added by Todd Tingey for program4

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConnectFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ConnectFragment extends Fragment {

    Context context = getActivity();

    private OnFragmentInteractionListener mListener;

    public String host, name="VRbot";
    public int port=3012, armor=0, power=0, scan=0;

    EditText et_host, et_port, et_name, et_armor, et_power, et_scan;
    Button btn_connect;
    Boolean isJoyStick = false, isGamePad = false;
    AlertDialog alert;

    public ConnectFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView =  inflater.inflate(R.layout.fragment_connect, container, false);


        et_host = (EditText)myView.findViewById(R.id.et_host);
        et_port = (EditText)myView.findViewById(R.id.et_port);
        et_name = (EditText)myView.findViewById(R.id.et_name);
        et_armor = (EditText)myView.findViewById(R.id.et_armor);
        et_power = (EditText)myView.findViewById(R.id.et_power);
        et_scan = (EditText)myView.findViewById(R.id.et_scan);
        btn_connect = (Button)myView.findViewById(R.id.btn_connect);
        btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                host = et_host.getText().toString();
                port = Integer.parseInt(et_port.getText().toString());
                name = et_name.getText().toString();
                armor = Integer.parseInt(et_armor.getText().toString());
                power = Integer.parseInt(et_power.getText().toString());
                scan = Integer.parseInt(et_scan.getText().toString());
                getGameControllerIds();
                //mListener.onFragmentConnection(0);  //now have the activity do the networking and switch to the other fragment.
                //seriously suggest using the controller or bt controller to say "click" ok here.  ie, do this, so you put the phone
                //in the headset.

                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Press A when ready");
                builder.setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                btn_connect.setVisibility(View.GONE);
                            }
                        });
                alert = builder.create();
                alert.show();
            }
        });

        return myView;

    }

    public void myOnKeyDown() {
        mListener.onFragmentConnection(0);
    }

    public ArrayList getGameControllerIds() {
        ArrayList gameControllerDeviceIds = new ArrayList();
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();

            // Verify that the device has gamepad buttons, control sticks, or both.
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((sources & InputDevice.SOURCE_JOYSTICK)
                    == InputDevice.SOURCE_JOYSTICK)) {
                // This device is a game controller. Store its device ID.
                if (!gameControllerDeviceIds.contains(deviceId)) {
                    gameControllerDeviceIds.add(deviceId);
                }

                if ((sources & InputDevice.SOURCE_GAMEPAD)  == InputDevice.SOURCE_GAMEPAD) isGamePad= true;
                if ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) isJoyStick = true;
            }
        }
        return gameControllerDeviceIds;
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
        void onFragmentConnection(int which);
    }
}
