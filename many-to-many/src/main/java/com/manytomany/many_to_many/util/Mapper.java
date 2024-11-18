package com.manytomany.many_to_many.util;

import com.manytomany.many_to_many.model.Course;
import com.manytomany.many_to_many.model.CourseDto;
import com.manytomany.many_to_many.model.Student;
import com.manytomany.many_to_many.model.StudentDto;

import java.util.List;
import java.util.stream.Collectors;

public class Mapper {

    public static Course mapCourse(CourseDto courseDto, List<Student> students) {
        Course course = new Course();

        course.setCourseName(courseDto.getCourseName());
        course.setStudents(students);
        return course;

    }

    public static Student mapStudent(StudentDto studentDto, List<Course> courses) {
        Student student = new Student();
        student.setName(studentDto.getName());
        student.setCourses(courses);
        return student;
    }

    public static CourseDto mapCourseDto(Course course) {
        CourseDto courseDto = new CourseDto();
        courseDto.setCourseId(course.getCourseId());
        courseDto.setCourseName(course.getCourseName());
        courseDto.setStudents(course.getStudents().stream().map(Student::getStudentId).collect(Collectors.toList()));
        return courseDto;
    }

    public static StudentDto mapStudentDto(Student student) {
        StudentDto studentDto = new StudentDto();
        studentDto.setStudentId(student.getStudentId());
        studentDto.setName(student.getName());
        studentDto.setCourses(student.getCourses().stream().map(Course::getCourseId).collect(Collectors.toList()));
        return studentDto;
    }
}
