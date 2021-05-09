package de.ma_vin.ape.users.service;

import de.ma_vin.ape.users.model.gen.dao.resource.UserResourceDao;
import de.ma_vin.ape.users.model.gen.domain.resource.UserResource;
import de.ma_vin.ape.users.persistence.UserResourceRepository;
import de.ma_vin.ape.utils.generators.IdGenerator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.openMocks;

public class UserResourceServiceTest {
    public static final Long USER_RESOURCE_ID = 1L;
    public static final String USER_RESOURCE_IDENTIFICATION = IdGenerator.generateIdentification(USER_RESOURCE_ID, UserResource.ID_PREFIX);


    private UserResourceService cut;
    private AutoCloseable openMocks;

    @Mock
    private UserResourceRepository userResourceRepository;
    @Mock
    private UserResource userResource;
    @Mock
    private UserResourceDao userResourceDao;

    @BeforeEach
    public void setUp() {
        openMocks = openMocks(this);

        cut = new UserResourceService();
        cut.setUserResourceRepository(userResourceRepository);
    }

    @AfterEach
    public void tearDown() throws Exception {
        openMocks.close();
    }


    @DisplayName("Delete users resource")
    @Test
    public void testDeleteUser() {
        when(userResource.getIdentification()).thenReturn(USER_RESOURCE_IDENTIFICATION);

        cut.delete(userResource);

        verify(userResourceRepository).delete(any());
    }

    @DisplayName("Check existence of users resource")
    @Test
    public void testUserExits() {
        when(userResourceRepository.existsById(eq(USER_RESOURCE_ID))).thenReturn(Boolean.TRUE);

        assertTrue(cut.userResourceExits(USER_RESOURCE_IDENTIFICATION), "The result should be true");
        verify(userResourceRepository).existsById(eq(USER_RESOURCE_ID));
    }

    @DisplayName("Find non existing users resource")
    @Test
    public void testFindUserNonExisting() {
        when(userResourceRepository.findById(any())).thenReturn(Optional.empty());

        Optional<UserResource> result = cut.findUserResource(USER_RESOURCE_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userResourceRepository).findById(eq(USER_RESOURCE_ID));
    }

    @DisplayName("Find non existing users resource")
    @Test
    public void testFindUser() {
        when(userResourceDao.getId()).thenReturn(USER_RESOURCE_ID);
        when(userResourceDao.getIdentification()).thenReturn(USER_RESOURCE_IDENTIFICATION);
        when(userResourceRepository.findById(any())).thenReturn(Optional.of(userResourceDao));

        Optional<UserResource> result = cut.findUserResource(USER_RESOURCE_IDENTIFICATION);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_RESOURCE_IDENTIFICATION, result.get().getIdentification(), "Wrong identification");

        verify(userResourceRepository).findById(eq(USER_RESOURCE_ID));
    }

    @DisplayName("Save new user at common group")
    @Test
    public void testSaveAtCommonGroupNew() {
        when(userResource.getIdentification()).thenReturn(null);
        when(userResourceDao.getId()).thenReturn(USER_RESOURCE_ID);
        when(userResourceDao.getIdentification()).thenReturn(USER_RESOURCE_IDENTIFICATION);
        when(userResourceRepository.findById(eq(USER_RESOURCE_ID))).thenReturn(Optional.of(userResourceDao));
        when(userResourceRepository.save(any())).then(a -> {
            ((UserResourceDao) a.getArgument(0)).setId(USER_RESOURCE_ID);
            return a.getArgument(0);
        });

        Optional<UserResource> result = cut.save(userResource);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_RESOURCE_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userResourceRepository, never()).findById(any());
        verify(userResourceRepository).save(any());
    }

    @DisplayName("Save existing user at common group")
    @Test
    public void testSaveAtCommonGroupExisting() {
        when(userResource.getIdentification()).thenReturn(USER_RESOURCE_IDENTIFICATION);
        when(userResourceDao.getId()).thenReturn(USER_RESOURCE_ID);
        when(userResourceDao.getIdentification()).thenReturn(USER_RESOURCE_IDENTIFICATION);
        when(userResourceRepository.findById(eq(USER_RESOURCE_ID))).thenReturn(Optional.of(userResourceDao));
        when(userResourceRepository.save(any())).then(a -> a.getArgument(0));

        Optional<UserResource> result = cut.save(userResource);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isPresent(), "The result should be present");
        assertEquals(USER_RESOURCE_IDENTIFICATION, result.get().getIdentification(), "Wrong identification at result");

        verify(userResourceRepository).findById(any());
        verify(userResourceRepository).save(any());
    }

    @DisplayName("Save non existing user at common group")
    @Test
    public void testSaveAtCommonGroupNonExisting() {
        when(userResource.getIdentification()).thenReturn(USER_RESOURCE_IDENTIFICATION);
        when(userResourceDao.getId()).thenReturn(USER_RESOURCE_ID);
        when(userResourceDao.getIdentification()).thenReturn(USER_RESOURCE_IDENTIFICATION);
        when(userResourceRepository.findById(eq(USER_RESOURCE_ID))).thenReturn(Optional.empty());
        when(userResourceRepository.save(any())).then(a -> a.getArgument(0));

        Optional<UserResource> result = cut.save(userResource);
        assertNotNull(result, "The result should not be null");
        assertTrue(result.isEmpty(), "The result should be empty");

        verify(userResourceRepository).findById(any());
        verify(userResourceRepository, never()).save(any());
    }
}
