package org.pathfinderfr.app.character;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.pdf.CardsPDF;
import org.pathfinderfr.app.pdf.CharacterPDF;
import org.pathfinderfr.app.util.AssetUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

public class GeneratePDFActivity extends AppCompatActivity implements GeneratePDFTask.IDataUI, GenerateCardsPDFTask.IDataUI {

    public static final String ARG_CHARACTER_ID = "pdf_characterid";

    private AsyncTask taskInProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_pdf);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.warningOldAPI).setVisibility(View.GONE);
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
                            input.options = new CharacterPDF.Options();
                            input.options.printInkSaving = ((CheckBox)findViewById(R.id.option_inksaving)).isChecked();
                            input.options.printLogo = ((CheckBox)findViewById(R.id.option_logo)).isChecked();
                            input.options.showWeaponsInInventory = ((CheckBox)findViewById(R.id.option_show_weapons)).isChecked();
                            input.options.showArmorsInInventory = ((CheckBox)findViewById(R.id.option_show_armors)).isChecked();
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

        Button buttonCards = findViewById(R.id.generateCardsPDFButton);
        buttonCards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(SheetMainFragment.class.getSimpleName(), "Generating PDF (cards)");
                if(taskInProgress == null) {
                    // disable buttons to force user to stay
                    if (getSupportActionBar() != null) {
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    }
                    Button button = findViewById(R.id.generateCardsPDFButton);
                    button.setText(getResources().getString(R.string.generatepdf_inprogress));
                    TextView errorMsg = (TextView)findViewById(R.id.generateCardsPDFInfos);
                    errorMsg.setVisibility(View.VISIBLE);
                    errorMsg.setText("");

                    FileOutputStream stream = null;
                    try {
                        File cachePath = new File(v.getContext().getCacheDir(), "characters");
                        cachePath.mkdirs(); // don't forget to make the directory
                        stream = new FileOutputStream(cachePath + "/cartes.pdf");
                        // get logo
                        InputStream ims = getApplicationContext().getAssets().open("cards/back.png");
                        Bitmap bmp = BitmapFactory.decodeStream(ims);
                        ByteArrayOutputStream back = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, back);

                        // execution
                        taskInProgress = new GenerateCardsPDFTask(GeneratePDFActivity.this);
                        DBEntity character = DBHelper.getInstance(v.getContext()).fetchEntity(characterId, CharacterFactory.getInstance());
                        if(character != null) {
                            GenerateCardsPDFTask.Input input = new GenerateCardsPDFTask.Input();
                            input.character = (Character)character;
                            input.stream = stream;
                            input.params = new CardsPDF.Params();
                            input.params.cardBack =  ImageDataFactory.create(back.toByteArray());
                            // full color version
                            if(((CheckBox)findViewById(R.id.option_9colors)).isChecked()) {
                                input.params.cardFront = new ImageData[9];
                                for (int i = 1; i < 10; i++) {
                                    Bitmap bitmap = BitmapFactory.decodeStream(getApplicationContext().getAssets().open(String.format(Locale.CANADA, "cards/card%d.png", i)));
                                    ByteArrayOutputStream image = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, image);
                                    input.params.cardFront[i - 1] = ImageDataFactory.create(image.toByteArray());
                                }
                                input.params.cardProp = new ImageData[9];
                                for (int i = 1; i < 10; i++) {
                                    Bitmap bitmap = BitmapFactory.decodeStream(getApplicationContext().getAssets().open(String.format(Locale.CANADA, "cards/comp%d.png", i)));
                                    ByteArrayOutputStream image = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, image);
                                    input.params.cardProp[i - 1] = ImageDataFactory.create(image.toByteArray());
                                }
                            }
                            // 1 color version
                            else {
                                input.params.cardFront = new ImageData[1];
                                Bitmap bitmap = BitmapFactory.decodeStream(getApplicationContext().getAssets().open(String.format(Locale.CANADA, "cards/card%d.png", 8)));
                                ByteArrayOutputStream image = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, image);
                                input.params.cardFront[0] = ImageDataFactory.create(image.toByteArray());
                                input.params.cardProp = new ImageData[1];
                                bitmap = BitmapFactory.decodeStream(getApplicationContext().getAssets().open(String.format(Locale.CANADA, "cards/comp%d.png", 8)));
                                image = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, image);
                                input.params.cardProp[0] = ImageDataFactory.create(image.toByteArray());
                            }
                            input.params.printBack = !((CheckBox)findViewById(R.id.option_back)).isChecked();
                            input.params.feats = ((CheckBox)findViewById(R.id.option_feats)).isChecked();
                            input.params.features = ((CheckBox)findViewById(R.id.option_features)).isChecked();
                            input.params.spells = ((CheckBox)findViewById(R.id.option_spells)).isChecked();
                            input.params.titleFont = PdfFontFactory.createFont(AssetUtil.assetToBytes(getApplicationContext().getAssets().open("cards/FOY1REG.TTF")), StandardCharsets.UTF_8.toString(), true);
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
        ((Button)findViewById(R.id.generatePDFButton)).setText(getResources().getString(R.string.generatepdf_start));
        ((Button)findViewById(R.id.generateCardsPDFButton)).setText(getResources().getString(R.string.generatepdf_cards_start));
    }

    @Override
    public void onProgressCompleted() {
        String file = taskInProgress instanceof GeneratePDFTask ? "personnage.pdf" : "cartes.pdf";
        resetProgress();
        finish();

        // read character from cache
        File charPath = new File(getApplicationContext().getCacheDir(), "characters");
        File newFile = new File(charPath, file);
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
        int id = taskInProgress instanceof  GeneratePDFTask ? R.id.generatePDFInfos : R.id.generateCardsPDFInfos;
        resetProgress();
        String error = getResources().getText(R.string.generatepdf_error).toString();
        ((TextView)findViewById(id)).setText(error + errorMessage);
    }
}
