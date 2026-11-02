# LookWeave interface research: recognizably designed, not generated

Research date: October 27, 2026

## Finding

There is no single “AI aesthetic.” Interfaces feel generated when they combine familiar visual tropes without a product-specific reason: identical rounded cards for unrelated content, oversized marketing copy inside a utility flow, pills everywhere, gratuitous gradients or glow, vague labels, and polished happy paths with unresolved loading, error, permission, or empty states. The strongest correction is not a cosmetic anti-gradient rule; it is visible editorial judgment.

For LookWeave, that judgment should be obvious in three places:

1. Clothing and the user’s next decision have priority over decoration.
2. Controls use familiar Android behavior and plain language.
3. Inclusive wearability evidence is shown as useful product information, not as a novelty aesthetic.

## Evidence reviewed

- [Nielsen Norman Group: principles of visual design](https://www.nngroup.com/articles/principles-visual-design/) describes hierarchy as directing attention in order of importance. This supports one dominant decision per screen rather than several equally loud cards.
- [Apple UI design dos and don’ts](https://developer.apple.com/design/tips/) recommends screen-fit content, sufficient contrast and spacing, and controls near the content they affect. Although written for Apple platforms, these visual fundamentals are platform-independent.
- [Apple design guidance on hierarchy and controls](https://developer.apple.com/news/?id=s8sl4tpa) recommends progressive disclosure, one key tint, familiar system patterns, and questioning every item in crowded layouts.
- [Android accessibility guidance](https://developer.android.com/guide/topics/ui/accessibility/apps.html) recommends at least 48 × 48 dp focusable targets, purpose-based descriptions, and no redundant announcement of decorative icons.
- [Material Design 3 foundations](https://m3.material.io/foundations/) treats accessibility, content design, layout, state, and tokens as one system—not separate polish steps.
- [InterfaceKit’s analysis of generated UI](https://blog.interfacekit.io/what-makes-a-website-look-ai-generated) identifies unresolved generic composition and repeated card patterns as stronger signals than any one color or radius.
- [InterfaceKit’s button analysis](https://blog.interfacekit.io/why-ai-generated-buttons-look-tacky) calls out universal pill shapes, excessive glow/gradient/shadow, and missing action hierarchy.
- [ElevenLabs’ official Kotlin SDK documentation](https://elevenlabs.io/docs/eleven-agents/libraries/kotlin) exposes voice activity and audio callbacks. A voice visual can therefore communicate real state instead of playing a decorative animation.

## Audit of the previous LookWeave visual language

The earlier build repeated the same beige surface, olive card, brass uppercase eyebrow, two-dp rectangle, and editorial serif headline across almost every screen. Individually these choices were defensible. Together they resembled a generated “premium wellness” template and made unlike content look equally important. The large glowing voice sphere was the most obvious example: its motion was attractive in isolation but did not explain whether the agent was idle, connecting, listening, or speaking.

Other problems found:

- Uppercase metadata appeared even when the text was a sentence or action.
- Cards were used for navigation, evidence, notices, content groups, and promotions alike.
- The serif display face and earthy palette competed with garment photography.
- Filled calls to action, dark promotional panels, and circular icon buttons were overused.
- Voice animation ran while idle, creating activity without meaning.

## Implemented direction: Product Workroom

The redesign uses the visual logic of a good garment workroom: neutral, exact, tactile, and focused on the item being evaluated.

### Color

- Warm bone and paper surfaces make the product feel tactile without becoming a generic beige wellness template.
- Charcoal-green carries text and the deliberate dark voice surface.
- Muted eucalyptus is the action color; brushed brass marks focus and editorial hierarchy; faded terracotta is reserved for conflicts.
- Saturated blue-purple gradients, neon glow, and translucent glass cards are deliberately absent.
- Color never carries a wearability result by itself; icon and text accompany it.

### Surface and motion

- A static, extremely light paper-dot field prevents dead flat expanses without adding an image or continuous renderer.
- Desktop garments and imagery gain shallow hover depth; buttons use a short press response and every keyboard focus is visible.
- Discovery uses a fine textile-line treatment and slow, small transitions instead of glow or floating decoration.
- Android uses the same paper field, muted selected navigation surface, and state-driven transitions. Reduced-motion settings still collapse web animation durations.

### Type

- Android’s system sans replaces the generic editorial serif.
- Display sizes are slightly smaller, heavier, and more compact.
- Sentence case is the default. Uppercase survives only where it is genuinely a compact status stamp.
- There are fewer simultaneous hierarchy levels.

### Composition

- Full-bleed garment imagery remains the emotional anchor on Today and Try-on.
- Informational navigation uses flat rows and dividers; it does not masquerade as a collection of promotional cards.
- Corners vary by purpose: imagery and the voice module are softened; data regions and evidence remain crisp; chips are reserved for filters.
- Primary actions are filled, secondary actions outlined, and tertiary actions textual.

### Voice

- The glowing “magic orb” is replaced by a stable signal disc.
- Seven waveform bars respond to the SDK audio level with short interpolation.
- Idle has no looping animation. Connecting, listening, speaking, error, and ready are all named in text.
- The entire 132 dp disc is one obvious, accessible start/stop target.

## Screen-specific decisions

### Deep editorial pass (implemented)

The final pass moves beyond palette changes and makes each high-value surface structurally distinct:

- Today uses one dominant editorial image with overlapping live-context and intervention panels.
- Wardrobe opens as a working visual edit, with a photographic capsule hero and evidence-forward garment tiles rather than a generic product grid.
- Planning is a two-part outfit canvas: constraints on one side, resolved image-led composition on the other.
- Day Signals uses a compact environmental horizon, keeping live conditions visually separate from personal confirmations.
- Wearability Lab presents the selected garment as an observation subject, then exposes timing and felt effort as user-controlled evidence.
- Profile visualizes only confirmed signals as a wearability signature; it avoids diagnoses, scores, and inferred identity labels.
- Voice uses a woven signal with stateful motion and audio-reactive bars, not a decorative “magic” sphere.

The generated capsule image is treated as art direction, not interface chrome. Controls remain native, foreground contrast stays legible, and ornamental motion does not block input or alter layout.

- **Welcome:** photography, one promise, one action. No feature grid.
- **Today:** current look first; Day Signals becomes a practical row; Wearability Lab becomes a tool entry rather than a second promotional hero.
- **Wardrobe:** search and filters remain familiar; garment art and evidence carry the personality.
- **Plan:** constraints are switches because they are persistent rules, not decorative chips.
- **Result:** outfit, single change, and evidence trace are visually distinct.
- **Try-on:** input selection and paid generation are explicit; results do not claim fit or comfort.
- **Agent:** the voice state is compact enough that conversation remains visible; privacy and confirmed constraints remain close to the control.
- **Profile:** preferences and observed evidence are separated so a user can distinguish what they stated from what the app measured.

## Review checklist

- Can the screen’s primary task be named after a two-second glance?
- Is a container present because it groups related content, or merely because cards look polished?
- Does motion report a state change?
- Is there exactly one visual primary action?
- Are error, loading, permission, and empty states written in the same voice as the happy path?
- Can every action be reached through a minimum 48 dp target and understood without color?
- Does the copy describe evidence and limits without diagnosing the user or overstating virtual try-on?
