package com.maxleap.pandora.data.support.utils;


import com.maxleap.pandora.core.exception.KeyInvalidException;

/**
 *
 * @author sneaky
 * @since 2.0.0
 */
public class ValidationUtils {
    public static void validateKeyFirstCharacter(char key) {
        int i = key;
        if (!(i >= 65 && i <= 90 || i >= 97 && i <= 122 || i == 42 || i == 95 || i <= 57 && i >= 48 || i == 36)) {
            throw new KeyInvalidException(String.format("field name should be start with A-Z, a-z, _, 0-9. [%s]", key));
        }
    }
}
