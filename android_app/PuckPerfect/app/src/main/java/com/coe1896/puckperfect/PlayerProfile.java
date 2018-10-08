package com.coe1896.puckperfect;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;
import java.util.Set;

public class PlayerProfile extends Activity {


    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private MyBluetoothService mBTService = null;


    // Instantiate the RequestQueue.
    RequestQueue queue;
    public static final String TAG = "PlayerProfile";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_player_profile);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }


    public void getPaired(View view)
    {
        final TextView mTextView = (TextView) findViewById(R.id.textView4);
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        String textViewString = "";
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = "Name: " + device.getName();
                String deviceHardwareAddress = "  MAC: " + device.getAddress() + "\n"; // MAC address
                textViewString += deviceName + deviceHardwareAddress;
            }
        }
        mTextView.setText(textViewString);

    }

    public void getRequest(View view)
    {
        final TextView mTextView = (TextView) findViewById(R.id.textView3);
        String url ="http://www.katiepucci.com/budget/cats/uncategorized";

         queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        mTextView.setText("Response: " + response.toString());
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });

        jsonObjectRequest.setTag(TAG);


        // Add the request to the RequestQueue.
        queue.add(jsonObjectRequest);
    }

    public void goToBluetooth(View view)
    {
        Intent intent = new Intent(this, BluetoothMain.class);
        startActivity(intent);
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }


}


