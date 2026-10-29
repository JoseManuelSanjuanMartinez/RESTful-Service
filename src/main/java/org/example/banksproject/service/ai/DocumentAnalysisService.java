package org.example.banksproject.service.ai;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentParagraph;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.ChatCompletions;
import com.azure.ai.openai.models.ChatCompletionsOptions;
import com.azure.ai.openai.models.ChatRequestMessage;
import com.azure.ai.openai.models.ChatRequestSystemMessage;
import com.azure.ai.openai.models.ChatRequestUserMessage;

import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import org.example.banksproject.models.Mortgage;
import org.example.banksproject.repositories.MortgageAttachmentRepository;
import org.example.banksproject.repositories.MortgageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DocumentAnalysisService {

    private final DocumentAnalysisClient documentAnalysisClient;
    private final OpenAIClient openAIClient;
    private final MortgageRepository mortgageRepository;
    private final MortgageAttachmentRepository mortgageAttachmentRepository;

    @Value("${azure.openai.deployment-name}")
    private String deploymentName;

    public DocumentAnalysisService(DocumentAnalysisClient documentAnalysisClient, OpenAIClient openAIClient, MortgageRepository mortgageRepository, MortgageAttachmentRepository mortgageAttachmentRepository) {
        this.documentAnalysisClient = documentAnalysisClient;
        this.openAIClient = openAIClient;
        this.mortgageRepository = mortgageRepository;
        this.mortgageAttachmentRepository = mortgageAttachmentRepository;
    }

    @Async
    public void analyzeMortgageAttachments(Mortgage mortgage) {
        List<byte[]> bytesList = new ArrayList<>();
        mortgageAttachmentRepository.findByMortgageId(mortgage.getId())
                .forEach(attachment -> bytesList.add(attachment.getFileBytes()));
        byte[][] attachmentsBytes = bytesList.toArray(new byte[0][]);

        String updatedDescription = generateMortgageSummary(attachmentsBytes);

        Mortgage updatedMortgage = new Mortgage(
                mortgage.getId(),
                mortgage.getBankId(),
                mortgage.getName(),
                mortgage.getInterestRateType(),
                updatedDescription, // <--- Updated mortgage summary
                mortgage.getApr()
        );
        mortgageRepository.save(updatedMortgage);
    }

    private String generateMortgageSummary(byte[][] attachmentsBytes) {

        StringBuilder extractedText = new StringBuilder();
        for (byte[] attachmentBytes : attachmentsBytes) {
            try {
                extractText(attachmentBytes).ifPresent(text -> {
                    if (!extractedText.isEmpty()) {
                        extractedText.append("\n\n");
                    }
                    extractedText.append(text);
                });
            } catch (IOException e) {
                throw new UncheckedIOException("Could not extract text from a mortgage attachment", e);
            }
        }

        if (extractedText.isEmpty()) {
            throw new IllegalStateException("No text could be extracted from the mortgage attachments");
        }

        List<ChatRequestMessage> chatMessages = List.of(
                new ChatRequestUserMessage("Documentos a analizar:\n" + extractedText)
        );
        ChatCompletions chatCompletions = openAIClient.getChatCompletions(
                deploymentName,
                new ChatCompletionsOptions(chatMessages)
        );
        return chatCompletions.getChoices().get(0).getMessage().getContent();
    }

    private Optional<String> extractText(byte[] fileBytes) throws IOException {
        BinaryData content = BinaryData.fromBytes(fileBytes);
        SyncPoller<OperationResult, AnalyzeResult> analyzeDocumentPoller =
                documentAnalysisClient.beginAnalyzeDocument("prebuilt-document", content);

        AnalyzeResult analyzeResult = analyzeDocumentPoller.getFinalResult();

        String extractedText = analyzeResult.getParagraphs().stream()
                .map(DocumentParagraph::getContent)
                .collect(Collectors.joining("\n"));

        if (extractedText.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(extractedText);
    }
}