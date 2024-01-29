package at.ac.tuwien.sepr.groupphase.backend.service.impl;

import at.ac.tuwien.sepr.groupphase.backend.auth.PasswordEncoder;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserRegisterDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateEmailAndPasswordDto;
import at.ac.tuwien.sepr.groupphase.backend.endpoint.dto.UserUpdateSettingsDto;
import at.ac.tuwien.sepr.groupphase.backend.entity.ApplicationUser;
import at.ac.tuwien.sepr.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepr.groupphase.backend.exception.DataStoreException;
import at.ac.tuwien.sepr.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepr.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepr.groupphase.backend.repository.UserRepository;
import at.ac.tuwien.sepr.groupphase.backend.service.UserService;
import at.ac.tuwien.sepr.groupphase.backend.service.validation.UserValidator;
import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Optional;

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
    public ApplicationUser getUserByEmail(String email) throws NotFoundException {
        LOGGER.trace("getUserByEmail({})", email);

        Optional<ApplicationUser> userOpt = userRepository.findByEmailIgnoreCase(email);
        return userOpt.orElseThrow(() -> new NotFoundException("User with email '" + email + "' could not be found"));
    }

    @Override
    public ApplicationUser getUserByNickname(String nickname) throws NotFoundException {
        LOGGER.trace("getUserByNickname({})", nickname);

        Optional<ApplicationUser> userOpt = userRepository.findByNickname(nickname);
        return userOpt.orElseThrow(() -> new NotFoundException("User with nickname '" + nickname + "' could not be found"));
    }

    @Override
    public ApplicationUser getUserById(Long userId) throws NotFoundException {
        LOGGER.trace("getUserById({})", userId);

        return userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User with ID " + userId + " could not be found"));
    }


    @Override
    public ApplicationUser create(UserRegisterDto registerDto) throws DataStoreException, ConflictException, ValidationException {
        LOGGER.trace("create({})", registerDto);

        // check if no other user has this email and nickname
        Optional<ApplicationUser> sameNickname = this.userRepository.findByNickname(registerDto.getName());
        Optional<ApplicationUser> sameEmail = this.userRepository.findByEmail(registerDto.getEmail());

        if (sameNickname.isPresent()) {
            throw new ConflictException("Unable to create new user as it is in conflict with other user entities",
                List.of("User with nickname '" + registerDto.getName() + "' already exists"));
        }
        if (sameEmail.isPresent()) {
            throw new ConflictException("Unable to create new user as it is in conflict with other user entities",
                List.of("User with email '" + registerDto.getEmail() + "' already exists"));
        }

        ApplicationUser applicationUser = new ApplicationUser(registerDto.getEmail(), registerDto.getPasswordEncoded())
            .setNickname(registerDto.getName())
            .setUserPicture(null)
            .setActiveProfile(null);

        validator.validateForCreate(applicationUser);

        try {
            return userRepository.save(applicationUser);
        } catch (JDBCException | DataIntegrityViolationException ex) {
            throw new DataStoreException(ex.getMessage(), ex);
        }
    }

    @Override
    public ApplicationUser updateEmailAndPassword(UserUpdateEmailAndPasswordDto dto, long userId)
        throws NotFoundException, DataStoreException, ValidationException, ConflictException {
        LOGGER.trace("updateEmailAndPassword({},{})", dto, userId);

        Optional<ApplicationUser> userOpt = userRepository.findById(userId);
        ApplicationUser user = userOpt.orElseThrow(() -> new NotFoundException("User with ID " + userId + " was not found"));

        // set new email
        String newEmail = dto.getEmail() == null || dto.getEmail().isEmpty()
            ? user.getEmail()
            : dto.getEmail();

        user.setEmail(newEmail);

        // set new password
        String newPassword = dto.getNewPassword() == null || dto.getNewPassword().isEmpty()
            ? dto.getCurrentPassword()
            : dto.getNewPassword();

        user.setPasswordEncoded(PasswordEncoder.encode(newPassword, user.getEmail()));

        return this.updateUser(user);
    }

    @Override
    public ApplicationUser updateSettings(UserUpdateSettingsDto dto, long userId)
        throws NotFoundException, DataStoreException, ValidationException, ConflictException {
        LOGGER.trace("updateSettings({},{})", dto, userId);

        Optional<ApplicationUser> userOpt = this.userRepository.findById(userId);
        ApplicationUser user = userOpt.orElseThrow(() -> new NotFoundException("User with ID " + userId + " was not found"));

        String newNickname = dto.getNickname() == null || dto.getNickname().isEmpty()
            ? user.getNickname()
            : dto.getNickname();

        user.setNickname(newNickname);

        byte[] newPicture = dto.getUserPicture() == null || dto.getUserPicture().length == 0
            ? user.getUserPicture()
            : dto.getUserPicture();

        user.setUserPicture(newPicture);

        return this.updateUser(user);
    }

    @Override
    public ApplicationUser updateUser(ApplicationUser userToUpdate)
        throws NotFoundException, ValidationException, ConflictException {
        LOGGER.trace("updateApplicationUser({})", userToUpdate);

        // validate user input
        validator.validateForUpdate(userToUpdate);

        // check if user with same nickname already exists
        Optional<ApplicationUser> sameNickname = this.userRepository.findByNickname(userToUpdate.getNickname());
        if (sameNickname.isPresent() && !sameNickname.get().getId().equals(userToUpdate.getId())) {
            throw new ConflictException("Can not update user settings as it is in conflict with another user entity",
                List.of("User with nickname '" + userToUpdate.getNickname() + "' already exists"));
        }

        // check if user with same email already exists
        Optional<ApplicationUser> sameEmail = this.userRepository.findByEmail(userToUpdate.getEmail());
        if (sameEmail.isPresent() && !sameEmail.get().getId().equals(userToUpdate.getId())) {
            throw new ConflictException("Can not update user settings as it is in conflict with another user entity",
                List.of("User with email '" + userToUpdate.getEmail() + "' already exists"));
        }

        // check if given ID exists in the data store
        if (userToUpdate.getId() == null || !userRepository.existsById(userToUpdate.getId())) {
            throw new NotFoundException("User with ID '" + userToUpdate.getId() + "' could not be found");
        }

        try {
            return userRepository.save(userToUpdate);
        } catch (JDBCException | DataIntegrityViolationException e) {
            throw new DataStoreException("The data store was unable to process the request", e);
        }
    }
}
