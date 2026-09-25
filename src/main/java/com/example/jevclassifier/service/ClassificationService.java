package com.example.jevclassifier.service;

import aQute.bnd.annotation.jpms.Open;
import com.example.jevclassifier.dto.ClassificationItem;
import com.example.jevclassifier.dto.ClassificationRequest;
import com.example.jevclassifier.dto.ClassificationResponse;
import com.example.jevclassifier.dto.DepartmentItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassificationService {

    private final DepartmentService departmentService;
    private final OpenAIService openAIService;
    private final JevService jevService;

    public ClassificationResponse callLLM(ClassificationRequest req){

        String aiPromptStart= """
                입력된 텍스트가 어떤 부서와 관련 있는지 분류,
                
                여러개의 부서와 관련있다면 부서명이 여러개일 수 있다
                분류할만한 부서가 없다면 "판별불가" 로 출력
                """;

        String departmentInfo=departmentService.getPromptFromDepartment();// DB의 부서정보를 추가
        String sysPrompt= aiPromptStart + departmentInfo;

        List<ClassificationItem> classificationItemList=new ArrayList<>();
        for(String context:req.getContexts()){ // context: 부서 분류가 필요한 텍스트
            String llmOutput= openAIService.generate(context,sysPrompt);
            classificationItemList.add(new ClassificationItem(context,llmOutput,0.0));
        }

        return new ClassificationResponse(classificationItemList);
    }

    public ClassificationResponse callJev(ClassificationRequest req){
        List<DepartmentItem> departments = departmentService.getDepartmentList();
        List<ClassificationItem> items = new ArrayList<>();

        double threshold = 0.65;

        for (String context : req.getContexts()) {
            List<Double> probabilities = jevService.classify(context, departments);
            List<String> matchedNames = new ArrayList<>();
            double highestMatchedProbability = 0.0;

            for (int i = 0; i < departments.size(); i++) {
                double probability = probabilities.get(i);
                if (probability >= threshold) {
                    matchedNames.add(departments.get(i).departmentName());
                    highestMatchedProbability =
                            Math.max(highestMatchedProbability, probability);
                }
            }

            String categoryName = matchedNames.isEmpty()
                    ? "판별불가"
                    : String.join(", ", matchedNames);

            items.add(new ClassificationItem(
                    context,
                    categoryName,
                    highestMatchedProbability
            ));
        }

        return new ClassificationResponse(items);
    }

}
