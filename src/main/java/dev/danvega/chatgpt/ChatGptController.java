package dev.danvega.chatgpt;

import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
public class ChatGptController {

    private static final Logger log = LoggerFactory.getLogger(ChatGptController.class);

//    Direct chalane ke liye simple
    private static final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent";


//   Direct chalane ke liye simple
    private static final  String geminiApiKey="AIzaSyAGfufx9golkO_V1xUSdWZb7bKSLRoMYtU";

    // Construct the complete API URL with the API key
    String apiUrlWithKey = geminiApiUrl + "?key=" + geminiApiKey;

    private final RestTemplate restTemplate;

    public ChatGptController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("")
    public String home() {
        return "index";
    }

    @HxRequest
    @PostMapping("/api/chat")
    public HtmxResponse generate(@RequestParam String message, Model model) {
        log.info("User Message: {}", message);

        // Create headers for the request
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        // Prepare the body of the request
        Map<String, Object> part = new HashMap<>();
        part.put("text", message);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", new Map[]{part});

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", new Map[]{content});

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // Log the URL for debugging
        log.info("API URL: {}", apiUrlWithKey);

        // Call the Gemini API
        ResponseEntity<Map> response = restTemplate.exchange(
                apiUrlWithKey, // Use the URL with the API key
                HttpMethod.POST,
                entity,
                Map.class
        );

        // Extract the response content (assuming 'content' is the correct key)
//        String responseContent = (String) response.getBody().get("content");
//ye extraction ke liye h debugger use krlena repsonse dkh jayega
        // Extract the 'text' part from the response
        String responseText = "";
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<String, Object> firstCandidate = candidates.get(0);
                Map<String, Object> contentMap = (Map<String, Object>) firstCandidate.get("content");
                List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                if (parts != null && !parts.isEmpty()) {
                    responseText = (String) parts.get(0).get("text");
                }
            }
        }
        log.info("responseText is: {}", responseText);
        model.addAttribute("response", responseText);
        model.addAttribute("message", message);

        return HtmxResponse.builder()
                .view("response :: responseFragment")
                .view("recent-message-list :: messageFragment")
                .build();
    }
}
