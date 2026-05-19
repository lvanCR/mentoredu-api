# HU32 — Earn and view badges

**Epic**: EP-10 — Gamification  
**Endpoint principal**: `GET /api/v1/gamification/users/{userId}/badges`

---

## Descripción

Como usuario autenticado, quiero ganar insignias por hitos de participación y visualizarlas en mi perfil.

Las insignias se otorgan **automáticamente** por el sistema al procesar eventos de XP (RN-31). No existe un endpoint de escritura directa de insignias.

---

## Endpoint

| Método | URL | Auth |
|---|---|---|
| `GET` | `/api/v1/gamification/users/{userId}/badges` | Bearer JWT |

### Path params

| Parámetro | Tipo | Descripción |
|---|---|---|
| `userId` | UUID | ID del usuario cuyas insignias se consultan |

### Headers

```
Authorization: Bearer {{access_token}}
```

### Respuesta exitosa (200)

```json
[
  {
    "badgeId": "uuid",
    "code": "FIRST_ANSWER",
    "name": "Primera Respuesta",
    "description": "Otorgada al publicar tu primera respuesta en el foro.",
    "earnedAt": "2026-05-18T10:00:00"
  }
]
```

Devuelve lista vacía `[]` si el usuario aún no ha ganado ninguna insignia (sin error).

---

## Insignias del sistema (seeded en V6)

| Código | Nombre | Hito |
|---|---|---|
| `FIRST_ANSWER` | Primera Respuesta | 1 respuesta en el foro |
| `FIRST_RESOURCE` | Primer Recurso | 1 recurso académico publicado |
| `LEVEL_5` | Nivel 5 Alcanzado | Alcanzar nivel 5 (500 XP) |
| `LEVEL_10` | Nivel 10 Alcanzado | Alcanzar nivel 10 (1000 XP) |

---

## Reglas de negocio

- **RN-31**: Las insignias se otorgan únicamente por acciones definidas por el sistema.
- **RN-33**: Una insignia no puede asignarse más de una vez al mismo usuario (deduplicación silenciosa).

---

## Escenarios Gherkin → casos de prueba

| Escenario | Archivo | Status esperado |
|---|---|---|
| Exitoso — hito alcanzado, insignia acreditada | `caso-01.json` | 200 con lista de insignias |
| Error — sin autenticación | `caso-02.json` | 401 Unauthorized |
| Alternativo exitoso — consulta de insignias con nombre y fecha | `caso-03.json` | 200 con campo `earnedAt` presente |
| Alternativo error — hito no alcanzado, sin insignia y sin error | `caso-04.json` | 200 con lista vacía |
