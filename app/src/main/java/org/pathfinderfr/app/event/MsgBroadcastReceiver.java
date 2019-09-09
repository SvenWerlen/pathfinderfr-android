package org.pathfinderfr.app.event;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterImportExport;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.PreferenceUtil;

import java.util.List;

public class MsgBroadcastReceiver extends BroadcastReceiver {

    private Activity activity;

    public MsgBroadcastReceiver(Activity parent) {
        this.activity = parent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Log.i(MsgBroadcastReceiver.class.getSimpleName(), "Processing with message ...");

            String character=intent.getStringExtra("character");
            String uuid=intent.getStringExtra("uuid");
            String sender=intent.getStringExtra("sender");

            if(PreferenceUtil.getApplicationUUID(context).equals(sender)) {
                Log.i(MsgBroadcastReceiver.class.getSimpleName(), "Ignoring self-sent message!");
                return;
            }

            Pair<Character, List<Integer>> result = CharacterImportExport.importCharacterAsYML(character, activity.getCurrentFocus());

            long id = DBHelper.getInstance(context).fetchCharacterIdByUUID(uuid);
            if(id > 0) {
                result.first.setId(id);
                result.first.setUniqID(uuid);
                DBHelper.getInstance(context).updateEntity(result.first);

                if(result.second.size() == 0) {
                    Log.i(MsgBroadcastReceiver.class.getSimpleName(), "Character updated!");
                    Toast.makeText(activity, context.getResources().getString(R.string.sync_character_updated), Toast.LENGTH_LONG).show();
                } else {
                    Log.i(MsgBroadcastReceiver.class.getSimpleName(), "Character updated with errors!");
                    Toast.makeText(activity, context.getResources().getString(R.string.sync_character_updated_errors), Toast.LENGTH_LONG).show();
                }
            }
            else {
                Log.w(MsgBroadcastReceiver.class.getSimpleName(), "Trying to update non-existing character: " + uuid);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
