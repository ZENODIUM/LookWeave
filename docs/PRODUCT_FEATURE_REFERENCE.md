# LookWeave / Loomie — Complete Product Feature Reference

## 1. Product in one sentence

LookWeave is an inclusive personal fashion agent that plans outfits from a real personal wardrobe, evaluates practical wearability constraints, explains every recommendation with evidence, and lets the user preview, shop, plan, and refine looks without reducing them to a diagnosis or a generic style label.

Its signature capability is **Adaptive Wearability Intelligence**: the system evaluates whether a complete look can work for the person and their day, not simply whether individual garments look fashionable.

## 2. The consumer problem it solves

Most fashion experiences optimize for a visual match, a trend, or a body category. They do not account for the details that can make an otherwise attractive item impossible or unpleasant to wear: closure effort, overhead reach, seated comfort, sensory load, medical-device clearance, pressure sensitivity, mobility-aid compatibility, temperature, UV, commute, or an already-owned wardrobe.

LookWeave separates four questions that are usually conflated:

1. What do I already own?
2. What can I comfortably and confidently wear today?
3. What is genuinely missing from my wardrobe?
4. If I do need something, which source-backed product is worth considering?

The result is a fashion assistant that preserves personal agency, avoids unnecessary shopping, and treats comfort and access as first-class styling inputs.

## 3. Core experience at a glance

| Area | What the user can do | What makes it different |
|---|---|---|
| Today | See a daily look, current conditions, upcoming plans, and practical adjustments | External context changes the recommendation instead of becoming a decorative weather widget |
| Wardrobe | Browse owned garments, evidence passports, categories, and availability | Each piece carries closure, fabric, wearability, and evidence provenance |
| Plan / Weave | Enter any occasion, including a custom occasion, then weave a complete look | A deterministic solver preserves the existing look wherever possible and changes only what fails confirmed needs |
| Fit & Feel Lab | Record real dressing experience, task timing, body-region friction, and comfort notes | The app learns from confirmed personal experience rather than pretending a virtual image proves comfort |
| Style Studio | Optional face proportions, colour story, eyewear and jewellery direction | Recommendations are advisory; user selections always override photo-derived suggestions |
| Try-on Studio | Generate and save apparel previews | Pre-generated results are bundled under the exact wardrobe-item keys for instant playback |
| Discovery | Swipe source-backed apparel and accessory finds, save preferences, and research only when requested | The agent distinguishes owned pieces, gaps, and external products |
| Nearby | Find adaptive retailers and nearby suggestions on a real interactive map | Retail accessibility context, distance, and saved places can inform the shopping plan |
| Day Signals | Use weather, UV, wind, air quality, pollen, and comfort context | Conditions are turned into concrete, explainable clothing adjustments |
| Loomie agent | Chat or speak to an agent that can plan, retrieve, research, draft rules, and update the profile | Agent reasoning proposes actions; confirmed constraints and the local solver remain authoritative |
| Profile | Maintain identity, preferences, wardrobe context, custom rules, evidence, budget, and plans | A free-form edit is organized into structured fields before it is saved |

## 4. Onboarding and identity-respecting profile setup

### Editorial welcome

The opening experience introduces Loomie as a wardrobe partner rather than a generic chatbot. It uses an editorial composition, garment sculpture, clear entry points, and a quiet visual system rather than noisy gradients, generic AI illustrations, or dashboard clutter.

### What a profile contains

Each profile can hold:

- Display name and portrait
- Optional gender/presentation label
- Private ZIP/postal code
- Preferred and avoided colours
- Preferred style directions
- Interests and daily-life context
- Monthly clothing budget and optional price limit
- Sizing and practical notes
- Wearability preferences and custom rules
- Saved wardrobe, dressing observations, applied looks, events, purchases, saved places, and try-on previews

The app includes multiple distinct personas to demonstrate that recommendations are profile-specific. Switching a profile switches the wardrobe, location, preferences, context, and cache namespace rather than merely changing a name on the screen.

### Editable “Profile at a glance”

The collapsible profile table presents Identity, Location, Style, Colours, Wearability, Interests, and Profile note. It is editable directly:

1. The user taps **Edit**.
2. They write naturally, for example: “I live in 60614, prefer sage and navy, like relaxed tailoring, and need easy-grip closures.”
3. Loomie extracts only explicit details into structured preference fields.
4. The existing confirmed profile-update path saves the structured changes.
5. The table refreshes to show the updated profile.

This makes profile creation approachable while retaining explicit, inspectable data rather than hiding decisions in an opaque paragraph.

## 5. Adaptive Wearability Intelligence

### Practical constraints supported

The profile and planning model supports conventional style preferences alongside practical needs, including:

- One-hand-friendly closures
- Seated comfort
- Low sensory load
- Medical-device or brace clearance
- Easy footwear
- Limited overhead reach
- Grip-friendly handling
- Seam or pressure sensitivity
- Easy removal
- Mobility-aid compatibility
- Wool avoidance and animal-free preferences
- Custom user-authored rules

The app deliberately models tasks and garment properties rather than asking the user to disclose a diagnosis. A person can say “avoid difficult back closures” or “I need room around a knee brace” without having to explain why.

### Evidence-aware garment passports

Every wardrobe item can expose:

- Category, colour, garment image, and availability
- Closure type and closure evidence
- Fabric and material evidence
- Wearability score
- One-hand, seated, sensory, device-clearance, reach, grip, pressure, removal, and mobility-aid fields where known
- A concise note explaining what is known versus unknown
- Evidence source and confidence

The interface never converts an unknown product field into an invented accessibility claim. Source information, user confirmation, and unresolved unknowns remain visibly distinct.

### Whole-look solver

The deterministic `ConstraintSolver` is the decision authority for hard constraints. It:

1. Starts with the selected/owned wardrobe context.
2. Filters garments against confirmed requirements.
3. Builds complete looks across top, bottom, outerwear, shoes, and optional accessories.
4. Preserves the greatest possible number of existing pieces.
5. Replaces only the garment(s) that break a hard constraint.
6. Returns kept pieces, changed pieces, unresolved needs, garment-level reasons, and a plan score.

This minimum-change approach is important: LookWeave does not discard someone’s identity or wardrobe because one detail needs adjustment.

### Weave result and decision trace

The Weave screen supports preset and custom occasions, saved occasions, and a clear result state. A completed plan shows:

- Overall score
- Kept versus changed piece count
- Resolved look
- The replacement, if one was necessary
- Garment-by-garment evidence
- Active rules used
- Remaining unknowns or unresolved needs
- A compact decision trace from context to check to minimal change to final look

An empty or infeasible state is explicit. The app does not imply success when no evidence-backed solution exists.

## 6. Today and Day Signals

### Today hub

Today is the operational home screen. It combines the next wearable look, active priorities, planned events, day-specific recommendations, wardrobe entry points, and the next useful action.

### External decision context

The Day Signals layer can use location-level conditions including:

- Temperature and apparent temperature
- Rain and precipitation probability
- Wind and gusts
- UV index
- Humidity
- Air quality
- Pollen

Signals produce understandable adjustments, not generic alerts. Examples:

- High UV can favor coverage, breathable layers, hats, or UV-aware accessories.
- Gusty weather can flag loose scarves or open fronts and recommend secure closures.
- Heat and humidity can prioritize breathable, low-friction fabrics.
- Cold conditions can favour removable warmth with manageable closures.
- Air quality and pollen can be surfaced when relevant to an outing decision.

The user’s exact address is not required; the private ZIP is sufficient for location-level context.

### Day Hub planning

The user can create, update, and link calendar events to a planned outfit. The agent can draft calendar events, suggest an outfit swap, attach an owned look, and account for purchases, price limits, and remaining budget. Changes are presented as confirmable actions.

## 7. Wardrobe and Fit & Feel Lab

### Wardrobe

The wardrobe is filterable by category and supports quick garment inspection. It is designed to answer “what do I own?” before the experience recommends shopping. Garment cards use the best available product/wardrobe image and avoid a generic hanger-placeholder experience whenever a local or source image exists.

### Fit & Feel Lab

The Fit & Feel Lab captures the personal evidence that retail product pages cannot provide. It includes:

- Profile-specific candidate garments, with the active capsule shown first
- Task selection such as putting on, fastening, reaching, sitting, or removal
- Live task timer
- Difficulty and comfort reporting
- Interactive body-region/focal-area capture with a fallback selector
- Custom comfort note entry
- Suggested fix or alternative item
- An analysis panel and personal insight history

Observations are saved as personal evidence. They can influence future suggestions while remaining distinct from retailer claims and virtual try-on output.

## 8. Style Studio: face, colour, and accessories

### Optional analysis

With consent and a clear portrait, Style Studio can request YouCam face attributes and colour analysis. Supported outputs include face shape, face ratios/proportions, visible colour information, and related style direction.

The user may override any result manually. The app does not infer ethnicity, diagnosis, or personal identity from a portrait.

### Glasses and accessory recommendations

Style Studio uses face direction, chosen metal preference, colour direction, low-bridge/hearing-device/easy-clasp preferences, and wearability rules to offer:

- Eyewear silhouette guidance
- Jewellery finish and closure guidance
- Hat and scarf styling directions
- Colour pairings for apparel and accessories

Advice is phrased as an exploration direction, not as a claim that a single face scan can determine what a person “should” wear.

### Accessory discovery

Accessory discovery is a dedicated source-backed capability for:

- Glasses
- Watches
- Jewellery
- Scarves
- Hats
- Shoes

Each search adapts the query and rejection criteria to avoid obvious irrelevant results such as watch straps, repair kits, jewellery findings, or glasses cases. Cards identify source-backed facts and retain unknowns such as closure effort, local stock, and physical comfort.

### Accessory Detail Lab

The Accessories tab also contains a guided close-up preview lab for earrings, necklaces, watches, rings, and bracelets. It uses the dedicated YouCam 2D VTO family, rather than pretending a full-body apparel image can accurately represent small accessories. The user selects a clear selfie for earrings/necklaces or a wrist/hand image for watches/rings/bracelets, selects the accessory product image, and receives a cached visual styling preview. The UI explains the capture requirement and labels the result as a visual preview rather than a fit or measurement claim.

## 9. Virtual Try-on and pre-generated preview cache

### YouCam apparel VTO

The Android client sends images only through the local/hosted gateway. The gateway:

1. Registers and uploads the person and garment images to YouCam.
2. Creates a YouCam Cloth V4 task.
3. Polls to completion.
4. Returns the completed image URL.
5. Stores a fingerprinted result cache to avoid duplicate provider calls.

The user can save a completed preview. Try-on is useful for visual styling exploration, but the product explicitly does **not** claim that it proves physical fit, closure effort, safety, comfort, or real-world measurements.

### Instant demo cache

The cache has memory, disk, bundled asset, stable persona-alias, and body-hash layers. The lookup order is memory → local disk → app asset → live generation.

The current build bundles 66 persona-cache images, including 30 additional real capsule-item previews across all six profiles. The cache seeder now understands the actual app garment IDs (`p1_01` through `p6_10`), so a Fit/Try-on card requests the exact pre-generated key instead of a generic stand-in. This makes selected cached previews open instantly and prevents accidental credit use during a demo.

## 10. Discovery, shopping, and nearby access

### Curated fashion deck

Discovery offers a swipe-oriented deck with product imagery, concise match reasons, save/pass feedback, and category filters. The deck is responsive because local persona-relevant candidates are retained in memory; network research is deliberately performed only when a user asks to search online or refresh.

### Live research policy

The app distinguishes:

- **Owned**: present in the user wardrobe context.
- **Need/gap**: a missing category or unverified requirement.
- **External product**: a researched item returned only after an explicit browse/search/shop/buy request.

This policy prevents an external suggestion from being represented as something the user already owns. Research products use source URLs, extracted source text, and available product images; prices, stock, and physical accessibility stay unknown unless a source explicitly provides them.

### Nearby retailers and interactive map

The Nearby experience combines a searchable retailer directory, contextual suggestions, saved likes, distance-aware results, and an interactive MapLibre map. It supports adaptive and inclusive retailer discovery, recommended items, and direct source actions. It is a real map overlay, not a static decorative map.

## 11. Loomie: agentic chat and voice

### Text agent

Loomie is a conversational layer over a structured action system. It can interpret natural-language requests and propose tools such as:

- Retrieve, draft, edit, or delete wearability rules
- Search owned wardrobe
- Solve an outfit or suggest an outfit swap
- Research products when explicitly requested
- Add or update calendar events
- Link a planned look to a day
- Record a purchase or price limit
- Update profile preferences
- Record a Fit & Feel observation

The response is intentionally short. Instead of raw tool JSON or long generic paragraphs, the UI renders action cards linking to relevant destinations such as wardrobe, discovery, Day Signals, Profile, or plan evidence.

### Agent safety model

- The LLM interprets language and proposes tool calls; it is not the authority for hard wearability constraints.
- The deterministic local solver checks confirmed constraints.
- Profile and Day Hub changes use confirmable drafts.
- The agent is instructed not to infer diagnoses.
- Raw JSON, markdown fences, provider scaffolding, and internal reasoning are sanitized before display.
- Explicit “do not browse” instructions prevent external research.

### Voice agent

The voice experience uses ElevenLabs conversation sessions and microphone permission. It includes a tactile animated agent/orb interface with live audio-reactive behavior, voice state feedback, transcript handling, error recovery, and a text fallback. Voice conversation is optional; all major workflows remain available through text and touch.

### Provider resilience

Gemini is the primary language-model route with Groq fallback. The gateway performs model discovery and chooses configured providers without exposing keys to the Android package.

## 12. Privacy and trust boundaries

- API keys remain in gateway environment configuration, never in the Android APK.
- Portrait/body images are used for the selected analysis or try-on transaction; user-facing messaging makes the purpose clear.
- Stored profile and evidence data are local/shared-state scoped and can be cleared from the Profile screen.
- The app uses a private ZIP rather than requiring a street address for location-aware decisions.
- Product-source facts, personal fit observations, virtual previews, and unknown fields are kept separate.
- Visual preview is never presented as medical, functional, or sizing proof.

## 13. Technical architecture

### Android

- Kotlin + Jetpack Compose
- Local profile and cache stores
- Deterministic constraint solver
- MapLibre interactive map
- Camera/photo pickers and local image handling
- ElevenLabs Android conversation client

### Gateway

- Node.js service at `backend/server.mjs`
- Provider boundary for YouCam, Gemini, Groq, ElevenLabs, Tavily, day signals, and shared state
- Rate/cooldown and one-in-flight protection for try-on
- Provider task polling and response sanitation
- Local development support through ADB reverse port forwarding

### Key endpoints

| Endpoint | Purpose |
|---|---|
| `/health` | Provider and gateway availability |
| `/v1/try-on` | YouCam apparel VTO task orchestration |
| `/v1/style/*` | Face/colour style analysis and profile preferences |
| `/v1/agent/respond` | Structured Loomie responses and tool proposals |
| `/v1/voice/session` | Short-lived ElevenLabs conversation credentials |
| `/v1/products/discover` | Apparel discovery |
| `/v1/style/accessories` | Accessory discovery |
| `/v1/products/research` | Explicit external product research |
| `/v1/context` and `/v1/nearby` | Day signals and nearby-context decisions |
| `/v1/sync/state` | Shared state synchronization |

## 14. End-to-end flows worth demonstrating

### Flow A — “What can I wear to dinner?”

1. Open Plan.
2. Enter a preset or custom occasion, such as “Outdoor dinner after work.”
3. Select active priorities such as seated comfort, easy closure, or sensory ease.
4. Tap **Weave**.
5. Show the minimum-change result, evidence, kept pieces, and replacement.
6. Open Try-on for an instant cached visual preview.

### Flow B — “My clothes need to work around my day”

1. Open Today / Day Signals.
2. Show weather, UV, wind, and other signals.
3. Show a condition-based adjustment, such as secure closures on a windy day.
4. Link the final outfit to a calendar event.

### Flow C — “I have a real comfort problem”

1. Open Fit & Feel Lab.
2. Choose a garment and dressing task.
3. Start/stop the task timer, note friction or body region, and add a custom comfort note.
4. Save the observation.
5. Show that the evidence appears in the Profile history and informs later planning.

### Flow D — “Help me find only what I need”

1. Ask Loomie what is already owned; show the owned-wardrobe result card.
2. Ask whether there is a gap; show a solver-backed gap assessment.
3. Explicitly ask to browse; show live/source-backed external products.
4. Save/pass items or find nearby retailers on the map.

### Flow E — “Personalize without a complicated form”

1. Open Profile at a glance.
2. Tap Edit.
3. Write a natural-language preference update.
4. Show the organized table refresh.
5. Optionally open Style Studio for colour, face-proportion, and accessory direction.

## 15. What makes the project unusually strong for the YouCam hackathon

1. **YouCam has a clear job, not a novelty cameo.** Apparel VTO is embedded in a full decision journey, with instant cached preview states, style analysis, and a direct path from wardrobe planning to visual exploration.
2. **The agent is genuinely agentic.** It has a structured tool layer, decision context, draft/confirmation behavior, and a deterministic safety boundary instead of being a chat-only wrapper.
3. **Inclusion changes the product logic.** Wearability is applied to garment selection, evidence, day conditions, discovery, retailer access, and learning from experience.
4. **The product preserves agency.** The user can override face/colour analysis, write their own rules, edit their profile in natural language, and inspect why a suggestion was made.
5. **It has a credible consumer story.** The product helps users buy less blindly, use what they own, plan for real environments, and find accessible alternatives only when needed.
6. **The design supports a clean demo.** Cached previews, pre-baked capsules, instant results, visible evidence, a polished voice state, and a focused 1–3 minute path reduce demo risk.

## 16. Current verification status

- Android debug unit tests pass.
- Android debug APK builds successfully.
- Backend contract and agent tests pass.
- Gateway health has been verified with configured provider availability.
- APK installation has been verified on the connected wireless ADB device.
- Profile-at-a-glance edit has been visually and interactively tested on-device.
- Weave result, evidence display, Fit candidate content, text-agent owned-wardrobe result card, and cached VTO assets have been physically checked in the product flow.
- Thirty additional YouCam persona-garment previews completed successfully and are bundled into the latest device build.

For build, API configuration, data-source, competitive, and design details, see the complementary documents in `docs/`.
