package org.pathfinderfr.app.character;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.pathfinderfr.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class GeneratePDFActivityFragment extends Fragment {

    public GeneratePDFActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generate_pdf, container, false);
    }
}
