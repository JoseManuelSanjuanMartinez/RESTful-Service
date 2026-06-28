package org.example.banksproject.controller;

import org.example.banksproject.models.Bank;
import org.example.banksproject.models.Mortgage;
import org.example.banksproject.models.ApiUser;
import org.example.banksproject.models.InterestRateType;
import org.example.banksproject.models.PrivateMortgageAttachment;
import org.example.banksproject.repositories.ApiUsersRepository;
import org.example.banksproject.repositories.BankRepository;
import org.example.banksproject.repositories.MortgageAttachmentRepository;
import org.example.banksproject.repositories.MortgageRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"attachments.storage.path=target/test-attachments"})
@AutoConfigureMockMvc
public class MortgageControllerAttachmentTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApiUsersRepository apiUsersRepository;

    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private MortgageRepository mortgageRepository;

    @Autowired
    private MortgageAttachmentRepository mortgageAttachmentRepository;

    @Value("${attachments.storage.path}")
    private String storagePath;

    private Bank bank;
    private Mortgage mortgage;
    private byte[] pdfBytes;
    // When true, the @AfterEach cleanup will skip deleting the uploaded attachments
    // This allows a test to leave the uploaded PDF in place for manual inspection.
    private boolean preserveAttachmentsAfterTest = false;

    private byte[] loadPdfTestFile() throws IOException {
        return Files.readAllBytes(Path.of("src/test/resources/test_pdf.pdf"));
    }

    @BeforeEach
    void setUp() throws IOException {
        Optional<ApiUser> a = apiUsersRepository.findByApiKey("admin-key");
        ApiUser adminUser = a.orElseThrow();

        bank = bankRepository.save(new Bank(null, "Test Bank", "SWIFT", "https://example.com", adminUser.getId()));
        mortgage = mortgageRepository.save(new Mortgage(null, bank.getId(), "M1", InterestRateType.FIJO, "desc", 1.0));

        pdfBytes = loadPdfTestFile();
    }


    @AfterEach
    void tearDown() throws IOException {
        if (preserveAttachmentsAfterTest) {
            // reset the flag for safety and skip cleanup so the uploaded PDF remains
            preserveAttachmentsAfterTest = false;
            return;
        }

        mortgageAttachmentRepository.deleteAll();
        mortgageRepository.deleteAll();
        bankRepository.deleteAll();

        Path testDir = Path.of(storagePath);
        if (Files.exists(testDir)) {
            Files.walk(testDir)
                    .sorted((a,b) -> b.compareTo(a))
                    .forEach(p -> {
                        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
                    });
        }
    }

    @Test
    void uploadDownloadAndDeleteByOwner() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", pdfBytes);

        // upload
        MockHttpServletResponse response = mockMvc.perform(multipart("/banks/{bankId}/mortgages/{mortgageId}/attachments", bank.getId(), mortgage.getId())
                        .file(file)
                        .header("ApiKey", "admin-key"))
                .andExpect(status().isCreated())
                .andReturn().getResponse();

        assertThat(response.getHeader("Location")).isNotNull();
        System.out.println(response.getContentAsString());

        // ensure metadata exists
        Iterable<PrivateMortgageAttachment> attachments = mortgageAttachmentRepository.findByMortgageId(mortgage.getId());
        PrivateMortgageAttachment saved = attachments.iterator().next();
        assertThat(saved.getFilename()).isEqualTo("contract.pdf");

        // download
        byte[] downloaded = mockMvc.perform(get("/banks/{bankId}/mortgages/{mortgageId}/attachments/{attachmentId}", bank.getId(), mortgage.getId(), saved.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(downloaded).isEqualTo(pdfBytes);

        // delete as owner
        mockMvc.perform(delete("/banks/{bankId}/mortgages/{mortgageId}/attachments/{attachmentId}", bank.getId(), mortgage.getId(), saved.getId())
                        .header("ApiKey", "admin-key"))
                .andExpect(status().isNoContent());
    }

    @Test
    void uploadRejectedForNonPdf() throws Exception {
        byte[] txt = "hello".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "readme.txt", "text/plain", txt);

        mockMvc.perform(multipart("/banks/{bankId}/mortgages/{mortgageId}/attachments", bank.getId(), mortgage.getId())
                        .file(file)
                        .header("ApiKey", "admin-key"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteForbiddenForNonOwner() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "contract.pdf", "application/pdf", pdfBytes);

        // upload as owner
        mockMvc.perform(multipart("/banks/{bankId}/mortgages/{mortgageId}/attachments", bank.getId(), mortgage.getId())
                        .file(file)
                        .header("ApiKey", "admin-key"))
                .andExpect(status().isCreated());

        PrivateMortgageAttachment saved = mortgageAttachmentRepository.findByMortgageId(mortgage.getId()).iterator().next();

        // attempt delete as different user
        mockMvc.perform(delete("/banks/{bankId}/mortgages/{mortgageId}/attachments/{attachmentId}", bank.getId(), mortgage.getId(), saved.getId())
                        .header("ApiKey", "test-key"))
                .andExpect(status().isForbidden());
    }

    @Test
    void uploadAndKeepAttachment() throws Exception {
        // mark to preserve uploaded attachment after the test
        // ATTENTION: only works if the test is guaranteed to be executed the last one (or individually)
        preserveAttachmentsAfterTest = true;

        MockMultipartFile file = new MockMultipartFile("file", "kept_contract.pdf", "application/pdf", pdfBytes);

        // upload
         String modelLocation = mockMvc.perform(multipart("/banks/{bankId}/mortgages/{mortgageId}/attachments", bank.getId(), mortgage.getId())
                         .file(file)
                         .header("ApiKey", "admin-key"))
                 .andExpect(status().isCreated()).andReturn().getResponse().getHeader("Location");

        // ensure the PFD was saved
        byte[] downloaded = mockMvc.perform(get(modelLocation))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        assertThat(downloaded).isEqualTo(pdfBytes);
    }
}
