package com.wonderfulteam.controller;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import com.wonderfulteam.Application;
import com.wonderfulteam.bean.NutritionInformation;
import com.wonderfulteam.bean.OrderedList;

import com.wonderfulteam.bean.Recipie;

import com.wonderfulteam.config.MatricsConfig;
import com.wonderfulteam.service.RecipieService;
import com.wonderfulteam.service.UserService;
import com.wonderfulteam.service.ImageService;
import com.wonderfulteam.util.Auth;
import com.wonderfulteam.util.JSONParser;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Qixiang Zhou on 2019-09-29 22:10
 */
@RestController
@RequestMapping("")
public class RecipieController {
    @Autowired
    UserService userService;
    @Autowired
    RecipieService recipieService;
    @Autowired
    ImageService imageService;
    @Autowired
    JSONParser jsonParser;
    @Autowired
    Auth auth;


    /**
     * @param authorization
     * @param payload
     * @return
     */
    @RequestMapping(value = "v1/recipe", method = RequestMethod.POST)
    public ResponseEntity<String> createRecipie(@RequestHeader(name = "Authorization") Optional<String> authorization, @RequestBody String payload) {
        Application.logger.info("make create recipe api call");
        MatricsConfig.statsd.increment("recipe.create");
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

        //String author, Integer cooktime, Integer preptime, String title, String cusine, Integer servings, String ingredients, List<OrderedList> steps, NutritionInformation nutrition
        String author = userService.getUUID(email);
        int cooktime, preptime, servings, position, calories, sodium_in_mg;
        String title, cusine, ingredients, items;
        float cholesterol_in_mg, carbohydrates_in_grams, protein_in_grams;
        List<OrderedList> steps = new ArrayList<>();
        NutritionInformation nutrition;

        // cooktime checker
        if (!input.has("cook_time_in_min")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'cook_time_in_min\'"));
        } else {
            cooktime = input.getInt("cook_time_in_min");
            if (cooktime % 5 != 0) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Cook time should be the multiple of 5"));
            }
        }

        // preptime checker
        if (!input.has("prep_time_in_min")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'prep_time_in_min\'"));
        } else {
            preptime = input.getInt("prep_time_in_min");
            if (preptime % 5 != 0) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Prep time should be the multiple of 5"));
            }
        }

        // title checker
        if (!input.has("title")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'title\'"));
        } else {
            title = input.getString("title");
        }

        // cusine checker
        if (!input.has("cusine")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'cusine\'"));
        } else {
            cusine = input.getString("cusine");
        }

        // servings checker
        if (!input.has("servings")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'servings\'"));
        } else {
            servings = input.getInt("servings");
            if (servings < 1 || servings > 5) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Servings should in range [1, 5]"));
            }
        }

        // ingredients checker
        if (!input.has("ingredients")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'ingredients\'"));
        } else {
            ingredients = input.getJSONArray("ingredients").toString();
        }

        // steps checker
        if (!input.has("steps")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'steps\'"));
        } else {
            JSONArray temp = input.getJSONArray("steps");
            for (int i = 0; i < temp.length(); i++) {
                JSONObject ob = temp.getJSONObject(i);
                // position checker
                if (!ob.has("position")) {
                    return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under steps: \'position\'"));
                } else {
                    position = ob.getInt("position");
                }

                // items checker
                if (!ob.has("items")) {
                    return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under steps: \'items\'"));
                } else {
                    items = ob.getString("items");
                }
                OrderedList orderedList = recipieService.createOrderList(position, items);
                steps.add(orderedList);
            }
        }

        // nutrition_information checker
        if (!input.has("nutrition_information")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'nutrition_information\'"));
        } else {
            JSONObject ob = input.getJSONObject("nutrition_information");
            // calories checker
            if (!ob.has("calories")) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under nutrition_information: \'calories\'"));
            } else {
                calories = ob.getInt("calories");
            }

            // cholesterol_in_mg checker
            if (!ob.has("cholesterol_in_mg")) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under nutrition_information: \'cholesterol_in_mg\'"));
            } else {
                cholesterol_in_mg = ob.getFloat("cholesterol_in_mg");
            }

            // sodium_in_mg checker
            if (!ob.has("sodium_in_mg")) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under nutrition_information: \'sodium_in_mg\'"));
            } else {
                sodium_in_mg = ob.getInt("sodium_in_mg");
            }

            // carbohydrates_in_grams checker
            if (!ob.has("carbohydrates_in_grams")) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under nutrition_information: \'carbohydrates_in_grams\'"));
            } else {
                carbohydrates_in_grams = ob.getFloat("carbohydrates_in_grams");
            }

            // protein_in_grams checker
            if (!ob.has("protein_in_grams")) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under nutrition_information: \'protein_in_grams\'"));
            } else {
                protein_in_grams = ob.getFloat("protein_in_grams");
            }

            nutrition = recipieService.createNutritionInfo(calories, cholesterol_in_mg, sodium_in_mg, carbohydrates_in_grams, protein_in_grams);
        }
        /*================finish input checking==================*/

        try {
            JSONObject res = recipieService.createRecipie(author,cooktime,preptime,title,cusine,servings,ingredients,steps,nutrition);
            long time2 = System.currentTimeMillis();
            MatricsConfig.statsd.recordExecutionTime("recipe.create.api_time", time2-time1);
            return ResponseEntity.ok().headers(responseHeaders).body(res.toString());
        } catch (Exception e) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Create recipie failed" + e.getMessage()));
        }

    }

    /**
     * @param payload
     * @param id
     * @param authorization
     * @return
     */
    @RequestMapping(value = "v1/recipe/{id}", method = RequestMethod.PUT)
    public ResponseEntity<String> updateRecipie(@RequestBody String payload,@PathVariable("id") String id,@RequestHeader(name = "Authorization") Optional<String> authorization) {
        Application.logger.info("make update recipe api call");
        MatricsConfig.statsd.increment("recipe.update");
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

        /*==================Check author=================*/
        if (recipieService.getRecipie(id) == null) {
            return ResponseEntity.status(204).headers(responseHeaders).body(jsonParser.getResponseMessage("No such recipie for id : " + id));
        }

        if (!recipieService.sameAuthor(email, id)) {
            return ResponseEntity.status(401).headers(responseHeaders).body(jsonParser.getResponseMessage("Current user have no auth to delete that recipie"));
        }
        /*==================Finish check=================*/

        /*==================CHECK INPUT==================*/
        JSONObject input = new JSONObject(payload);

        //String author, Integer cooktime, Integer preptime, String title, String cusine, Integer servings, String ingredients, List<OrderedList> steps, NutritionInformation nutrition
        String author = userService.getUUID(email);
        int cooktime, preptime, servings, position, calories, sodium_in_mg;
        String title, cusine, ingredients, items;
        float cholesterol_in_mg, carbohydrates_in_grams, protein_in_grams;
        List<OrderedList> steps = new ArrayList<>();
        NutritionInformation nutrition;

        // cooktime checker
        if (!input.has("cook_time_in_min")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'cook_time_in_min\'"));
        } else {
            cooktime = input.getInt("cook_time_in_min");
            if (cooktime % 5 != 0) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Cook time should be the multiple of 5"));
            }
        }

        // preptime checker
        if (!input.has("prep_time_in_min")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'prep_time_in_min\'"));
        } else {
            preptime = input.getInt("prep_time_in_min");
            if (preptime % 5 != 0) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Prep time should be the multiple of 5"));
            }
        }

        // title checker
        if (!input.has("title")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'title\'"));
        } else {
            title = input.getString("title");
        }

        // cusine checker
        if (!input.has("cusine")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'cusine\'"));
        } else {
            cusine = input.getString("cusine");
        }

        // servings checker
        if (!input.has("servings")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'servings\'"));
        } else {
            servings = input.getInt("servings");
            if (servings < 1 || servings > 5) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Servings should in range [1, 5]"));
            }
        }

        // ingredients checker
        if (!input.has("ingredients")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'ingredients\'"));
        } else {
            ingredients = input.getJSONArray("ingredients").toString();
        }

        // steps checker
        if (!input.has("steps")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'steps\'"));
        } else {
            JSONArray temp = input.getJSONArray("steps");
            for (int i = 0; i < temp.length(); i++) {
                JSONObject ob = temp.getJSONObject(i);
                // position checker
                if (!ob.has("position")) {
                    return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under steps: \'position\'"));
                } else {
                    position = ob.getInt("position");
                }

                // items checker
                if (!ob.has("items")) {
                    return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under steps: \'items\'"));
                } else {
                    items = ob.getString("items");
                }
                OrderedList orderedList = recipieService.createOrderList(position, items);
                steps.add(orderedList);
            }
        }

        // nutrition_information checker
        if (!input.has("nutrition_information")) {
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json: \'nutrition_information\'"));
        } else {
            JSONObject ob = input.getJSONObject("nutrition_information");
            // calories checker
            if (!ob.has("calories")) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under nutrition_information: \'calories\'"));
            } else {
                calories = ob.getInt("calories");
            }

            // cholesterol_in_mg checker
            if (!ob.has("cholesterol_in_mg")) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under nutrition_information: \'cholesterol_in_mg\'"));
            } else {
                cholesterol_in_mg = ob.getFloat("cholesterol_in_mg");
            }

            // sodium_in_mg checker
            if (!ob.has("sodium_in_mg")) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under nutrition_information: \'sodium_in_mg\'"));
            } else {
                sodium_in_mg = ob.getInt("sodium_in_mg");
            }

            // carbohydrates_in_grams checker
            if (!ob.has("carbohydrates_in_grams")) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under nutrition_information: \'carbohydrates_in_grams\'"));
            } else {
                carbohydrates_in_grams = ob.getFloat("carbohydrates_in_grams");
            }

            // protein_in_grams checker
            if (!ob.has("protein_in_grams")) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing filed in json under nutrition_information: \'protein_in_grams\'"));
            } else {
                protein_in_grams = ob.getFloat("protein_in_grams");
            }

            nutrition = recipieService.createNutritionInfo(calories, cholesterol_in_mg, sodium_in_mg, carbohydrates_in_grams, protein_in_grams);
        }
        /*================finish input checking==================*/
        try {
            JSONObject res = recipieService.updateRecipie(id, cooktime, preptime, title, cusine, servings, ingredients, steps, nutrition);
            if(res == null) {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("update recipie failed"));
            }
            long time2 = System.currentTimeMillis();
            MatricsConfig.statsd.recordExecutionTime("recipe.update.api_time", time2-time1);
            return ResponseEntity.ok().headers(responseHeaders).body(res.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).headers(responseHeaders).body(jsonParser.getResponseMessage(e.getMessage()));
        }
    }

    /**
     * @param id
     * @param authorization
     * @return
     */
    @RequestMapping(value = "v1/recipe/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteRecipie(@PathVariable("id") String id, @RequestHeader(name = "Authorization") Optional<String> authorization) {
        Application.logger.info("make delete recipe api call");
        MatricsConfig.statsd.increment("recipe.delete");
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
        /*==================CHECK SAME AUTHOR============*/

        if (recipieService.getRecipie(id) == null) {
            return ResponseEntity.status(204).headers(responseHeaders).body(jsonParser.getResponseMessage("No such recipie for id : " + id));
        }

        if (!recipieService.sameAuthor(email, id)) {
            return ResponseEntity.status(401).headers(responseHeaders).body(jsonParser.getResponseMessage("Current user have no auth to delete that recipie"));
        }
        /*==================FINISH SAME AUTHOR===========*/

        try {
            if (recipieService.deleteRecipie(id)) {
                long time2 = System.currentTimeMillis();
                MatricsConfig.statsd.recordExecutionTime("recipe.delete.api_time", time2 - time1);
                return ResponseEntity.ok().headers(responseHeaders).body(jsonParser.getResponseMessage("Delete recipie : "+ id +" Success..."));
            } else {
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Delete recipie : "+ id +" Fail..."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).headers(responseHeaders).body(jsonParser.getResponseMessage(e.getMessage()));
        }
    }

    /**
     * @param id
     * @return
     */
    @RequestMapping(value = "v1/recipe/{id}", method = RequestMethod.GET)
    public ResponseEntity<String> getRecipie(@PathVariable("id") String id) {
        Application.logger.info("make get recipe api call");
        MatricsConfig.statsd.increment("recipe.get");
        long time1 = System.currentTimeMillis();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");

        if (recipieService.getRecipie(id) == null) {
            return ResponseEntity.status(204).headers(responseHeaders).body(jsonParser.getResponseMessage("No such recipie for id : " + id));
        }

        try {
            JSONObject res = recipieService.getRecipie(id);
            long time2 = System.currentTimeMillis();
            MatricsConfig.statsd.recordExecutionTime("recipe.get.api_time", time2-time1);
            return ResponseEntity.ok().headers(responseHeaders).body(res.toString());
        } catch (Exception e) {
            return ResponseEntity.status(500).headers(responseHeaders).body(jsonParser.getResponseMessage("get recipie information failed" + e.getMessage()));
        }
    }


    /**
     * @param authorization
     * @param file
     * @param id
     * @return
     */
    // upload image function
    @RequestMapping(value = "v1/recipe/{id}/image", method = RequestMethod.POST)
    public ResponseEntity<String> uploadImage(@RequestHeader(name = "Authorization") Optional<String> authorization, @RequestPart("file") MultipartFile file,@PathVariable("id") String id) {
        Application.logger.info("make upload image api call");
        MatricsConfig.statsd.increment("image.create");
        long time1 = System.currentTimeMillis();
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("Content-Type", "application/json");

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

        if(file.isEmpty()){
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Missing image filed"));
        }
        String contentType = file.getContentType();
        if(!contentType.equals("image/jpeg") && !contentType.equals("image/png")){
            return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("image filed should be *.jpg/png/jpeg"));
        }

        if (recipieService.getRecipie(id) == null) {
            return ResponseEntity.status(204).headers(responseHeaders).body(jsonParser.getResponseMessage("No such recipie for id : " + id));
        }

        Regions clientRegion = Regions.US_EAST_1;
        String bucket = "";
        String keyName = UUID.randomUUID().toString();
        File f = new File(System.getProperty("java.io.tmpdir")+"/" + file.getOriginalFilename());
        System.out.println(f.getAbsolutePath());
        try {
            AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
                    .withRegion(clientRegion)
                    .withCredentials(new ProfileCredentialsProvider())
                    .build();
            List<Bucket> buckets = s3Client.listBuckets();
            if(buckets == null || buckets.isEmpty()){
                return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Create image failed" ));
            }
            Bucket b = buckets.get(0);
            bucket = b.getName();
            // TransferManager processes all transfers asynchronously,
            // so this call returns immediately.

            file.transferTo(f);

            // create meta data
            ObjectMetadata metadata = new ObjectMetadata();
            FileInputStream fis = new FileInputStream(f);
            byte[] content_bytes = IOUtils.toByteArray(fis);
            String md5 = new String(org.apache.commons.codec.binary.Base64.encodeBase64(DigestUtils.md5(content_bytes)));

            metadata.setContentMD5(md5);
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(f.length());
            PutObjectRequest request = new PutObjectRequest(bucket, keyName, f);
//            request.setMetadata(metadata);
            System.out.println("Object upload started");
            long times3_1 = System.currentTimeMillis();
            s3Client.putObject(request);
            long times3_2 = System.currentTimeMillis();
            MatricsConfig.statsd.recordExecutionTime("image.create.s3_time", times3_2 - times3_1);
            // Optionally, wait for the upload to finish before continuing.
            System.out.println("Object upload complete");
            String Url = s3Client.getUrl(bucket,keyName).toString();
            S3Object ob = s3Client.getObject(bucket,keyName);
            JSONObject res = imageService.createImage(id,keyName,Url,metadata,ob,s3Client,bucket);
            if(res != null){
                long time2 = System.currentTimeMillis();
                MatricsConfig.statsd.recordExecutionTime("image.create.api_time", time2-time1);
                return ResponseEntity.ok().headers(responseHeaders).body(res.toString());
            }

        } catch (AmazonServiceException e) {
            // The call was transmitted successfully, but Amazon S3 couldn't process
            // it, so it returned an error response.
            e.printStackTrace();
        } catch (SdkClientException e) {
            // Amazon S3 couldn't be contacted for a response, or the client
            // couldn't parse the response from Amazon S3.
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(f.exists()){
                f.delete();
            }
        }
        return ResponseEntity.status(400).headers(responseHeaders).body(jsonParser.getResponseMessage("Create image failed" ));
    }
}
