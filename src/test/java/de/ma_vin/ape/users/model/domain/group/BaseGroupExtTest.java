package de.ma_vin.ape.users.model.domain.group;

import de.ma_vin.ape.users.model.domain.user.UserExt;
import de.ma_vin.ape.users.model.gen.domain.group.BaseGroup;
import de.ma_vin.ape.users.model.gen.domain.user.User;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class BaseGroupExtTest {
    public static final Long BASE_GROUP_ID = 1L;
    public static final Long OTHER_BASE_GROUP_ID = 2L;
    public static final Long USER_ID = 3L;
    public static final Long ANOTHER_USER_ID = 4L;

    public static final String BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(BASE_GROUP_ID, BaseGroup.ID_PREFIX);
    public static final String OTHER_BASE_GROUP_IDENTIFICATION = IdGenerator.generateIdentification(OTHER_BASE_GROUP_ID, BaseGroup.ID_PREFIX);
    public static final String USER_IDENTIFICATION = IdGenerator.generateIdentification(USER_ID, User.ID_PREFIX);
    public static final String ANOTHER_USER_IDENTIFICATION = IdGenerator.generateIdentification(ANOTHER_USER_ID, User.ID_PREFIX);

    private BaseGroupExt cut;
    private BaseGroupExt anotherCut;
    private AutoCloseable openMocks;

    @Mock
    private UserExt user;
    @Mock
    private UserExt anotherUser;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);
        cut = new BaseGroupExt();
        cut.setIdentification(BASE_GROUP_IDENTIFICATION);
        anotherCut = new BaseGroupExt();
        anotherCut.setIdentification(OTHER_BASE_GROUP_IDENTIFICATION);

        when(user.getIdentification()).thenReturn(USER_IDENTIFICATION);
        when(anotherUser.getIdentification()).thenReturn(ANOTHER_USER_IDENTIFICATION);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }

    @Test
    public void testGetAllUsers() {
        cut.addUsers(user);
        cut.addSubBaseGroups(anotherCut);
        anotherCut.addUsers(anotherUser);

        Set<User> result = cut.getAllUsers();
        assertNotNull(result, "There should be a result");
        assertEquals(2, result.size(), "Wrong number of users");
        assertTrue(result.contains(user), "The direct user should be contained");
        assertTrue(result.contains(anotherUser), "The indirect user should be contained");
    }
}
