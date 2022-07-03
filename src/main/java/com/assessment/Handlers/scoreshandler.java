package com.assessment.Handlers;

public class scoreshandler {

    private String subjectname;
    private int score;


    public String getSubjectname() {
        return subjectname;
    }

    public scoreshandler() {
    }

    public scoreshandler(String subjectname, int score) {
        this.subjectname = subjectname;
        this.score = score;
    }

    public void setSubjectname(String subjectname) {
        this.subjectname = subjectname;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
