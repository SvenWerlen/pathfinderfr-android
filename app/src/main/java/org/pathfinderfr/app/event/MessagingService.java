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

    private static final String MESSAGE_TITLE      = "title";
    private static final String MESSAGE_CONTINUOUS = "c";
    private static final String MESSAGE_CONTENT    = "message";
    private static final String MESSAGE_ID_SYNC    = "character-sync";
    private Map<String, String> data;

    public MessagingService() {
        data = new HashMap<>();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            //Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            Map<String, String> data = remoteMessage.getData();
            if(!data.containsKey(MESSAGE_TITLE)) {
               return;
            }

            // sync character
            if(MESSAGE_ID_SYNC.equals(data.get(MESSAGE_TITLE))) {
                String continous = data.containsKey(MESSAGE_CONTINUOUS) ? data.get(MESSAGE_CONTINUOUS) : null;
                String charString = data.get(MESSAGE_CONTENT);

                if(continous != null) {
                    if(this.data.containsKey(remoteMessage.getFrom())) {
                        charString = this.data.get(remoteMessage.getFrom()) + charString;
                    }
                    // more messages expected?
                    if(Boolean.toString(true).equals(continous)) {
                        this.data.put(remoteMessage.getFrom(), charString);
                        return;
                    } else {
                        this.data.remove(remoteMessage.getFrom());
                    }
                }

                LocalBroadcastManager broadcaster = LocalBroadcastManager.getInstance(getBaseContext());
                Intent intent = new Intent(REQUEST_ACCEPT);
                intent.putExtra("character", charString);
                broadcaster.sendBroadcast(intent);
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }


    }

    @Override
    public void onDeletedMessages() {
        System.out.println("DELETED!!!");
    }
}
