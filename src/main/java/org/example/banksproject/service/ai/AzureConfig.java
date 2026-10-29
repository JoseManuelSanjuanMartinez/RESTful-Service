package org.example.banksproject.service.ai;

import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClient;
import com.azure.ai.formrecognizer.documentanalysis.DocumentAnalysisClientBuilder;
import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {

    @Value("${azure.documentintelligence.endpoint}")
    private String endpoint;

    @Value("${azure.documentintelligence.api-key}")
    private String apiKey;

    @Value("${azure.openai.endpoint}")
    private String openAIEndpoint;

    @Value("${azure.openai.api-key}")
    private String openAIApiKey;

    @Bean
    public DocumentAnalysisClient documentAnalysisClient() {
        return new DocumentAnalysisClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(apiKey))
                .buildClient();
    }

    @Bean
    public OpenAIClient openAIClient() {
        return new OpenAIClientBuilder()
                .endpoint(openAIEndpoint)
                .credential(new AzureKeyCredential(openAIApiKey))
                .buildClient();
    }
}