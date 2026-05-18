package com.kotak.onboarding.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class MoxoService {

    @Value("${moxo.api.base}")   private String apiBase;
    @Value("${moxo.org.id}")     private String orgId;
    @Value("${moxo.client.id}")  private String clientId;
    @Value("${moxo.client.secret}") private String clientSecret;
    @Value("${moxo.admin.email}") private String adminEmail;
    @Value("${moxo.webhook.url}") private String webhookUrl;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public String getToken(String email) throws Exception {
        Map<String, String> body = new HashMap<>();
        body.put("client_id", clientId);
        body.put("client_secret", clientSecret);
        body.put("email", email);
        body.put("org_id", orgId);

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(apiBase + "/v1/core/oauth/token"))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
            .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode json = mapper.readTree(resp.body());
        JsonNode token = json.path("access_token");
        if (token.isMissingNode()) token = json.path("data").path("access_token");
        return token.asText();
    }

    public String triggerWebhookAndAssignNrm(
        String fullName, String email, String phone, String state,
        String rmEmail, String rmName
    ) throws Exception {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        int hr12 = now.getHour() % 12 == 0 ? 12 : now.getHour() % 12;
        String ampm = now.getHour() >= 12 ? "PM" : "AM";
        String workspaceName = String.format(
            "Account Opening - %s - %d %s %d:%02d %s",
            fullName, now.getDayOfMonth(),
            now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH),
            hr12, now.getMinute(), ampm
        );

        Map<String, String> body = new HashMap<>();
        body.put("workspace_name", workspaceName);
        body.put("workspace_description", "Workflow for account opening with Kotak Mahindra Bank.");
        body.put("workspace_owner", adminEmail);
        body.put("role.Client.email", email);
        body.put("role.Client.user_name", fullName);
        body.put("role.Client.phone_number", "+91" + phone);
        body.put("role.NRM.email", rmEmail);
        body.put("role.NRM.user_name", rmName);
        body.put("workspace_variable.location", state);

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(webhookUrl))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
            .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        JsonNode json = mapper.readTree(resp.body());

        // Try common binder ID paths in the response
        for (String path : new String[]{"data.board.id", "data.workspace_id", "data.binder_id"}) {
            String[] parts = path.split("\\.");
            JsonNode node = json;
            for (String p : parts) node = node.path(p);
            if (!node.isMissingNode() && !node.asText().isBlank()) return node.asText();
        }
        return null;
    }
}
