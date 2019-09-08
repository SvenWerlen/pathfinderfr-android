package org.pathfinderfr.app.util;

import androidx.core.app.ActivityCompat;

import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

public class FragmentUtil {

    public static TextView copyExampleTextFragment(TextView fragment) {
        TextView tv = new TextView(fragment.getContext());
        tv.setText(fragment.getText());
        tv.setLayoutParams(fragment.getLayoutParams());
        tv.setBackground(fragment.getBackground());
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, fragment.getTextSize());
        tv.setPadding(fragment.getPaddingLeft(),fragment.getPaddingTop(), fragment.getPaddingRight(), fragment.getPaddingBottom());
        tv.setTag(fragment.getTag());
        tv.setTypeface(fragment.getTypeface());
        tv.setTextColor(fragment.getTextColors());
        tv.setTextAlignment(fragment.getTextAlignment()); // doesn't work????
        return tv;
    }

    public static ImageView copyExampleImageFragment(ImageView fragment) {
        ImageView iv = new ImageView(fragment.getContext());
        iv.setLayoutParams(fragment.getLayoutParams());
        iv.setPadding(fragment.getPaddingLeft(),fragment.getPaddingTop(), fragment.getPaddingRight(), fragment.getPaddingBottom());
        iv.setTag(fragment.getTag());
        iv.setImageDrawable(fragment.getDrawable());
        return iv;
    }

    public static void adaptForFatFingers(TextView fragment, int minHeight, float scale) {
        fragment.setMinHeight(minHeight);
        fragment.setTextSize(TypedValue.COMPLEX_UNIT_PX, fragment.getTextSize() * scale);
    }

}
