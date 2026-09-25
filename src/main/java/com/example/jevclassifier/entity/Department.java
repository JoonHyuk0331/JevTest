package com.example.jevclassifier.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length=50)
    private String departmentName;

    @Column(columnDefinition = "TEXT")
    private String departmentInfo;

    @Builder
    public Department(String departmentInfo,String departmentName){
        this.departmentInfo=departmentInfo;
        this.departmentName=departmentName;
    }
}
