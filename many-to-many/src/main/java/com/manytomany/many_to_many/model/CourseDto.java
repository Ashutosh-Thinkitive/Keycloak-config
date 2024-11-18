package com.manytomany.many_to_many.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CourseDto {

    private Integer courseId;

    private String courseName;

    private List<Integer> students;
}
