package com.neusoft.amos.system;

import com.neusoft.amos.system.domain.AmosUser;
import com.neusoft.amos.system.domain.Department;
import com.neusoft.amos.system.domain.Installation;
import com.neusoft.amos.system.domain.Role;
import com.neusoft.amos.system.domain.UserDepartmentAccess;
import com.neusoft.amos.system.domain.UserOption;
import com.neusoft.amos.system.repository.AmosUserRepository;
import com.neusoft.amos.system.repository.DepartmentRepository;
import com.neusoft.amos.system.repository.InstallationRepository;
import com.neusoft.amos.system.repository.RoleRepository;
import com.neusoft.amos.system.repository.UserDepartmentAccessRepository;
import com.neusoft.amos.system.repository.UserOptionRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动后注入 bootstrap 管理员（带 BCrypt 密码哈希，无法用纯 SQL 生成）。
 * 幂等：仅当 amos_user 为空时执行。参考种子 installation/department/role 由 V4 迁移脚本写入。
 */
@Component
public class SystemSeedRunner implements ApplicationRunner {

    private final AmosUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final InstallationRepository installationRepository;
    private final DepartmentRepository departmentRepository;
    private final UserDepartmentAccessRepository udaRepository;
    private final UserOptionRepository optionRepository;

    public SystemSeedRunner(AmosUserRepository userRepository,
                            RoleRepository roleRepository,
                            InstallationRepository installationRepository,
                            DepartmentRepository departmentRepository,
                            UserDepartmentAccessRepository udaRepository,
                            UserOptionRepository optionRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.installationRepository = installationRepository;
        this.departmentRepository = departmentRepository;
        this.udaRepository = udaRepository;
        this.optionRepository = optionRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        Installation traveller = installationRepository.findByCode("Traveller")
                .orElseThrow(() -> new IllegalStateException("seed installation Traveller missing"));
        Department er = departmentRepository.findByInstallationCode("Traveller").stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("seed department ER missing"));
        Role adminRole = roleRepository.findByCode("ADMIN")
                .orElseThrow(() -> new IllegalStateException("seed role ADMIN missing"));

        AmosUser admin = new AmosUser();
        admin.setUsername("admin");
        admin.setDisplayName("Administrator");
        admin.setPasswordHash(new BCryptPasswordEncoder().encode("admin"));
        admin.setStatus("Active");
        admin.getRoles().add(adminRole);
        admin = userRepository.save(admin);

        UserDepartmentAccess access = new UserDepartmentAccess();
        access.setUser(admin);
        access.setInstallation(traveller);
        access.setDepartment(er);
        udaRepository.save(access);

        UserOption theme = new UserOption();
        theme.setUser(admin);
        theme.setOptKey("theme");
        theme.setOptValue("dark");
        optionRepository.save(theme);
    }
}
