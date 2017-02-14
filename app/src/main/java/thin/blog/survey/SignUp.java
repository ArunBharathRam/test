package thin.blog.survey;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import network.CustomRequest;
import network.VolleySingleton;

public class SignUp extends AppCompatActivity {
    Toolbar toolbar;
    private ProgressDialog progressDialog;
    private int serverSuccess;
    private String serverMessage;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private EditText name, email, password;
    private Button createAccount;
    private String userInputName, userInputEmail, userInputPassword;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setToolbar();
        initialize();
        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInputName = name.getText().toString();
                userInputEmail = email.getText().toString();
                userInputPassword = password.getText().toString();
                serverSuccess = 0;
                serverMessage = "Cannot contact server\nCheck your Internet Connection and Try again";
                if (userInputEmail.contentEquals("") || userInputPassword.contentEquals("") || userInputName.contentEquals("")) {
                    createToast("Fill in all the fields");
                } else {
                    RequestQueue requestQueue = VolleySingleton.getInstance().getRequestQueue();
                    Map<String, String> formData = new HashMap<>();
                    formData.put("name", userInputName);
                    formData.put("email", userInputEmail);
                    formData.put("password", userInputPassword);

                    final CustomRequest request = new CustomRequest(Request.Method.POST, ApplicationHelper.SIGNUP, formData, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            jsonParser(response);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            finalDecision();
                        }
                    });
                    request.setTag(ApplicationHelper.SIGNUP);
                    progressDialog = new ProgressDialog(SignUp.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("Creating Account");
                    progressDialog.setMessage("Please wait...\nContacting Server");
                    progressDialog.show();
                    //request.setRetryPolicy(new DefaultRetryPolicy(1000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(request);
                }
            }
        });
    }

    private void jsonParser(JSONObject response) {
        try {
            serverSuccess = response.getInt("success");
            serverMessage = response.getString("message");
            finalDecision();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void finalDecision() {
        if (serverSuccess == 1) {
            editor.putString(ApplicationHelper.USER_DATA_EMAIL, userInputEmail);
            editor.putString(ApplicationHelper.USER_DATA_PASSWORD, userInputPassword);
            editor.putString(ApplicationHelper.USER_DATA_NAME, userInputName);
            editor.putBoolean(ApplicationHelper.SUCCESSFUL_REGISTRATION_HISTORY, true);
            editor.apply();
            progressDialog.dismiss();
            builder.setCancelable(false);
            builder.setTitle("Successfully Registered");
            builder.setMessage(serverMessage);
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        } else {
            progressDialog.dismiss();
            builder.setCancelable(false);
            builder.setTitle("Cannot Register");
            builder.setMessage(serverMessage);
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        }

    }

    private void initialize() {
        name = (EditText) findViewById(R.id.name);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        createAccount = (Button) findViewById(R.id.create_account);
        sharedPreferences = getSharedPreferences(ApplicationHelper.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        userInputPassword = userInputName = userInputEmail = "";
        serverSuccess = 0;
        serverMessage = "Cannot contact server\nCheck your Internet Connection and Try again";
        builder = new AlertDialog.Builder(SignUp.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_signup, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                return true;
            case R.id.about:
                startActivity(new Intent(SignUp.this, About.class));
                return true;
            case android.R.id.home:
                startActivity(new Intent(SignUp.this, Login.class));
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SignUp.this, Login.class));
        finish();
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setSubtitle("Enter Details to create an new Account");
    }

    @Override
    protected void onPause() {
        super.onPause();
        name.setText("");
        email.setText("");
        password.setText("");
    }

    private void createToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast, (ViewGroup) findViewById(R.id.toast_layout_root));
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(message);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
