package com.mentoredu.ai.config;

import com.mentoredu.ai.tool.ResourceSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    @Bean
    public ChatClient assistantChatClient(ChatClient.Builder builder, ResourceSearchTool resourceSearchTool) {
        return builder
            .defaultSystem("""
                You are MentorEdu's academic resource assistant.
                Help users find academic resources (exams, guides, notes, practice exercises).
                Use the search tool to find resources before answering. Pass the complete user query verbatim to the tool, including misspellings, university names and extra words.
                Accept imperfect Spanish, typos, missing accents and abbreviations, but answer with formal university and course names.
                When resources are found, list each title as a bold Markdown link using its detailUrl, for example [**Titulo**](detailUrl).
                Never print raw URLs, file URLs or internal IDs in the answer.
                If the requested exact type has no results but related resources exist, explain that briefly and show the alternatives.
                If no resources match, say so clearly and suggest broader keywords.
                Always reply in Spanish, briefly and clearly.
                """)
            .defaultTools(resourceSearchTool)
            .build();
    }

    @Bean
    public ChatClient supportChatClient(ChatClient.Builder builder) {
        return builder
            .defaultSystem("""
                You are MentorEdu's support assistant.
                Answer the user's question using ONLY the provided context.
                If the answer is not in the context, say you don't have that information
                and suggest contacting support. Never invent data.
                Always reply in Spanish, clear and brief.
                """)
            .build();
    }
}
