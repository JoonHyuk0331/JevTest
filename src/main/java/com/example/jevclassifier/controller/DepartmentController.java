package com.example.jevclassifier.controller;

import com.example.jevclassifier.dto.DepartmentCreateRequest;
import com.example.jevclassifier.dto.DepartmentItem;
import com.example.jevclassifier.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    //조회용
    @GetMapping
    public List<DepartmentItem> getDepartList(){
        return departmentService.getDepartmentList();
    }

    //생성
    @PostMapping
    public void createDepartment(@RequestBody DepartmentCreateRequest req){
        departmentService.createDepartment(req);
    }

    //삭제
    @DeleteMapping
    public void deleteDepartment(){
        departmentService.deleteDepartment();
    }
}
