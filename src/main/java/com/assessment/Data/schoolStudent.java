package com.assessment.Data;

public class schoolStudent {

    private int studentId;

    public schoolStudent() {
    }

    public schoolStudent(int studentId, String studentName, int studentClass) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.studentClass = studentClass;
    }

    private String studentName;

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public int getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(int studentClass) {
        this.studentClass = studentClass;
    }

    private int studentClass;
}
