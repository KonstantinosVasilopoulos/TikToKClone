package gr.aueb.tiktokclone.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import org.jetbrains.annotations.NotNull;

import gr.aueb.tiktokclone.R;

public class BottomBarFragment extends Fragment {

    public BottomBarFragment() {
        // Required empty public constructor
    }

    public static BottomBarFragment newInstance() {
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

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Add a listener to the home button
        LinearLayout homeBtn = view.findViewById(R.id.homeBtn);
        homeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), MainActivity.class);
            startActivity(intent);
        });

        // Add a listener to the upload button
        ImageButton uploadBtn = view.findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), UploadActivity.class);
            startActivity(intent);
        });

        // Add a listener to the discover button
        LinearLayout discoverBtn = view.findViewById(R.id.discoverBtn);
        discoverBtn.setOnClickListener(v -> {
            Intent intent =  new Intent(view.getContext(), DiscoverActivity.class);
            startActivity(intent);
        });
    }
}