package ru.futoke.arproject;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ru.futoke.arproject.renderer.R;

/**
 * Created by ichiro on 29.03.2017.
 */

public class Utils {

    private static final String TAG = "Utils";

    public static final String downloadDir = "AR-Download";
    public static final String workDir = "AR-Project";
    public static final String modelsListFile = "models.json";
    public static final String login = "admin";
    public static final String password = "admin";
    public static final String mainUrl = "http://ar.futoke.ru";

    static void downloadModelsList(final Context context)
    {
        if (isConnectingToInternet(context)) {
            Ion.with(context)
                .load("POST", mainUrl + "/" + "load_models_list")
                .setBodyParameter("username", login)
                .setBodyParameter("password", password)
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        Writer output;
                        File wDir = getDir(workDir);

                        if (wDir != null) {
                            File file = new File(wDir, modelsListFile);
                            try {
                                output = new BufferedWriter(new FileWriter(file));
                                output.write(result.toString());
                                output.close();

                            } catch (IOException ioe) {
                                Log.e(TAG, ioe.getLocalizedMessage());
                            }
                        } else {
                            Log.e(TAG, "Can not create file " + modelsListFile);
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


    public static ArrayList<HashMap<String, String>> getModelsList(Context context)
    {
        ArrayList<HashMap<String, String>> modelsList = new ArrayList<>();
        File wDir = getDir(workDir);

        if (wDir != null) {
            try {
                File file = new File(wDir, modelsListFile);
                FileInputStream stream = new FileInputStream(file);
                String jsonStr = null;

                try {
                    FileChannel fc = stream.getChannel();
                    MappedByteBuffer bb = fc.map(
                            FileChannel.MapMode.READ_ONLY,
                            0,
                            fc.size()
                    );
                    jsonStr = Charset.defaultCharset().decode(bb).toString();
                }
                catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(
                        context,
                        "Can not open models file.",
                        Toast.LENGTH_SHORT
                    ).show();
                }
                finally {
                    stream.close();
                }

                Gson gson = new Gson();
                JsonArray data = gson.fromJson(jsonStr, JsonArray.class);

                for (int i = 0; i < data.size(); i++) {
                    String id = data.get(i)
                        .getAsJsonObject()
                        .get("id")
                        .getAsString();

                    String name = data.get(i)
                        .getAsJsonObject()
                        .get("name")
                        .getAsString();

                    String img = data.get(i)
                            .getAsJsonObject()
                            .get("preview")
                            .getAsJsonObject()
                            .get("content")
                            .getAsString();

                    HashMap<String, String> model = new HashMap<>();

                    model.put("id", id);
                    model.put("name", name);
                    model.put("img", img);

                    modelsList.add(model);
                }

                return modelsList;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(
                    context,
                    R.string.no_list_file,
                    Toast.LENGTH_SHORT
                ).show();
            }
        } else {
            Toast.makeText(
                context,
                R.string.no_list_file,
                Toast.LENGTH_SHORT
            ).show();
        }
        return null;
    }


    public static File getDir(String dirName)
    {
        if (isSDCardPresent()) {
            File dir = new File(
                Environment.getExternalStorageDirectory()
                    + "/"
                    + dirName);
            if (!dir.exists()) {
                dir.mkdirs(); // TODO: Custom handler here!
                Log.e(TAG, "The directory " + dirName + " has been created.");
            }
            return dir;
        } else {
            Log.e(TAG, "There is no SD Card.");
            return null;
        }
    }


    public static void cleanDir(File dir)
    {
        if (dir.exists()) {
            try {
                FileUtils.cleanDirectory(dir);
            } catch (IOException ioe) {
                Log.e("NYAAA", ioe.getLocalizedMessage());
            }
        }
    }

    public static boolean isSDCardPresent() {
        return Environment.getExternalStorageState().equals(
            Environment.MEDIA_MOUNTED
        );
    }


    public static boolean isConnectingToInternet(Context context)
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
            .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }


    public static boolean isDirEmpty(File dir)
    {
        File[] contents = dir.listFiles();
        return contents.length == 0;
    }

    public static String getFileByExtension(String ext, File dir)
    {
        String[] extensions = new String[] { ext };
        List<File> files = (List<File>) FileUtils
            .listFiles(dir, extensions, true);
        return files.get(0).getName();
    }
}
