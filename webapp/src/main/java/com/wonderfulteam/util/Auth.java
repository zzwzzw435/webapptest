package com.wonderfulteam.util;


import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Decoder;

/**
 * Created by Qixiang Zhou on 2019-09-28 23:43
 */
@Component
public class Auth {
    /**
     * Generate a Salt value
     * @return generated salt string
     * */
    public String saltGenerator() {
        return BCrypt.gensalt();
    }

    /**
     * Check the password input is same as stored
     * @param pass plaintext password
     * @param salt salt from database
     * @param encodedpass hashed password from database
     * */
    public boolean correctPass(String pass, String salt, String encodedpass) {
        return encodePass(pass, salt).equals(encodedpass);
    }

    /**
     * @param pass plaintext password
     * @param salt salt string generated for this user
     * @return sha256 hashed value with pass and salt
     * */
    public String encodePass(String pass, String salt) {
        return BCrypt.hashpw(pass, salt);
    }

    /**
     * Check the password is valid format
     *
     * @param password plaintext password
     * @return
     */
    public boolean verifyPassword(String password) {
        // password length should longer then 8
        if (password == null || password.length() <= 8) {
            return false;
        }

        if (checkCharTypes(password) && checkRepeatSubstring(password)) {
            return true;
        }
        return false;
    }

    /**
     * Password should contain AT LEAST 3 with UpperCase char, lowercase char, digits and other special characters
     *
     * @param password plaintext password
     * @return
     */
    public boolean checkCharTypes(String password) {
        int upperCase = 0, lowerCase = 0, digit = 0, other = 0;

        for (Character ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) {
                upperCase = 1;
            } else if (Character.isLowerCase(ch)) {
                lowerCase = 1;
            } else if (Character.isDigit(ch)) {
                digit = 1;
            } else {
                other = 1;
            }
        }

        if (upperCase + lowerCase + digit + other >= 3) {
            return true;
        }
        return false;
    }

    /**
     * Shouldn't contain repetitive content
     *
     * @param password
     * @return
     */
    public boolean checkRepeatSubstring(String password) {
        for (int i = 0; i < password.length() - 3; i++) {
            String s = password.substring(i, i + 3);
            String tempStr = password.substring(i + 3, password.length());
            if (tempStr.contains(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return the input email is valid format
     * @param email String email
     * @return
     * */
    public boolean validEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    public String[] checkHeaderAuth(String auth){

        if ((auth != null) && (auth.length() > 6)) {
            auth = auth.substring(6, auth.length());
            String decodedAuth = getFromBASE64(auth);
            String[] temp = decodedAuth.split(":");
            return temp;
        }
        return null;
    }

    public String getFromBASE64(String auth) {
        if (auth == null)
            return null;
        BASE64Decoder decoder = new BASE64Decoder();
        try {
            byte[] b = decoder.decodeBuffer(auth);
            return new String(b);
        } catch (Exception e) {
            return null;
        }
    }
}
