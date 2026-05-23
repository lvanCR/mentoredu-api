package com.mentoredu.community.service;

import com.mentoredu.community.dto.CreateVerificationRequest;
import com.mentoredu.community.dto.ReviewVerificationRequest;
import com.mentoredu.community.dto.VerificationResponse;

import java.util.List;
import java.util.UUID;

public interface IVerificationService {
    /**
     * Envía una nueva solicitud de verificación.
     * @param request Datos de la solicitud y documentos.
     * @param userEmail Email del usuario que solicita.
     * @return Respuesta con los datos de la solicitud creada.
     */
    VerificationResponse submit(CreateVerificationRequest request, String userEmail);

    /**
     * Obtiene todas las solicitudes de verificación del usuario actual.
     * @param userEmail Email del usuario.
     * @return Lista de solicitudes.
     */
    List<VerificationResponse> getMyRequests(String userEmail);

    /**
     * Obtiene todas las solicitudes de verificación del sistema (para moderadores).
     * @return Lista de todas las solicitudes.
     */
    List<VerificationResponse> getAllRequests();

    /**
     * Revisa (aprueba o rechaza) una solicitud de verificación.
     * @param requestId ID de la solicitud.
     * @param request Datos de la revisión (acción y notas).
     * @param reviewerEmail Email del moderador/admin que revisa.
     * @return Respuesta con los datos actualizados.
     */
    VerificationResponse review(UUID requestId, ReviewVerificationRequest request, String reviewerEmail);
}
