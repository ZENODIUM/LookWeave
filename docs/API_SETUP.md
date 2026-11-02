# API setup

LookWeave keeps provider keys on the gateway. No provider secret is compiled into the Android APK.

## One-command configuration

From the project root:

```powershell
.\scripts\configure-integrations.ps1 `
  -GeminiApiKey "your-key" `
  -GroqApiKey "your-key" `
  -ElevenLabsApiKey "your-key" `
  -YouCamApiKey "your-key" `
  -Target Emulator
```

Use `-Target Phone` for a wirelessly debugged Android phone. Both targets configure `adb reverse tcp:8787 tcp:8787`, which is more reliable than relying on host-network aliases; reconnect wireless debugging and rerun the command if the pairing changes.

Then rebuild the app and start the gateway:

```powershell
# Set JAVA_HOME to your local Android Studio JBR or another compatible JDK.
.\gradlew.bat assembleDebug
.\scripts\start-gateway.ps1
```

Only the four core provider keys are required. Tavily is optional and is used only for source-backed product research when `TAVILY_API_KEY` is configured. The gateway automatically:

- discovers a compatible Gemini Flash model available to the supplied key;
- falls back to a compatible Groq structured-output model when Gemini is rate-limited or unavailable, with a local request guard and cooldown;
- creates and remembers a private LookWeave ElevenLabs agent when no agent ID is supplied;
- mints short-lived ElevenLabs conversation tokens for Android;
- registers and uploads YouCam images, creates the V4 clothes task, polls it, and returns the result.

Optional overrides include `GEMINI_MODEL`, `GROQ_MODEL`, `GROQ_LOCAL_RPM_LIMIT`, `ELEVENLABS_AGENT_ID`, and `ELEVENLABS_VOICE_ID`. The gateway prevents concurrent try-on tasks and returns a cached result for identical inputs. Do not rely on `YOUCAM_DAILY_TASK_LIMIT` or `YOUCAM_TASK_COOLDOWN_SECONDS` as enforced spend controls in this revision; the current `/v1/try-on` implementation does not check those values before a new provider task.

For the hosted demo, `ELEVENLABS_API_KEY_2` through `_4` are ordered voice backups, each with a corresponding `ELEVENLABS_AGENT_ID_2` through `_4`. The gateway checks account quota before creating a session and advances to the next account if the current one is unavailable. `GROQ_API_KEY` and `GROQ_API_KEY_2` alternate on successful fallback requests. Groq rate limits apply to the organization, so both keys share the local request cap and a provider `Retry-After` cooldown; rotation does not increase the organization's quota.

The implementation follows the providers’ current primary documentation: Gemini [API keys](https://ai.google.dev/gemini-api/docs/api-key) and [structured outputs](https://ai.google.dev/gemini-api/docs/structured-output); Groq [API reference](https://console.groq.com/docs/api-reference), [structured outputs](https://console.groq.com/docs/structured-outputs), and [rate limits](https://console.groq.com/docs/rate-limits); ElevenLabs [Kotlin SDK](https://elevenlabs.io/docs/eleven-agents/libraries/kotlin), [agent creation](https://elevenlabs.io/docs/api-reference/agents/create), and [conversation tokens](https://elevenlabs.io/docs/eleven-agents/api-reference/conversations/get-webrtc-token); and YouCam [AI Clothes integration](https://docs.perfectcorp.com/reference/ai_clothes/section/overview/integration-guide) plus the [V4 task schema](https://docs.perfectcorp.com/reference/ai_clothes/v4.0/paths/~1s2s~1v2.0~1task~1cloth-v4/post).

Tavily product research uses the official [Search](https://docs.tavily.com/api-reference/endpoint/search) endpoint with basic depth, a small result count and a six-request-per-minute local guard, then the official [Extract](https://docs.tavily.com/api-reference/endpoint/extract) endpoint for up to three selected URLs. Results are cached for six hours and returned with source URLs. The service never turns missing product facts into claims about price, stock, fit or accessibility.

## Security and deployment

`.env`, `backend/.env`, and generated runtime state are ignored by source control. The Android app receives only the gateway URL and short-lived conversation credentials. The local development build allows HTTP so it can reach the workstation. Deploy the gateway behind HTTPS and disable cleartext traffic before production distribution.

## Verification

The gateway includes an offline provider-contract suite that exercises Gemini, Gemini-to-Groq fallback, ElevenLabs, and YouCam orchestration without using provider credits:

```powershell
cd backend
npm test
```

With real keys configured, `http://127.0.0.1:8787/health` reports whether all four providers are present. A successful health response verifies configuration, while the in-app agent, voice session, and Virtual Studio validate provider entitlements and live traffic.

To pre-seed offline YouCam try-on JPEGs for Person 1 (after the gateway is up): `node tools/seed-youcam-cache.mjs --garments g12` — see [YouCam API reference](YOUCAM_API_REFERENCE.md#offline-try-on-cache-lookweave).
