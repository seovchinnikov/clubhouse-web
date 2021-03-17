package edu.clubhouseapi.util;

import edu.clubhouseapi.config.AppConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

@Service
public class ByteUtils {

    @Autowired
    protected AppConfigProperties appConfigProperties;

    public String randomCookieId() {
        if (appConfigProperties.isMockEnable()) {
            return appConfigProperties.getMockCookie();
        }
        return randomHex(21) + new Random().ints(1, 0, 10).toArray()[0];

    }

    public String randomHex(int num) {
        byte[] nonce = new byte[num];
        new SecureRandom().nextBytes(nonce);
        return convertBytesToHex(nonce);

    }

    private String convertBytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte temp : bytes) {
            result.append(String.format("%02x", temp));
        }
        return result.toString();
    }

    public String fixedUuidFromString(String base) {
        return uuid4FixedOnRandomBytes(base.getBytes()).toString().toUpperCase();// UUID.nameUUIDFromBytes(base.getBytes()).toString();
    }

    public UUID uuid4FixedOnRandomBytes(byte[] bytes) {
        // SecureRandom ng = new SecureRandom();
        // byte[] randomBytes = new byte[16];
        //
        // ng.nextBytes(randomBytes);

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException nsae) {
            throw new InternalError("MD5 not supported", nsae);
        }
        byte[] md5Bytes = md.digest(bytes);
        byte[] randomBytes = md5Bytes;
        randomBytes[6] &= 0x0f; /* clear version */
        randomBytes[6] |= 0x40; /* set to version 4 */
        randomBytes[8] &= 0x3f; /* clear variant */
        randomBytes[8] |= 0x80; /* set to IETF variant */

        long msb = 0;
        long lsb = 0;
        assert randomBytes.length == 16 : "data must be 16 bytes in length";
        for (int i = 0; i < 8; i++)
            msb = (msb << 8) | (randomBytes[i] & 0xff);
        for (int i = 8; i < 16; i++)
            lsb = (lsb << 8) | (randomBytes[i] & 0xff);
        return new UUID(msb, lsb);

    }
}
