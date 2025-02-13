package com.resume.backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class ResumeServiceImpl implements ResumeService {

    private final ChatClient chatClient;

    public ResumeServiceImpl(ChatClient.Builder builder) {
        this.chatClient = Objects.requireNonNull(builder, "ChatClient.Builder cannot be null").build();
    }

    @Override
    public Map<String, Object> generateResumeResponse(String userResumeDescription) throws IOException {
        String promptString = this.loadPromptFromFile("resume_prompt.txt");

        // Handle null user description
        String promptContent = this.putValuesToTemplate(promptString, Map.of(
                "userDescription", userResumeDescription != null ? userResumeDescription : ""
        ));

        Prompt prompt = new Prompt(promptContent);

        // Check if chatClient is initialized
        if (chatClient == null) {
            throw new IllegalStateException("ChatClient is not initialized.");
        }

        String response = chatClient.prompt(prompt).call().content();
        return parseMultipleResponses(response);
    }

    String loadPromptFromFile(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource(filename);
        if (!resource.exists()) {
            throw new IOException("Prompt file not found: " + filename);
        }
        return Files.readString(resource.getFile().toPath());
    }

    String putValuesToTemplate(String template, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return template;
    }

    public static Map<String, Object> parseMultipleResponses(String response) {
        Map<String, Object> jsonResponse = new HashMap<>();

        if (response == null) {
            jsonResponse.put("think", null);
            jsonResponse.put("data", null);
            return jsonResponse;
        }

        // Extract content inside <think> tags
        int thinkStart = response.indexOf("<think>") + 7;
        int thinkEnd = response.indexOf("</think>");
        if (thinkStart != -1 && thinkEnd != -1) {
            jsonResponse.put("think", response.substring(thinkStart, thinkEnd).trim());
        } else {
            jsonResponse.put("think", null);
        }

        // Extract JSON formatted content
        int jsonStart = response.indexOf("```json");
        int jsonEnd = response.lastIndexOf("```");

        if (jsonStart != -1 && jsonEnd != -1 && jsonStart < jsonEnd) {
            jsonStart += 7; // Skip "```json"
            String jsonContent = response.substring(jsonStart, jsonEnd).trim();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                jsonResponse.put("data", objectMapper.readValue(jsonContent, Map.class));
            } catch (Exception e) {
                jsonResponse.put("data", null);
                System.err.println("Invalid JSON format: " + e.getMessage());
            }
        } else {
            jsonResponse.put("data", null);
        }

        return jsonResponse;
    }
}





//package com.resume.backend.services;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.json.JSONObject;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.prompt.Prompt;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class ResumeServiceImpl implements ResumeService{
//
//    private ChatClient chatClient;
//
//    public ResumeServiceImpl(ChatClient.Builder builder) {
//        this.chatClient = builder.build();
//    }
//
//    @Override
//    public Map<String , Object> generateResumeResponse(String userResumeDiscription) throws IOException {
//        String promptString = this.loadPromptFromFile("resume_prompt.txt");
//        String promptContent = this.putValuesToTemplate(promptString, Map.of(
//                "userDescription", userResumeDiscription
//        ));
//        Prompt prompt = new Prompt(promptContent);
//
//        String response = chatClient.prompt(prompt).call().content();
//        Map<String , Object> stringObjectMap = parseMultipleResponses(response);
//        //modify :
//        return stringObjectMap;
//    }
//
//    String loadPromptFromFile(String filename) throws IOException {
//        Path path = new ClassPathResource(filename).getFile().toPath();
//        return Files.readString(path);
//    }
//
//    String putValuesToTemplate(String template  , Map<String ,String> values){
//        for(Map.Entry<String  , String> entry : values.entrySet()){
//           template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
//        };
//        return  template;
//    }
//
//
//    public static Map<String, Object> parseMultipleResponses(String response) {
//        Map<String, Object> jsonResponse = new HashMap<>();
//
//        // Extract content inside <think> tags
//        int thinkStart = response.indexOf("<think>") + 7;
//        int thinkEnd = response.indexOf("</think>");
//        if (thinkStart != -1 && thinkEnd != -1) {
//            String thinkContent = response.substring(thinkStart, thinkEnd).trim();
//            jsonResponse.put("think", thinkContent);
//        } else {
//            jsonResponse.put("think", null); // Handle missing <think> tags
//        }
//
//        // Extract content that is in JSON format
//        int jsonStart = response.indexOf("```json") + 7; // Start after ```json
//        int jsonEnd = response.lastIndexOf("```");       // End before ```
//        if (jsonStart != -1 && jsonEnd != -1 && jsonStart < jsonEnd) {
//            String jsonContent = response.substring(jsonStart, jsonEnd).trim();
//            try {
//                // Convert JSON string to Map using Jackson ObjectMapper
//                ObjectMapper objectMapper = new ObjectMapper();
//                Map<String, Object> dataContent = objectMapper.readValue(jsonContent, Map.class);
//                jsonResponse.put("data", dataContent);
//            } catch (Exception e) {
//                jsonResponse.put("data", null); // Handle invalid JSON
//                System.err.println("Invalid JSON format in the response: " + e.getMessage());
//            }
//        } else {
//            jsonResponse.put("data", null); // Handle missing JSON
//        }
//
//        return jsonResponse;
//    }
//}