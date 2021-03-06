package com.example.birdsofafeather;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Intent;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.shadows.ShadowActivity;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import com.example.birdsofafeather.model.db.AppDatabase;
import com.example.birdsofafeather.model.db.Course;
import com.example.birdsofafeather.model.db.Student;
import com.example.birdsofafeather.model.db.StudentSorter;

import java.util.List;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class Story1Test {
    AppDatabase db;
    ActivityScenario<HomeActivity> scenario;

    @Before
    public void init() {
        AppDatabase.useTestSingleton(getApplicationContext());
        db = AppDatabase.singleton(getApplicationContext());
        scenario = ActivityScenario.launch(HomeActivity.class);
    }

    @After
    public void tearDown() {
        db.close();
        scenario.close();
    }

    /**
     * Tests if the database properly works by checking if there are originally
     * are 0 students and 0 courses and if there are however many students and
     * courses after adding a specific number of students and courses.
     */
    @Test
    public void testDatabase() {
        scenario.onActivity(activity ->{
            // There should be 1 student (user) in the database before any are added
            assertEquals(1, db.studentWithCoursesDao().count());

            // There should be 0 courses in the database before any are added
            assertEquals(0, db.coursesDao().count());

            Student student1 = new Student(1, "Jeff", "google.jpg", UUID.randomUUID().toString());
            Student student2 = new Student(2, "Eric", "gmail.jpg", UUID.randomUUID().toString());

            db.studentWithCoursesDao().insert(student1);
            db.studentWithCoursesDao().insert(student2);

            Course jeffCourse = new Course(1, 2022, "WI", "CSE", "110", 0);
            Course ericCourse1 = new Course(2, 2022, "WI", "CSE", "110", 0);
            Course ericCourse2 = new Course(2, 2021, "FA", "CSE", "105", 0);

            db.coursesDao().insert(jeffCourse);
            db.coursesDao().insert(ericCourse1);
            db.coursesDao().insert(ericCourse2);

            // There should be 3 students now that Jeff and Eric have been added
            assertEquals(3, db.studentWithCoursesDao().count());

            // There should be 3 notes now that notes have been added
            assertEquals(3, db.coursesDao().count());
        });
    }

    @Test
    public void testLaunchesAddCourses() {
        scenario.onActivity(activity -> {
            Button button = activity.findViewById(R.id.add_courses_button);
            button.callOnClick();

            Intent expectedIntent = new Intent(activity, AddCourseActivity.class);
            ShadowActivity shadowActivity = Shadows.shadowOf(activity);
            Intent actualIntent = shadowActivity.getNextStartedActivity();
            assertTrue(actualIntent.filterEquals(expectedIntent));
        });
    }

    @Test
    public void testLaunchesMock() {
        scenario.onActivity(activity -> {
            Button button = activity.findViewById(R.id.mock_nearby_messages_btn);
            button.callOnClick();

            Intent expectedIntent = new Intent(activity, NearbyMessagesMockActivity.class);
            ShadowActivity shadowActivity = Shadows.shadowOf(activity);
            Intent actualIntent = shadowActivity.getNextStartedActivity();
            assertTrue(actualIntent.filterEquals(expectedIntent));
        });
    }

    @Test
    public void testStartAndStopCorrectDatabaseItems() {
        Student student1 = new Student(1, "Jeff", "google.jpg", UUID.randomUUID().toString());
        student1.sessionID = 1;
        db.studentWithCoursesDao().insert(student1);

        Course jeffCourse = new Course(1, 2022, "WI", "CSE", "110");
        db.coursesDao().insert(jeffCourse);

        scenario.onActivity(activity -> {
            Button startButton = activity.findViewById(R.id.start_btn);
            startButton.callOnClick();

            Button stopButton = activity.findViewById(R.id.stop_btn);
            stopButton.callOnClick();

            activity.studentSorter.getSortedStudents(StudentSorter.COMM_COURSE, 1);

            assertEquals(1, db.sessionDao().getLast().sessionID);
            // Should expect Jeff to have 0 matched courses, since the user has 0 courses
            assertEquals(0, db.studentWithCoursesDao().get(1).getCommonCourseCount());
        });
    }
}
