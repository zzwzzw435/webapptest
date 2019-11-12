package com.wonderfulteam.bean;

import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by Qixiang Zhou on 2019-09-29 23:20
 */
@Entity
@Table
public class Recipie {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String uuid;
    private Date created_ts;
    private Date updated_ts;
    private String author_id;
    private int cook_time_in_min;
    private int prep_time_in_min;
    private int total_time_in_min;
    private String title;
    private String cusine;
    private int servings;
    private String ingredients;
    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderedList> steps;
    @OneToOne(cascade = CascadeType.ALL)
    private NutritionInformation nutrition_information;
    @OneToOne(cascade = CascadeType.ALL)
    private Image image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Date getCreated_ts() {
        return created_ts;
    }

    public void setCreated_ts(Date created_ts) {
        this.created_ts = created_ts;
    }

    public Date getUpdated_ts() {
        return updated_ts;
    }

    public void setUpdated_ts(Date updated_ts) {
        this.updated_ts = updated_ts;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public int getCook_time_in_min() {
        return cook_time_in_min;
    }

    public void setCook_time_in_min(int cook_time_in_min) {
        this.cook_time_in_min = cook_time_in_min;
    }

    public int getPrep_time_in_min() {
        return prep_time_in_min;
    }

    public void setPrep_time_in_min(int prep_time_in_min) {
        this.prep_time_in_min = prep_time_in_min;
    }

    public int getTotal_time_in_min() {
        return total_time_in_min;
    }

    public void setTotal_time_in_min(int total_time_in_min) {
        this.total_time_in_min = total_time_in_min;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCusine() {
        return cusine;
    }

    public void setCusine(String cusine) {
        this.cusine = cusine;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public List<OrderedList> getSteps() {
        return steps;
    }

    public void setSteps(List<OrderedList> steps) {
        this.steps = steps;
    }

    public NutritionInformation getNutrition_information() {
        return nutrition_information;
    }

    public void setNutrition_information(NutritionInformation nutrition_information) {
        this.nutrition_information = nutrition_information;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
