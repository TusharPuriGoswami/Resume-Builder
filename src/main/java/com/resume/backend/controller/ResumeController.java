package com.resume.backend.controller;

import com.resume.backend.ResumeRequest;
import com.resume.backend.services.ResumeService;
import netscape.javascript.JSObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/resume")
public class ResumeController {


   // @Autowired
    private ResumeService resumeService;

public ResumeController(ResumeService resumeService){
    this.resumeService = resumeService;
}

    @PostMapping("/generate")
    public ResponseEntity<Map<String , Object>> getResumeData(@RequestBody ResumeRequest resumeRequest) throws IOException {
        Map<String , Object> stringObjectMap = resumeService.generateResumeResponse(resumeRequest.userDiscription());
        return new ResponseEntity<>(stringObjectMap, HttpStatus.OK);
    }


}
