package com.example.diploma;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.diploma.model.Task;
import com.example.diploma.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LogIn extends AppCompatActivity {

    EditText login = null;
    EditText password = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        login = findViewById(R.id.editTextLogin);
        password = findViewById(R.id.editTextPassword);
    }

    public void onLogin(View view) {
        new GetDataFromURL().execute("http://192.168.3.7:8080/antiprocrastinate-api/v1/users/user?login=" + login.getText().toString() + "&password=" + password.getText().toString());
    }

    private class GetDataFromURL extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuilder buffer = new StringBuilder();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                return buffer.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject object = new JSONObject(result);
                User currentUser = Utils.parseUserJsonObject(object);

                FileOutputStream fos = null;
                try {
                    String text = currentUser.getId() + ":" + currentUser.getLogin() + ":" + currentUser.getPassword();
                    fos = openFileOutput("profile.txt", MODE_PRIVATE);
                    fos.write(text.getBytes());
                }
                catch(IOException ignored) {}
                finally{
                    try{
                        if(fos!=null)
                            fos.close();
                    }
                    catch(IOException ignored){}
                }

                Intent intent = new Intent(LogIn.this, MainActivity.class);
                intent.putExtra("UserId", currentUser.getId());
                startActivity(intent);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}