package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class AuthTokenUtilsTest {

    private final ApplicationUser TESTUSER = new ApplicationUser()
        .setId(1L)
        .setNickname("maxmuster")
        .setEmail("max.mustermann@test.at");

    @Test
    public void testCreateTokenIsValid() throws Exception {
        String authToken = AuthTokenUtils.createToken(TESTUSER.getId(), TESTUSER.getNickname());
        assertTrue(AuthTokenUtils.isValid(authToken));
    }

    @Test
    public void testExpiredTokenIsInvalid() throws Exception {
        String expiredToken = "Token eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJCaXRlQnVkZHktU2VydmVyIiwic3ViIjoidGVzdHVzZXIiLCJhdWQiOiJCaXRlQnVkZHktQXBwIiwiZXhwIjoxNzAxNjYxNjQ1LCJpYXQiOjE3MDE2NTQ0NDUsImp0aSI6IjEifQ.Kp7iQXqSdLlxNVTiZuf2sZz_0QhUDT_EAV7olUCQDc_CkR8BjZgK71tBosG9Nl6FSKx3ezY7NUDLN8c0dwq28-cY0u7LwcEydacGq46-5ys6zbsU0CnLza9gHprl-uLH5oyTALRRqSXpx_o4HkvXDpTrBQghqsDiw9qLKseDAWjnba68n57GLwtBub9zyiwSP737DqklYfCdI5tmQ1C84tnLv2LMJ0oxvowTtcmEAkJ1PfMcF-hxZj6WlFOBUd5jzt5LlQc65azKazWgyf8DwFDv-_6D8n_Msc_jT_LHdPRKJ-d3rsBnOKg9GXSK_KkJKwql-rISLGWJ29WHkmOjkg";
        assertFalse(AuthTokenUtils.isValid(expiredToken));
    }

    @Test
    public void testInvalidFormatTokenIsInvalid() throws Exception {
        assertFalse(AuthTokenUtils.isValid("notAToken"));
    }

    @Test
    public void testGetExpirationDate() {
        String authToken = AuthTokenUtils.createToken(TESTUSER.getId(), TESTUSER.getNickname());
        Assertions.assertNotNull(AuthTokenUtils.getExpirationDate(authToken));
    }

}
