# Personal colour and accessory integration

Scope: clothing, jewellery, glasses, hats, scarves and bags. No skincare recommendations, face reshaping, gender/age detection or attractiveness scores.

## Provider contracts checked October 28, 2026

Official OpenAPI snapshots are saved in `docs/api/ai_skin_tone_analysis.json` and `docs/api/ai_face_analyzer.json`.

- [Facial colours](https://docs.perfectcorp.com/reference/ai_skin_tone_analysis): POST `/s2s/v2.0/task/skin-tone-analysis`, then GET the same path plus task ID. Results use `results.color.skin_color`, `eye_color`, `hair_color`, `eyebrow_color`, `lip_color`. Hex samples are not an undertone classification or ethnicity inference. Cost checked against the account feature-cost endpoint: 20 units.
- [Face attributes](https://docs.perfectcorp.com/reference/ai_face_analyzer/section/overview): POST `/s2s/v2.0/task/face-attr-analysis`; request exactly `faceShape`, `faceAspectRatio`, `horizontalThird`, `verticalFifth`, `eyeDistance`. Results use `faceshape`, `facialratio` and `eyelid.setting`. Five features cost 10 units. No age or gender requested.
- Both use the file-registration and signed-upload workflow, high face-angle strictness, bounded polling every 10 seconds, JPEG/PNG under 10 MB, minimum 320 pixels and maximum side 4096. Colour analysis requires the face to occupy most of the picture.

## Product behaviour

The studio is available from Android welcome and Profile, and `/desktop/style.html`. User explicitly chooses analysis and consents before upload. Manual choices work without a photo. Results are stored separately from wearability preferences in `.runtime/style-state.json`; image bytes are not persisted by LookWeave. Provider retention is governed by the provider, not our delete action.

Photo samples, shape and ratios are displayed as estimates. Users can correct the skin swatch and shape, choose warm/cool/mixed/exploratory colour direction, and override jewellery finish. A contrasting neutral uses the selected colour's brightness; palette and frame-shape rules are styling heuristics, not scientific undertone detection or guaranteed fit. No lightening/darkening preference or beauty ranking is applied.

Glasses checks include bridge fit, hearing-device clearance, frame weight and confirmed material composition. A portrait does not provide millimetre sizes, prescription PD or verified optical fit. Nickel avoidance requires manufacturer evidence; metal colour alone cannot establish composition. Easy-clasp suggestions also respect existing one-hand preferences.

The text agent receives the current recommendations and evidence context. This does not create product inventory or verify a retailer's stock. Shape suggestions remain categories rather than fabricated exact-product fits.

## Spending and recovery

Analysis returns a local job ID immediately, so closing/reopening a page can resume polling without uploading again. Duplicate image/mode fingerprints return the existing job, including after restart. A durable conservative reservation budget defaults to 120 units for this feature. Failed/interrupted jobs are not automatically resubmitted. Reservations are intentionally not refunded automatically after ambiguous errors. The ledger never stores portrait bytes. Deletion clears profile, task IDs and hashes while retaining aggregate spending.

## Validation

- Unit/service tests cover documented field parsing, missing results, forbidden unrequested attributes, preference override, conditional fit checks, deduplication, restart persistence and delete-with-budget-retention.
- Android test covers no-photo disabled action, saved colour/metal/shape choices and corresponding glasses/jewellery recommendations.
- Browser checks cover consent gating, narrow-screen overflow and JavaScript errors.
- Live provider results and measured debit will be recorded in the enhancement tracker after completion. A synthetic test portrait is not evidence of accuracy across real skin tones or populations.
