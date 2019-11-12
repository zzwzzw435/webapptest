package com.wonderfulteam.util;

import com.wonderfulteam.bean.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Component
public class JSONParser {

    //parse user to Json object
    public JSONObject parseUser(User u) {
        JSONObject json = new JSONObject(new LinkedHashMap<>());
        String pattern = "EEEEE dd MMMMM yyyy HH:mm:ss.SSSZ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        json.put("id", u.getUuid());
        json.put("first_name", u.getFirstName());
        json.put("last_name", u.getLastName());
        json.put("email_address", u.getEmail());
        json.put("account_created", simpleDateFormat.format(u.getAccount_created()));
        json.put("account_updated", simpleDateFormat.format(u.getAccount_updated()));
        System.out.println("json:::::" + json.toString());
        return json;
    }

    //get response json object
    public String getResponseMessage(String s) {
        JSONObject response = new JSONObject();
        response.put("message", s);

        return response.toString();
    }

    public List<OrderedList>  getSteps(JSONObject js){
        if(js == null){
            return null;
        }
        List<OrderedList> step = new ArrayList<>();
        JSONArray ja = js.getJSONArray("steps");
        for(int i = 0; i < ja.length(); i++){
            JSONObject jo = (JSONObject) ja.get(i);
            OrderedList ol = new OrderedList();
            ol.setPosition(jo.getNumber("position").intValue());
            ol.setItems(jo.getString("items"));
            step.add(ol);
        }
        return step;
    }

    public NutritionInformation getNutritionInformation(JSONObject js){
        if(js == null){
            return null;
        }
        NutritionInformation nu = new NutritionInformation();
        JSONObject jo = js.getJSONObject("nutrition_information");
        nu.setCalories(jo.getNumber("calories").intValue());
        nu.setCholesterol_in_mg(jo.getNumber("cholesterol_in_mg").intValue());
        nu.setCarbohydrates_in_grams(jo.getNumber("carbohydrates_in_grams").intValue());
        nu.setProtein_in_grams(jo.getNumber("protein_in_grams").intValue());
        return nu;
    }

    //parse recipe to Json Object
    public JSONObject parseRecipe(Recipie r){
        JSONObject json = new JSONObject();
        String pattern = "EEEEE dd MMMMM yyyy HH:mm:ss.SSSZ";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        json.put("id", r.getUuid());
        json.put("created_ts", simpleDateFormat.format(r.getCreated_ts()));
        json.put("updated_ts", simpleDateFormat.format(r.getUpdated_ts()));
        json.put("author_id", r.getAuthor_id());
        json.put("cook_time_in_min", r.getCook_time_in_min());
        json.put("prep_time_in_min", r.getPrep_time_in_min());
        json.put("total_time_in_min",r.getTotal_time_in_min());
        json.put("title",r.getTitle());
        json.put("cusine",r.getCusine());
        json.put("servings",r.getServings());
        json.put("ingredients",new JSONArray(r.getIngredients()));

        JSONArray steps = new JSONArray();
        for(OrderedList ol : r.getSteps()){
            JSONObject orderList = new JSONObject();
            orderList.put("position", ol.getPosition());
            orderList.put("items", ol.getItems());
            steps.put(orderList);
        }
        json.put("steps", steps);

        JSONObject nutritionInfo = new JSONObject();
        nutritionInfo.put("calories", r.getNutrition_information().getCalories());
        nutritionInfo.put("cholesterol_in_mg", r.getNutrition_information().getCholesterol_in_mg());
        nutritionInfo.put("sodium_in_mg", r.getNutrition_information().getSodium_in_mg());
        nutritionInfo.put("carbohydrates_in_grams", r.getNutrition_information().getCarbohydrates_in_grams());
        nutritionInfo.put("protein_in_grams", r.getNutrition_information().getProtein_in_grams());
        json.put("nutrition_information", nutritionInfo);

        return json;
    }

    public JSONObject parseImage(Image i) {
        JSONObject json = new JSONObject();
        json.put("id" , i.getUuid());
        json.put("url", i.getUrl());

        return json;
    }
}
