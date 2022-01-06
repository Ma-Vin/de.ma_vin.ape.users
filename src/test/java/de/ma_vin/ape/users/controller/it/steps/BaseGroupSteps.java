package de.ma_vin.ape.users.controller.it.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.users.model.gen.dto.group.BaseGroupDto;
import de.ma_vin.ape.users.model.gen.dto.group.BaseGroupIdRoleDto;
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

public class BaseGroupSteps extends AbstractIntegrationTestSteps {
    @Given("There exists a base group with name {string} with alias {string} at common group {string}")
    public void createBaseGroup(String groupName, String baseGroupAlias, String commonGroupAlias) throws Exception {
        if (!shared.containsKey(commonGroupAlias)) {
            fail("There is not any common group with alias " + commonGroupAlias);
        }
        MultiValueMap<String, String> createBaseGroupValues = createValueMap("groupName", groupName
                , "commonGroupIdentification", getIdentification(commonGroupAlias));

        MockHttpServletResponse createBaseGroupResponse = performPostWithAuthorization("/group/base/createBaseGroup", createBaseGroupValues)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status", is(Status.OK.name())))
                .andExpect(jsonPath("response.groupName", is(groupName)))
                .andExpect(jsonPath("response.identification", anything()))
                .andReturn().getResponse();

        String createdBaseGroupText = TestUtil.getObjectMapper().readTree(createBaseGroupResponse.getContentAsString()).findValue("response").toString();
        shared.put(baseGroupAlias, TestUtil.getObjectMapper().readValue(createdBaseGroupText, BaseGroupDto.class));
    }

    @Given("There are base groups with name and alias at common group {string}")
    public void createBaseGroups(String commonGroupAlias, DataTable dataTable) {
        dataTable.asLists().forEach(row -> {
            if (row.size() != 2) {
                fail("Wrong number of columns while creating base groups");
            }
            try {
                createBaseGroup(row.get(0), row.get(1), commonGroupAlias);
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
    }

    @Given("The {string} of the base group with alias {string} is set to {string}")
    public void setBaseGroupValue(String property, String alias, String valueToSet) {
        setStringValue(property, alias, valueToSet);
    }

    @Given("The base groups are added to base group")
    public void addBaseToBaseGroup(DataTable dataTable) {
        dataTable.asLists().forEach(row -> {
            if (row.size() != 2) {
                fail("Wrong number of columns while adding base groups to base one");
            }
            try {
                callControllerToAddBaseToBaseGroup(row.get(0), row.get(1));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
    }

    @Given("The base groups are added to privilege group with role")
    public void addBaseToPrivilegeGroup(DataTable dataTable) {
        dataTable.asLists().forEach(row -> {
            if (row.size() != 3) {
                fail("Wrong number of columns while adding base groups to privilege one");
            }
            try {
                callControllerToAddBaseToPrivilegeGroup(row.get(0), Role.valueOf(row.get(2)), row.get(1));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
    }

    @When("The Controller is called to create a base group with name {string} at common group {string}")
    public void callControllerToCreateBaseGroup(String groupName, String commonGroupAlias) {
        MultiValueMap<String, String> createBaseGroupValues = createValueMap("groupName", groupName
                , "commonGroupIdentification", getIdentification(commonGroupAlias));
        shared.setResultActions(performPostWithAuthorization("/group/base/createBaseGroup", createBaseGroupValues));
    }

    @When("Controller is called to get the base group with the identification of the alias {string}")
    public void callControllerToGetBaseGroup(String baseGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/group/base/getBaseGroup", getIdentification(baseGroupAlias)));
    }

    @When("Controller is called to update the base group with the identification of the alias {string}")
    public void callControllerToUpdateBaseGroup(String baseGroupAlias) {
        shared.setResultActions(performPutWithAuthorization("/group/base/updateBaseGroup", getIdentification(baseGroupAlias), baseGroupAlias));
    }

    @When("Controller is called to delete the base group with the identification of the alias {string}")
    public void callControllerToDeleteBaseGroup(String baseGroupAlias) {
        shared.setResultActions(performDeleteWithAuthorization("/group/base/deleteBaseGroup", getIdentification(baseGroupAlias)));
    }

    @When("Controller is called to add the base group with alias {string} to base group with alias {string}")
    public void callControllerToAddBaseToBaseGroup(String baseGroupAlias, String baseGroupParentAlias) {
        shared.setResultActions(performPatchWithAuthorization("/group/base/addBaseToBaseGroup", getIdentification(baseGroupParentAlias)
                , getIdentification(baseGroupAlias)));
    }

    @When("Controller is called to remove the base group with alias {string} from base group with alias {string}")
    public void callControllerToRemoveBaseFromBaseGroup(String baseGroupAlias, String baseGroupParentAlias) {
        shared.setResultActions(performPatchWithAuthorization("/group/base/removeBaseFromBaseGroup", getIdentification(baseGroupParentAlias)
                , getIdentification(baseGroupAlias)));
    }

    @When("Controller is called to count sub base groups at base group with alias {string}")
    public void callControllerToCountBaseAtBaseGroup(String baseGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/group/base/countBaseAtBaseGroup", getIdentification(baseGroupAlias)));
    }

    @When("Controller is called to get all sub groups of base group with alias {string}")
    public void callControllerToFindAllBaseAtBaseGroup(String baseGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/group/base/findAllBaseAtBaseGroup", getIdentification(baseGroupAlias)));
    }

    @When("Controller is called to get all base groups at page {int} with size {int} from base group with alias {string}")
    public void callControllerToFindAllBaseAtBaseGroup(int page, int size, String baseGroupAlias) {
        MultiValueMap<String, String> getAllBaseGroupValues = createValueMap("page", "" + page, "size", "" + size);
        shared.setResultActions(performGetWithAuthorization("/group/base/findAllBaseAtBaseGroup", getIdentification(baseGroupAlias), getAllBaseGroupValues));
    }

    @When("Controller is called to add the base group with alias {string} as {roleValue} to privilege group with alias {string}")
    public void callControllerToAddBaseToPrivilegeGroup(String baseGroupAlias, Role role, String privilegeGroupAlias) {
        BaseGroupIdRoleDto baseGroupIdRoleDto = new BaseGroupIdRoleDto();
        baseGroupIdRoleDto.setBaseGroupIdentification(getIdentification(baseGroupAlias));
        baseGroupIdRoleDto.setRole(role);
        try {
            shared.setResultActions(performPatchWithAuthorization("/group/base/addBaseToPrivilegeGroup", getIdentification(privilegeGroupAlias)
                    , TestUtil.getObjectMapper().writeValueAsString(baseGroupIdRoleDto)));
        } catch (JsonProcessingException e) {
            fail("JsonProcessingException: " + e.getMessage());
        }
    }

    @When("Controller is called to remove the base group with alias {string} from privilege group with alias {string}")
    public void callControllerToRemoveBaseFromPrivilegeGroup(String baseGroupAlias, String privilegeGroupAlias) {
        shared.setResultActions(performPatchWithAuthorization("/group/base/removeBaseFromPrivilegeGroup", getIdentification(privilegeGroupAlias)
                , getIdentification(baseGroupAlias)));
    }

    @When("Controller is called to count sub base groups at privilege group with alias {string}")
    public void callControllerToCountBaseAtPrivilegeGroup(String privilegeGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/group/base/countBaseAtPrivilegeGroup", getIdentification(privilegeGroupAlias)));
    }

    @When("Controller is called to count sub base groups at privilege group with alias {string} and role {roleValue}")
    public void callControllerToCountBaseAtPrivilegeGroup(String privilegeGroupAlias, Role role) {
        MultiValueMap<String, String> countBaseAtPrivilegeGroupValues = createValueMap("role", "" + role.name());
        shared.setResultActions(performGetWithAuthorization("/group/base/countBaseAtPrivilegeGroup", getIdentification(privilegeGroupAlias), countBaseAtPrivilegeGroupValues));
    }

    @When("Controller is called to get all sub base groups of privilege group with alias {string}")
    public void callControllerToFindAllBaseAtPrivilegeGroup(String privilegeGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/group/base/findAllBaseAtPrivilegeGroup", getIdentification(privilegeGroupAlias)));
    }

    @When("Controller is called to get all sub base groups of privilege group with alias {string} and role {roleValue}")
    public void callControllerToFindAllBaseAtPrivilegeGroup(String privilegeGroupAlias, Role role) {
        MultiValueMap<String, String> findAllBaseAtPrivilegeGroupValues = createValueMap("role", "" + role.name());
        shared.setResultActions(performGetWithAuthorization("/group/base/findAllBaseAtPrivilegeGroup", getIdentification(privilegeGroupAlias)
                , findAllBaseAtPrivilegeGroupValues));
    }

    @When("Controller is called to get all base groups at page {int} with size {int} from privilege group with alias {string}")
    public void callControllerToFindAllBaseAtPrivilegeGroup(int page, int size, String privilegeGroupAlias) {
        MultiValueMap<String, String> getAllBaseGroupValues = createValueMap("page", "" + page, "size", "" + size);
        shared.setResultActions(performGetWithAuthorization("/group/base/findAllBaseAtPrivilegeGroup", getIdentification(privilegeGroupAlias), getAllBaseGroupValues));
    }

    @When("Controller is called to count base groups at common group with alias {string}")
    public void callControllerToCountBaseGroup(String commonGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/group/base/countBaseGroups", getIdentification(commonGroupAlias)));
    }

    @When("Controller is called to get all base groups from common group with alias {string}")
    public void callControllerToGetAllBaseGroups(String commonGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/group/base/getAllBaseGroups", getIdentification(commonGroupAlias)));
    }

    @When("Controller is called to get all base groups at page {int} with size {int} from common group with alias {string}")
    public void callControllerToGetAllBaseGroups(int page, int size, String commonGroupAlias) {
        MultiValueMap<String, String> getAllBaseGroupValues = createValueMap("page", "" + page, "size", "" + size);
        shared.setResultActions(performGetWithAuthorization("/group/base/getAllBaseGroups", getIdentification(commonGroupAlias), getAllBaseGroupValues));
    }
}
