package thin.blog.survey;

import java.util.ArrayList;

/**
 * Created by jmprathab on 17/11/15.
 */
public class Question {
    int id;
    String question;
    ArrayList<Option> option;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public ArrayList<Option> getOption() {
        return option;
    }

    public void setOption(ArrayList<Option> option) {
        this.option = option;
    }
}
