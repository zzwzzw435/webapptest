package com.wonderfulteam.service;

import com.wonderfulteam.bean.User;
import com.wonderfulteam.config.MatricsConfig;
import com.wonderfulteam.dao.UserRepository;
import com.wonderfulteam.util.Auth;
import com.wonderfulteam.util.JSONParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Qixiang Zhou on 2019-09-29 00:23
 */
@Service
public class UserService {

    @Autowired
    JSONParser jsonParser;
    @Autowired
    Auth auth;
    @Autowired
    UserRepository userRepository;

    public JSONObject createUser(String email,String password,String first_name,String last_name) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateKeyException(email);
        }
        String salt = auth.saltGenerator();
        String pass = auth.encodePass(password, salt);
        User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setEmail(email);
        user.setPassword(pass);
        user.setFirstName(first_name);
        user.setLastName(last_name);
        user.setAccount_created(new Date());
        user.setAccount_updated(new Date());
        user.setSalt(salt);
        long dbtime1 = System.currentTimeMillis();
        User userdone = userRepository.save(user);
        long dbtime2 = System.currentTimeMillis();
        MatricsConfig.statsd.recordExecutionTime("user.create_db_time", dbtime2 - dbtime1);
        if (userdone != null) {
            return jsonParser.parseUser(userdone);
        }
        return null;
    }

    public JSONObject updateUser(String email,String password,String first_name,String last_name){
        User user = getUser(email);
        if (user == null) return null; // it is actually unnecessary since previous logic make sure null will not happen
        if (password != null) {
            user.setPassword(auth.encodePass(password, user.getSalt()));
        }
        if (first_name != null) {
            user.setFirstName(first_name);
        }
        if (last_name != null) {
            user.setLastName(last_name);
        }
        user.setAccount_updated(new Date());
        long dbtime1 = System.currentTimeMillis();
        User userdone = userRepository.save(user);
        long dbtime2 = System.currentTimeMillis();
        MatricsConfig.statsd.recordExecutionTime("user.update.db_time", dbtime2 - dbtime1);
        if(userdone != null){
            return jsonParser.parseUser(userdone);
        }
        return null;
    }

    public JSONObject getUserInfo(String email) {
        User user = getUser(email);
        if (user == null) return null;
        return jsonParser.parseUser(user);
    }

    private User getUser(String email){
        long dbtime1 = System.currentTimeMillis();
        Optional<User> user = userRepository.findByEmail(email);
        long dbtime2 = System.currentTimeMillis();
        MatricsConfig.statsd.recordExecutionTime("user.get.db_time", dbtime2 - dbtime1);
        if (user.isPresent()) {
            return user.get();
        }
        return null;
    }

    public boolean login(String email, String password) {
        User user = getUser(email);
        if (user == null) return false;
        return auth.correctPass(password, user.getSalt(), user.getPassword());
    }

    public String getUUID(String email) {
        User user = getUser(email);
        if (user != null) {
            return user.getUuid();
        }
        return null;
    }
}
