package com.wonderfulteam.dao;

import com.wonderfulteam.bean.Recipie;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by Qixiang Zhou on 2019-09-29 23:40
 */

@Repository
public interface RecipieRepository extends CrudRepository<Recipie, Integer> {
    public Optional<Recipie> findByUuid(String uuid);
    public boolean existsByUuid(String uuid);
    public void deleteByUuid(String uuid);
}
