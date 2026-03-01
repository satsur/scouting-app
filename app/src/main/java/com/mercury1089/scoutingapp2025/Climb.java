package com.mercury1089.scoutingapp2025;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import java.util.LinkedHashMap;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.mercury1089.scoutingapp2025.qr.QRRunnable;

public class Climb extends Fragment {
    //HashMaps for sending QR data between screens
    private LinkedHashMap<String, String> setupHashMap;
    private LinkedHashMap<String, String> climbHashMap;

    //Buttons
    private ImageButton scoredTrapButton;
    private ImageButton notScoredTrapButton;
    private ImageButton missedTrapButton;
    private ImageButton notMissedTrapButton;
    private Button generateQRButton;

    //Switches
    private Switch parkSwitch;
    private Switch hangSwitch;

    //TextViews
    private TextView endgameID;
    private TextView endgameDirectionsID;
    private TextView bargeZoneID;
    private TextView bargeZoneDirections;
    private TextView parkID;
    private TextView hangDirections;
    private TextView hangID;

    //Containers
    private TabLayout bargeTabs;

    //other variables
    private ProgressDialog progressDialog;
    private Dialog loading_alert;
    public final static int QRCodeSize = 500;

    public static Climb newInstance() {
        Climb fragment = new Climb();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private MatchActivity context;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        context = (MatchActivity) getActivity();
        return inflater.inflate(R.layout.fragment_climb, container, false);
    }

    public void onStart(){
        super.onStart();

        //linking variables to XML elements on the screen
        endgameID = getView().findViewById(R.id.IDEndgame);
        endgameDirectionsID = getView().findViewById(R.id.IDEndgameDirections);
        bargeZoneID = getView().findViewById(R.id.IDBargeZone);
        bargeZoneDirections = getView().findViewById(R.id.IDBargeZoneDirections);

        parkID = getView().findViewById(R.id.IDPark);
        parkSwitch = getView().findViewById(R.id.parkSwitch);

        hangID = getView().findViewById(R.id.IDHanged);
        hangSwitch = getView().findViewById(R.id.hangSwitch);
        hangDirections = getView().findViewById(R.id.IDHangedDirections);
        bargeTabs = getView().findViewById(R.id.stageTabs);

        generateQRButton = getView().findViewById(R.id.GenerateQRButton);

        //Removes tab indicator because climb switch starts out as null
        bargeTabs.setSelectedTabIndicator(null);

        //set listeners for buttons

        parkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                climbHashMap.put("Park", isChecked ? "Y" : "N");
                updateXMLObjects();
            }
        });

        hangSwitch.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                climbHashMap.put("Hang", isChecked ? "Y" : "N");
                //Default option for rung is LOW
                if (isChecked) {
                    //Sets tab indicator to built-in default
                    bargeTabs.setSelectedTabIndicator(R.drawable.mtrl_tabs_default_indicator);
                    bargeTabs.getTabAt(0).select();
                    climbHashMap.put("Barge", "S");
                } else {
                    //Removes tab indicator
                    bargeTabs.setSelectedTabIndicator(null);

                }
                updateXMLObjects();
            }
        });

        bargeTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String text = (String) tab.getText();
                if (text.equals(getResources().getString(R.string.Shallow)))
                    climbHashMap.put("Barge", "S");
                else if (text.equals(getResources().getString(R.string.Deep)))
                    climbHashMap.put("Barge", "D");
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { climbHashMap.put("Barge", "N"); }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        generateQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog dialog = new Dialog(context);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.generate_qrcode_confirm_popup);

                Button generateQRButton = dialog.findViewById(R.id.GenerateQRButton);
                Button cancelConfirm = dialog.findViewById(R.id.btn_cancel);

                dialog.show();

                generateQRButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        HashMapManager.putSetupHashMap(setupHashMap);
                        HashMapManager.putClimbHashMap(climbHashMap);
                        dialog.dismiss(); // Dismiss the confirmation dialog
                        // Show the loading dialog
                        loading_alert = new Dialog(context);
                        loading_alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        loading_alert.setContentView(R.layout.loading_screen);
                        loading_alert.setCancelable(false);
                        loading_alert.show();

                        QRRunnable qrRunnable = new QRRunnable(context, loading_alert); // Uses MatchActivity context
                        new Thread(qrRunnable).start();
                        dialog.dismiss();
                    }
                });

                cancelConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void bargeTabsEnabledState(boolean enable) {
        if (!enable)
            climbHashMap.put("Barge", "N");
        bargeTabs.setEnabled(enable);
        LinearLayout tabStrip = ((LinearLayout) bargeTabs.getChildAt(0));
        tabStrip.setEnabled(enable);
        for(int i = 0; i < tabStrip.getChildCount(); i++) {
            tabStrip.getChildAt(i).setEnabled(enable);
            tabStrip.getChildAt(i).setClickable(enable);
        }
    }

    private void bargeButtonsEnabledState(boolean enable) {
        parkSwitch.setEnabled(enable);
        hangSwitch.setEnabled(enable);

        //Always want the climbed switch and "climb" text next to switch to be enabled unless fell over/died is checked
        bargeTabsEnabledState(enable);
    }

    private void bargeZoneEnabledState(boolean enable) {
        // "Post Match" title and directions
        endgameID.setEnabled(enable);
        endgameDirectionsID.setEnabled(enable);
        // Other TextViews
        bargeZoneID.setEnabled(enable);
        bargeZoneDirections.setEnabled(enable);
        hangDirections.setEnabled(enable);
        parkID.setEnabled(enable);
        hangID.setEnabled(enable);

        // Buttons
        bargeButtonsEnabledState(enable);
    }

    private void updateXMLObjects() {
        if (setupHashMap.get("FellOver").equals("Y")) {
            hangSwitch.setChecked(false);
            climbHashMap.put("Hang", "N");
            climbHashMap.put("Barge", "N");
            bargeButtonsEnabledState(false);
            bargeZoneEnabledState(false);
        } else if (setupHashMap.get("FellOver").equals("N")) {
            bargeZoneEnabledState(true);
            if (climbHashMap.get("Hang").equals("N")) {
                climbHashMap.put("Barge", "N");
                hangSwitch.setChecked(false);
                bargeTabsEnabledState(false);
            } else if (climbHashMap.get("Hang").equals("Y")) {
                // If robot is onstage, they cannot be parked
                parkSwitch.setChecked(false);
                parkSwitch.setEnabled(false);
                parkID.setEnabled(false);
                climbHashMap.put("Park", "N");

                hangSwitch.setChecked(true);
                bargeTabsEnabledState(true);
            }
            if (climbHashMap.get("Park").equals("Y")) {
                hangSwitch.setChecked(false);
                hangSwitch.setEnabled(false);
                hangID.setEnabled(false);
                bargeTabsEnabledState(false);
                climbHashMap.put("Onstage", "N");
            }
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
                climbHashMap = HashMapManager.getClimbHashMap();
                updateXMLObjects();
                //set all objects in the fragment to their values from the HashMaps
            } else {
                HashMapManager.putSetupHashMap(setupHashMap);
                HashMapManager.putClimbHashMap(climbHashMap);
            }
        }
    }
}
