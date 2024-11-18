package com.manytomany.many_to_many.controller;

import com.manytomany.many_to_many.model.CourseDto;
import com.manytomany.many_to_many.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseDto> createCourse(@RequestBody CourseDto courseDto) {
        return courseService.addCourse(courseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Integer id, @RequestBody CourseDto courseDto) {
        return courseService.updateCourse(id, courseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCourse(@PathVariable Integer id) {
        return courseService.deleteCourse(id);
    }

    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        return courseService.getAllCourse();
    }
}
