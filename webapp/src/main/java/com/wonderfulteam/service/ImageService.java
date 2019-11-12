package com.wonderfulteam.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.wonderfulteam.bean.Image;
import com.wonderfulteam.bean.MetaData;
import com.wonderfulteam.bean.Recipie;
import com.wonderfulteam.config.MatricsConfig;
import com.wonderfulteam.dao.ImageRepository;
import com.wonderfulteam.dao.RecipieRepository;
import com.wonderfulteam.util.Auth;
import com.wonderfulteam.util.JSONParser;
import org.aspectj.apache.bcel.classfile.Module;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Qixiang Zhou on 2019-10-23 17:35
 */
@Service
public class ImageService {

    @Autowired
    JSONParser jsonParser;
    @Autowired
    Auth auth;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    RecipieRepository recipieRepository;


    public JSONObject getImageInfo(String imageid) {
        Image image = getImage(imageid);
        if (image == null) return null;
        return jsonParser.parseImage(image);
    }

    public JSONObject createImage(String recipieid, String imageid, String url, ObjectMetadata om, S3Object ob, AmazonS3 s3Client, String bucket) {
        if(!recipieRepository.existsByUuid(recipieid)){
            return null;
        }
        Optional<Recipie> recipie = recipieRepository.findByUuid(recipieid);
        Recipie recipie1 = recipie.get();

        if(recipie1.getImage() != null){
            s3Client.deleteObject(new DeleteObjectRequest(bucket,recipie1.getImage().getUuid()));
        }

        Image image;
        if (recipie1.getImage() == null) {
            image = new Image();
        } else {
            image = recipie1.getImage();
        }
        MetaData metaData = new MetaData();
        metaData.setContent(ob.getObjectMetadata().getETag() + ob.getObjectMetadata().getContentType() + ob.getObjectMetadata().getServerSideEncryption());
        image.setUuid(imageid);
        image.setUrl(url);
        image.setDate(s3Client.getObjectMetadata(bucket, imageid).getLastModified());
        image.setMD5(om.getContentMD5());
        image.setSize(om.getContentLength());
        image.setType(om.getContentType());
        image.setMetaData(metaData);
        recipie1.setImage(image);
        long dbtime1 = System.currentTimeMillis();
        recipieRepository.save(recipie1);
        long dbtime2 = System.currentTimeMillis();
        MatricsConfig.statsd.recordExecutionTime("image.create.dp_time", dbtime2 - dbtime1);
        return jsonParser.parseImage(image);
    }

    @Transactional
    public boolean deleteImage(String recipieid, String imageid,AmazonS3 s3Client,String bucket) {
        if (!imageRepository.existsByUuid(imageid)) {
            return false;
        }
        Optional<Recipie> recipie = recipieRepository.findByUuid(recipieid);
        Recipie recipie1 = recipie.get();
        long timed1 =System.currentTimeMillis();
        s3Client.deleteObject(new DeleteObjectRequest(bucket,imageid));
        long timed2 =System.currentTimeMillis();
        MatricsConfig.statsd.recordExecutionTime("image.delete.s3", timed2 - timed1);

        long dbtime1 = System.currentTimeMillis();
        imageRepository.deleteByUuid(imageid);
        recipie1.setImage(null);
        recipieRepository.save(recipie1);
        long dbtime2 = System.currentTimeMillis();
        MatricsConfig.statsd.recordExecutionTime("image.delete.db_time", dbtime2 - dbtime1);
        return true;
    }

    private Image getImage(String id) {
        long dbtime1 = System.currentTimeMillis();
        Optional<Image> image = imageRepository.findByUuid(id);
        long dbtime2 = System.currentTimeMillis();
        MatricsConfig.statsd.recordExecutionTime("image.get.db_time", dbtime2 - dbtime1);
        if (image.isPresent()) {
            return image.get();
        }
        return null;
    }
}
