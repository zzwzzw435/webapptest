package com.wonderfulteam.controller;

import com.wonderfulteam.Application;
import com.wonderfulteam.config.MatricsConfig;
import com.wonderfulteam.service.UserService;
import com.wonderfulteam.util.Auth;
import com.wonderfulteam.util.JSONParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


/**
 * Created by Qixiang Zhou on 2019-09-29 01:06
 */
@RestController
@RequestMapping("")
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    JSONParser jsonParser;
    @Autowired
    Auth auth;

    /**
     *
     * */
    @RequestMapping(value = "/v1/user", method = RequestMethod.POST)
    public ResponseEntity<String> createUser(@RequestBody String payload) {
        Application.logger.info("make create user api call");
        MatricsConfig.statsd.increment("user.create");
        long time1 = System.currentTimeMillis();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        JSONObject input = new JSONObject(payload);
        String email, password, firstname, lastname;

        // Email checker
        if (!input.has("email")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Email can not be empty"));
        } else {

            if (!auth.validEmail(input.getString("email"))) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Can not use non-email to create user"));
            }
        }

        // Password checker
        if (!input.has("password")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Password can not be empty"));
        } else {

            if (!auth.verifyPassword(input.getString("password"))) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Password is too weak! Use at least 3 type in Uppercase letter, Lowercase letter, numbers, and special character. Don't repeat same character more than 3 times"));
            }
        }

        //firstname checker
        if (!input.has("first_name")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("First name can not be empty"));
        }

        //lastname checker
        if (!input.has("last_name")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Last name can not be empty"));
        }

        email = input.getString("email");
        password = input.getString("password");
        lastname = input.getString("last_name");
        firstname = input.getString("first_name");

        try {
            JSONObject res = userService.createUser(email, password, firstname, lastname);
            long time2 = System.currentTimeMillis();
            MatricsConfig.statsd.recordExecutionTime("user.create.api_time", time2 - time1);
            return ResponseEntity.status(201).headers(responseHeaders).body(res.toString());
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("User already exist"));
        }

    }


    /**
     *
     * */
    @RequestMapping(value = "/v1/user/self", method = RequestMethod.PUT)
    public ResponseEntity<String> updateUser(@RequestHeader(name = "Authorization") Optional<String> authorization, @RequestBody String payload) {
        Application.logger.info("make update user api call");
        MatricsConfig.statsd.increment("user.update");
        long time1 = System.currentTimeMillis();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");

        /*==================CHECK AUTH INFO======================*/
        if (!authorization.isPresent()) {
            return ResponseEntity.status(401).headers(responseHeaders).body(jsonParser.getResponseMessage("Protected URL, please provide proper token to access!!!"));
        }
        String[] info = auth.checkHeaderAuth(authorization.get());
        if (info == null || info[0].length() == 0 || info.length == 1) {
            return ResponseEntity.status(401).headers(responseHeaders).body(jsonParser.getResponseMessage("Authentication failed, Please provide full information"));
        }

        // Those are decode from auth token
        String email = info[0];
        String password = info[1];
        if (!userService.login(email, password)) {
            return ResponseEntity.status(401).headers(responseHeaders).body(jsonParser.getResponseMessage("Authentication failed, Please provide correct information"));
        }

        /*==================AUTH PASSED==================*/
        /*==================CHECK INPUT==================*/
        JSONObject input = new JSONObject(payload);
        // email checker
        String changepass, changefirstname, changelastname;
        if (input.has("email") && !input.getString("email").equals(email)) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Can not update email information"));
        }
        // password checker
        if (!input.has("password")) {
            password = null;
        } else {
            password = input.getString("password");
            if (!auth.verifyPassword(password)) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Password is too weak!"));
            }
        }
        // first name checker
        if (!input.has("first_name")) {
            changefirstname = null;
        } else {
            changefirstname = input.getString("first_name");
        }
        // last name checker
        if (!input.has("last_name")) {
            changelastname = null;
        } else {
            changelastname = input.getString("last_name");
        }

        try {
            JSONObject res = userService.updateUser(email, password, changefirstname, changelastname);
            long time2 = System.currentTimeMillis();
            MatricsConfig.statsd.recordExecutionTime("user.update.api_time", time2 - time1);
            return ResponseEntity.ok().headers(responseHeaders).body(res.toString());
        } catch (Exception e) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Update user information failed"));
        }
    }

    /**
     *
     * */
    @RequestMapping(value = "/v1/user/self", method = RequestMethod.GET)
    public ResponseEntity<String> getUser(@RequestHeader(name = "Authorization") Optional<String> authorization) {
        Application.logger.info("make get user api call");
        MatricsConfig.statsd.increment("user.get");
        long time1 = System.currentTimeMillis();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");

        /*==================CHECK AUTH INFO======================*/
        if (!authorization.isPresent()) {
            return ResponseEntity.status(401).headers(responseHeaders).body(jsonParser.getResponseMessage("Protected URL, please provide proper token to access!!!"));
        }
        String[] info = auth.checkHeaderAuth(authorization.get());
        if (info == null || info[0].length() == 0 || info.length == 1) {
            return ResponseEntity.status(401).headers(responseHeaders).body(jsonParser.getResponseMessage("Authentication failed, Please provide full information"));
        }

        // Those are decode from auth token
        String email = info[0];
        String password = info[1];
        if (!userService.login(email, password)) {
            return ResponseEntity.status(401).headers(responseHeaders).body(jsonParser.getResponseMessage("Authentication failed, Please provide correct information"));
        }
        /*==================AUTH PASSED==================*/
        try {
            JSONObject res = userService.getUserInfo(email);
            long time2 = System.currentTimeMillis();
            MatricsConfig.statsd.recordExecutionTime("user.get.api_time", time2 - time1);
            return ResponseEntity.ok().headers(responseHeaders).body(res.toString());
        } catch (Exception e) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Get user information failed"));
        }
    }
}
