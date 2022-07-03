package com.assessment.Data;

public class Registrations {

    int studentID, classID, term;

    public Registrations() {
    }

    public Registrations(int studentID, int classID, int term) {
        this.studentID = studentID;
        this.classID = classID;
        this.term = term;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public int getClassID() {
        return classID;
    }

    public void setClassID(int classID) {
        this.classID = classID;
    }

    public int getTerm() {
        return term;
    }

    public void setTerm(int term) {
        this.term = term;
    }
}
