package com.manytomany.many_to_many.service;

import com.manytomany.many_to_many.model.Course;
import com.manytomany.many_to_many.model.CourseDto;
import com.manytomany.many_to_many.model.Student;
import com.manytomany.many_to_many.repository.CourseRepository;

import static com.manytomany.many_to_many.util.Mapper.*;

import com.manytomany.many_to_many.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.manytomany.many_to_many.util.Mapper.mapCourseDto;

@Service
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    public ResponseEntity<CourseDto> addCourse(CourseDto courseDto) {

        List<Student> studentList = studentRepository.findAllById(courseDto.getStudents());
        Course course = mapCourse(courseDto, studentList);
        for (Student student : studentList) {
            student.getCourses().add(course);
        }
        courseRepository.save(course);
        return new ResponseEntity<>(mapCourseDto(course), HttpStatus.CREATED);

    }

    public ResponseEntity<CourseDto> updateCourse(Integer courseId, CourseDto courseDto) {
        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if (courseOptional.isPresent()) {
            Course existingCourse = courseOptional.get();
            existingCourse.setCourseName(courseDto.getCourseName());
            List<Student> currentStudents = existingCourse.getStudents();


            if (courseDto.getStudents() != null && !courseDto.getStudents().isEmpty()) {
                for (Student student : currentStudents) {
                    student.getCourses().remove(existingCourse);
                }
                List<Student> studentList = studentRepository.findAllById(courseDto.getStudents());
                for (Student student : studentList) {
                    student.getCourses().add(existingCourse);
                }
                existingCourse.setStudents(studentList);

            }
            courseRepository.save(existingCourse);
            return new ResponseEntity<>(mapCourseDto(existingCourse), HttpStatus.CREATED);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> deleteCourse(Integer courseId) {
        Optional<Course> courseOptional = courseRepository.findById(courseId);
        if (courseOptional.isPresent()) {
            Course course = courseOptional.get();
            List<Student> students = course.getStudents();
            for (Student student : students) {
                student.getCourses().remove(course);
            }
            course.getStudents().clear();
            courseRepository.delete(course);
            return new ResponseEntity<>("Deleted Successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<List<CourseDto>> getAllCourse() {
        List<Course> courseList = courseRepository.findAll();
        List<CourseDto> courseDtos = courseList.stream().map(s -> mapCourseDto(s)).collect(Collectors.toList());
        return new ResponseEntity<>(courseDtos, HttpStatus.OK);
    }

}
