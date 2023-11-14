package ru.rustore.rustoreupdatejavaexample;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import kotlinx.coroutines.flow.FlowKt;

public class MainActivity extends AppCompatActivity {
    private MainViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.init(this);

        viewModel.getEvents().observe(this, event -> {
            if (event != null) {
                popupSnackBarForCompleteUpdate();
            }
        });
    }

    private void popupSnackBarForCompleteUpdate() {
        Snackbar.make(
                findViewById(R.id.main),
                getString(R.string.downloading_completed),
                Snackbar.LENGTH_INDEFINITE
        ).setAction(getString(R.string.button_install), view -> viewModel.completeUpdateRequested()).show();
    }
}