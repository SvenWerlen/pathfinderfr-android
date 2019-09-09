package org.pathfinderfr.app.event;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MessageBroker extends AsyncTask<Void, String, Integer> {

    private static final String URL_API = "https://pathfinderfr-android.appspot.com/api/message/";

    public static final String TYPE_SYNC = "character-sync";

    private ISender sender;
    private String senderId;
    private String topic;
    private String messageType;
    private String content;

    public interface ISender {
        void onCompleted(Integer status);
    };

    public MessageBroker(ISender sender, String senderId, String topic, String messageType, String content) {
        this.senderId = senderId;
        this.sender = sender;
        this.topic = topic;
        this.messageType = messageType;
        this.content = content;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(Void... params) {
        String urlString = URL_API;
        OutputStream out = null;
        try {
            JSONObject obj = new JSONObject();
            obj.put("sender", senderId);
            obj.put("topicName", topic);
            obj.put("messageType", messageType);
            obj.put("content", content);

            URL url = new URL(urlString);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setReadTimeout(15000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            out = new BufferedOutputStream(urlConnection.getOutputStream());

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
            writer.write(obj.toString());
            writer.flush();
            writer.close();
            out.close();

            urlConnection.connect();
            int httpCode = urlConnection.getResponseCode();
            urlConnection.disconnect();
            return httpCode;
        } catch (Exception e) {
            Log.e(MessageBroker.class.getSimpleName(), "Error during message sending", e);
            return 500;
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        sender.onCompleted(result);
    }
}
