# LookWeave desktop planning desk

The desktop companion is intentionally not a stretched phone interface. It is the larger planning surface for reviewing a full wardrobe, editing wearability priorities, comparing the smallest outfit change, inspecting evidence, and bringing live Day Signals into a plan.

## Run it

Start the existing gateway and open:

```text
http://127.0.0.1:8787/desktop/
```

The desktop application is buildless HTML, CSS, and JavaScript in `desktop/dist`. The gateway serves it directly, so there is no second package manager, rendering framework, or client-side secret.

## Shared state

Android and desktop use:

```text
GET /v1/sync/state
PUT /v1/sync/state
```

The state is stored in `backend/.runtime/shared-state.json` and currently contains:

- five task-based wearability preferences;
- selected planning occasion;
- bounded observation history;
- revision and update time.

The API validates every field, retains only the latest 30 observations, and never accepts arbitrary keys. The Android app restores this state on launch and writes preference or occasion changes back to the gateway. Desktop updates are debounced and surface live, saving, and offline states.

For a multi-user production deployment, replace the local JSON persistence behind these routes with authenticated records. The client contract does not need to change.

## Lightweight visual depth

The interactive garment form is three ordinary CSS planes using perspective and pointer-driven transforms. It requires no WebGL bundle, model download, animation library, or continuous render loop. It stops moving when the pointer leaves and honors `prefers-reduced-motion`.

Android uses the same principle: three GPU-composited Compose layers on onboarding, with a single slow transform. Core workflow screens remain still and legible.

## Verification

- Static HTML, CSS, JavaScript, image, and API routes return HTTP 200.
- JavaScript passes `node --check`.
- Chrome headless executes the application and renders responsive screenshots.
- Day Signals and agent requests use the existing gateway.
- Desktop-to-Android and Android-to-desktop preference changes were verified against the shared revision counter.
