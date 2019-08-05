package app.sample.aes256cbc;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import app.sample.aes256cbc.helper.Helper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    TextView txt_result_crypted, txt_result;
    Button btnGetData, btnSendData;
    EditText inputTxt;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt_result_crypted = findViewById(R.id.result_crypted);
        txt_result = findViewById(R.id.result);
        btnGetData = findViewById(R.id.btngetdata);
        btnSendData = findViewById(R.id.btnsenddata);
        inputTxt = findViewById(R.id.inputTxt);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(true);


        btnGetData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new StartGetData("ok", "http://domain:port/getdata").execute();
            }
        });

        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = inputTxt.getText().toString();
                if (input.equalsIgnoreCase("")) {
                    Toast.makeText(MainActivity.this, "input null", Toast.LENGTH_SHORT).show();

                } else {
                    new StartGetData(input,"http://domain:port/senddata").execute();
                }


            }
        });

    }

    public class StartGetData extends AsyncTask<URL, Integer, String> {
        String code;
        String inputdata;
        String url;
        JSONObject json = null;
        JSONObject jsonfinal = null;
        String res;

        public StartGetData(String inputdata, String url) {
            this.inputdata = inputdata;
            this.url = url;
        }


        protected void onPreExecute() {
            pDialog.setMessage("loading... ");
            showDialog();
            json = new JSONObject();
            jsonfinal = new JSONObject();
            try {
                //your data sent to server
                json.put("inputdata", inputdata);
//                json.put("inputdata1", inputdata);
//                json.put("inputdata2", inputdata);
//                json.put("inputdata3", inputdata);
//                json.put("inputdata4", inputdata);


                //encrypt payload
                Log.e("ORIGINAL", json.toString());
                String postBody = Helper.encrypt(json.toString());

                jsonfinal.put("data", postBody);
                Log.e("CRYPTED", jsonfinal.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        protected String doInBackground(URL... urls) {
            //get result and then decrypt
            res = getAsyncCall(jsonfinal.toString(), url);
            Log.e("RES ORIGINAL", res + "");
            code = Helper.decrypt(res);
            return code;
        }

        protected void onPostExecute(String result) {
            Log.e("RES DECRYPTED", result + "");
            if (result == null || !result.contains("data")) {
                Toast.makeText(MainActivity.this, "No result found, code 1", Toast.LENGTH_SHORT).show();
                hideDialog();

            } else {
                JSONObject jsonObj = null;
                JSONArray resultparam = null;
                try {
                    jsonObj = new JSONObject(result);

                    if (jsonObj.getString("status").equalsIgnoreCase("1")) {
                        String resultdata = jsonObj.getString("data");
                        txt_result.setText(result);
                        txt_result_crypted.setText(res);
                        hideDialog();
                    } else {
                        //error
                        Toast.makeText(MainActivity.this, jsonObj.getString("data"), Toast.LENGTH_SHORT).show();
                        hideDialog();
                    }
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, "error, code 2 " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    hideDialog();

                }


            }


        }

        protected void onDestroy() {

        }
    }

    public String getAsyncCall(String val, String url) {
        OkHttpClient httpClient = new OkHttpClient().newBuilder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");


        String postBody = val;

        RequestBody body = RequestBody.create(JSON, postBody);

        final Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();


        Response response = null;
        try {
            response = httpClient.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            Log.e("Error", e.getMessage() + "");
            return null;
        }


    }


    private void showDialog() {
        if (pDialog != null) {
            if (!pDialog.isShowing())
                pDialog.show();
        }

    }

    private void hideDialog() {
        if (pDialog != null) {
            if (pDialog.isShowing())
                pDialog.dismiss();
        }

    }
}
