package org.pathfinderfr.app;


import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.pathfinderfr.R;
import org.pathfinderfr.app.treasure.TreasureFragment;

public class TreasureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        Fragment f = getSupportFragmentManager(). findFragmentById(R.id.fragment_treasure);
        if (f != null && f instanceof TreasureFragment) {
            if(((TreasureFragment)f).onBack()) {
                return;
            }
        }
        super.onBackPressed();
    }
}
