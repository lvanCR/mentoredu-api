# HU11 — Register Academic Offering

**Epic**: EP-03 Academy  
**Endpoints**:
- `POST /api/v1/academies/{academyId}/programs` — Registrar programa académico
- `POST /api/v1/academies/{academyId}/cycles` — Registrar ciclo académico

**Nombre en Postman**: `MentorEduAcademyHU11-CreateProgramPOST`, `MentorEduAcademyHU11-CreateCyclePOST`

---

## Prerequisitos

1. Obtener un access token de una cuenta `ORGANIZATION` (HU02 Login).
2. Tener el perfil de organización creado (HU10).
3. Tener una academia registrada y conocer su `academyId` (HU33).

---

## Casos de prueba — Programs

### caso-01: Programa exitoso (todos los campos)

**Request**:
```
POST {{api_v1}}/academies/{{academyId}}/programs
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "name": "Preparatorio UNI",
  "modality": "PRESENCIAL",
  "intensity": "INTENSIVO",
  "targetUniversity": "Universidad Nacional de Ingeniería"
}
```

**Esperado**: `201 Created`
```json
{
  "id": "<uuid>",
  "academyId": "<uuid>",
  "name": "Preparatorio UNI",
  "modality": "PRESENCIAL",
  "intensity": "INTENSIVO",
  "targetUniversity": "Universidad Nacional de Ingeniería",
  "createdAt": "...",
  "updatedAt": "..."
}
```

---

### caso-02: Programa con modalidad virtual

**Request**:
```
POST {{api_v1}}/academies/{{academyId}}/programs

{
  "name": "Preparatorio PUCP Online",
  "modality": "VIRTUAL",
  "intensity": "NORMAL",
  "targetUniversity": "PUCP"
}
```

**Esperado**: `201 Created`

---

### caso-03: Name vacío → 400

**Request**:
```json
{ "name": "", "modality": "PRESENCIAL", "intensity": "NORMAL", "targetUniversity": "UNI" }
```

**Esperado**: `400 Bad Request` con `details.name`

---

### caso-04: Campo obligatorio faltante (modality) → 400

**Request**:
```json
{ "name": "Preparatorio UNI", "intensity": "NORMAL", "targetUniversity": "UNI" }
```

**Esperado**: `400 Bad Request` con `details.modality`

---

### caso-05: Programa duplicado en la misma academia → 409

Registrar el mismo nombre de programa dos veces en la misma academia.

**Esperado**: `409 Conflict`
```json
{ "error": "Conflict", "message": "A program with this name already exists in the academy: ..." }
```

---

### caso-06: Academia no encontrada → 404

**Request**: `POST /api/v1/academies/<uuid-inexistente>/programs`

**Esperado**: `404 Not Found`

---

### caso-07: Tipo de cuenta incorrecto → 409

Usar un token de cuenta `STUDENT` o `TEACHER`.

**Esperado**: `409 Conflict`

---

### caso-08: Sin autenticación → 401

Sin header `Authorization`.

**Esperado**: `401 Unauthorized`

---

## Casos de prueba — Cycles

### caso-09: Ciclo exitoso

**Request**:
```
POST {{api_v1}}/academies/{{academyId}}/cycles
Authorization: Bearer {{access_token}}
Content-Type: application/json

{
  "name": "Ciclo Intensivo Verano 2026",
  "cycleType": "INTENSIVO",
  "startDate": "2026-01-06",
  "endDate": "2026-03-31"
}
```

**Esperado**: `201 Created`
```json
{
  "id": "<uuid>",
  "academyId": "<uuid>",
  "name": "Ciclo Intensivo Verano 2026",
  "cycleType": "INTENSIVO",
  "startDate": "2026-01-06",
  "endDate": "2026-03-31",
  "createdAt": "...",
  "updatedAt": "..."
}
```

---

### caso-10: Formato de fecha inválido → 400

**Request**:
```json
{
  "name": "Ciclo Verano",
  "cycleType": "REGULAR",
  "startDate": "06/01/2026",
  "endDate": "31/03/2026"
}
```

**Esperado**: `400 Bad Request`

---

### caso-11: Name vacío → 400

**Request**:
```json
{ "name": "", "cycleType": "REGULAR", "startDate": "2026-01-06", "endDate": "2026-03-31" }
```

**Esperado**: `400 Bad Request` con `details.name`

---

### caso-12: Ciclo duplicado en la misma academia → 409

Registrar el mismo nombre de ciclo dos veces en la misma academia.

**Esperado**: `409 Conflict`
```json
{ "error": "Conflict", "message": "A cycle with this name already exists in the academy: ..." }
```

---

### caso-13: endDate anterior a startDate → 400

**Request**:
```json
{
  "name": "Ciclo Inválido",
  "cycleType": "REGULAR",
  "startDate": "2026-06-01",
  "endDate": "2026-01-01"
}
```

**Esperado**: `400 Bad Request`

---

### caso-14: Academia no encontrada → 404

**Esperado**: `404 Not Found`

---

### caso-15: Sin autenticación → 401

**Esperado**: `401 Unauthorized`

---

## Valores permitidos

| Campo | Valores sugeridos |
|---|---|
| `modality` | `PRESENCIAL`, `VIRTUAL`, `HIBRIDO` |
| `intensity` | `NORMAL`, `INTENSIVO` |
| `cycleType` | `REGULAR`, `INTENSIVO`, `VACACIONAL` |
| `startDate` / `endDate` | `yyyy-MM-dd` (ej. `2026-01-06`) |
