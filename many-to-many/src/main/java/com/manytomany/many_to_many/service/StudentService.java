package com.manytomany.many_to_many.service;

import com.manytomany.many_to_many.model.Course;
import com.manytomany.many_to_many.model.Student;
import com.manytomany.many_to_many.model.StudentDto;
import com.manytomany.many_to_many.repository.CourseRepository;
import com.manytomany.many_to_many.repository.StudentRepository;

import static com.manytomany.many_to_many.util.Mapper.*;

import com.manytomany.many_to_many.util.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {


    @Autowired
    private StudentRepository studentRepository;


    @Autowired
    private CourseRepository courseRepository;


    public ResponseEntity<StudentDto> addStudent(StudentDto studentDto) {
        List<Course> courses = courseRepository.findAllById(studentDto.getCourses());
        Student student = mapStudent(studentDto, courses);
        for (Course course : courses) {
            course.getStudents().add(student);
        }
        studentRepository.save(student);
        return new ResponseEntity<>(mapStudentDto(student), HttpStatus.CREATED);
    }

    public ResponseEntity<StudentDto> updateStudent(Integer studentId, StudentDto studentDto) {
        Optional<Student> studentOptional = studentRepository.findById(studentId);
        if (studentOptional.isPresent()) {
            Student existingStudent = studentOptional.get();
            existingStudent.setName(studentDto.getName());
            List<Course> currentCourses = existingStudent.getCourses();
            if (studentDto.getCourses() != null && !studentDto.getCourses().isEmpty()) {
                for (Course course:currentCourses){
                    course.getStudents().remove(existingStudent);
                }
                List<Course> courses = courseRepository.findAllById(studentDto.getCourses());
                for (Course course : courses){
                    course.getStudents().add(existingStudent);
                }
                existingStudent.setCourses(courses);
            }
            studentRepository.save(existingStudent);
            return new ResponseEntity<>(mapStudentDto(existingStudent), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    public ResponseEntity<String> deleteStudent(Integer id) {
        Optional<Student> studentOptional = studentRepository.findById(id);
        if (studentOptional.isPresent()) {
            Student student = studentOptional.get();
            List<Course> courses = student.getCourses();
            for (Course course : courses) {
                course.getStudents().remove(student);
            }
            student.getCourses().clear();
            studentRepository.delete(student);
            return new ResponseEntity<>("Deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);

    }

    public ResponseEntity<List<StudentDto>> getAllStudent() {
        List<Student> students = studentRepository.findAll();
        List<StudentDto> studentDtoList = students.stream().map(Mapper::mapStudentDto).collect(Collectors.toList());
        return new ResponseEntity<>(studentDtoList, HttpStatus.OK);
    }
}
