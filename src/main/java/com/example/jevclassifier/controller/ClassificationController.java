package com.example.jevclassifier.controller;

import com.example.jevclassifier.dto.ClassificationRequest;
import com.example.jevclassifier.dto.ClassificationResponse;
import com.example.jevclassifier.service.ClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/classification")
@RequiredArgsConstructor
public class ClassificationController {

    private final ClassificationService classificationService;

    @PostMapping("/llm")
    public ClassificationResponse classifyLLM(@RequestBody ClassificationRequest req){
        return classificationService.callLLM(req);
    }

/*    @PostMapping("/classification/jev")
    public ClassificationRequest classifyJEV(){

    }*/

}
