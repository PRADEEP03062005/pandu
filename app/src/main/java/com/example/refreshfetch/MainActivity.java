package com.example.refreshfetch;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private TextView dataTextView;
    private final String CHANNEL_ID = "2826969";
    private final String API_KEY = "YTLX4OYWXOHL79FOuh";
    private Handler handler;
    private Runnable runnable;
    private final int UPDATE_INTERVAL = 5000;  // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataTextView = findViewById(R.id.dataTextView);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Fetch data from ThingSpeak
                new FetchDataTask().execute();
                // Schedule the next update
                handler.postDelayed(this, UPDATE_INTERVAL);
            }
        };

        // Start the auto-update
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop the handler when the activity is destroyed
        handler.removeCallbacks(runnable);
    }

    private class FetchDataTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            String result = "";
            try {
                String urlString = "https://api.thingspeak.com/channels/" + CHANNEL_ID +
                        "/feeds.json?api_key=" + API_KEY + "&results=2";
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(urlConnection.getInputStream()));

                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }

                    // Convert content to JSON
                    JSONObject jsonObject = new JSONObject(content.toString());
                    JSONArray feeds = jsonObject.getJSONArray("feeds");

                    // Extract the data you need
                    if (feeds.length() > 0) {
                        JSONObject feed = feeds.getJSONObject(0);
                        String field1 = feed.getString("field1");
                        result = "Field1: " + field1;
                    }

                    in.close();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                result = "Error fetching data";
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // Update the UI with the result
            dataTextView.setText(result);
        }
    }
}
