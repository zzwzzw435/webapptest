package com.wonderfulteam.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Qixiang Zhou on 2019-09-30 01:47
 */

public class AuthTest {
    Auth a = new Auth();
    private String encoded = "Basic emhvdXFpeGlhbmcyMDEwQGhvdG1haWwuY29tOlBhc3NXMCgqKQ==";

    @Test
    public void saltGenerator() {
        String salt = a.saltGenerator();
        System.out.println(salt);
    }

    @Test
    public void correctPass() {
        String salt = a.saltGenerator();
        String pass = "passW0RD(*)";
        String encodepass = a.encodePass(pass, salt);
        assertTrue(a.correctPass(pass, salt, encodepass));
    }

    @Test
    public void encodePass() {
        String salt = a.saltGenerator();
        String pass = "passW0RD(*)";
        String pass2 = "Passw0rd(*)";
        String encodepass = a.encodePass(pass, salt);
        String encodepass2 = a.encodePass(pass, salt);
        String encodepass3 = a.encodePass(pass2, salt);
        assertEquals(encodepass, encodepass2);
        assertNotEquals(encodepass, encodepass3);
    }

    @Test
    public void verifyPassword() {
        String pass1 = ""; // empty
        String pass2 = "Ax_0"; // shorter then 8
        String pass3 = "Aa_0aaaaaaaaa"; // have repetitive
        String pass4 = "a1b2c3ANH"; // no special
        String pass5 = "-0=2;4abc"; // no upper case
        String pass6 = "abcABC-()&"; // no num
        String pass7 = "AHR-()8349"; // no lower case
        String pass8 = "PassW0(42)*"; // valid password
        assertTrue(a.verifyPassword(pass8));
        assertFalse(a.verifyPassword(pass1));
        assertFalse(a.verifyPassword(pass2));
        assertFalse(a.verifyPassword(pass3));
        assertTrue(a.verifyPassword(pass4));
        assertTrue(a.verifyPassword(pass5));
        assertTrue(a.verifyPassword(pass6));
        assertTrue(a.verifyPassword(pass7));
    }

    @Test
    public void validEmail() {
        String mail1 = "";
        String mail2 = "1324";
        String mail3 = "1234@";
        String mail4 = "1234@adf";
        String mail5 = "1234@adf.";
        String mail6 = "1234@adf.com";
        assertTrue(a.validEmail(mail6));
        assertFalse(a.validEmail(mail1));
        assertFalse(a.validEmail(mail2));
        assertFalse(a.validEmail(mail3));
        assertFalse(a.validEmail(mail4));
        assertFalse(a.validEmail(mail5));
    }

    @Test
    public void checkHeaderAuth() {
        String[] array = a.checkHeaderAuth(encoded);
        assertEquals(array[0], "zhouqixiang2010@hotmail.com");
        assertEquals(array[1], "PassW0(*)");
    }

    @Test
    public void getFromBASE64() {
        String ans = a.getFromBASE64("emhvdXFpeGlhbmcyMDEwQGhvdG1haWwuY29tOlBhc3NXMCgqKQ==");
        assertEquals(ans, "zhouqixiang2010@hotmail.com:PassW0(*)");
    }
}