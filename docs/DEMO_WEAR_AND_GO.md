# Wear & go: judging walkthrough

1. Open Profile. Select step-free shopping, conserve my energy and the material preferences relevant to the scenario. These are voluntary requirements, not inferred diagnoses.
2. Open Lab. Choose a garment, start and finish a real observation, select difficulty and save. Profile should show the persisted observation.
3. Plan a look. Explain the smallest necessary replacements and preserved items. If a requirement cannot be met, show the missing alternative rather than claiming success.
4. Open Agent → Wear & go. Enter a US ZIP. Show place search, weather, access review and explanation in the execution trace. Explain why unknown venue access needs confirmation and why an explicitly inaccessible venue is excluded.
5. Open Wardrobe → Adaptive store directory. Swipe to shortlist, then open the real interactive map. Distances originate at the ZIP area, not the user's home.
6. Open the outfit preview. Use a consented person photo and selected garment. One successful cloth-v4 result costs 2 units at the account's checked rate. Avoid repeating paid generation during rehearsal; the recorded output is in artifacts/live-validation/youcam-outer-result.jpg.
7. Briefly demonstrate voice connection and end the session. Voice conversation and the Wear & go orchestrator are separate capabilities today.

## Evidence available

- Five emulator journeys passed on API 37 in 41.103 seconds.
- Android build, unit tests and lint passed before the final two-label copy correction; final APK rebuilt afterwards.
- Backend contract suite and two outing-decision tests passed.
- Live YouCam generation succeeded; balance changed 972 → 970 and stayed 970 on recheck.
- Live ElevenLabs Android session reached Speaking; full spoken exchange is not yet validated.

## Claims to avoid

Do not present catalogue illustrations as verified retail inventory, OSM access tags as certification, straight-line distance as an accessible route, virtual try-on as physical fit testing, or this combination as proven world-first. The current gateway is single-user and local. New Android functionality still needs desktop parity and broader visual review.
