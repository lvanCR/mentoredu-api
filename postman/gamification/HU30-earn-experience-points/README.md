# HU30 — Earn experience points

**Endpoint verificación**: `GET /api/v1/gamification/users/{userId}/points`  
**Auth requerida**: Bearer JWT

> Los puntos de experiencia (XP) se generan internamente por eventos del sistema (RN-31).
> No existe un endpoint de escritura directa. Para probar HU30 en Postman, primero
> realiza una acción recompensada (responder un hilo o publicar un recurso), luego
> consulta el total de XP para confirmar que el sistema lo registró.

---

## Acciones recompensadas

| Acción | Puntos | sourceType |
|---|---|---|
| Responder un hilo (`POST /api/v1/threads/{id}/answers`) | +5 XP | `ANSWER_GIVEN` |
| Publicar un recurso (`POST /api/v1/resources`) | +10 XP | `RESOURCE_PUBLISHED` |

---

## Casos de prueba

### caso-01.json — Exitoso: acción recompensada registra XP

Prerrequisito: el usuario con `{{userId}}` ha respondido al menos un hilo.

```
GET {{api_v1}}/gamification/users/{{userId}}/points
Authorization: Bearer {{access_token}}
```

**Respuesta esperada (200)**:
```json
{
  "userId": "{{userId}}",
  "totalPoints": 5
}
```

---

### caso-02.json — Error: acción ya procesada, no genera duplicados (RN-34)

Prerrequisito: el evento con mismo `sourceType` + `sourceId` ya fue procesado.

El sistema no añade puntos duplicados. Consultar XP devuelve el mismo total que antes.

```
GET {{api_v1}}/gamification/users/{{userId}}/points
Authorization: Bearer {{access_token}}
```

**Respuesta esperada (200)**: misma cantidad de puntos, sin incremento.

---

### caso-03.json — Alternativo exitoso: suficientes XP → nivel actualizado

Prerrequisito: el usuario acumula suficientes XP (≥ 100 para nivel 2, según la lógica del sistema).

Verificar tanto `/points` como `/level`:

```
GET {{api_v1}}/gamification/users/{{userId}}/level
Authorization: Bearer {{access_token}}
```

**Respuesta esperada (200)**:
```json
{
  "userId": "{{userId}}",
  "currentLevel": 2,
  "experience": <xp_total>,
  "progressPercentage": <porcentaje>
}
```

---

### caso-04.json — Sin autenticación → 401 Unauthorized

```
GET {{api_v1}}/gamification/users/{{userId}}/points
```

**Respuesta esperada (401)**: sin body.

---

## Variables de entorno necesarias

| Variable | Descripción |
|---|---|
| `{{api_v1}}` | `http://localhost:8080/api/v1` |
| `{{access_token}}` | JWT del usuario autenticado |
| `{{userId}}` | UUID del usuario a consultar |
