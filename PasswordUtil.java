package com.onlinmart.onlinmart.util;
import org.mindrot.jbcrypt.BCrypt;
public class PasswordUtil {
    public static String hash(String plain) { return BCrypt.hashpw(plain, BCrypt.gensalt(10)); }
    public static boolean check(String plain, String hashed) {
        if (plain == null || hashed == null) return false;
        try { return BCrypt.checkpw(plain, hashed); } catch (Exception e) { return false; }
    }
}
