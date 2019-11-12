package com.wonderfulteam.service;

import com.wonderfulteam.bean.Image;
import com.wonderfulteam.bean.NutritionInformation;
import com.wonderfulteam.bean.OrderedList;
import com.wonderfulteam.bean.Recipie;
import com.wonderfulteam.config.MatricsConfig;
import com.wonderfulteam.dao.RecipieRepository;
import com.wonderfulteam.util.Auth;
import com.wonderfulteam.util.JSONParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

/**
 * Created by Qixiang Zhou on 2019-09-30 01:42
 */
@Service
public class RecipieService {

    @Autowired
    JSONParser jsonParser;
    @Autowired
    Auth auth;
    @Autowired
    RecipieRepository recipieRepository;
    @Autowired
    UserService userService;

    public JSONObject createRecipie(String author, Integer cooktime, Integer preptime, String title, String cusine, Integer servings, String ingredients, List<OrderedList> steps, NutritionInformation nutrition) {
        Recipie recipe = new Recipie();
        recipe.setUuid(UUID.randomUUID().toString());
        recipe.setCreated_ts(new Date());
        recipe.setUpdated_ts(new Date());
        recipe.setAuthor_id(author);
        recipe.setCook_time_in_min(cooktime);
        recipe.setPrep_time_in_min(preptime);
        recipe.setTotal_time_in_min(cooktime + preptime);
        recipe.setTitle(title);
        recipe.setCusine(cusine);
        recipe.setServings(servings);
        recipe.setIngredients(ingredients);
        recipe.setSteps(steps);
        recipe.setNutrition_information(nutrition);
        long dbtime1 = System.currentTimeMillis();
        Recipie recipedone = recipieRepository.save(recipe);
        long dbtime2 = System.currentTimeMillis();
        MatricsConfig.statsd.recordExecutionTime("recipe.create.db_time", dbtime2 - dbtime1);
        if (recipedone != null) {
            return jsonParser.parseRecipe(recipedone);
        }
        return null;
    }

    public JSONObject updateRecipie(String id, Integer cooktime, Integer preptime, String title, String cusine, Integer servings, String ingredients, List<OrderedList> steps, NutritionInformation nutrition) {
        Recipie recipie = getRecipieObject(id);
        if(recipie == null) return null;

        if(cooktime != null){
            recipie.setCook_time_in_min(cooktime);
        }
        if(preptime != null){
            recipie.setPrep_time_in_min(preptime);
        }
        if(title != null){
            recipie.setTitle(title);
        }
        if(cusine != null){
            recipie.setCusine(cusine);
        }
        if(servings != null){
            recipie.setServings(servings);
        }
        if(ingredients != null){
            recipie.setIngredients(ingredients);
        }
        if(steps != null){
            recipie.setSteps(steps);
        }
        if(nutrition != null){
            recipie.setNutrition_information(nutrition);
        }
        recipie.setTotal_time_in_min(recipie.getCook_time_in_min() + recipie.getPrep_time_in_min());
        recipie.setUpdated_ts(new Date());
        long dbtime1 = System.currentTimeMillis();
        Recipie recipiedone = recipieRepository.save(recipie);
        long dbtime2 = System.currentTimeMillis();
        MatricsConfig.statsd.recordExecutionTime("recipe.update.db_time", dbtime2 - dbtime1);
        if(recipiedone != null){
            return jsonParser.parseRecipe(recipiedone);
        }
        return null;
    }

    @Transactional
    public boolean deleteRecipie(String id) {

        if (!recipieRepository.existsByUuid(id)) {
            return false;
        }
        long dbtime1 = System.currentTimeMillis();
        recipieRepository.deleteByUuid(id);
        long dbtime2 = System.currentTimeMillis();
        MatricsConfig.statsd.recordExecutionTime("recipe.delete.db_time", dbtime2 - dbtime1);
        return true;
    }

    public JSONObject getRecipie(String id) {
        Recipie recipe = getRecipieObject(id);
        if (recipe == null) return null;
        return jsonParser.parseRecipe(recipe);
    }

    /**
     * Private method used to get the object
     * @param uuid Random generated id (unique)
     * */
    private Recipie getRecipieObject(String uuid) {
        long dbtime1 = System.currentTimeMillis();
        Optional<Recipie> recipie = recipieRepository.findByUuid(uuid);
        long dbtime2 = System.currentTimeMillis();
        MatricsConfig.statsd.recordExecutionTime("recipe.get.db_time", dbtime2 - dbtime1);
        if (recipie.isPresent()) {
            return recipie.get();
        }
        return null;
    }

    public NutritionInformation createNutritionInfo(int calories, float cholesterol_in_mg, int sodium_in_mg, float carbohydrates_in_grams, float protein_in_grams) {
        NutritionInformation nutritionInformation = new NutritionInformation();
        nutritionInformation.setCalories(calories);
        nutritionInformation.setCholesterol_in_mg(cholesterol_in_mg);
        nutritionInformation.setSodium_in_mg(sodium_in_mg);
        nutritionInformation.setCarbohydrates_in_grams(carbohydrates_in_grams);
        nutritionInformation.setProtein_in_grams(protein_in_grams);
        return nutritionInformation;
    }

    public OrderedList createOrderList(int position, String items) {
        OrderedList orderedList = new OrderedList();
        orderedList.setPosition(position);
        orderedList.setItems(items);
        return orderedList;
    }

    public boolean sameAuthor(String authoremail, String recipieuuid) {
        Recipie recipie = getRecipieObject(recipieuuid);
        String authoruuid = userService.getUUID(authoremail);
        if (recipie.getAuthor_id().equals(authoruuid)) {
            return true;
        }
        return false;
    }

    public boolean hasImage(String imageID, String recipeid) {
        Recipie recipie = getRecipieObject(recipeid);
        Image image = recipie.getImage();
        if (image == null) return false;
        if (!image.getUuid().equals(imageID)) return false;
        return true;
    }
}
