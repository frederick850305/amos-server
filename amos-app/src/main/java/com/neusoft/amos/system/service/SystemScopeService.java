package com.neusoft.amos.system.service;

import com.neusoft.amos.system.domain.AmosUser;
import com.neusoft.amos.system.domain.UserDepartmentAccess;
import com.neusoft.amos.system.domain.UserOption;
import com.neusoft.amos.system.dto.ScopeResponse;
import com.neusoft.amos.system.dto.UserOptionDto;
import com.neusoft.amos.system.repository.AmosUserRepository;
import com.neusoft.amos.system.repository.UserDepartmentAccessRepository;
import com.neusoft.amos.system.repository.UserOptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemScopeService {

    private final UserDepartmentAccessRepository udaRepository;
    private final AmosUserRepository userRepository;
    private final UserOptionRepository optionRepository;

    public SystemScopeService(UserDepartmentAccessRepository udaRepository,
                              AmosUserRepository userRepository,
                              UserOptionRepository optionRepository) {
        this.udaRepository = udaRepository;
        this.userRepository = userRepository;
        this.optionRepository = optionRepository;
    }

    public ScopeResponse getScopes(String username) {
        AmosUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + username));
        List<UserDepartmentAccess> accesses = udaRepository.findByUserUsername(username);

        Map<String, ScopeResponse.ScopeInstallation> instMap = new LinkedHashMap<>();
        for (UserDepartmentAccess a : accesses) {
            String instCode = a.getInstallation().getCode();
            ScopeResponse.ScopeInstallation inst = instMap.computeIfAbsent(instCode,
                    k -> new ScopeResponse.ScopeInstallation(
                            instCode, a.getInstallation().getName(), new ArrayList<>()));
            inst.departments().add(new ScopeResponse.ScopeDepartment(
                    a.getDepartment().getCode(), a.getDepartment().getName()));
        }
        return new ScopeResponse(new ArrayList<>(instMap.values()));
    }

    public List<UserOptionDto> getOptions(String username) {
        AmosUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + username));
        return optionRepository.findByUserId(user.getId()).stream()
                .map(o -> new UserOptionDto(o.getOptKey(), o.getOptValue()))
                .toList();
    }

    @Transactional
    public UserOptionDto setOption(String username, String key, String value) {
        AmosUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("user not found: " + username));
        UserOption opt = optionRepository.findByUserIdAndOptKey(user.getId(), key)
                .orElseGet(() -> {
                    UserOption o = new UserOption();
                    o.setUser(user);
                    o.setOptKey(key);
                    return o;
                });
        opt.setOptValue(value);
        opt = optionRepository.save(opt);
        return new UserOptionDto(opt.getOptKey(), opt.getOptValue());
    }
}
