# Contrato da API CT Pablo Forlan BJJ

Base: `https://academia-jiu-jitsu-api.onrender.com` (instância Render; o primeiro acesso pode demorar até ~60 s devido ao cold start)

| Operação | Método / Rota | Payload / Headers | Observação |
|---|---|---|---|
| **Login** | `POST /api/auth/login?photoMode=metadata` | `{"email","password"}` | Retorna `access_token`, `refresh_token`, `token_type`, `user_id`, `aluno_id`, `matricula`, `full_name`, `cor_faixa`, `qntd_graus`, `photo` |
| **Refresh** | `POST /api/auth/refresh?photoMode=metadata` | `{"refresh_token"}` | Rota validada na API real. Retorna novo par de tokens. |
| **Check-in (Escrita)** | `POST /api/checkins-treino` | `{"schedule_id": 3, "data_treino": "2026-11-09"}` | Headers: `Authorization: Bearer <token>`, `Origin`, `Referer`. Retorna estado atualizado com o check-in no `upcoming` e no `history`. |
| **Cancelar Check-in** | `DELETE /api/checkins-treino/{id}` | Bearer | Cancela um check-in previamente confirmado. |
| **Painel de Treinos** | `GET /api/checkins-treino` | Bearer | Retorna `main.slot`, `upcoming`, `summary`, `history` e `now`. |
| **Perfil** | `GET /api/auth/me?photoMode=metadata` | Bearer | Dados cadastrais do aluno. |
| **Graduações** | `GET /api/graduacoes/me` | Bearer | Retorna cor da faixa, quantidade de graus e próxima faixa. |
| **Configurações** | `GET /api/settings/me?photoMode=metadata` | Bearer | Preferências do usuário. |
| **Comunicados** | `GET /api/academia/comunicados?page=1&pageSize=10` | Bearer | Avisos gerais da academia. |

Validade (campo `exp` do JWT): access ≈ 20 min; refresh ≈ 7 dias.
Nenhum segredo ou senha é registrado neste documento.
