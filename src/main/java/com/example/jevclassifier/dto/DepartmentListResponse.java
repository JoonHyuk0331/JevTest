package com.example.jevclassifier.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DepartmentListResponse {
    private List<DepartmentItem> departmentItemList;
}
