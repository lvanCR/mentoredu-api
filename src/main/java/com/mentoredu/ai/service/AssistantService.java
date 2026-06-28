package com.mentoredu.ai.service;

import com.mentoredu.ai.dto.SolutionInsight;
import com.mentoredu.ai.dto.SolutionReportResponse;
import com.mentoredu.pedagogy.dto.SolutionResponse;
import com.mentoredu.pedagogy.service.ISolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssistantService {

    private final ChatClient assistantChatClient;
    private final ChatModel chatModel;
    private final ISolutionService solutionService;

    public String chat(String message) {
        return assistantChatClient.prompt()
            .user(message)
            .call()
            .content();
    }

    public SolutionReportResponse report(UUID resourceId, String requesterEmail) {
        List<SolutionResponse> solutions = solutionService.listByResource(resourceId, requesterEmail);

        SolutionInsight insight = ChatClient.create(chatModel)
            .prompt()
            .system("""
                You are a MentorEdu academic analyst. Given the list of student solutions for an exercise,
                write a brief summary (resumen) and a recommendation (recomendacion) for the teacher.
                Use ONLY the provided data. Do not invent numbers or names.
                Write both fields in Spanish.
                """)
            .user(solutions.toString())
            .call()
            .entity(SolutionInsight.class);

        return new SolutionReportResponse(solutions, insight);
    }
}
