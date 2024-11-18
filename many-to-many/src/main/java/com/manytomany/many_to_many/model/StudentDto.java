package com.manytomany.many_to_many.model;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class StudentDto {

    private Integer studentId;


    private String name;

    private List<Integer> courses;
}
