package org.pathfinderfr.app.character;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.itextpdf.io.image.ImageData;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.util.CharacterPDF;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.StringUtil;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

public class GeneratePDFTask extends AsyncTask<GeneratePDFTask.Input, Void, Void> {

    public interface IDataUI {
        void onProgressCompleted();
        void onError(String errorMessage);
    };

    static class Input {
        Character character;
        List<DBEntity> skills;
        ImageData logo;
        FileOutputStream stream;
        CharacterPDF.Options options;
    };

    private IDataUI caller;
    private String errorMessage;
    private Properties props;

    GeneratePDFTask(@NonNull IDataUI caller) {
        this.caller = caller;
        this.errorMessage = null;
        this.props = ConfigurationUtil.getInstance().getProperties();
    }

    @Override
    protected Void doInBackground(Input... inputs) {
        DBHelper dbHelper = DBHelper.getInstance(null);

        // supports only 1 character!
        Input input = inputs.length > 0 ? inputs[0] : null;

        if(input == null) {
            errorMessage = props.getProperty("generatepdf.error.invalidcharacter");
            return null;
        }

        // assuming that all inputs are properly filled
        if(input.character == null || input.skills == null || input.stream == null || input.logo == null) {
            errorMessage = props.getProperty("generatepdf.error.invalidinput");
            return null;
        }

        // save character to cache directory
        try {
            new CharacterPDF(input.options, input.character, input.skills, input.character.getInventoryWeapons(), input.character.getInventoryArmors()).generatePDF(input.stream, input.logo);
        } catch (Throwable t) {
            Log.w(GeneratePDFTask.class.getSimpleName(), "Error during PDF generation", t);
            errorMessage = StringUtil.getStackTrace(t);
            return null;
        } finally {
            try {
                input.stream.close();
            } catch(Exception e) {}
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void none) {
        if(errorMessage == null) {
            caller.onProgressCompleted();
        } else {
            caller.onError(errorMessage);
        }
    }

    @Override
    protected void onCancelled(Void none) {
        caller.onProgressCompleted();
    }

}
