package com.example.birdsofafeather.model.db;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "courses")
public class Course {
    @PrimaryKey
    @ColumnInfo(name = "id")
    public int courseId;

    @ColumnInfo(name = "student_id")
    public int studentId;

    @ColumnInfo(name="year")
    public int year;

    @ColumnInfo(name="quarter")
    public String quarter;

    @ColumnInfo(name="subject")
    public String subject;

    @ColumnInfo(name="course_num")
    public int courseNum;

    @ColumnInfo(name = "course_title")
    public String courseTitle;

    public Course(int year, String quarter, String subject, int courseNum) {
        this.year = year;
        this.quarter = quarter;
        this.subject = subject;
        this.courseNum = courseNum;
        this.courseTitle = "" + year + " " + quarter + " " + subject + " " + courseNum;
    }
}