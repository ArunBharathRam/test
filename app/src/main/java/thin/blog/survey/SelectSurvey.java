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

public class SelectSurvey extends AppCompatActivity {
    Toolbar toolbar;
    SurveyData surveyData;
    SharedPreferences.Editor editor;
    private int userId;
    private SharedPreferences sharedPreferences;
    private EditText surveyId;
    private Button selectSurvey;
    private String userInputSurveyId;
    private int serverSuccess;
    private String serverMessage;
    private AlertDialog.Builder builder;
    private ProgressDialog progressDialog;
    private AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_survey);
        setToolbar();
        initialize();
        selectSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInputSurveyId = surveyId.getText().toString();
                serverSuccess = 0;
                serverMessage = "Cannot contact server\nCheck your Internet Connection and Try again";
                if (userInputSurveyId.contentEquals("")) {
                    createToast("Enter a survey ID");
                } else {
                    RequestQueue requestQueue = VolleySingleton.getInstance().getRequestQueue();
                    Map<String, String> formData = new HashMap<>();
                    formData.put("user_id", String.valueOf(userId));
                    formData.put("survey_id", userInputSurveyId);

                    final CustomRequest request = new CustomRequest(Request.Method.POST, ApplicationHelper.SELECT_SURVEY, formData, new Response.Listener<JSONObject>() {
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
                    request.setTag(ApplicationHelper.SELECT_SURVEY);
                    progressDialog = new ProgressDialog(SelectSurvey.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("Contacting Server");
                    progressDialog.setMessage("Please wait...\nFetching Information about survey");
                    progressDialog.show();
                    //request.setRetryPolicy(new DefaultRetryPolicy(1000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(request);
                }
            }
        });

    }

    private void initialize() {
        surveyId = (EditText) findViewById(R.id.survey_id);
        selectSurvey = (Button) findViewById(R.id.show_survey);
        userInputSurveyId = "";
        serverSuccess = 0;
        serverMessage = "Cannot contact server\nCheck your Internet Connection and Try again";
        builder = new AlertDialog.Builder(SelectSurvey.this);
        sharedPreferences = getSharedPreferences(ApplicationHelper.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        userId = sharedPreferences.getInt(ApplicationHelper.USER_DATA_USER_ID, 1);
    }


    private void jsonParser(JSONObject response) {
        try {
            serverSuccess = response.getInt("success");
            if (serverSuccess == 1) {

                int surveyId = Integer.parseInt(response.getString("survey_id"));
                int organizationId = Integer.parseInt(response.getString("organization_id"));
                String organizationName = response.getString("organization_name");
                String title = response.getString("title");
                String instructions = response.getString("instructions");
                Boolean alreadyTaken = Boolean.parseBoolean(response.getString("already_taken"));
                surveyData = new SurveyData(surveyId, organizationId, organizationName, title, instructions, alreadyTaken);
            } else {
                serverMessage = response.getString("message");
            }
            finalDecision();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void finalDecision() {
        if (serverSuccess == 1) {
            progressDialog.dismiss();
            Intent intent = new Intent(SelectSurvey.this, SurveyInformation.class);
            intent.putExtra("SURVEY_DATA", surveyData);
            startActivity(intent);
            finish();
        } else if (serverSuccess == 3) {
            progressDialog.dismiss();
            builder.setCancelable(false);
            builder.setTitle("User ID Incorrect");
            builder.setMessage(serverMessage);
            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editor.putBoolean(ApplicationHelper.SUCCESSFUL_LOGIN_HISTORY, false);
                    editor.apply();
                    dialog.cancel();
                    startActivity(new Intent(SelectSurvey.this, Login.class));
                    finish();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        } else {
            progressDialog.dismiss();
            builder.setCancelable(false);
            builder.setTitle("Information");
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


    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Select Survey");
        getSupportActionBar().setSubtitle("Enter Survey ID");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.help:
                return true;
            case R.id.about:
                startActivity(new Intent(SelectSurvey.this, About.class));
                return true;
            case R.id.sign_out:
                editor.putBoolean(ApplicationHelper.SUCCESSFUL_LOGIN_HISTORY, false);
                editor.commit();
                startActivity(new Intent(SelectSurvey.this, Login.class));
                finish();
                return true;
            case android.R.id.home:
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}
