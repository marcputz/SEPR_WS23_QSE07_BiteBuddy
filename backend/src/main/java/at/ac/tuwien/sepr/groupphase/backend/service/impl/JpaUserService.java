package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.UserNotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validation.UserValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaUserService implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UserRepository userRepository;
    private final UserValidator validator;

    @Autowired
    public JpaUserService(UserRepository userRepository, UserValidator validator) {
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @Override
    public ApplicationUser getUserByEmail(String email) throws UserNotFoundException {
        LOGGER.trace("getUserByEmail({})", email);

        ApplicationUser user = userRepository.findByEmailIgnoreCase(email);
        if (user == null) {
            // no user found
            throw new UserNotFoundException("User with email '" + email + "' could not be found");
        }

        return user;
    }

    @Override
    public ApplicationUser getUserByNickname(String nickname) throws UserNotFoundException {
        LOGGER.trace("getUserByNickname({})", nickname);

        ApplicationUser user = userRepository.findByNickname(nickname);
        if (user == null) {
            // no user found
            throw new UserNotFoundException("User with Nickname '" + nickname + "' could not be found");
        }

        return user;
    }

    @Override
    public ApplicationUser getUserById(Long userId) throws UserNotFoundException {
        return userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User with id " + userId + " could not be found"));
    }


    @Override
    public ApplicationUser create(UserRegisterDto registerDto) throws ValidationException {
        LOGGER.trace("create({})", registerDto);
        ApplicationUser applicationUser = new ApplicationUser(registerDto.getEmail(), registerDto.getPasswordEncoded());
        applicationUser.setNickname(registerDto.getName());
        validator.validateForCreate(applicationUser);
        ApplicationUser existingUser1 = userRepository.findByEmailIgnoreCase(applicationUser.getEmail());
        ApplicationUser existingUser2 = userRepository.findByNickname(applicationUser.getNickname());
        List<String> validationErrors = new ArrayList<>();
        if (existingUser1 != null) {
            LOGGER.info("existingUser1({})", existingUser1);
            throw new ValidationException("User with this Email already exists", validationErrors);
        }
        if (existingUser2 != null) {
            LOGGER.info("existingUser2({})", existingUser2);
            throw new ValidationException("User with this Name already exists", validationErrors);
        }
        LOGGER.info("existingUserAfter({})", existingUser2);
        return userRepository.save(applicationUser);
    }

    @Override
    public ApplicationUser update(UserUpdateDto userUpdateDto, Long currentUserId)
        throws UserNotFoundException, ValidationException, ConflictException {
        LOGGER.trace("update({})", userUpdateDto);

        ApplicationUser existingUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new UserNotFoundException("User with Id '" + currentUserId + "' could not be found"));

        List<String> validationErrors = new ArrayList<>();
        if (!userUpdateDto.getEmail().equals(existingUser.getEmail())) {
            List<String> conflictErrors = checkUniqueConstraints(userUpdateDto, existingUser.getId());
            if (!conflictErrors.isEmpty()) {
                throw new ConflictException("Conflicts in ApplicationUser update", conflictErrors);
            }
        } else if (userUpdateDto.getNewPassword().isEmpty()) {
            validationErrors.add("Nothing Changed");
        }
        if (!validationErrors.isEmpty()) {
            throw new ValidationException("Validation of ApplicationUser for create failed", validationErrors);
        }
        String encodedPassword;
        if (userUpdateDto.getNewPassword().isEmpty()) {
            encodedPassword = PasswordEncoder.encode(userUpdateDto.getCurrentPassword(), userUpdateDto.getEmail());
        } else {
            encodedPassword = PasswordEncoder.encode(userUpdateDto.getNewPassword(), userUpdateDto.getEmail());
        }
        existingUser.setEmail(userUpdateDto.getEmail())
            .setPasswordEncoded(encodedPassword);

        validator.validateForUpdate(existingUser);

        try {
            return userRepository.save(existingUser);
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("DataIntegrityViolationException occurred during update: ", e);
            throw new ConflictException("An unexpected error occurred while updating the user, try again", new ArrayList<>());
        }
    }


    private List<String> checkUniqueConstraints(UserUpdateDto userUpdateDto, Long userId) {
        LOGGER.trace("checkUniqueConstraints({},{})", userUpdateDto, userId);
        List<String> conflictErrors = new ArrayList<>();

        // Email conflict check
        ApplicationUser userWithSameEmail = userRepository.findByEmailIgnoreCase(userUpdateDto.getEmail());
        if (userWithSameEmail != null && !userWithSameEmail.getId().equals(userId)) {
            conflictErrors.add("Email '" + userUpdateDto.getEmail() + "' is already in use by another user");
        }

        // Nickname conflict check
        ApplicationUser userWithSameNickname = userRepository.findByNickname(userUpdateDto.getCurrentPassword());
        if (userWithSameNickname != null && !userWithSameNickname.getId().equals(userId)) {
            conflictErrors.add("Nickname '" + userUpdateDto.getCurrentPassword() + "' is already in use by another user");
        }

        return conflictErrors;
    }
}
