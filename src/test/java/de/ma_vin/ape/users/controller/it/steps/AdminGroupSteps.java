package de.ma_vin.ape.users.controller.it.steps;

import de.ma_vin.ape.users.model.gen.dto.group.AdminGroupDto;
import de.ma_vin.ape.users.model.gen.dto.user.UserDto;
import de.ma_vin.ape.utils.TestUtil;
import de.ma_vin.ape.utils.controller.response.Status;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsAnything.anything;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

public class AdminGroupSteps extends AbstractIntegrationTestSteps {


    @Given("Init admin group features")
    public void initAdminGroupFeatures() {
        if (shared.isInitAdminGroupFeature()) {
            return;
        }
        shared.setInitAdminGroupFeature(true);
        shared.setCreatedAdmins(1);
    }

    @Given("The admin group with identification {string} is known as alias {string}")
    public void getAdminGroup(String identification, String adminGroupAlias) throws Exception {
        MockHttpServletResponse getAdminGroupResponse = performGetWithAuthorization("/admin/getAdminGroup", identification)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status", is(Status.OK.name())))
                .andExpect(jsonPath("response.identification", anything()))
                .andReturn().getResponse();

        String getAdminGroupText = TestUtil.getObjectMapper().readTree(getAdminGroupResponse.getContentAsString()).findValue("response").toString();
        shared.put(adminGroupAlias, TestUtil.getObjectMapper().readValue(getAdminGroupText, AdminGroupDto.class));
    }

    @Given("The admin with identification {string} is known as alias {string}")
    public void getAdmin(String identification, String admin) throws Exception {
        MockHttpServletResponse getAdminResponse = performGetWithAuthorization("/admin/getAdmin", identification)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status", is(Status.OK.name())))
                .andExpect(jsonPath("response.identification", anything()))
                .andReturn().getResponse();

        String getAdminText = TestUtil.getObjectMapper().readTree(getAdminResponse.getContentAsString()).findValue("response").toString();
        shared.put(admin, TestUtil.getObjectMapper().readValue(getAdminText, UserDto.class));
    }

    @Given("There exists an admin with first name {string} and last name {string} with alias {string} at admin group {string}")
    public void createAdmin(String firstName, String lastName, String adminAlias, String adminGroupAlias) throws Exception {
        if (!shared.containsKey(adminGroupAlias)) {
            fail("There is not any admin group with alias " + adminGroupAlias);
        }
        MultiValueMap<String, String> createAdminValues = createValueMap("firstName", firstName, "lastName", lastName
                , "adminGroupIdentification", getIdentification(adminGroupAlias));

        MockHttpServletResponse createAdminResponse = performPostWithAuthorization("/admin/createAdmin", createAdminValues)
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("status", is(Status.OK.name())))
                .andExpect(jsonPath("response.firstName", is(firstName)))
                .andExpect(jsonPath("response.lastName", is(lastName)))
                .andExpect(jsonPath("response.identification", anything()))
                .andReturn().getResponse();

        String createdAdminText = TestUtil.getObjectMapper().readTree(createAdminResponse.getContentAsString()).findValue("response").toString();
        shared.put(adminAlias, TestUtil.getObjectMapper().readValue(createdAdminText, UserDto.class));
        shared.setCreatedAdmins(shared.getCreatedAdmins() + 1);
    }

    @Given("The {string} of the admin with alias {string} is set to {string}")
    public void setAdminValue(String property, String alias, String valueToSet) {
        setStringValue(property, alias, valueToSet);
    }

    @Given("The {string} of the admin group with alias {string} is set to {string}")
    public void setAdminGroupValue(String property, String alias, String valueToSet) {
        setStringValue(property, alias, valueToSet);
    }

    @When("The Controller is called to create an admin with first name {string} and last name {string} at admin group {string}")
    public void callControllerToCreateAdmin(String firstName, String lastName, String adminGroupAlias) {
        MultiValueMap<String, String> createAdminValues = createValueMap("firstName", firstName, "lastName", lastName
                , "adminGroupIdentification", getIdentification(adminGroupAlias));
        shared.setResultActions(performPostWithAuthorization("/admin/createAdmin", createAdminValues));
        shared.setCreatedAdmins(shared.getCreatedAdmins() + 1);
    }

    @When("Controller is called to delete the admin with the identification of the alias {string}")
    public void callControllerToDeleteAdmin(String adminAlias) {
        shared.setResultActions(performDeleteWithAuthorization("/admin/deleteAdmin", getIdentification(adminAlias)));
        shared.setCreatedAdmins(shared.getCreatedAdmins() - 1);
    }

    @When("Controller is called to get the admin with the identification of the alias {string}")
    public void callControllerToGetAdmin(String adminAlias) {
        shared.setResultActions(performGetWithAuthorization("/admin/getAdmin", getIdentification(adminAlias)));
    }

    @When("Controller is called to count admins at admin group with alias {string}")
    public void callControllerToCountAdmins(String adminGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/admin/countAdmins", getIdentification(adminGroupAlias)));
    }

    @When("Controller is called to get all admins from admin group with identification of {string}")
    public void callControllerToGetAllAdmins(String adminGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/admin/getAllAdmins", getIdentification(adminGroupAlias)));
    }

    @When("Controller is called to get all admin parts from admin group with identification of {string}")
    public void callControllerToGetAllAdminParts(String adminGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/admin/getAllAdminParts", getIdentification(adminGroupAlias)));
    }

    @When("Controller is called to update the admin with the identification of the alias {string}")
    public void callControllerToUpdateAdmin(String adminAlias) {
        shared.setResultActions(performPutWithAuthorization("/admin/updateAdmin", getIdentification(adminAlias), adminAlias));
    }

    @When("Controller is called to set the password {string} of admin with the identification of the alias {string}")
    public void callControllerToSetAdminsPassword(String password, String adminAlias) {
        shared.setResultActions(performPatchWithAuthorization("/admin/setAdminPassword", getIdentification(adminAlias)
                , password));
    }

    @When("Controller is called to get the admin group with the identification of the alias {string}")
    public void callControllerToGetAdminGroup(String adminGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/admin/getAdminGroup", getIdentification(adminGroupAlias)));
    }

    @When("Controller is called to update the admin group with the identification of the alias {string}")
    public void callControllerToUpdateAdminGroup(String adminGroupAlias) {
        shared.setResultActions(performPutWithAuthorization("/admin/updateAdminGroup", getIdentification(adminGroupAlias), adminGroupAlias));
    }

    @When("Controller is called to get the history of admin group with the identification of the alias {string}")
    public void callControllerToGetAdminGroupHistory(String adminGroupAlias) {
        shared.setResultActions(performGetWithAuthorization("/admin/getAdminGroupHistory", getIdentification(adminGroupAlias)));
    }

    @When("Controller is called to get the history of admin with the identification of the alias {string}")
    public void callControllerToGetAdminHistory(String adminAlias) {
        shared.setResultActions(performGetWithAuthorization("/admin/getAdminHistory", getIdentification(adminAlias)));
    }

    @Then("The response is equal to the number of created admins")
    public void checkCreatedAdmins() throws Exception {
        shared.getResultActions().andExpect(jsonPath("response", is(shared.getCreatedAdmins())));
    }

    @Then("There are not more entries than the number of created admins")
    public void checkListHasNotMoreThanCreatedAdmins() throws Exception {
        shared.getResultActions().andExpect(jsonPath("response[" + shared.getCreatedAdmins() + "].identification").doesNotExist());
    }
}
