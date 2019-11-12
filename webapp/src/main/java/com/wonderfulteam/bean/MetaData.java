package com.wonderfulteam.bean;

import javax.persistence.*;

/**
 * Created by Qixiang Zhou on 2019-10-29 18:25
 */
@Entity
@Table
public class MetaData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
