package com.manytomany.many_to_many.controller;

import com.manytomany.many_to_many.model.StudentDto;
import com.manytomany.many_to_many.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {


    @Autowired
    private StudentService studentService;

    @PostMapping
    public ResponseEntity<StudentDto> createStudent(@RequestBody StudentDto studentDto){
        return studentService.addStudent(studentDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentDto> updateStudent(@PathVariable Integer id, @RequestBody StudentDto studentDto){
        return studentService.updateStudent(id,studentDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletdStudent(@PathVariable Integer id){
        return studentService.deleteStudent(id);
    }

    @GetMapping
    public ResponseEntity<List<StudentDto>> getAllStudent(){
        return studentService.getAllStudent();
    }
}
