package com.mercury1089.scoutingapp2025;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Vibrator;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import java.util.LinkedHashMap;
import androidx.fragment.app.Fragment;

import com.mercury1089.scoutingapp2025.databinding.FragmentAutonBinding;
import com.mercury1089.scoutingapp2025.listeners.NumericalDataInputListener;
import com.mercury1089.scoutingapp2025.listeners.UpdateListener;
import com.mercury1089.scoutingapp2025.utils.GenUtils;

public class Auton extends Fragment implements UpdateListener {
    private FragmentAutonBinding binding;

    //HashMaps for sending QR data between screens
    private LinkedHashMap<String, String> setupHashMap;
    private LinkedHashMap<String, String> autonHashMap;
    private static CountDownTimer timer;
    private boolean firstTime = true;
    private boolean running = true;
    private ValueAnimator teleopButtonAnimation;
    private AnimatorSet animatorSet;

    /*
    - Runs when a new instance of this fragment is created (i.e. when it is first loaded in from PregameActivity.java)
     */
    public static Auton newInstance() {
        Auton fragment = new Auton();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    MatchActivity context;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAutonBinding.inflate(inflater, container, false);

        context = (MatchActivity) getActivity();
        View inflated = null;
        try {
            inflated = inflater.inflate(R.layout.fragment_auton, container, false);
        } catch (InflateException e) {
            Log.d("Oncreateview", "ERROR");
            throw e;
        }
        return inflated;
    }

    public void onStart(){
        super.onStart();

        //get HashMap data (fill with defaults if empty or null)
        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.SETUP);
        HashMapManager.checkNullOrEmpty(HashMapManager.HASH.AUTON);
        setupHashMap = HashMapManager.getSetupHashMap();
        autonHashMap = HashMapManager.getAutonHashMap();

        //fill in counters with data
        updateXMLObjects();

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        timer = new CountDownTimer(15000, 1000) {

            public void onTick(long millisUntilFinished) {
                binding.tvAutonSecondsCounter.setText(GenUtils.padLeftZeros("" + millisUntilFinished / 1000, 2));

                if(!running)
                    return;

                if (millisUntilFinished / 1000 <= 3 && millisUntilFinished / 1000 > 0) {  //play the blinking animation
                    binding.tvTeleopWarning.setVisibility(View.VISIBLE);
                    binding.tvAutonSecondsRemaining.setTextColor(context.getResources().getColor(R.color.banana));
                    binding.tvAutonSecondsRemaining.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.timer_yellow, 0, 0, 0);

                    vibrator.vibrate(500);

                    ObjectAnimator topEdgeLighter = ObjectAnimator.ofFloat(binding.imgTopEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator bottomEdgeLighter = ObjectAnimator.ofFloat(binding.imgBottomEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator rightEdgeLighter = ObjectAnimator.ofFloat(binding.imgRightEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator leftEdgeLighter = ObjectAnimator.ofFloat(binding.imgLeftEdgeBar, View.ALPHA, 0.0f, 1.0f);

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

                }
            }

            public void onFinish() { //sets the label to display a teleop error background and text
                if(running) {
                    binding.tvAutonSecondsCounter.setText("00");
                    binding.imgTopEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_error));
                    binding.imgBottomEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_error));
                    binding.imgLeftEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_error));
                    binding.imgRightEdgeBar.setBackground(getResources().getDrawable(R.drawable.teleop_error));
                    binding.tvAutonSecondsRemaining.setTextColor(context.getResources().getColor(R.color.border_warning));
                    binding.tvAutonSecondsRemaining.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.timer_red, 0, 0, 0);
                    binding.tvTeleopWarning.setTextColor(getResources().getColor(R.color.white));
                    binding.tvTeleopWarning.setBackground(getResources().getDrawable(R.drawable.teleop_error));
                    binding.tvTeleopWarning.setText(getResources().getString(R.string.TeleopError));

                    ObjectAnimator topEdgeLighter = ObjectAnimator.ofFloat(binding.imgTopEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator bottomEdgeLighter = ObjectAnimator.ofFloat(binding.imgBottomEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator rightEdgeLighter = ObjectAnimator.ofFloat(binding.imgRightEdgeBar, View.ALPHA, 0.0f, 1.0f);
                    ObjectAnimator leftEdgeLighter = ObjectAnimator.ofFloat(binding.imgLeftEdgeBar, View.ALPHA, 0.0f, 1.0f);

                    int currentButtonColor = GenUtils.getAColor(context, R.color.melon);
                    if(!binding.btnNextTeleop.isEnabled())
                        currentButtonColor = GenUtils.getAColor(context, R.color.night);

                    ValueAnimator teleopButtonAnim = ValueAnimator.ofArgb(currentButtonColor, GenUtils.getAColor(context, R.color.fire));
                    teleopButtonAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            binding.btnNextTeleop.setBackgroundColor((Integer)animation.getAnimatedValue());
                        }
                    });

                    int currentArrowColor = GenUtils.getAColor(context, R.color.ice);
                    if(!binding.btnNextTeleop.isEnabled())
                        currentArrowColor = GenUtils.getAColor(context, R.color.ocean);

                    ValueAnimator teleopArrowAnim = ValueAnimator.ofArgb(currentArrowColor, GenUtils.getAColor(context, R.color.ice));
                    teleopArrowAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            binding.btnNextTeleop.getCompoundDrawablesRelative()[2].setColorFilter((Integer)animation.getAnimatedValue(), PorterDuff.Mode.SRC_IN);
                        }
                    });

                    teleopArrowAnim.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            binding.btnNextTeleop.getCompoundDrawablesRelative()[2].clearColorFilter();
                            binding.btnNextTeleop.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.right,0);
                        }
                    });

                    ValueAnimator teleopTextAnim = ValueAnimator.ofArgb(binding.btnNextTeleop.getCurrentTextColor(), GenUtils.getAColor(context, R.color.ice));
                    teleopTextAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            binding.btnNextTeleop.setTextColor((Integer)animation.getAnimatedValue());
                        }
                    });

                    topEdgeLighter.setDuration(500);
                    bottomEdgeLighter.setDuration(500);
                    leftEdgeLighter.setDuration(500);
                    rightEdgeLighter.setDuration(500);
                    teleopButtonAnim.setDuration(500);
                    teleopTextAnim.setDuration(500);
                    teleopArrowAnim.setDuration(500);

                    AnimatorSet animatorSet1 = new AnimatorSet();
                    animatorSet1.playTogether(topEdgeLighter, bottomEdgeLighter, leftEdgeLighter, rightEdgeLighter, teleopButtonAnim, teleopTextAnim, teleopArrowAnim);

                    teleopButtonAnimation = ValueAnimator.ofArgb(GenUtils.getAColor(context, R.color.fire), GenUtils.getAColor(context, R.color.ocean));

                    teleopButtonAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            binding.btnNextTeleop.setBackgroundColor((Integer)animation.getAnimatedValue());
                        }
                    });

                    teleopButtonAnimation.setDuration(500);;
                    teleopButtonAnimation.setRepeatMode(ValueAnimator.REVERSE);
                    teleopButtonAnimation.setRepeatCount(ValueAnimator.INFINITE);

                    animatorSet = new AnimatorSet();
                    animatorSet.playSequentially(animatorSet1, teleopButtonAnimation);
                    animatorSet.start();
                }
            }
        };

        if(firstTime) {
            firstTime = false;
            timer.start();
        }
        else {
            binding.imgTopEdgeBar.setAlpha(1);
            binding.imgBottomEdgeBar.setAlpha(1);
            binding.imgRightEdgeBar.setAlpha(1);
            binding.imgLeftEdgeBar.setAlpha(1);
        }

        //set listeners for buttons and fill the hashmap with data
        binding.btnCoralPlusPickedUp.setOnClickListener(new NumericalDataInputListener(binding.tvCoralPickedUpCounter, autonHashMap, "CoralPickedUp", true, this));
        binding.btnCoralMinusPickedUp.setOnClickListener(new NumericalDataInputListener(binding.tvCoralPickedUpCounter, autonHashMap, "CoralPickedUp", false, this));
        binding.btnAlgaePlusPickedUp.setOnClickListener(new NumericalDataInputListener(binding.tvAlgaePickedUpCounter, autonHashMap, "AlgaePickedUp", true, this));
        binding.btnAlgaeMinusPickedUp.setOnClickListener(new NumericalDataInputListener(binding.tvAlgaePickedUpCounter, autonHashMap, "AlgaePickedUp", false, this));

        binding.btnL4PlusScored.setOnClickListener(new NumericalDataInputListener(binding.tvL4ScoredCounter, autonHashMap, "ScoredCoralL4", true, this));
        binding.btnL4MinusScored.setOnClickListener(new NumericalDataInputListener(binding.tvL4ScoredCounter, autonHashMap, "ScoredCoralL4", false, this));
        binding.btnL3PlusScored.setOnClickListener(new NumericalDataInputListener(binding.tvL3ScoredCounter, autonHashMap, "ScoredCoralL3", true, this));
        binding.btnL3MinusScored.setOnClickListener(new NumericalDataInputListener(binding.tvL3ScoredCounter, autonHashMap, "ScoredCoralL3", false, this));
        binding.btnL2PlusScored.setOnClickListener(new NumericalDataInputListener(binding.tvL2ScoredCounter, autonHashMap, "ScoredCoralL2", true, this));
        binding.btnL2MinusScored.setOnClickListener(new NumericalDataInputListener(binding.tvL2ScoredCounter, autonHashMap, "ScoredCoralL2", false, this));
        binding.btnL1PlusScored.setOnClickListener(new NumericalDataInputListener(binding.tvL1ScoredCounter, autonHashMap, "ScoredCoralL1", true, this));
        binding.btnL1MinusScored.setOnClickListener(new NumericalDataInputListener(binding.tvL1ScoredCounter, autonHashMap, "ScoredCoralL1", false, this));

        binding.btnL4PlusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvL4MissedCounter, autonHashMap, "MissedCoralL4", true, this));
        binding.btnL4MinusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvL4MissedCounter, autonHashMap, "MissedCoralL4", false, this));
        binding.btnL3PlusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvL3MissedCounter, autonHashMap, "MissedCoralL3", true, this));
        binding.btnL3MinusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvL3MissedCounter, autonHashMap, "MissedCoralL3", false, this));
        binding.btnL2PlusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvL2MissedCounter, autonHashMap, "MissedCoralL2", true, this));
        binding.btnL2MinusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvL2MissedCounter, autonHashMap, "MissedCoralL2", false, this));
        binding.btnL1PlusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvL1MissedCounter, autonHashMap, "MissedCoralL1", true, this));
        binding.btnL1MinusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvL1MissedCounter, autonHashMap, "MissedCoralL1", false, this));

        binding.btnL3PlusRemoved.setOnClickListener(new NumericalDataInputListener(binding.tvL3RemovedCounter, autonHashMap, "RemovedAlgaeL3", true, this));
        binding.btnL3MinusRemoved.setOnClickListener(new NumericalDataInputListener(binding.tvL3RemovedCounter, autonHashMap, "RemovedAlgaeL3", false, this));
        binding.btnL2PlusRemoved.setOnClickListener(new NumericalDataInputListener(binding.tvL2RemovedCounter, autonHashMap, "RemovedAlgaeL2", true, this));
        binding.btnL2MinusRemoved.setOnClickListener(new NumericalDataInputListener(binding.tvL2RemovedCounter, autonHashMap, "RemovedAlgaeL2", false, this));

        binding.btnL3PlusAttempted.setOnClickListener(new NumericalDataInputListener(binding.tvL3AttemptedCounter, autonHashMap, "AttemptedAlgaeL3", true, this));
        binding.btnL3MinusAttempted.setOnClickListener(new NumericalDataInputListener(binding.tvL3AttemptedCounter, autonHashMap, "AttemptedAlgaeL3", false, this));
        binding.btnL2PlusAttempted.setOnClickListener(new NumericalDataInputListener(binding.tvL2AttemptedCounter, autonHashMap, "AttemptedAlgaeL2", true, this));
        binding.btnL2MinusAttempted.setOnClickListener(new NumericalDataInputListener(binding.tvL2AttemptedCounter, autonHashMap, "AttemptedAlgaeL2", false, this));

        binding.btnProcessorPlusScored.setOnClickListener(new NumericalDataInputListener(binding.tvProcessorScoredCounter, autonHashMap, "ScoredAlgaeProcessor", true, this));
        binding.btnProcessorMinusScored.setOnClickListener(new NumericalDataInputListener(binding.tvProcessorScoredCounter, autonHashMap, "ScoredAlgaeProcessor", false, this));
        binding.btnProcessorPlusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvProcessorMissedCounter, autonHashMap, "MissedAlgaeProcessor", true, this));
        binding.btnProcessorMinusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvProcessorMissedCounter, autonHashMap, "MissedAlgaeProcessor", false, this));

        binding.btnNetPlusScored.setOnClickListener(new NumericalDataInputListener(binding.tvNetScoredCounter, autonHashMap, "ScoredAlgaeNet", true, this));
        binding.btnNetMinusScored.setOnClickListener(new NumericalDataInputListener(binding.tvNetScoredCounter, autonHashMap, "ScoredAlgaeNet", false, this));
        binding.btnNetPlusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvNetMissedCounter, autonHashMap, "MissedAlgaeNet", true, this));
        binding.btnNetMinusMissed.setOnClickListener(new NumericalDataInputListener(binding.tvNetMissedCounter, autonHashMap, "MissedAlgaeNet", false, this));


        binding.switchLeave.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                autonHashMap.put("Leave", isChecked ? "Y" : "N");
                updateXMLObjects();
            }
        });

        binding.switchFellOver.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setupHashMap.put("FellOver", isChecked ? "Y" : "N");
                updateXMLObjects();
            }
        });

        binding.btnNextTeleop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.tabs.getTabAt(1).select();
            }
        });
    }

    private void possessionButtonsEnabledState(boolean enable){
        binding.tvCoralPossession.setEnabled(enable);
        binding.tvCoralPickedUp.setEnabled(enable);
        binding.btnCoralPlusPickedUp.setEnabled(enable);
        binding.btnAlgaeMinusPickedUp.setEnabled(enable);
        binding.tvCoralPickedUpCounter.setEnabled(enable);

        binding.tvAlgaePossession.setEnabled(enable);
        binding.tvAlgaePickedUp.setEnabled(enable);
        binding.btnAlgaePlusPickedUp.setEnabled(enable);
        binding.btnAlgaeMinusPickedUp.setEnabled(enable);
        binding.tvAlgaePickedUpCounter.setEnabled(enable);
    }

    private void scoringButtonsEnabledState(boolean enable){
        binding.tvCoralHeader.setEnabled(enable);
        binding.tvReef.setEnabled(enable);
        binding.tvL4Coral.setEnabled(enable);
        binding.tvL3Coral.setEnabled(enable);
        binding.tvL2Coral.setEnabled(enable);
        binding.tvL1Coral.setEnabled(enable);

        binding.tvL4Scored.setEnabled(enable);
        binding.btnL4PlusScored.setEnabled(enable);
        binding.btnL4MinusScored.setEnabled(enable);
        binding.tvL4ScoredCounter.setEnabled(enable);

        binding.tvL4Missed.setEnabled(enable);
        binding.btnL4PlusMissed.setEnabled(enable);
        binding.btnL4MinusMissed.setEnabled(enable);
        binding.tvL4MissedCounter.setEnabled(enable);

        binding.tvL3Scored.setEnabled(enable);
        binding.btnL3PlusScored.setEnabled(enable);
        binding.btnL3MinusScored.setEnabled(enable);
        binding.tvL3ScoredCounter.setEnabled(enable);

        binding.tvL3Missed.setEnabled(enable);
        binding.btnL3PlusMissed.setEnabled(enable);
        binding.btnL3MinusMissed.setEnabled(enable);
        binding.tvL3MissedCounter.setEnabled(enable);

        binding.tvL2Scored.setEnabled(enable);
        binding.btnL2PlusScored.setEnabled(enable);
        binding.btnL2MinusScored.setEnabled(enable);
        binding.tvL2ScoredCounter.setEnabled(enable);

        binding.tvL2Missed.setEnabled(enable);
        binding.btnL2PlusMissed.setEnabled(enable);
        binding.btnL2MinusMissed.setEnabled(enable);
        binding.tvL2MissedCounter.setEnabled(enable);

        binding.tvL1Scored.setEnabled(enable);
        binding.btnL1PlusScored.setEnabled(enable);
        binding.btnL1MinusScored.setEnabled(enable);
        binding.tvL1ScoredCounter.setEnabled(enable);

        binding.tvL1Missed.setEnabled(enable);
        binding.btnL1PlusMissed.setEnabled(enable);
        binding.btnL1MinusMissed.setEnabled(enable);
        binding.tvL1MissedCounter.setEnabled(enable);

        binding.tvAlgaeHeader.setEnabled(enable);
        binding.tvDealgaefying.setEnabled(enable);
        binding.tvL3Algae.setEnabled(enable);
        binding.tvL2Algae.setEnabled(enable);

        binding.tvL3Removed.setEnabled(enable);
        binding.btnL3PlusRemoved.setEnabled(enable);
        binding.btnL3MinusRemoved.setEnabled(enable);
        binding.tvL3RemovedCounter.setEnabled(enable);

        binding.tvL2Removed.setEnabled(enable);
        binding.btnL2PlusRemoved.setEnabled(enable);
        binding.btnL2MinusRemoved.setEnabled(enable);
        binding.tvL2RemovedCounter.setEnabled(enable);

        binding.tvL3Attempted.setEnabled(enable);
        binding.btnL3PlusAttempted.setEnabled(enable);
        binding.btnL3MinusAttempted.setEnabled(enable);
        binding.tvL3AttemptedCounter.setEnabled(enable);

        binding.tvL2Attempted.setEnabled(enable);
        binding.btnL2PlusAttempted.setEnabled(enable);
        binding.btnL2MinusAttempted.setEnabled(enable);
        binding.tvL2AttemptedCounter.setEnabled(enable);

        binding.tvProcessor.setEnabled(enable);
        binding.tvProcessorScored.setEnabled(enable);
        binding.btnProcessorPlusScored.setEnabled(enable);
        binding.btnProcessorMinusScored.setEnabled(enable);
        binding.tvProcessorScoredCounter.setEnabled(enable);
        binding.tvProcessorMissed.setEnabled(enable);
        binding.btnProcessorPlusMissed.setEnabled(enable);
        binding.btnProcessorMinusMissed.setEnabled(enable);
        binding.tvProcessorMissedCounter.setEnabled(enable);

        binding.tvNet.setEnabled(enable);
        binding.tvNetScored.setEnabled(enable);
        binding.btnNetPlusScored.setEnabled(enable);
        binding.btnNetMinusScored.setEnabled(enable);
        binding.tvNetScoredCounter.setEnabled(enable);
        binding.tvNetMissed.setEnabled(enable);
        binding.btnNetPlusMissed.setEnabled(enable);
        binding.tvNetMissedCounter.setEnabled(enable);
}

private void miscButtonsEnabledState(boolean enable){
        binding.tvMiscDirections.setEnabled(enable);
        binding.switchLeave.setEnabled(enable);
        binding.tvLeave.setEnabled(enable);
        binding.btnNextTeleop.setEnabled(enable);
    }

    private void allButtonsEnabledState(boolean enable){
        possessionButtonsEnabledState(enable);
        scoringButtonsEnabledState(enable);
        miscButtonsEnabledState(enable);
    }

    public void updateXMLObjects(){
        binding.tvL4ScoredCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("ScoredCoralL4"), 3));
        binding.tvL3ScoredCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("ScoredCoralL3"), 3));
        binding.tvL2ScoredCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("ScoredCoralL2"), 3));
        binding.tvL1ScoredCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("ScoredCoralL1"), 3));

        binding.tvL4MissedCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("MissedCoralL4"), 3));
        binding.tvL3MissedCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("MissedCoralL3"), 3));
        binding.tvL2MissedCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("MissedCoralL2"), 3));
        binding.tvL1MissedCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("MissedCoralL1"), 3));

        binding.tvL3RemovedCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("RemovedAlgaeL3"), 3));
        binding.tvL2RemovedCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("RemovedAlgaeL2"), 3));
        binding.tvL3AttemptedCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("AttemptedAlgaeL3"), 3));
        binding.tvL2AttemptedCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("AttemptedAlgaeL2"), 3));

        binding.tvProcessorScoredCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("ScoredAlgaeProcessor"), 3));
        binding.tvProcessorMissedCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("MissedAlgaeProcessor"), 3));
        binding.tvNetScoredCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("ScoredAlgaeNet"), 3));
        binding.tvNetMissedCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("MissedAlgaeNet"), 3));

        binding.tvCoralPickedUpCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("CoralPickedUp"), 3));
        binding.tvAlgaePickedUpCounter.setText(GenUtils.padLeftZeros(autonHashMap.get("AlgaePickedUp"), 3));

        binding.switchLeave.setChecked(autonHashMap.get("Leave").equals("Y"));

        if(setupHashMap.get("FellOver").equals("Y")) {
            binding.switchFellOver.setChecked(true);
            binding.btnNextTeleop.setPadding(150, 0, 150, 0);
            binding.btnNextTeleop.setText(R.string.GenerateQRCode);
            allButtonsEnabledState(false);
        } else {
            binding.switchFellOver.setChecked(false);
            binding.btnNextTeleop.setPadding(150, 0, 185, 0);
            binding.btnNextTeleop.setText(R.string.TeleopNext);
            allButtonsEnabledState(true);
            // Disables decrement buttons if counter is at 0
            // There's totally a better way to do this without the redundancy
            binding.btnCoralMinusPickedUp.setEnabled(Integer.parseInt(binding.tvCoralPickedUpCounter.getText().toString()) > 0);
            binding.btnAlgaeMinusPickedUp.setEnabled(Integer.parseInt(binding.tvAlgaePickedUpCounter.getText().toString()) > 0);
            binding.btnCoralMinusPickedUp.setEnabled(Integer.parseInt(binding.tvCoralPickedUpCounter.getText().toString()) > 0);
            binding.btnAlgaeMinusPickedUp.setEnabled(Integer.parseInt(binding.tvAlgaePickedUpCounter.getText().toString()) > 0);
            binding.btnL4MinusScored.setEnabled(Integer.parseInt(binding.tvL4ScoredCounter.getText().toString()) > 0);
            binding.btnL3MinusScored.setEnabled(Integer.parseInt(binding.tvL3ScoredCounter.getText().toString()) > 0);
            binding.btnL2MinusScored.setEnabled(Integer.parseInt(binding.tvL2ScoredCounter.getText().toString()) > 0);
            binding.btnL1MinusScored.setEnabled(Integer.parseInt(binding.tvL1ScoredCounter.getText().toString()) > 0);

            binding.btnL4MinusMissed.setEnabled(Integer.parseInt(binding.tvL4MissedCounter.getText().toString()) > 0);
            binding.btnL3MinusMissed.setEnabled(Integer.parseInt(binding.tvL3MissedCounter.getText().toString()) > 0);
            binding.btnL2MinusMissed.setEnabled(Integer.parseInt(binding.tvL2MissedCounter.getText().toString()) > 0);
            binding.btnL1MinusMissed.setEnabled(Integer.parseInt(binding.tvL1MissedCounter.getText().toString()) > 0);

            binding.btnL3MinusRemoved.setEnabled(Integer.parseInt(binding.tvL3RemovedCounter.getText().toString()) > 0);
            binding.btnL2MinusRemoved.setEnabled(Integer.parseInt(binding.tvL2RemovedCounter.getText().toString()) > 0);
            binding.btnL3MinusAttempted.setEnabled(Integer.parseInt(binding.tvL3AttemptedCounter.getText().toString()) > 0);
            binding.btnL2MinusAttempted.setEnabled(Integer.parseInt(binding.tvL2AttemptedCounter.getText().toString()) > 0);

            binding.btnProcessorMinusScored.setEnabled(Integer.parseInt(binding.tvProcessorScoredCounter.getText().toString()) > 0);
            binding.btnProcessorMinusMissed.setEnabled(Integer.parseInt(binding.tvProcessorMissedCounter.getText().toString()) > 0);

            binding.btnNetMinusScored.setEnabled(Integer.parseInt(binding.tvNetScoredCounter.getText().toString()) > 0);
            binding.btnNetMinusMissed.setEnabled(Integer.parseInt(binding.tvNetMissedCounter.getText().toString()) > 0);
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        // Make sure that we are currently visible
        if (this.isVisible()) {
            // If we are becoming visible, then...
            if (isVisibleToUser) {
                setupHashMap = HashMapManager.getSetupHashMap();
                autonHashMap = HashMapManager.getAutonHashMap();
                updateXMLObjects();
                // Set all objects in the fragment to their values from the HashMaps
            } else {
                if(teleopButtonAnimation != null) {
                    teleopButtonAnimation.cancel();
                    binding.btnNextTeleop.setBackground(getResources().getDrawable(R.drawable.button_next_states));
                    binding.btnNextTeleop.setTextColor(new ColorStateList(
                            new int [] [] {
                                    new int [] {android.R.attr.state_enabled},
                                    new int [] {}
                            },
                            new int [] {
                                    GenUtils.getAColor(context, R.color.ice),
                                    GenUtils.getAColor(context, R.color.ocean)
                            }
                    ));
                    binding.btnNextTeleop.setCompoundDrawablesRelativeWithIntrinsicBounds(0,0,R.drawable.right_states,0);
                    binding.btnNextTeleop.setSelected(true);
                }
                HashMapManager.putSetupHashMap(setupHashMap);
                HashMapManager.putAutonHashMap(autonHashMap);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        /*
         * A fragment instance can outlive its View, so if we don't set
         * it to null, the binding variable will hold a reference to a destroyed
         * fragment instance even after the view is destroyed which causes a memory leak
        */
        binding = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        running = false;
        timer.cancel();
    }

    @Override
    public void onUpdate() {
        updateXMLObjects();
    }
}
