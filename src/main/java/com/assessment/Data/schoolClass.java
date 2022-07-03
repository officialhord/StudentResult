package com.assessment.Data;

import java.util.List;

public class schoolClass {

    private int classID;
    private List<Integer> subjectIDs;

    public schoolClass(int classID, List<Integer> subjectIDs, String className) {
        this.classID = classID;
        this.subjectIDs = subjectIDs;
        this.className = className;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    private String className;


    public int getClassID() {
        return classID;
    }

    public void setClassID(int classID) {
        this.classID = classID;
    }

    public List<Integer> getSubjectIDs() {
        return subjectIDs;
    }

    public void setSubjectIDs(List<Integer> subjectIDs) {
        this.subjectIDs = subjectIDs;
    }
}
