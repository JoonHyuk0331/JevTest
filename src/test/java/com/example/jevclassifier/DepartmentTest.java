package com.example.jevclassifier;

import com.example.jevclassifier.entity.Department;
import com.example.jevclassifier.repository.DepartmentRepository;
import com.example.jevclassifier.service.DepartmentService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DepartmentTest {

    @Test
    void getPromptFromDepartment_부서목록으로_프롬프트문자열을_생성한다() {
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        DepartmentService departmentService = new DepartmentService(departmentRepository);

        when(departmentRepository.findAll()).thenReturn(List.of(
                Department.builder()
                        .departmentName("개발팀")
                        .departmentInfo("서비스 개발과 유지보수를 담당합니다.")
                        .build(),
                Department.builder()
                        .departmentName("인사팀")
                        .departmentInfo("채용과 인사 관리를 담당합니다.")
                        .build()
        ));

        String prompt = departmentService.getPromptFromDepartment();

        System.out.println("생성된 부서 프롬프트:");
        System.out.println(prompt);

        assertThat(prompt).isEqualTo("""
                - 부서명: 개발팀 
                  설명: 서비스 개발과 유지보수를 담당합니다.

                - 부서명: 인사팀 
                  설명: 채용과 인사 관리를 담당합니다.
                """);
    }
}
