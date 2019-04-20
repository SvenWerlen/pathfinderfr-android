package org.pathfinderfr.app;

import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.pathfinderfr.R;
import org.pathfinderfr.app.character.CharacterSheetActivity;
import org.pathfinderfr.app.database.entity.Character;
import org.pathfinderfr.app.database.entity.CharacterFactory;
import org.pathfinderfr.app.database.entity.ClassFeature;
import org.pathfinderfr.app.database.entity.Condition;
import org.pathfinderfr.app.database.entity.DBEntity;
import org.pathfinderfr.app.database.entity.FavoriteFactory;
import org.pathfinderfr.app.database.entity.Feat;
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

    ItemListRecyclerViewAdapter(MainActivity parent,
                                List<DBEntity> items,
                                boolean twoPane) {
        mValues = items;
        mParentActivity = parent;
        mTwoPane = twoPane;
        factoryId = null;
        showNameLong = false;
    }

    public void setFactoryId(String factoryId) {
        this.factoryId = factoryId;
    }
    public void setShowNameLong(boolean nameLong) { this.showNameLong = nameLong; }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DBEntity item = (DBEntity) view.getTag();

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

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        //holder.mIdView.setText(String.valueOf(mValues.get(position).getId()));

        DBEntity entity = mValues.get(position);
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

        ImageView icon = (ImageView) holder.itemView.findViewById(R.id.itemIcon);
        icon.setVisibility(View.VISIBLE);
        if(entity instanceof Feat) {
            icon.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_feat));
        } else if(entity instanceof ClassFeature) {
            icon.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_classfeature));
        } else if(entity instanceof Skill) {
            icon.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_skill));
        } else if(entity instanceof Spell) {
            icon.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_spell));
        } else if(entity instanceof Condition) {
            icon.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_condition));
        } else if(entity instanceof Weapon) {
            icon.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_item_icon_weapons));
        } else if(entity instanceof Character) {
            // show icon for pined character
            long characterId = PreferenceManager.getDefaultSharedPreferences(holder.itemView.getContext()).getLong(CharacterSheetActivity.PREF_SELECTED_CHARACTER_ID, 0L);
            if(characterId > 0 && characterId == entity.getId()) {
                icon.setImageDrawable(ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_pin));
            } else {
                icon.setVisibility(View.INVISIBLE);
            }
        }
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