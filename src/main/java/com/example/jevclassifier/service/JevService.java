package com.example.jevclassifier.service;

import com.example.jevclassifier.dto.DepartmentItem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JevService {

    private final RestClient restClient;
    private final String model;

    public JevService(
            RestClient.Builder builder,
            @Value("${jev.api.base-url}") String baseUrl,
            @Value("${jev.api.key}") String apiKey,
            @Value("${jev.api.model}") String model){
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer "+apiKey)
                .build();
        this.model = model;
    }

    public List<Double> classify(String context, List<DepartmentItem> departments){
        if(departments.isEmpty()){
            return List.of(); //아무것도 없으면 빈 리스트를 내보낸다
        }
        Map<String, Object> questions= new LinkedHashMap<>();

        for(int i =0;i<departments.size();i++){
            DepartmentItem department = departments.get(i);
            questions.put("department_"+i,Map.of("type","noul","instructions", """
                    다음 텍스트가 '%s' 부서와 관련 있는가?
                    이 부서의 담당 업무: %s
                    """.formatted(department.departmentName(),department.departmentInfo()
                    )
                )
            );
        }

        JevResponse response = restClient.post()
                .uri("/v1/systemone")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new JevRequest(model,context,questions))
                .retrieve()
                .body(JevResponse.class);
        if(response==null||response.answers()==null){
            throw new IllegalStateException("JEV 응답에 answers가 없습니다");
        }
        List<Double> probabilities = new ArrayList<>();

        for (int i=0;i<departments.size();i++){
            JevAnswer answer = response.answers().get("department_"+i);
            if (answer == null || !"noul".equals(answer.type())
                    || answer.noul() == null
                    || !Double.isFinite(answer.noul())
                    || answer.noul() < 0 || answer.noul() > 1) {
                throw new IllegalStateException("JEV 부서 응답이 올바르지 않습니다: department_" + i);
            }
            probabilities.add(answer.noul());
        }
        return probabilities;
    }

    public record JevRequest(String model,String state,Map<String,Object> questions){}
    public record JevResponse(Map<String,JevAnswer> answers){}
    public record JevAnswer(String type,Double noul){}
}
