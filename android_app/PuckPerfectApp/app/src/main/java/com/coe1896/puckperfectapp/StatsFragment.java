package com.coe1896.puckperfectapp;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static android.content.Context.MODE_PRIVATE;

public class StatsFragment extends Fragment {

    View inflatedView;
    EditText start;
    EditText end;

    // Instantiate the RequestQueue.
    RequestQueue queue;

    private SharedPreferences.Editor editor;
    public static final String MY_PREFS_NAME = "PuckPerfectPrefs";

    private static final String TAG = "PracticeFragment";

    // Plot score vs date
    private ArrayList<Integer> scores = new ArrayList<>();
    private ArrayList<Date> dates = new ArrayList<>();

    private ArrayList<DataPoint> points = new ArrayList<>();
    LineGraphSeries<DataPoint> series;

    GraphView graph;

    Date startDateInput = null;
    Date endDateInput = null;

    public StatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment PracticeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatsFragment newInstance() {
        return new StatsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        inflatedView = inflater.inflate(R.layout.fragment_stats, container, false);

        final EditText startDate = (EditText) inflatedView.findViewById(R.id.startDate);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment date = new DatePickerFragment();

                Calendar calender = Calendar.getInstance();
                Bundle args = new Bundle();
                args.putInt("year", calender.get(Calendar.YEAR));
                args.putInt("month", calender.get(Calendar.MONTH));
                args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
                date.setArguments(args);

                date.setCallBack(startOnDate);
                date.show(getFragmentManager(), "Start Date Picker");
            }
        });

        startDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DatePickerFragment date = new DatePickerFragment();

                    Calendar calender = Calendar.getInstance();
                    Bundle args = new Bundle();
                    args.putInt("year", calender.get(Calendar.YEAR));
                    args.putInt("month", calender.get(Calendar.MONTH));
                    args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
                    date.setArguments(args);

                    date.setCallBack(startOnDate);
                    date.show(getFragmentManager(), "Start Date Picker");
                }
            }
        });

        start = startDate;

        final EditText endDate = (EditText) inflatedView.findViewById(R.id.endDate);
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment date = new DatePickerFragment();

                Calendar calender = Calendar.getInstance();
                Bundle args = new Bundle();
                args.putInt("year", calender.get(Calendar.YEAR));
                args.putInt("month", calender.get(Calendar.MONTH));
                args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
                date.setArguments(args);

                date.setCallBack(endOnDate);
                date.show(getFragmentManager(), "End Date Picker");
            }
        });

        endDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    DatePickerFragment date = new DatePickerFragment();

                    Calendar calender = Calendar.getInstance();
                    Bundle args = new Bundle();
                    args.putInt("year", calender.get(Calendar.YEAR));
                    args.putInt("month", calender.get(Calendar.MONTH));
                    args.putInt("day", calender.get(Calendar.DAY_OF_MONTH));
                    date.setArguments(args);

                    date.setCallBack(endOnDate);
                    date.show(getFragmentManager(), "End Date Picker");
                }
            }
        });

        end = endDate;

        Switch allTime = (Switch) inflatedView.findViewById(R.id.allTimeSwitch);

        if (allTime != null) {
            allTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        getAllPractices();
                    } else {
                        startDate.getText();

//                        getPracticeRange();
                    }
                }
            });
        }

        graph = (GraphView) inflatedView.findViewById(R.id.graph);
        getAllPractices();

        return inflatedView;
    }

    DatePickerDialog.OnDateSetListener startOnDate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            start.setText(String.valueOf(dayOfMonth) + "-" + String.valueOf(monthOfYear+1)
                    + "-" + String.valueOf(year));

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, monthOfYear);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            startDateInput = cal.getTime();

            if(endDateInput != null && endDateInput.compareTo(startDateInput) > 0)
            {
                getPracticeRange();
            }
        }
    };

    DatePickerDialog.OnDateSetListener endOnDate = new DatePickerDialog.OnDateSetListener() {

        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {

            end.setText(String.valueOf(dayOfMonth) + "-" + String.valueOf(monthOfYear+1)
                    + "-" + String.valueOf(year));

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, monthOfYear);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            endDateInput = cal.getTime();

            if(startDateInput != null && endDateInput.compareTo(startDateInput) > 0)
            {
                getPracticeRange();
            }
        }
    };

    public void getPracticeRange()
    {
        // Get user id
        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        String url ="http://www.katiepucci.com/puckperfect/players/" + userId + "/practices";

        queue = MySingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // GET request to players/player_id to retrieve user data
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        TextView debugText = (TextView) inflatedView.findViewById(R.id.debugText);
                        ScrollView statsDebug = (ScrollView) inflatedView.findViewById(R.id.statsDebug);

                        points = new ArrayList<>();
                        dates = new ArrayList<>();
                        scores = new ArrayList<>();

                        debugText.setText("");

                        try{
                            // Loop through the array elements
                            for(int i=0;i<response.length();i++){
                                // Get current json object
                                JSONObject practice = response.getJSONObject(i);
                                Date timestamp = null;

                                debugText.append(practice.toString() + "\n");
                                statsDebug.fullScroll(View.FOCUS_DOWN);

                                try{
                                    SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
                                    timestamp = df.parse(practice.getString("timestamp"));

                                    SimpleDateFormat dfComp = new SimpleDateFormat( "yyyy-MM-dd");
                                    Date timestampComp = dfComp.parse(practice.getString("timestamp"));

                                    debugText.append(startDateInput + "\n");
                                    debugText.append(endDateInput + "\n");
                                    debugText.append(timestampComp + "\n");
                                    debugText.append(startDateInput.compareTo(timestampComp) <= 0 ? "true\n":"false\n");
                                    statsDebug.fullScroll(View.FOCUS_DOWN);

                                    debugText.append(endDateInput.compareTo(timestampComp) >= 0 ? "true\n":"false\n");
                                    statsDebug.fullScroll(View.FOCUS_DOWN);

                                    if(startDateInput.compareTo(timestampComp) <= 0 && endDateInput.compareTo(timestampComp) >= 0)
                                    {
                                        debugText.append(timestamp.toString() + "\n\n");
                                        statsDebug.fullScroll(View.FOCUS_DOWN);

                                        // Add data point
                                        points.add(new DataPoint(timestamp.getTime(), practice.getInt("score")));
                                        dates.add(timestamp);
                                        scores.add(practice.getInt("score"));
                                    }

                                } catch(ParseException e) {
                                    // TODO: Error handling
                                    debugText.append("\nERROR\n");
                                    statsDebug.fullScroll(View.FOCUS_DOWN);
                                }

                            }

                            populateGraph();

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(),"getPlayerInfo Error",Toast.LENGTH_LONG).show();
                    }
                });

        jsonArrayRequest.setTag(TAG);
        MySingleton.getInstance(getActivity()).addToRequestQueue(jsonArrayRequest);
    }

    public void getAllPractices()
    {
        // Get user id
        SharedPreferences prefs = getActivity().getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        String url ="http://www.katiepucci.com/puckperfect/players/" + userId + "/practices";

        queue = MySingleton.getInstance(getActivity().getApplicationContext()).getRequestQueue();

        // GET request to players/player_id to retrieve user data
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        TextView debugText = (TextView) inflatedView.findViewById(R.id.debugText);
                        ScrollView statsDebug = (ScrollView) inflatedView.findViewById(R.id.statsDebug);

                        points = new ArrayList<>();
                        dates = new ArrayList<>();
                        scores = new ArrayList<>();

                        debugText.setText("");

                        try{
                            // Loop through the array elements
                            for(int i=0;i<response.length();i++){
                                // Get current json object
                                JSONObject practice = response.getJSONObject(i);
                                Date timestamp = null;

                                debugText.append(practice.toString() + "\n");
                                statsDebug.fullScroll(View.FOCUS_DOWN);

                                try{
                                    SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
                                    timestamp = df.parse(practice.getString("timestamp"));

                                    debugText.append(timestamp.toString() + "\n\n");
                                    statsDebug.fullScroll(View.FOCUS_DOWN);

                                    // Add data point
                                    points.add(new DataPoint(timestamp.getTime(), practice.getInt("score")));
                                    dates.add(timestamp);
                                    scores.add(practice.getInt("score"));

                                } catch(ParseException e) {
                                    // TODO: Error handling
                                    debugText.append("\nERROR\n");
                                    statsDebug.fullScroll(View.FOCUS_DOWN);
                                }

                            }

                            populateGraph();

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(),"getPlayerInfo Error",Toast.LENGTH_LONG).show();
                    }
                });

        jsonArrayRequest.setTag(TAG);
        MySingleton.getInstance(getActivity()).addToRequestQueue(jsonArrayRequest);
    }

    public void populateGraph()
    {
        if(this.points.size() != 0)
        {
            DataPoint[] points = new DataPoint[this.points.size()];

            for(int i=0; i<this.points.size(); i++)
                points[i] = this.points.get(i);

            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
            graph.addSeries(series);
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
            graph.getGridLabelRenderer().setNumHorizontalLabels(this.points.size());

            graph.getViewport().setMinX(points[0].getX());
            graph.getViewport().setMaxX(points[this.points.size()-1].getX());
            graph.getViewport().setXAxisBoundsManual(true);

            graph.getGridLabelRenderer().setHorizontalLabelsAngle(90);
            graph.getGridLabelRenderer().setLabelHorizontalHeight(200);
        }
        else
        {
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>();
            graph.addSeries(series);
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));

            graph.getGridLabelRenderer().setHorizontalLabelsAngle(90);
            graph.getGridLabelRenderer().setLabelHorizontalHeight(200);
        }

    }

    public static Date parseDate(String input ) throws java.text.ParseException {

        //NOTE: SimpleDateFormat uses GMT[-+]hh:mm for the TZ which breaks
        //things a bit.  Before we go on we have to repair this.
        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );

        //this is zero time so we need to add that TZ indicator for
        if (input.endsWith( "Z" )) {
            input = input.substring( 0, input.length() - 1) + "GMT-00:00";
        } else {
            int inset = 6;

            String s0 = input.substring( 0, input.length() - inset );
            String s1 = input.substring( input.length() - inset, input.length() );

            input = s0 + "GMT" + s1;
        }

        return df.parse( input );

    }

    public static String dateToString( Date date ) {

        SimpleDateFormat df = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssz" );

        TimeZone tz = TimeZone.getTimeZone( "UTC" );

        df.setTimeZone( tz );

        String output = df.format( date );

        int inset0 = 9;
        int inset1 = 6;

        String s0 = output.substring( 0, output.length() - inset0 );
        String s1 = output.substring( output.length() - inset1, output.length() );

        String result = s0 + s1;

        result = result.replaceAll( "UTC", "+00:00" );

        return result;

    }

}
