package thin.blog.survey;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
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

public class Login extends AppCompatActivity {
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;
    private int userDataUserId;
    private String userInputEmail, userInputPassword;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private EditText email, password;
    private Button login;
    private String serverMessage;
    private int serverSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(ApplicationHelper.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean(ApplicationHelper.SUCCESSFUL_LOGIN_HISTORY, false)) {
            // if (true) {
            startActivity(new Intent(Login.this, SelectSurvey.class));
            finish();
        }
        setContentView(R.layout.activity_login);
        initialize();//XML References
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInputEmail = email.getText().toString();
                userInputPassword = password.getText().toString();

                if (userInputEmail.contentEquals("") || userInputPassword.contentEquals("")) {
                    createToast("Fill in all the fields");
                } else {
                    final RequestQueue requestQueue = VolleySingleton.getInstance().getRequestQueue();
                    Map<String, String> formData = new HashMap<>();
                    formData.put("email", userInputEmail);
                    formData.put("password", userInputPassword);

                    final CustomRequest request = new CustomRequest(Request.Method.POST, ApplicationHelper.LOGIN, formData, new Response.Listener<JSONObject>() {
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
                    request.setTag(ApplicationHelper.LOGIN);
                    //request.setRetryPolicy(new DefaultRetryPolicy(1000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(request);
                    progressDialog = new ProgressDialog(Login.this);
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(true);
                    progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            request.cancel();
                        }
                    });
                    progressDialog.setTitle("Logging In");
                    progressDialog.setMessage("Please wait...\nContacting Server");
                    progressDialog.show();
                }
            }
        });
    }

    private void jsonParser(JSONObject response) {
        try {
            serverSuccess = response.getInt("success");
            serverMessage = response.getString("message");
            if (serverSuccess == 1) {
                userDataUserId = Integer.parseInt(response.getString("user_id"));
            }
            finalDecision();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void finalDecision() {
        if (serverSuccess == 1) {
            editor.putInt(ApplicationHelper.USER_DATA_USER_ID, userDataUserId);
            editor.putString(ApplicationHelper.USER_DATA_EMAIL, userInputEmail);
            editor.putString(ApplicationHelper.USER_DATA_PASSWORD, userInputPassword);
            editor.putBoolean(ApplicationHelper.SUCCESSFUL_LOGIN_HISTORY, true);
            editor.apply();
            progressDialog.dismiss();
            startActivity(new Intent(Login.this, SelectSurvey.class));
            finish();
        } else {
            progressDialog.dismiss();
            builder.setTitle("Cannot Login");
            builder.setMessage(serverMessage);
            builder.setCancelable(false);
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

    public void createAccount(View v) {
        startActivity(new Intent(Login.this, SignUp.class));
    }


    public void forgotPassword(View v) {
        builder.setTitle("Reset Password");
        builder.setMessage("Visit \nwww.survey.com/forgot.php\n to reset password");
        builder.setCancelable(false);
        builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }


    private void initialize() {
        email = (EditText) findViewById(R.id.lemail);
        password = (EditText) findViewById(R.id.lpassword);
        login = (Button) findViewById(R.id.login);
        editor = sharedPreferences.edit();
        userDataUserId = 1;
        userInputEmail = userInputPassword = "";
        serverSuccess = 0;
        serverMessage = "Cannot contact server\nCheck your Internet Connection and Try again";
        builder = new AlertDialog.Builder(Login.this);
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

    @Override
    protected void onPause() {
        super.onPause();
        email.setText("");
        password.setText("");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
