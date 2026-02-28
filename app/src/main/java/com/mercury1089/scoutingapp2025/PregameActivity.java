package com.mercury1089.scoutingapp2025;

import com.mercury1089.scoutingapp2025.database.model.Match;
import com.mercury1089.scoutingapp2025.database.util.DBUtil;
import com.mercury1089.scoutingapp2025.databinding.ActivityPregameBinding;
import com.mercury1089.scoutingapp2025.qr.QRRunnable;
import com.mercury1089.scoutingapp2025.repository.MatchRepository;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class PregameActivity extends AppCompatActivity {
    private ActivityPregameBinding binding;

    // Strategy was here
    //Set the default password in HashMapManager.setDefaultValues();
    String password;
    private CompositeDisposable disposables = new CompositeDisposable();
    private MatchRepository matchRepository;

    //HashMaps
    private LinkedHashMap<String, String> settingsHashMap;
    private LinkedHashMap<String, String> setupHashMap;

    private Dialog loading_alert;
    private ProgressDialog progressDialog;


    Bitmap bitmap;
    //ProgressDialog progressDialog;
    boolean isQRButton = false;

    //others
    private MediaPlayer rooster;
    private ImageView slackCenter;

    /*
    - This is where you initialize the activity:
        - Define views (screen elements) here
        - Make sure relevant HashMaps aren't empty using checkNullOrEmpty()
        - Define EventListeners -> Not using lambda to make it easier for newcomers
        -
        -
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPregameBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_pregame);
        matchRepository = new MatchRepository(getApplicationContext());

        rooster = MediaPlayer.create(PregameActivity.this, R.raw.sound);

        // Make sure hash maps are not empty/null, then get HashMaps and set password for settings screen
        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.SETTINGS);
        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.SETUP);
        settingsHashMap = HashMapManager.getSettingsHashMap();
        setupHashMap = HashMapManager.getSetupHashMap();
        password = settingsHashMap.get("DefaultPassword");

        //setting group buttons to default state
        updateXMLObjects(true);

        binding.etScouterName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setupHashMap.put("ScouterName", binding.etScouterName.getText().toString());
                updateXMLObjects(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etMatchNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setupHashMap.put("MatchNumber", binding.etMatchNumber.getText().toString());
                updateXMLObjects(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etTeamNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setupHashMap.put("TeamNumber", binding.etTeamNumber.getText().toString());
                updateXMLObjects(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etFirstAlliancePartner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setupHashMap.put("AlliancePartner1", binding.etFirstAlliancePartner.getText().toString());
                updateXMLObjects(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        binding.etSecondAlliancePartner.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setupHashMap.put("AlliancePartner2", binding.etSecondAlliancePartner.getText().toString());
                updateXMLObjects(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //starting listener to check the status of the switch
        binding.switchNoShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    setupHashMap.put("PreloadNote", "N");
                setupHashMap.put("NoShow", isChecked ? "Y" : "N");
                updateXMLObjects(false);
            }
        });

        //starting listener to check status of switch
        binding.switchPreload.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                setupHashMap.put("PreloadNote", isChecked ? "Y" : "N");
                updateXMLObjects(false);
            }
        });

        //click methods
        binding.btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] passwordData = HashMapManager.pullSettingsPassword(PregameActivity.this);
                final String password, requiredPassword;
                String tempPassword, tempRequired;
                try {
                    tempPassword = passwordData[0];
                    tempRequired = passwordData[1];
                } catch (Exception e) {
                    tempPassword = PregameActivity.this.password;
                    tempRequired = "N";
                }

                password = tempPassword;
                requiredPassword = tempRequired;

                if (requiredPassword.equals("N")) {
                    HashMapManager.putSetupHashMap(setupHashMap);
                    disposables.clear();
                    Intent intent = new Intent(PregameActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    finish();
                    return;
                }

                Dialog dialog = new Dialog(PregameActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.settings_password);

                TextView passwordField = dialog.findViewById(R.id.PasswordField);
                Button confirm = dialog.findViewById(R.id.ConfirmButton);
                Button cancel = dialog.findViewById(R.id.CancelButton);
                ImageView topEdgeBar = dialog.findViewById(R.id.topEdgeBar);
                ImageView bottomEdgeBar = dialog.findViewById(R.id.bottomEdgeBar);
                ImageView leftEdgeBar = dialog.findViewById(R.id.leftEdgeBar);
                ImageView rightEdgeBar = dialog.findViewById(R.id.rightEdgeBar);

                dialog.show();

                passwordField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                            //do what you want on the press of 'done'
                            confirm.performClick();
                        }
                        return false;
                    }
                });

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String savedPassword = !password.equals("") ? password : PregameActivity.this.password;
                        if (passwordField.getText().toString().equals(savedPassword)) {
                            HashMapManager.putSetupHashMap(setupHashMap);
                            disposables.clear();
                            Intent intent = new Intent(PregameActivity.this, SettingsActivity.class);
                            startActivity(intent);
                            dialog.dismiss();
                            finish();
                        } else {
                            Toast.makeText(PregameActivity.this, "Incorrect Password", Toast.LENGTH_SHORT).show();

                            ObjectAnimator topEdgeLighter = ObjectAnimator.ofFloat(topEdgeBar, View.ALPHA, 0.0f, 1.0f);
                            ObjectAnimator bottomEdgeLighter = ObjectAnimator.ofFloat(bottomEdgeBar, View.ALPHA, 0.0f, 1.0f);
                            ObjectAnimator rightEdgeLighter = ObjectAnimator.ofFloat(rightEdgeBar, View.ALPHA, 0.0f, 1.0f);
                            ObjectAnimator leftEdgeLighter = ObjectAnimator.ofFloat(leftEdgeBar, View.ALPHA, 0.0f, 1.0f);

                            topEdgeLighter.setDuration(500);
                            bottomEdgeLighter.setDuration(500);
                            leftEdgeLighter.setDuration(500);
                            rightEdgeLighter.setDuration(500);

                            topEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE);
                            topEdgeLighter.setRepeatCount(1);
                            bottomEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE);
                            bottomEdgeLighter.setRepeatCount(1);
                            leftEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE);
                            leftEdgeLighter.setRepeatCount(1);
                            rightEdgeLighter.setRepeatMode(ObjectAnimator.REVERSE);
                            rightEdgeLighter.setRepeatCount(1);

                            AnimatorSet animatorSet = new AnimatorSet();
                            animatorSet.playTogether(topEdgeLighter, bottomEdgeLighter, leftEdgeLighter, rightEdgeLighter);
                            animatorSet.start();

                            /*
                            ObjectAnimator topEdgeLighterOn = ObjectAnimator.ofFloat(topEdgeBar, View.ALPHA, 0.0f, 1.0f);
                            ObjectAnimator bottomEdgeLighterOn = ObjectAnimator.ofFloat(bottomEdgeBar, View.ALPHA, 0.0f, 1.0f);
                            ObjectAnimator rightEdgeLighterOn = ObjectAnimator.ofFloat(rightEdgeBar, View.ALPHA, 0.0f, 1.0f);
                            ObjectAnimator leftEdgeLighterOn = ObjectAnimator.ofFloat(leftEdgeBar, View.ALPHA, 0.0f, 1.0f);

                            ObjectAnimator topEdgeLighterOff = ObjectAnimator.ofFloat(topEdgeBar, View.ALPHA, 1.0f, 0.0f);
                            ObjectAnimator bottomEdgeLighterOff = ObjectAnimator.ofFloat(bottomEdgeBar, View.ALPHA, 1.0f, 0.0f);
                            ObjectAnimator rightEdgeLighterOff = ObjectAnimator.ofFloat(rightEdgeBar, View.ALPHA, 1.0f, 0.0f);
                            ObjectAnimator leftEdgeLighterOff = ObjectAnimator.ofFloat(leftEdgeBar, View.ALPHA, 1.0f, 0.0f);

                            topEdgeLighterOn.setDuration(250);
                            bottomEdgeLighterOn.setDuration(250);
                            rightEdgeLighterOn.setDuration(250);
                            leftEdgeLighterOn.setDuration(250);

                            topEdgeLighterOff.setDuration(200);
                            bottomEdgeLighterOff.setDuration(200);
                            rightEdgeLighterOff.setDuration(200);
                            leftEdgeLighterOff.setDuration(200);

                            AnimatorSet animateOn = new AnimatorSet();
                            AnimatorSet animateOff = new AnimatorSet();
                            AnimatorSet animatorSet = new AnimatorSet();

                            animateOn.playTogether(topEdgeLighterOn, bottomEdgeLighterOn, rightEdgeLighterOn, leftEdgeLighterOn);

                            animateOff.playTogether(topEdgeLighterOff, bottomEdgeLighterOff, rightEdgeLighterOff, leftEdgeLighterOff);

                            animatorSet.playSequentially(animateOn, animateOff);
                            animatorSet.start();
                             */
                        }
                    }
                });

                cancel.setOnClickListener((new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }));
            }
        });

        binding.btnBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupHashMap.put("AllianceColor", setupHashMap.get("AllianceColor").equals("Blue") ? "" : "Blue");
                updateXMLObjects(false);
            }
        });

        binding.btnRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupHashMap.put("AllianceColor", setupHashMap.get("AllianceColor").equals("Red") ? "" : "Red");
                updateXMLObjects(false);
            }
        });

        binding.btnFirstRobot.setOnClickListener(view -> {
            setupHashMap.put("RobotAssignment", "1");
            updateXMLObjects(false);
        });

        binding.btnSecondRobot.setOnClickListener(view -> {
            setupHashMap.put("RobotAssignment", "2");
            updateXMLObjects(false);
        });

        binding.btnThirdRobot.setOnClickListener(view -> {
            setupHashMap.put("RobotAssignment", "3");
            updateXMLObjects(false);
        });

        binding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ensure that any async operations (like fetching from database) are cancelled
                disposables.clear();
                if (isQRButton) {

                    Dialog dialog = new Dialog(PregameActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.generate_qrcode_confirm_popup);

                    Button generateQRButton = dialog.findViewById(R.id.GenerateQRButton);
                    Button cancelConfirm = dialog.findViewById(R.id.CancelConfirm);

                    dialog.show();

                    generateQRButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            HashMapManager.putSetupHashMap(setupHashMap);
                            dialog.dismiss(); // Dismiss the confirmation dialogue
                            // Show the loading dialog
                            loading_alert = new Dialog(PregameActivity.this);
                            loading_alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            loading_alert.setContentView(R.layout.loading_screen);
                            loading_alert.setCancelable(false);
                            loading_alert.show();

                            QRRunnable runnable = new QRRunnable(PregameActivity.this, loading_alert);
                            new Thread(runnable).start();
                        }
                    });

                    cancelConfirm.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                } else {
                    HashMapManager.putSetupHashMap(setupHashMap);
                    if (binding.etScouterName.getText().toString().equals("Mercury") &&
                            binding.etMatchNumber.getText().toString().equals("1") &&
                            binding.etTeamNumber.getText().toString().equals("0") &&
                            binding.etFirstAlliancePartner.getText().toString().equals("8") &&
                            binding.etSecondAlliancePartner.getText().toString().equals("9")) {
                        settingsHashMap.put("NothingToSeeHere", "1");
                        HashMapManager.setDefaultValues(HashMapManager.HASH.SETUP);
                        setupHashMap = HashMapManager.getSetupHashMap();

                        updateXMLObjects(true);
                        return;
                    } else if (binding.etScouterName.getText().toString().equals("0x") &&
                            binding.etMatchNumber.getText().toString().equals("441") &&
                            binding.etTeamNumber.getText().toString().equals("1089") &&
                            binding.etFirstAlliancePartner.getText().toString().equals("1089") &&
                            binding.etSecondAlliancePartner.getText().toString().equals("1089")) {
                        settingsHashMap.put("Slack", "1");
                        HashMapManager.setDefaultValues(HashMapManager.HASH.SETUP);
                        setupHashMap = HashMapManager.getSetupHashMap();

                        updateXMLObjects(true);
                        return;
                    } else if (binding.etScouterName.getText().toString().equals("admin") &&
                            binding.etMatchNumber.getText().toString().equals("1") &&
                            binding.etTeamNumber.getText().toString().equals("0") &&
                            binding.etFirstAlliancePartner.getText().toString().equals("8") &&
                            binding.etSecondAlliancePartner.getText().toString().equals("9")) {
                        HashMapManager.saveSettingsPassword(new String[]{"", "N"}, PregameActivity.this);
                        HashMapManager.setDefaultValues(HashMapManager.HASH.SETUP);
                        setupHashMap = HashMapManager.getSetupHashMap();

                        updateXMLObjects(true);
                        return;
                    } else if (settingsHashMap.get("NothingToSeeHere").equals("1")) {
                        rooster.start();
                    } else if (binding.etTeamNumber.getText().toString().equals(binding.etFirstAlliancePartner.getText().toString()) ||
                            binding.etTeamNumber.getText().toString().equals(binding.etSecondAlliancePartner.getText().toString())) {
                        Toast.makeText(PregameActivity.this, "A team cannot be its own partner.", Toast.LENGTH_SHORT).show();
                        setupHashMap.put("TeamNumber", "");
                        setupHashMap.put("AlliancePartner1", "");
                        setupHashMap.put("AlliancePartner2", "");
                        binding.etTeamNumber.requestFocus();
                        updateXMLObjects(true);
                        return;

                    }
                    Intent intent = new Intent(PregameActivity.this, MatchActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        binding.btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(PregameActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.clear_confirm_popup);

                Button clearConfirm = dialog.findViewById(R.id.ClearConfirm);
                Button cancelConfirm = dialog.findViewById(R.id.CancelConfirm);

                dialog.show();

                cancelConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                clearConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                        HashMapManager.setDefaultValues(HashMapManager.HASH.SETUP);
                        setupHashMap = HashMapManager.getSetupHashMap();
                        updateXMLObjects(true);
                    }
                });
            }
        });

        binding.btnAutofill.setOnClickListener(view -> {
            if (!canAutoFill()) {
                Toast.makeText(getApplicationContext(), "Match Number, Alliance Color, and Robot Assignment are required to autofill.", Toast.LENGTH_SHORT).show();
                return;
            }
            int matchNumber = Integer.parseInt(binding.etMatchNumber.getText().toString());
            disposables.add(matchRepository.getStoredEventKey().subscribe(
                    eventKey -> {
                        String assignment = (setupHashMap.get("AllianceColor").charAt(0) + setupHashMap.get("RobotAssignment")).toUpperCase();
                        disposables.add(matchRepository.getStoredMatch(DBUtil.createQualificationMatchKey(eventKey, matchNumber)).subscribe(
                                match -> autofillMatchInfo(match, assignment),
                                throwable -> Toast.makeText(getApplicationContext(), "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show()
                        ));
                    },
                    throwable -> Toast.makeText(getApplicationContext(), "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show()
            ));

        });
    }

    public void autofillMatchInfo(Match match, String assignment) {
        boolean allianceColor = assignment.toLowerCase().charAt(0) == 'r'; // 1 = red, 0 = blue
        List<Integer> teams = allianceColor ? match.getRedAllianceTeams() : match.getBlueAllianceTeams();
        // Clear boxes that are going to be autofilled
        binding.etTeamNumber.getText().clear();
        binding.etFirstAlliancePartner.getText().clear();
        binding.etSecondAlliancePartner.getText().clear();
        int assignmentNumber = Integer.parseInt(String.valueOf(assignment.charAt(assignment.length()-1))) - 1; // because teams is zero-indexed
        for (int i = 0; i < teams.size(); i++) {
            int team = teams.get(i);
            if (i == assignmentNumber) binding.etTeamNumber.setText(String.valueOf(team));
            else if (binding.etFirstAlliancePartner.getText().toString().isEmpty()) {
                binding.etFirstAlliancePartner.setText(String.valueOf(team));
            }
            else {
                binding.etSecondAlliancePartner.setText(String.valueOf(team));
            }
        }
        Toast.makeText(getApplicationContext(), "Successfully autofilled match info!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateXMLObjects(true);
    }

    //call methods
    /*public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }*/

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        return super.dispatchTouchEvent(ev);
    }

    /*
    - Validate text field input and toggle values to make sure it is safe (and necessary) to
    - move into the MatchActivity
     */
    private boolean readyToStart() {
        return binding.etScouterName.getText().length() > 0 &&
                binding.etMatchNumber.getText().length() > 0 &&
                binding.etTeamNumber.getText().length() > 0 &&
                binding.etFirstAlliancePartner.getText().length() > 0 &&
                binding.etSecondAlliancePartner.getText().length() > 0 &&
                !setupHashMap.get("AllianceColor").isEmpty() &&
                !setupHashMap.get("RobotAssignment").isEmpty() &&
                (Objects.equals(setupHashMap.get("NoShow"), "Y") || Objects.equals(setupHashMap.get("NoShow"), "N"));
    }

    private boolean canAutoFill() {
        return !binding.etMatchNumber.getText().toString().isEmpty() &&
                !setupHashMap.get("AllianceColor").isEmpty() &&
                !setupHashMap.get("RobotAssignment").isEmpty();
    }

    /*
    - Check to see if there's any values that need to be cleared
    - (if nothing is filled out, clear button should be disabled)
     */
    private boolean canClearInputs() {
        return binding.etScouterName.getText().length() > 0 ||
                binding.etMatchNumber.getText().length() > 0 ||
                binding.etTeamNumber.getText().length() > 0 ||
                binding.switchNoShow.isChecked() ||
                binding.etFirstAlliancePartner.getText().length() > 0 ||
                binding.etSecondAlliancePartner.getText().length() > 0 ||
                binding.btnBlue.isSelected() || binding.btnRed.isSelected() ||
                binding.btnFirstRobot.isSelected() || binding.btnSecondRobot.isSelected() ||
                binding.btnThirdRobot.isSelected();
    }

    private void setRobotAssignmentButtonsEnabled(boolean enabled) {
        if (!enabled) setupHashMap.put("RobotAssignment", "");
        binding.tvRobotAssignment.setEnabled(enabled);
        binding.btnFirstRobot.setEnabled(enabled);
        binding.btnSecondRobot.setEnabled(enabled);
        binding.btnThirdRobot.setEnabled(enabled);
    }

    private void setRobotAssignmentButtonsBackground(String allianceColor) {
        int bgDrawableID = allianceColor.equals("Red") ? R.drawable.toggle_red_states : allianceColor.equals("Blue") ? R.drawable.toggle_blue_states : 0;
        if (bgDrawableID == 0) return;
        binding.btnFirstRobot.setBackground(getDrawable(bgDrawableID));
        binding.btnSecondRobot.setBackground(getDrawable(bgDrawableID));
        binding.btnThirdRobot.setBackground(getDrawable(bgDrawableID));
    }

    /*
    - Big complicated looking function, so let's break it down
        - This is called on most events (so in all the View EventListeners)
        - It updates hashmaps and the visual appearance of Views
     */
    private void updateXMLObjects(boolean updateText) {
        boolean readyToStart = readyToStart();
        boolean canClear = canClearInputs();

        binding.btnAutofill.setEnabled(!binding.etMatchNumber.getText().toString().isEmpty());

        /*
        - updateText should only be true if you want to reset the basic info fields to the stored hashmap values
            - e.g. if you're returning from SettingsActivity or if you used the "Clear" button
         */
        if (updateText) {
            binding.etScouterName.setText(setupHashMap.get("ScouterName"));
            binding.etMatchNumber.setText(setupHashMap.get("MatchNumber"));
            binding.etTeamNumber.setText(setupHashMap.get("TeamNumber"));
            binding.etFirstAlliancePartner.setText(setupHashMap.get("AlliancePartner1"));
            binding.etSecondAlliancePartner.setText(setupHashMap.get("AlliancePartner2"));
        }
        setRobotAssignmentButtonsEnabled(!setupHashMap.get("AllianceColor").isEmpty());
        // Dynamically set background of robot assignment buttons
        setRobotAssignmentButtonsBackground(setupHashMap.get("AllianceColor"));

        binding.btnBlue.setSelected(setupHashMap.get("AllianceColor").equals("Blue"));
        binding.btnRed.setSelected(setupHashMap.get("AllianceColor").equals("Red"));
        binding.btnFirstRobot.setSelected(setupHashMap.get("RobotAssignment").equals("1"));
        binding.btnSecondRobot.setSelected(setupHashMap.get("RobotAssignment").equals("2"));
        binding.btnThirdRobot.setSelected(setupHashMap.get("RobotAssignment").equals("3"));

        if (settingsHashMap.get("Slack").equals("1"))
            slackCenter.setVisibility(View.VISIBLE);

        binding.switchPreload.setChecked(setupHashMap.get("PreloadNote").equals("Y"));

        if (setupHashMap.get("NoShow").equals("Y")) {
            binding.switchPreload.setEnabled(false);
            binding.switchNoShow.setChecked(true);

            binding.btnStart.setPadding(185, 0, 185, 0);
            binding.btnStart.setText(R.string.GenerateQRCode);
            binding.btnStart.setCompoundDrawablesRelativeWithIntrinsicBounds(this.getDrawable(R.drawable.qr), null, null, null);
            isQRButton = true;
        } else {
            binding.switchPreload.setEnabled(true);
            binding.switchNoShow.setChecked(false);
            binding.btnStart.setCompoundDrawablesRelativeWithIntrinsicBounds(this.getDrawable(R.drawable.start_button_symbol_states), null, null, null);
            binding.btnStart.setPadding(234, 0, 234, 0);
            binding.btnStart.setText(R.string.Start);
            isQRButton = false;
        }

        binding.btnStart.setEnabled(readyToStart);
        binding.tvStartDirections.setEnabled(readyToStart && !isQRButton);
        binding.btnClear.setEnabled(canClear);
    }
}