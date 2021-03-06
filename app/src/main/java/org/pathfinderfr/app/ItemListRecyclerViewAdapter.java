package org.pathfinderfr.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.character.CharacterSheetActivity;
import org.pathfinderfr.app.database.entity.Armor;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.Condition;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.Equipment;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Feat;
import org.pathfinderfr.app.database.entity.MagicItem;
import org.pathfinderfr.app.database.entity.Trait;
import org.pathfinderfr.app.database.entity.Skill;
import org.pathfinderfr.app.database.entity.Spell;
import org.pathfinderfr.app.database.entity.Weapon;

import java.util.List;

public class ItemListRecyclerViewAdapter
        extends RecyclerView.Adapter<ItemListRecyclerViewAdapter.ViewHolder> {

    private final MainActivity mParentActivity;
    private final List<DBEntity> mValues;
    private final boolean mTwoPane;
    private String factoryId;
    private boolean showNameLong;
    private int lineHeight;

    private int colorEnabled;

    ItemListRecyclerViewAdapter(MainActivity parent,
                                List<DBEntity> items,
                                boolean twoPane) {
        mValues = items;
        mParentActivity = parent;
        mTwoPane = twoPane;
        factoryId = null;
        showNameLong = false;
        lineHeight = 0;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }

    public void setShowNameLong(boolean nameLong) { this.showNameLong = nameLong; }

    public int getMinimumLineHeight() { return this.lineHeight; }
    public void setMinimumLineHeight(int lineHeight) { this.lineHeight = lineHeight; }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DBEntity item = (DBEntity) view.getTag();

            if(item == null || item.getId() <= 0) {
                return;
            }

            if(CharacterFactory.FACTORY_ID.equals(factoryId)) {
                Context context = view.getContext();
                Intent intent = new Intent(mParentActivity, CharacterSheetActivity.class);
                intent.putExtra(CharacterSheetActivity.SELECTED_CHARACTER_ID, item.getId());
                context.startActivity(intent);
                factoryId = CharacterFactory.FACTORY_ID;
            } else {
                Context context = view.getContext();
                Intent intent = new Intent(context, ItemDetailActivity.class);
                intent.putExtra(ItemDetailFragment.ARG_ITEM_ID, item.getId());
                intent.putExtra(ItemDetailFragment.ARG_ITEM_FACTORY_ID, item.getFactory().getFactoryId());
                context.startActivity(intent);
            }
        }
    };

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_content, parent, false);

        colorEnabled = view.getContext().getResources().getColor(R.color.colorBlack);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //holder.mIdView.setText(String.valueOf(mValues.get(position).getId()));

        DBEntity entity = mValues.get(position);
        if(entity == null) {
            return;
        }
        String name = showNameLong ? entity.getNameLong() : entity.getName();

        // TODO: find a way to better handle this special case.
        // Favorites stores long names as names because details are not available
        if(FavoriteFactory.FACTORY_ID.equalsIgnoreCase(factoryId) && !showNameLong && !(entity instanceof Skill)) {
            int idx = name.indexOf('(');
            if(idx > 0) {
                name = name.substring(0, idx);
            }
        }

        holder.mContentView.setText(name);
        holder.itemView.setTag(entity);
        holder.itemView.setOnClickListener(mOnClickListener);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(holder.itemView.getContext());
        holder.itemView.setMinimumHeight(lineHeight);
        holder.itemView.setBackgroundColor(ContextCompat.getColor(
                holder.itemView.getContext(), position % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));

        ImageView icon = (ImageView) holder.itemView.findViewById(R.id.itemIcon);

        // default view
        if(entity.getId() > 0) {
            holder.itemView.setBackgroundColor(ContextCompat.getColor(
                    holder.itemView.getContext(), position % 2 == 1 ? R.color.colorPrimaryAlternate : R.color.colorWhite));
            holder.mContentView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorBlack));
            // special case for feats
            if(entity instanceof Feat && ((Feat)entity).getDepth() < 0) {
                icon.setVisibility(View.GONE);
                holder.mContentView.setTypeface(null, Typeface.BOLD_ITALIC);
            } else if(entity instanceof Feat && ((Feat)entity).getDepth() > 0) {
                icon.setVisibility(View.GONE);
                holder.mContentView.setTypeface(null, Typeface.NORMAL);
            } else {
                icon.setVisibility(View.VISIBLE);
                holder.mContentView.setTypeface(null, Typeface.NORMAL);
            }
        }
        // view for separators
        else if(entity.getId() <= 0) {
            icon.setVisibility(View.GONE);
            holder.itemView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorPrimaryDark));
            holder.mContentView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorWhite));
            holder.mContentView.setTypeface(null, Typeface.NORMAL);
        }

        if(entity instanceof Feat) {
            icon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_feat));
        } else if(entity instanceof ClassFeature) {
            icon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_classfeature));
        } else if(entity instanceof Skill) {
            icon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_skill));
        } else if(entity instanceof Spell) {
            icon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_spell));
        } else if(entity instanceof Condition) {
            icon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_condition));
        } else if(entity instanceof Weapon) {
            icon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_weapons));
        } else if(entity instanceof Armor) {
            icon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_armors));
        } else if(entity instanceof Equipment) {
            icon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_equipment));
        } else if(entity instanceof MagicItem) {
            icon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_magic));
        } else if(entity instanceof Trait) {
            icon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_trait));
        } else if(entity instanceof Character) {
            // show icon for pined character
            long characterId = PreferenceManager.getDefaultSharedPreferences(holder.itemView.getContext()).getLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, 0L);
            if(characterId > 0 && characterId == entity.getId()) {
                icon.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_pin));
            } else {
                icon.setVisibility(View.INVISIBLE);
            }
        }
        icon.getBackground().setColorFilter(colorEnabled, PorterDuff.Mode.SRC_ATOP);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        //final TextView mIdView;
        final TextView mContentView;

        ViewHolder(View view) {
            super(view);
            //mIdView = (TextView) view.findViewById(R.id.id_text);
            mContentView = (TextView) view.findViewById(R.id.content);
        }
    }
}