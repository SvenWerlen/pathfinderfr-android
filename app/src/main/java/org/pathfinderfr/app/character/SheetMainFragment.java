package org.pathfinderfr.app.character;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.MainActivity;
import org.pathfinderfr.app.character.FragmentRacePicker.OnFragmentInteractionListener;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.util.CharacterUtil;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;

import java.util.ArrayList;
import java.util.List;


/**
 * Skill tab on character sheet
 */
public class SheetMainFragment extends Fragment implements FragmentAbilityPicker.OnFragmentInteractionListener,
        OnFragmentInteractionListener, FragmentClassPicker.OnFragmentInteractionListener,
        FragmentModifPicker.OnFragmentInteractionListener, FragmentHitPointsPicker.OnFragmentInteractionListener,
        FragmentSpeedPicker.OnFragmentInteractionListener, FragmentNamePicker.OnFragmentInteractionListener,
        FragmentDeleteAction.OnFragmentInteractionListener {

    private static final String ARG_CHARACTER_ID     = "character_id";
    private static final String DIALOG_PICK_ABILITY  = "ability-picker";
    private static final String DIALOG_DELETE_ACTION = "delete-action";
    private static final String DIALOG_PICK_NAME     = "name-picker";
    private static final String DIALOG_PICK_RACE     = "race-picker";
    private static final String DIALOG_PICK_CLASS    = "class-picker";
    private static final String DIALOG_PICK_HP       = "hitpoint-picker";
    private static final String DIALOG_PICK_SPEED    = "speed-picker";
    private static final String DIALOG_PICK_MODIFS   = "modifs-picker";

    private Character character;
    private List<TextView> classPickers;
    private List<ImageView> modifPickers;

    private long characterId;
    ProfileListener listener;
    private OnFragmentInteractionListener mListener;



    public SheetMainFragment() {
        // Required empty public constructor
        classPickers = new ArrayList<>();
        modifPickers = new ArrayList<>();
    }

    /**
     * @param characterId character id to display (should never be <=0)
     * @return A new instance of fragment SheetMainFragment.
     */
    public static SheetMainFragment newInstance(long characterId) {
        if(characterId<=0) {
            throw new IllegalArgumentException("Invalid characterId " + characterId);
        }
        SheetMainFragment fragment = new SheetMainFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_CHARACTER_ID, characterId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            characterId = getArguments().getLong(ARG_CHARACTER_ID);
        }
    }

    private static void showTooltip(View v, String message) {
        Toast t = Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT);
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        t.setGravity(Gravity.TOP|Gravity.LEFT, xy[0], xy[1]);
        t.show();
    }

    /**
     * Generates a string (tables rows) for each individual other bonus
     * @param character character object
     * @param modifId modif identifier
     * @param tooltipTemplate template for a row entry
     * @return
     */
    public static String generateOtherBonusText(Character character, int modifId, String tooltipTemplate) {
        List<Character.CharacterModif> modifs = character.getModifsForId(modifId);
        StringBuffer buf = new StringBuffer();
        for(Character.CharacterModif modif: modifs) {
            if(modif.isEnabled()) {
                buf.append(String.format(tooltipTemplate, modif.getSource(), modif.getModif(0).second));
            }
        }
        return buf.toString();
    }

    /**
     * Initializes the character modifs based on preferences (setEnable(true))
     *
     * @param context current context (for finding preferences)
     * @param character character object
     */
    public static void initializeCharacterModifsStates(Context context, Character character) {
        // initialize character modifs states
        String modifStates = PreferenceManager.getDefaultSharedPreferences(context).getString(
                CharacterSheetActivity.PREF_CHARACTER_MODIF_STATES + character.getId(), null);

        if(modifStates != null) {
            Log.d(SheetMainFragment.class.getSimpleName(), "Modif states = " + modifStates);
            if (modifStates.length() == character.getModifsCount()) {
                int idx = 0;
                for(Character.CharacterModif modif : character.getModifs()) {
                    if(modifStates.charAt(idx) == '1') {
                        modif.setEnabled(true);
                    }
                    idx++;
                }
            } else {
                Log.w(SheetMainFragment.class.getSimpleName(), "Something went wrong. Préférences don't match modif's count.");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sheet_main, container, false);
        listener = new ProfileListener(this);

        // fetch character
        if(characterId > 0) {
            character = (Character)DBHelper.getInstance(getContext()).fetchEntity(characterId, CharacterFactory.getInstance());
        }
        if(character == null) {
            throw new IllegalStateException("Something is wrong! Invalid character.");
        } else {
            initializeCharacterModifsStates(view.getContext(), character);
        }

        view.findViewById(R.id.ability_str).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_ability_strength));}
        });
        view.findViewById(R.id.ability_dex).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_ability_dexterity));}
        });
        view.findViewById(R.id.ability_con).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_ability_constitution));}
        });
        view.findViewById(R.id.ability_int).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_ability_intelligence));}
        });
        view.findViewById(R.id.ability_wis).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_ability_wisdom));}
        });
        view.findViewById(R.id.ability_cha).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_ability_charisma));}
        });

        view.findViewById(R.id.ability_str_value).setOnClickListener(listener);
        view.findViewById(R.id.ability_dex_value).setOnClickListener(listener);
        view.findViewById(R.id.ability_con_value).setOnClickListener(listener);
        view.findViewById(R.id.ability_int_value).setOnClickListener(listener);
        view.findViewById(R.id.ability_wis_value).setOnClickListener(listener);
        view.findViewById(R.id.ability_cha_value).setOnClickListener(listener);

        view.findViewById(R.id.actionDelete).setOnClickListener(listener);
        view.findViewById(R.id.sheet_main_namepicker).setOnClickListener(listener);
        view.findViewById(R.id.sheet_main_racepicker).setOnClickListener(listener);
        view.findViewById(R.id.sheet_main_classpicker).setVisibility(View.GONE);

        final CharacterSheetActivity act = ((CharacterSheetActivity)getActivity());

        // ACTIONS
        ImageView actionPin = view.findViewById(R.id.actionPin);
        actionPin.findViewById(R.id.actionPin).setOnClickListener(listener);
        actionPin.setBackground(null);
        actionPin.setImageResource(R.drawable.ic_pin);
        if(character.getId() == PreferenceManager.getDefaultSharedPreferences(view.getContext()).getLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, 0L)) {
            ((ImageView)view.findViewById(R.id.actionPin)).setColorFilter(view.getContext().getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        } else {
            ((ImageView)view.findViewById(R.id.actionPin)).setColorFilter(view.getContext().getResources().getColor(R.color.colorDisabled), PorterDuff.Mode.SRC_ATOP);
        }
        // ABILITIES
        final String abTooltipTitle = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.abilities.title");
        final String abTooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.abilities.content");

        View.OnClickListener abTooltipListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.showTooltip(abTooltipTitle, abTooltipContent);
            }
        };
        view.findViewById(R.id.ability_str_modif).setOnClickListener(abTooltipListener);
        view.findViewById(R.id.ability_dex_modif).setOnClickListener(abTooltipListener);
        view.findViewById(R.id.ability_con_modif).setOnClickListener(abTooltipListener);
        view.findViewById(R.id.ability_int_modif).setOnClickListener(abTooltipListener);
        view.findViewById(R.id.ability_wis_modif).setOnClickListener(abTooltipListener);
        view.findViewById(R.id.ability_cha_modif).setOnClickListener(abTooltipListener);

        // INITIATIVE
        final String iniTooltipTitle = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.initiative.title");
        final String tooltipModif = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.modif.entry");
        final String iniTooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.initiative.content");

        view.findViewById(R.id.other_ini).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_initiative));}
        });
        view.findViewById(R.id.initiative_value).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.showTooltip(iniTooltipTitle, String.format(iniTooltipContent,
                        character.getDexterityModif(),
                        generateOtherBonusText(character, Character.MODIF_COMBAT_INI, tooltipModif),
                        character.getInitiative()));
            }
        });

        // ARMOR CLASS
        final String acTooltipTitle = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.ac.title");
        final String acTooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.ac.content");

        view.findViewById(R.id.other_ac).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_armorclass));}
        });
        view.findViewById(R.id.armorclass_value).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.showTooltip(acTooltipTitle,String.format(acTooltipContent,
                        character.getDexterityModif(), // dex modif
                        character.getRaceSize() == Character.SIZE_SMALL ? 1 : 0, // size
                        generateOtherBonusText(character, Character.MODIF_COMBAT_AC, tooltipModif), // others
                        character.getArmorClass()));
            }
        });

        // MAGIC RESISTANCE
        final String magicTooltipTitle = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.magic.title");
        final String magicTooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.magic.content");

        view.findViewById(R.id.other_mag).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_magicresistance));}
        });
        view.findViewById(R.id.magicresistance_value).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                act.showTooltip(magicTooltipTitle,String.format(
                        magicTooltipContent,
                        generateOtherBonusText(character, Character.MODIF_COMBAT_MAG, tooltipModif),
                        character.getMagicResistance()));
            }
        });

        // HIT POINTS
        view.findViewById(R.id.other_hp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_hitpoints));}
        });
        view.findViewById(R.id.hitpoint_value).setOnClickListener(listener);

        // SPEED
        view.findViewById(R.id.other_speed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_speed));}
        });
        view.findViewById(R.id.speed_value).setOnClickListener(listener);

        // BASE ATTACK BONUS
        final String babTooltipTitle = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.bab.title");
        final String babTooltipEntry = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.bab.entry");
        final String babTooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.bab.content");

        view.findViewById(R.id.combat_bab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_baseattackbonus));}
        });
        view.findViewById(R.id.base_attack_bonus_value).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer text = new StringBuffer();
                for(int i=0; i<character.getClassesCount(); i++) {
                    Pair<Class,Integer> cl = character.getClass(i);
                    Class.Level lvl = cl.first.getLevel(cl.second);
                    if(lvl != null) {
                        text.append(String.format(babTooltipEntry, cl.first.getName(), cl.second, lvl.getBaseAttackBonusAsString() ));
                    }
                }
                act.showTooltip(babTooltipTitle,String.format(
                        babTooltipContent,
                        text,
                        "", // other
                        character.getBaseAttackBonusAsString()));
            }
        });

        // ATTACK BONUS (MELEE & RANGED)
        final String tooltipBabModif = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.babmodif.entry");
        final String meleeTooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.attmelee.content");
        final String rangedTooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.attranged.content");
        view.findViewById(R.id.combat_attack_melee).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_attack_melee));}
        });
        view.findViewById(R.id.attack_melee_value).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer text = new StringBuffer();
                for(int i=0; i<character.getClassesCount(); i++) {
                    Pair<Class,Integer> cl = character.getClass(i);
                    Class.Level lvl = cl.first.getLevel(cl.second);
                    if(lvl != null) {
                        text.append(String.format(babTooltipEntry, cl.first.getName(), cl.second, lvl.getBaseAttackBonusAsString() ));
                    }
                }
                act.showTooltip(babTooltipTitle,String.format(
                        meleeTooltipContent,
                        text,
                        character.getStrengthModif(),
                        generateOtherBonusText(character, Character.MODIF_COMBAT_ATT_MELEE, tooltipBabModif), // other
                        character.getAttackBonusMeleeAsString()));
            }
        });

        view.findViewById(R.id.combat_attack_distance).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_attack_distance));}
        });
        view.findViewById(R.id.attack_distance_value).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer text = new StringBuffer();
                for(int i=0; i<character.getClassesCount(); i++) {
                    Pair<Class,Integer> cl = character.getClass(i);
                    Class.Level lvl = cl.first.getLevel(cl.second);
                    if(lvl != null) {
                        text.append(String.format(babTooltipEntry, cl.first.getName(), cl.second, lvl.getBaseAttackBonusAsString() ));
                    }
                }
                act.showTooltip(babTooltipTitle,String.format(
                        rangedTooltipContent,
                        text,
                        character.getDexterityModif(),
                        generateOtherBonusText(character, Character.MODIF_COMBAT_ATT_RANGED, tooltipBabModif), // other
                        character.getAttackBonusRangeAsString()));
            }
        });

        // COMBAT MANEUVER BONUS
        final String cmbTooltipTitle = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.cmb.title");
        final String cmbTooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.cmb.content");

        view.findViewById(R.id.combat_cmb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_combat_man_bonus));}
        });
        view.findViewById(R.id.combat_cmb_total).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] bab = character.getBaseAttackBonus();
                int bonus = bab == null || bab.length == 0 ? 0 : bab[0];
                act.showTooltip(cmbTooltipTitle,String.format(cmbTooltipContent,
                        bonus,
                        character.getStrengthModif(),
                        character.getRaceSize() == Character.SIZE_SMALL ? -1 : 0, // size
                        generateOtherBonusText(character, Character.MODIF_COMBAT_CMB, tooltipModif), // other
                        character.getCombatManeuverBonus()));
            }
        });

        // COMBAT MANEUVER DEFENSE
        final String cmdTooltipTitle = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.cmd.title");
        final String cmdTooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.cmd.content");

        view.findViewById(R.id.combat_cmd).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_combat_man_defense));}
        });
        view.findViewById(R.id.combat_cmd_total).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] bab = character.getBaseAttackBonus();
                int bonus = bab == null || bab.length == 0 ? 0 : bab[0];
                act.showTooltip(cmdTooltipTitle,String.format(cmdTooltipContent,
                        bonus,
                        character.getStrengthModif(),
                        character.getDexterityModif(),
                        character.getRaceSize() == Character.SIZE_SMALL ? -1 : 0, // size
                        generateOtherBonusText(character, Character.MODIF_COMBAT_CMD, tooltipModif), // other
                        character.getCombatManeuverDefense()));
            }
        });

        // SAVING THROWS
        final String savTooltipTitle = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.sav.title");
        final String savTooltipEntry = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.sav.entry");
        final String savTooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.sav.content");

        view.findViewById(R.id.savingthrows_for).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_savingthrows_fortitude));}
        });
        view.findViewById(R.id.savingthrows_fortitude_total).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer text = new StringBuffer();
                for(int i=0; i<character.getClassesCount(); i++) {
                    Pair<Class,Integer> cl = character.getClass(i);
                    Class.Level lvl = cl.first.getLevel(cl.second);
                    if(lvl != null) {
                        text.append(String.format(savTooltipEntry, cl.first.getShortName(), cl.second, lvl.getFortitudeBonus()));
                    }
                }
                act.showTooltip(
                        String.format(savTooltipTitle,getResources().getString(R.string.sheet_savingthrows_fortitude)),
                        String.format(savTooltipContent,
                                text,
                                getResources().getString(R.string.sheet_ability_constitution).toLowerCase(), character.getConstitutionModif(),
                                generateOtherBonusText(character, Character.MODIF_SAVES_ALL, tooltipModif)
                                        + generateOtherBonusText(character, Character.MODIF_SAVES_FOR, tooltipModif), // other
                                character.getSavingThrowsFortitudeTotal()));
            }
        });

        view.findViewById(R.id.savingthrows_ref).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_savingthrows_reflex));}
        });
        view.findViewById(R.id.savingthrows_reflex_total).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer text = new StringBuffer();
                for(int i=0; i<character.getClassesCount(); i++) {
                    Pair<Class,Integer> cl = character.getClass(i);
                    Class.Level lvl = cl.first.getLevel(cl.second);
                    if(lvl != null) {
                        text.append(String.format(savTooltipEntry, cl.first.getShortName(), cl.second, lvl.getReflexBonus()));
                    }
                }
                act.showTooltip(
                        String.format(savTooltipTitle,getResources().getString(R.string.sheet_savingthrows_reflex)),
                        String.format(savTooltipContent,
                                text,
                                getResources().getString(R.string.sheet_ability_dexterity).toLowerCase(), character.getDexterityModif(),
                                generateOtherBonusText(character, Character.MODIF_SAVES_ALL, tooltipModif)
                                        + generateOtherBonusText(character, Character.MODIF_SAVES_REF, tooltipModif), // other
                                character.getSavingThrowsReflexesTotal()));
            }
        });

        view.findViewById(R.id.savingthrows_wil).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_savingthrows_will));}
        });
        view.findViewById(R.id.savingthrows_will_total).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuffer text = new StringBuffer();
                for(int i=0; i<character.getClassesCount(); i++) {
                    Pair<Class,Integer> cl = character.getClass(i);
                    Class.Level lvl = cl.first.getLevel(cl.second);
                    if(lvl != null) {
                        text.append(String.format(savTooltipEntry, cl.first.getShortName(), cl.second, lvl.getWillBonus()));
                    }
                }
                act.showTooltip(
                        String.format(savTooltipTitle,getResources().getString(R.string.sheet_savingthrows_will)),
                        String.format(savTooltipContent,
                                text,
                                getResources().getString(R.string.sheet_ability_wisdom).toLowerCase(), character.getWisdomModif(),
                                generateOtherBonusText(character, Character.MODIF_SAVES_ALL, tooltipModif)
                                        + generateOtherBonusText(character, Character.MODIF_SAVES_WIL, tooltipModif), // other
                                character.getSavingThrowsWillTotal()));
            }
        });

        // update name
        TextView nameTv = view.findViewById(R.id.sheet_main_namepicker);
        if(character.getName() != null) {
            nameTv.setText(character.getName());
        }

        // update race
        TextView raceTv = view.findViewById(R.id.sheet_main_racepicker);
        if(character.getRace() != null) {
            raceTv.setText(character.getRace().getName());
        }

        updateClassPickers(view);
        updateModifsPickers(view);

        view.findViewById(R.id.sheet_main_modifs_example_icon).setVisibility(View.GONE);
        view.findViewById(R.id.sheet_main_modifpicker).setOnClickListener(listener);

        // reset listeners for opened dialogs
        if (savedInstanceState != null) {
            FragmentDeleteAction fragDelete = (FragmentDeleteAction)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_DELETE_ACTION);
            if (fragDelete != null) {
                fragDelete.setListener(this);
            }
            FragmentNamePicker fragName = (FragmentNamePicker)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_PICK_NAME);
            if (fragName != null) {
                fragName.setListener(this);
            }
            FragmentRacePicker fragRace = (FragmentRacePicker)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_PICK_RACE);
            if (fragRace != null) {
                fragRace.setListener(this);
            }
            FragmentClassPicker fragClass = (FragmentClassPicker)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_PICK_CLASS);
            if (fragClass != null) {
                fragClass.setListener(this);
            }
            FragmentModifPicker fragModifs = (FragmentModifPicker)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_PICK_MODIFS);
            if (fragModifs != null) {
                fragModifs.setListener(this);
            }
            FragmentAbilityPicker fragAbility = (FragmentAbilityPicker)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_PICK_ABILITY);
            if (fragAbility != null) {
                fragAbility.setListener(this);
            }
            FragmentHitPointsPicker fragHP = (FragmentHitPointsPicker)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_PICK_HP);
            if (fragHP != null) {
                fragHP.setListener(this);
            }
            FragmentSpeedPicker fragSpeed = (FragmentSpeedPicker)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_PICK_SPEED);
            if (fragSpeed != null) {
                fragSpeed.setListener(this);
            }
        }

        return view;
    }

    private void updateClassPickers(View view) {
        TextView reference = view.findViewById(R.id.sheet_main_classpicker);
        FlowLayout layout = view.findViewById(R.id.sheet_main_classlayout);
        // make sure #pickers > #classes
        int toCreate = character.getClassesCount() - classPickers.size();
        for(int i = 0; i <= toCreate; i++) {
            TextView newPicker = FragmentUtil.copyExampleTextFragment(reference);
            newPicker.setOnClickListener(listener);
            classPickers.add(newPicker);
            layout.addView(newPicker);
        }
        // configure #pickers
        int idx = 0;
        int maxLevel = 20 - character.getOtherClassesLevel(-1);
        for(TextView tv : classPickers) {
            Pair<Class,Integer> cl = character.getClass(idx);
            if(cl != null) {
                tv.setText(cl.first.getName() + " " + cl.second);
                tv.setVisibility(View.VISIBLE);
            } else if(idx == character.getClassesCount() && maxLevel > 0) {
                tv.setText(reference.getText());
                tv.setVisibility(View.VISIBLE);
            } else {
                tv.setVisibility(View.GONE);
            }
            idx++;
        }
        // update stats
        updateSheet(view);
    }

    private void updateModifsPickers(View view) {
        ImageView reference = view.findViewById(R.id.sheet_main_modifs_example_icon);
        FlowLayout layout = view.findViewById(R.id.sheet_main_modifslayout);
        // make sure #pickers > #icons
        int toCreate = character.getModifsCount() - modifPickers.size();
        for(int i = 0; i <= toCreate; i++) {
            ImageView newPicker = FragmentUtil.copyExampleImageFragment(reference);
            newPicker.setOnClickListener(listener);
            newPicker.setOnLongClickListener(listener);
            modifPickers.add(newPicker);
            layout.addView(newPicker);
        }
        // configure #pickers
        final int colorDisabled = view.getContext().getResources().getColor(R.color.colorBlack);
        final int colorEnabled = view.getContext().getResources().getColor(R.color.colorPrimaryDark);
        int idx = 0;
        for(ImageView iv : modifPickers) {
            Character.CharacterModif modif = character.getModif(idx);
            if(modif != null) {
                final int resourceId = view.getResources().getIdentifier("modif_" + modif.getIcon(), "drawable",
                        view.getContext().getPackageName());
                if(resourceId > 0) {
                    iv.setTag(idx);
                    iv.setBackgroundColor(modif.isEnabled() ? colorEnabled : colorDisabled);
                    iv.setImageResource(resourceId);
                    iv.setVisibility(View.VISIBLE);
                }
            } else {
                iv.setVisibility(View.GONE);
            }
            idx++;
        }
        // update stats
        updateSheet(view);
    }


    private void updateSheet(View view) {
        // update abilities
        ((TextView)view.findViewById(R.id.ability_str_value)).setText(String.valueOf(character.getStrength()));
        ((TextView)view.findViewById(R.id.ability_dex_value)).setText(String.valueOf(character.getDexterity()));
        ((TextView)view.findViewById(R.id.ability_con_value)).setText(String.valueOf(character.getConstitution()));
        ((TextView)view.findViewById(R.id.ability_int_value)).setText(String.valueOf(character.getIntelligence()));
        ((TextView)view.findViewById(R.id.ability_wis_value)).setText(String.valueOf(character.getWisdom()));
        ((TextView)view.findViewById(R.id.ability_cha_value)).setText(String.valueOf(character.getCharisma()));
        ((TextView)view.findViewById(R.id.ability_str_modif)).setText(String.valueOf(character.getStrengthModif()));
        ((TextView)view.findViewById(R.id.ability_dex_modif)).setText(String.valueOf(character.getDexterityModif()));
        ((TextView)view.findViewById(R.id.ability_con_modif)).setText(String.valueOf(character.getConstitutionModif()));
        ((TextView)view.findViewById(R.id.ability_int_modif)).setText(String.valueOf(character.getIntelligenceModif()));
        ((TextView)view.findViewById(R.id.ability_wis_modif)).setText(String.valueOf(character.getWisdomModif()));
        ((TextView)view.findViewById(R.id.ability_cha_modif)).setText(String.valueOf(character.getCharismaModif()));

        ((TextView)view.findViewById(R.id.hitpoint_value)).setText(String.valueOf(character.getHitpoints()));
        ((TextView)view.findViewById(R.id.speed_value)).setText(String.valueOf(character.getSpeed()));

        TextView initiative = view.findViewById(R.id.initiative_value);
        TextView armorClass = view.findViewById(R.id.armorclass_value);
        TextView magicResis = view.findViewById(R.id.magicresistance_value);

        TextView savingFortitudeTotal = view.findViewById(R.id.savingthrows_fortitude_total);
        TextView savingReflexTotal = view.findViewById(R.id.savingthrows_reflex_total);
        TextView savingWillTotal = view.findViewById(R.id.savingthrows_will_total);

        TextView savingFortitude = view.findViewById(R.id.savingthrows_fortitude);
        TextView savingReflex = view.findViewById(R.id.savingthrows_reflex);
        TextView savingWill = view.findViewById(R.id.savingthrows_will);

        TextView savingFortitudeAbility = view.findViewById(R.id.savingthrows_fortitude_ability);
        TextView savingReflexAbility = view.findViewById(R.id.savingthrows_reflex_ability);
        TextView savingWillAbility = view.findViewById(R.id.savingthrows_will_ability);

        TextView baseAttackBonus = view.findViewById(R.id.base_attack_bonus_value);
        TextView attackMeleeBonus = view.findViewById(R.id.attack_melee_value);
        TextView attackDistanceBonus = view.findViewById(R.id.attack_distance_value);
        TextView combatManBonusTotal = view.findViewById(R.id.combat_cmb_total);
        TextView combatManBonusBab = view.findViewById(R.id.combat_cmb_bab);
        TextView combatManBonusAbility = view.findViewById(R.id.combat_cmb_ability);
        TextView combatManDefenseTotal = view.findViewById(R.id.combat_cmd_total);
        TextView combatManDefenseBab = view.findViewById(R.id.combat_cmd_bab);
        TextView combatManDefenseAbility = view.findViewById(R.id.combat_cmd_ability);

        initiative.setText(String.valueOf(character.getInitiative()));
        armorClass.setText(String.valueOf(character.getArmorClass()));
        magicResis.setText(String.valueOf(character.getMagicResistance()));

        savingFortitudeTotal.setText(String.valueOf(character.getSavingThrowsFortitudeTotal()));
        savingReflexTotal.setText(String.valueOf(character.getSavingThrowsReflexesTotal()));
        savingWillTotal.setText(String.valueOf(character.getSavingThrowsWillTotal()));

        savingFortitude.setText(String.valueOf(character.getSavingThrowsFortitude()));
        savingReflex.setText(String.valueOf(character.getSavingThrowsReflexes()));
        savingWill.setText(String.valueOf(character.getSavingThrowsWill()));

        savingFortitudeAbility.setText(String.valueOf(character.getConstitutionModif()));
        savingReflexAbility.setText(String.valueOf(character.getDexterityModif()));
        savingWillAbility.setText(String.valueOf(character.getWisdomModif()));

        int[] bab = character.getBaseAttackBonus();
        baseAttackBonus.setText(character.getBaseAttackBonusAsString());
        attackMeleeBonus.setText(character.getAttackBonusMeleeAsString());
        attackDistanceBonus.setText(character.getAttackBonusRangeAsString());
        combatManBonusTotal.setText(String.valueOf(character.getCombatManeuverBonus()));
        combatManBonusBab.setText(String.valueOf(bab == null || bab.length == 0 ? 0: bab[0]));
        combatManBonusAbility.setText(String.valueOf(character.getStrengthModif()));
        combatManDefenseTotal.setText(String.valueOf(character.getCombatManeuverDefense()));
        combatManDefenseBab.setText(String.valueOf(bab == null || bab.length == 0 ? 0: bab[0]));
        combatManDefenseAbility.setText(String.valueOf(character.getStrengthModif()+character.getDexterityModif()));
    }


    /**
     * Persists current modification states into preferences
     * Stores as '01001' where each character represents a modif (1 is enabled, 0 disabled)
     */
    private void modifStatesIntoPreferences() {
        StringBuffer buf = new StringBuffer();
        for(Character.CharacterModif modif : character.getModifs()) {
            buf.append(modif.isEnabled() ? '1' : '0');
        }
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().
                putString(CharacterSheetActivity.PREF_CHARACTER_MODIF_STATES + character.getId(), buf.toString()).apply();
    }

    private static class ProfileListener implements View.OnClickListener, View.OnLongClickListener {

        SheetMainFragment parent;

        public ProfileListener(SheetMainFragment fragment) {
            parent = fragment;
        }

        @Override
        public void onClick(View v) {

            if (v instanceof TextView && v.getTag() != null && v.getTag().toString().startsWith("ability")) {
                TextView tv = (TextView) v;
                int abilityId = Integer.parseInt(v.getTag().toString().substring("ability".length()));
                int value = parent.character.getAbilityValue(abilityId, false);

                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_ABILITY);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentAbilityPicker.newInstance(parent);

                Bundle arguments = new Bundle();
                arguments.putInt(FragmentAbilityPicker.ARG_ABILITY_ID, tv.getId());
                arguments.putInt(FragmentAbilityPicker.ARG_ABILITY_VALUE, value);
                newFragment.setArguments(arguments);
                newFragment.show(ft, DIALOG_PICK_ABILITY);
                return;
            }
            else if(v instanceof TextView && "name".equals(v.getTag())) {
                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_NAME);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentNamePicker.newInstance(parent);

                Bundle arguments = new Bundle();
                String name = parent.character.getName();
                if(name != null) {
                    arguments.putString(FragmentNamePicker.ARG_NAME, name);
                }
                newFragment.setArguments(arguments);
                newFragment.show(ft, DIALOG_PICK_NAME);
                return;
            }
            else if(v instanceof TextView && "race".equals(v.getTag())) {
                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_RACE);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentRacePicker.newInstance(parent);

                Bundle arguments = new Bundle();
                Long raceId = parent.character.getRace() == null ? 0L : parent.character.getRace().getId();
                arguments.putLong(FragmentRacePicker.ARG_RACE_ID,  raceId);
                newFragment.setArguments(arguments);
                newFragment.show(ft, DIALOG_PICK_RACE);
                return;
            }
            else if(v instanceof TextView && "class".equals(v.getTag())) {
                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_CLASS);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentClassPicker.newInstance(parent);

                // find which class was selected
                int idx = 0;
                for(TextView tv : parent.classPickers) {
                    if(tv == v) {
                        break;
                    }
                    idx++;
                }
                // not found??
                if(idx == parent.classPickers.size()) {
                    Log.w(SheetMainFragment.class.getSimpleName(), "Class picker couldn't be found!!");
                    return;
                }
                Pair<Class,Integer> curClass = parent.character.getClass(idx);

                // prepare parameters
                long[] excluded = parent.character.getOtherClassesIds(curClass == null ? -1 : curClass.first.getId());
                int maxLevel = 20 - parent.character.getOtherClassesLevel(curClass == null ? -1 : curClass.first.getId());

                Bundle arguments = new Bundle();
                if(curClass != null) {
                    arguments.putLong(FragmentClassPicker.ARG_CLASS_ID, curClass.first.getId());
                    arguments.putInt(FragmentClassPicker.ARG_CLASS_LVL, curClass.second);
                }
                arguments.putLongArray(FragmentClassPicker.ARG_CLASS_EXCL, excluded);
                arguments.putInt(FragmentClassPicker.ARG_CLASS_MAX_LVL, maxLevel);
                newFragment.setArguments(arguments);
                newFragment.show(ft, DIALOG_PICK_CLASS);
                return;
            }
            else if(v instanceof TextView && "modif".equals(v.getTag())) {
                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_MODIFS);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentModifPicker.newInstance(parent);

                Bundle arguments = new Bundle();
                newFragment.setArguments(arguments);
                newFragment.show(ft, DIALOG_PICK_MODIFS);
                return;
            }
            else if(v instanceof TextView && "hitpoints".equals(v.getTag())) {
                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_HP);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentHitPointsPicker.newInstance(parent, parent.character.getHitpoints());

                Bundle arguments = new Bundle();
                newFragment.setArguments(arguments);
                newFragment.show(ft, DIALOG_PICK_HP);
                return;
            }
            else if(v instanceof TextView && "speed".equals(v.getTag())) {
                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_SPEED);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentSpeedPicker.newInstance(parent, parent.character.getBaseSpeed());

                Bundle arguments = new Bundle();
                newFragment.setArguments(arguments);
                newFragment.show(ft, DIALOG_PICK_SPEED);
                return;
            }

            else if(v instanceof ImageView) {
                if(v.getId() == R.id.actionPin) {
                    final int colorDisabled = parent.getContext().getResources().getColor(R.color.colorDisabled);
                    final int colorEnabled = parent.getContext().getResources().getColor(R.color.colorPrimaryDark);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(parent.getContext());
                    long characterId = prefs.getLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, 0L);
                    if(characterId == parent.character.getId()) {
                        prefs.edit().remove(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID).apply();
                        ((ImageView)parent.getView().findViewById(R.id.actionPin)).setColorFilter(colorDisabled, PorterDuff.Mode.SRC_ATOP);
                    } else {
                        prefs.edit().putLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, parent.character.getId()).apply();
                        ((ImageView)parent.getView().findViewById(R.id.actionPin)).setColorFilter(colorEnabled, PorterDuff.Mode.SRC_ATOP);
                    }
                    return;
                }
                else if(v.getId() == R.id.actionDelete) {
                    FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_DELETE_ACTION);
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    DialogFragment newFragment = FragmentDeleteAction.newInstance(parent);

                    Bundle arguments = new Bundle();
                    String name = parent.character.getName();
                    if(name != null) {
                        arguments.putString(FragmentDeleteAction.ARG_NAME, name);
                    }
                    newFragment.setArguments(arguments);
                    newFragment.show(ft, DIALOG_DELETE_ACTION);
                    return;
                }
                // MODIFICATION ENABLED/DISABLED
                else {
                    ImageView icon = (ImageView) v;
                    Character.CharacterModif modif = parent.character.getModif((int) v.getTag());
                    if (modif != null) {
                        // toggle modification
                        modif.setEnabled(!modif.isEnabled());
                        final int colorDisabled = parent.getContext().getResources().getColor(R.color.colorBlack);
                        final int colorEnabled = parent.getContext().getResources().getColor(R.color.colorPrimaryDark);
                        if (icon.getDrawable() != null) {
                            icon.setBackgroundColor(modif.isEnabled() ? colorEnabled : colorDisabled);
                        }
                        parent.updateSheet(parent.getView());

                        // save into preferences
                        parent.modifStatesIntoPreferences();
                    }
                    return;
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(v instanceof ImageView) {
                ImageView icon = (ImageView)v;
                Character.CharacterModif modif = parent.character.getModif((Integer)v.getTag());
                if(modif != null) {
                    FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_MODIFS);
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    DialogFragment newFragment = FragmentModifPicker.newInstance(parent);

                    Bundle arguments = new Bundle();
                    arguments.putInt(FragmentModifPicker.ARG_MODIF_IDX, (Integer)v.getTag());
                    arguments.putString(FragmentModifPicker.ARG_MODIF_SOURCE, modif.getSource());
                    ArrayList<Integer> modifIds = new ArrayList<>();
                    ArrayList<Integer> modifVals = new ArrayList<>();
                    for(int i = 0; i<modif.getModifCount(); i++) {
                        modifIds.add(modif.getModif(i).first);
                        modifVals.add(modif.getModif(i).second);
                    }
                    arguments.putIntegerArrayList(FragmentModifPicker.ARG_MODIF_IDS, modifIds);
                    arguments.putIntegerArrayList(FragmentModifPicker.ARG_MODIF_VALS, modifVals);
                    arguments.putString(FragmentModifPicker.ARG_MODIF_ICON, modif.getIcon());

                    newFragment.setArguments(arguments);
                    newFragment.show(ft, DIALOG_PICK_MODIFS);
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Updates the data into the database for the character
     */
    private void characterDBUpdate() {
        DBHelper.getInstance(getContext()).updateEntity(character);
    }

    @Override
    public void onAbilityValueChosen(int abilityId, int abilityValue) {
        View v = getView().findViewById(abilityId);
        if(v != null && v instanceof TextView) {
            TextView tv = (TextView)v;
            tv.setText(String.valueOf(abilityValue));

            switch(abilityId) {
                case R.id.ability_str_value:
                    character.setStrength(abilityValue);
                    break;
                case R.id.ability_dex_value:
                    character.setDexterity(abilityValue);
                    break;
                case R.id.ability_con_value:
                    character.setConstitution(abilityValue);
                    break;
                case R.id.ability_int_value:
                    character.setIntelligence(abilityValue);
                    break;
                case R.id.ability_wis_value:
                    character.setWisdom(abilityValue);
                    break;
                case R.id.ability_cha_value:
                    character.setCharisma(abilityValue);
                    break;
            }

            // update stats
            updateSheet(getView());
            // store changes
            characterDBUpdate();
        }
    }

    @Override
    public void onDelete() {
        PreferenceManager.getDefaultSharedPreferences(getView().getContext()).edit()
                .putBoolean(MainActivity.KEY_RELOAD_REQUIRED, true)
                .remove(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID).apply();
        DBHelper.getInstance(getContext()).deleteEntity(character);
        getActivity().finish();
    }

    @Override
    public void onNameChoosen(String name) {
        character.setName(name);
        TextView tv = getView().findViewById(R.id.sheet_main_namepicker);
        if(name != null) {
            tv.setText(character.getName());
        }
        // store changes
        characterDBUpdate();
    }

    @Override
    public void onRaceChosen(long raceId) {
        Race race = (Race)DBHelper.getInstance(getContext()).fetchEntity(raceId, RaceFactory.getInstance());
        TextView tv = getView().findViewById(R.id.sheet_main_racepicker);
        character.setRace(race);
        if(race != null) {
            tv.setText(race.getName());
        }
        // update stats
        updateSheet(getView());
        // store changes
        characterDBUpdate();
    }

    @Override
    public void onClassDeleted(long classId) {
        Class cl = (Class)DBHelper.getInstance(getContext()).fetchEntity(classId, ClassFactory.getInstance());
        if(cl != null) {
            character.removeClass(cl);
            updateClassPickers(getView());
        }
        // store changes
        characterDBUpdate();
    }

    @Override
    public void onClassChosen(long classId, int level) {
        Class cl = (Class)DBHelper.getInstance(getContext()).fetchEntity(classId, ClassFactory.getInstance());
        if(cl != null) {
            character.addOrSetClass(cl, level);
            updateClassPickers(getView());
        }
        // store changes
        characterDBUpdate();
    }

    @Override
    public void onAddModif(Character.CharacterModif modif) {
        if(modif != null && modif.isValid()) {
            character.addModif(modif);
            updateModifsPickers(getView());
            modifStatesIntoPreferences();
            // store changes
            characterDBUpdate();
        }
    }

    @Override
    public void onDeleteModif(int modifIdx) {
        Character.CharacterModif modif = character.getModif(modifIdx);
        if(modif != null) {
            character.deleteModif(modif);
            updateModifsPickers(getView());
            modifStatesIntoPreferences();
            // store changes
            characterDBUpdate();
        }
    }

    @Override
    public void onModifUpdated(int modifIdx, Character.CharacterModif newModif) {
        Character.CharacterModif modif = character.getModif(modifIdx);
        if(modif != null) {
            modif.update(newModif);
            updateModifsPickers(getView());
            // store changes
            characterDBUpdate();
        }
    }

    @Override
    public void onSaveHP(int value) {
        character.setHitpoints(value);
        ((TextView)getView().findViewById(R.id.hitpoint_value)).setText(String.valueOf(value));
        // store changes
        characterDBUpdate();
    }

    @Override
    public void onSaveSpeed(int value) {
        character.setSpeed(value);
        ((TextView)getView().findViewById(R.id.speed_value)).setText(String.valueOf(value));
        // store changes
        characterDBUpdate();
    }

}
