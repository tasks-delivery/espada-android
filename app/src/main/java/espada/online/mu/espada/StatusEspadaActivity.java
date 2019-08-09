package espada.online.mu.espada;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    }

    @Override
    public void onResume(){
        super.onResume();
        statusHendler();
    }

    @Override
    public void onPause(){
        super.onPause();
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
                                    if (response.code() == 503){
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
                }else {
                    updateTextView("Live");
                }

                handler.postDelayed(this, delay);
            }
        }, delay);
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