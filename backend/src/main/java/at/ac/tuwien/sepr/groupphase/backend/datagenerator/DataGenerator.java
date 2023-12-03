package at.ac.tuwien.sepr.groupphase.backend.datagenerator;

import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
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

    public DataGenerator(UserRepository userRepository) {
        this.userRepository = userRepository;
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
                userRepository.deleteById(testUser1.getId());
            }
            ApplicationUser testUser2 = userRepository.findByNickname("johndoe");
            if (testUser2 != null) {
                userRepository.deleteById(testUser2.getId());
            }
        }

        LOGGER.debug("generating default test users");

        ApplicationUser user1 = new ApplicationUser().setId(-1L).setEmail("max.mustermann@test.at")
            .setPasswordEncoded("ba527ca265c37cf364b057b4f412d175f79d363e0e15d709097f188a4fe979ba2cc1c048e1c97da7804465cef5f8abe7") // "password"
            .setNickname("maxmuster");
        LOGGER.debug("saving user '" + user1.getNickname() + "'");
        userRepository.save(user1);

        ApplicationUser user2 = new ApplicationUser().setId(-2L).setEmail("john.doe@nasa.org")
            .setPasswordEncoded("ba527ca265c37cf364b057b4f412d175f79d363e0e15d709097f188a4fe979ba2cc1c048e1c97da7804465cef5f8abe7") // "password"
            .setNickname("johndoe");
        LOGGER.debug("saving user '" + user2.getNickname() + "'");
        userRepository.save(user2);
    }
}
