# Hosted Integration Architecture

## Principle

Hosted intelligence interprets and renders. The deterministic local/domain solver remains the authority on feasibility.

```text
Android app
  ├─ local wardrobe + evidence + consent
  ├─ deterministic constraint solver
  └─ authenticated backend
       ├─ Gemini: intent, explanation, bounded tool proposals
       ├─ YouCam: file upload, task orchestration, result retrieval
       └─ ElevenLabs: private conversation token minting
```

No vendor secret belongs in the APK.

## Gemini recommendation

For a new project, Google’s current model catalog recommends newer Flash families rather than starting on the restricted 2.5 line. Use a stable Flash-Lite model for high-frequency intent classification and structured tool selection, and a stronger Flash model only for complex multimodal garment interpretation. Confirm the exact models and quotas visible to the project in AI Studio because limits are project- and tier-specific.

Official sources: [models](https://ai.google.dev/gemini-api/docs/models), [rate limits](https://ai.google.dev/gemini-api/docs/rate-limits), [pricing](https://ai.google.dev/gemini-api/docs/pricing).

The model receives a narrow evidence payload and may call tools such as:

- `search_wardrobe`
- `solve_outfit`
- `explain_plan`
- `start_try_on`
- `record_user_confirmation`

It cannot write hard constraints directly or approve an infeasible plan. Use structured outputs, schema validation, idempotency keys, timeouts, and retry only safe operations.

## ElevenLabs recommendation

ElevenLabs provides an Android Kotlin SDK (`io.elevenlabs:elevenlabs-android`) for conversational agents. For private agents, the backend creates a conversation token; the API key is never embedded in the client. The SDK supports connection/listening/speaking callbacks, feedback, mute/unmute, contextual updates, and client tools: [Kotlin SDK](https://elevenlabs.io/docs/eleven-agents/libraries/kotlin).

The current free Agents tier includes a limited monthly call allowance, so keep text as a full first-class path and use voice for the high-value “hands occupied while dressing” moment: [Agents pricing](https://elevenlabs.io/pricing/agents).

Recommended client tools:

- `describe_active_look`
- `replace_garment` → invokes the local solver, not arbitrary mutation
- `read_evidence`
- `confirm_observation`
- `open_virtual_studio`

Voice must never perform purchase, sharing, deletion, or preference changes without explicit confirmation.

## YouCam

Use the backend sequence documented in [YOUCAM_API_REFERENCE.md](YOUCAM_API_REFERENCE.md). Cache a person `file_id` only within the permitted lifetime/consent scope, fan out category tasks only when needed, and display actual task failures with recovery choices.

## Observability

- Correlation ID across Android → backend → vendor task
- Redacted structured events, never raw images or bearer tokens
- Latency by phase: auth, upload, queue, generation, download
- Constraint-solver decision trace stored separately from LLM prose
- User-visible deletion of images and observation history
- Metrics: preserved-piece ratio, unsupported-attribute rate, confirmed usefulness, and task completion—not invented accuracy percentages
