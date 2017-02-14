package thin.blog.survey;

import java.io.Serializable;

/**
 * Created by jmprathab on 16/11/15.
 */
public class SurveyData implements Serializable {
    int surveyId;
    int organizationId;
    String organizationName;
    String title;
    String instructions;
    boolean alreadyTaken;

    public SurveyData(int surveyId, int organizationId, String organizationName, String title, String instructions, boolean alreadyTaken) {
        this.surveyId = surveyId;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.title = title;
        this.instructions = instructions;
        this.alreadyTaken = alreadyTaken;
    }

    public int getSurveyId() {
        return surveyId;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getTitle() {
        return title;
    }


    public String getInstructions() {
        return instructions;
    }


    public boolean isAlreadyTaken() {
        return alreadyTaken;
    }

}
