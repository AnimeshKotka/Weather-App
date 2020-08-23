package animesh.com.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView results;
    EditText city;
    ImageView icon;
    ConstraintLayout cl;
    //https://api.openweathermap.org/data/2.5/weather?q=Paris&appid=c18012423cf4e5b5345d36c5f55e7933
    //http://openweathermap.org/img/wn/01n@2x.png
    String baseURL = "https://api.openweathermap.org/data/2.5/weather?q=";
    String API = "&appid=9654c040f20327697e85f4c795dc589b";
    String icon_url = "http://openweathermap.org/img/wn/";


    RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        results = findViewById(R.id.result);
        button = findViewById(R.id.button);
        city = findViewById(R.id.getCity);
        icon = findViewById(R.id.iconView);
        cl = findViewById(R.id.BigB);
        icon.setVisibility(View.INVISIBLE);

        requestQueue = MySingleton.getInstance(this).getRequestQueue();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (city.getText().toString().equals("")) {
                    city.setError("Please enter your city");
                }
                    fetchDataUsingVolley();
                //new getIcon().execute();

            }
        });


    }

    private void fetchDataUsingVolley() {
        String cname = city.getText().toString();
        String Myurl = baseURL + cname + API;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, Myurl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("weather");
                    JSONObject jsonObject = jsonArray.getJSONObject(0);
                    String desc = jsonObject.getString("description");
                    String icon_str = jsonObject.getString("icon");
                    changeBackground(jsonObject.getString("main"));
                    Log.i("My Josn data",response.toString());
                    results.setText(desc);
                    String urlmain=icon_url + icon_str + "@2x.png";
                    Bitmap bitmap = getBitmap(urlmain);
                    //Log.i("BitMap@@@",String.valueOf(bitmap.getWidth()));
                    icon.setImageBitmap(bitmap);
                    //icon.setVisibility(View.VISIBLE);
                    new DownloadImageTask(icon).execute(urlmain);

                    Log.i("icon",icon_url + icon_str + "@2x.png");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        MySingleton.getInstance(MainActivity.this).addToRequestQue(request);
    }


    private void changeBackground(String main) {
        switch (main){
            case "Clear" :
                cl.setBackground(getResources().getDrawable(R.drawable.clearsky));
                break;
            case "Rain" :
                cl.setBackground(getResources().getDrawable(R.drawable.rain));
                break;
            case "Haze":
                cl.setBackground(getResources().getDrawable(R.drawable.haze));
                break;
            case "Fog":
                cl.setBackground(getResources().getDrawable(R.drawable.fogg));
                break;
                default:
                    cl.setBackground(getResources().getDrawable(R.drawable.winter));
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }




    /////***************************************************************************************
    private class getIcon extends AsyncTask<Void, Void, String>
    {
        Context context= MainActivity.this;
        ProgressDialog pd = new ProgressDialog(context);
        @Override
        protected void onPreExecute() {
            pd.setMessage("Loading...");
            pd.show();
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONArray jsonArray = new JSONArray(s);
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                String desc = jsonObject.getString("description");
                String icon_str = jsonObject.getString("icon");
                changeBackground(jsonObject.getString("main"));
                Log.i("My Josn data",s);
                results.setText(desc);
                pd.dismiss();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(Void... voids) {
            String cname = city.getText().toString();
            String Myurl = baseURL + cname + API;
            try {
                URL url = new URL(Myurl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line=reader.readLine())!=null)
                        sb.append(line).append("\n");

                    return sb.toString();
                } finally {
                    connection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }
    }

    public static Bitmap getBitmap(String url) {
        try {
            URL url1 = new URL(url);//
            HttpURLConnection connection = (HttpURLConnection) url1.openConnection();
            InputStream is = connection.getInputStream();
            Bitmap mybitmap = BitmapFactory.decodeStream(is);
            connection.disconnect();
            is.close();
            return  mybitmap;
        } catch (Exception e) {
            return null;
        }
    }
}
