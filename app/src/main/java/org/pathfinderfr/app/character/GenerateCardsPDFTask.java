package org.pathfinderfr.app.character;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.pdf.CardsPDF;
import org.pathfinderfr.app.pdf.CharacterPDF;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.StringUtil;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Properties;

public class GenerateCardsPDFTask extends AsyncTask<Object, Void, Void> {

    public interface IDataUI {
        void onProgressCompleted();
        void onError(String errorMessage);
    };

    static class Input {
        Character character;
        CardsPDF.Params params;
        FileOutputStream stream;
    };

    private IDataUI caller;
    private String errorMessage;
    private Properties props;

    GenerateCardsPDFTask(@NonNull IDataUI caller) {
        this.caller = caller;
        this.errorMessage = null;
        this.props = ConfigurationUtil.getInstance().getProperties();
    }

    @Override
    protected Void doInBackground(Object... inputs) {
        // supports only 1 character!
        Input input = inputs.length > 0 ? (Input)inputs[0] : null;

        if(input == null) {
            errorMessage = props.getProperty("generatepdf.error.invalidcharacter");
            return null;
        }

        // assuming that all inputs are properly filled
        if(input.character == null) {
            errorMessage = props.getProperty("generatepdf.error.invalidinput");
            return null;
        }

        if(input.character.getSpells().size() == 0) {
            errorMessage = props.getProperty("generatepdf.error.nospell");
            return null;
        }

        // save pdf to cache directory
        try {
            new CardsPDF(input.character.getSpells(), input.params).generatePDF(input.stream);
        } catch (Throwable t) {
            Log.w(GenerateCardsPDFTask.class.getSimpleName(), "Error during PDF generation", t);
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
