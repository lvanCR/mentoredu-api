# HU28 — Comment on forum answer

## Endpoints

| Método | URL | Descripción |
|---|---|---|
| `POST` | `/api/v1/answers/{answerId}/comments` | Publicar un comentario sobre una respuesta |
| `GET` | `/api/v1/answers/{answerId}/comments` | Listar comentarios de una respuesta |

## Headers requeridos

```
Authorization: Bearer {{access_token}}
Content-Type: application/json
```

## Body fields (POST)

| Campo | Tipo | Requerido | Validación |
|---|---|---|---|
| `body` | `string` | Sí | No puede estar vacío (`@NotBlank`) |

## Reglas de negocio

- El comentario debe tener contenido válido (no vacío).
- La respuesta (`answerId`) debe existir; si no, el sistema devuelve 404.
- El sistema asocia el comentario a la respuesta y al hilo al que pertenece.
- Requiere autenticación JWT en todos los endpoints.

## Escenarios Gherkin

| Escenario | Archivo | Status esperado |
|---|---|---|
| Exitoso: comentario válido sobre respuesta existente | `caso-01-exitoso.json` | 201 |
| Error: body vacío | `caso-02-body-vacio.json` | 400 |
| Alternativo error: respuesta inexistente | `caso-03-respuesta-inexistente.json` | 404 |
| Alternativo error: sin autenticación | `caso-04-sin-autenticacion.json` | 401 |

## Response body (201 - creado)

```json
{
  "id": "uuid",
  "answerId": "uuid",
  "threadId": "uuid",
  "body": "Exactamente, ese es el punto clave.",
  "authorDisplay": "Juan Pérez",
  "createdAt": "2026-05-17T12:00:00",
  "updatedAt": "2026-05-17T12:00:00"
}
```
