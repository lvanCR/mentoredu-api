package com.mentoredu.ai.service;

import com.mentoredu.auth.entity.User;
import com.mentoredu.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final VectorStore vectorStore;
    private final UserService userService;
    private final ChatClient supportChatClient;

    public String ask(String question, String email) {
        User user = userService.findByEmailOrThrow(email);
        String context = findRelevantContext(question) + "\n\nReglas actuales de MentorEdu:\n" + currentRules();
        String roleContext = "Rol de la cuenta autenticada: " + user.getRole().getName() + ". "
            + "Responde solo con acciones disponibles para ese rol.";

        return supportChatClient.prompt()
            .user("Contexto:\n" + context + "\n\n" + roleContext + "\n\nPregunta: " + question)
            .call()
            .content();
    }

    public int ingest() throws IOException {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Resource[] files = resolver.getResources("classpath:docs/*.pdf");

        List<Document> docs = new ArrayList<>();
        for (Resource file : files) {
            docs.addAll(new PagePdfDocumentReader(file).get());
        }

        List<Document> chunks = new TokenTextSplitter().apply(docs);
        vectorStore.add(chunks);
        return chunks.size();
    }

    private String findRelevantContext(String question) {
        try {
            List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(4).build());

            String context = docs.stream()
                .map(Document::getText)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining("\n\n"));

            if (!context.isBlank()) return context;
        } catch (RuntimeException ignored) {
            // Local OpenRouter setups can run chat without OpenAI embeddings.
        }

        return loadMarkdownGuide();
    }

    private String currentRules() {
        return "Roles: estudiantes buscan recursos, participan en foro y suben resoluciones solo cuando el recurso acepta resoluciones; "
            + "docentes y academias publican recursos, revisan resoluciones recibidas y pueden usar analisis IA sobre las resoluciones de sus practicas; "
            + "administradores moderan y gestionan catalogos. "
            + "Edicion y eliminacion: estudiantes gestionan sus propias resoluciones, hilos, respuestas y comentarios; docentes gestionan sus publicaciones y contenido propio; "
            + "academias gestionan sus publicaciones y las de docentes asociados; administradores pueden intervenir sobre todo el contenido. "
            + "Analisis IA: desde Resoluciones recibidas o el detalle de resoluciones de una practica, docentes y academias pueden solicitar un resumen de patrones, fortalezas, errores frecuentes y recomendaciones. "
            + "Si una accion no corresponde al rol autenticado, indicalo claramente y sugiere la alternativa disponible.";
    }

    private String loadMarkdownGuide() {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource guide = resolver.getResource("classpath:docs/guia-mentoredu.md");
            if (guide.exists()) {
                return StreamUtils.copyToString(guide.getInputStream(), StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) {
            // The system prompt will handle insufficient context.
        }
        return "MentorEdu permite buscar recursos academicos, publicar documentos, gestionar foros, notificaciones y perfiles. "
            + "Roles: estudiantes buscan recursos, participan en foro y suben resoluciones solo cuando el recurso acepta resoluciones; docentes y academias publican recursos, revisan resoluciones recibidas y pueden usar analisis IA sobre las resoluciones de sus practicas; administradores moderan y gestionan catalogos. "
            + "Edicion y eliminacion: estudiantes gestionan sus propias resoluciones, hilos, respuestas y comentarios; docentes gestionan sus publicaciones y contenido propio; academias gestionan sus publicaciones y las de docentes asociados; administradores pueden intervenir sobre todo el contenido. "
            + "Analisis IA: desde Resoluciones recibidas o el detalle de resoluciones de una practica, docentes y academias pueden solicitar un resumen de patrones, fortalezas, errores frecuentes y recomendaciones para retroalimentar a estudiantes. "
            + "Si una accion no corresponde al rol autenticado, indicalo claramente y sugiere la alternativa disponible.";
    }
}