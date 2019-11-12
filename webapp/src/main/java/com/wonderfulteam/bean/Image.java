package com.wonderfulteam.bean;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Qixiang Zhou on 2019-10-23 14:57
 */
@Entity
@Table
public class Image {
    @Id
    @GeneratedValue(strategy =  GenerationType.AUTO)
    private int id;
    private String uuid;
    private String url;

    // metadata below , should be get from s3 bucket
    private Date date;
    private String MD5;
    private long size;
    private String type;
    @OneToOne(cascade = CascadeType.ALL)
    private MetaData metaData;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMD5() {
        return MD5;
    }

    public void setMD5(String MD5) {
        this.MD5 = MD5;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

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

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public MetaData getMetaData() {
        return metaData;
    }

    public void setMetaData(MetaData metaData) {
        this.metaData = metaData;
    }
}
