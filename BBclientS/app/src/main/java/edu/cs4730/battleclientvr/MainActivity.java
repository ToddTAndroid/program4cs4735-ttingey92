package edu.cs4730.battleclientvr;

import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.vr.sdk.base.GvrActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.controller.Controller.ConnectionStates;
import com.google.vr.sdk.controller.ControllerManager;
import com.google.vr.sdk.controller.ControllerManager.ApiStatus;

public class MainActivity extends GvrActivity implements
        ConnectFragment.OnFragmentInteractionListener,
        GVRFragment.OnFragmentInteractionListener {
    String TAG = "MainActivity";


    //object used for waiting
    // private static final Object lock = new Object();

    //fragment variables
    FragmentManager fragmentManager;
    ConnectFragment myConnectFrag;
    GVRFragment myGVRFrag;

    //networking variables
    boolean connected = false, running = false;
    network mynetwork = null;
    String host, botline = null;
    int port;

    //game variables
    ArrayList ScanInfo = new ArrayList();


    // These two objects are the primary APIs for interacting with the Daydream controller.  I'm leaving them in case you want to use them.
    private ControllerManager controllerManager;
    private Controller controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentManager = getFragmentManager();
        myConnectFrag = new ConnectFragment();

        //setup DD controller here if needed.


        fragmentManager.beginTransaction().replace(R.id.container, myConnectFrag).commit();


    }

    //when called, the user says connect!  which is not really used right now.
    @Override
    public void onFragmentConnection(int which) {

        /*   later, do this with the preference system.
        host = prefs.getString("Host", "");
        port = Integer.parseInt(prefs.getString("Port", "3012"));
        botline = prefs.getString("Botname", "") + " " + prefs.getString("Botarmour", "") + " " + prefs.getString("Botshot", "") + " "
                + prefs.getString("Botscan", "") + " ";
        */

        host = myConnectFrag.host;
        port = myConnectFrag.port;
        botline = myConnectFrag.name + " " + myConnectFrag.armor + " " + myConnectFrag.power + " "
                + myConnectFrag.scan;
        if (!connected) {
            mynetwork = new network();
            mynetwork.set(host, port, botline);
            new Thread(mynetwork).start();
        }
        //once connected and we have the info, it will switch the GVRfragment to display the game.

    }

    @Override
    public void onFragmentInteraction(String cmd) {
        //so send the command to the server!
    }


    //this is used to correct the angle so it pointed correctly.
    public int getAngle() {
        //test firing first.
        int angle = myGVRFrag.render.getAngle();

        //so 0 to 180 is 0 to -180, 181 to 360 ti 180 to 0
        if (angle < 0) {
            angle = -angle;
        } else if (angle > 0 && angle < 181) {
            angle = 360 - angle;
        } else {
            //well shit...
            angle = -2;  //error code basically.
        }
        Log.d(TAG, "Angle is " + angle);
        return angle;
    }

    //using the angle determine which direction we are facing.  but maybe you don't want to do that.
    private void onMove(int angle) {

        String cmdn = "";

        /* based on direction you are facing, move that way.
        if I could suggest group of 45 angle, which is +/-22.5 from say 0 (UP), but we are using integers, so 22 or 23 you'll call..
          which would set cmdn="move 0 -1";
         */
        synchronized (mynetwork.cmd) {
            mynetwork.cmd = cmdn;
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String str = msg.getData().getString("msg");
            if (str == null) {
                return true;
            }
            if (str.startsWith("Err: ")) {
                Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
            } else if (str.startsWith("READY")) {
                //We can now switch the GVRfragment.
                fragmentManager.beginTransaction().replace(R.id.container, myGVRFrag).commit();
            } else if (str.startsWith("RESET")) {
                //We can now switch the GVRfragment.
                fragmentManager.beginTransaction().replace(R.id.container, myConnectFrag).commit();
            } else if (str.startsWith("STATUS")) {
                //Log.v(TAG, "Status: " + str);
                //statusupdate(str);
            } else {
                Log.v(TAG, "General: " + str);
                //output.setText(str);
                //procesStr(str);
            }
            return true;
        }

    });


    public static String[] token(String text) {
        return text.split("[ ]+");
    }

    public void mkmsg(String str) {
        //handler junk, because thread can't update screen!
        Message msg = new Message();
        Bundle b = new Bundle();
        b.putString("msg", str);
        msg.setData(b);
        handler.sendMessage(msg);
    }


    public class network implements Runnable {
        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;
        String host, botline;
        int port;
        //public boolean send = false;
        String cmd = "";

        String TAG = "myNetwork";

        network() {
            host = "k2win.cs.uwyo.edu";
            port = 3012;
            botline = "Testbot 0 0 4";
        }

        public void set(String h, int p, String b) {
            host = h;
            port = p;
            botline = b;
        }

        public void done() {
            running = false;
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException e) {
                //don't care.
                System.out.println("yea. died while closing!");
            }
            in = null;
            out = null;
            socket = null;
            connected = false;

        }

        public boolean connect() {
            //int p = Integer.parseInt(port.getText().toString());
            //String h = hostname.getText().toString();
            InetAddress serverAddr;
            try {
                serverAddr = InetAddress.getByName(host);
            } catch (UnknownHostException e) {
                mkmsg("Err: Unknown host");
                return false;
            }
            try {
                socket = new Socket(serverAddr, port);
            } catch (IOException e) {
                mkmsg("Err: Unable to make connection");
                socket = null;
                return false;
            }
            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                mkmsg("Err: Made connection, but streams failed");
                in = null;
                out = null;
                return false;
            }
            connected = true;
            return true;
        }


        @Override
        public void run() {
            if (!connect()) {
                return;
            }
            running = true;
            String line;
            //get init data and bot setup.
            line = readln();  //setup line
            //tokenize and get PID!
            Log.v(TAG, "Setup: " + line);
            //line ia PID WidthArena HeighArena NumberOfBots
            //String[] str = token(line);
            // myPID = Integer.valueOf(str[1]);
            // mkmsg("SETUP " + line);

            writeln(botline);  //my bot

            //Now the response with
            //name ArmourValue MoveRate ScanDistance BulletPower RateOfFire BulletDistance
            String line2 = readln();  //init line about bot.
            Log.v(TAG, "Setup2: " + line2);

            // send the message here to startup the GVR fragment!
            myGVRFrag = GVRFragment.newInstance(line, line2);
            mkmsg("READY");

            //now wait for game to start and get the first status line
            String str[];
            String temp;
            while (running && connected) {
                //Read in the status line, plus info lines.
                /// System.out.println("Info or status line: ");
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                str = readandToken();
                int mr, sl;
                // System.out.println("str[0] is "+str[0]);
                while (str[0].equals("Info")) {
                    if (str[1].equals("Dead") || str[1].equals("GameOver")) {
                        running = false;
                        //disconnect and connected = false
                        done();
                        break;
                    } else if (str[1].equals("Alive")) {
                        //abots = Integer.parseInt(str[2]);
                    }
                    str = readandToken();
                }
                // So assuming we are still alive, issue a command.
                if (running) {
                    //get status line
                    //          x = Integer.parseInt(str[1]);  //curent x pos
                    //          y = Integer.parseInt(str[2]);  //curent y pos
                    //          mr = Integer.parseInt(str[3]); //movement rate
                    //          sl = Integer.parseInt(str[4]); //shot left to fire
                    //          hp = Integer.parseInt(str[5]); //hit points
                    //send status message for drawing purposes
                    mkmsg("STATUS " + str[1] + " " + str[2] + " " + str[3] + " " + str[4] + " " + str[5]);
                    myGVRFrag.setme(Float.parseFloat(str[1]), Float.parseFloat(str[2]));
                    mr = Integer.parseInt(str[3]); //movement rate
                    sl = Integer.parseInt(str[4]);
                    synchronized (cmd) {

                        if (mr != 0 && sl != 0)
                            doScan();
                        else if (cmd.compareTo("") == 0)   // there is no command, so issue a scan
                            doScan();
                        else if (cmd.startsWith("fire") && sl != 0)
                            doScan();
                        else if (cmd.startsWith("move") && mr != 0)
                            doScan();
                        else {
                            writeln(cmd);
                            Log.d(TAG, "Wrote: " + cmd);
                            cmd = "";
                        }
                    }  //end of synchronized.
                }
            }
            mkmsg("RESET");

        }

        public void doScan() {
            String temp;
            writeln("scan");
            ScanInfo.clear();
            temp = readln();
            //System.out.println("Scan: "+temp);
            while (temp != null && !temp.equals("scan done") && !temp.equals("FAILED")) {
//                            Log.v(TAG, "Scan: " + temp);
                ScanInfo.add(temp);
                //read in scan info and deal it
                temp = readln();
            }
            //now send that to the GVR to display.
            myGVRFrag.SetInfo(ScanInfo);
        }

        public void writeln(String str) {
            out.println(str);
            out.flush();
        }

        public String readln() {
            String str = "FAILED";
            if (in != null && running) {
                try {
                    str = in.readLine();
                } catch (IOException e) {
                    mkmsg("Err: Read failed");
                    connected = false;
                    running = false;
                }
            } else {
                connected = false;
                running = false;
            }
            return str;
        }

        public String[] readandToken() {
            String Failed[] = {"Failed"};
            String line = readln();
            if (line == null) {
                line = "FAILED";
            }
            // mkmsg(line);
            //System.out.println(line);
            if (line.compareTo("FAILED") == 0) {
                return Failed;
            } else {
                //return  token(line);
                return line.split("[ ]+");
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //controllerManager.start();

    }

    @Override
    protected void onStop() {
        //controllerManager.stop();

        super.onStop();
    }


    // We receive all events from the Controller through this listener. In this example, our
    // listener handles both ControllerManager.EventListener and Controller.EventListener events.
/*
    private class EventListener extends Controller.EventListener
            implements ControllerManager.EventListener {

    }
*/
}
