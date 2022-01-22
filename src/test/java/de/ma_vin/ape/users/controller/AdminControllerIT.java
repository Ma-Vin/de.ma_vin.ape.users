package de.ma_vin.ape.users.controller;

import de.ma_vin.ape.users.UserApplication;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"pretty", "summary"}
        , snippets = CucumberOptions.SnippetType.CAMELCASE
        , glue = "de.ma_vin.ape.users.controller.it.steps"
        , features = "src/test/resources/de/ma_vin/ape/users/controller/admin")
public class AdminControllerIT {
}
