package de.ma_vin.ape.users.controller.it.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.dto.group.UserIdRoleDto;
import de.ma_vin.ape.users.model.gen.dto.user.UserDto;
import de.ma_vin.ape.utils.TestUtil;
import de.ma_vin.ape.utils.controller.response.Status;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsAnything.anything;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class UserSteps extends AbstractIntegrationTestSteps {

    @Given("There exists an user with first name {string} and last name {string} with alias {string} at common group {string}")
    public void createUser(String firstName, String lastName, String userAlias, String commonGroupAlias) throws Exception {
        if (!shared.containsKey(commonGroupAlias)) {
            fail("There is not any common group with alias " + commonGroupAlias);
        }
        MultiValueMap<String, String> createUserValues = createValueMap("firstName", firstName, "lastName", lastName
                , "commonGroupIdentification", getIdentification(commonGroupAlias));

        MockHttpServletResponse createUserResponse = performPostWithAuthorization("/user/createUser", createUserValues)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status", is(Status.OK.name())))
                .andExpect(jsonPath("response.firstName", is(firstName)))
                .andExpect(jsonPath("response.lastName", is(lastName)))
                .andExpect(jsonPath("response.identification", anything()))
                .andReturn().getResponse();

        String createdUserText = TestUtil.getObjectMapper().readTree(createUserResponse.getContentAsString()).findValue("response").toString();
        shared.put(userAlias, TestUtil.getObjectMapper().readValue(createdUserText, UserDto.class));
    }

    @Given("There are users with first and last name and alias at common group {string}")
    public void createBaseGroups(String commonGroupAlias, DataTable dataTable) {
        dataTable.asLists().forEach(row -> {
            if (row.size() != 3) {
                fail("Wrong number of columns while creating users");
            }
            try {
                createUser(row.get(0), row.get(1), row.get(2), commonGroupAlias);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
    }

    @Given("The users are added to base group")
    public void addBaseToBaseGroup(DataTable dataTable) {
        dataTable.asLists().forEach(row -> {
            if (row.size() != 2) {
                fail("Wrong number of columns while adding users to base one");
            }
            try {
                callControllerToAddUserToBaseGroup(row.get(0), row.get(1));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
    }

    @Given("The users are added to privilege group with role")
    public void addBaseToPrivilegeGroup(DataTable dataTable) {
        dataTable.asLists().forEach(row -> {
            if (row.size() != 3) {
                fail("Wrong number of columns while adding users to privilege one");
            }
            try {
                callControllerToAddUserToPrivilegeGroup(row.get(0), Role.valueOf(row.get(2)), row.get(1));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
    }

    @Given("The {string} of the user with alias {string} is set to {string}")
    public void setUserValue(String property, String alias, String valueToSet) {
        setStringValue(property, alias, valueToSet);
    }

    @Given("There exists an user with first name {string}, last name {string}, password {string} and role {roleValue} with alias {string} at common group {string}")
    public void createUser(String firstName, String lastName, String password, Role role, String userAlias, String commonGroupAlias) throws Exception {
        createUser(firstName, lastName, userAlias, commonGroupAlias);
        callControllerToSetUsersPassword(password, userAlias);
        checkResponseExistsAndOk();
        callControllerToSetUsersRole(role, userAlias);
        checkResponseExistsAndOk();
    }

    @When("The Controller is called to create an user with first name {string} and last name {string} at common group {string}")
    public void callControllerToCreateUser(String firstName, String lastName, String commonGroupAlias) {
        MultiValueMap<String, String> createUserValues = createValueMap("firstName", firstName, "lastName", lastName
                , "commonGroupIdentification", getIdentification(commonGroupAlias));
        shared.setResultActions(performPostWithAuthorization("/user/createUser", createUserValues));
    }

    @When("Controller is called to get the user with the identification of the alias {string}")
    public void callControllerToGetUser(String userAlias) {
        shared.setResultActions(performGetWithAuthorization("/user/getUser", getIdentification(userAlias)));
    }

    @When("Controller is called to update the user with the identification of the alias {string}")
    public void callControllerToUpdateUser(String userAlias) {
        shared.setResultActions(performPutWithAuthorization("/user/updateUser", getIdentification(userAlias), userAlias));
    }

    @When("Controller is called to delete the user with the identification of the alias {string}")
    public void callControllerToDeleteUser(String userAlias) {
        shared.setResultActions(performDeleteWithAuthorization("/user/deleteUser", getIdentification(userAlias)));
    }

    @When("Controller is called to count users at common group with alias {string}")
    public void callControllerToCountUsers(String commonGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/user/countUsers", getIdentification(commonGroupAlias)));
    }

    @When("Controller is called to get all users from common group with identification of {string}")
    public void callControllerToGetAllUsers(String commonGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/user/getAllUsers", getIdentification(commonGroupAlias)));
    }

    @When("Controller is called to get all user parts from common group with identification of {string}")
    public void callControllerToGetAllUserParts(String commonGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/user/getAllUserParts", getIdentification(commonGroupAlias)));
    }

    @When("Controller is called to get all users at page {int} with size {int} from common group with alias {string}")
    public void callControllerToGetAllUsers(int page, int size, String commonGroupAlias) {
        MultiValueMap<String, String> getAllUsersValues = createValueMap("page", "" + page
                , "size", "" + size);
        shared.setResultActions(performGetWithAuthorization("/user/getAllUsers", getIdentification(commonGroupAlias), getAllUsersValues));
    }

    @When("Controller is called to add the user with alias {string} to base group with alias {string}")
    public void callControllerToAddUserToBaseGroup(String userAlias, String baseGroupAlias) {
        shared.setResultActions(performPatchWithAuthorization("/user/addUserToBaseGroup", getIdentification(baseGroupAlias)
                , getIdentification(userAlias)));
    }

    @When("Controller is called to remove the user with alias {string} from base group with alias {string}")
    public void callControllerToRemoveUserFromBaseGroup(String userAlias, String baseGroupAlias) {
        shared.setResultActions(performPatchWithAuthorization("/user/removeUserFromBaseGroup", getIdentification(baseGroupAlias)
                , getIdentification(userAlias)));
    }

    @When("Controller is called to count users at base group with alias {string}")
    public void callControllerToCountUsersAtBaseGroup(String baseGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/user/countUsersAtBaseGroup", getIdentification(baseGroupAlias)));
    }

    @When("Controller is called to get all user of base group with alias {string} and dissolving sub groups {booleanValue}")
    public void callControllerToGetAllUsersFromBaseGroup(String baseGroupAlias, Boolean dissolveSubgroups) {
        MultiValueMap<String, String> findAllUsers = createValueMap("dissolveSubgroups", dissolveSubgroups.toString());
        shared.setResultActions(performGetWithAuthorization("/user/getAllUsersFromBaseGroup", getIdentification(baseGroupAlias), findAllUsers));
    }

    @When("Controller is called to get all users at page {int} with size {int} from base group with alias {string}")
    public void callControllerToGetAllUsersFromBaseGroup(int page, int size, String baseGroupAlias) {
        MultiValueMap<String, String> getAllUsersValues = createValueMap("page", "" + page, "size", "" + size);
        shared.setResultActions(performGetWithAuthorization("/user/getAllUsersFromBaseGroup", getIdentification(baseGroupAlias), getAllUsersValues));
    }

    @When("Controller is called to add the user with alias {string} as {roleValue} to privilege group with alias {string}")
    public void callControllerToAddUserToPrivilegeGroup(String userAlias, Role role, String privilegeGroupAlias) {
        UserIdRoleDto baseGroupIdRoleDto = new UserIdRoleDto();
        baseGroupIdRoleDto.setUserIdentification(getIdentification(userAlias));
        baseGroupIdRoleDto.setRole(role);
        try {
            shared.setResultActions(performPatchWithAuthorization("/user/addUserToPrivilegeGroup", getIdentification(privilegeGroupAlias)
                    , TestUtil.getObjectMapper().writeValueAsString(baseGroupIdRoleDto)));
        } catch (JsonProcessingException e) {
            fail("JsonProcessingException: " + e.getMessage());
        }
    }

    @When("Controller is called to count users at privilege group with alias {string} with role {roleValue}")
    public void callControllerToCountUsersAtPrivilegeGroup(String privilegeGroupAlias, Role role) {
        MultiValueMap<String, String> countUsers = createValueMap("role", role.name());
        shared.setResultActions(performGetWithAuthorization("/user/countUsersAtPrivilegeGroup", getIdentification(privilegeGroupAlias), countUsers));
    }

    @When("Controller is called to get all user of privilege group with alias {string} with role {roleValue} and dissolving sub groups {booleanValue}")
    public void callControllerToGetAllUsersFromPrivilegeGroup(String privilegeGroupAlias, Role role, Boolean dissolveSubgroups) {
        MultiValueMap<String, String> findAllUsers = createValueMap("dissolveSubgroups", dissolveSubgroups.toString(), "role", role.name());
        shared.setResultActions(performGetWithAuthorization("/user/getAllUsersFromPrivilegeGroup", getIdentification(privilegeGroupAlias), findAllUsers));
    }

    @When("Controller is called to get all users with role {roleValue} at page {int} with size {int} from privilege group with alias {string}")
    public void callControllerToGetAllUsersFromPrivilegeGroup(Role role, int page, int size, String privilegeGroupAlias) {
        MultiValueMap<String, String> getAllUsersValues = createValueMap("page", "" + page, "size", "" + size, "role", role.name());
        shared.setResultActions(performGetWithAuthorization("/user/getAllUsersFromPrivilegeGroup", getIdentification(privilegeGroupAlias), getAllUsersValues));
    }

    @When("Controller is called to remove the user with alias {string} from privilege group with alias {string}")
    public void callControllerToRemoveUserFromPrivilegeGroup(String userAlias, String privilegeGroupAlias) {
        shared.setResultActions(performPatchWithAuthorization("/user/removeUserFromPrivilegeGroup", getIdentification(privilegeGroupAlias)
                , getIdentification(userAlias)));
    }

    @When("Controller is called to set the password {string} of user with the identification of the alias {string}")
    public void callControllerToSetUsersPassword(String password, String userAlias) {
        shared.setResultActions(performPatchWithAuthorization("/user/setUserPassword", getIdentification(userAlias)
                , password));
    }

    @When("Controller is called to set the role {roleValue} of user with the identification of the alias {string}")
    public void callControllerToSetUsersRole(Role role, String userAlias) throws JsonProcessingException {
        shared.setResultActions(performPatchWithAuthorization("/user/setUserRole", getIdentification(userAlias)
                , TestUtil.getObjectMapper().writeValueAsString(role)));
    }

    private void checkResponseExistsAndOk() throws Exception {
        shared.getResultActions().andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }
}
