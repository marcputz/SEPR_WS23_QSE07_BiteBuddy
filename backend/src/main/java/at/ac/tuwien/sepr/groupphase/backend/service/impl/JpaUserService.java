package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateEmailAndPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateSettingsDto;
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
    public ApplicationUser updateEmailAndPassword(UserUpdateEmailAndPasswordDto userUpdateEmailAndPasswordDto, Long currentUserId)
        throws UserNotFoundException, ValidationException, ConflictException {
        LOGGER.trace("updateEmailAndPassword({})", userUpdateEmailAndPasswordDto);

        ApplicationUser existingUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new UserNotFoundException("User with Id '" + currentUserId + "' could not be found"));
        if (userUpdateEmailAndPasswordDto.getEmail() != null && !userUpdateEmailAndPasswordDto.getEmail().isEmpty() && !userUpdateEmailAndPasswordDto.getEmail()
            .equals(existingUser.getEmail())) {
            existingUser.setEmail(userUpdateEmailAndPasswordDto.getEmail());
        }
        String newPassword = userUpdateEmailAndPasswordDto.getNewPassword() == null || userUpdateEmailAndPasswordDto.getNewPassword().isEmpty()
            ? userUpdateEmailAndPasswordDto.getCurrentPassword()
            : userUpdateEmailAndPasswordDto.getNewPassword();
        if (newPassword != null) {
            existingUser.setPasswordEncoded(PasswordEncoder.encode(newPassword, existingUser.getEmail()));
        }
        return updateApplicationUser(existingUser);
    }


    @Override
    public ApplicationUser updateSettings(UserUpdateSettingsDto userUpdateSettingsDto, Long currentUserId)
        throws UserNotFoundException, ValidationException, ConflictException {
        LOGGER.trace("updateSettings({})", userUpdateSettingsDto);

        ApplicationUser existingUser = userRepository.findById(currentUserId)
            .orElseThrow(() -> new UserNotFoundException("User with Id '" + currentUserId + "' could not be found"));
        if (userUpdateSettingsDto.getNickname() != null && !userUpdateSettingsDto.getNickname().isEmpty() && !userUpdateSettingsDto.getNickname()
            .equals(existingUser.getNickname())) {
            existingUser.setNickname(userUpdateSettingsDto.getNickname());
        }
        if (userUpdateSettingsDto.getUserPicture() != null && userUpdateSettingsDto.getUserPicture().length != 0) {
            existingUser.setUserPicture(userUpdateSettingsDto.getUserPicture());
        }
        return updateApplicationUser(existingUser);
    }

    @Override
    public ApplicationUser updateApplicationUser(ApplicationUser userToUpdate)
        throws UserNotFoundException, ValidationException, ConflictException {
        LOGGER.trace("updateApplicationUser({})", userToUpdate);
        if (userToUpdate.getId() == null || !userRepository.existsById(userToUpdate.getId())) {
            throw new UserNotFoundException("User with Id '" + userToUpdate.getId() + "' could not be found");
        }
        validator.validateForUpdate(userToUpdate);
        checkUniqueConstraints(userToUpdate, true);
        try {
            return userRepository.save(userToUpdate);
        } catch (DataIntegrityViolationException e) {
            LOGGER.error("DataIntegrityViolationException occurred during update: ", e);
            throw new ConflictException("We encountered an unexpected issue while updating your account. Please try again", new ArrayList<>());
        }
    }


    private void checkUniqueConstraints(ApplicationUser user, boolean isUpdate) throws ConflictException {
        LOGGER.trace("checkUniqueConstraints({})", user);
        List<String> conflictErrors = new ArrayList<>();

        // Email conflict check
        ApplicationUser userWithSameEmail = userRepository.findByEmailIgnoreCase(user.getEmail());
        if (userWithSameEmail != null && (!isUpdate || !userWithSameEmail.getId().equals(user.getId()))) {
            conflictErrors.add("Email '" + user.getEmail() + "' is already in use");
        }

        // Nickname conflict check
        ApplicationUser userWithSameNickname = userRepository.findByNickname(user.getNickname());
        if (userWithSameNickname != null && (!isUpdate || !userWithSameNickname.getId().equals(user.getId()))) {
            conflictErrors.add("Nickname '" + user.getNickname() + "' is already in use");
        }
        if (!conflictErrors.isEmpty()) {
            String operation = isUpdate ? "update" : "creation";
            throw new ConflictException("User " + operation + " failed due to data conflicts", conflictErrors);
        }
    }
}
