package io.github.kylelam.appleanddroid;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A placeholder fragment containing a simple view.
 */
public class ResultActivityFragment extends Fragment {

    public ResultActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_result, container, false);


        /*
        // The detail Activity called via intent.  Inspect the intent for forecast data.
        Intent intent = getActivity().getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            String forecastStr = intent.getStringExtra(Intent.EXTRA_TEXT);
            ((TextView) rootView.findViewById(R.id.score))
                    .setText(forecastStr);

        }
        */

        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();
        int total_score = extras.getInt("EXTRA_SCORE");
        int maxCombo = extras.getInt("EXTRA_MAX_COMBO");


        ((TextView) rootView.findViewById(R.id.score))
                .setText(total_score+"");
        ((TextView) rootView.findViewById(R.id.comb))
                .setText(maxCombo+" COMB");


        return rootView;
    }
}
