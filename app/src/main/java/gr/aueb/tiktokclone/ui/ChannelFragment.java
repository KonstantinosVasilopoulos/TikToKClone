package gr.aueb.tiktokclone.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import gr.aueb.tiktokclone.R;

public class ChannelFragment extends Fragment {

    private static final String ARG_PARAM1 = "CHANNEL_NAME";

    private String name;

    public ChannelFragment() {
        // Required empty public constructor
    }

    public static ChannelFragment newInstance(String name) {
        ChannelFragment fragment = new ChannelFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            name = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_channel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Change the label
        TextView channelNameBtn = view.findViewById(R.id.channelNameBtn);
        channelNameBtn.setText(name);

        // Add a listener to the button for redirecting to the channel's videos
        channelNameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChannelActivity.class);
                intent.putExtra("CHANNEL_NAME", name);
                startActivity(intent);
            }
        });
    }
}