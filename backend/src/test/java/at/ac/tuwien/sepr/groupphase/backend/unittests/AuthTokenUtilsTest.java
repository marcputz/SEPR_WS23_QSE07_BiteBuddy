package at.ac.tuwien.sepr.groupphase.backend.unittests;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.utils.AuthTokenUtils;
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

    private final ApplicationUser testUser = new ApplicationUser()
        .setId(1L)
        .setNickname("maxmuster")
        .setEmail("max.mustermann@test.at");

    @Test
    public void testCreateToken_WithValidData_Returns() throws Exception {
        String authToken = AuthTokenUtils.createToken(testUser.getId(), testUser.getNickname(), testUser.getEmail());
        assertTrue(AuthTokenUtils.isValid(authToken));
    }

    @Test
    public void testIsValid_WithExpiredToken_ReturnsFalse() throws Exception {
        String expiredToken = "Token eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJpc3MiOiJCaXRlQnVkZHktU2VydmVyIiwic3ViIjoidGVzdHVzZXIiLCJhdWQiOiJCaXRlQnVkZHktQXBwI"
            + "iwiZXhwIjoxNzAxNjYxNjQ1LCJpYXQiOjE3MDE2NTQ0NDUsImp0aSI6IjEifQ.Kp7iQXqSdLlxNVTiZuf2sZz_0QhUDT_EAV7olUCQDc_CkR8BjZgK71tBosG9Nl6FSKx3ezY7NUDLN8c0dw"
            + "q28-cY0u7LwcEydacGq46-5ys6zbsU0CnLza9gHprl-uLH5oyTALRRqSXpx_o4HkvXDpTrBQghqsDiw9qLKseDAWjnba68n57GLwtBub9zyiwSP737DqklYfCdI5tmQ1C84tnLv2LMJ0oxvo"
            + "wTtcmEAkJ1PfMcF-hxZj6WlFOBUd5jzt5LlQc65azKazWgyf8DwFDv-_6D8n_Msc_jT_LHdPRKJ-d3rsBnOKg9GXSK_KkJKwql-rISLGWJ29WHkmOjkg";
        assertFalse(AuthTokenUtils.isValid(expiredToken));
    }

    @Test
    public void testIsValid_InvalidFormatToken_ReturnsFalse() throws Exception {
        assertFalse(AuthTokenUtils.isValid("notAToken"));
    }

    @Test
    public void testGetExpirationDate_WithValidToken_Returns() {
        String authToken = AuthTokenUtils.createToken(testUser.getId(), testUser.getNickname(), testUser.getEmail());
        Assertions.assertNotNull(AuthTokenUtils.getExpirationDate(authToken));
    }

}
