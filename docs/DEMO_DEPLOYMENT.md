# Demo deployment handoff

## Current hosted gateway

Production gateway: `https://lookweave-api.vercel.app`

The deployed Vercel project is `lookweave-api`. Production environment variables are stored as sensitive Vercel variables. The APK contains only the HTTPS gateway URL; it does not contain Gemini, Groq, ElevenLabs, YouCam, or Tavily keys.

## Judge APK

Use the signed debug APK for direct installation:

`app/build/outputs/apk/debug/app-debug.apk`

The APK was rebuilt with `LOOKWEAVE_BACKEND_URL=https://lookweave-api.vercel.app`, installed to the connected device, and verified against the hosted gateway. It can be installed with:

```powershell
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

For a judge who does not have ADB, share the APK through the submission artifact or a private file link. Android may require allowing installation from that file source.

## Connection verification

The following production checks passed:

- `GET /health` returned 200 with Gemini, Groq, agent, ElevenLabs, YouCam, and Tavily configured.
- `GET /v1/rules` returned 200.
- `POST /v1/agent/respond` returned a clean natural-language response.
- APK build and Android unit tests passed.

## Important runtime note

Vercel Hobby is suitable for a judge demo and supports Node.js Functions, but its filesystem is ephemeral. The gateway therefore should be treated as a stateless demo API: the Android client carries the active profile context in requests, while server-side runtime files such as shared state, style jobs, and try-on cache can reset after a cold start or redeploy. The bundled Android cache keeps the main try-on demonstration instant regardless.

For a durable public launch, move `SYNC_STATE_PATH`, `STYLE_STATE_PATH`, and `TRY_ON_CACHE_PATH` to a managed database/object store (or deploy the persistent Node gateway on a VM/container). This is not required for judges to explore the current APK.

## YouCam protection

Use the bundled previews for the main walkthrough and reserve live YouCam calls for one or two deliberate interactions. The current gateway prevents concurrent try-on tasks but does not enforce a daily task limit or cooldown despite configuration fields that suggest them; watch provider credit usage directly.

## Recommended three-minute judging path

1. Open Today and show the editorial daily hub.
2. Open Plan, enter a custom occasion, and tap Weave.
3. Expand evidence and show kept/changed pieces and the decision trace.
4. Open Try-on and show an instant cached result.
5. Open Fit & Feel Lab and save a comfort observation.
6. Open Profile at a glance, tap Edit, and enter a natural-language preference.
7. Open Loomie and ask what is owned, then explicitly ask to browse.
8. If time permits, demonstrate Style Studio → Accessories → Accessory Detail Lab with a wrist/selfie and accessory image.
