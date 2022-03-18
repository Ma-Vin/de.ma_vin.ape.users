package de.ma_vin.ape.users.listener;

import de.ma_vin.ape.users.model.gen.domain.group.AdminGroup;
import de.ma_vin.ape.users.model.gen.domain.group.CommonGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.users.persistence.UserRepository;
import de.ma_vin.ape.users.service.AdminGroupService;
import de.ma_vin.ape.users.service.CommonGroupService;
import de.ma_vin.ape.users.service.UserService;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class InitUserEventListenerTest {
    public static final String ADMIN_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(1L, AdminGroup.ID_PREFIX);
    public static final String COMMON_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(2L, CommonGroup.ID_PREFIX);
    public static final String USER_IDENTIFICATION = IdGenerator.generateIdentification(3L, User.ID_PREFIX);
    public static final String APPLICATION_DISPLAY_NAME = "UsersApp";

    private InitUserEventListener cut;
    private AutoCloseable openMocks;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private AdminGroupService adminGroupService;
    @Mock
    private CommonGroupService commonGroupService;
    @Mock
    private ContextRefreshedEvent event;
    @Mock
    private ApplicationContext applicationContext;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new InitUserEventListener();
        cut.setPasswordEncoder(passwordEncoder);
        cut.setUserRepository(userRepository);
        cut.setUserService(userService);
        cut.setAdminGroupService(adminGroupService);
        cut.setCommonGroupService(commonGroupService);
        cut.setToInitialize(true);
        cut.setCommonGroupToInitialize(true);

        when(event.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getDisplayName()).thenReturn(APPLICATION_DISPLAY_NAME);
        when(userRepository.count()).thenReturn(0L);
        when(userService.saveAtAdminGroup(any(), any(), any())).then(a -> {
            ((User) a.getArgument(0)).setIdentification(USER_IDENTIFICATION);
            return Optional.of(a.getArgument(0));
        });
        when(adminGroupService.save(any())).then(a -> {
            ((AdminGroup) a.getArgument(0)).setIdentification(ADMIN_GROUP_IDENTIFICATION);
            return Optional.of(a.getArgument(0));
        });
        when(commonGroupService.save(any(), any())).then(a -> {
            ((CommonGroup) a.getArgument(0)).setIdentification(COMMON_GROUP_IDENTIFICATION);
            return Optional.of(a.getArgument(0));
        });
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @DisplayName("Refresh application")
    @Test
    public void testOnApplicationEventRefresh() {
        cut.getContextStates().put(APPLICATION_DISPLAY_NAME, InitUserEventListener.STATE_INITIALIZED);
        when(userRepository.count()).thenReturn(1L);

        cut.onApplicationEvent(event);

        verify(adminGroupService, never()).save(any());
        verify(commonGroupService, never()).save(any(), any());
        verify(userService, never()).saveAtAdminGroup(any(), anyString(), any());
    }

    @DisplayName("Do not initialize")
    @Test
    public void testOnApplicationEventNoInitialize() {
        cut.setToInitialize(false);
        cut.onApplicationEvent(event);

        verify(adminGroupService, never()).save(any());
        verify(commonGroupService, never()).save(any(), any());
        verify(userService, never()).saveAtAdminGroup(any(), anyString(), any());
    }

    @DisplayName("Existing users")
    @Test
    public void testOnApplicationEventExistingUsers() {
        when(userRepository.count()).thenReturn(1L);
        cut.onApplicationEvent(event);

        verify(adminGroupService, never()).save(any());
        verify(commonGroupService, never()).save(any(), any());
        verify(userService, never()).saveAtAdminGroup(any(), anyString(), any());
    }

    @DisplayName("Initialize admin and common group")
    @Test
    public void testOnApplicationEvent() {
        cut.onApplicationEvent(event);

        verify(adminGroupService).save(any());
        verify(commonGroupService).save(any(), any());
        verify(userService).saveAtAdminGroup(any(), anyString(), any());
    }

    @DisplayName("Do not initialize common group")
    @Test
    public void testOnApplicationEventCommonNotToInitialize() {
        cut.setCommonGroupToInitialize(false);
        cut.onApplicationEvent(event);

        verify(adminGroupService).save(any());
        verify(commonGroupService, never()).save(any(), any());
        verify(userService).saveAtAdminGroup(any(), anyString(), any());
    }


    @DisplayName("Fail to initialize admin group")
    @Test
    public void testOnApplicationEventFailAdminGroup() {
        reset(adminGroupService);
        when(adminGroupService.save(any())).then(a -> Optional.empty());
        cut.onApplicationEvent(event);

        verify(adminGroupService).save(any());
        verify(commonGroupService, never()).save(any(), any());
        verify(userService, never()).saveAtAdminGroup(any(), anyString(), any());
    }

    @DisplayName("Fail to initialize user")
    @Test
    public void testOnApplicationEventFailUser() {
        reset(userService);
        when(userService.saveAtAdminGroup(any(), any(), any())).then(a -> Optional.empty());
        cut.onApplicationEvent(event);

        verify(adminGroupService).save(any());
        verify(commonGroupService, never()).save(any(), any());
        verify(userService).saveAtAdminGroup(any(), anyString(), any());
    }

    @DisplayName("Fail to initialize common group")
    @Test
    public void testOnApplicationEventFailCommonGroup() {
        reset(commonGroupService);
        when(commonGroupService.save(any(), any())).then(a -> Optional.empty());
        cut.onApplicationEvent(event);

        verify(adminGroupService).save(any());
        verify(commonGroupService).save(any(), any());
        verify(userService).saveAtAdminGroup(any(), anyString(), any());
    }
}
