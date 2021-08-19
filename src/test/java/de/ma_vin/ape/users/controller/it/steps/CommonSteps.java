package de.ma_vin.ape.users.controller.it.steps;

import de.ma_vin.ape.users.enums.Role;
import de.ma_vin.ape.utils.controller.response.Status;
import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Then;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsAnything.anything;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CommonSteps extends AbstractIntegrationTestSteps {

    @Before
    public void initShared() {
        shared.getCreatedObjects().clear();
    }


    @Then("The result is Ok and Json")
    public void checkResponseExists() throws Exception {
        shared.getResultActions().andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Then("The result is successful logged in")
    public void checkResponseIsLogin() throws Exception {
        checkResponseIsRedirected("/");
    }

    @Then("The result is redirected to location {string}")
    public void checkResponseIsRedirected(String redirectedTo) throws Exception {
        shared.getResultActions().andExpect(status().is3xxRedirection()).andExpect(header().string("Location", redirectedTo));
    }

    @Then("The result is a {httpCodeRange}")
    public void checkResponseIsHttpCode(ResultMatcher codeMatcher) throws Exception {
        shared.getResultActions().andExpect(codeMatcher);
    }

    @Then("The status of the result should be {string}")
    public void checkStatus(String status) throws Exception {
        shared.getResultActions().andExpect(jsonPath("status", is(Status.valueOf(status).name())));
    }

    @Then("The {string} property at response is {string}")
    public void checkPropertyResponse(String propertyName, String propertyValue) throws Exception {
        checkProperty("response." + propertyName, propertyValue);
    }

    @Then("The {string} property is {string}")
    public void checkProperty(String propertyName, String propertyValue) throws Exception {
        shared.getResultActions().andExpect(jsonPath(propertyName, is(propertyValue)));
    }

    @Then("The {string} property is anything")
    public void checkAnythingAt(String propertyName) throws Exception {
        shared.getResultActions().andExpect(jsonPath(propertyName, anything()));
    }

    @Then("The {string} property at response is {int}")
    public void checkProperty(String propertyName, Integer propertyValue) throws Exception {
        shared.getResultActions().andExpect(jsonPath("response." + propertyName, is(propertyValue)));
    }

    @Then("The {string} property at response is {booleanValue}")
    public void checkProperty(String propertyName, Boolean propertyValue) throws Exception {
        shared.getResultActions().andExpect(jsonPath("response." + propertyName, is(propertyValue)));
    }

    @Then("The {string} property at response position {int} is {string}")
    public void checkPropertyAt(String propertyName, int position, String propertyValue) throws Exception {
        shared.getResultActions().andExpect(jsonPath("response[" + position + "]." + propertyName, is(propertyValue)));
    }

    @Then("The {string} property at response position {int} is {int}")
    public void checkPropertyAt(String propertyName, int position, Integer propertyValue) throws Exception {
        shared.getResultActions().andExpect(jsonPath("response[" + position + "]." + propertyName, is(propertyValue)));
    }

    @Then("The {string} property at response position {int} is anything")
    public void checkPropertyIsAnythingAt(String propertyName, int position) throws Exception {
        shared.getResultActions().andExpect(jsonPath("response[" + position + "]." + propertyName, anything()));
    }

    @Then("At response position {int} is anything")
    public void checkAnythingAt(int position) throws Exception {
        shared.getResultActions().andExpect(jsonPath("response[" + position + "]", anything()));
    }

    @Then("At response position {int} does not exists")
    public void checkDoesNotExistsAt(int position) throws Exception {
        shared.getResultActions().andExpect(jsonPath("response[" + position + "]").doesNotExist());
    }

    @Then("The {string} property at response position {int} does not exists")
    public void checkPropertyDoesNotExistsAt(String propertyName, int position) throws Exception {
        shared.getResultActions().andExpect(jsonPath("response[" + position + "]." + propertyName).doesNotExist());
    }

    @Then("The response is {booleanValue}")
    public void checkProperty(Boolean responseValue) throws Exception {
        shared.getResultActions().andExpect(jsonPath("response", is(responseValue)));
    }

    @Then("The response is {int}")
    public void checkProperty(int responseValue) throws Exception {
        shared.getResultActions().andExpect(jsonPath("response", is(responseValue)));
    }

    @Then("There is any identification at response")
    public void checkIdentificationExists() throws Exception {
        shared.getResultActions().andExpect(jsonPath("response.identification", anything()));
    }

    @Then("The identification is the same like the one of alias {string}")
    public void checkIdentificationAt(String alias) throws Exception {
        checkPropertyResponse("identification", getIdentification(alias));
    }

    @Then("The identification at {int} is the same like the one of alias {string}")
    public void checkIdentificationAt(int position, String alias) throws Exception {
        checkPropertyAt("identification", position, getIdentification(alias));
    }

    @Then("The identification of {string} at {int} is the same like the one of alias {string}")
    public void checkIdentificationAt(String propertyName, int position, String alias) throws Exception {
        checkPropertyAt(propertyName + ".identification", position, getIdentification(alias));
    }

    @Then("The identification at property {string} is the same like the one of alias {string}")
    public void checkIdentificationAt(String propertyName, String alias) throws Exception {
        checkPropertyResponse(propertyName + ".identification", getIdentification(alias));
    }

    @ParameterType(value = "true|True|TRUE|false|False|FALSE")
    public Boolean booleanValue(String value) {
        return Boolean.valueOf(value);
    }

    @ParameterType(value = "ADMIN||MANAGER||CONTRIBUTOR||VISITOR|BLOCKED|NOT_RELEVANT")
    public Role roleValue(String value) {
        return Role.valueOf(value);
    }

    @ParameterType(value = "1xx|2xx|3xx|4xx|5xx")
    public ResultMatcher httpCodeRange(String value) {
        return switch (value) {
            case "1xx" -> status().is1xxInformational();
            case "2xx" -> status().is2xxSuccessful();
            case "3xx" -> status().is3xxRedirection();
            case "4xx" -> status().is4xxClientError();
            case "5xx" -> status().is5xxServerError();
            default -> null;
        };
    }
}
