package org.pathfinderfr.app.event;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = MessagingService.class.getSimpleName();
    public static final String REQUEST_ACCEPT = "reqacc";

    private static final String MESSAGE_UUID       = "uuid";
    private static final String MESSAGE_SENDER     = "sender";
    private static final String MESSAGE_TITLE      = "title";
    private static final String MESSAGE_MULTI_IDX  = "idx";
    private static final String MESSAGE_CONTENT    = "message";
    private static final String MESSAGE_ID_SYNC    = "character-sync";

    private Map<String, Multipart> data;

    private static class Multipart {
        private Map<Integer, String> parts = new HashMap<>();
        public void add(int idx, String part) {
            parts.put(idx, part);
        }
        // returns true if multipart message is complete
        public boolean isComplete() {
            int idx = 1;
            while(true) {
                if(parts.containsKey(-idx)) {
                    return true;
                } else if(!parts.containsKey(idx)) {
                    return false;
                }
                idx++;
            }
        }
        // return full message
        public String getMessage() {
            int idx = 1;
            StringBuffer buf = new StringBuffer();
            while(true) {
                if(parts.containsKey(-idx)) {
                    buf.append(parts.get(-idx));
                    return buf.toString();
                } else if(!parts.containsKey(idx)) {
                    return null;
                }
                buf.append(parts.get(idx));
                idx++;
            }
        }
    }

    public MessagingService() {
        data = new HashMap<>();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Map<String, String> data = remoteMessage.getData();
            if(!data.containsKey(MESSAGE_TITLE) || !data.containsKey(MESSAGE_SENDER)) {
               return;
            }

            // sync character
            if(MESSAGE_ID_SYNC.equals(data.get(MESSAGE_TITLE))) {
                String uuid = data.containsKey(MESSAGE_UUID) ? data.get(MESSAGE_UUID) : null;
                String multipartIdx = data.containsKey(MESSAGE_MULTI_IDX) ? data.get(MESSAGE_MULTI_IDX) : null;
                String charString = data.get(MESSAGE_CONTENT);

                if(uuid == null) {
                    Log.w(MessagingService.class.getSimpleName(), "Trying to sync character without specifying GUID!");
                    return;
                }

                Log.d(TAG, "Multipart " + multipartIdx);

                if(multipartIdx != null) {
                    String sender = remoteMessage.getFrom() == null ? "--" : remoteMessage.getFrom();
                    // first part received
                    if(!this.data.containsKey(sender)) {
                        this.data.put(sender, new Multipart());
                    }
                    this.data.get(sender).add(Integer.valueOf(multipartIdx), charString);
                    // more messages expected?
                    if(!this.data.get(sender).isComplete()) {
                        return;
                    } else {
                        charString = this.data.get(sender).getMessage();
                        this.data.remove(sender);
                    }
                }

                if(charString != null) {
                    Log.d(TAG, "Notify UI");
                    LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getBaseContext());
                    Intent intent = new Intent(REQUEST_ACCEPT);
                    intent.putExtra("sender", data.get(MESSAGE_SENDER));
                    intent.putExtra("uuid", uuid);
                    intent.putExtra("character", charString);
                    broadcaster.sendBroadcast(intent);
                }
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }


    }

    @Override
    public void onDeletedMessages() {
    }
}
