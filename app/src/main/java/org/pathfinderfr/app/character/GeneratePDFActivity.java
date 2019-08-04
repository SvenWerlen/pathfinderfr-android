package org.pathfinderfr.app.character;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.itextpdf.io.image.ImageDataFactory;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.LoadDataTask;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.util.CharacterPDF;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

public class GeneratePDFActivity extends AppCompatActivity implements GeneratePDFTask.IDataUI {

    public static final String ARG_CHARACTER_ID = "pdf_characterid";

    private GeneratePDFTask taskInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_pdf);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final long characterId = getIntent().getLongExtra(GeneratePDFActivity.ARG_CHARACTER_ID, 0);

        Button button = findViewById(R.id.generatePDFButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(SheetMainFragment.class.getSimpleName(), "Generating PDF");
                if(taskInProgress == null) {
                    // disable buttons to force user to stay
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    }
                    Button button = findViewById(R.id.generatePDFButton);
                    button.setText(getResources().getString(R.string.generatepdf_inprogress));
                    TextView errorMsg = (TextView)findViewById(R.id.generatePDFInfos);
                    errorMsg.setVisibility(View.VISIBLE);
                    errorMsg.setText("");

                    FileOutputStream stream = null;
                    try {
                        File cachePath = new File(v.getContext().getCacheDir(), "characters");
                        cachePath.mkdirs(); // don't forget to make the directory
                        stream = new FileOutputStream(cachePath + "/personnage.pdf");
                        // get logo
                        InputStream ims = getApplicationContext().getAssets().open("pdf-logo.png");
                        Bitmap bmp = BitmapFactory.decodeStream(ims);
                        ByteArrayOutputStream logo = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, logo);
                        List<DBEntity> skills = DBHelper.getInstance(v.getContext()).getAllEntitiesWithAllFields(SkillFactory.getInstance());

                        // execution
                        taskInProgress = new GeneratePDFTask(GeneratePDFActivity.this);
                        DBEntity character = DBHelper.getInstance(v.getContext()).fetchEntity(characterId, CharacterFactory.getInstance());
                        if(character != null) {
                            GeneratePDFTask.Input input = new GeneratePDFTask.Input();
                            input.character = (Character)character;
                            input.logo = ImageDataFactory.create(logo.toByteArray());
                            input.skills = skills;
                            input.stream = stream;
                            taskInProgress.execute(input);
                        }
                    } catch( Exception exc ) {
                        if(stream != null) {
                            try {
                                stream.close();
                            } catch(Exception e) {}
                        }
                        onError(exc.getMessage());
                    }

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (taskInProgress == null) {
            super.onBackPressed();
        } else {
            Snackbar.make(findViewById(android.R.id.content),
                    getResources().getString(R.string.loaddata_pleasewait), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    private void resetProgress() {
        taskInProgress = null;
        // disable buttons to force user to stay
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        Button button = findViewById(R.id.generatePDFButton);
        button.setText(getResources().getString(R.string.generatepdf_start));
    }

    @Override
    public void onProgressCompleted() {
        resetProgress();
        finish();

        // read character from cache
        File charPath = new File(getApplicationContext().getCacheDir(), "characters");
        File newFile = new File(charPath, "personnage.pdf");
        System.out.println("SIZE = " + newFile.length());
        Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "org.pathfinderfr.app.fileprovider", newFile);

        if (contentUri != null) {
            Intent pdfIntent = new Intent();
            pdfIntent.setAction(Intent.ACTION_VIEW);
            pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pdfIntent.setDataAndType(contentUri, "application/pdf");
            startActivity(Intent.createChooser(pdfIntent, getResources().getString(R.string.sheet_choose_app_export)));
        }
    }

    @Override
    public void onError(String errorMessage) {
        resetProgress();
        ((TextView)findViewById(R.id.generatePDFInfos)).setText(errorMessage);
    }
}
