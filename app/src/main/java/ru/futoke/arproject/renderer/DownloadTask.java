package ru.futoke.arproject.renderer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import org.apache.commons.io.FileUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

public class DownloadTask {

    private static final String TAG = "download_task";
    private static final String ARCHIVE = "archive.zip";
    private Context context;
    private String downloadUrl = "";

    public DownloadTask(Context context, String downloadUrl)
    {
        this.context = context;
        this.downloadUrl = downloadUrl;
        Log.e(TAG, downloadUrl);

        // Start Downloading Task.
        new DownloadingTask().execute();
    }

    private class DownloadingTask extends AsyncTask<Void, Void, Void>
    {
        File downloadDir = null;
        File outputFile = null;

        @Override
        protected Void doInBackground(Void... arg0)
        {
            try {
                HashMap<String, String> params = new HashMap<>();
                params.put("username", "admin");
                params.put("password", "admin");

                StringBuilder sbParams = new StringBuilder();
                String charset = "UTF-8";

                int i = 0;
                for (String key : params.keySet()) {
                    try {
                        if (i != 0) {
                            sbParams.append("&");
                        }
                        sbParams.append(key).append("=")
                            .append(URLEncoder.encode(params.get(key), charset));

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    i++;
                }

                // Create Download URL.
                URL url = new URL(downloadUrl);
                // Open Url Connection.
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setReadTimeout(10000);
                c.setConnectTimeout(15000);
                c.setRequestMethod("POST");
                c.setRequestProperty("Accept-Charset", charset);
                c.setDoInput(true);
                c.setDoOutput(true);

                // Connect the URL Connection.
                c.connect();

                String paramsString = sbParams.toString();

                DataOutputStream wr = new DataOutputStream(c.getOutputStream());
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

                // Get File if SD card is present.
                if (new CheckForSDCard().isSDCardPresent()) {
                    downloadDir = new File(
                        Environment.getExternalStorageDirectory()
                            + "/"
                            + Utils.downloadDirectory);
                } else {
                    Toast.makeText(
                            context,
                            "Oops!! There is no SD Card.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
                // If File is not present create directory.
                if (!downloadDir.exists()) {
                    downloadDir.mkdir();
                    Log.e(TAG, "Download directory created.");
                }
                // Create Output file in Main File/
                outputFile = new File(downloadDir, ARCHIVE);

                // Create New File if not present.
                if (!outputFile.exists()) {
                    outputFile.createNewFile();
                    Log.e(TAG, "Archive file Created");
                }

                // Get OutputStream for NewFile Location.
                FileOutputStream fos = new FileOutputStream(outputFile);
                // Get InputStream for connection.
                InputStream is = c.getInputStream();

                byte[] buffer = new byte[1024]; // Set buffer type.
                int length = 0; // Init length
                while ((length = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, length); // Write a new file.
                }

                // Close all connection after doing task.
                fos.close();
                is.close();

                String source =
                    Environment.getExternalStorageDirectory()
                        + "/"
                        + Utils.downloadDirectory
                        + "/"
                        + ARCHIVE;
                String destination =
                    Environment.getExternalStorageDirectory()
                        + "/"
                        + Utils.workDirectory;

                FileUtils.cleanDirectory(new File(destination));
                try {
                    ZipFile zipFile = new ZipFile(source);
                    zipFile.extractAll(destination);
                } catch (ZipException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                // Read exception if something went wrong.
                e.printStackTrace();
                outputFile = null;
                Log.e(TAG, "Download Error Exception " + e.getMessage());
            }
            return null;
        }
    }
}
