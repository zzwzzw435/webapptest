package com.wonderfulteam.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Qixiang Zhou on 2019-09-28 21:55
 */
@RestController
public class HelloController {

    @RequestMapping(value = "/")
    public ResponseEntity<String> index() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        String ans = "{\"message\":\"Welcome to our REST services\"}";
        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(ans);
    }
}
