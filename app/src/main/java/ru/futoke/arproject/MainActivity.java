package ru.futoke.arproject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import ru.futoke.arproject.renderer.ArActivity;
import ru.futoke.arproject.renderer.R;


public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "MainActivity";
    Handler updateBarHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updateBarHandler = new Handler();

//        Utils.downloadModelsList(MainActivity.this);

        ArrayList<HashMap<String, String>> models = Utils
            .getModelsList(MainActivity.this);

        if (models != null) {
            createModelsList(models);
        }

//        Ion.with(MainActivity.this)
//            .load("POST", "http://ar.futoke.ru/load_models_list")
//            .setBodyParameter("username", "admin")
//            .setBodyParameter("password", "admin")
//            .asJsonArray()
//            .setCallback(new FutureCallback<JsonArray>() {
//                @Override
//                public void onCompleted(Exception e, JsonArray result) {
//                    List<String> titles = new ArrayList<>();
//                    List<String> images = new ArrayList<>();
//                    final List<String> ids = new ArrayList<>();
//
//                    for (int i = 0; i < result.size(); i++) {
//                        titles.add(
//                            result.get(i)
//                                .getAsJsonObject()
//                                .get("name")
//                                .getAsString()
//                        );
//                        images.add(
//                            result.get(i)
//                                .getAsJsonObject()
//                                .get("preview")
//                                .getAsJsonObject()
//                                .get("content")
//                                .getAsString()
//                        );
//                        ids.add(
//                            result.get(i)
//                                .getAsJsonObject()
//                                .get("id")
//                                .getAsString()
//                        );
//
//                    }
//                    ListAdapter simpleAdapter = new ListAdapter(
//                        getBaseContext(),
//                        titles,
//                        images
//                    );
//                    ListView androidListView = (ListView) findViewById(R.id.list_view);
//                    androidListView.setAdapter(simpleAdapter);
//                    androidListView.setOnItemClickListener(
//                        new AdapterView.OnItemClickListener()
//                    {
//                        @Override
//                        public void onItemClick(
//                            AdapterView<?> parent,
//                            View view,
//                            int position,
//                            long id)
//                        {
//                            if (isConnectingToInternet()) {
//                                String url =
//                                    "http://ar.futoke.ru/load_model_file/"
//                                        + ids.get(position);
//                                downloadModel(url);
//                            } else {
//                                Toast.makeText(
//                                    MainActivity.this,
//                                    R.string.no_internet,
//                                    Toast.LENGTH_SHORT
//                                ).show();
//                            }
//                        }
//                    });
//                }
//            });
    }


    private void createModelsList(final ArrayList<HashMap<String, String>> models)
    {
        ListAdapter simpleAdapter = new ListAdapter(
            MainActivity.this,
            models
        );

        ListView androidListView = (ListView) findViewById(R.id.list_view);
        androidListView.setAdapter(simpleAdapter);
        androidListView.setOnItemClickListener(
            new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id)
                {
                    String modelId = models.get(position).get("id");
                    File modelDir = Utils.getDir(Utils.workDir + "/" + modelId);

                    if (Utils.isDirEmpty(modelDir)) {
                        if (Utils.isConnectingToInternet(MainActivity.this)) {
                            downloadModel(modelId);
                        } else {
                            Toast.makeText(
                                MainActivity.this,
                                R.string.no_internet,
                                Toast.LENGTH_SHORT
                            ).show();
                        }
                    } else {
                        // Open Activity
                    }
                }
            });
    }

    private void downloadModel(final String modelId)
    {
        final String ARCHIVE = "archive.zip";
        final ProgressDialog ringProgressDialog = ProgressDialog.show(
            MainActivity.this,
            "Please wait ...",
            "Downloading a model ...",
            true
        );

        ringProgressDialog.setCancelable(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    try {
//                        File downloadDir = null;
//                        File outputFile = null;

                        HashMap<String, String> params = new HashMap<>();
                        params.put("username", Utils.login);
                        params.put("password", Utils.password);

                        StringBuilder sbParams = new StringBuilder();
                        String charset = "UTF-8";

                        int i = 0;
                        for (String key : params.keySet()) {
                            try {
                                if (i != 0) {
                                    sbParams.append("&");
                                }
                                sbParams.append(key).append("=")
                                    .append(URLEncoder
                                        .encode(params.get(key), charset));

                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            i++;
                        }

                        // Create Download URL.
                        URL url = new URL(Utils.mainUrl
                            + "/"
                            + "load_model_file"
                            + "/"
                            + modelId);
                        // Open Url Connection.
                        HttpURLConnection c = (HttpURLConnection) url
                            .openConnection();
                        c.setReadTimeout(10000);
                        c.setConnectTimeout(15000);
                        c.setRequestMethod("POST");
                        c.setRequestProperty("Accept-Charset", charset);
                        c.setDoInput(true);
                        c.setDoOutput(true);

                        // Connect the URL Connection.
                        c.connect();

                        String paramsString = sbParams.toString();

                        DataOutputStream wr = new DataOutputStream(
                            c.getOutputStream()
                        );
                        wr.writeBytes(paramsString);
                        wr.flush();
                        wr.close();

                        // If Connection response is not OK then show Logs.
                        if (c.getResponseCode() != HttpURLConnection.HTTP_OK) {
                            Log.e(
                                TAG,
                                "Server returned HTTP "
                                    + c.getResponseCode()
                                    + " "
                                    + c.getResponseMessage()
                            );
                        }
                        File downloadDir = Utils.getDir(
                            Environment.getExternalStorageDirectory()
                                + "/"
                                + Utils.downloadDir
                        );
                        Utils.cleanDir(downloadDir);

                        File outputFile = new File(downloadDir, ARCHIVE);

                        if (!outputFile.exists()) {
                            outputFile.createNewFile();
                            Log.e(TAG, "The archive file has been created.");
                        }

                        FileOutputStream fos = new FileOutputStream(outputFile);
                        InputStream is = c.getInputStream();

                        byte[] buffer = new byte[1024]; // Set buffer type.
                        int length = 0; // Init length
                        while ((length = is.read(buffer)) != -1) {
                            fos.write(buffer, 0, length); // Write a new file.
                        }
                        fos.close();
                        is.close();

                        String source =
                            Environment.getExternalStorageDirectory()
                                + "/"
                                + Utils.downloadDir
                                + "/"
                                + ARCHIVE;
                        String destination =
                            Environment.getExternalStorageDirectory()
                                + "/"
                                + Utils.workDir
                                + "/"
                                + modelId;

                        File modelDir = Utils.getDir(destination);
                        Utils.cleanDir(modelDir);

                        try {
                            ZipFile zipFile = new ZipFile(source);
                            zipFile.extractAll(destination);
                        } catch (ZipException e) {
                            e.printStackTrace();
                        }
//                        startArActivity(modelId);
                    } catch (Exception e) {
                        Log.e(TAG, e.getLocalizedMessage());
                    }
                } catch (Exception e) {
                    Log.e(TAG, e.getLocalizedMessage());
                }
                ringProgressDialog.dismiss();
            }
        }).start();
    }

    private void startArActivity(String modelId)
    {
        Intent intent = new Intent(MainActivity.this, ArActivity.class);
        intent.putExtra("MODEL_ID", modelId);
        startActivity(intent);
    }
}