package com.assessment.Data;

public class schoolSubject {

    private String subjectName;

    public schoolSubject(String subjectName, int subjectID) {
        this.subjectName = subjectName;
        this.subjectID = subjectID;
    }

    public schoolSubject() {
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public int getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(int subjectID) {
        this.subjectID = subjectID;
    }

    private int subjectID;
}
