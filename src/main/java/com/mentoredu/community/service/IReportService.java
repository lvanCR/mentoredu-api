package com.mentoredu.community.service;

import com.mentoredu.community.dto.ReportRequest;
import com.mentoredu.community.dto.ReportResponse;
import com.mentoredu.community.dto.ResolveReportRequest;

import java.util.List;
import java.util.UUID;

public interface IReportService {
    /**
     * Crea un nuevo reporte de contenido inapropiado.
     * @param request Datos del reporte.
     * @param reporterEmail Email del usuario que reporta.
     * @return El reporte creado.
     */
    ReportResponse create(ReportRequest request, String reporterEmail);

    /**
     * Lista todos los reportes que aún no han sido resueltos.
     * @return Lista de reportes abiertos.
     */
    List<ReportResponse> listOpen();

    /**
     * Resuelve un reporte existente.
     * @param reportId ID del reporte a resolver.
     * @param request Datos de la resolución.
     * @param resolverEmail Email del moderador/admin que resuelve.
     * @return El reporte resuelto.
     */
    ReportResponse resolve(UUID reportId, ResolveReportRequest request, String resolverEmail);
}
