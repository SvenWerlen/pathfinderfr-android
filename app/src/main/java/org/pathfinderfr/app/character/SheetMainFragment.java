package org.pathfinderfr.app.character;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.wefika.flowlayout.FlowLayout;

import org.pathfinderfr.R;
import org.pathfinderfr.app.ItemDetailActivity;
import org.pathfinderfr.app.ItemDetailFragment;
import org.pathfinderfr.app.MainActivity;
import org.pathfinderfr.app.character.FragmentRacePicker.OnFragmentInteractionListener;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.CharacterImportExport;
import org.pathfinderfr.app.database.entity.CharacterItem;
import org.pathfinderfr.app.database.entity.CharacterItemFactory;
import org.pathfinderfr.app.database.entity.Class;
import org.pathfinderfr.app.database.entity.ClassArchetype;
import org.pathfinderfr.app.database.entity.ClassArchetypesFactory;
import org.pathfinderfr.app.database.entity.ClassFactory;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.EquipmentFactory;
import org.pathfinderfr.app.database.entity.MagicItemFactory;
import org.pathfinderfr.app.database.entity.Modification;
import org.pathfinderfr.app.database.entity.ModificationFactory;
import org.pathfinderfr.app.database.entity.Race;
import org.pathfinderfr.app.database.entity.RaceFactory;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.SkillFactory;
import org.pathfinderfr.app.database.entity.Weapon;
import org.pathfinderfr.app.event.MessageBroker;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;
import org.pathfinderfr.app.util.PreferenceUtil;
import org.pathfinderfr.app.util.StringUtil;
import org.pathfinderfr.app.util.Triplet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;


/**
 * Skill tab on character sheet
 */
public class SheetMainFragment extends Fragment implements MessageBroker.ISender,
        FragmentAbilityPicker.OnFragmentInteractionListener, FragmentAbilityCalc.OnFragmentInteractionListener,
        OnFragmentInteractionListener, FragmentClassPicker.OnFragmentInteractionListener,
        FragmentModifPicker.OnFragmentInteractionListener, FragmentHitPointsPicker.OnFragmentInteractionListener,
        FragmentSpeedPicker.OnFragmentInteractionListener, FragmentNamePicker.OnFragmentInteractionListener,
        FragmentDeleteAction.OnFragmentInteractionListener, FragmentInventoryPicker.OnFragmentInteractionListener,
        FragmentInfosPicker.OnFragmentInteractionListener, FragmentMoneyPicker.OnFragmentInteractionListener,
        FragmentSync.OnFragmentInteractionListener, FragmentContextMenu.OnFragmentInteractionListener {

    private static final String ARG_CHARACTER_ID      = "character_id";
    protected static final String DIALOG_PICK_ABILITY   = "ability-picker";
    protected static final String DIALOG_CALC_ABILITY   = "ability-calc";
    protected static final String DIALOG_DELETE_ACTION  = "delete-action";
    protected static final String DIALOG_PICK_NAME      = "name-picker";
    protected static final String DIALOG_PICK_RACE      = "race-picker";
    protected static final String DIALOG_PICK_INFOS     = "infos-picker";
    protected static final String DIALOG_PICK_CLASS     = "class-picker";
    protected static final String DIALOG_PICK_HP        = "hitpoint-picker";
    protected static final String DIALOG_PICK_SPEED     = "speed-picker";
    protected static final String DIALOG_PICK_MODIFS    = "modifs-picker";
    protected static final String DIALOG_PICK_MONEY     = "money-picker";
    protected static final String DIALOG_PICK_INVENTORY = "inventory-picker";
    protected static final String DIALOG_SYNC_ACTION    = "sync-action";
    protected static final String DIALOG_CONTEXT_MENU   = "contextmenu";

    private static final int CONTEXT_EQUIP = 1;
    private static final int CONTEXT_SHOWREF = 2;
    private static final int CONTEXT_EDIT = 3;
    private static final int CONTEXT_ENABLE = 100;

    public static final String KEY_INVENTORY_SHOWALL = "pref_inventory_showall";


    private Character character;
    private List<TextView> classPickers;
    private List<ImageView> modifPickers;

    private TableLayout weapons;
    private TextView weaponNameExample;
    private TextView weaponTextExample;
    private TableLayout inventory;
    private ImageView inventoryIconExample;
    private ImageView inventoryIconBagExample;
    private TextView inventoryNameExample;
    private TextView inventoryLocationExample;

    private boolean isFABOpen;
    private LinearLayout fabClass;
    private LinearLayout fabModif;
    private LinearLayout fabItem;
    private LinearLayout fabItemMagic;
    private LinearLayout fabItemManual;
    private TextView fabClassText;
    private TextView fabModifText;
    private TextView fabItemText;
    private TextView fabItemMagicText;
    private TextView fabItemManualText;

    private long characterId;
    ProfileListener listener;

    private boolean refreshNeeded;

    private SheetFeatFragment.Callbacks mCallbacks;


    public interface Callbacks {
        void onRefreshRequest();
    }

    public SheetMainFragment() {
        // Required empty public constructor
        classPickers = new ArrayList<>();
        modifPickers = new ArrayList<>();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Activities containing this fragment must implement its callbacks
        if(context instanceof SheetFeatFragment.Callbacks) {
            mCallbacks = (SheetFeatFragment.Callbacks) context;
        }
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
        refreshNeeded = false;
    }

    protected Character getCharacter() {
        return character;
    }

    private static void showTooltip(View v, String message) {
        Toast t = Toast.makeText(v.getContext(), message, Toast.LENGTH_SHORT);
        int[] xy = new int[2];
        v.getLocationOnScreen(xy);
        t.setGravity(Gravity.TOP|Gravity.LEFT, xy[0], xy[1]);
        t.show();
    }


    public static String generateOtherBonusText(Character character, int modifId, String tooltipTemplate) {
        return generateOtherBonusText(character, modifId, tooltipTemplate, 0);
    }

    /**
     * Generates a string (tables rows) for each individual other bonus
     * @param character character object
     * @param modifId modif identifier
     * @param tooltipTemplate template for a row entry
     * @param itemId applied for specific weapon
     * @return
     */
    public static String generateOtherBonusText(Character character, int modifId, String tooltipTemplate, long itemId) {
        List<Modification> modifs = character.getModifsForId(modifId);
        StringBuffer buf = new StringBuffer();
        for(Modification modif: modifs) {
            if((modif.getItemId() == 0 || modif.getItemId() == itemId) && modif.isEnabled()) {
                buf.append(String.format(tooltipTemplate, modif.getName(), modif.getModif(0).second));
            }
        }
        return buf.toString();
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

        view.findViewById(R.id.actionShare).setOnClickListener(listener);
        view.findViewById(R.id.actionSync).setOnClickListener(listener);
        view.findViewById(R.id.actionDelete).setOnClickListener(listener);
        view.findViewById(R.id.sheet_main_namepicker).setOnClickListener(listener);
        view.findViewById(R.id.sheet_main_racepicker).setOnClickListener(listener);
        view.findViewById(R.id.sheet_main_otherpicker).setOnClickListener(listener);
        view.findViewById(R.id.sheet_main_classpicker).setVisibility(View.GONE);

        final CharacterSheetActivity act = ((CharacterSheetActivity)getActivity());

        // FAB
        FloatingActionButton fab = view.findViewById(R.id.fabAdd);
        fabClass = view.findViewById(R.id.fabAddClass); fabClass.setOnClickListener(listener);
        fabModif = view.findViewById(R.id.fabAddModif); fabModif.setOnClickListener(listener);
        fabItem = view.findViewById(R.id.fabAddItem); fabItem.setOnClickListener(listener);
        fabItemMagic = view.findViewById(R.id.fabAddMagic); fabItemMagic.setOnClickListener(listener);
        fabItemManual = view.findViewById(R.id.fabAddObject); fabItemManual.setOnClickListener(listener);
        fabClassText = view.findViewById(R.id.fabAddClassText); fabClassText.setVisibility(View.GONE);
        fabModifText = view.findViewById(R.id.fabAddModifText); fabModifText.setVisibility(View.GONE);
        fabItemText = view.findViewById(R.id.fabAddItemText); fabItemText.setVisibility(View.GONE);
        fabItemMagicText = view.findViewById(R.id.fabAddMagicText); fabItemMagicText.setVisibility(View.GONE);
        fabItemManualText = view.findViewById(R.id.fabAddObjectText); fabItemManualText.setVisibility(View.GONE);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });

        // ACTIONS
        ImageView actionPin = view.findViewById(R.id.actionPin);
        actionPin.setOnClickListener(listener);
        actionPin.setBackground(null);
        actionPin.setImageResource(R.drawable.ic_pin);
        if(character.getId() == PreferenceManager.getDefaultSharedPreferences(view.getContext()).getLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, 0L)) {
            actionPin.setColorFilter(view.getContext().getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
        } else {
            actionPin.setColorFilter(view.getContext().getResources().getColor(R.color.colorDisabled), PorterDuff.Mode.SRC_ATOP);
        }
        final View viewInfos = view.findViewById(R.id.sheet_main_profile_infos);
        final View viewStats = view.findViewById(R.id.sheet_main_statistics);
        final View viewInventory = view.findViewById(R.id.sheet_main_inventory);
        final View viewModifLabel = view.findViewById(R.id.sheet_main_modif_label);
        final View viewModifPicker = view.findViewById(R.id.sheet_main_modifpicker);
        final ImageView toggleview = view.findViewById(R.id.actionToggle);
        final View viewSummary = view.findViewById(R.id.sheet_main_summary_view);
        toggleview.setBackground(null);
        toggleview.setImageResource(R.drawable.ic_toggle_pos1);
        toggleview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean viewModeEdit = viewInfos.getVisibility() == View.GONE;
                viewInfos.setVisibility(viewModeEdit ? View.VISIBLE: View.GONE);
                viewModifLabel.setVisibility(viewModeEdit ? View.VISIBLE: View.GONE);
                viewModifPicker.setVisibility(viewModeEdit ? View.VISIBLE: View.GONE);
                viewStats.setVisibility(viewModeEdit ? View.VISIBLE: View.GONE);
                viewInventory.setVisibility(viewModeEdit ? View.VISIBLE: View.GONE);
                toggleview.setImageResource(viewModeEdit ? R.drawable.ic_toggle_pos1 : R.drawable.ic_toggle_pos2);
                viewSummary.setVisibility(viewModeEdit ? View.GONE: View.VISIBLE);
                if(viewModeEdit) {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().
                            remove(CharacterSheetActivity.PREF_MAIN_VIEW_SUMMARY).apply();
                } else {
                    PreferenceManager.getDefaultSharedPreferences(getContext()).edit().
                            putBoolean(CharacterSheetActivity.PREF_MAIN_VIEW_SUMMARY, true).apply();
                }
            }
        });
        view.findViewById(R.id.actionPDF).setOnClickListener(listener);
        // initialize view
        if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(CharacterSheetActivity.PREF_MAIN_VIEW_SUMMARY, false)) {
            viewInfos.setVisibility(View.GONE);
            viewModifLabel.setVisibility(View.GONE);
            viewModifPicker.setVisibility(View.GONE);
            viewStats.setVisibility(View.GONE);
            viewInventory.setVisibility(View.GONE);
            toggleview.setImageResource(R.drawable.ic_toggle_pos2);
            viewSummary.setVisibility(View.VISIBLE);
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
                        generateOtherBonusText(character, Modification.MODIF_COMBAT_INI, tooltipModif),
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
                        character.getAdditionalBonus(Modification.MODIF_COMBAT_AC_ARMOR),
                        character.getAdditionalBonus(Modification.MODIF_COMBAT_AC_SHIELD),
                        character.getDexterityModif(), // dex modif
                        character.getSizeModifierArmorClass(),
                        character.getAdditionalBonus(Modification.MODIF_COMBAT_AC_NATURAL),
                        character.getAdditionalBonus(Modification.MODIF_COMBAT_AC_PARADE),
                        generateOtherBonusText(character, Modification.MODIF_COMBAT_AC, tooltipModif), // others
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
                        generateOtherBonusText(character, Modification.MODIF_COMBAT_MAG, tooltipModif),
                        character.getMagicResistance()));
            }
        });

        // HIT POINTS
        view.findViewById(R.id.other_hp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_hitpoints));}
        });
        view.findViewById(R.id.hitpoint_value).setOnClickListener(listener);
        view.findViewById(R.id.other_hptemp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_hitpointstemp));}
        });
        view.findViewById(R.id.hitpointtemp_value).setOnClickListener(listener);

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
                    Triplet<Class, ClassArchetype,Integer> cl = character.getClass(i);
                    Class.Level lvl = cl.first.getLevel(cl.third);
                    if(lvl != null) {
                        text.append(String.format(babTooltipEntry, cl.first.getName(), cl.third, lvl.getBaseAttackBonusAsString() ));
                    }
                }
                act.showTooltip(babTooltipTitle,String.format(
                        babTooltipContent,
                        text,
                        "", // other
                        character.getBaseAttackBonusAsString()));
            }
        });

        // Weapons
        weapons = view.findViewById(R.id.weapons_table);
        weaponNameExample = view.findViewById(R.id.weapon_name);
        weaponTextExample = view.findViewById(R.id.weapon_bonus);

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
                        character.getSizeModifierManeuver(), // size
                        generateOtherBonusText(character, Modification.MODIF_COMBAT_CMB, tooltipModif), // other
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
                        character.getSizeModifierManeuver(), // size
                        generateOtherBonusText(character, Modification.MODIF_COMBAT_CMD, tooltipModif), // other
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
                    Triplet<Class, ClassArchetype,Integer> cl = character.getClass(i);
                    Class.Level lvl = cl.first.getLevel(cl.third);
                    if(lvl != null) {
                        text.append(String.format(savTooltipEntry, cl.first.getNameShort(), cl.third, lvl.getFortitudeBonus()));
                    }
                }
                act.showTooltip(
                        String.format(savTooltipTitle,getResources().getString(R.string.sheet_savingthrows_fortitude)),
                        String.format(savTooltipContent,
                                text,
                                getResources().getString(R.string.sheet_ability_constitution).toLowerCase(), character.getConstitutionModif(),
                                character.getAdditionalBonus(Modification.MODIF_SAVES_MAG_ALL) + character.getAdditionalBonus(Modification.MODIF_SAVES_MAG_FOR),
                                generateOtherBonusText(character, Modification.MODIF_SAVES_ALL, tooltipModif)
                                        + generateOtherBonusText(character, Modification.MODIF_SAVES_FOR, tooltipModif), // other
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
                    Triplet<Class, ClassArchetype,Integer> cl = character.getClass(i);
                    Class.Level lvl = cl.first.getLevel(cl.third);
                    if(lvl != null) {
                        text.append(String.format(savTooltipEntry, cl.first.getNameShort(), cl.third, lvl.getReflexBonus()));
                    }
                }
                act.showTooltip(
                        String.format(savTooltipTitle,getResources().getString(R.string.sheet_savingthrows_reflex)),
                        String.format(savTooltipContent,
                                text,
                                getResources().getString(R.string.sheet_ability_dexterity).toLowerCase(), character.getDexterityModif(),
                                character.getAdditionalBonus(Modification.MODIF_SAVES_MAG_ALL) + character.getAdditionalBonus(Modification.MODIF_SAVES_MAG_REF),
                                generateOtherBonusText(character, Modification.MODIF_SAVES_ALL, tooltipModif)
                                        + generateOtherBonusText(character, Modification.MODIF_SAVES_REF, tooltipModif), // other
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
                    Triplet<Class, ClassArchetype,Integer> cl = character.getClass(i);
                    Class.Level lvl = cl.first.getLevel(cl.third);
                    if(lvl != null) {
                        text.append(String.format(savTooltipEntry, cl.first.getNameShort(), cl.third, lvl.getWillBonus()));
                    }
                }
                act.showTooltip(
                        String.format(savTooltipTitle,getResources().getString(R.string.sheet_savingthrows_will)),
                        String.format(savTooltipContent,
                                text,
                                getResources().getString(R.string.sheet_ability_wisdom).toLowerCase(), character.getWisdomModif(),
                                character.getAdditionalBonus(Modification.MODIF_SAVES_MAG_ALL) + character.getAdditionalBonus(Modification.MODIF_SAVES_MAG_WIL),
                                generateOtherBonusText(character, Modification.MODIF_SAVES_ALL, tooltipModif)
                                        + generateOtherBonusText(character, Modification.MODIF_SAVES_WIL, tooltipModif), // other
                                character.getSavingThrowsWillTotal()));
            }
        });

        // MONEY
        view.findViewById(R.id.money_cp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_money_cupperpieces));}
        });
        view.findViewById(R.id.money_sp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_money_silverpieces));}
        });
        view.findViewById(R.id.money_gp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_money_goldpieces));}
        });
        view.findViewById(R.id.money_pp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { showTooltip(v, getResources().getString(R.string.sheet_money_platinepieces));}
        });
        view.findViewById(R.id.money_cp_value).setOnClickListener(listener);
        view.findViewById(R.id.money_sp_value).setOnClickListener(listener);
        view.findViewById(R.id.money_gp_value).setOnClickListener(listener);
        view.findViewById(R.id.money_pp_value).setOnClickListener(listener);

        // update name
        TextView nameTv = view.findViewById(R.id.sheet_main_namepicker);
        if(character.getName() != null) {
            if(character.getPlayer() != null) {
                nameTv.setText(String.format("%s (%s)", character.getName(), character.getPlayer()));
            } else {
                nameTv.setText(character.getName());
            }
        }

        // update race
        TextView raceTv = view.findViewById(R.id.sheet_main_racepicker);
        if(character.getRace() != null) {
            raceTv.setText(character.getRace().getName());
        }

        // update infos
        updateAdditionalInfos(view);
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
            FragmentInfosPicker fragInfos = (FragmentInfosPicker)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_PICK_INFOS);
            if (fragInfos != null) {
                fragInfos.setListener(this);
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
            FragmentAbilityCalc fragAbilityCalc = (FragmentAbilityCalc)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_CALC_ABILITY);
            if (fragAbilityCalc != null) {
                fragAbilityCalc.setListener(this);
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
            FragmentInventoryPicker fragInventory = (FragmentInventoryPicker)getActivity().getSupportFragmentManager()
                    .findFragmentByTag(DIALOG_PICK_INVENTORY);
            if (fragInventory != null) {
                fragInventory.setListener(this);
            }
        }

        // inventory
        inventory = view.findViewById(R.id.sheet_inventory_table);
        inventoryIconExample = view.findViewById(R.id.sheet_inventory_example_icon);
        inventoryIconBagExample = view.findViewById(R.id.sheet_inventory_unequiped);
        inventoryNameExample = view.findViewById(R.id.sheet_inventory_example_name);
        inventoryLocationExample = view.findViewById(R.id.sheet_inventory_example_location);
        updateInventory(view);

        view.findViewById(R.id.sheet_main_inventory_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(v.getContext());
                // toggle
                boolean showAll = !prefs.getBoolean(KEY_INVENTORY_SHOWALL, true);
                if(showAll) {
                    prefs.edit().remove(KEY_INVENTORY_SHOWALL).apply();
                } else {
                    prefs.edit().putBoolean(KEY_INVENTORY_SHOWALL, false).apply();
                }
                updateInventory(SheetMainFragment.this.getView());
            }
        });

        // fat fingers
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        int base = 32;
        float scale = 1f;
        try {
            scale = (Integer.parseInt(preferences.getString(MainActivity.PREF_FATFINGERS, "0"))/100f);
        } catch(NumberFormatException nfe) {}
        if(scale > 1) {
            int minHeight = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, base * scale, view.getResources().getDisplayMetrics());

            FragmentUtil.adaptForFatFingers(nameTv, 0, scale);
            FragmentUtil.adaptForFatFingers(raceTv, 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.sheet_main_otherpicker), 0, scale);
            for (TextView tv : classPickers) {
                FragmentUtil.adaptForFatFingers(tv, 0, scale);
            }
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.sheet_main_modifpicker), 0, scale);

            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_str), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_str_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_str_modif), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_dex), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_dex_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_dex_modif), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_con), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_con_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_con_modif), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_int), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_int_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_int_modif), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_wis), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_wis_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_wis_modif), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_cha), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_cha_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.ability_cha_modif), minHeight, scale);

            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.other_ini), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.initiative_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.other_ac), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.armorclass_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.other_hp), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.hitpoint_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.other_hptemp), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.hitpointtemp_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.other_mag), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.magicresistance_value), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.other_speed), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.speed_value), minHeight, scale);

            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_ref), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_reflex_total), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_reflex), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_reflex_ability), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_for), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_fortitude_total), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_fortitude), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_fortitude_ability), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_wil), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_will_total), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_will), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.savingthrows_will_ability), minHeight, scale);

            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.combat_bab), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.base_attack_bonus_value), minHeight, scale);

            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.combat_cmb), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.combat_cmb_total), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.combat_cmb_bab), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.combat_cmb_ability), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.combat_cmd), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.combat_cmd_total), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.combat_cmd_bab), minHeight, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.combat_cmd_ability), minHeight, scale);

            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.money_cp), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.money_cp_value), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.money_sp), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.money_sp_value), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.money_gp), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.money_gp_value), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.money_pp), 0, scale);
            FragmentUtil.adaptForFatFingers((TextView) view.findViewById(R.id.money_pp_value), 0, scale);
        }

        return view;
    }

    private void showFABMenu() {
        isFABOpen=true;
        fabClass.animate().translationY(-550);
        fabModif.animate().translationY(-450);
        fabItem.animate().translationY(-350);
        fabItemManual.animate().translationY(-250);
        fabItemMagic.animate().translationY(-150);
        fabClassText.setVisibility(View.VISIBLE);
        fabModifText.setVisibility(View.VISIBLE);
        fabItemText.setVisibility(View.VISIBLE);
        fabItemManualText.setVisibility(View.VISIBLE);
        fabItemMagicText.setVisibility(View.VISIBLE);
    }

    private void closeFABMenu() {
        isFABOpen=false;
        fabClass.animate().translationY(0);
        fabModif.animate().translationY(0);
        fabItem.animate().translationY(0);
        fabItemMagic.animate().translationY(0);
        fabItemManual.animate().translationY(0);
        fabClassText.setVisibility(View.GONE);
        fabModifText.setVisibility(View.GONE);
        fabItemText.setVisibility(View.GONE);
        fabItemManualText.setVisibility(View.GONE);
        fabItemMagicText.setVisibility(View.GONE);
    }



    private void updateAdditionalInfos(View view) {
        TextView infosTv = view.findViewById(R.id.sheet_main_otherpicker);
        Resources res = view.getResources();

        StringBuffer buf = new StringBuffer();
        if(character.getSex() > 0 && character.getSex() <= Character.SEX_F) {
            buf.append(res.getStringArray(R.array.sex)[character.getSex()-1]).append(", ");
        }
        if(character.getAge() > 0) {
            buf.append(String.format(res.getString(R.string.summary_age), character.getAge())).append(", ");
        }
        if(character.getAlignment() > 0 && character.getAlignment() <= Character.ALIGN_CE) {
            buf.append(res.getStringArray(R.array.alignment)[character.getAlignment()-1]).append(", ");
        }
        if(character.getSizeType() > 0 && character.getSizeType() <= Character.SIZE_COLO_LONG) {
            String sizeName = res.getStringArray(R.array.size_types)[character.getSizeType()];
            int idxB = sizeName.indexOf('(');
            int idxE = sizeName.indexOf(')');
            if(idxB > 0 && idxE > 0) {
                buf.append(String.format(res.getString(R.string.size_type), sizeName.substring(idxB + 1, idxE))).append(", ");
            }
        }
//        if(character.getHeight() > 0) {
//            buf.append(String.format(res.getString(R.string.summary_height), character.getHeight())).append(", ");
//        }
//        if(character.getWeight() > 0) {
//            buf.append(String.format(res.getString(R.string.summary_weight), character.getWeight())).append(", ");
//        }

        if(buf.length() > 0) {
            infosTv.setText(buf.substring(0, buf.length()-2));
        } else {
            infosTv.setText(res.getString(R.string.sheet_click_to_fill));
        }
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
            Triplet<Class, ClassArchetype,Integer> cl = character.getClass(idx);
            if(cl != null) {
                tv.setText(cl.first.getName() + " " + cl.third);
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
        List<Modification> modifs = character.getModifications(0);
        int toCreate = modifs.size() - modifPickers.size();
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
            Modification modif = idx < modifs.size() ? modifs.get(idx) : null;
            if(modif != null) {
                final int resourceId = view.getResources().getIdentifier("modif_" + modif.getIcon(), "drawable",
                        view.getContext().getPackageName());
                iv.setTag(modif.getId());
                iv.setVisibility(View.VISIBLE);
                if(resourceId > 0) {
                    iv.setBackgroundColor(modif.isEnabled() ? colorEnabled : colorDisabled);
                    iv.setImageResource(resourceId);
                }
            } else {
                iv.setVisibility(View.GONE);
            }
            idx++;
        }
        // update stats
        updateSheet(view);
    }

    private void updateWeapons(View view) {
        weapons.removeViews(2, weapons.getChildCount()-2);

        boolean showAll = PreferenceManager.getDefaultSharedPreferences(view.getContext()).getBoolean(KEY_INVENTORY_SHOWALL, true);
        List<Weapon> weaponsList = character.getInventoryWeapons(!showAll);
        if(weaponsList.size() == 0) {
            weapons.setVisibility(View.GONE);
            return;
        }
        weapons.setVisibility(View.VISIBLE);

        // fat fingers
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        int base = 32;
        float scale = 1f;
        try {
            scale = (Integer.parseInt(preferences.getString(MainActivity.PREF_FATFINGERS, "0"))/100f);
        } catch(NumberFormatException nfe) {}

        Resources r = getResources();
        int px2 = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, r.getDisplayMetrics());

        final CharacterSheetActivity act = ((CharacterSheetActivity)getActivity());
        final String babTooltipTitle = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.bab.title");
        final String babTooltipEntry = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.bab.entry");
        final String tooltipBabModif = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("tooltip.babmodif.entry");

        for(final Weapon weapon : weaponsList) {
            final String attackBonus = weapon.isRanged() ? character.getAttackBonusRangeAsString(weapon.getId()) : character.getAttackBonusMeleeAsString(weapon.getId());
            String damageString = character.getDamage(weapon, weapon.getId());

            TableRow row = new TableRow(view.getContext());
            TextView name = FragmentUtil.copyExampleTextFragment(weaponNameExample);
            name.setText(weapon.getName());
            name.setLayoutParams(new TableRow.LayoutParams(name.getLayoutParams()));
            ((TableRow.LayoutParams)name.getLayoutParams()).span = 3;
            ((TableRow.LayoutParams)name.getLayoutParams()).setMargins(px2,px2,px2,px2);
            row.addView(name);
            TextView bonus = FragmentUtil.copyExampleTextFragment(weaponTextExample);
            bonus.setText(attackBonus);
            bonus.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            row.addView(bonus);
            TextView critical = FragmentUtil.copyExampleTextFragment(weaponTextExample);
            critical.setText(weapon.getCritical());
            critical.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            row.addView(critical);
            weapons.addView(row);
            // second part
            row = new TableRow(view.getContext());
            TextView type = FragmentUtil.copyExampleTextFragment(weaponTextExample);
            type.setText(weapon.getType());
            type.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            row.addView(type);
            TextView range = FragmentUtil.copyExampleTextFragment(weaponTextExample);
            range.setText(weapon.getRangeInMeters());
            range.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            row.addView(range);
            TextView ammo = FragmentUtil.copyExampleTextFragment(weaponTextExample);
            ammo.setText(weapon.getDescription());
            range.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            row.addView(ammo);
            TextView damage = FragmentUtil.copyExampleTextFragment(weaponTextExample);
            damage.setText(damageString);
            damage.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            damage.setLayoutParams(new TableRow.LayoutParams(damage.getLayoutParams()));
            ((TableRow.LayoutParams)damage.getLayoutParams()).span = 2;
            ((TableRow.LayoutParams)damage.getLayoutParams()).setMargins(px2,px2,px2,px2);
            row.addView(damage);
            weapons.addView(row);

            // fat fingers
            if(scale > 1) {
                int minHeight = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, base * scale, view.getResources().getDisplayMetrics());

                FragmentUtil.adaptForFatFingers(name, minHeight, scale);
                FragmentUtil.adaptForFatFingers(bonus, minHeight, scale);
                FragmentUtil.adaptForFatFingers(critical, minHeight, scale);
                FragmentUtil.adaptForFatFingers(type, minHeight, scale);
                FragmentUtil.adaptForFatFingers(range, minHeight, scale);
                FragmentUtil.adaptForFatFingers(ammo, minHeight, scale);
                FragmentUtil.adaptForFatFingers(damage, minHeight, scale);
            }

            final String tooltipContent = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty(weapon.isRanged() ? "tooltip.attranged.content" : "tooltip.attmelee.content");

            // listeners
            bonus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    StringBuffer text = new StringBuffer();
                    for(int i=0; i<character.getClassesCount(); i++) {
                        Triplet<Class, ClassArchetype,Integer> cl = character.getClass(i);
                        Class.Level lvl = cl.first.getLevel(cl.third);
                        if(lvl != null) {
                            text.append(String.format(babTooltipEntry, cl.first.getName(), cl.third, lvl.getBaseAttackBonusAsString() ));
                        }
                    }
                    act.showTooltip(babTooltipTitle,String.format(
                            tooltipContent,
                            text,
                            weapon.isRanged() ? character.getDexterityModif(weapon.getId()) : character.getStrengthModif(weapon.getId()),
                            character.getSizeModifierAttack(),
                            generateOtherBonusText(character,
                                    weapon.isRanged() ? Modification.MODIF_COMBAT_ATT_RANGED : Modification.MODIF_COMBAT_ATT_MELEE,
                                    tooltipBabModif,
                                    weapon.getId()), // other
                            attackBonus));
                }
            });

            name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // try to find matching inventory item
                    CharacterItem matchingItem = null;
                    for(CharacterItem item : character.getInventoryItems()) {
                        if(item.isWeapon() && item.getName().equals(weapon.getName())) {
                            matchingItem = item;
                            break;
                        }
                    }
                    // not found
                    if(matchingItem == null) {
                        return;
                    }

                    FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_INVENTORY);
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    DialogFragment newFragment = FragmentInventoryPicker.newInstance(SheetMainFragment.this);


                    Bundle arguments = new Bundle();
                    arguments.putLong(FragmentInventoryPicker.ARG_INVENTORY_ID, matchingItem.getId());
                    arguments.putLong(FragmentInventoryPicker.ARG_INVENTORY_CHARACID, character.getId());
                    arguments.putString(FragmentInventoryPicker.ARG_INVENTORY_NAME, matchingItem.getName());
                    arguments.putInt(FragmentInventoryPicker.ARG_INVENTORY_WEIGHT, matchingItem.getWeight());
                    arguments.putLong(FragmentInventoryPicker.ARG_INVENTORY_PRICE, matchingItem.getPrice());
                    arguments.putLong(FragmentInventoryPicker.ARG_INVENTORY_OBJID, matchingItem.getItemRef());
                    arguments.putInt(FragmentInventoryPicker.ARG_INVENTORY_CATEGORY, matchingItem.getCategory());
                    arguments.putInt(FragmentInventoryPicker.ARG_INVENTORY_LOCATION, matchingItem.getLocation());
                    arguments.putString(FragmentInventoryPicker.ARG_INVENTORY_INFOS, matchingItem.getAmmo());
                    arguments.putBoolean(FragmentInventoryPicker.ARG_INVENTORY_EQUIPED, matchingItem.isEquiped());
                    newFragment.setArguments(arguments);
                    newFragment.show(ft, DIALOG_PICK_INVENTORY);
                }
            });
        }
    }

    private int inventoryIcon(int category) {
        switch(category) {
            case CharacterItem.CATEGORY_EQUIPMENT: return R.drawable.ic_item_icon_equipment;
            case CharacterItem.CATEGORY_WEAPON_HAND: return R.drawable.ic_item_icon_weapons;
            case CharacterItem.CATEGORY_WEAPON_RANGED: return R.drawable.ic_item_icon_ranged;
            case CharacterItem.CATEGORY_ARMOR: return R.drawable.ic_item_icon_armors;
            case CharacterItem.CATEGORY_SHIELD: return R.drawable.ic_item_icon_shields;
            case CharacterItem.CATEGORY_CLOTH: return R.drawable.ic_item_icon_cloth;
            case CharacterItem.CATEGORY_POTION: return R.drawable.ic_item_icon_potion;
            case CharacterItem.CATEGORY_RING: return R.drawable.ic_item_icon_ring;
            case CharacterItem.CATEGORY_SCEPTER: return R.drawable.ic_item_icon_scepter;
            case CharacterItem.CATEGORY_SCROLL: return R.drawable.ic_item_icon_scroll;
            case CharacterItem.CATEGORY_STAFF: return R.drawable.ic_item_icon_staff;
            case CharacterItem.CATEGORY_WAND: return R.drawable.ic_item_icon_wand;
            case CharacterItem.CATEGORY_MAGIC: return R.drawable.ic_item_icon_magic;
            default: return R.drawable.ic_item_icon_equipment;
        }
    }

    private void updateInventory(View view) {
        inventory.removeAllViews();
        int rowId = 0;
        boolean showAll = PreferenceManager.getDefaultSharedPreferences(view.getContext()).getBoolean(KEY_INVENTORY_SHOWALL, true);
        List<CharacterItem> items = showAll ? character.getInventoryItems() : character.getEquipedItems();
        ((TextView)view.findViewById(R.id.sheet_main_inventory_header)).setText(showAll ? R.string.sheet_main_inventory_header_all : R.string.sheet_main_inventory_header_active);
        view.findViewById(R.id.sheet_main_inventory_header_filter).setVisibility(showAll ? View.GONE : View.VISIBLE);

        // determine size
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        int lineHeight = Integer.parseInt(prefs.getString(MainActivity.PREF_LINEHEIGHT, "0"));
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, lineHeight, view.getResources().getDisplayMetrics());

        for(CharacterItem item : items) {
            TableRow row = new TableRow(view.getContext());
            row.setMinimumHeight(height);
            row.setGravity(Gravity.CENTER);
            ImageView icon = FragmentUtil.copyExampleImageFragment(inventoryIconExample);
            TextView name = FragmentUtil.copyExampleTextFragment(inventoryNameExample);
            name.setText(item.getName());
            LinearLayout nameLayout = new LinearLayout(view.getContext());
            nameLayout.setOrientation(LinearLayout.VERTICAL);
            TextView dropHere = FragmentUtil.copyExampleTextFragment(inventoryNameExample);
            dropHere.setText(R.string.dragdrop_here);
            dropHere.setGravity(Gravity.CENTER);
            dropHere.setHeight(80);
            dropHere.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            dropHere.setTextColor(getResources().getColor(R.color.colorWhite));
            dropHere.setVisibility(View.GONE);
            dropHere.setPadding(30, 5, 30, 5);
            nameLayout.addView(dropHere);
            nameLayout.addView(name);
            icon.setImageDrawable(ContextCompat.getDrawable(view.getContext(), inventoryIcon(item.getCategory())));
            icon.setVisibility(item.getCategory() == CharacterItem.CATEGORY_UNCLASSIFIED ? View.INVISIBLE : View.VISIBLE);
            icon.setColorFilter(Color.BLACK);
            row.addView(icon);
            row.addView(nameLayout);
            if(item.isEquiped()) {
                TextView location = FragmentUtil.copyExampleTextFragment(inventoryLocationExample);
                location.setText(ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("sheet.inventory.location." + item.getLocation()));
                location.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                row.addView(location);
            } else {
                ImageView bagIcon = FragmentUtil.copyExampleImageFragment(inventoryIconBagExample);
                row.addView(bagIcon);
            }
            row.setBackgroundColor(ContextCompat.getColor(getContext(), rowId % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));
            row.setTag(new Triplet<TextView, TextView, Long>(dropHere, name, item.getId()));
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().openContextMenu(v);
                }
            });
            row.setOnLongClickListener(listener);
            row.setOnDragListener(listener);
            row.setOnClickListener(listener);
            inventory.addView(row);
            rowId++;
        }
        int totalWeight = (int)Math.ceil(character.getInventoryTotalWeight()/1000d);
        ((TextView)view.findViewById(R.id.sheet_inventory_item_totalweight)).setText(totalWeight + "kg");
        updateSheetSummary(view);
        updateMoneyValue(view);
        updateWeapons(view);
    }

    private void updateMoneyValue(View view) {
        long total = 0;
        for(CharacterItem item : character.getInventoryItems()) {
            total += item.getPrice();
        }
        total += character.getMoneyCP();
        total += character.getMoneySP() * 10;
        total += character.getMoneyGP() * 100;
        total += character.getMoneyPP() * 1000;

        String text = StringUtil.cost2String(total);
        ((TextView)view.findViewById(R.id.sheet_main_money_total_value)).setText(text);
    }

    private void updateSheetSummary(View view) {
        final String template = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.sheet.summary");
        final String templatePVt = ConfigurationUtil.getInstance(view.getContext()).getProperties().getProperty("template.sheet.summary.hptemp");
        Skill perc = (Skill)DBHelper.getInstance(view.getContext()).fetchEntityByName("Perception", SkillFactory.getInstance());
        String pvTemp = (character.getHitpointsTemp() > 0 ? String.format(templatePVt, character.getHitpointsTemp()): "");
        String text = String.format(template,
                character.getName(),
                character.getRaceName(), character.getClassNames(),
                character.getInitiative(), character.getSkillTotalBonus(perc),
                character.getArmorClass(),
                character.getArmorClassDetails(),
                character.getHitpoints(), pvTemp,
                character.getSavingThrowsReflexesTotal(),
                character.getSavingThrowsFortitudeTotal(),
                character.getSavingThrowsWillTotal(),
                character.getSpeedAsMeters(),
                character.getSpeed(),
                character.getAttackBonusAsString(true),
                character.getAttackBonusAsString(false),
                character.getStrength(),
                character.getDexterity(),
                character.getConstitution(),
                character.getIntelligence(),
                character.getWisdom(),
                character.getCharisma(),
                character.getBaseAttackBonusBest(),
                character.getCombatManeuverBonus(),
                character.getCombatManeuverDefense(),
                character.getFeatsAsString(),
                character.getSkillsAsString(),
                character.getInventoryAsString()
                );
        WebView content = view.findViewById(R.id.sheet_main_summary);
        text = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + text;
        content.loadDataWithBaseURL("file:///android_asset/", text, "text/html", "utf-8", null);
    }


    protected void updateSheet(View view) {
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
        ((TextView)view.findViewById(R.id.hitpointtemp_value)).setText(String.valueOf(character.getHitpointsTemp()));
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
        combatManBonusTotal.setText(String.valueOf(character.getCombatManeuverBonus()));
        combatManBonusBab.setText(String.valueOf(bab == null || bab.length == 0 ? 0: bab[0]));
        combatManBonusAbility.setText(String.valueOf(character.getStrengthModif()));
        combatManDefenseTotal.setText(String.valueOf(character.getCombatManeuverDefense()));
        combatManDefenseBab.setText(String.valueOf(bab == null || bab.length == 0 ? 0: bab[0]));
        combatManDefenseAbility.setText(String.valueOf(character.getStrengthModif()+character.getDexterityModif()));

        ((TextView)view.findViewById(R.id.money_cp_value)).setText(String.valueOf(character.getMoneyCP()));
        ((TextView)view.findViewById(R.id.money_sp_value)).setText(String.valueOf(character.getMoneySP()));
        ((TextView)view.findViewById(R.id.money_gp_value)).setText(String.valueOf(character.getMoneyGP()));
        ((TextView)view.findViewById(R.id.money_pp_value)).setText(String.valueOf(character.getMoneyPP()));

        updateSheetSummary(view);
        updateWeapons(view);
    }


    private static class ProfileListener implements View.OnClickListener, View.OnLongClickListener, View.OnDragListener {

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
                String player = parent.character.getPlayer();
                if(player != null) {
                    arguments.putString(FragmentNamePicker.ARG_PNAME, player);
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
            else if(v instanceof TextView && "infos".equals(v.getTag())) {
                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_INFOS);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentInfosPicker.newInstance(parent,
                    parent.character.getExperience(),
                    parent.character.getAlignment(), parent.character.getDivinity(),
                    parent.character.getOrigin(), parent.character.getSizeType(),
                    parent.character.getSex(), parent.character.getAge(),
                    parent.character.getHeight(), parent.character.getWeight(),
                    parent.character.getHair(), parent.character.getEyes(),
                    parent.character.getLanguages());
                newFragment.show(ft, DIALOG_PICK_INFOS);
                return;
            }
            else if("class".equals(v.getTag())) {
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

                Triplet<Class, ClassArchetype,Integer> selClass = null;

                // not found => new
                if(idx < parent.classPickers.size()) {
                    selClass = parent.character.getClass(idx);
                }

                // prepare parameters
                long[] excluded = parent.character.getOtherClassesIds(selClass == null ? -1 : selClass.first.getId());
                int maxLevel = 20 - parent.character.getOtherClassesLevel(selClass == null ? -1 : selClass.first.getId());

                Bundle arguments = new Bundle();
                if(selClass != null) {
                    arguments.putLong(FragmentClassPicker.ARG_CLASS_ID, selClass.first.getId());
                    if(selClass.second != null) {
                        arguments.putLong(FragmentClassPicker.ARG_ARCHETYPE_ID, selClass.second.getId());
                    }
                    arguments.putInt(FragmentClassPicker.ARG_CLASS_LVL, selClass.third);
                }
                arguments.putLongArray(FragmentClassPicker.ARG_CLASS_EXCL, excluded);
                arguments.putInt(FragmentClassPicker.ARG_CLASS_MAX_LVL, maxLevel);
                newFragment.setArguments(arguments);
                newFragment.show(ft, DIALOG_PICK_CLASS);
                return;
            }
            else if("modif".equals(v.getTag())) {
                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_MODIFS);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentModifPicker.newInstance(parent);
                Bundle arguments = new Bundle();
                arguments.putString(FragmentModifPicker.ARG_MODIF_ITEMS, parent.character.getInventoryItemsAsString());
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
                DialogFragment newFragment = FragmentHitPointsPicker.newInstance(parent, parent.character.getHitpoints(), parent.character.getHitpointsTemp());

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
                DialogFragment newFragment = FragmentSpeedPicker.newInstance(parent,
                        parent.character.getBaseSpeed(),
                        parent.character.getBaseSpeedWithArmor(),
                        parent.character.getBaseSpeedDig(),
                        parent.character.getBaseSpeedFly(),
                        parent.character.getBaseSpeedManeuverability());

                Bundle arguments = new Bundle();
                newFragment.setArguments(arguments);
                newFragment.show(ft, DIALOG_PICK_SPEED);
                return;
            }
            else if(v instanceof TextView && "money".equals(v.getTag())) {
                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_MONEY);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                DialogFragment newFragment = FragmentMoneyPicker.newInstance(parent,
                        parent.character.getMoneyCP(),
                        parent.character.getMoneySP(),
                        parent.character.getMoneyGP(),
                        parent.character.getMoneyPP());
                Bundle arguments = new Bundle();
                newFragment.setArguments(arguments);
                newFragment.show(ft, DIALOG_PICK_MONEY);
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
                else if(v.getId() == R.id.actionShare) {
                    // convert character to YAML
                    String characterYML = CharacterImportExport.exportCharacterAsYML(parent.character, parent.getContext());
                    if(characterYML == null) {
                        View root = parent.getActivity().findViewById(R.id.sheet_container);
                        if (root != null) {
                            Snackbar.make(root, parent.getView().getResources().getString(R.string.character_export_failed),
                                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }
                        return;
                    }

                    Log.i(SheetMainFragment.class.getSimpleName(), characterYML);
                    // save character to cache directory
                    try {
                        File cachePath = new File(parent.getContext().getCacheDir(), "characters");
                        cachePath.mkdirs(); // don't forget to make the directory
                        FileOutputStream stream = new FileOutputStream(cachePath + "/personnage.pfc");
                        stream.write(characterYML.getBytes());
                        stream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                    // read character from cache
                    File charPath = new File(parent.getContext().getCacheDir(), "characters");
                    File newFile = new File(charPath, "personnage.pfc");
                    Uri contentUri = FileProvider.getUriForFile(parent.getContext(), "org.pathfinderfr.app.fileprovider", newFile);

                    if (contentUri != null) {
                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        shareIntent.setType("text/plain");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                        parent.startActivity(Intent.createChooser(shareIntent, parent.getResources().getString(R.string.sheet_choose_app_export)));
                    }
                    return;
                }
                else if(v.getId() == R.id.actionPDF) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, GeneratePDFActivity.class);
                    intent.putExtra(GeneratePDFActivity.ARG_CHARACTER_ID, parent.character.getId());
                    context.startActivity(intent);
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
                else if(v.getId() == R.id.actionSync) {
                    FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_SYNC_ACTION);
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    DialogFragment newFragment = FragmentSync.newInstance(parent);

                    Bundle arguments = new Bundle();
                    String name = parent.character.getName();
                    if(name != null) {
                        arguments.putString(FragmentSync.ARG_NAME, name);
                    }
                    String uuid = parent.character.getShortUniqID();
                    if(uuid != null) {
                        arguments.putString(FragmentSync.ARG_UUID, uuid);
                    }
                    newFragment.setArguments(arguments);
                    newFragment.show(ft, DIALOG_SYNC_ACTION);
                    return;
                }
                // MODIFICATION ENABLED/DISABLED
                else {
                    ImageView icon = (ImageView) v;
                    Modification modif = parent.character.getModificationById((long) v.getTag());
                    if (modif != null) {
                        // toggle modification
                        modif.setEnabled(!modif.isEnabled());
                        final int colorDisabled = parent.getContext().getResources().getColor(R.color.colorBlack);
                        final int colorEnabled = parent.getContext().getResources().getColor(R.color.colorPrimaryDark);
                        if (icon.getDrawable() != null) {
                            icon.setBackgroundColor(modif.isEnabled() ? colorEnabled : colorDisabled);
                        }
                        parent.updateSheet(parent.getView());
                        // save new state
                        DBHelper.getInstance(parent.getContext()).updateEntity(modif, new HashSet<>(Arrays.asList(ModificationFactory.FLAG_ACTIVE)));
                    }
                    return;
                }
            }
            else if(v.getId() == R.id.fabAddObject) {
                if(parent.isFABOpen) {
                    FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_INVENTORY);
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    DialogFragment newFragment = FragmentInventoryPicker.newInstance(parent);

                    Bundle arguments = new Bundle();
                    newFragment.setArguments(arguments);
                    newFragment.show(ft, DIALOG_PICK_INVENTORY);
                }
                return;
            }
            else if(v.getId() == R.id.fabAddItem) {
                if(parent.isFABOpen) {
                    // set character as "selected"
                    PreferenceManager.getDefaultSharedPreferences(parent.getContext()).edit().
                            putLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, parent.character.getId()).
                            apply();
                    Context context = parent.getContext();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(MainActivity.KEY_CONTEXTUAL, true);
                    intent.putExtra(MainActivity.KEY_CONTEXTUAL_NAV, EquipmentFactory.FACTORY_ID);
                    context.startActivity(intent);
                    parent.refreshNeeded = true;
                }
                return;
            }
            else if(v.getId() == R.id.fabAddMagic) {
                if(parent.isFABOpen) {
                    // set character as "selected"
                    PreferenceManager.getDefaultSharedPreferences(parent.getContext()).edit().
                            putLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, parent.character.getId()).
                            apply();
                    Context context = parent.getContext();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.putExtra(MainActivity.KEY_CONTEXTUAL, true);
                    intent.putExtra(MainActivity.KEY_CONTEXTUAL_NAV, MagicItemFactory.FACTORY_ID);
                    context.startActivity(intent);
                    parent.refreshNeeded = true;
                }
                return;
            }
            // inventory items
            else if(v instanceof TableRow && v.getTag() != null) {
                Triplet<TextView, TextView, Long> itemTag = (Triplet<TextView, TextView, Long>)v.getTag();
                FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_CONTEXT_MENU);
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                ArrayList<Integer> menuIds = new ArrayList<>();
                ArrayList<Integer> icons = new ArrayList<>();
                ArrayList<Integer> colors = new ArrayList<>();
                ArrayList<String> titles = new ArrayList<>();
                Context ctx = parent.getContext();
                DialogFragment newFragment = FragmentContextMenu.newInstance(parent);
                Bundle arguments = new Bundle();
                CharacterItem item = (CharacterItem)DBHelper.getInstance(ctx).fetchEntity(itemTag.third, CharacterItemFactory.getInstance());
                if(item.isEquiped()) {
                    menuIds.add(CONTEXT_EQUIP); icons.add(R.drawable.ic_putin); colors.add(0);
                    titles.add(parent.getResources().getString(R.string.sheet_inventory_ctx_unequip));
                } else {
                    String location = ConfigurationUtil.getInstance(ctx).getProperties().getProperty("sheet.inventory.location." + item.getLocation());
                    menuIds.add(CONTEXT_EQUIP); icons.add(R.drawable.ic_takeout); colors.add(0);
                    titles.add(String.format(parent.getResources().getString(R.string.sheet_inventory_ctx_equip), location));
                }
                if(item.getItemRef() > 0) {
                    DBEntity ref = DBHelper.getInstance(ctx).fetchObjectEntity(item);
                    menuIds.add(CONTEXT_SHOWREF); icons.add(R.drawable.ic_zoom); colors.add(0);
                    titles.add(String.format(parent.getResources().getString(R.string.sheet_inventory_ctx_reference), ref.getName()));
                }
                menuIds.add(CONTEXT_EDIT); icons.add(R.drawable.ic_edit); colors.add(0);
                titles.add(parent.getResources().getString(R.string.sheet_inventory_ctx_edit));
                List<Modification> modifs = parent.character.getModifications(item.getId());
                for(Modification modif : modifs) {
                    final int resourceId = parent.getResources().getIdentifier("modif_" + modif.getIcon(), "drawable", ctx.getPackageName());
                    menuIds.add(CONTEXT_ENABLE + (int)modif.getId()); icons.add(resourceId);
                    if(!modif.isEnabled()) {
                        colors.add(ctx.getResources().getColor(R.color.colorBlack));
                        titles.add(String.format(parent.getResources().getString(R.string.sheet_inventory_ctx_enable), modif.getName()));
                    } else {
                        colors.add(ctx.getResources().getColor(R.color.colorPrimaryDark));
                        titles.add(String.format(parent.getResources().getString(R.string.sheet_inventory_ctx_disable), modif.getName()));
                    }
                }
                arguments.putLong(FragmentContextMenu.ARG_CONTEXT_ITEMID, item.getId());
                arguments.putString(FragmentContextMenu.ARG_CONTEXT_TITLE, item.getName());
                arguments.putIntegerArrayList(FragmentContextMenu.ARG_CONTEXT_ICONS, icons);
                arguments.putIntegerArrayList(FragmentContextMenu.ARG_CONTEXT_ICONSCOLOR, colors);
                arguments.putIntegerArrayList(FragmentContextMenu.ARG_CONTEXT_MENUIDS, menuIds);
                arguments.putStringArrayList(FragmentContextMenu.ARG_CONTEXT_NAMES, titles);
                newFragment.setArguments(arguments);
                newFragment.show(ft, DIALOG_CONTEXT_MENU);
                return;
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if(v instanceof ImageView) {
                ImageView icon = (ImageView)v;
                Modification modif = parent.character.getModificationById((Long)v.getTag());
                if(modif != null) {
                    FragmentTransaction ft = parent.getActivity().getSupportFragmentManager().beginTransaction();
                    Fragment prev = parent.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_MODIFS);
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.addToBackStack(null);
                    DialogFragment newFragment = FragmentModifPicker.newInstance(parent);

                    Bundle arguments = new Bundle();
                    arguments.putLong(FragmentModifPicker.ARG_MODIF_ID, (Long)v.getTag());
                    arguments.putString(FragmentModifPicker.ARG_MODIF_NAME, modif.getName());
                    ArrayList<Integer> modifIds = new ArrayList<>();
                    ArrayList<Integer> modifVals = new ArrayList<>();
                    for(int i = 0; i<modif.getModifCount(); i++) {
                        modifIds.add(modif.getModif(i).first);
                        modifVals.add(modif.getModif(i).second);
                    }
                    arguments.putIntegerArrayList(FragmentModifPicker.ARG_MODIF_IDS, modifIds);
                    arguments.putIntegerArrayList(FragmentModifPicker.ARG_MODIF_VALS, modifVals);
                    arguments.putLong(FragmentModifPicker.ARG_MODIF_ITEMID, modif.getItemId());
                    arguments.putString(FragmentModifPicker.ARG_MODIF_ITEMS, parent.character.getInventoryItemsAsString());
                    arguments.putString(FragmentModifPicker.ARG_MODIF_ICON, modif.getIcon());

                    newFragment.setArguments(arguments);
                    newFragment.show(ft, DIALOG_PICK_MODIFS);
                    return true;
                }
            } else {
                parent.getActivity().closeContextMenu();
                Triplet<TextView,TextView,Long> triplet = (Triplet<TextView,TextView,Long>)v.getTag();
                TextView tvName = triplet.second;
                ClipData.Item item = new ClipData.Item(triplet.third.toString());
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};

                ClipData dragData = new ClipData(tvName.getText(), mimeTypes, item);
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(tvName);

                //v.setVisibility(View.GONE);
                v.startDrag(dragData,myShadow,null,0);
                return true;
            }
            return false;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            Triplet<TextView,TextView,Long> triplet = (Triplet<TextView,TextView,Long>)v.getTag();

            switch(event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    break;

                case DragEvent.ACTION_DRAG_ENTERED:
                    triplet.first.setVisibility(View.VISIBLE);
                    break;

                case DragEvent.ACTION_DRAG_EXITED :
                    triplet.first.setVisibility(View.GONE);
                    break;

                case DragEvent.ACTION_DRAG_LOCATION  :
                    break;

                case DragEvent.ACTION_DRAG_ENDED   :
                    break;

                case DragEvent.ACTION_DROP:
                    triplet.first.setVisibility(View.GONE);
                    ClipData.Item item = event.getClipData().getItemAt(0);
                    long itemIdToMove = Long.parseLong(item.getText().toString());
                    long itemIdBefore = triplet.third;
                    CharacterItemFactory.moveItem(DBHelper.getInstance(parent.getContext()), parent.character.getInventoryItems(), itemIdToMove, itemIdBefore);
                    Log.i(SheetMainFragment.class.getSimpleName(), "Item was moved!");
                    parent.character.resyncInventory();
                    parent.updateInventory(parent.getView());
                    break;
                default: break;
            }
            return true;
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
    public void onAbilityCalcChosen() {
        FragmentTransaction ft = this.getActivity().getSupportFragmentManager().beginTransaction();
        Fragment prev = this.getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_CALC_ABILITY);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        DialogFragment newFragment = FragmentAbilityCalc.newInstance(this);

        Bundle arguments = new Bundle();
        if(character.getRace() != null) {
            arguments.putLong(FragmentAbilityCalc.ARG_RACE_ID, character.getRace().getId());
        }
        newFragment.setArguments(arguments);
        newFragment.show(ft, DIALOG_CALC_ABILITY);
    }

    @Override
    public void onAbilityValueChosen(int str, int dex, int con, int intel, int wis, int cha,
                                     int strR, int dexR, int conR, int intR, int wisR, int chaR) {
        character.setStrength(str);
        character.setDexterity(dex);
        character.setConstitution(con);
        character.setIntelligence(intel);
        character.setWisdom(wis);
        character.setCharisma(cha);

        if(strR != 0 || dexR != 0 || conR != 0 || intR != 0 || wisR != 0 || chaR != 0) {
            List<Pair<Integer,Integer>> modifs = new ArrayList<>();
            if(strR != 0) {
                modifs.add(new Pair<>(Modification.MODIF_ABILITY_STR, strR));
            }
            if(dexR != 0) {
                modifs.add(new Pair<>(Modification.MODIF_ABILITY_DEX, dexR));
            }
            if(conR != 0) {
                modifs.add(new Pair<>(Modification.MODIF_ABILITY_CON, conR));
            }
            if(intR != 0) {
                modifs.add(new Pair<>(Modification.MODIF_ABILITY_INT, intR));
            }
            if(wisR != 0) {
                modifs.add(new Pair<>(Modification.MODIF_ABILITY_WIS, wisR));
            }
            if(chaR != 0) {
                modifs.add(new Pair<>(Modification.MODIF_ABILITY_CHA, chaR));
            }
            String modifLabel = ConfigurationUtil.getInstance(getView().getContext()).getProperties().getProperty("ability.calc.modifs.title");
            onAddModif(new Modification(modifLabel, modifs, "rollingdices", true));
        }

        // update modifs
        updateModifsPickers(getView());
        // update stats
        updateSheet(getView());
        // store changes
        characterDBUpdate();
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
    public void onNameChoosen(String name, String playerName) {
        character.setName(name);
        character.setPlayer(playerName == null || playerName.length() == 0 ? null : playerName);
        TextView nameTv = getView().findViewById(R.id.sheet_main_namepicker);
        if(character.getName() != null) {
            if(character.getPlayer() != null) {
                nameTv.setText(String.format("%s (%s)", character.getName(), character.getPlayer()));
            } else {
                nameTv.setText(character.getName());
            }
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
    public void onClassChosen(long classId, long archetypeId, int level) {
        Class cl = (Class)DBHelper.getInstance(getContext()).fetchEntity(classId, ClassFactory.getInstance());
        ClassArchetype arch = null;
        if(archetypeId > 0) {
            arch = (ClassArchetype) DBHelper.getInstance(getContext()).fetchEntity(archetypeId, ClassArchetypesFactory.getInstance());
        }
        if(cl != null) {
            character.addOrSetClass(cl, arch, level);
            updateClassPickers(getView());
        }
        // store changes
        characterDBUpdate();
    }

    @Override
    public void onAddModif(Modification modif) {
        if(modif != null && modif.isValid()) {
            modif.setCharacterId(character.getId());
            if(DBHelper.getInstance(getContext()).insertEntity(modif) > 0) {
                character.resyncModifs();
                updateModifsPickers(getView());
                updateSheet(getView());
            }
        }
    }

    @Override
    public void onDeleteModif(long modifId) {
        Modification modif = character.getModificationById(modifId);
        if(modif != null && DBHelper.getInstance(getContext()).deleteEntity(modif)) {
            character.resyncModifs();
            updateModifsPickers(getView());
            updateSheet(getView());
        }
    }

    @Override
    public void onModifUpdated(long modifId, Modification newModif) {
        Modification modif = character.getModificationById(modifId);
        if(modif != null) {
            newModif.setId(modifId);
            newModif.setCharacterId(character.getId());
            newModif.setEnabled(modif.isEnabled());
            character.resyncModifs();
            DBHelper.getInstance(getContext()).updateEntity(newModif);
            updateModifsPickers(getView());
            updateSheet(getView());
        }
    }

    @Override
    public void onSaveHP(int value, int valueTemp) {
        character.setHitpoints(value);
        character.setHitpointsTemp(valueTemp);
        ((TextView)getView().findViewById(R.id.hitpoint_value)).setText(String.valueOf(value));
        ((TextView)getView().findViewById(R.id.hitpointtemp_value)).setText(String.valueOf(valueTemp));
        // update sheet
        updateSheet(getView());
        // store changes
        characterDBUpdate();
    }

    @Override
    public void onSaveSpeed(int speed, int speedArmor, int speedDig, int speedFly, int speedManeuver) {
        character.setSpeed(speed);
        character.setSpeedWithArmor(speedArmor);
        character.setSpeedDig(speedDig);
        character.setSpeedFly(speedFly);
        System.out.println("Maneuver: " + speedManeuver);
        character.setSpeedManeuverability(speedManeuver);
        ((TextView)getView().findViewById(R.id.speed_value)).setText(String.valueOf(speed));
        // update sheet
        updateSheet(getView());
        // store changes
        characterDBUpdate();
    }

    @Override
    public void onSaveInfos(int xp, int alignment, String divinity, String origin, int sizeType, int sex, int age, int height, int weight, String hair, String eyes, String lang) {
        boolean refresh = sizeType != character.getSizeType();
        character.setExperience(xp);
        character.setAlignment(alignment);
        character.setDivinity(divinity == null || divinity.length() == 0 ? null : divinity);
        character.setOrigin(origin == null || origin.length() == 0 ? null : origin);
        character.setSizeType(sizeType);
        character.setSex(sex);
        character.setAge(age);
        character.setWeight(weight);
        character.setHeight(height);
        character.setHair(hair == null || hair.length() == 0 ? null : hair);
        character.setEyes(eyes == null || eyes.length() == 0 ? null : eyes);
        character.setLanguages(lang == null || lang.length() == 0 ? null : lang);
        // update sheet
        updateAdditionalInfos(getView());
        if(refresh) {
            updateSheet(getView());
        }
        // store changes
        characterDBUpdate();
    }

    @Override
    public void onSaveMoney(int cp, int sp, int gp, int pp) {
        character.setMoneyCP(cp);
        character.setMoneySP(sp);
        character.setMoneyGP(gp);
        character.setMoneyPP(pp);
        // update sheet
        updateSheet(getView());
        updateMoneyValue(getView());
        // store changes
        characterDBUpdate();
    }

    @Override
    public void onAddItem(CharacterItem item) {
        if(item != null && item.isValid()) {
            item.setCharacterId(character.getId());
            DBHelper helper = DBHelper.getInstance(getContext());
            long itemId = helper.insertEntity(item);
            CharacterItemFactory.moveItem(helper, character.getInventoryItems(), itemId, -1);
            character.resyncInventory();
            updateInventory(getView());
        }
    }

    @Override
    public void onDeleteItem(long itemId) {
        CharacterItem item = character.getInventoryItemById(itemId);
        if(item != null) {
            DBHelper.getInstance(getContext()).deleteEntity(item);
            character.resyncInventory();
            updateInventory(getView());
        }
    }

    @Override
    public void onUpdateItem(long itemId, CharacterItem updatedItem) {
        CharacterItem item = character.getInventoryItemById(itemId);
        if(item != null && item.isValid()) {
            // keep some settings
            updatedItem.setId(itemId);
            updatedItem.setCharacterId(character.getId());
            updatedItem.setOrder(item.getOrder());
            DBHelper.getInstance(getContext()).updateEntity(updatedItem);
            character.resyncInventory();
            updateInventory(getView());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(refreshNeeded && mCallbacks != null) {
            mCallbacks.onRefreshRequest();
        }
        refreshNeeded = false;
    }

    @Override
    public void onCompleted(Integer status) {
        View root = getActivity().findViewById(R.id.sheet_container);
        if (root != null) {
            if(status == 201) {
                Snackbar.make(root, getView().getResources().getString(R.string.sync_character_success),
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            } else {
                Snackbar.make(root, String.format(getView().getResources().getString(R.string.sync_character_failure), status),
                        Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
    }

    @Override
    public void onSync() {
        final View root = getActivity().findViewById(R.id.sheet_container);
        if(!character.hasUUID()) {
            // force generating UUID (for old characters)
            character.getUniqID();
            if (!DBHelper.getInstance(getContext()).updateEntity(character)) {
                if (root != null) {
                    Snackbar.make(root, getView().getResources().getString(R.string.sync_character_noguid),
                            Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
                return;
            }
        }
        Snackbar.make(root, getView().getResources().getString(R.string.sync_character_inprogress),
                Snackbar.LENGTH_SHORT).setAction("Action", null).show();

        // send synchronisation message
        String sender = PreferenceUtil.getApplicationUUID(getContext());
        String content = CharacterImportExport.exportCharacterAsYML(character, getContext());
        MessageBroker broker = new MessageBroker(this, sender, character.getUniqID(), MessageBroker.TYPE_SYNC, content);
        broker.execute();
    }


    @Override
    public void onContextMenu(long itemId, int menuId) {
        DBHelper helper = DBHelper.getInstance(getContext());
        CharacterItem cItem = (CharacterItem) helper.fetchEntity(itemId, CharacterItemFactory.getInstance());
        if(cItem == null) {
            return;
        }
        if(menuId == CONTEXT_EDIT) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            Fragment prev = getActivity().getSupportFragmentManager().findFragmentByTag(DIALOG_PICK_INVENTORY);
            if (prev != null) {
                ft.remove(prev);
            }
            ft.addToBackStack(null);
            DialogFragment newFragment = FragmentInventoryPicker.newInstance(SheetMainFragment.this);

            Bundle arguments = new Bundle();
            arguments.putLong(FragmentInventoryPicker.ARG_INVENTORY_ID, cItem.getId());
            arguments.putLong(FragmentInventoryPicker.ARG_INVENTORY_CHARACID, character.getId());
            arguments.putString(FragmentInventoryPicker.ARG_INVENTORY_NAME, cItem.getName());
            arguments.putInt(FragmentInventoryPicker.ARG_INVENTORY_WEIGHT, cItem.getWeight());
            arguments.putLong(FragmentInventoryPicker.ARG_INVENTORY_PRICE, cItem.getPrice());
            arguments.putLong(FragmentInventoryPicker.ARG_INVENTORY_OBJID, cItem.getItemRef());
            arguments.putInt(FragmentInventoryPicker.ARG_INVENTORY_CATEGORY, cItem.getCategory());
            arguments.putInt(FragmentInventoryPicker.ARG_INVENTORY_LOCATION, cItem.getLocation());
            arguments.putString(FragmentInventoryPicker.ARG_INVENTORY_INFOS, cItem.getAmmo());
            arguments.putBoolean(FragmentInventoryPicker.ARG_INVENTORY_EQUIPED, cItem.isEquiped());
            newFragment.setArguments(arguments);
            newFragment.show(ft, DIALOG_PICK_INVENTORY);
        }
        else if(menuId == CONTEXT_EQUIP) {
            cItem.setEquiped(!cItem.isEquiped());
            helper.updateEntity(cItem, new HashSet<>(Arrays.asList(CharacterItemFactory.FLAG_EQUIPED)));
            character.resyncInventory();
            updateInventory(getView());
            updateSheet(getView());
        }
        else if(menuId == CONTEXT_SHOWREF) {
            DBEntity object = helper.fetchObjectEntity(cItem);
            if(object != null) {
                Context context = getContext();
                Intent intent = new Intent(context, ItemDetailActivity.class);
                intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, object.getId());
                intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, object.getFactory().getFactoryId());
                context.startActivity(intent);
            }
        }
        else if(menuId >= CONTEXT_ENABLE) {
            Modification modification = character.getModificationById(menuId - CONTEXT_ENABLE);
            if(modification != null) {
                modification.setEnabled(!modification.isEnabled());
                helper.updateEntity(modification, new HashSet<>(Arrays.asList(ModificationFactory.FLAG_ACTIVE)));
                character.resyncModifs();
                updateModifsPickers(getView());
            }
        }
    }
}
