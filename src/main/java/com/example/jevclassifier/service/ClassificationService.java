package com.example.jevclassifier.service;

import com.example.jevclassifier.dto.ClassificationItem;
import com.example.jevclassifier.dto.ClassificationRequest;
import com.example.jevclassifier.dto.ClassificationResponse;
import com.example.jevclassifier.dto.DepartmentItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

@Service
@RequiredArgsConstructor
public class ClassificationService {

    private final DepartmentService departmentService;
    private final OpenAIService openAIService;
    private final JevService jevService;

    public ClassificationResponse callLLM(ClassificationRequest req){
        List<ClassificationItem> items = new ArrayList<>();
        streamLLM(req, (index, item) -> items.add(item), (index, error) -> {
            throw error;
        });
        return new ClassificationResponse(items);
    }

    public void streamLLM(ClassificationRequest req,
                          BiConsumer<Integer, ClassificationItem> onResult,
                          BiConsumer<Integer, RuntimeException> onError) {
        String aiPromptStart= """
                입력된 텍스트가 어떤 부서와 관련 있는지 분류,
                
                여러개의 부서와 관련있다면 부서명이 여러개일 수 있다
                분류할만한 부서가 없다면 "판별불가" 로 출력
                """;

        String departmentInfo=departmentService.getPromptFromDepartment();// DB의 부서정보를 추가
        String sysPrompt= aiPromptStart + departmentInfo;

        for (int index = 0; index < req.getContexts().size(); index++) {
            String context = req.getContexts().get(index);
            ClassificationItem item;
            try {
                String llmOutput = openAIService.generate(context, sysPrompt);
                item = new ClassificationItem(context, llmOutput, 0.0);
            } catch (RuntimeException error) {
                onError.accept(index, error);
                continue;
            }
            onResult.accept(index, item);
        }
    }

    public ClassificationResponse callJev(ClassificationRequest req){
        List<ClassificationItem> items = new ArrayList<>();
        streamJev(req, (index, item) -> items.add(item), (index, error) -> {
            throw error;
        });
        return new ClassificationResponse(items);
    }

    public void streamJev(ClassificationRequest req,
                          BiConsumer<Integer, ClassificationItem> onResult,
                          BiConsumer<Integer, RuntimeException> onError) {
        List<DepartmentItem> departments = departmentService.getDepartmentList();

        double threshold = 0.65;

        for (int index = 0; index < req.getContexts().size(); index++) {
            String context = req.getContexts().get(index);
            ClassificationItem item;
            try {
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

                item = new ClassificationItem(
                        context,
                        categoryName,
                        highestMatchedProbability
                );
            } catch (RuntimeException error) {
                onError.accept(index, error);
                continue;
            }
            onResult.accept(index, item);
        }
    }

}
