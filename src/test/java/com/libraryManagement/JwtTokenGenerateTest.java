package com.libraryManagement;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

public class JwtTokenGenerateTest
{

    @Test
    public void generateToken()
    {
        SecretKey key = Jwts.SIG.HS256.key().build();
        String encoded = Base64.getEncoder().encodeToString(key.getEncoded());
        System.out.printf("\nKey = [ %s ]\n", encoded);
    }
}
