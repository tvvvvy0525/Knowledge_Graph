package com.pinewilt.kg.controller;

import com.pinewilt.kg.dto.DiagnosisRequest;
import com.pinewilt.kg.dto.DiagnosisResult;
import com.pinewilt.kg.service.DiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnosis")
@CrossOrigin // 允许前端跨域
public class DiagnosisController {

    @Autowired
    private DiagnosisService diagnosisService;

    @PostMapping("/assess")
    public DiagnosisResult assessRisk(@RequestBody DiagnosisRequest request) {
        return diagnosisService.assess(request);
    }
}