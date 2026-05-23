# HU15 — Download academic resource

**Endpoint:** `GET /api/v1/resources/{resource_id}/download`
**Auth:** Bearer JWT requerido
**Respuesta exitosa:** `200 OK` con body `application/pdf` y header `Content-Disposition: attachment; filename="..."`

## Regla de acceso (RN-15)

| Visibilidad | Condición de acceso |
|---|---|
| `PUBLIC` | Cualquier usuario autenticado |
| `PREMIUM` | Suscripción activa (`status=ACTIVE`, `ends_at` vigente) **O** `coin_wallet.balance > 0` |
| `PRIVATE` | Solo el autor del recurso |

## Casos de prueba

| Caso | Escenario | Resultado esperado |
|---|---|---|
| [caso-01](./caso-01.json) | Recurso PUBLIC disponible, usuario autenticado | `200 OK` + PDF |
| [caso-02](./caso-02.json) | Recurso no existe | `404 Not Found` |
| [caso-03](./caso-03.json) | Recurso PRIVATE y usuario no es el autor | `403 Forbidden` |
| [caso-04](./caso-04.json) | Recurso PREMIUM sin suscripción ni monedas | `403 Forbidden` |

## Prerrequisitos

1. Ejecutar HU02 para obtener `{{access_token}}`
2. Ejecutar HU12 + HU13 para obtener `{{resource_id}}` de un recurso PUBLIC

## Notas

- La descarga queda registrada en la tabla `download_logs`.
- Si el archivo físico no existe en disco (aunque el registro en BD sí), el sistema devuelve `503 Service Unavailable`.
- El header de respuesta en éxito es `Content-Disposition: attachment; filename="nombre-original.pdf"`.
