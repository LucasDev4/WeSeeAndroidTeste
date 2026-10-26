# WeSee (Android / Kotlin / Jetpack Compose)

```
com.wesee.app
├── MainActivity.kt
├── navigation/WeSeeNavigation.kt      # rotas + botão voltar
└── ui
    ├── theme/        Color.kt, Type.kt (Host Grotesk)
    ├── components/   WeSeeText, WeSeeHeader (logo/progresso/olhos), WeSeeButtons, WeSeeTextField, BottomPanel
    └── screens/
        ├── welcome/  WelcomeScreen        (Login / Cadastro)
        ├── profile/  WhoAreYouScreen      (Quem é você?)
        ├── guardian/ GuardianLoginScreen, GuardianRegisterScreen
        ├── pcd/      home/ (PcdHomeScreen, CameraArea, HomeCards, NoteEditor), chat/ (PcdChatScreen)
        └── disabled/ DisabledLoginScreen, DisabledRegisterScreen, VoiceAuthScreen (microfone/biometria)

com.wesee.app.voice  VoiceAssistant (TTS + reconhecimento + bipes, VoiceConfig = voz), VoiceSession (conversa guiada)
com.wesee.app.auth   BiometricAuth
```
Fluxo: Welcome → (Cadastro) WhoAreYou → (Responsável do PCD) GuardianLogin ⇄ GuardianRegister;
(PCD visual) DisabledRegister → DisabledLogin.
Textos das telas do responsável ficam em `res/values/strings.xml`. Imagens em `res/drawable-nodpi`.

com.wesee.app.data    UserStore, Notes (NotesStore)
com.wesee.app.device  Battery (bateria real + estimativa)

## Mapa (Google Maps Platform)
Ative no Google Cloud: **Maps SDK for Android**, **Places API (New)** e **Routes API**. Restrinja a chave ao pacote `com.wesee.app`.
A chave NÃO fica no código: use a variável de ambiente `MAPS_API_KEY` (no GitHub: Settings → Secrets and variables → Actions → New repository secret)
ou `MAPS_API_KEY=...` em `local.properties` (já no .gitignore).

## Conexão com o backend (Backend-atlas)
O app fala com o Spring Boot via `network/AtlasApi.kt` (`/auth/register`, `/auth/login`, `/user/me`). URL em `API_BASE_URL`:
- Emulador: padrão `http://10.0.2.2:8080/` (nada a configurar).
- Celular real: coloque `API_BASE_URL=http://IP-DO-SEU-PC:8080/` em `local.properties` (celular e PC na mesma rede Wi-Fi; libere a porta 8080 no firewall).
- GitHub Actions: crie o secret `API_BASE_URL` (e `MAPS_API_KEY`).
HTTP puro só é permitido no build **debug** (`src/debug`). Em produção use HTTPS.
Regras do backend refletidas no app: senha com **exatamente 6 números** (na voz: "um dois três quatro cinco seis"), CPF 11 dígitos, telefone 10–11 dígitos.
Sessão: o token JWT fica salvo no aparelho; ao abrir o app ele é validado em `/user/me` (401/403 → volta ao login). Logout apaga tudo.

## Área do Responsável (dados demonstrativos)
Telas: Home, painel de Notificações (sino) e Localização — `ui/screens/guardian/home` + `ui/screens/guardian/components`.
Dados fictícios em `data/GuardianDemoData.kt` (trocar por API depois). O mapa (`GuardianMap.kt`) usa o Google Maps SDK quando
`MAPS_API_KEY` está configurada (variável de ambiente / GitHub Secret / `local.properties`) e, sem chave, mostra um mapa **ilustrativo**.
Fluxo: Login do responsável → GuardianHome ⇄ GuardianMaps. A aba Account ainda não tem tela.

## Perfil do Responsável e nomes dinâmicos
`ui/screens/profile/ProfileContent.kt` é o Perfil compartilhado (PCD e Responsável). O nome do responsável vem do login (`UserStore.name`);
`data/PersonNames.kt` deriva o nome da pessoa acompanhada ("Gabriel Rodrigues" → "Gabriel Rodrigues Jr.", sem duplicar "Jr.").
## Ícones
Ícones convertidos para VectorDrawable a partir dos SVGs em `design/icons/` (lock, mail, call_end, nomeIcon, Exterior → casa, OlhosIconAzul → olhos).

## Vínculos PCD ⇄ Responsável (modais no Perfil)
Opção **"Meus responsáveis"** (PCD) / **"Pessoas vinculadas"** (Responsável) no Perfil → modal (`ui/screens/links/`):
`LinksController` (estado + chamadas) e `LinksModal` (UI). Endpoints usados (token do login): `GET /api/vinculos`,
`POST /api/vinculos/codigos` (PCD) e `POST /api/vinculos/solicitar` (Responsável). O tamanho do código é `VINCULO_CODE_LENGTH` (6)
e deve ser igual a `vinculo.codigo-tamanho` do backend. Ícone da opção: `design/icons/family_restroom.svg`.
