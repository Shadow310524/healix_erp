package in.healix.modules.organization.service;

import in.healix.core.exception.HealixException;
import in.healix.core.tenant.TenantContext;
import in.healix.modules.organization.domain.Role;
import in.healix.modules.organization.domain.User;
import in.healix.modules.organization.domain.event.RoleAssignedEvent;
import in.healix.modules.organization.domain.event.UserCreatedEvent;
import in.healix.modules.organization.repository.UserRepository;
import in.healix.modules.organization.repository.RoleRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, RoleRepository roleRepository,
                       ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new HealixException("User not found", "USER_NOT_FOUND"));
    }

    @Transactional
    public User createUser(User user, String password) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new HealixException("Tenant context required to create user", "TENANT_CONTEXT_MISSING");
        }
        
        user.setTenantId(UUID.fromString(tenantId));
        user.setPasswordHash(passwordEncoder.encode(password));
        User savedUser = userRepository.save(user);

        // Publish domain event
        eventPublisher.publishEvent(new UserCreatedEvent(
            UUID.randomUUID(),
            savedUser.getTenantId(),
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getFirstName(),
            savedUser.getLastName()
        ));

        return savedUser;
    }

    @Transactional
    public User assignRole(UUID userId, UUID roleId) {
        User user = getUserById(userId);
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new HealixException("Role not found", "ROLE_NOT_FOUND"));

        user.getRoles().add(role);
        User savedUser = userRepository.save(user);

        // Publish domain event
        eventPublisher.publishEvent(new RoleAssignedEvent(
            UUID.randomUUID(),
            user.getTenantId(),
            user.getId(),
            role.getId(),
            role.getName()
        ));

        return savedUser;
    }
}
