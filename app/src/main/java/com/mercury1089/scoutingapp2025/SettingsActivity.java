package com.mercury1089.scoutingapp2025;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.mercury1089.scoutingapp2025.repository.MatchRepository;
import com.mercury1089.scoutingapp2025.utils.ListAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity {
    private MatchRepository matchRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private LinkedHashMap<String, String> settingsHashMap;
    private String[] qrList;
    private ListView qrCodeSelector;
    private ListAdapter listAdapter;

    // Password buttons
    private Button createResetPasswordButton, changePasswordButton;
    private TextView passwordPreviewID;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        matchRepository = new MatchRepository(getApplicationContext());

        //assigning variables to their equivalent screen elements
        Button localStorageResetButton = findViewById(R.id.LocalStorageResetButton);
        Button backButton = findViewById(R.id.BackButton);
        Button clearQRCache = findViewById(R.id.ClearQRCodesButton);
        createResetPasswordButton = findViewById(R.id.CreateResetPasswordButton);
        changePasswordButton = findViewById(R.id.ChangePasswordButton);
        passwordPreviewID = findViewById(R.id.IDPasswordPreview);
        updatePasswordButtons();

        EditText eventKeyInput = findViewById(R.id.EventKeyInput);

        disposables.add(matchRepository.getStoredEventKey()
                .subscribe(
                        eventKeyInput::setText,
                        throwable -> Log.d("1089", Objects.requireNonNull(throwable.getMessage()))
                ));
        Button fetchMatchesButton = findViewById(R.id.FetchMatchesFromEventButton);
        TextView lastFetchedID = findViewById(R.id.LastFetchedAtID);

        // onComplete (third argument) is called when the action completes but doesn't return a result
        disposables.add(getLastFetchedDate().subscribe(
                str -> lastFetchedID.setText(getString(R.string.LastFetchedAtID, str)),
                throwable -> Log.d("1089", Objects.requireNonNull(throwable.getMessage())),
                () -> lastFetchedID.setText(getString(R.string.LastFetchedAtID, "Unknown"))
        ));

        qrCodeSelector = findViewById(R.id.QRCodeListView);

        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.SETTINGS);
        settingsHashMap = HashMapManager.getSettingsHashMap();
        qrList = HashMapManager.setupQRList(getApplicationContext());

        listAdapter = new ListAdapter(this, qrList);
        addQRCodes();

        clearQRCache.setEnabled(qrList.length > 0);

        localStorageResetButton.setOnClickListener(v -> {
            HashMapManager.setDefaultValues(HashMapManager.HASH.SETTINGS);
            HashMapManager.setDefaultValues(HashMapManager.HASH.SETUP);
            HashMapManager.setDefaultValues(HashMapManager.HASH.AUTON);
            HashMapManager.setDefaultValues(HashMapManager.HASH.TELEOP);
            HashMapManager.setDefaultValues(HashMapManager.HASH.CLIMB);
            Toast.makeText(SettingsActivity.this, "All variables successfully reset.", Toast.LENGTH_SHORT).show();
        });

        createResetPasswordButton.setOnClickListener(v -> {
            if (createResetPasswordButton.getText().equals(getString(R.string.ResetPassword))) {
                HashMapManager.saveSettingsPassword(new String[] {"", "N"}, getApplicationContext());
                Toast.makeText(getApplicationContext(),"Successfully reset password!", Toast.LENGTH_SHORT).show();
                updatePasswordButtons();
            } else {
                createPasswordChangeDialog();
            }
        });

        changePasswordButton.setOnClickListener(v -> createPasswordChangeDialog());

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, PregameActivity.class);
            startActivity(intent);
            finish();
        });

        clearQRCache.setOnClickListener(v -> {
            Dialog dialog = new Dialog(SettingsActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.clear_qr_cache_confirm);

            Button clearConfirm = dialog.findViewById(R.id.ClearConfirm);
            Button cancelConfirm = dialog.findViewById(R.id.CancelConfirm);

            dialog.show();

            clearConfirm.setOnClickListener(view -> {
                HashMapManager.outputQRList(new String[0], SettingsActivity.this);
                qrList = HashMapManager.setupQRList(SettingsActivity.this);
                listAdapter = new ListAdapter(SettingsActivity.this, qrList);
                qrCodeSelector.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();
                clearQRCache.setEnabled(false);
                dialog.dismiss();
            });

            cancelConfirm.setOnClickListener(view -> dialog.dismiss());
        });

        fetchMatchesButton.setOnClickListener(view -> {
            String eventKey = eventKeyInput.getText().toString();

            disposables.add(matchRepository.storeMatchesByEvent(eventKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> {
                            Toast.makeText(getApplicationContext(), "Matches fetched and stored!", Toast.LENGTH_SHORT).show();
                            disposables.add(getLastFetchedDate().subscribe(
                                    str -> lastFetchedID.setText(getString(R.string.LastFetchedAtID, str)),
                                    throwable -> Log.d("1089", Objects.requireNonNull(throwable.getMessage()))
                            ));
                        },
                        error -> Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show()
                ));
        });
    }

    private void createPasswordChangeDialog() {
        Context context = SettingsActivity.this;
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.enter_password_popup);

        TextView passwordField = dialog.findViewById(R.id.PasswordField);
        Switch requirePasswordSwitch = dialog.findViewById(R.id.SettingsPasswordSwitch);
        Button doneButton = dialog.findViewById(R.id.DoneButton);
        Button cancelButton = dialog.findViewById(R.id.CancelButton);

        String[] passwordData = HashMapManager.pullSettingsPassword(context);
        if (passwordData != null && passwordData.length == 2) {
            passwordField.setText(passwordData[0]);
            requirePasswordSwitch.setChecked(passwordData[1].equals("Y"));
        } else {
            Toast.makeText(getApplicationContext(), "Fatal error: Could not fetch password data", Toast.LENGTH_SHORT).show();
            return;
        }

        passwordField.setHint(settingsHashMap.get("DefaultPassword"));

        dialog.show();

        doneButton.setOnClickListener(v -> {
            String password = passwordField.getText().toString(), requiredPassword = requirePasswordSwitch.isChecked() ? "Y" : "N";
            HashMapManager.saveSettingsPassword(new String[]{password, requiredPassword}, context);
            dialog.dismiss();
            HashMapManager.saveSettingsPassword(new String[]{password, requiredPassword}, context);
            createResetPasswordButton.setSelected(requiredPassword.equals("Y"));
            updatePasswordButtons();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void updatePasswordButtons() {
        boolean passwordSet, usePassword;
        String[] password = HashMapManager.pullSettingsPassword(getApplicationContext());
        if (password != null) {
            passwordSet = !password[0].isEmpty();
            usePassword = password[1].equalsIgnoreCase("y");
        } else {
            passwordSet = false;
            usePassword = false;
        }
        createResetPasswordButton.setSelected(usePassword);
        createResetPasswordButton.setText(passwordSet ? R.string.ResetPassword : R.string.CreatePassword);
        changePasswordButton.setVisibility(passwordSet ? View.VISIBLE : View.INVISIBLE);
        passwordPreviewID.setText(
                passwordSet ? "*".repeat(Math.min(password[0].length(), 16)): getString(R.string.DefaultPasswordPreview));
    }

    private Maybe<String> getLastFetchedDate() {
        return matchRepository.getLastFetchedTime()
                .subscribeOn(Schedulers.io())
                .map(longTime -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss", Locale.getDefault());
                    return sdf.format(new Date(longTime));
                })
                .onErrorReturnItem("Unknown")
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void addQRCodes(){
        qrCodeSelector.setAdapter(listAdapter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        disposables.clear();
        HashMapManager.putSettingsHashMap(settingsHashMap);
    }
}