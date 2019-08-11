package espada.online.mu.espada;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.TextView;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import espada.online.mu.espada.service.NotificationService;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StatusEspadaActivity extends BaseActivity {

    @BindView(R.id.hero_status)
    TextView hero_status;

    @BindView(R.id.hero_status_text)
    TextView hero_status_text;

    private boolean trigger;

    private String name;

    public Response sendRequest(String url){
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_espada);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        updateTextView("Live");
        trigger = true;
        statusHendler();
    }

    private void statusHendler(){
        Handler handler = new Handler();
        int delay = 60000;

        handler.postDelayed(new Runnable(){
            public void run(){

                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        try {
                            Response response = sendRequest(espada_api+"api/espada/hero/hendler?name=" + name);
                            String body = response.body().string();
                            System.out.println(body);
                            if(!response.isSuccessful()){
                                if(response.code() == 404){
                                    String error = "Hero " + name + " not found";
                                    errorMessage(error, StatusEspadaActivity.this.getApplicationContext());
                                    closeStatusActivity();
                                }else {
                                    if (response.code() == 503 | response.code() == 500){
                                        String error = "Espada service lost connection. \n We will fix this problem in near time";
                                        errorMessage(error, StatusEspadaActivity.this.getApplicationContext());
                                        closeStatusActivity();
                                    }else {
                                        String error = "Connection lost. \n Please check internet connection on device";
                                        errorMessage(error, StatusEspadaActivity.this.getApplicationContext());
                                        closeStatusActivity();
                                    }
                                }
                            }
                            trigger = body.equals("true");
                        }
                        catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }).start();

                if(!trigger){
                    updateTextView("Death");
                    setNotification();
                }else {
                    updateTextView("Live");
                }

                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void setNotification(){
        NotificationService notificationService = new NotificationService();
        Uri notification = notificationService.runNotificationHendler(StatusEspadaActivity.this.getApplicationContext());
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(StatusEspadaActivity.this.getApplicationContext(), "test")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Hero information")
                        .setContentText("Your hero was killed")
                        .setSound(notification, AudioManager.STREAM_NOTIFICATION)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("test",
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            AudioAttributes att = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            channel.setSound(notification, att);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }
    }

    private void closeStatusActivity(){
        finish();
        System.exit(0);
    }

    private void updateTextView(final String status) {
        StatusEspadaActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hero_status.setText(status);
            }
        });
    }

    @Override
    public void onStart(){
        super.onStart();
    }
}