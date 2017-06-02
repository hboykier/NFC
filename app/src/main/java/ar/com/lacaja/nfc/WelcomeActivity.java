package ar.com.lacaja.nfc;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WelcomeActivity extends AppCompatActivity {

    private String name;
    private String lastName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sp=getSharedPreferences(MainActivity.PREFS_NAME, 0);
        name=sp.getString("name", "");
        lastName=sp.getString("lastName", "");

        EditText nombre=(EditText) findViewById(R.id.nombre);
        EditText apellido=(EditText) findViewById(R.id.apellido);
        nombre.setText(name);
        apellido.setText(lastName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    public void insert(View view) {
        Log.i("INS", "Ingresando al metodo");
        EditText nombre=(EditText) findViewById(R.id.nombre);
        EditText apellido=(EditText) findViewById(R.id.apellido);
        Spinner tramite=(Spinner) findViewById(R.id.tramite);
        Spinner producto=(Spinner) findViewById(R.id.producto);
        Log.i("INS", "Nombre=" + nombre.getText().toString());
        Log.i("INS", "Apellido=" + apellido.getText().toString());
        Log.i("INS", "Tramite=" + tramite.getSelectedItem().toString());
        Log.i("INS", "Producto=" + producto.getSelectedItem().toString());

        String server="hboykier.esy.es";
        String stringUrl = "http://"+server+"/insert.php?nombre="+nombre.getText()+"&apellido="+apellido.getText()+"&tramite="+tramite.getSelectedItem().toString()+"&producto="+producto.getSelectedItem().toString();
        Log.i("INS", "URL=" + stringUrl);

        SharedPreferences sp=getSharedPreferences(MainActivity.PREFS_NAME, 0);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString("name", nombre.getText().toString());
        editor.putString("lastName", apellido.getText().toString());
        editor.commit();

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(stringUrl);
            finish();
        } else {
            Context context = getApplicationContext();
            CharSequence text = "No hay red";
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            // params comes from the execute() call: params[0] is the url.
            try {
                downloadUrl(urls[0]);
                return "Se inserto el pedido";
            } catch (IOException e) {
                return "Problemas al insertar el pedido";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Context context = getApplicationContext();
            CharSequence text = result;
            int duration = Toast.LENGTH_LONG;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

        private String downloadUrl(String myurl) throws IOException {
            InputStream is = null;
            // Only display the first 500 characters of the retrieved
            // web page content.
            int len = 500;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.i("DOWN", "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readIt(is, len);
                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }

        private String readIt(InputStream stream, int len)
        {
            try {
                Reader reader = null;
                reader = new InputStreamReader(stream, "UTF-8");
                char[] buffer = new char[len];
                reader.read(buffer);
                return new String(buffer);
            }
            catch (Exception e)
            {
                Log.i("READIT", e.getMessage());
            }
            return "";
        }

    }

}
