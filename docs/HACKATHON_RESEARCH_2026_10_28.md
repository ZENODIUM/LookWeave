# Research and product decisions — 28 October 2026

## Comparison with submitted projects

Input: a supplied hackathon projects summary, 286 summaries across two galleries. These are submitters' descriptions, not independent validation. Prize labels in the supplied report are used as reported.

| Project | Relevant lesson | Implication for LookWeave |
| --- | --- | --- |
| Aloud, reported first place | Voice, barcode and ingredient tools address a concrete independent-shopping barrier | Inclusion needs operational tools and accessible output, not demographic badges |
| CASTING, reported prize winner | Cohort coverage, measured attributes, partial results | Show provenance and incomplete evidence; do not make a universal representation claim |
| ConfidFit, reported prize winner | Real commercial VTO lifecycle and metering | Test actual upload/task/result, not only mocked contracts |
| KeepMe | User control over generative identity drift | Distinguish appearance preview from evidence of physical fit or identity preservation |
| AccessWear | Constraint matching, measurements, YouCam | Adaptive matching already exists; it is not a novelty claim |
| Aequidrape | Functional profile, garment changes, voice and seller contact | A broad adaptive profile alone is insufficient differentiation |
| ClimaSkin VTO | Weather/context plus preview | Weather alone is already represented in the supplied field |

Relevant public project pages: [Aloud](https://devpost.com/software/aloud-lxqr70), [CASTING](https://devpost.com/software/casting), [AccessWear](https://devpost.com/software/accesswear), [Aequidrape](https://devpost.com/software/aequidrape). Comparison above is based on the supplied report; those pages are reference links, not a claim that each submission has been independently tested.

## Chosen direction: decide whether the outing is worth it

Wear & go connects confirmed preferences and recorded garment effort to destination evidence and local conditions. It checks places, evaluates access uncertainty, considers an energy-conserving one-stop plan, and explains why previewing at home may be the useful next step. Unknown venue access remains a confirmation question. Distances are from the ZIP centroid, not a route or an estimate of physical effort.

This is a differentiated combination relative to the inspected examples. We cannot substantiate a claim that no other project or app has implemented it. A defensible demo shows actual decisions and their sources, not a world-first slogan.

## External sources and technical choices

- [OpenStreetMap wheelchair tagging](https://wiki.openstreetmap.org/wiki/Key:wheelchair): `yes`, `limited`, `no`, and absence are distinct. A missing tag is not accessible. The planner excludes explicit `no` when step-free shopping is selected, prioritizes `yes`, and asks for confirmation for unknown/limited records. Tags are community evidence, not current certification.
- [Nominatim search API](https://nominatim.org/release-docs/latest/api/Search/): request extra tags for venue evidence. Preserve attribution, cache results, and pace requests. Existing brand-only scores are not measurements of accessibility.
- [Open-Meteo air quality](https://open-meteo.com/en/docs/air-quality-api): forecast pollutants and pollen have geographic/model limitations. Missing values must stay missing; the audit fixed `Number(null)` incorrectly becoming zero.
- [Open-Meteo weather](https://open-meteo.com/en/docs): feels-like temperature supports the user's selected heat preference. It does not diagnose a condition or establish garment protection.
- [SiriWave](https://github.com/kopiro/siriwave): MIT-licensed, existing waveform implementation, version 2.4.0 pinned and bundled. Uses real ElevenLabs SDK audio level; stops when inactive. Local asset renderer makes no network requests.
- [YouCam V4](https://docs.perfectcorp.com/reference/ai_clothes/v4.0/paths/~1s2s~1v2.0~1task~1cloth-v4/post): registered files, exact signed PUT headers, task creation and bounded polling. Signed header names are case insensitive; duplicate Content-Type fields corrupt the upload signature.
- [YouCam units](https://docs.perfectcorp.com/reference/unit_system): account credit and feature-cost endpoints checked live. Cloth-v4 returned 2 units per result image; pagination uses `starting_token` with the previous `next_token`.

## Profile design

Existing one-hand closure, seated comfort, sensory load, device clearance and easy footwear remain. Added wool avoidance and animal-free material preferences operate on recorded fabric descriptions. Added step-free shopping, energy conservation and heat comfort influence external-data planning. All are voluntary functional choices. No diagnosis, religion, gender or disability is inferred.

Fabric substring checks only exclude listed materials; they do not certify an undocumented blend. Garment photos created earlier are generated catalogue illustrations in photographic style, not evidence that a named retailer sells those exact items. Real retail provenance remains a separate product-data requirement.

## UI direction

Use a coherent task sequence: Profile → Lab → Agent / Wear & go → outfit decision → YouCam preview. Visible tool evidence is short, factual and expandable in future, not an animation pretending that reasoning occurred. The voice waveform communicates session/audio state. A mascot is deferred because a low-quality character would add visual noise and obscure the useful voice controls.

## Validation priorities

1. Multi-conflict solver and explicit missing alternatives.
2. Observation persistence and its effect on outing decisions.
3. Unknown venue access cannot be upgraded by the language model.
4. External failure yields partial evidence, not fabricated results.
5. One real YouCam output plus measured account debit.
6. Emulator interaction and screenshots of Profile, Agent, Lab and preview.

## Remaining product risks

## Competitive baseline checked against official product pages

- [Acloset](https://www.acloset.app/): clothing organization and context-aware outfit suggestions already exist. Weather alone is not a defensible differentiator.
- [Whering Dress Me](https://whering.co.uk/faq/outfit-suggestions-on-Whering): wardrobe-based outfit discovery is established. A swipe interface is presentation, not the innovation.
- [Indyx](https://www.myindyx.com/home/): digital wardrobe and human styling combine organization with personal advice.

LookWeave's strongest demonstration is the feedback loop: a user records a difficult closure, the next plan excludes that garment when energy conservation is selected, venue access is checked separately, unknown access remains a confirmation question, and a minimal outfit change can be visualized. This is a proposed differentiation, not proof of universal novelty.

Voice implementation uses the [official ElevenLabs Android SDK](https://github.com/elevenlabs/elevenlabs-android). Voice and structured text now have separate prompts. The voice session currently offers conversation, not the Wear & go tool chain; it must never claim otherwise.

The current backend is a local single-user prototype, not an authenticated multi-tenant service. Four prior UI tests did not cover a full spoken conversation, generated output, every preference combination, or all catalogue claims. These gaps must be reported explicitly and not replaced by a blanket 'production-ready' claim.
