package de.ma_vin.ape.users.controller;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "summary"}
        , snippets = CucumberOptions.SnippetType.CAMELCASE
        , glue = "de.ma_vin.ape.users.controller.it.steps"
        , features = "src/test/resources/de/ma_vin/ape/users/controller/auth")
public class AuthControllerIT {
}
