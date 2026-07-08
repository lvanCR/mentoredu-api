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
        return "Estas reglas actuales tienen prioridad sobre cualquier guia importada si hay contradiccion. "
            + "Roles: estudiantes buscan recursos, participan en foro, siguen usuarios, completan su perfil y suben resoluciones cuando una practica acepta resoluciones; "
            + "docentes publican recursos, gestionan sus recursos, revisan resoluciones recibidas, dan feedback, usan analisis IA y participan en el foro; "
            + "academias publican recursos propios, gestionan publicaciones de docentes asociados, revisan resoluciones recibidas, usan analisis IA y gestionan asociaciones; "
            + "administradores moderan y pueden gestionar todo el contenido. "
            + "Edicion y eliminacion: estudiantes pueden editar o eliminar sus propias resoluciones, hilos, respuestas y comentarios desde sus paginas o desde el detalle del contenido; "
            + "docentes pueden editar o eliminar sus publicaciones, hilos, respuestas y comentarios propios; "
            + "academias pueden editar o eliminar sus publicaciones propias y las de docentes asociados, ademas de su contenido de foro; "
            + "administradores pueden editar o eliminar recursos, hilos, respuestas, comentarios y resoluciones de cualquier usuario. "
            + "Recursos: docentes y academias publican desde Biblioteca con Publicar recurso; editan o eliminan desde Mis recursos o desde el detalle del recurso cuando el boton aparece. "
            + "Resoluciones: estudiantes gestionan sus entregas desde Mis resoluciones o Mi resolucion en la practica; docentes, academias y administradores revisan desde Resoluciones recibidas. "
            + "Foro: para abrir un hilo se entra a Foro y se hace clic en el titulo o tarjeta del hilo; para crear uno se usa Publicar pregunta; el autor puede cerrar su hilo. "
            + "Tema visual: el modo oscuro o cambio de tema se activa desde Settings/Configuracion usando el control de Dark Mode o Modo oscuro. "
            + "Notificaciones: aparecen junto al usuario en el encabezado y pueden llevar al lugar donde ocurrio la actividad. "
            + "Analisis IA: desde Resoluciones recibidas o el detalle de resoluciones de una practica, docentes y academias pueden analizar una, varias o todas las resoluciones seleccionadas. "
            + "Si una accion no corresponde al rol autenticado, indicalo claramente y sugiere la alternativa disponible. No digas que no tienes informacion sobre editar, eliminar, abrir hilos o modo oscuro: usa estas reglas.";
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
            + "Roles: estudiantes buscan recursos, participan en foro, siguen usuarios y suben resoluciones cuando el recurso acepta resoluciones; docentes y academias publican recursos, revisan resoluciones recibidas y usan analisis IA; administradores moderan y gestionan todo. "
            + "Edicion y eliminacion: estudiantes gestionan sus propias resoluciones, hilos, respuestas y comentarios; docentes gestionan sus publicaciones y contenido propio; academias gestionan sus publicaciones y las de docentes asociados; administradores pueden intervenir sobre todo el contenido. "
            + "Recursos se editan o eliminan desde Mis recursos o el detalle del recurso. Resoluciones se gestionan desde Mis resoluciones o Resoluciones recibidas. Foro permite abrir hilos desde su tarjeta o titulo, publicar preguntas y cerrar hilos propios. "
            + "Settings permite activar o desactivar el modo oscuro. Analisis IA permite evaluar una, varias o todas las resoluciones seleccionadas. "
            + "Si una accion no corresponde al rol autenticado, indicalo claramente y sugiere la alternativa disponible.";
    }
}
