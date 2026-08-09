package com.desenvolvimento.logica.conviva.conviva_service.config;

import com.desenvolvimento.logica.conviva.conviva_service.auth.entity.Role;
import com.desenvolvimento.logica.conviva.conviva_service.auth.entity.UserRole;
import com.desenvolvimento.logica.conviva.conviva_service.auth.repository.RoleRepository;
import com.desenvolvimento.logica.conviva.conviva_service.auth.repository.UserRoleRepository;
import com.desenvolvimento.logica.conviva.conviva_service.user.entity.User;
import com.desenvolvimento.logica.conviva.conviva_service.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialDataLoader implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRoleRepository userRoleRepository;


    @Override
    public void run(String... args) throws Exception {
        Role role = loadRoles();
        User user = loadUser();
        loadUserRole(user, role);
    }

    private Role loadRoles() {
        if (roleRepository.count() > 0) {
            return roleRepository.findAll().getFirst();
        }
        saveRole("ADMIN", "ADMINISTRADOR");
        saveRole("USER", "USUÁRIO");
        return roleRepository.findRoleByName("MASTER");
    }

    private void saveRole(String name, String description) {
        Role role = new Role();
        role.setName(name.toUpperCase());
        role.setDescription(description);
        roleRepository.save(role);
    }

    private User loadUser() {
        if (userRepository.count() > 0) {
            return userRepository.findAll().getFirst();
        }
        User user = new User();
        user.setName("RHUAN SILVA RESENDE");
        user.setDocument("03412808105");
        user.setEmail("rhuan.resende@hotmail.com.br");
        user.setPhone("62996487512");
        user.setPassword(passwordEncoder.encode("Abc@123"));
        user.setFailedLoginAttempts(0);
        user.setActive(true);
        user.setFirstAccess(true);
        user.setForcePasswordChange(false);
        return userRepository.save(user);
    }

    private void loadUserRole(User user, Role role) {
        if (userRoleRepository.count() > 0) {
            return;
        }
        UserRole userRole = new UserRole();
        userRole.setUser(user.getId());
        userRole.setRole(role.getId());
        userRoleRepository.save(userRole);
    }
}
