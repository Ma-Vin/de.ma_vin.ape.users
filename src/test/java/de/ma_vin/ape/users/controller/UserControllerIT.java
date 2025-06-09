package de.ma_vin.ape.users.controller;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.platform.suite.api.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("de/ma_vin/ape/users/controller/user")
@ConfigurationParameters({
        @ConfigurationParameter(key = Constants.PLUGIN_PROPERTY_NAME, value = "pretty,summary")
        , @ConfigurationParameter(key = Constants.SNIPPET_TYPE_PROPERTY_NAME, value = "camelcase")
        , @ConfigurationParameter(key = Constants.GLUE_PROPERTY_NAME, value = "de.ma_vin.ape.users.controller.it.steps")
})
public class UserControllerIT {
}
