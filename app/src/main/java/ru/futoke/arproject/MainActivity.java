package ru.futoke.arproject;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
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
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        updateBarHandler = new Handler();

        ArrayList<HashMap<String, String>> models = Utils
            .getModelsList(MainActivity.this);

        if (models != null) {
            createModelsList(models);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.reload:

                File wDir = Utils.getDir(Utils.workDir);
                Utils.cleanDir(wDir);

                downloadModelsList(MainActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void downloadModelsList(final Context context)
    {
        if (Utils.isConnectingToInternet(MainActivity.this)) {
            Ion.with(MainActivity.this)
                .load("POST", Utils.mainUrl + "/" + "load_models_list")
                .setBodyParameter("username", Utils.login)
                .setBodyParameter("password", Utils.password)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        Writer output;
                        File wDir = Utils.getDir(Utils.workDir);

                        if (wDir != null) {
                            File file = new File(wDir, Utils.modelsListFile);
                            try {
                                output = new BufferedWriter(new FileWriter(file));
                                output.write(result.toString());
                                output.close();

                            } catch (IOException ioe) {
                                Log.e(TAG, ioe.getLocalizedMessage());
                            } finally {
                                finish();
                                startActivity(getIntent());
                            }
                        } else {
                            Log.e(
                                TAG,
                                "Can not create file " + Utils.modelsListFile
                            );
                        }
                    }
                });
        } else {
            Toast.makeText(
                context,
                R.string.no_internet,
                Toast.LENGTH_SHORT
            ).show();
        }
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
                        startArActivity(modelId);
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

                        // Open URL Connection.
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
                        File downloadDir = Utils.getDir(Utils.downloadDir);
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

                        String source = null;
                        if (downloadDir != null) {
                            source = downloadDir.getPath() + "/" + ARCHIVE;
                        }

                        File destination = Utils.getDir(Utils.workDir
                            + "/"
                            + modelId
                        );
                        Utils.cleanDir(destination);

                        try {
                            ZipFile zipFile = new ZipFile(source);
                            zipFile.extractAll(destination.getPath());
                            Utils.cleanDir(downloadDir);
                        } catch (ZipException e) {
                            e.printStackTrace();
                        } finally {
                            startArActivity(modelId);
                        }
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