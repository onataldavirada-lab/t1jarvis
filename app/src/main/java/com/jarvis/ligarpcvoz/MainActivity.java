package com.jarvis.ligarpcvoz;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {

    private static final int REQ_PERMISSIONS = 1001;
    private TextView status;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor = Executors.newSingleThreadExecutor();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 60, 40, 40);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("Ligar PC por Voz");
        title.setTextSize(28);
        title.setGravity(Gravity.CENTER);

        TextView phrases = new TextView(this);
        phrases.setText(
                "\nFrases aceitas:\n" +
                "• ligar o PC\n" +
                "• ligar PC\n" +
                "• ligar computador\n" +
                "• ligar o computador\n"
        );
        phrases.setTextSize(18);

        status = new TextView(this);
        status.setText("\nServiço parado");
        status.setTextSize(18);

        Button start = new Button(this);
        start.setText("INICIAR ESCUTA");

        Button stop = new Button(this);
        stop.setText("PARAR ESCUTA");

        Button test = new Button(this);
        test.setText("TESTAR LIGAR PC");

        layout.addView(title);
        layout.addView(phrases);
        layout.addView(status);
        layout.addView(start);
        layout.addView(stop);
        layout.addView(test);

        setContentView(layout);

        start.setOnClickListener(v -> ensurePermissionsAndStart());

        stop.setOnClickListener(v -> {
            stopService(new Intent(this, VoiceService.class));
            status.setText("\nServiço parado");
        });

        test.setOnClickListener(v ->
                executor.execute(() -> {
                    try {
                        for (int i = 0; i < 3; i++) {
                            WakeOnLan.send();
                            Thread.sleep(150);
                        }

                        runOnUiThread(() ->
                                Toast.makeText(
                                        this,
                                        "Comando Wake-on-LAN enviado.",
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    } catch (Exception e) {
                        runOnUiThread(() ->
                                Toast.makeText(
                                        this,
                                        "Erro ao enviar Wake-on-LAN.",
                                        Toast.LENGTH_LONG
                                ).show()
                        );
                    }
                })
        );
    }

    private void ensurePermissionsAndStart() {
        List<String> missing = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED) {
            missing.add(Manifest.permission.RECORD_AUDIO);
        }

        if (Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            missing.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (!missing.isEmpty()) {
            requestPermissions(
                    missing.toArray(new String[0]),
                    REQ_PERMISSIONS
            );
            return;
        }

        startVoiceService();
    }

    private void startVoiceService() {
        Intent intent = new Intent(this, VoiceService.class);

        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }

        status.setText("\nServiço ativo — ouvindo");
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_PERMISSIONS) {
            boolean allGranted = grantResults.length > 0;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                startVoiceService();
            } else {
                Toast.makeText(
                        this,
                        "Permita microfone e notificações.",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (executor != null) executor.shutdownNow();
        super.onDestroy();
    }
}
