package com.jarvis.ligarpcvoz;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VoiceService extends Service {

    private static final String CHANNEL_ID = "jarvis_voice";
    private static final int NOTIFICATION_ID = 2001;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private SpeechRecognizer recognizer;
    private Intent recognizerIntent;
    private boolean stopping = false;
    private boolean listening = false;

    @Override
    public void onCreate() {
        super.onCreate();

        createChannel();
        startForeground(
                NOTIFICATION_ID,
                buildNotification("Ouvindo: ligar o PC")
        );

        setupRecognizer();
        startListeningDelayed(500);
    }

    private void setupRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            updateNotification("Reconhecimento de voz indisponível");
            return;
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this);

        recognizerIntent = new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "pt-BR"
        );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                true
        );

        recognizerIntent.putExtra(
                RecognizerIntent.EXTRA_MAX_RESULTS,
                5
        );

        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(android.os.Bundle params) {
                listening = true;
                updateNotification("Ouvindo...");
            }

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {
                listening = false;
                updateNotification("Processando...");
            }

            @Override
            public void onError(int error) {
                listening = false;
                if (!stopping) {
                    startListeningDelayed(error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY ? 1200 : 500);
                }
            }

            @Override
            public void onResults(android.os.Bundle results) {
                listening = false;
                handleResults(results);
                if (!stopping) {
                    startListeningDelayed(350);
                }
            }

            @Override
            public void onPartialResults(android.os.Bundle partialResults) {
                ArrayList<String> texts =
                        partialResults.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                        );

                if (texts != null && containsTrigger(texts)) {
                    wakePc();
                    try {
                        recognizer.cancel();
                    } catch (Exception ignored) {}
                }
            }

            @Override
            public void onEvent(int eventType, android.os.Bundle params) {}
        });
    }

    private void handleResults(android.os.Bundle results) {
        ArrayList<String> texts =
                results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION
                );

        if (texts != null && containsTrigger(texts)) {
            wakePc();
        } else {
            updateNotification("Ouvindo: ligar o PC");
        }
    }

    private boolean containsTrigger(ArrayList<String> texts) {
        for (String raw : texts) {
            String text = normalize(raw);

            if (
                    text.contains("ligar o pc") ||
                    text.equals("ligar pc") ||
                    text.contains("ligar computador") ||
                    text.contains("ligar o computador")
            ) {
                return true;
            }
        }

        return false;
    }

    private String normalize(String input) {
        String text = Normalizer.normalize(
                input.toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD
        );

        return text.replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private void wakePc() {
        updateNotification("Comando detectado — ligando PC");

        executor.execute(() -> {
            try {
                // Envia três pacotes para aumentar a confiabilidade.
                for (int i = 0; i < 3; i++) {
                    WakeOnLan.send();
                    Thread.sleep(150);
                }

                handler.postDelayed(
                        () -> updateNotification("Ouvindo: ligar o PC"),
                        1500
                );

            } catch (Exception e) {
                handler.post(
                        () -> updateNotification("Erro ao enviar Wake-on-LAN")
                );
            }
        });
    }

    private void startListeningDelayed(long delayMs) {
        handler.postDelayed(() -> {
            if (stopping || recognizer == null || listening) {
                return;
            }

            try {
                recognizer.startListening(recognizerIntent);
            } catch (Exception e) {
                startListeningDelayed(1000);
            }
        }, delayMs);
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Jarvis - Ligar PC",
                    NotificationManager.IMPORTANCE_LOW
            );

            channel.setDescription(
                    "Mantém a escuta por voz ativa."
            );

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification(String text) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_btn_speak_now)
                .setContentTitle("Ligar PC Voz")
                .setContentText(text)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .build();
    }

    private void updateNotification(String text) {
        NotificationManager manager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        manager.notify(
                NOTIFICATION_ID,
                buildNotification(text)
        );
    }

    @Override
    public int onStartCommand(
            Intent intent,
            int flags,
            int startId
    ) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopping = true;

        handler.removeCallbacksAndMessages(null);

        if (recognizer != null) {
            try {
                recognizer.cancel();
            } catch (Exception ignored) {}

            recognizer.destroy();
            recognizer = null;
        }

        executor.shutdownNow();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
