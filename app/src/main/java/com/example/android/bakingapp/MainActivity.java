package com.example.android.bakingapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.example.android.bakingapp.dummy.DummyContent;

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

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends AppCompatActivity implements RecipeListFragment.OnListFragmentInteractionListener {

    @InjectView(R.id.progress_bar)
    ProgressBar progressBar;
    public static String itemForWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
        new JsonTask().execute("http://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json");
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {
        Intent intent = new Intent(this, RecipeDetails.class);
        intent.putExtra("id", item.id);
        startActivity(intent);
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (progressBar.getVisibility() == View.VISIBLE)
                progressBar.setVisibility(View.GONE);
            RecipeJson.jsonData = result;
            try {
                JSONArray jsonArray = new JSONArray(result);
                JSONObject recipe = jsonArray.getJSONObject(0);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(recipe.getString("name"))
                        .append("\n");
                JSONArray ingredientsArray = recipe.getJSONArray("ingredients");
                for (int index = 0; index < ingredientsArray.length(); index++) {
                    JSONObject jsonObject = ingredientsArray.getJSONObject(index);
                    stringBuilder.append(jsonObject.getString("quantity"))
                            .append(" ")
                            .append(jsonObject.getString("measure"))
                            .append(" ")
                            .append(jsonObject.getString("ingredient"))
                            .append("\n");
                }
                itemForWidget = stringBuilder.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RecipeListFragment recipeListFragment = new RecipeListFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.container, recipeListFragment)
                    .commit();
        }
    }
}
