package ro.piata.localmarket.core.service.user.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.piata.localmarket.config.ConfigConstants;
import ro.piata.localmarket.core.model.user.Role;
import ro.piata.localmarket.core.model.user.User;
import ro.piata.localmarket.core.repository.RoleRepository;
import ro.piata.localmarket.core.repository.UserRepository;
import ro.piata.localmarket.core.security.AuthoritiesConstants;
import ro.piata.localmarket.core.security.SecurityUtils;
import ro.piata.localmarket.core.service.exceptions.EmailAlreadyUsedException;
import ro.piata.localmarket.core.service.exceptions.InvalidPasswordException;
import ro.piata.localmarket.core.service.exceptions.UsernameAlreadyUsedException;
import ro.piata.localmarket.core.service.user.UserService;
import ro.piata.localmarket.core.service.user.data.AdminUserData;
import ro.piata.localmarket.core.service.user.data.UserData;
import ro.piata.localmarket.util.RandomUtil;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;

    private final CacheManager cacheManager;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository,
            CacheManager cacheManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.cacheManager = cacheManager;
    }

    @Override
    public Optional<User> activateRegistration(String key) {
        LOG.debug("Activating user for activation key {}", key);
        return userRepository
                .findOneByActivationKey(key)
                .map(user -> {
                    // activate given user for the registration key.
                    user.setActivated(true);
                    user.setActivationKey(null);
                    this.clearUserCaches(user);
                    LOG.debug("Activated user: {}", user);
                    return user;
                });
    }

    @Override
    public Optional<User> completePasswordReset(String newPassword, String key) {
        LOG.debug("Reset user password for reset key {}", key);
        return userRepository
                .findOneByResetKey(key)
                .filter(user -> user.getResetDate().isAfter(Instant.now().minus(1, ChronoUnit.DAYS)))
                .map(user -> {
                    user.setPassword(passwordEncoder.encode(newPassword));
                    user.setResetKey(null);
                    user.setResetDate(null);
                    this.clearUserCaches(user);
                    return user;
                });
    }

    @Override
    public Optional<User> requestPasswordReset(String mail) {
        return userRepository
                .findOneByEmailIgnoreCase(mail)
                .filter(User::isActivated)
                .map(user -> {
                    user.setResetKey(RandomUtil.generateResetKey());
                    user.setResetDate(Instant.now());
                    this.clearUserCaches(user);
                    return user;
                });
    }

    @Override
    public User registerUser(AdminUserData userDTO, String password) {
        userRepository
                .findOneByLogin(userDTO.getLogin().toLowerCase())
                .ifPresent(existingUser -> {
                    boolean removed = removeNonActivatedUser(existingUser);
                    if (!removed) {
                        throw new UsernameAlreadyUsedException();
                    }
                });
        userRepository
                .findOneByEmailIgnoreCase(userDTO.getEmail())
                .ifPresent(existingUser -> {
                    boolean removed = removeNonActivatedUser(existingUser);
                    if (!removed) {
                        throw new EmailAlreadyUsedException();
                    }
                });
        User newUser = new User();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setLogin(userDTO.getLogin().toLowerCase());
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            newUser.setEmail(userDTO.getEmail().toLowerCase());
        }
        newUser.setImageUrl(userDTO.getImageUrl());
        newUser.setLangKey(userDTO.getLangKey());
        // new user is not active
        newUser.setActivated(false);
        // new user gets registration key
        newUser.setActivationKey(RandomUtil.generateActivationKey());
        Set<Role> roles = new HashSet<>();
        roleRepository.findById(AuthoritiesConstants.USER).ifPresent(roles::add);
        newUser.setRoles(roles);
        userRepository.save(newUser);
        this.clearUserCaches(newUser);
        LOG.debug("Created Information for User: {}", newUser);
        return newUser;
    }

    private boolean removeNonActivatedUser(User existingUser) {
        if (existingUser.isActivated()) {
            return false;
        }
        userRepository.delete(existingUser);
        userRepository.flush();
        this.clearUserCaches(existingUser);
        return true;
    }

    @Override
    public User createUser(AdminUserData userDTO) {
        User user = new User();
        user.setLogin(userDTO.getLogin().toLowerCase());
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail().toLowerCase());
        }
        user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getLangKey() == null) {
            user.setLangKey(ConfigConstants.DEFAULT_LANGUAGE); // default language
        } else {
            user.setLangKey(userDTO.getLangKey());
        }
        String encryptedPassword = passwordEncoder.encode(RandomUtil.generatePassword());
        user.setPassword(encryptedPassword);
        user.setResetKey(RandomUtil.generateResetKey());
        user.setResetDate(Instant.now());
        user.setActivated(true);
        if (userDTO.getRoles() != null) {
            Set<Role> roles = userDTO
                    .getRoles()
                    .stream()
                    .map(roleRepository::findById)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        userRepository.save(user);
        this.clearUserCaches(user);
        LOG.debug("Created Information for User: {}", user);
        return user;
    }

    /**
     * Update all information for a specific user, and return the modified user.
     *
     * @param userDTO user to update.
     * @return updated user.
     */
    @Override
    public Optional<AdminUserData> updateUser(AdminUserData userDTO) {
        return Optional.of(userRepository.findById(userDTO.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(user -> {
                    this.clearUserCaches(user);
                    user.setLogin(userDTO.getLogin().toLowerCase());
                    user.setFirstName(userDTO.getFirstName());
                    user.setLastName(userDTO.getLastName());
                    if (userDTO.getEmail() != null) {
                        user.setEmail(userDTO.getEmail().toLowerCase());
                    }
                    user.setImageUrl(userDTO.getImageUrl());
                    user.setActivated(userDTO.isActivated());
                    user.setLangKey(userDTO.getLangKey());
                    Set<Role> managedRoles = user.getRoles();
                    managedRoles.clear();
                    userDTO
                            .getRoles()
                            .stream()
                            .map(roleRepository::findById)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .forEach(managedRoles::add);
                    userRepository.save(user);
                    this.clearUserCaches(user);
                    LOG.debug("Changed Information for User: {}", user);
                    return user;
                })
                .map(AdminUserData::new);
    }

    @Override
    public void deleteUser(String login) {
        userRepository
                .findOneByLogin(login)
                .ifPresent(user -> {
                    userRepository.delete(user);
                    this.clearUserCaches(user);
                    LOG.debug("Deleted User: {}", user);
                });
    }

    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     */
    @Override
    public void updateUser(String firstName, String lastName, String email, String langKey, String imageUrl) {
        SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findOneByLogin)
                .ifPresent(user -> {
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    if (email != null) {
                        user.setEmail(email.toLowerCase());
                    }
                    user.setLangKey(langKey);
                    user.setImageUrl(imageUrl);
                    userRepository.save(user);
                    this.clearUserCaches(user);
                    LOG.debug("Changed Information for User: {}", user);
                });
    }

    @Override
    @Transactional
    public void changePassword(String currentClearTextPassword, String newPassword) {
        SecurityUtils.getCurrentUserLogin()
                .flatMap(userRepository::findOneByLogin)
                .ifPresent(user -> {
                    String currentEncryptedPassword = user.getPassword();
                    if (!passwordEncoder.matches(currentClearTextPassword, currentEncryptedPassword)) {
                        throw new InvalidPasswordException();
                    }
                    String encryptedPassword = passwordEncoder.encode(newPassword);
                    user.setPassword(encryptedPassword);
                    this.clearUserCaches(user);
                    LOG.debug("Changed password for User: {}", user);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserData> getAllManagedUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(AdminUserData::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserData> getAllPublicUsers(Pageable pageable) {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map(UserData::new);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserWithRolesByLogin(String login) {
        return userRepository.findOneWithRolesByLogin(login);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> getUserWithRoles() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithRolesByLogin);
    }

    /**
     * Not activated users should be automatically deleted after 3 days.
     * <p>
     * This is scheduled to get fired every day, at 01:00 (am).
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Override
    public void removeNotActivatedUsers() {
        userRepository
                .findAllByActivatedIsFalseAndActivationKeyIsNotNullAndCreatedDateBefore(Instant.now().minus(3, ChronoUnit.DAYS))
                .forEach(user -> {
                    LOG.debug("Deleting not activated user {}", user.getLogin());
                    userRepository.delete(user);
                    this.clearUserCaches(user);
                });
    }

    /**
     * Gets a list of all the Roles.
     *
     * @return a list of all the Roles.
     */
    @Transactional(readOnly = true)
    @Override
    public List<String> getRoles() {
        return roleRepository.findAll().stream().map(Role::getCode).toList();
    }

    private void clearUserCaches(User user) {
        Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE)).evictIfPresent(user.getLogin());
        if (user.getEmail() != null) {
            Objects.requireNonNull(cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE)).evictIfPresent(user.getEmail());
        }
    }
}
