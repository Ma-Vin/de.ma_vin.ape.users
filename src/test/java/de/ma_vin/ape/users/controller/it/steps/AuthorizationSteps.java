package de.ma_vin.ape.users.controller.it.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.ma_vin.ape.users.controller.auth.GrantType;
import de.ma_vin.ape.users.controller.auth.ResponseType;
import de.ma_vin.ape.users.controller.auth.TokenResponse;
import de.ma_vin.ape.users.model.gen.dto.group.CommonGroupDto;
import de.ma_vin.ape.users.security.jwt.JsonWebTokenTest;
import de.ma_vin.ape.utils.TestUtil;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;

public class AuthorizationSteps extends AbstractIntegrationTestSteps {
    private String clientId = null;
    private String clientSecret = null;
    private String redirection = null;
    private String scope = null;
    private String state = null;
    private String userId = null;
    private String userPwd = null;

    private String code = null;
    private String token = null;
    private String refreshToken = null;

    @Given("No user is given")
    public void initUser() {
        userId = null;
        userPwd = null;
    }

    @Given("No scope is given")
    public void iniScope() {
        scope = null;
    }

    @Given("No state is given")
    public void initState() {
        state = null;
    }

    @Given("No tokens and code is given")
    public void initTokenAndCode() {
        code = null;
        token = null;
        refreshToken = null;
    }

    @Given("The clientId is {string}")
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Given("The clientSecret is {string}")
    public void setClientSecret(String clientSecret) {
        this.clientSecret = Base64.getUrlEncoder().encodeToString(clientSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Given("The authorization code is {string}")
    public void setCode(String code) {
        this.code = code;
    }

    @Given("The refresh token is {string}")
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Given("The redirection is {string}")
    public void setRedirection(String redirection) {
        this.redirection = redirection;
    }

    @Given("The scope is {string}")
    public void setScope(String scope) {
        this.scope = scope;
    }

    @Given("The state is {string}")
    public void setState(String state) {
        this.state = state;
    }

    @Given("The user has id {string} with password {string}")
    public void setUserIdAndPwd(String userId, String userPwd) {
        this.userId = userId;
        this.userPwd = userPwd;
    }

    @When("The user {string} is logged in with password {string}")
    public void performLogin(String userId, String userPwd) throws Exception {
        this.userId = userId;
        this.userPwd = userPwd;
        shared.setResultActions(mvc.perform(formLogin().user(userId).password(userPwd)));
    }

    @When("The Controller is called to authorize with response type {responseType}")
    public void callControllerToAuthorize(ResponseType responseType) {
        MultiValueMap<String, String> authValues = createValueMap("response_type", responseType.getTypeName()
                , "client_id", clientId, "client_secret", clientSecret, "redirect_uri", redirection, "scope", scope, "state", state);
        if (userId != null) {
            shared.setResultActions(performGet("/oauth/authorize", authValues, userId));
        } else {
            shared.setResultActions(performGet("/oauth/authorize", authValues));
        }
    }

    @When("The Controller is called to issue a token with grand type {grantType}")
    public void callControllerToToken(GrantType grantType) {
        MultiValueMap<String, String> tokenValues = createValueMap("grant_type", grantType.getTypeName(), "code", code
                , "refresh_token", refreshToken, "redirect_uri", redirection, "client_id", clientId, "client_secret", clientSecret
                , "username", userId, "password", userPwd != null ? Base64.getUrlEncoder().encodeToString(userPwd.getBytes(StandardCharsets.UTF_8)) : null, "scope", scope);
        shared.setResultActions(performPost("/oauth/token", tokenValues));
    }

    @Given("There is token for user {string} with password {string}")
    public void callControllerToToken(String userId, String userPwd) throws Exception {
        shared.setPrincipalUserId(userId);
        shared.setPrincipalPassword(userPwd);
        MultiValueMap<String, String> tokenValues = createValueMap("grant_type", GrantType.PASSWORD.getTypeName()
                , "client_id", clientId, "client_secret", clientSecret
                , "username", userId, "password", userPwd != null ? Base64.getUrlEncoder().encodeToString(userPwd.getBytes(StandardCharsets.UTF_8)) : null
                , "scope", scope);
        MockHttpServletResponse tokenResponse = performPost("/oauth/token", tokenValues).andReturn().getResponse();
        shared.setTokenResponse(TestUtil.getObjectMapper().readValue(tokenResponse.getContentAsString(), TokenResponse.class));
    }

    @Given("There is token for user with alias {string} and password {string}")
    public void callControllerToTokenWithAlias(String userAlias, String userPwd) throws Exception {
        callControllerToToken(shared.getCreatedObjects().get(userAlias).getIdentification(), userPwd);
    }


    @Given("Use an unknown token")
    public void setUnknownToken() {
        TokenResponse response = new TokenResponse();
        response.setTokenType("JWT");
        response.setAccessToken(JsonWebTokenTest.VALID_TOKEN);
        response.setRefreshToken(JsonWebTokenTest.VALID_TOKEN);
        response.setScope("read");
        response.setExpiresIn(100L);
        shared.setTokenResponse(response);
    }

    @When("The code is taken over")
    public void takeOverCode() {
        code = getJsonElement("code");
    }

    @When("The token is taken over")
    public void takeOverToken() {
        token = getJsonElement("access_token");
    }

    @When("The refresh token is taken over")
    public void takeOverRefreshToken() {
        refreshToken = getJsonElement("refresh_token");
    }

    private String getJsonElement(String valueName) {
        return getJsonElement(valueName, shared.getResultActions().andReturn().getResponse());
    }

    private String getJsonElement(String valueName, MockHttpServletResponse response) {
        try {
            String value = TestUtil.getObjectMapper().readTree(response.getContentAsString()).findValue(valueName).toString();
            assertTrue(value.startsWith("\"") && value.endsWith("\""), "The " + valueName + " json element should start and end with quotes");
            return value.substring(1, value.length() - 1);
        } catch (JsonProcessingException | UnsupportedEncodingException e) {
            fail("Exception while take over " + valueName + ": " + e.getMessage());
        }
        return "";
    }

    @Then("The token changes")
    public void checkTokenChanges() {
        assertNotEquals(token, getJsonElement("access_token"), "The token did not change");
    }

    @Then("The refresh token changes")
    public void checkRefreshTokenChanges() {
        assertNotEquals(refreshToken, getJsonElement("refresh_token"), "The refresh token did not change");
    }

    @ParameterType(value = "CODE|code||TOKEN|token|NOT_SUPPORTED|not_supported")
    public ResponseType responseType(String value) {
        return ResponseType.getByTypeName(value.toLowerCase());
    }

    @ParameterType(value = "AUTHORIZATION_CODE|authorization_code|IMPLICIT|implicit|PASSWORD|password|"
            + "CLIENT_CREDENTIALS|client_credentials|REFRESH_TOKEN|refresh_token|NOT_SUPPORTED|not_supported")
    public GrantType grantType(String value) {
        return GrantType.getByTypeName(value.toLowerCase());
    }
}
