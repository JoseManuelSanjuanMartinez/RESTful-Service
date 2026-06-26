package org.example.banksproject.controller;

import org.example.banksproject.models.Bank;
import org.example.banksproject.models.InterestRateType;
import org.example.banksproject.models.Mortgage;
import org.example.banksproject.repositories.BankRepository;
import org.example.banksproject.repositories.MortgageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class MortgageControllerPutEndpointTests {

    private final String API_AUTH_HEADER = "ApiKey";
    private final String TEST_API_KEY = "test-key";
    private final String ADMIN_API_KEY = "admin-key";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private MortgageRepository mortgageRepository;

    private ObjectMapper mapper = new ObjectMapper();

    private Bank testBank;
    private Mortgage testMortgage;

    @BeforeEach
    public void setUp() throws Exception {
        mortgageRepository.deleteAll();
        bankRepository.deleteAll();

        String testBankJson = "{\"name\":\"ING Bank\",\"swiftBicCode\":\"INGDESMM\",\"web\":\"ing.es\"}";
        String testMortgageJson = "{\"name\":\"Hipoteca Naranja\",\"interestRateType\":\"FIJO\",\"description\":\"\",\"apr\":1.75}";

        // Crear banco y hipoteca para tests que lo requieran (owned by test-key)
        MvcResult bankResult = mockMvc.perform(post("/banks/")
                .header(API_AUTH_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(testBankJson)
        ).andReturn();
        testBank = mapper.readValue(bankResult.getResponse().getContentAsString(), Bank.class);

        MvcResult mortgageResult = mockMvc.perform(post(String.format("/banks/%d/mortgages/", testBank.getId()))
                .header(API_AUTH_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(testMortgageJson)
        ).andReturn();
        testMortgage = mapper.readValue(mortgageResult.getResponse().getContentAsString(), Mortgage.class);

        // Crear un banco/hipoteca owned by admin for forbidden tests
        String adminBankJson = "{\"name\":\"Admin Bank\",\"swiftBicCode\":\"ADMBANK\",\"web\":\"admin.es\"}";
        MvcResult adminBankResult = mockMvc.perform(post("/banks/")
                .header(API_AUTH_HEADER, ADMIN_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(adminBankJson)
        ).andReturn();
        Bank adminBank = mapper.readValue(adminBankResult.getResponse().getContentAsString(), Bank.class);

        String adminMortgageJson = "{\"name\":\"Admin Hipoteca\",\"interestRateType\":\"FIJO\",\"description\":\"\",\"apr\":2.0}";
        MvcResult adminMortgageResult = mockMvc.perform(post(String.format("/banks/%d/mortgages/", adminBank.getId()))
                .header(API_AUTH_HEADER, ADMIN_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(adminMortgageJson)
        ).andReturn();
        // not stored to field; will be used by id when needed
    }

    @Test
    public void shouldEditMortgage() throws Exception {
        String updatedTestMortgageJson = "{\"name\":\"Nueva Hipoteca Naranja\",\"interestRateType\":\"VARIABLE\",\"description\":\"actualizada!\",\"apr\":1.75}";

        MvcResult result = mockMvc.perform(put(String.format("/banks/%d/mortgages/%d", testBank.getId(), testMortgage.getId()))
                .header(API_AUTH_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(updatedTestMortgageJson)
        ).andExpect(status().isOk()).andReturn();

        assertThat(mapper.readValue(result.getResponse().getContentAsString(), Mortgage.class)
                .equals(new Mortgage(testMortgage.getId(), testBank.getId(), "Nueva Hipoteca Naranja", InterestRateType.VARIABLE, "actualizada!", 1.75))
        ).isTrue();
    }

    @Test
    public void shouldReturn404WhenBankDoesNotExist() throws Exception {
        String updatedMortgageJson = "{\"name\":\"X\",\"interestRateType\":\"VARIABLE\",\"description\":\"\",\"apr\":1.5}";

        mockMvc.perform(put("/banks/999999/mortgages/1")
                .header(API_AUTH_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(updatedMortgageJson)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn404WhenMortgageDoesNotExist() throws Exception {
        String updatedMortgageJson = "{\"name\":\"X\",\"interestRateType\":\"VARIABLE\",\"description\":\"\",\"apr\":1.5}";

        mockMvc.perform(put(String.format("/banks/%d/mortgages/%d", testBank.getId(), 999999))
                .header(API_AUTH_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(updatedMortgageJson)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnForbiddenWhenNotAuthenticated() throws Exception {
        String updatedMortgageJson = "{\"name\":\"X\",\"interestRateType\":\"VARIABLE\",\"description\":\"\",\"apr\":1.5}";

        mockMvc.perform(put(String.format("/banks/%d/mortgages/%d", testBank.getId(), testMortgage.getId()))
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(updatedMortgageJson)
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnForbiddenWhenNotOwner() throws Exception {
        // Find the admin-owned mortgage id: fetch all mortgages for admin bank would be an extra call,
        // but we can assume the last created mortgage has id > 0; better to create and read it specifically.
        // Create admin bank and mortgage here to retrieve ids deterministically.
        String adminBankJson = "{\"name\":\"Other Bank\",\"swiftBicCode\":\"OBANK\",\"web\":\"other.es\"}";
        MvcResult adminBankResult = mockMvc.perform(post("/banks/")
                .header(API_AUTH_HEADER, ADMIN_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(adminBankJson)
        ).andReturn();
        Bank adminBank = mapper.readValue(adminBankResult.getResponse().getContentAsString(), Bank.class);

        String adminMortgageJson = "{\"name\":\"Other Hipoteca\",\"interestRateType\":\"FIJO\",\"description\":\"\",\"apr\":2.0}";
        MvcResult adminMortgageResult = mockMvc.perform(post(String.format("/banks/%d/mortgages/", adminBank.getId()))
                .header(API_AUTH_HEADER, ADMIN_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(adminMortgageJson)
        ).andReturn();
        Mortgage adminMortgage = mapper.readValue(adminMortgageResult.getResponse().getContentAsString(), Mortgage.class);

        String updatedMortgageJson = "{\"name\":\"X\",\"interestRateType\":\"VARIABLE\",\"description\":\"\",\"apr\":1.5}";

        mockMvc.perform(put(String.format("/banks/%d/mortgages/%d", adminBank.getId(), adminMortgage.getId()))
                .header(API_AUTH_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(updatedMortgageJson)
        ).andExpect(status().isForbidden());
    }
}
