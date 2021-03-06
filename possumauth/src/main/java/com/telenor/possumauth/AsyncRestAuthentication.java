package com.telenor.possumauth;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.telenor.possumauth.interfaces.IAuthCompleted;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class AsyncRestAuthentication extends AsyncTask<JsonObject, Void, Exception> {
    private final URL url;
    private final String apiKey;
    private String successMessage;
    private String responseMessage;
    private IAuthCompleted listener;

    private static final String tag = AsyncRestAuthentication.class.getName();

    AsyncRestAuthentication(@NonNull String url, @NonNull String apiKey, IAuthCompleted listener) throws MalformedURLException {
        this.url = new URL(url);
        this.apiKey = apiKey;
        this.listener = listener;
    }

    @Override
    protected Exception doInBackground(JsonObject... params) {
        OutputStream os = null;
        InputStream is = null;
        Exception exception = null;
        JsonObject object = params[0];
        Log.i(tag, "AP: -------------------------------------------------------------------------\n");
        for (Map.Entry<String, JsonElement> el :  object.entrySet()) {
            String key = el.getKey();
            if (el.getValue().isJsonArray()) {
                byte[] temp = el.getValue().getAsJsonArray().toString().getBytes();
                int size = (temp.length/1000);
                if (size == 0) {
                    Log.i(tag, "AP: "+key+" -> "+temp.length+"B\n");
                } else {
                    Log.i(tag, "AP: "+key+" -> "+(temp.length/1000)+"KB\n");
                }
            }
        }
        try {
            byte[] data = object.toString().getBytes();
            //// Zip data
            //  ByteArrayOutputStream bos = new ByteArrayOutputStream();
            //  ZipOutputStream zipOut = new ZipOutputStream(bos);
            //  ZipEntry entry = new ZipEntry("data");
            //  zipOut.putNextEntry(entry);
            //  zipOut.write(data);
            //  zipOut.close();
            //  byte[] dataZipped = bos.toByteArray();

            Log.i(tag, "AP: Start connection to auth - uploading:" + (data.length / 1000) + " KB");
            long startTime = System.currentTimeMillis();
//            successMessage = "{\"sensors\": {\"bluetooth\": [{\"name\": \"default\", \"score\": 0, \"status\": 0}], \"gac\": [{\"name\": \"default\", \"score\": 0.8024234771728516, \"status\": 1.0}], \"image\": [{\"name\": \"default\", \"score\": 0, \"status\": 0}], \"network\": [{\"name\": \"default\", \"score\": 0.0, \"status\": \"1.0\"}, {\"name\": \"binary\", \"score\": 0.0, \"status\": \"1.0\"}], \"sound\": [{\"name\": \"classic\", \"score\": 1.0, \"status\": 1.0}]}, \"trustscore\": [{\"name\": \"default\", \"score\": 0.3604846954345703, \"status\": 0}]}";
//            responseMessage = "200 OK";
//            return null;

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("x-api-key", apiKey);
            urlConnection.setConnectTimeout(0); // These timeouts should be removed later
            urlConnection.setReadTimeout(0); // These timeouts should be removed later
            urlConnection.setRequestMethod("POST");

            urlConnection.setFixedLengthStreamingMode(data.length);
            urlConnection.connect();

            os = urlConnection.getOutputStream();
            os.write(data);
            int responseCode = urlConnection.getResponseCode();
            responseMessage = urlConnection.getResponseMessage();
            Log.d(tag, "AP: " + responseCode + " -> " + responseMessage);
            is = urlConnection.getInputStream();
            StringBuilder output = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null)
                output.append(line);
            successMessage = output.toString();
            Log.i(tag, "AP: Received upload - time spent:" + ((System.currentTimeMillis() - startTime) / 1000) + " seconds, bytes uploaded:" + data.length);
        } catch (Exception e) {
            Log.e(tag, "AP: Ex:", e);
            exception = e;
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Log.e(tag, "AP: Failed to close output stream:", e);
                    exception = e;
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(tag, "AP: Failed to close input stream:", e);
                    exception = e;
                }
            }
        }
        return exception;
    }

    @Override
    public void onPostExecute(Exception exception) {
        listener.messageReturned(successMessage, responseMessage, exception);
    }
}