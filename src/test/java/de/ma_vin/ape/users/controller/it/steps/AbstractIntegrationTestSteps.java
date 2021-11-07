package de.ma_vin.ape.users.controller.it.steps;

import de.ma_vin.ape.users.UserApplication;
import de.ma_vin.ape.utils.TestUtil;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@CucumberContextConfiguration
@ActiveProfiles("integration-test")
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = UserApplication.class)
public abstract class AbstractIntegrationTestSteps {

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected CucumberShared shared;

    @Autowired
    protected BCryptPasswordEncoder encoder;

    protected MultiValueMap<String, String> createValueMap(String... argPairs) {
        if (argPairs.length % 2 != 0) {
            fail("Wrong number of keyValue var arg");
        }
        MultiValueMap<String, String> valueMap = new LinkedMultiValueMap();
        for (int i = 0; i < argPairs.length; i = i + 2) {
            valueMap.add(argPairs[i], argPairs[i + 1]);
        }
        return valueMap;
    }

    protected ResultActions performPostWithAuthorization(String url, MultiValueMap<String, String> valueMap) {
        try {
            return mvc.perform(
                    post(url)
                            .with(csrf())
                            .header("Authorization", "Bearer " + shared.getAccessToken())
                            .params(valueMap)
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call post at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performPostWithBasicAuthorization(String url, MultiValueMap<String, String> valueMap) {
        try {
            return mvc.perform(
                    post(url)
                            .with(csrf())
                            .header("Authorization", getClientAuthBasic())
                            .params(valueMap)
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call post at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performPost(String url, MultiValueMap<String, String> valueMap) {
        try {
            return mvc.perform(
                    post(url)
                            .with(csrf())
                            .params(valueMap)
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call post at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performGetWithAuthorization(String url, String pathVariable) {
        try {
            return mvc.perform(
                    get(url + "/" + pathVariable)
                            .with(csrf())
                            .header("Authorization", "Bearer " + shared.getAccessToken())
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call get at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performGetWithBasicAuthorization(String url, String pathVariable) {
        try {
            return mvc.perform(
                    get(url + "/" + pathVariable)
                            .with(csrf())
                            .header("Authorization", getClientAuthBasic())
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call get at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performGet(String url, MultiValueMap<String, String> valueMap) {
        try {
            return mvc.perform(
                    get(url)
                            .with(csrf())
                            .params(valueMap)
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call get at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performGet(String url, MultiValueMap<String, String> valueMap, String username) {
        try {
            return mvc.perform(
                    get(url)
                            .with(csrf())
                            .with(user(username))
                            .params(valueMap)
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call get at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performGetWithAuthorization(String url, String pathVariable, MultiValueMap<String, String> valueMap) {
        try {
            return mvc.perform(
                    get(url + "/" + pathVariable)
                            .with(csrf())
                            .header("Authorization", "Bearer " + shared.getAccessToken())
                            .params(valueMap)
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call get at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performGetWithAuthorization(String url, MultiValueMap<String, String> valueMap) {
        try {
            return mvc.perform(
                    get(url)
                            .with(csrf())
                            .header("Authorization", "Bearer " + shared.getAccessToken())
                            .params(valueMap)
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call get at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performGetWithAuthorization(String url) {
        try {
            return mvc.perform(
                    get(url)
                            .with(csrf())
                            .header("Authorization", "Bearer " + shared.getAccessToken())
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call get at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performPutWithAuthorization(String url, String pathVariable, String updatedResourceAlias) {
        try {
            return mvc.perform(
                    put(url + "/" + pathVariable)
                            .with(csrf())
                            .header("Authorization", "Bearer " + shared.getAccessToken())
                            .content(TestUtil.getObjectMapper().writeValueAsString(shared.get(updatedResourceAlias)))
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call put at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performPatchWithAuthorization(String url, String pathVariable, String changedValue) {
        try {
            return mvc.perform(
                    patch(url + "/" + pathVariable)
                            .with(csrf())
                            .header("Authorization", "Bearer " + shared.getAccessToken())
                            .content(changedValue)
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call patch at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected ResultActions performDeleteWithAuthorization(String url, String pathVariable) {
        try {
            return mvc.perform(
                    delete(url + "/" + pathVariable)
                            .with(csrf())
                            .header("Authorization", "Bearer " + shared.getAccessToken())
                            .contentType(MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            fail(String.format("fail to call put at %s: %s", url, e.getMessage()));
        }
        return null;
    }

    protected String getIdentification(String alias) {
        return shared.containsKey(alias) ? shared.get(alias).getIdentification() : "";
    }

    public void setStringValue(String property, String alias, String valueToSet) {
        setValue(property, alias, valueToSet, String.class);
    }

    public void setBooleanValue(String property, String alias, Boolean valueToSet) {
        setValue(property, alias, valueToSet, Boolean.class);
    }

    public void setIntegerValue(String property, String alias, Integer valueToSet) {
        setValue(property, alias, valueToSet, Integer.class);
    }

    public void setValue(String property, String alias, Object valueToSet, Class<?> classToSet) {
        if (!shared.containsKey(alias)) {
            fail("There is not any object with alias " + alias);
        }
        String setterMethodName = "set" + property.substring(0, 1).toUpperCase() + property.substring(1);
        try {
            Method setterMethod = getSetterMethod(shared.get(alias).getClass(), setterMethodName, classToSet);
            if (setterMethod == null) {
                fail(String.format("The setter %s at object with alias %s and %s parameter could not be called, because it was not found"
                        , setterMethodName, alias, classToSet.getSimpleName()));
            }
            setterMethod.invoke(shared.get(alias), valueToSet);
        } catch (IllegalAccessException | InvocationTargetException e) {
            fail(String.format("The setter %s at object with alias %s and %s parameter could not be called: %s"
                    , setterMethodName, alias, classToSet.getSimpleName(), e.getMessage()));
        }
    }

    private Method getSetterMethod(Class<?> methodOwner, String setterMethodName, Class<?> classToSet) {
        for (Method m : methodOwner.getDeclaredMethods()) {
            if (setterMethodName.equals(m.getName())
                    && m.getParameterCount() == 1 && classToSet.getSimpleName().equals(m.getParameterTypes()[0].getSimpleName())) {
                return m;
            }
        }
        if (Object.class.getSimpleName().equals(methodOwner.getSuperclass().getSimpleName())) {
            return null;
        }
        return getSetterMethod(methodOwner.getSuperclass(), setterMethodName, classToSet);
    }

    private String getClientAuthBasic() {
        return "Basic " + Base64.getUrlEncoder().encodeToString((shared.getClientId() + ":" + shared.getClientSecret()).getBytes(StandardCharsets.UTF_8));
    }
}
