package com.wonderfulteam.bean;

import javax.persistence.*;

/**
 * Created by Qixiang Zhou on 2019-09-29 23:13
 */
@Entity
@Table
public class OrderedList {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private int position;
    private String items;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getItems() {
        return items;
    }

    public void setItems(String items) {
        this.items = items;
    }
}
