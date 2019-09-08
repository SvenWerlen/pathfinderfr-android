package org.pathfinderfr.app.event;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.pathfinderfr.app.MainActivity;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterImportExport;
import org.pathfinderfr.app.util.Pair;

import java.util.List;

public class MsgBroadcastReceiver extends BroadcastReceiver {

    private Activity activity;

    public MsgBroadcastReceiver(Activity parent) {
        this.activity = parent;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String character=intent.getStringExtra("character");
            Pair<Character, List<Integer>> result = CharacterImportExport.importCharacterAsYML(character, activity.getCurrentFocus());

            if(result != null && result.second.size() == 0) {
                Toast.makeText(activity, "Personnage mise à jour!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity, "Personnage mise à jour avec erreurs!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
