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
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.CustomRequest;
import network.VolleySingleton;

public class Survey extends AppCompatActivity {
    int surveyId, userId;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    JSONArray data;
    int numberOfQuestionsSet = 0;
    ArrayList<Answer> userAnswers = new ArrayList<>();
    private List<Question> questions = new ArrayList<>();
    private LinearLayout linearLayout;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private TextView question;
    private RadioGroup radioGroup;
    private RadioButton rb1, rb2, rb3, rb4;
    private Button next;
    private int serverSuccess;
    private String serverMessage;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        setToolbar();
        initialize();
        RequestQueue requestQueue = VolleySingleton.getInstance().getRequestQueue();
        Map<String, String> formData = new HashMap<>();
        formData.put("survey_id", String.valueOf(surveyId));
        formData.put("user_id", String.valueOf(userId));

        final CustomRequest request = new CustomRequest(Request.Method.POST, ApplicationHelper.FETCH_QUESTIONS, formData, new Response.Listener<JSONObject>() {
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
        progressDialog = new ProgressDialog(Survey.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Fetching Questions");
        progressDialog.setMessage("Please wait...\nContacting Server");
        progressDialog.show();
        //request.setRetryPolicy(new DefaultRetryPolicy(1000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }

    private void jsonParser(JSONObject response) {
        try {
            serverSuccess = response.getInt("success");
            if (serverSuccess == 1) {
                data = response.getJSONArray("data");
                collectData();

            } else {
                serverMessage = response.getString("message");
                finalDecision();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void collectData() {
        for (int i = 0; i < data.length(); i++) {
            try {
                JSONObject questionObject = data.getJSONObject(i);
                Question currentQuestion = new Question();
                currentQuestion.setId(Integer.parseInt(questionObject.getString("id")));
                currentQuestion.setQuestion(questionObject.getString("question"));
                JSONArray optionArray = questionObject.getJSONArray("options");
                ArrayList<Option> currentOption = new ArrayList<>();
                for (int j = 0; j < optionArray.length(); j++) {
                    JSONObject optionObject = optionArray.getJSONObject(j);
                    Option option = new Option();
                    option.setId(Integer.parseInt(optionObject.getString("id")));
                    option.setOption(optionObject.getString("option"));
                    currentOption.add(option);
                }
                currentQuestion.setOption(currentOption);
                questions.add(currentQuestion);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        setQuestions();
        progressDialog.dismiss();
        linearLayout.setVisibility(View.VISIBLE);
    }

    private boolean setQuestions() {
        radioGroup.clearCheck();
        if (numberOfQuestionsSet < questions.size()) {
            if (numberOfQuestionsSet == 3) {
                next.setText("Submit");
            }
            Question current = questions.get(numberOfQuestionsSet);
            question.setText(current.getQuestion());
            ArrayList<Option> options = current.getOption();
            rb1.setText(options.get(0).getOption());
            rb2.setText(options.get(1).getOption());
            rb3.setText(options.get(2).getOption());
            rb4.setText(options.get(3).getOption());
            numberOfQuestionsSet += 1;
            return true;
        } else {
            return false;
        }
    }


    private void finalDecision() {
        if (serverSuccess == 3) {
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
                    startActivity(new Intent(Survey.this, Login.class));
                    finish();
                }
            });
            alertDialog = builder.create();
            alertDialog.show();
        } else if (serverSuccess == 10) {
            createToast("Successfully submitted Answers");
            finish();
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

    private void initialize() {
        linearLayout = (LinearLayout) findViewById(R.id.survey_page);
        linearLayout.setVisibility(View.INVISIBLE);
        question = (TextView) findViewById(R.id.question);
        radioGroup = (RadioGroup) findViewById(R.id.rg);
        rb1 = (RadioButton) findViewById(R.id.option1);
        rb2 = (RadioButton) findViewById(R.id.option2);
        rb3 = (RadioButton) findViewById(R.id.option3);
        rb4 = (RadioButton) findViewById(R.id.option4);
        next = (Button) findViewById(R.id.next);

        sharedPreferences = getSharedPreferences(ApplicationHelper.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        surveyId = getIntent().getIntExtra("SURVEY_ID", 2);
        userId = sharedPreferences.getInt(ApplicationHelper.USER_DATA_USER_ID, 1);

        serverSuccess = 0;
        serverMessage = "Cannot contact server\nCheck your Internet Connection and Try again";
        builder = new AlertDialog.Builder(Survey.this);

        data = new JSONArray();
    }


    public void accumulateAnswers(View v) {
        Question currentQuestion = questions.get(numberOfQuestionsSet - 1);
        Answer currentAnswer = new Answer();
        currentAnswer.setQuestionId(currentQuestion.getId());
        ArrayList<Option> currentOptions = currentQuestion.getOption();
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.option1:
                currentAnswer.setAnswerId(currentOptions.get(0).getId());
                break;
            case R.id.option2:
                currentAnswer.setAnswerId(currentOptions.get(1).getId());
                break;
            case R.id.option3:
                currentAnswer.setAnswerId(currentOptions.get(2).getId());
                break;
            case R.id.option4:
                currentAnswer.setAnswerId(currentOptions.get(3).getId());
                break;
            default:
                currentAnswer.setAnswerId(0);
                break;
        }
        userAnswers.add(currentAnswer);
        if (numberOfQuestionsSet < questions.size()) {
            setQuestions();
        } else {
            submitAnswers();
        }
    }

    private void submitAnswers() {
        RequestQueue requestQueue = VolleySingleton.getInstance().getRequestQueue();
        Map<String, String> formData = new HashMap<>();
        formData.put("survey_id", String.valueOf(surveyId));
        formData.put("user_id", String.valueOf(userId));

        formData.put("question1", String.valueOf(questions.get(0).getId()));
        formData.put("answer1", String.valueOf(userAnswers.get(0).getAnswerId()));

        formData.put("question2", String.valueOf(questions.get(1).getId()));
        formData.put("answer2", String.valueOf(userAnswers.get(1).getAnswerId()));

        formData.put("question3", String.valueOf(questions.get(2).getId()));
        formData.put("answer3", String.valueOf(userAnswers.get(2).getAnswerId()));

        formData.put("question4", String.valueOf(questions.get(3).getId()));
        formData.put("answer4", String.valueOf(userAnswers.get(3).getAnswerId()));

        final CustomRequest request = new CustomRequest(Request.Method.POST, ApplicationHelper.SUBMIT_ANSWERS, formData, new Response.Listener<JSONObject>() {
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
        progressDialog = new ProgressDialog(Survey.this);
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Submitting Answers");
        progressDialog.setMessage("Please wait...\nContacting Server");
        progressDialog.show();
        //request.setRetryPolicy(new DefaultRetryPolicy(1000, 3, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
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

    public void clearChecked(View v) {
        radioGroup.clearCheck();
    }


    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Survey");
    }
}
