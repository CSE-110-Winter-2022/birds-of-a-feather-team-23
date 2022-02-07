package com.example.birdsofafeather;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.birdsofafeather.model.DummyStudent;
import com.example.birdsofafeather.model.IStudent;
import com.example.birdsofafeather.model.db.AppDatabase;
import com.example.birdsofafeather.model.db.Course;
import com.example.birdsofafeather.model.db.Student;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "BoaF";
    private static final int USER_ID = 0;
    private Message msg;
    protected RecyclerView matchedStudentsView;
    protected RecyclerView.LayoutManager studentsLayoutManager;
    protected StudentsViewAdapter studentsViewAdapter;
    private MessageListener messageListener;

    protected IStudent user = new DummyStudent(0, "Daniel", "",
            new String[] {
                    "DUMMY COURSE 1"
            });
    protected IStudent[] data_dummy = {
            new DummyStudent(1, "Elizabeth", "", new String[]{
                    "DUMMY COURSE 1"
            }),
            new DummyStudent(2, "Rye", "", new String[]{}),
            new DummyStudent(3, "Jeff", "", new String[]{}),
            new DummyStudent(4, "Helen", "", new String[]{
                    "DUMMY COURSE 1"
            }),
            new DummyStudent(5, "Eric", "", new String[]{})
    };

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Button stop = findViewById(R.id.stop_btn);
        //stop.setVisibility(View.GONE);

        AppDatabase db = AppDatabase.singleton(getApplicationContext());

        //FOR TESTING STORY 8
        db.clearAllTables();
        Student user_temp = new Student(USER_ID,"Daniel", "");
        Student friend1 = new Student(1, "Elizabeth", "");
        Student friend2 = new Student(2, "Rye", "");
        Student friend3 = new Student(3, "Jeff", "");
        Student friend4 = new Student(4, "Helen", "");
        Student friend5 = new Student(5, "Eric", "");

        db.studentWithCoursesDao().insert(user_temp);
        db.studentWithCoursesDao().insert(friend1);
        db.studentWithCoursesDao().insert(friend2);
        db.studentWithCoursesDao().insert(friend3);
        db.studentWithCoursesDao().insert(friend4);
        db.studentWithCoursesDao().insert(friend5);
        db.coursesDao().insert(new Course(db.coursesDao().getCourses().size() + 1,
                USER_ID, 2021, "FA", "CSE", 100));
        db.coursesDao().insert(new Course(db.coursesDao().getCourses().size() + 1,
                1, 2021, "FA", "CSE", 100));
        db.coursesDao().insert(new Course(db.coursesDao().getCourses().size() + 1,
                4, 2021, "FA", "CSE", 100));
        // END OF TESTING

        List<? extends IStudent> students = db.studentWithCoursesDao().getAll();
        user = students.remove(0);
        this.msg = new Message("Daniel".getBytes());

        matchedStudentsView = findViewById(R.id.matched_students_view);

        studentsLayoutManager = new LinearLayoutManager(this);
        matchedStudentsView.setLayoutManager(studentsLayoutManager);

        Pair<List<IStudent>, List<Integer>> orderedStudents = orderStudents(students);
        studentsViewAdapter = new StudentsViewAdapter(orderedStudents.first
                , orderedStudents.second);
        matchedStudentsView.setAdapter(studentsViewAdapter);
    }

    private Pair<List<IStudent>, List<Integer>> orderStudents(List<? extends IStudent> currOrder) {
        List<Integer> commCourses = new ArrayList<>();
        List<IStudent> newOrder = new ArrayList<>();

        PriorityQueue<StudentByCommonCourses> pq = new PriorityQueue<>();

        for(IStudent curr: currOrder) {
            int cc = 0;
            List<String> courses = curr.getClasses();
            AppDatabase db = AppDatabase.singleton(getApplicationContext());
            for(String c : courses) {
                if(user.getClasses().contains(c)){
                    cc++;
                }
            }

            pq.add(new StudentByCommonCourses(curr, cc));
        }

        while(!pq.isEmpty()) {
            StudentByCommonCourses sbcc = pq.poll();
            newOrder.add(sbcc.student);
            commCourses.add(sbcc.commCourse);
        }

        return new Pair<>(newOrder, commCourses);
    }

    public void onAddCoursesClicked(View view) {
        Intent intent = new Intent(this, AddCourseActivity.class);
        startActivity(intent);
    }

    protected class StudentByCommonCourses implements Comparable {
        protected IStudent student;
        protected int commCourse;

        protected StudentByCommonCourses (IStudent student, int cc) {
            this.student = student;
            this.commCourse = cc;
        }

        @Override
        public int compareTo(Object o) {
            if(o instanceof StudentByCommonCourses) {
                return ((StudentByCommonCourses)o).commCourse - this.commCourse;
            } else {
                return 0;
            }
        }
    }

    public void onStartClicked(View view) {
        Button start = findViewById(R.id.start_btn);
        start.setVisibility(View.GONE);

        Button stop = findViewById(R.id.stop_btn);
        stop.setVisibility(View.VISIBLE);

        MessageListener realListener = new MessageListener() {
            //put information into database
            @Override
            public void onFound(@NonNull Message message) {
                Log.d(TAG, "Found message: " + new String(message.getContent()));
            }

            @Override
            public void onLost(@NonNull Message message) {
                Log.d(TAG, "Lost sight of message: " + new String(message.getContent()));
            }
        };

        //eventually not faked
        this.messageListener = new FakedMessageListener(realListener, 3, "Hello world!");
        Nearby.getMessagesClient(this).subscribe(messageListener);
        Nearby.getMessagesClient(this).publish(msg);
    }

    public void onStopClicked(View view) {
        Button start = findViewById(R.id.start_btn);
        start.setVisibility(View.VISIBLE);

        Button stop = findViewById(R.id.stop_btn);
        stop.setVisibility(View.GONE);

        Nearby.getMessagesClient(this).unsubscribe(messageListener);
        Nearby.getMessagesClient(this).unpublish(this.msg);
    }
}