package de.ma_vin.ape.users.controller.it.steps;

import de.ma_vin.ape.users.UserApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        classes = UserApplication.class)
@AutoConfigureMockMvc
@CucumberContextConfiguration
@ActiveProfiles("integration-test")
public class CucumberSpringConfiguration {

}
