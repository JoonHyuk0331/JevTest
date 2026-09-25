package com.example.jevclassifier.repository;

import com.example.jevclassifier.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department,Long> {
}
