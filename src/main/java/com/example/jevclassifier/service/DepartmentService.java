package com.example.jevclassifier.service;

import com.example.jevclassifier.dto.DepartmentCreateRequest;
import com.example.jevclassifier.dto.DepartmentItem;
import com.example.jevclassifier.dto.DepartmentListResponse;
import com.example.jevclassifier.entity.Department;
import com.example.jevclassifier.repository.DepartmentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    //전체 부서 조회
    public List<DepartmentItem> getDepartmentList(){
        return departmentRepository.findAll().stream()
                .map(department -> new DepartmentItem(
                        department.getDepartmentName(),
                        department.getDepartmentInfo()
                )).toList();
    }

    //전체 부서 삭제
    @Transactional
    public void deleteDepartment(){
        departmentRepository.deleteAll();
    }

    //부서 생성
    @Transactional
    public void createDepartment(DepartmentCreateRequest req){
        if(req.getDepartmentInfo() ==null || req.getDepartmentInfo().isBlank()){
            throw new IllegalArgumentException("부서명은 필수입니다.");
        }
        if(req.getDepartmentName()==null || req.getDepartmentName().isBlank()){
            throw new IllegalArgumentException("부서 설명은 필수입니다.");
        }

        Department department = Department.builder()
                .departmentInfo(req.getDepartmentInfo())
                .departmentName(req.getDepartmentName())
                .build();

        departmentRepository.save(department);
    }

    public String getPromptFromDepartment(){
        return getDepartmentList().stream()
            .map(department -> """
                - 부서명: %s 
                  설명: %s
                """.formatted(
                        department.departmentName(),
                department.departmentInfo()
                ))
            .collect(Collectors.joining("\n"));
    }
}
