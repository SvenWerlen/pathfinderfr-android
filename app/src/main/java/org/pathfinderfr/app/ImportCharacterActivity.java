package org.pathfinderfr.app;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterImportExport;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.Pair;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.List;

public class ImportCharacterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_character);

        String content = handleIntent();
        WebView webView = findViewById(R.id.importCharacterInfo);
        webView.loadDataWithBaseURL(null, content, "text/html", "utf-8", null);
        webView.setBackgroundColor(Color.TRANSPARENT);
    }

    private String handleIntent() {
        StringBuffer content = new StringBuffer();

        Uri uri = getIntent().getData();
        if (uri == null) {
            tellUserThatCouldntOpenFile();
            return content.toString();
        }
        content.append(getString(R.string.importcharacter_info_acces)).append("<br/>");

        String text = null;
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            text = getStringFromInputStream(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (text == null) {
            tellUserThatCouldntOpenFile();
            return content.toString();
        }
        content.append(getString(R.string.importcharacter_info_read)).append("<br/>");

        Pair<Character, List<Integer>> result =
                CharacterImportExport.importCharacterAsYML(text, findViewById(android.R.id.content));

        if(result == null) {
            return content.toString();
        }
        content.append(getString(R.string.importcharacter_info_parse)).append("<br/>");
        DBHelper.getInstance(getApplicationContext()).insertEntity(result.first);
        content.append(getString(R.string.importcharacter_info_import)).append("<br/>");

        if(result.second.size() > 0) {
            content.append(getString(R.string.importcharacter_info_errors)).append("<br/><font color=\"red\">");
            for(int errorNo : result.second) {
                content.append("- ").append(ConfigurationUtil.getInstance().getProperties().get("importcharacter.error" + errorNo)).append("<br/>");
            }
            content.append("</font>");
        }
        content.append("<br/>").append(getString(R.string.importcharacter_filecontent)).append("<br/>");
        content.append("<pre>").append(text).append("</pre>");

        return content.toString();
    }

    private void tellUserThatCouldntOpenFile() {
        Toast.makeText(this, getString(R.string.importcharacter_error_file), Toast.LENGTH_LONG).show();
    }

    public static String getStringFromInputStream(InputStream stream) throws IOException {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
        return writer.toString();
    }

}
