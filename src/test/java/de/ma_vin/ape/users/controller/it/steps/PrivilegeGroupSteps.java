package de.ma_vin.ape.users.controller.it.steps;

import de.ma_vin.ape.users.model.gen.dto.group.PrivilegeGroupDto;
import de.ma_vin.ape.utils.TestUtil;
import de.ma_vin.ape.utils.controller.response.Status;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsAnything.anything;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PrivilegeGroupSteps extends AbstractIntegrationTestSteps {
    @Given("There exists a privilege group with name {string} with alias {string} at common group {string}")
    public void createPrivilegeGroup(String groupName, String privilegeGroupAlias, String commonGroupAlias) throws Exception {
        if (!shared.containsKey(commonGroupAlias)) {
            fail("There is not any common group with alias " + commonGroupAlias);
        }
        MultiValueMap<String, String> createPrivilegeGroupValues = createValueMap("groupName", groupName
                , "commonGroupIdentification", getIdentification(commonGroupAlias));

        MockHttpServletResponse createPrivilegeGroupResponse = performPostWithAuthorization("/group/privilege/createPrivilegeGroup", createPrivilegeGroupValues)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status", is(Status.OK.name())))
                .andExpect(jsonPath("response.groupName", is(groupName)))
                .andExpect(jsonPath("response.identification", anything()))
                .andReturn().getResponse();

        String createdPrivilegeGroupText = TestUtil.getObjectMapper().readTree(createPrivilegeGroupResponse.getContentAsString()).findValue("response").toString();
        shared.put(privilegeGroupAlias, TestUtil.getObjectMapper().readValue(createdPrivilegeGroupText, PrivilegeGroupDto.class));
    }

    @Given("The {string} of the privilege group with alias {string} is set to {string}")
    public void setPrivilegeGroupValue(String property, String alias, String valueToSet) {
        setStringValue(property, alias, valueToSet);
    }

    @When("The Controller is called to create a privilege group with name {string} at common group {string}")
    public void callControllerToCreatePrivilegeGroup(String groupName, String commonGroupAlias) {
        MultiValueMap<String, String> createPrivilegeGroupValues = createValueMap("groupName", groupName
                , "commonGroupIdentification", getIdentification(commonGroupAlias));
        shared.setResultActions(performPostWithAuthorization("/group/privilege/createPrivilegeGroup", createPrivilegeGroupValues));
    }

    @When("Controller is called to get the privilege group with the identification of the alias {string}")
    public void callControllerToGetPrivilegeGroup(String privilegeGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/group/privilege/getPrivilegeGroup", getIdentification(privilegeGroupAlias)));
    }

    @When("Controller is called to update the privilege group with the identification of the alias {string}")
    public void callControllerToUpdatePrivilegeGroup(String privilegeGroupAlias) {
        shared.setResultActions(performPutWithAuthorization("/group/privilege/updatePrivilegeGroup", getIdentification(privilegeGroupAlias), privilegeGroupAlias));
    }

    @When("Controller is called to delete the privilege group with the identification of the alias {string}")
    public void callControllerToDeletePrivilegeGroup(String privilegeGroupAlias) {
        shared.setResultActions(performDeleteWithAuthorization("/group/privilege/deletePrivilegeGroup", getIdentification(privilegeGroupAlias)));
    }

    @When("Controller is called to count privilege groups at common group with alias {string}")
    public void callControllerToCountPrivilegeGroup(String commonGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/group/privilege/countPrivilegeGroups", getIdentification(commonGroupAlias)));
    }

    @When("Controller is called to get all privilege groups from common group with alias {string}")
    public void callControllerToGetAllPrivilegeGroups(String commonGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/group/privilege/getAllPrivilegeGroups", getIdentification(commonGroupAlias)));
    }
}
