package co.edu.corhuila.opti.customers.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import co.edu.corhuila.opti.customers.testsupport.TestClock;

/**
 * Negative authorization tests for the security story
 * "[security] Patients can only read their own record and formulas" (STRIDE row E, risk 9).
 * A PACIENTE token must never read the clinical data of a patient that is not its own.
 * Goes in opti-customers-api/customers-app/src/test/java/co/edu/corhuila/opti/customers/app/.
 */
@SpringBootTest(classes = HttpTestApplication.class)
@AutoConfigureMockMvc
class PatientFormulasAuthorizationTest {

    private static final AtomicInteger SEQUENCE = new AtomicInteger(50_000);

    @Autowired
    MockMvc mvc;
    @Autowired
    TestClock clock;

    @Test
    void patientTokenCannotListFormulasOfAnotherPatient() throws Exception {
        String otherPatient = patientWithFormula();

        as(get("/api/v1/patients/" + otherPatient + "/formulas"), "PACIENTE")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void patientTokenCannotReadCurrentFormulaOfAnotherPatient() throws Exception {
        String otherPatient = patientWithFormula();

        as(get("/api/v1/patients/" + otherPatient + "/formulas/current"), "PACIENTE")
                .andExpect(status().isForbidden());
    }

    // ---- helpers --------------------------------------------------------------------------

    private String patientWithFormula() throws Exception {
        String body = as(post("/api/v1/patients").header("Idempotency-Key", "key-" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content(patientJson()), "ADMIN")
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        String id = idOf(body);
        as(post("/api/v1/patients/" + id + "/formulas").header("Idempotency-Key", "f-" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON).content(formulaJson()), "OPTOMETRIST")
                .andExpect(status().isCreated());
        return id;
    }

    private ResultActions as(MockHttpServletRequestBuilder request, String... roles) throws Exception {
        return mvc.perform(request.header("Authorization", "Bearer " + TestTokens.valid(clock.instant(), roles)));
    }

    private static String patientJson() {
        return """
                {"documentType":"CC","documentNumber":"%d","firstName":"Carlos Andres","lastName":"Perdomo Silva",
                 "phone":"3157783310","email":"carlos.perdomo@gmail.com","eps":"EPS Sanitas","city":"Neiva",
                 "birthDate":"1988-03-14"}""".formatted(SEQUENCE.incrementAndGet() * 1000L);
    }

    private static String formulaJson() {
        return """
                {"od":{"sphere":-2.0,"cylinder":-0.5,"axis":180},"oi":{"sphere":-1.75},
                 "pupillaryDistance":63.0,"lensType":"MONOFOCAL","optometristName":"Ana Torres",
                 "formulaDate":"2026-09-20"}""";
    }

    private static String idOf(String json) {
        int start = json.indexOf("\"id\":\"") + 6;
        return json.substring(start, json.indexOf('"', start));
    }
}
