package org.uteq.sgacfinal.util;
import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS    = "0123456789";
    private static final String ALL_CHARS = UPPERCASE + LOWERCASE + DIGITS;
    private static final int PASSWORD_LENGTH = 12;
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        char[] password = new char[PASSWORD_LENGTH];

        password[0] = UPPERCASE.charAt(random.nextInt(UPPERCASE.length()));
        password[1] = LOWERCASE.charAt(random.nextInt(LOWERCASE.length()));
        password[2] = DIGITS.charAt(random.nextInt(DIGITS.length()));

        for (int i = 3; i < PASSWORD_LENGTH; i++)
            password[i] = ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length()));

        for (int i = PASSWORD_LENGTH - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = password[i];
            password[i] = password[j];
            password[j] = tmp;
        }
        return new String(password);
    }
}