package thin.blog.survey;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SurveyInformation extends AppCompatActivity {
    Toolbar toolbar;
    TextView title, instructions, organizationName, message;
    Button takeSurvey;
    Intent intent;
    SurveyData surveyData;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey_information);
        setToolbar();
        initialize();
        takeSurvey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent startIntent = new Intent(SurveyInformation.this, Survey.class);
                startIntent.putExtra("SURVEY_ID", surveyData.getSurveyId());
                startActivity(startIntent);
            }
        });
    }

    private void initialize() {
        title = (TextView) findViewById(R.id.survey_title);
        organizationName = (TextView) findViewById(R.id.survey_organization_name);
        instructions = (TextView) findViewById(R.id.survey_instructions);
        message = (TextView) findViewById(R.id.survey_already_taken);
        takeSurvey = (Button) findViewById(R.id.take_survey);
        intent = getIntent();
        surveyData = (SurveyData) intent.getSerializableExtra("SURVEY_DATA");
        title.setText(surveyData.getTitle());
        organizationName.setText("by " + surveyData.getOrganizationName());
        instructions.setText("\t" + surveyData.getInstructions());
        if (surveyData.isAlreadyTaken()) {
            takeSurvey.setVisibility(View.INVISIBLE);
            message.setVisibility(View.VISIBLE);
        }
        sharedPreferences = getSharedPreferences(ApplicationHelper.SHARED_PREFS_USER_DATA, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
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
                startActivity(new Intent(SurveyInformation.this, About.class));
                return true;
            case android.R.id.home:
                startActivity(new Intent(SurveyInformation.this, SelectSurvey.class));
                finish();
            case R.id.sign_out:
                editor.putBoolean(ApplicationHelper.SUCCESSFUL_LOGIN_HISTORY, false);
                editor.commit();
                startActivity(new Intent(SurveyInformation.this, Login.class));
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SurveyInformation.this, SelectSurvey.class));
        finish();
    }

    private void setToolbar() {
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Survey Information");
    }
}
