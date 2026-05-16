package com.featureflow.integration.connector.jira;

import com.featureflow.integration.connector.jira.model.*;
import com.featureflow.integration.model.ConnectorConfig;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Component
public class JiraClient {

    private final WebClient.Builder webClientBuilder;

    public JiraClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    private WebClient jiraWebClient(ConnectorConfig config) {
        return webClientBuilder
            .baseUrl(config.baseUrl())
            .defaultHeaders(headers -> {
                if (config.authToken() != null) {
                    headers.setBearerAuth(config.authToken());
                } else if (config.apiToken() != null) {
                    String auth = config.username() + ":" + config.apiToken();
                    String encoded = Base64.getEncoder().encodeToString(auth.getBytes());
                    headers.setBasicAuth(encoded);
                }
            })
            .build();
    }

    public List<JiraIssue> searchIssues(ConnectorConfig config, String jql, int maxResults) {
        JiraSearchResponse response = jiraWebClient(config)
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/rest/api/3/search")
                .queryParam("jql", jql)
                .queryParam("maxResults", maxResults)
                .queryParam("fields", "summary,description,status,priority,labels," +
                    "assignee,components,customfield_10016,customfield_10014," +
                    "issuelinks,created,updated,duedate,project,timetracking")
                .build()
            )
            .retrieve()
            .bodyToMono(JiraSearchResponse.class)
            .block(Duration.ofMinutes(5));

        return response != null ? response.issues() : List.of();
    }

    public JiraMyself getMyself(ConnectorConfig config) {
        return jiraWebClient(config)
            .get()
            .uri("/rest/api/3/myself")
            .retrieve()
            .bodyToMono(JiraMyself.class)
            .block(Duration.ofSeconds(10));
    }

    public List<JiraSprint> getSprints(ConnectorConfig config, String boardId) {
        JiraSprintResponse response = jiraWebClient(config)
            .get()
            .uri("/rest/agile/1.0/board/{boardId}/sprint", boardId)
            .retrieve()
            .bodyToMono(JiraSprintResponse.class)
            .block(Duration.ofMinutes(2));

        return response != null ? response.sprints() : List.of();
    }

    public List<JiraBoard> getBoards(ConnectorConfig config, String projectKey) {
        JiraBoardResponse response = jiraWebClient(config)
            .get()
            .uri(uriBuilder -> uriBuilder
                .path("/rest/agile/1.0/board")
                .queryParam("projectKeyOrId", projectKey)
                .build()
            )
            .retrieve()
            .bodyToMono(JiraBoardResponse.class)
            .block(Duration.ofSeconds(30));

        return response != null ? response.boards() : List.of();
    }

    public void updateIssue(ConnectorConfig config, String issueKey, JiraUpdateRequest request) {
        jiraWebClient(config)
            .put()
            .uri("/rest/api/3/issue/{key}", issueKey)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(Void.class)
            .block(Duration.ofSeconds(30));
    }

    public JiraIssue getIssue(ConnectorConfig config, String issueKey) {
        return jiraWebClient(config)
            .get()
            .uri("/rest/api/3/issue/{key}", issueKey)
            .retrieve()
            .bodyToMono(JiraIssue.class)
            .block(Duration.ofSeconds(10));
    }
}
