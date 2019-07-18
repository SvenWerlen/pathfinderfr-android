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
import org.pathfinderfr.app.util.FragmentUtil;
import org.pathfinderfr.app.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreasureFragment extends Fragment implements View.OnClickListener {

    private int curType;
    private int curSource;
    private String curTable;
    private List<Pair<String,String>> history;

    public TreasureFragment() {
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

        view.findViewById(R.id.treasure_back).setOnClickListener(this);
        view.findViewById(R.id.treasure_type_weak).setOnClickListener(this);
        view.findViewById(R.id.treasure_type_intermediate).setOnClickListener(this);
        view.findViewById(R.id.treasure_type_powerful).setOnClickListener(this);
        view.findViewById(R.id.treasure_source_mj).setOnClickListener(this);
        view.findViewById(R.id.treasure_source_mjra).setOnClickListener(this);

        TableLayout table = view.findViewById(R.id.treasure_table);
        TreasureTable treasure = TreasureUtil.getInstance(view.getContext()).generateTable(curTable);
        showTable(view, this, treasure, curType);

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
            buf.append("> ").append(el.second);
        }
        return buf.toString();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.treasure_back) {
            // go back one step
            if(history.size() > 0) {
                Pair<String,String> choice = history.remove(history.size()-1);
                curTable = choice.first;
                showTable(getView(), this, TreasureUtil.getInstance(getContext()).generateTable(curTable), curType);
                ((TextView)getView().findViewById(R.id.treasure_history_text)).setText(generateHistory());
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
            // hide treasure type choices
            getView().findViewById(R.id.treasure_type_choices).setVisibility(View.GONE);
            getView().findViewById(R.id.treasure_source_choices).setVisibility(View.GONE);
            getView().findViewById(R.id.treasure_actions).setVisibility(View.GONE);

            // determine next choice
            String result = v.getTag().toString();
            try {
                String newTable = TreasureUtil.nextTable(curSource, history, curTable, result);
                // store choice
                history.add(new Pair<String, String>(curTable, result));
                curTable = newTable;
            } catch(IllegalArgumentException e) {
                Snackbar.make(v.getRootView().findViewById(android.R.id.content),
                        e.getMessage(), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            }

            // show choice
            getView().findViewById(R.id.treasure_history).setVisibility(View.VISIBLE);
            ((TextView)getView().findViewById(R.id.treasure_history_text)).setText(generateHistory());

            // show next table
            if(curTable != null) {
                // show next table
                showTable(getView(), this, TreasureUtil.getInstance(getContext()).generateTable(curTable), curType);
            } else {
                // FOUND!!
                getView().findViewById(R.id.treasure_table).setVisibility(View.GONE);
                List<String> results = TreasureUtil.getResults(history);
                if(results != null) {
                    TextView tv1 = ((TextView)getView().findViewById(R.id.treasure_result1));
                    TextView tv2 = ((TextView)getView().findViewById(R.id.treasure_result2));
                    TextView tv3 = ((TextView)getView().findViewById(R.id.treasure_result3));

                    final long id1, id2, id3;

                    tv1.setText(results.get(0));
                    Map<String, Long> items = new HashMap<>();
                    for(DBEntity entity : DBHelper.getInstance(getContext()).getAllEntities(MagicItemFactory.getInstance())) {
                        int prefixIdx = entity.getName().indexOf(':');
                        if(prefixIdx > 0) {
                            items.put(entity.getName().substring(prefixIdx + 2).toLowerCase(), entity.getId());
                        } else {
                            items.put(entity.getName().toLowerCase(), entity.getId());
                        }
                    }

                    id1 = items.containsKey(results.get(0).toLowerCase()) ? items.get(results.get(0).toLowerCase()) : 0L;

                    if(results.size() > 1) {
                        tv2.setText(results.get(1));
                        tv2.setVisibility(View.VISIBLE);
                        id2 = items.containsKey(results.get(1).toLowerCase()) ? items.get(results.get(1).toLowerCase()) : 0L;
                    } else {
                        tv2.setVisibility(View.GONE);
                        id2 = 0L;
                    }
                    if(results.size() > 2) {
                        tv3.setText(results.get(2));
                        tv3.setVisibility(View.VISIBLE);
                        id3 = items.containsKey(results.get(2).toLowerCase()) ? items.get(results.get(2).toLowerCase()) : 0L;
                    } else {
                        tv3.setVisibility(View.GONE);
                        id3 = 0L;
                    }
                    getView().findViewById(R.id.treasure_results).setVisibility(View.VISIBLE);

                    final Context ctx = getView().getContext();
                    if(id1 > 0) {
                        tv1.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ctx, ItemDetailActivity.class);
                                intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id1);
                                intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, MagicItemFactory.FACTORY_ID);
                                ctx.startActivity(intent);
                            }
                        });
                    }
                    if(id2 > 0) {
                        tv2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ctx, ItemDetailActivity.class);
                                intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id2);
                                intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, MagicItemFactory.FACTORY_ID);
                                ctx.startActivity(intent);
                            }
                        });
                    }
                    if(id3 > 0) {
                        tv3.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ctx, ItemDetailActivity.class);
                                intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, id3);
                                intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, MagicItemFactory.FACTORY_ID);
                                ctx.startActivity(intent);
                            }
                        });
                    }
                }
            }
        }
    }
}
