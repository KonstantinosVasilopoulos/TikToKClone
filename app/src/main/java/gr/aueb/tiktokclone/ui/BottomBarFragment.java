package gr.aueb.tiktokclone.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import gr.aueb.tiktokclone.R;

public class BottomBarFragment extends Fragment {

    public BottomBarFragment() {
        // Required empty public constructor
    }

    public static BottomBarFragment newInstance(String param1, String param2) {
        return new BottomBarFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bottom_bar, container, false);
    }
}