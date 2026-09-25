package com.example.jevclassifier.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ClassificationRequest {
    private List<String> contexts;
}
