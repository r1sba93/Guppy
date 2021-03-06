package com.example.androidhive;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SearchViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.message.BasicNameValuePair;

/**
 * Created by Ryan on 5/9/2016.
 */
public class Following_Fragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    public Explore main_activity;
    public SearchView search_box;
    public ProgressDialog pdialog;
    public SwipeRefreshLayout mySwipe;
    public boolean refresh = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View rootview = inflater.inflate(R.layout.following_fragment, container, false);

        main_activity = (Explore) getActivity();
        ListView imageButtonContextMenu;
        //imageButtonContextMenu = (ListView) rootview.findViewById(R.id.following_list);
        //registerForContextMenu(imageButtonContextMenu);
        main_activity.lv = (ListView) rootview.findViewById(R.id.following_list);
        main_activity.listDataChild = new HashMap<String, List<String>>();

        android.support.v7.app.ActionBar bar = main_activity.getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#209CF2")));
        bar.setTitle(Html.fromHtml("<font color='#ffffff'>Following</font>"));

        new LoadFollowingUsers().execute(main_activity.current_user_id);


        //new SearchUsers().execute("'%" + search_box.getQuery().toString() + "%'");



        search_box = (SearchView) rootview.findViewById(R.id.search_bar);
        search_box.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                Log.d("Search Submitted", "'%" + search_box.getQuery().toString() + "%'");
                new SearchUsers().execute("'%" + search_box.getQuery().toString() + "%'", main_activity.current_user_id);
                search_box.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("Searching", "'%" + search_box.getQuery().toString() + "%'");
                new SearchUsers().execute("'%" + search_box.getQuery().toString() + "%'", main_activity.current_user_id);
                return true;
            }
        });

        mySwipe = (SwipeRefreshLayout) rootview.findViewById(R.id.swiperfresh);
        mySwipe.setOnRefreshListener(this);





        return rootview;

    }

    public void onRefresh() {
        refresh = true;
        new LoadFollowingUsers().execute(main_activity.current_user_id);

    }

    /*
    @Override
    public void onClick(View v)
    {
        Log.d("Searching", "Doing Stuff");
        new SearchUsers().execute(search_box.getQuery().toString());

    }
    */

    class LoadFollowingUsers extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            if(refresh == false) {
                pdialog = new ProgressDialog(getActivity());
                pdialog.setMessage("Loading Following Users. Please wait...");
                pdialog.setIndeterminate(false);
                pdialog.setCancelable(false);
                pdialog.show();
            }

        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            params.add(new BasicNameValuePair("current_user_id", args[0]));
            JSONObject json = main_activity.jsonParser.makeHttpRequest(main_activity.url_get_following, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("Following Users: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(main_activity.TAG_SUCCESS);
                main_activity.arrayOfBroadcasts.clear();
                main_activity.pid_list.clear();

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    main_activity.products = json.getJSONArray(main_activity.TAG_PRODUCTS);


                    // looping through All Products
                    for (int i = 0; i < main_activity.products.length(); i++) {
                        JSONObject c = main_activity.products.getJSONObject(i);
                        boolean exists = false;

                        // Storing each json item in variable
                        String id = c.getString("guppy_id");
                        String name = c.getString(main_activity.TAG_NAME);
                        String song = c.getString("song");
                        String artist = c.getString("artist");
                        //String listeners = c.getString("listeners");
                        //int real_listeners = Integer.parseInt(listeners);


                        main_activity.arrayOfBroadcasts.add(new Broadcast(name, 0, song, artist, "guppy"));
                        main_activity.pid_list.add(i, id);
                        main_activity.name_list.add(i, name);
                        Log.d("array of broadcasts", main_activity.arrayOfBroadcasts.toString());



                        System.out.println(name);

                        for (String str: main_activity.listDataHeader){
                            if(str.trim().contains(name))
                                exists = true;
                        }
                        if(!exists) {
                            main_activity.listDataHeader.add(name);
                            main_activity.listDataChild.put(name, main_activity.user_options);
                            main_activity.pid_list.add(id);
                        }



                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products

            // updating UI from Background Thread
            main_activity.runOnUiThread(new Runnable() {
                public void run() {




                    main_activity.adapter = new BroadcastAdapter(main_activity.getApplicationContext(), main_activity.arrayOfBroadcasts);
                    main_activity.lv.setAdapter(main_activity.adapter);
                    main_activity.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String temp = main_activity.pid_list.get(position);
                            if(temp.equals(main_activity.current_following_id) == false) {
                                main_activity.current_following_id = temp;
                                main_activity.current_following_name = main_activity.name_list.get(position);
                                new Increment_Listeners().execute(main_activity.current_following_id, main_activity.current_user_id);
                                new Get_Following_Or_Nah().execute();
                                search_box.clearFocus();
                                main_activity.TuneIn();
                            }

                            else{
                                android.support.v4.app.FragmentTransaction fragmentTransaction = main_activity.fm.beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_container, main_activity.tuning_in_fragment);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }
                        }
                    });


                }
            });

            if(refresh)
            {
                mySwipe.setRefreshing(false);
                refresh = false;
            }
            else {
                pdialog.dismiss();
            }

        }

    }

    class SearchUsers extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * getting All products from url
         * */
        protected String doInBackground(String... args) {
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            params.add(new BasicNameValuePair("search_term", args[0]));
            params.add(new BasicNameValuePair("current_user_id", args[1]));
            JSONObject json = main_activity.jsonParser.makeHttpRequest(main_activity.url_search_users, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("Searched Users: ", json.toString());

            try {
                // Checking for SUCCESS TAG
                int success = json.getInt(main_activity.TAG_SUCCESS);
                main_activity.arrayOfBroadcasts.clear();
                main_activity.pid_list.clear();

                if (success == 1) {
                    // products found
                    // Getting Array of Products
                    main_activity.products = json.getJSONArray(main_activity.TAG_PRODUCTS);


                    // looping through All Products
                    for (int i = 0; i < main_activity.products.length(); i++) {
                        JSONObject c = main_activity.products.getJSONObject(i);
                        boolean exists = false;

                        // Storing each json item in variable
                        String id = c.getString("guppy_id");
                        String name = c.getString("name");
                        String song = c.getString("song");
                        String artist = c.getString("artist");
                        //String listeners = c.getString("listeners");
                        //int real_listeners = Integer.parseInt(listeners);


                        main_activity.arrayOfBroadcasts.add(new Broadcast(name, 0, song, artist, "guppy"));
                        main_activity.pid_list.add(i, id);
                        main_activity.name_list.add(i, name);
                        Log.d("array of broadcasts", main_activity.arrayOfBroadcasts.toString());



                        System.out.println(name);

                        for (String str: main_activity.listDataHeader){
                            if(str.trim().contains(name))
                                exists = true;
                        }
                        if(!exists) {
                            main_activity.listDataHeader.add(name);
                            main_activity.listDataChild.put(name, main_activity.user_options);
                            main_activity.pid_list.add(id);
                        }



                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after getting all products

            // updating UI from Background Thread
            main_activity.runOnUiThread(new Runnable() {
                public void run() {

                    main_activity.adapter = new BroadcastAdapter(main_activity.getApplicationContext(), main_activity.arrayOfBroadcasts);
                    main_activity.lv.setAdapter(main_activity.adapter);
                    main_activity.lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String temp = main_activity.pid_list.get(position);
                            if(temp.equals(main_activity.current_following_id) == false) {
                                main_activity.current_following_id = temp;
                                main_activity.current_following_name = main_activity.name_list.get(position);
                                new Increment_Listeners().execute(main_activity.current_following_id, main_activity.current_user_id);
                                new Get_Following_Or_Nah().execute();
                                search_box.clearFocus();
                                main_activity.TuneIn();
                            }

                            else{
                                search_box.clearFocus();
                                android.support.v4.app.FragmentTransaction fragmentTransaction = main_activity.fm.beginTransaction();
                                fragmentTransaction.replace(R.id.fragment_container, main_activity.tuning_in_fragment);
                                fragmentTransaction.addToBackStack(null);
                                fragmentTransaction.commit();
                            }
                        }
                    });


                }
            });



        }

    }

    class Increment_Listeners extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        /**
         * Creating product
         */
        protected String doInBackground(String... args) {


            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id", args[0]));
            params.add(new BasicNameValuePair("listener_id", args[1]));



            // getting JSON Object
            // Note that create product url accepts POST method

            JSONObject json = main_activity.jsonParser.makeHttpRequest(main_activity.url_increment_listeners,
                    "POST", params);

            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(main_activity.TAG_SUCCESS);

                if (success == 1) {

                } else {
                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done


        }
    }

    class Get_Following_Or_Nah extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pdialog = new ProgressDialog(getActivity());
            pdialog.setMessage("Loading" + main_activity.current_following_name + ". Please wait...");
            pdialog.setIndeterminate(false);
            pdialog.setCancelable(false);
            pdialog.show();
        }

        /**
         * Getting product details in background thread
         * */
        protected String doInBackground(String... args) {


            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("current_user_id", main_activity.current_user_id));
            params.add(new BasicNameValuePair("current_following_id", main_activity.current_following_id));


            Log.d("PID", main_activity.current_following_id);

            // getting product details by making HTTP request
            // Note that product details url will use GET request
            JSONObject json = main_activity.jsonParser.makeHttpRequest(
                    main_activity.url_get_following_or_nah, "GET", params);
            try {

                int success = json.getInt(main_activity.TAG_SUCCESS);
                if (success == 1) {
                    // successfully received product details
                    JSONArray productObj = json
                            .getJSONArray("user"); // JSON Array

                    // get first product object from JSON Array
                    JSONObject product = productObj.getJSONObject(0);
                    String result = product.getString("result");
                    if (result.equals("true"))
                    {
                        main_activity.following_or_nah = true;
                    }
                    else
                    {
                        main_activity.following_or_nah = false;
                    }
                    Log.d("Following Or Nah", result);

                }else{
                    // product with pid not found
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }


        /**
         * After completing background task Dismiss the progress dialog
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details
            pdialog.dismiss();

        }
    }




    public class BroadcastAdapter extends ArrayAdapter<Broadcast> {

        private final int VIEW_TYPE_SELECTED = 1;
        private final int VIEW_TYPE_NORMAL = 0;

        public BroadcastAdapter(Context context, ArrayList<Broadcast> broadcasts)
        {
            super(context, 0, broadcasts);
        }

        @Override
        public int getViewTypeCount()
        {
            return 2;
        }

        @Override
        public int getItemViewType(int position)
        {
            return (position == 1) ? VIEW_TYPE_SELECTED : VIEW_TYPE_NORMAL;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            Broadcast mBroadcast = getItem(position);
            int viewType = getItemViewType(position);
            int layoutId = -1;
            if (convertView == null)
            {
                //if (viewType == VIEW_TYPE_SELECTED)
                //{
                //	layoutId = R.layout.list_item_selected;
                //}
                //else
                //{
                layoutId = R.layout.list_item;
                //}
                convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
            }
            TextView userTextView = (TextView) convertView.findViewById(R.id.name);
            TextView listenersTextView = (TextView) convertView.findViewById(R.id.text_view_listeners);
            TextView songArtistTextView = (TextView) convertView.findViewById(R.id.song_text_view);
            ImageView profilePicImage = (ImageView) convertView.findViewById(R.id.list_item_image);

            int resId = getResources().getIdentifier(mBroadcast.image_title, "drawable", getActivity().getPackageName());
            profilePicImage.setImageResource(resId);

            userTextView.setText(mBroadcast.username);
            String listeners = mBroadcast.listeners + " Listeners";
            listenersTextView.setText(listeners);
            String songAndArtist = mBroadcast.song + " - " + mBroadcast.artist;
            songArtistTextView.setText(songAndArtist);
            return convertView;
        }

    }
}
