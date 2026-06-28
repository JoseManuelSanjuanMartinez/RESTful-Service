package org.example.banksproject.controller;

import org.example.banksproject.models.ApiUser;
import org.example.banksproject.models.Bank;
import org.example.banksproject.models.InterestRateType;
import org.example.banksproject.models.Mortgage;
import org.example.banksproject.repositories.ApiUsersRepository;
import org.example.banksproject.repositories.BankRepository;
import org.example.banksproject.repositories.MortgageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.NoSuchElementException;
import java.util.Optional;

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
    private final String UPDATE_MORTGAGE_JSON_PLACEHOLDER = "{\"name\":\"Hipoteca Generica\",\"interestRateType\":\"FIJO\",\"description\":\"\",\"apr\":999.0}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BankRepository bankRepository;
    @Autowired
    private MortgageRepository mortgageRepository;
    @Autowired
    private ApiUsersRepository apiUsersRepository;

    private ObjectMapper mapper = new ObjectMapper();

    private Bank adminOwnedBank;
    private Mortgage adminOwnedMortgage;
    private Bank testOwnedBank;
    private Mortgage testOwnedMortgage;

    @BeforeEach
    public void setUp() throws Exception {
        try {
            Optional<ApiUser> optionalAdmin = apiUsersRepository.findByApiKey(ADMIN_API_KEY);
            Optional<ApiUser> optionalTest = apiUsersRepository.findByApiKey(TEST_API_KEY);
            ApiUser adminUser = optionalAdmin.orElseThrow();
            ApiUser testUser = optionalTest.orElseThrow();

            adminOwnedBank = bankRepository.save(new Bank(null, "Admin Bank", "SWIFT", "https://example.com", adminUser.getId()));
            adminOwnedMortgage = mortgageRepository.save(new Mortgage(null, adminOwnedBank.getId(), "M1", InterestRateType.FIJO, "desc", 1.0));
            testOwnedBank = bankRepository.save(new Bank(null, "Test Bank", "SWIFT", "https://example.com", testUser.getId()));
            testOwnedMortgage = mortgageRepository.save(new Mortgage(null, testOwnedBank.getId(), "M1", InterestRateType.FIJO, "desc", 1.0));
        } catch (NoSuchElementException e) {
            throw new Exception(String.format("User not found with ApiKey:%s", ADMIN_API_KEY));
        }
    }

    @AfterEach
    void tearDown() {
        bankRepository.deleteAll();
        mortgageRepository.deleteAll();
    }

    @Test
    public void shouldEditMortgage() throws Exception {
        Mortgage updatedTestMortgage = new Mortgage(testOwnedMortgage.getId(), testOwnedBank.getId(), "Nueva Hipoteca", InterestRateType.VARIABLE, "actualizada!", 1.75);
        String updatedTestMortgageJson = "{\"name\":\"Nueva Hipoteca\",\"interestRateType\":\"VARIABLE\",\"description\":\"actualizada!\",\"apr\":1.75}";

        MvcResult result = mockMvc.perform(put("/banks/{bankId}/mortgages/{mortgageId}", testOwnedBank.getId(), testOwnedMortgage.getId())
                .header(API_AUTH_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(updatedTestMortgageJson)
        ).andExpect(status().isOk()).andReturn();

        assertThat(mapper.readValue(result.getResponse().getContentAsString(), Mortgage.class)
                .equals(updatedTestMortgage)
        ).isTrue();
    }

    @Test
    public void shouldReturn404WhenBankDoesNotExist() throws Exception {
        mockMvc.perform(put("/banks/-1/mortgages/{mortgageId}", testOwnedMortgage.getId())
                .header(API_AUTH_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(UPDATE_MORTGAGE_JSON_PLACEHOLDER)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturn404WhenMortgageDoesNotExist() throws Exception {
        mockMvc.perform(put("/banks/{bankId}/mortgages/-1", testOwnedBank.getId())
                .header(API_AUTH_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(UPDATE_MORTGAGE_JSON_PLACEHOLDER)
        ).andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnForbiddenWhenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/banks/{bankId}/mortgages/{mortgageId}", testOwnedBank.getId(), testOwnedMortgage.getId())
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(UPDATE_MORTGAGE_JSON_PLACEHOLDER)
        ).andExpect(status().isForbidden());
    }

    @Test
    public void shouldReturnForbiddenWhenNotOwner() throws Exception {
        mockMvc.perform(put("/banks/{bankId}/mortgages/{mortgageId}", adminOwnedBank.getId(), adminOwnedMortgage.getId())
                .header(API_AUTH_HEADER, TEST_API_KEY)
                .contentType(MediaType.APPLICATION_JSON.toString())
                .content(UPDATE_MORTGAGE_JSON_PLACEHOLDER)
        ).andExpect(status().isForbidden());
    }
}
