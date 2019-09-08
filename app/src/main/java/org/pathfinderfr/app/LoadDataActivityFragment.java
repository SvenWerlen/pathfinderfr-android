package org.pathfinderfr.app;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pathfinderfr.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class LoadDataActivityFragment extends Fragment {

    public LoadDataActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_load_data, container, false);
    }
}
