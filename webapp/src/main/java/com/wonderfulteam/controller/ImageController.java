package com.wonderfulteam.controller;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.wonderfulteam.Application;
import com.wonderfulteam.config.MatricsConfig;
import com.wonderfulteam.service.ImageService;
import com.wonderfulteam.service.RecipieService;
import com.wonderfulteam.service.UserService;
import com.wonderfulteam.util.Auth;
import com.wonderfulteam.util.JSONParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Created by Qixiang Zhou on 2019-10-23 17:19
 */
@RestController
@RequestMapping("")
public class ImageController {

    @Autowired
    JSONParser jsonParser;
    @Autowired
    Auth auth;
    @Autowired
    ImageService imageService;
    @Autowired
    UserService userService;
    @Autowired
    RecipieService recipieService;

    @RequestMapping(value = "/v1/recipe/{id}/image/{imageID}", method = RequestMethod.GET)
    public ResponseEntity<String> getImage(@PathVariable("id") String id, @PathVariable("imageID") String imageID) {
        Application.logger.info("make get image api call");
        /*************************************No auth here***************************************/
        MatricsConfig.statsd.increment("image.get");
        long time1 = System.currentTimeMillis();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");
        // check recipe exist
        if (recipieService.getRecipie(id) == null) {
            return ResponseEntity.status(204).headers(responseHeaders).body(jsonParser.getResponseMessage("No such recipie for id : " + id));
        }
        // check image exist
        if (imageService.getImageInfo(imageID) == null) {
            return ResponseEntity.status(204).headers(responseHeaders).body(jsonParser.getResponseMessage("No such Image for id : " + imageID));
        }
        // check image is belong to recipe
        if (!recipieService.hasImage(imageID, id)) {
            return ResponseEntity.status(404).headers(responseHeaders).body(jsonParser.getResponseMessage("Current recipe doesn't have image you want"));
        }

        try {
            JSONObject res = imageService.getImageInfo(imageID);
            long time2 = System.currentTimeMillis();
            MatricsConfig.statsd.recordExecutionTime("image.get.api_time", time2-time1);
            return ResponseEntity.status(201).headers(responseHeaders).body(res.toString());
        } catch(Exception e) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Get image information failed" + e.getMessage()));
        }
    }

    @RequestMapping(value = "/v1/recipe/{id}/image/{imageID}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteImage(@PathVariable("id") String id, @PathVariable("imageID") String imageID, @RequestHeader(name = "Authorization") Optional<String> authorization) {
        Application.logger.info("make delete image api call");
        MatricsConfig.statsd.increment("image.delete");
        long time1 = System.currentTimeMillis();
        /*************************************Auth here *****************************************/
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");

        Regions clientRegion = Regions.US_EAST_1;
        String bucket = "";
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

        /*==================Check author=================*/
        if (recipieService.getRecipie(id) == null) {
            return ResponseEntity.status(204).headers(responseHeaders).body(jsonParser.getResponseMessage("No such recipie for id : " + id));
        }

        if (!recipieService.sameAuthor(email, id)) {
            return ResponseEntity.status(401).headers(responseHeaders).body(jsonParser.getResponseMessage("Current user have no auth to delete that recipie"));
        }
        /*==================Finish check=================*/
        // check image exist
        if (imageService.getImageInfo(imageID) == null) {
            return ResponseEntity.status(204).headers(responseHeaders).body(jsonParser.getResponseMessage("No such Image for id : " + imageID));
        }
        // check image is belong to recipe
        if (!recipieService.hasImage(imageID, id)) {
            return ResponseEntity.status(404).headers(responseHeaders).body(jsonParser.getResponseMessage("Current recipe doesn't have image you want"));
        }

        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new ProfileCredentialsProvider())
                    .build();
            List<Bucket> buckets = s3Client.listBuckets();
            if(buckets == null || buckets.isEmpty()){
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("get bucket failed" ));
            }
            Bucket b = buckets.get(0);
            bucket = b.getName();
            if (imageService.deleteImage(id, imageID,s3Client,bucket)) {
                long time2 = System.currentTimeMillis();
                MatricsConfig.statsd.recordExecutionTime("image.delete.api_time", time2-time1);
                return ResponseEntity.ok().headers(responseHeaders).body(jsonParser.getResponseMessage("Delete image : "+ imageID +" Success..."));
            } else {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Delete image : "+ imageID +" Fail..."));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).headers(responseHeaders).body(jsonParser.getResponseMessage(e.getMessage()));
        }


        /****************************************************************************************/
    }
}
