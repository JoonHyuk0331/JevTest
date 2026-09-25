package com.example.jevclassifier.dto;

public record ClassificationItem (
    String text, //원문
    String categoryName, //분류한 부서명
    Double confidence //확률 (Jev 전용,LLM호출일 경우 없음)
){}
