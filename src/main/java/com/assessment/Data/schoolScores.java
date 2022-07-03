package com.assessment.Data;

public class schoolScores {


    private int studentId, subjectId, term, score;

    public schoolScores() {
    }

    public schoolScores(int studentId, int subjectId, int term, int score) {
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.term = term;
        this.score = score;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(int subjectId) {
        this.subjectId = subjectId;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
