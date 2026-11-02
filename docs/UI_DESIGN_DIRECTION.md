# UI Design Direction: Editorial, Human, Product-Specific

> Historical direction. The implemented 2026 redesign and its research are documented in [NON_AI_UI_RESEARCH.md](NON_AI_UI_RESEARCH.md). Where the two differ, the newer Product Workroom system is authoritative.

## Goal

The interface should feel like a thoughtful independent fashion publication crossed with a dependable utility—not a generic AI dashboard and not a clinical accessibility tool.

## Research signals

Google’s design review guidance asks whether information hierarchy is clear, whether primary flows are uncluttered, whether platform patterns remain predictable, and whether cards are being overused merely because other apps use them. It also asks whether a new user can understand the product’s unique value immediately: [Reviewing Graphic Design](https://design.google/library/reviewing-the-design-review).

Google’s Lookout team argues that assistive products should be delightful and empowering, with sound, haptics, and visual craft considered together rather than accessibility being reduced to bare functionality: [Prioritizing Beauty and Accessibility](https://design.google/library/google-lookout-design-accessibility).

Common machine-generated visual defaults include purple-blue gradients, decorative glow, excessive glass effects, identical feature cards, universal pill shapes, and hierarchy-free softness. The important critique is repetition without product meaning, not any single color or radius: [Anti-slop principles](https://design.yaps.ai/docs/principles), [InterfaceKit analysis](https://blog.interfacekit.io/why-ai-generated-websites-all-look-the-same).

## LookWeave system

### Visual language

- Warm parchment canvas, paper surfaces, ink text, deep olive action color, restrained brass metadata, and clay warnings
- Serif display typography for editorial identity; compact sans serif for actions and evidence
- Square-to-subtle 2–4 dp corner language rather than pills everywhere
- Hairline dividers and flat surfaces; elevation only when it communicates layering
- Full-bleed, natural-light fashion photography used as a narrative moment
- Garment silhouettes drawn as a consistent visual catalog system
- Asymmetric image grids that resemble styling boards rather than equal SaaS cards

### Interaction language

- One dominant task per screen
- Explanation follows the decision instead of preceding it
- Human wording: “What should the look respect?” rather than “Configure constraints”
- Task-based inclusion controls rather than disability labels
- A visible separation between appearance preview and verified wearability evidence
- At least 48 dp interactive targets and explicit accessibility descriptions for non-text content

### Deliberately avoided

- Purple/blue AI gradients and glowing orbs
- Glassmorphism
- Sparkles as the overall brand language
- Three identical benefit cards
- Dense analytics dashboards on the home screen
- Gratuitous badges, emojis, and “AI-powered” labels
- Fake social proof, fake metrics, or synthetic testimonials
- Medicalized iconography or inspiration framing

## Screen-specific rationale

- **Welcome:** one editorial image makes the proposition emotional before explaining technology.
- **Today:** a single resolved outfit is the hero; the actual conflict is visible just below it.
- **Wardrobe:** catalog density and evidence scores support fast scanning without ornamental containers.
- **Plan:** toggles describe functional outcomes in ordinary language.
- **Result:** an asymmetric styling board makes the single substituted item unmistakable.
- **Lab:** the timer is visually serious but user confirmation remains authoritative.
- **Agent:** constraints are surfaced as a stable promise above the conversation.
- **Profile:** evidence history is shown as personal observations, never a diagnosis.
