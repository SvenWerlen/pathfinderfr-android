package org.pathfinderfr.app.treasure;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.ItemDetailActivity;
import org.pathfinderfr.app.ItemDetailFragment;
import org.pathfinderfr.app.MainActivity;
import org.pathfinderfr.app.character.SheetFeatFragment;
import org.pathfinderfr.app.database.DBHelper;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.MagicItemFactory;
import org.pathfinderfr.app.database.entity.SpellFactory;
import org.pathfinderfr.app.util.ConfigurationUtil;
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class TreasureFragment extends Fragment implements View.OnClickListener {

    public static final String ARG_CUR_TYPE     = "arg_treasureType";
    public static final String ARG_CUR_SOURCE   = "arg_treasureSource";
    public static final String ARG_CUR_TABLE    = "arg_treasureTable";
    public static final String ARG_CUR_HISTORY  = "arg_treasureHistory";
    public static final String ARG_CUR_RANDOM   = "arg_treasureRandom";

    private int curType;
    private int curSource;
    private String curTable;
    private List<Pair<String,String>> history;
    private boolean random;

    public TreasureFragment() {
        random = false;
        history = new ArrayList<>();
        curType = TreasureRow.TYPE_WEAK;
        curSource = TreasureUtil.TABLE_SOURCE_MJ;
        curTable = TreasureUtil.TABLE_MAIN;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_treasure, container, false);

        view.findViewById(R.id.treasure_table_row).setVisibility(View.GONE);
        view.findViewById(R.id.treasure_history).setVisibility(View.GONE);
        view.findViewById(R.id.treasure_results).setVisibility(View.GONE);
        view.findViewById(R.id.treasure_random_again).setVisibility(View.GONE);

        view.findViewById(R.id.treasure_back).setOnClickListener(this);
        view.findViewById(R.id.treasure_random_dice).setOnClickListener(this);
        view.findViewById(R.id.treasure_type_weak).setOnClickListener(this);
        view.findViewById(R.id.treasure_type_intermediate).setOnClickListener(this);
        view.findViewById(R.id.treasure_type_powerful).setOnClickListener(this);
        view.findViewById(R.id.treasure_source_mj).setOnClickListener(this);
        view.findViewById(R.id.treasure_source_mjra).setOnClickListener(this);

        if(savedInstanceState != null) {
            curType = savedInstanceState.getInt(ARG_CUR_TYPE);
            curSource = savedInstanceState.getInt(ARG_CUR_SOURCE);
            curTable = savedInstanceState.getString(ARG_CUR_TABLE);
            random = savedInstanceState.getBoolean(ARG_CUR_RANDOM);
            String[] historySaved = savedInstanceState.getString(ARG_CUR_HISTORY).split("\\|");
            for(String h : historySaved) {
                String[] keyval = h.split("@");
                if(keyval.length == 2) {
                    history.add(new Pair<String, String>(keyval[0], keyval[1]));
                }
            }
            // initialize view
            if(history.size() > 0) {
                // hide treasure type choices
                view.findViewById(R.id.treasure_type_choices).setVisibility(View.GONE);
                view.findViewById(R.id.treasure_source_choices).setVisibility(View.GONE);
                view.findViewById(R.id.treasure_actions).setVisibility(View.GONE);
                // show history
                showHistory(view);
                if(curTable == null) {
                    showResult(view);
                } else {
                    showTable(view, this, TreasureUtil.getInstance(getContext()).generateTable(curTable), curType);
                }
            } else {
                showTable(view, this, TreasureUtil.getInstance(view.getContext()).generateTable(curTable), curType);
            }

        } else {
            showTable(view, this, TreasureUtil.getInstance(view.getContext()).generateTable(curTable), curType);
        }

        return view;
    }

    private static void showTable(View view, View.OnClickListener listener, TreasureTable treasure, int dicesType) {
        TableLayout table = view.findViewById(R.id.treasure_table);
        TextView exampleDices = view.findViewById(R.id.treasure_dice_example);
        TextView exampleResult = view.findViewById(R.id.treasure_dice_result);
        view.findViewById(R.id.treasure_table).setVisibility(View.VISIBLE);
        view.findViewById(R.id.treasure_results).setVisibility(View.GONE);

        // clean table (remove all rows except the first two (header and example))
        int childCount = table.getChildCount();
        if (childCount > 2) {
            table.removeViews(2, childCount - 2);
        }

        // determine size
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        int lineHeight = Integer.parseInt(prefs.getString(MainActivity.PREF_LINEHEIGHT, "0"));
        int height = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, lineHeight, view.getResources().getDisplayMetrics());
        int rowId = 0;

        int prevDice = 1;
        for(TreasureRow r : treasure.getRows()) {
            TableRow row = new TableRow(view.getContext());
            row.setMinimumHeight(height);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundColor(ContextCompat.getColor(view.getContext(),
                    rowId % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));

            // dices
            TextView dicesTv = FragmentUtil.copyExampleTextFragment(exampleDices);
            if(r.getDiceTo(dicesType) == null) {
                dicesTv.setText("–");
            } else {
                if(prevDice == r.getDiceTo(dicesType)) {
                    dicesTv.setText(String.format("%02d", r.getDiceTo(dicesType)));
                } else {
                    dicesTv.setText(String.format("%02d–%02d", prevDice, r.getDiceTo(dicesType)));
                }
                prevDice = r.getDiceTo(dicesType) + 1;
            }
            row.addView(dicesTv);

            // result
            TextView resultTv = FragmentUtil.copyExampleTextFragment(exampleResult);
            resultTv.setText(r.getResultName());
            row.addView(resultTv);

            // add to table
            row.setOnClickListener(listener);
            row.setTag(r.getResultName());
            table.addView(row);

            rowId++;
        }

    }

    private final String generateHistory() {
        StringBuffer buf = new StringBuffer();
        for(Pair<String,String> el : history) {
            buf.append(" > ").append(el.second);
        }
        return buf.toString();
    }


    private void showHistory(View view) {
        view.findViewById(R.id.treasure_history).setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(R.id.treasure_history_text)).setText(generateHistory());
    }

    private void showResult(View view) {
        view.findViewById(R.id.treasure_table).setVisibility(View.GONE);

        if(random) {
            view.findViewById(R.id.treasure_actions).setVisibility(View.VISIBLE);
            view.findViewById(R.id.treasure_action_label).setVisibility(View.GONE);
            view.findViewById(R.id.treasure_random_again).setVisibility(View.VISIBLE);
        }

        List<String> results = TreasureUtil.getResults(history);
        if(results != null) {
            TextView tv1 = ((TextView)view.findViewById(R.id.treasure_result1));
            TextView tv2 = ((TextView)view.findViewById(R.id.treasure_result2));
            TextView tv3 = ((TextView)view.findViewById(R.id.treasure_result3));

            final long id1, id2, id3;

            tv1.setText(results.get(0));
            Map<String, Long> items = new HashMap<>();
            final String factoryId;
            if(TreasureUtil.resultsIsSpell(history)) {
                for (DBEntity entity : DBHelper.getInstance(getContext()).getAllEntities(SpellFactory.getInstance())) {
                    items.put(entity.getName().toLowerCase(), entity.getId());
                }
                factoryId = SpellFactory.FACTORY_ID;
            } else {
                for (DBEntity entity : DBHelper.getInstance(getContext()).getAllEntities(MagicItemFactory.getInstance())) {
                    int prefixIdx = entity.getName().indexOf(':');
                    if (prefixIdx > 0) {
                        items.put(entity.getName().substring(prefixIdx + 2).toLowerCase(), entity.getId());
                    } else {
                        items.put(entity.getName().toLowerCase(), entity.getId());
                    }
                }
                factoryId = MagicItemFactory.FACTORY_ID;
            }

            if(items.containsKey(results.get(0).toLowerCase())) {
                id1 = items.get(results.get(0).toLowerCase());
            } else if(items.containsKey(TreasureUtil.getNameForSearch(results.get(0)))) {
                id1 = items.get(TreasureUtil.getNameForSearch(results.get(0)));
            } else {
                id1 = 0L;
                Log.w(TreasureFragment.class.getSimpleName(), "Correspondance objet magique non-trouvée: " + results.get(0));
            }

            if(results.size() > 1) {
                tv2.setText(results.get(1));
                tv2.setVisibility(View.VISIBLE);
                if(items.containsKey(results.get(1).toLowerCase())) {
                    id2 = items.get(results.get(1).toLowerCase());
                } else if(items.containsKey(TreasureUtil.getNameForSearch(results.get(1)))) {
                    id2 = items.get(TreasureUtil.getNameForSearch(results.get(1)));
                } else {
                    id2 = 0L;
                    Log.w(TreasureFragment.class.getSimpleName(), "Correspondance objet magique non-trouvée: " + results.get(1));
                }
            } else {
                tv2.setVisibility(View.GONE);
                id2 = 0L;
            }
            if(results.size() > 2) {
                tv3.setText(results.get(2));
                tv3.setVisibility(View.VISIBLE);
                if(items.containsKey(results.get(2).toLowerCase())) {
                    id3 = items.get(results.get(2).toLowerCase());
                } else if(items.containsKey(TreasureUtil.getNameForSearch(results.get(2)))) {
                    id3 = items.get(TreasureUtil.getNameForSearch(results.get(2)));
                } else {
                    id3 = 0L;
                    Log.w(TreasureFragment.class.getSimpleName(), "Correspondance objet magique non-trouvée: " + results.get(2));
                }
            } else {
                tv3.setVisibility(View.GONE);
                id3 = 0L;
            }
            view.findViewById(R.id.treasure_results).setVisibility(View.VISIBLE);

            final Context ctx = view.getContext();
            if(id1 > 0) {
                tv1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ctx, ItemDetailActivity.class);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id1);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, factoryId);
                        ctx.startActivity(intent);
                    }
                });
            } else {
                tv1.setOnClickListener(null);
            }
            if(id2 > 0) {
                tv2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ctx, ItemDetailActivity.class);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id2);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, factoryId);
                        ctx.startActivity(intent);
                    }
                });
            } else {
                tv2.setOnClickListener(null);
            }
            if(id3 > 0) {
                tv3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ctx, ItemDetailActivity.class);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id3);
                        intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, factoryId);
                        ctx.startActivity(intent);
                    }
                });
            } else {
                tv3.setOnClickListener(null);
            }
        }
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.treasure_random_dice) {
            // reset
            random = true;
            curTable = TreasureUtil.TABLE_MAIN;

            // clear history (shouldn't be necessary)
            history.clear();

            // iterate until choice done
            int iter = 0;
            while(iter < 10) {
                TreasureTable table = TreasureUtil.getInstance(getContext()).generateTable(curTable);
                int maxChoice = table.maxChoice(curType);
                // no choice? something is wrong
                if(maxChoice == 0) {
                    Snackbar.make(v.getRootView().findViewById(android.R.id.content),
                            ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.generator.1"), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    history.clear();
                    return;
                }
                int resultDice = (new Random()).nextInt(maxChoice) + 1;
                // Uncomment to test on specific table
                //if(curTable == TreasureUtil.TABLE_MAIN) {
                //    resultDice = 45;
                //}
                Log.i(TreasureFragment.class.getSimpleName(), String.format("Dice result for #%d = %d", iter, resultDice));
                String result = table.getChoice(curType, resultDice);
                // no result? something is wrong
                if(result == null) {
                    Snackbar.make(v.getRootView().findViewById(android.R.id.content),
                            ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.generator.2"), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    history.clear();
                    return;
                }
                try {
                    String newTable = TreasureUtil.nextTable(curType, curSource, history, curTable, result);
                    // store choice
                    history.add(new Pair<String, String>(curTable, result));
                    curTable = newTable;
                } catch(IllegalArgumentException e) {}

                // magic item generator worked!
                if(curTable == null) {
                    // hide treasure type choices
                    getView().findViewById(R.id.treasure_results).setVisibility(View.GONE);
                    getView().findViewById(R.id.treasure_type_choices).setVisibility(View.GONE);
                    getView().findViewById(R.id.treasure_source_choices).setVisibility(View.GONE);
                    getView().findViewById(R.id.treasure_actions).setVisibility(View.GONE);
                    // show results
                    showHistory(getView());
                    showResult(getView());
                    return;
                }

                iter++;
            }

            Snackbar.make(v.getRootView().findViewById(android.R.id.content),
                    ConfigurationUtil.getInstance().getProperties().getProperty("treasure.error.generator.3"), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            history.clear();
            return;
        }
        else if(v.getId() == R.id.treasure_back) {
            // go back to first step
            if(random) {
                getView().findViewById(R.id.treasure_type_choices).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.treasure_source_choices).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.treasure_actions).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.treasure_action_label).setVisibility(View.VISIBLE);
                getView().findViewById(R.id.treasure_random_again).setVisibility(View.GONE);
                getView().findViewById(R.id.treasure_history).setVisibility(View.GONE);
                history.clear();
                random = false;
                curTable = TreasureUtil.TABLE_MAIN;
            }
            // go back one step
            if(history.size() > 0) {
                Pair<String,String> choice = history.remove(history.size()-1);
                curTable = choice.first;
                showHistory(getView());
                // back to root? => reset
                if(history.size() == 0) {
                    getView().findViewById(R.id.treasure_type_choices).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.treasure_source_choices).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.treasure_actions).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.treasure_history).setVisibility(View.GONE);
                }
            }
            showTable(getView(), this, TreasureUtil.getInstance(getContext()).generateTable(curTable), curType);

        } else if(v.getId() == R.id.treasure_type_weak) {
            curType = TreasureRow.TYPE_WEAK;
            showTable(getView(), this, TreasureUtil.getInstance(getContext()).generateTable(curTable), curType);
        } else if(v.getId() == R.id.treasure_type_intermediate) {
            curType = TreasureRow.TYPE_INTERMEDIATE;
            showTable(getView(), this, TreasureUtil.getInstance(getContext()).generateTable(curTable), curType);
        } else if(v.getId() == R.id.treasure_type_powerful) {
            curType = TreasureRow.TYPE_POWERFUL;
            showTable(getView(), this, TreasureUtil.getInstance(getContext()).generateTable(curTable), curType);
        } else if(v.getId() == R.id.treasure_source_mj) {
            curSource = TreasureUtil.TABLE_SOURCE_MJ;
        } else if(v.getId() == R.id.treasure_source_mjra) {
            curSource = TreasureUtil.TABLE_SOURCE_MJRA;
        } else if(v instanceof TableRow) {
            random = false;
            // hide treasure type choices
            getView().findViewById(R.id.treasure_type_choices).setVisibility(View.GONE);
            getView().findViewById(R.id.treasure_source_choices).setVisibility(View.GONE);
            getView().findViewById(R.id.treasure_actions).setVisibility(View.GONE);

            // determine next choice
            String result = v.getTag().toString();
            try {
                String newTable = TreasureUtil.nextTable(curType, curSource, history, curTable, result);
                // store choice
                history.add(new Pair<String, String>(curTable, result));
                curTable = newTable;
            } catch(IllegalArgumentException e) {
                Snackbar.make(v.getRootView().findViewById(android.R.id.content),
                        e.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }

            showHistory(getView());

            // show next table
            if(curTable != null) {
                // show next table
                showTable(getView(), this, TreasureUtil.getInstance(getContext()).generateTable(curTable), curType);
            } else {
                // FOUND!!
                showResult(getView());
            }
        }
    }

    /**
     * Tries to go back in history
     * Returns false if normal onBack should be done
     */
    public boolean onBack() {
        if(history.size() == 0) {
            return false;
        } else {
            onClick(getView().findViewById(R.id.treasure_back));
            return true;
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // store selected radios
        outState.putInt(ARG_CUR_TYPE, curType);
        outState.putInt(ARG_CUR_SOURCE, curSource);

        // store state
        outState.putString(ARG_CUR_TABLE, curTable);
        outState.putBoolean(ARG_CUR_RANDOM, random);

        // store history
        StringBuffer buf = new StringBuffer();
        for(Pair<String,String> p : history) {
            buf.append(p.first).append("@").append(p.second).append("|");
        }
        if(buf.length() > 0) {
            buf.deleteCharAt(buf.length()-1);
        }
        outState.putString(ARG_CUR_HISTORY, buf.toString());
    }
}
