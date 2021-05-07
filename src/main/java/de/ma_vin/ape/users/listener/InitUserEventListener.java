package de.ma_vin.ape.users.listener;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.domain.group.AdminGroupExt;
import de.ma_vin.ape.users.model.domain.group.CommonGroupExt;
import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.UserRepository;
import de.ma_vin.ape.users.service.AdminGroupService;
import de.ma_vin.ape.users.service.CommonGroupService;
import de.ma_vin.ape.users.service.UserService;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
@Log4j2
@Component
public class InitUserEventListener {
    public static final String STATE_INITIALIZED = "initialized";
    public static final String STATE_REFRESHED = "refreshed";

    private final Map<String, String> contextStates = new HashMap<>();

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;
    @Autowired
    private AdminGroupService adminGroupService;
    @Autowired
    private CommonGroupService commonGroupService;

    @Value("${db.user.isToInitialize}")
    boolean isToInitialize;
    @Value("${db.user.isCommonGroupToInitialize}")
    boolean isCommonGroupToInitialize;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!contextStates.containsKey(event.getApplicationContext().getDisplayName())) {
            contextStates.put(event.getApplicationContext().getDisplayName(), STATE_INITIALIZED);
            initEmptyDB();
        } else {
            contextStates.put(event.getApplicationContext().getDisplayName(), STATE_REFRESHED);
        }
    }

    private void initEmptyDB() {
        if (!isToInitialize || userRepository.count() > 0) {
            return;
        }

        AdminGroupExt adminGroup = new AdminGroupExt("admins");
        Optional<AdminGroup> storedAdminGroup = adminGroupService.save(adminGroup);

        if (storedAdminGroup.isEmpty()) {
            log.error("could not initialize database with default admin group");
            return;
        }

        UserExt admin = new UserExt("admin", "global", Role.ADMIN);
        admin.setRawPassword(passwordEncoder, "admin");
        Optional<User> storedUser = userService.saveAtAdminGroup(admin, storedAdminGroup.get().getIdentification());

        if (storedUser.isEmpty()) {
            log.error("could not initialize database with default admin user");
            return;
        }

        if (!isCommonGroupToInitialize) {
            return;
        }

        CommonGroupExt commonGroup = new CommonGroupExt("common");
        Optional<CommonGroup> storedCommonGroup = commonGroupService.save(commonGroup);
        if (storedCommonGroup.isEmpty()) {
            log.error("could not initialize database with default common group");
        }
    }
}
