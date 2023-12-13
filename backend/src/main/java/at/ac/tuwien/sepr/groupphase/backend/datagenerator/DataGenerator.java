package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.entity.PasswordResetRequest;
import at.ac.tuwien.sepr.groupphase.backend.repository.PasswordResetRequestRepository;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.PasswordResetService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.List;

@Profile("generateData")
@Component
public class DataGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final PasswordResetRequestRepository passwordResetRepository;

    public DataGenerator(UserRepository userRepository, PasswordResetRequestRepository passwordResetRepository) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
    }

    @PostConstruct
    public void generateData() {
        // Generate data here
        generateUserData();
    }

    private void generateUserData() {
        if (userRepository.findAll().size() > 0) {
            ApplicationUser testUser1 = userRepository.findByNickname("maxmuster");
            if (testUser1 != null) {
                passwordResetRepository.deleteByUser(testUser1);
                userRepository.deleteById(testUser1.getId());
            }
            ApplicationUser testUser2 = userRepository.findByNickname("marcputz");
            if (testUser2 != null) {
                passwordResetRepository.deleteByUser(testUser2);
                userRepository.deleteById(testUser2.getId());
            }
        }

        LOGGER.debug("generating default test users");

        ApplicationUser user1 = new ApplicationUser().setId(-1L).setEmail("max.mustermann@test.at")
            .setPasswordEncoded(PasswordEncoder.encode("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "max.mustermann@test.at")) // "password"
            .setNickname("maxmuster");
        LOGGER.debug("saving user '" + user1.getNickname() + "'");
        userRepository.save(user1);

        ApplicationUser user2 = new ApplicationUser().setId(-2L).setEmail("mail@marcputz.at")
            .setPasswordEncoded(PasswordEncoder.encode("5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8", "mail@marcputz.at")) // "password"
            .setNickname("marcputz");
        LOGGER.debug("saving user '" + user2.getNickname() + "'");
        userRepository.save(user2);
    }
}
