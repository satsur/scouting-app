package com.mercury1089.scoutingapp2025.qr;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mercury1089.scoutingapp2025.HashMapManager;
import com.mercury1089.scoutingapp2025.PregameActivity;
import com.mercury1089.scoutingapp2025.R;
import com.mercury1089.scoutingapp2025.utils.GenUtils;
import com.mercury1089.scoutingapp2025.utils.QRStringBuilder;

import java.util.LinkedHashMap;

public class QRRunnable implements Runnable {
    private final Activity context;
    private final Dialog loading_alert;
    LinkedHashMap<String, String> setupHashMap = HashMapManager.getSetupHashMap();
    private String scouter, teamNum, matchNum, qrString;
    private boolean needsToBeStored;
    public QRRunnable(Activity ctx, Dialog loading_alert) {
        this.context = ctx;
        this.loading_alert = loading_alert;
        // Make the actual qr string and stores match info
        QRStringBuilder.buildQRString();
        this.qrString = QRStringBuilder.getQRString();
        this.scouter = QRStringBuilder.getScouterName();
        this.teamNum = QRStringBuilder.getTeamNumber();
        this.matchNum = QRStringBuilder.getMatchNumber();
        needsToBeStored = true;
    }
    public QRRunnable(String qrString, Activity ctx, Dialog loading_alert) {
        this.context = ctx;
        this.loading_alert = loading_alert;
        // Uses the given qrString to find match info
        this.qrString = qrString;
        String[] data = qrString.split(QRStringBuilder.DELIMITER);
        this.scouter = data[QRStringBuilder.SCOUTER_NAME_INDEX];
        this.teamNum = data[QRStringBuilder.TEAM_NUM_INDEX];
        this.matchNum = data[QRStringBuilder.MATCH_NUM_INDEX];
        needsToBeStored = false;
    }

    @Override
    public void run() {
        // Once QR is generated, hashmap values go back to defaults
        HashMapManager.setDefaultValues(HashMapManager.HASH.AUTON);
        HashMapManager.setDefaultValues(HashMapManager.HASH.TELEOP);
        HashMapManager.setDefaultValues(HashMapManager.HASH.CLIMB);

        try {
            Bitmap bitmap = QRUtils.textToImageEncode(qrString);
            context.runOnUiThread(() -> {
                if (needsToBeStored) {
                    HashMapManager.putSetupHashMap(setupHashMap);

                    // Show the QR and store it in the cache
                    Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.popup_qr);
                    // If this runnable is called from Pregame or post match activity, the string should be stored in the cahce
                    // If this is called from the list adapter, the string doesn't need to eb stored again
                    if (needsToBeStored) QRStringBuilder.storeQRString(context);

                    ImageView imageView = dialog.findViewById(R.id.imageView);
                    TextView scouterName = dialog.findViewById(R.id.ScouterNameQR);
                    TextView teamNumber = dialog.findViewById(R.id.TeamNumberQR);
                    TextView matchNumber = dialog.findViewById(R.id.MatchNumberQR);
                    Button goBackToMain = dialog.findViewById(R.id.GoBackButton);
                    imageView.setImageBitmap(bitmap);

                    dialog.setCancelable(false);

                    scouterName.setText(this.scouter);
                    teamNumber.setText(this.teamNum);
                    matchNumber.setText(this.matchNum);

                    if (loading_alert != null && loading_alert.isShowing()) {
                        loading_alert.dismiss();
                    }
                    dialog.show();

                    goBackToMain.setOnClickListener(v -> {
                        Dialog confirmDialog = new Dialog(context);
                        confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        confirmDialog.setContentView(R.layout.setup_next_match_confirm_popup);

                        Button setupNextMatchButton = confirmDialog.findViewById(R.id.SetupNextMatchButton);
                        Button cancelConfirm = confirmDialog.findViewById(R.id.btn_cancel);

                        confirmDialog.show();

                        setupNextMatchButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                QRStringBuilder.clearQRString();
                                HashMapManager.setupNextMatch();
                                Intent intent = new Intent(context, PregameActivity.class);
                                dialog.dismiss();
                                context.startActivity(intent);
                                context.finish();
                                confirmDialog.dismiss();
                            }
                        });

                        cancelConfirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                confirmDialog.dismiss();
                            }
                        });
                    });
                } else {
                    Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.popup_qr_cached);

                    ImageView imageView = dialog.findViewById(R.id.imageView);
                    TextView scouterName = dialog.findViewById(R.id.ScouterNameQR);
                    TextView teamNumber = dialog.findViewById(R.id.TeamNumberQR);
                    TextView matchNumber = dialog.findViewById(R.id.MatchNumberQR);
                    Button closeButton = dialog.findViewById(R.id.CloseButton);
                    imageView.setImageBitmap(bitmap);

                    dialog.setCancelable(false);

                    scouterName.setText(scouter);
                    teamNumber.setText(GenUtils.padLeftZeros(teamNum, 2));
                    matchNumber.setText(GenUtils.padLeftZeros(matchNum, 2));

                    loading_alert.dismiss();

                    dialog.show();

                    closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                }
            });
        } catch (Exception e){
            Log.d("QRGen", "Something went wrong while generating a QR Code.");
        }
    }
}
