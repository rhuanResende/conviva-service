package com.desenvolvimento.logica.conviva.conviva_service.auth.service;

import com.desenvolvimento.logica.conviva.conviva_service.auth.entity.SecurityEvent;
import com.desenvolvimento.logica.conviva.conviva_service.auth.enums.SecurityEventType;
import com.desenvolvimento.logica.conviva.conviva_service.auth.repository.SecurityEventRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecurityEventService {

    private final SecurityEventRepository securityEventRepository;

    public SecurityEventService(SecurityEventRepository securityEventRepository) {
        this.securityEventRepository = securityEventRepository;
    }

    public void register(UUID user, SecurityEventType event, String message) {
        SecurityEvent securityEvent = new SecurityEvent();
        securityEvent.setUser(user);
        securityEvent.setEvent(event);
        securityEvent.setMessage(message);
        securityEventRepository.save(securityEvent);
    }
}
