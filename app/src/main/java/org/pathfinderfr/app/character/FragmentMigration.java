package org.pathfinderfr.app.character;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import org.pathfinderfr.R;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.MigrationHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.CharacterItem;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.Trait;
import org.pathfinderfr.app.util.FragmentUtil;

import java.util.List;
import java.util.Locale;


public class FragmentMigration extends DialogFragment implements View.OnClickListener {

    public static final String ARG_CHARACID = "argCharacterId";

    private long characterId;

    public FragmentMigration() {
        // Required empty public constructor
    }

    public static FragmentMigration newInstance() {
        FragmentMigration fragment = new FragmentMigration();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_CHARACID)) {
            characterId = getArguments().getLong(ARG_CHARACID);
        }

        // restore value that was selected
        if(savedInstanceState != null) {
            characterId = savedInstanceState.getLong(ARG_CHARACID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_sheet_main_migration, container, false);

        Character character = (Character)DBHelper.getInstance(rootView.getContext()).fetchEntity(characterId, CharacterFactory.getInstance());
        TextView sample = rootView.findViewById(R.id.sampletext);
        if(character != null) {
            List<DBEntity> elements =  MigrationHelper.migrate(character, true);
            System.out.println(elements.size());
            LinearLayout layout = rootView.findViewById(R.id.linearLayout);
            for(DBEntity e : elements) {
                String type = "??";
                if(e instanceof Skill) type = "Compétence";
                else if(e instanceof Feat) type = "Don";
                else if(e instanceof Race) type = "Race";
                else if(e instanceof Trait) type = "Trait";
                else if(e instanceof Class) type = "Classe";
                else if(e instanceof ClassArchetype) type = "Archétype";
                else if(e instanceof Spell) type = "Sort";
                else if(e instanceof ClassFeature) type = "Aptitude";
                else if(e instanceof CharacterItem) type = "Équipement";
                TextView tv = FragmentUtil.copyExampleTextFragment(sample);
                tv.setText(String.format(Locale.CANADA, "%s : %s", type, e.getName()));
                layout.addView(tv);
            }
        }
        sample.setVisibility(View.GONE);

        rootView.findViewById(R.id.close).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onClick(View v) {
        ((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getView().getWindowToken(),0);

        if(v.getId() == R.id.close) {
            dismiss();
        }
    }
}

