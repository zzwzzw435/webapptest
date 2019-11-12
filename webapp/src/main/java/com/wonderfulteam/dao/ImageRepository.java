package com.wonderfulteam.dao;

import com.wonderfulteam.bean.Image;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Qixiang Zhou on 2019-10-23 17:33
 */
@Repository
public interface ImageRepository extends CrudRepository<Image, Integer> {
    public Optional<Image> findByUuid(String uuid);
    public boolean existsByUuid(String uuid);
    public void deleteByUuid(String uuid);
}
