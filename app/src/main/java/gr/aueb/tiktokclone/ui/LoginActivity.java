package gr.aueb.tiktokclone.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import gr.aueb.tiktokclone.R;
import gr.aueb.tiktokclone.domain.AppNode;
import gr.aueb.tiktokclone.domain.AppNodeFactory;

public class LoginActivity extends AppCompatActivity {
    EditText loginChannelNameField;
    Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Populate the app with fake accounts(mainly for debugging)
        new AppNodeFactory().execute(2);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Find the text field and the button
        loginChannelNameField = findViewById(R.id.loginChannelNameField);
        loginBtn = findViewById(R.id.loginBtn);

        // Add a listener to the button for logging in
        loginBtn.setOnClickListener(v -> {
                // Get the text from the field
                String channelName = loginChannelNameField.getText().toString();

                // Initialize the new app node
                if (channelName.length() > 0) {
                    AppNode.init(channelName, getApplicationContext());

                    // Redirect to the main activity
                    Intent intent = new Intent(v.getContext(), MainActivity.class);
                    startActivity(intent);

                } else {
                    // Display an error popup
                    Snackbar.make(findViewById(R.id.loginView),
                            R.string.login_error,
                            Snackbar.LENGTH_SHORT).show();
                }
        });
    }
}