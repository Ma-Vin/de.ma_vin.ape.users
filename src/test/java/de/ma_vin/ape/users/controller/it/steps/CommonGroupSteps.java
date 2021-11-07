package de.ma_vin.ape.users.controller.it.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import de.ma_vin.ape.users.model.gen.dto.group.CommonGroupDto;
import de.ma_vin.ape.utils.TestUtil;
import de.ma_vin.ape.utils.controller.response.Status;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsAnything.anything;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class CommonGroupSteps extends AbstractIntegrationTestSteps {

    @Given("There exists a common group with name {string} with alias {string}")
    public void createCommonGroup(String groupName, String commonGroupAlias) throws Exception {
        MultiValueMap<String, String> createCommonGroupValues = createValueMap("groupName", groupName);

        MockHttpServletResponse createCommonGroupResponse = performPostWithAuthorization("/group/common/createCommonGroup", createCommonGroupValues)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status", is(Status.OK.name())))
                .andExpect(jsonPath("response.groupName", is(groupName)))
                .andExpect(jsonPath("response.identification", anything()))
                .andReturn().getResponse();

        String createdCommonGroupText = TestUtil.getObjectMapper().readTree(createCommonGroupResponse.getContentAsString()).findValue("response").toString();
        shared.put(commonGroupAlias, TestUtil.getObjectMapper().readValue(createdCommonGroupText, CommonGroupDto.class));
    }

    @Given("There are common groups with group name")
    public void createCommonGroups(DataTable dataTable) {
        dataTable.asLists().forEach(row -> {
            if (row.size() != 2) {
                fail("Wrong number of columns while creating common groups");
            }
            try {
                createCommonGroup(row.get(0), row.get(1));
            } catch (Exception e) {
                fail(e.getMessage());
            }
        });
    }

    @Given("All existing common groups are deleted")
    public void deleteAllExistingCommonGroups() throws Exception {
        MockHttpServletResponse getAllCommonGroupsResponse = performGetWithAuthorization("/group/common/getAllCommonGroups")
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status", is(Status.OK.name()))).andReturn().getResponse();

        String getAllCommonGroupsText = TestUtil.getObjectMapper().readTree(getAllCommonGroupsResponse.getContentAsString()).findValue("response").toString();
        List<CommonGroupDto> allCommonGroups = TestUtil.getObjectMapper().readValue(getAllCommonGroupsText, new TypeReference<List<CommonGroupDto>>() {
        });

        allCommonGroups.forEach(cg ->
        {
            try {
                performDeleteWithAuthorization("/group/common/deleteCommonGroup", cg.getIdentification())
                        .andExpect(status().isOk())
                        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("status", is(Status.OK.name())));
            } catch (Exception e) {
                fail("Exception while deleting common group " + cg.getIdentification(), e);
            }
        });
    }

    @Given("The {string} of the common group with alias {string} is set to {string}")
    public void setCommonGroupValue(String property, String alias, String valueToSet) {
        setStringValue(property, alias, valueToSet);
    }

    @When("The Controller is called to create a common group with name {string}")
    public void callControllerToCreateCommonGroup(String groupName) {
        MultiValueMap<String, String> createCommonGroupValues = createValueMap("groupName", groupName);
        shared.setResultActions(performPostWithAuthorization("/group/common/createCommonGroup", createCommonGroupValues));
    }

    @When("Controller is called to get the common group with the identification of the alias {string}")
    public void callControllerToGetCommonGroup(String commonGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/group/common/getCommonGroup", getIdentification(commonGroupAlias)));
    }

    @When("Controller is called to get all common groups")
    public void callControllerToGetAllCommonGroups() {
        shared.setResultActions(performGetWithAuthorization("/group/common/getAllCommonGroups"));
    }

    @When("Controller is called to get all common groups at page {int} with size {int}")
    public void callControllerToGetAllCommonGroups(int page, int size) {
        MultiValueMap<String, String> getAllCommonGroupsValues = createValueMap("page", "" + page, "size", "" + size);
        shared.setResultActions(performGetWithAuthorization("/group/common/getAllCommonGroups", getAllCommonGroupsValues));
    }

    @When("Controller is called to update the common group with the identification of the alias {string}")
    public void callControllerToUpdateCommonGroup(String commonGroupAlias) {
        shared.setResultActions(performPutWithAuthorization("/group/common/updateCommonGroup", getIdentification(commonGroupAlias), commonGroupAlias));
    }

    @When("Controller is called to delete the common group with the identification of the alias {string}")
    public void callControllerToDeleteCommonGroup(String commonGroupAlias) {
        shared.setResultActions(performDeleteWithAuthorization("/group/common/deleteCommonGroup", getIdentification(commonGroupAlias)));
    }
}
