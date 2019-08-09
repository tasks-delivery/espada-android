package espada.online.mu.espada;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeEspadaActivity extends BaseActivity {

    @BindView(R.id.btn_start)
    Button btn_start;

    @BindView(R.id.field_hero_name)
    EditText field_hero_name;

    Response response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_espada);
        ButterKnife.bind(this);
    }

    public ProgressDialog dialog;

    protected void onPreExecute() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Search of hero...");
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.show();
    }


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

    @OnClick(R.id.btn_start)
    public void clickStartButton()  {
       onPreExecute();
       new Thread(new Runnable(){
           @Override
           public void run() {
               try {
                   response = sendRequest(espada_api + "api/espada/hero?name=" + field_hero_name.getText());
                   openStatusActivity(field_hero_name.getText().toString());
               }
               catch (Exception ex) {
                   ex.printStackTrace();
               }
           }
       }).start();
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    private void openStatusActivity(String name){
        dialog.dismiss();
        if (response.isSuccessful()){
            Intent intent = new Intent(HomeEspadaActivity.this, StatusEspadaActivity.class);
            intent.putExtra("name", name);
            startActivity(intent);
        }else {
            System.out.println(response.code());
            if(response.code() == 404){
                String error = "Hero " + name + " not found";
                errorMessage(error, HomeEspadaActivity.this.getApplicationContext());
            }else {
                if (response.code() == 503){
                    String error = "Espada service lost connection. \n We will fix this problem in near time";
                    errorMessage(error, HomeEspadaActivity.this.getApplicationContext());
                }else {
                    String error = "Connection lost. \n Please check internet connection on device";
                    errorMessage(error, HomeEspadaActivity.this.getApplicationContext());
                }
            }
        }
    }
}