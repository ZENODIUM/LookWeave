# YouCam API Reference for LookWeave

Research snapshot: 2026-10-26. Verify the live documentation and account entitlements immediately before integration.

## Best-fit submission integration

Use **AI Clothes Virtual Try-On V4** as the primary judged integration. It directly supports the winning story: the solver identifies the smallest justified garment change, then YouCam helps the user assess whether the revised look still feels like them.

Official docs: [AI Clothes Virtual Try-On](https://docs.perfectcorp.com/reference/ai_clothes/section/overview).

### V4 request lifecycle

1. Create an upload through `POST /s2s/v2.0/file`.
2. Upload bytes to the returned signed `PUT` URL.
3. Provide a garment reference via another `file_id`, image URL, or template.
4. Create a task with `POST /s2s/v2.0/task/cloth-v4`.
5. Supply the person as `src_file_id`/`src_file_url`, the garment as `ref_file_id`/`ref_file_url`/`template_id`, and the garment category.
6. Poll `GET /s2s/v2.0/task/cloth-v4/{task_id}` until success or failure.
7. Download the signed result URL promptly and store it according to consent/retention policy.

V4 supports apparel categories such as full body, upper body, lower body, shoes, and outerwear, with Auto category assistance documented in recent releases. The official release notes added jackets and vests as an outerwear category in August 2026: [YouCam changelog](https://docs.perfectcorp.com/release/changelog).

### Input validation

- Reject multiple-person images before upload.
- Explain framing requirements in the camera UI.
- Validate JPEG/PNG, dimensions, and file size client-side.
- Current source guidance is 1024×768 recommended, 512×384 minimum, maximum side 4096 px, under 10 MB, one forward-facing standing person; product references should be a front-facing single garment.
- Keep the API key on a backend; the app receives only signed, bounded upload/task access.
- Treat output as an appearance estimate, not evidence of fit, comfort, reach, closure force, or safety.
- Preserve the original and result relationship so the user can compare and delete both.

## Relevant fashion capability map

| Capability | LookWeave use | Priority |
|---|---|---|
| AI Clothes VTO V4 | Preview the solver’s complete or single-category substitution | P0 |
| Shoes VTO | Compare easy-entry footwear without losing outfit continuity | P1 |
| Hat VTO | Accessory exploration after face/style analysis | P2 |
| Scarf VTO | Test color and visual balance; never infer sensory comfort | P2 |
| Bag VTO | Check visual placement around mobility/medical devices, with user confirmation | P2 |
| Fabric VTO | Explore alternative materials or prints visually | P2 |
| Ring, bracelet, watch, earrings, necklace VTO | Complete-look accessory preview | P2 |
| Photo background removal/enhance | Normalize wardrobe item photography | P1 |
| Face attributes/color tones | Optional accessory and palette suggestions | P2 |
| Makeup/look VTO | Optional complete-look expansion, outside the core story | P3 |

Perfect Corp describes clothes, shoes, jewelry, eyewear, makeup, and hair try-on across mobile/web and notes that high-quality 2D images can support several categories: [Virtual Try On overview](https://www.perfectcorp.com/business/products/virtual-try-on).

## Error and state handling

The app needs explicit states for upload authorization, byte upload, task creation, queued/processing, success, recoverable failure, non-recoverable validation failure, cancellation, and expired result URL. Use bounded exponential backoff and stop polling when the screen/session is cancelled.

Recommended UX messages explain the next action: improve framing, use one person, select a clearer garment reference, retry later, or continue without a preview. Never expose raw vendor errors or leave an indefinite spinner.

For V4, `change_shoes` affects `full_body` and `lower_body`; `filter_multi_person` accepts `off`, `normal`, or `strict`. LookWeave uses `strict`, polls at the documented roughly 10-second interval, and accepts `full_body`, `lower_body`, `upper_body`, `shoes`, `auto`, and `outer` categories.

## Offline try-on cache (LookWeave)

App lookup order for Fit / Virtual Studio composites: **memory → disk (`filesDir/youcam-cache`) → bundled `assets/youcam-cache` → live gateway**.

Seed composites for demos (gateway on `:8787` with YouCam configured):

```text
node tools/seed-youcam-cache.mjs --garments g12
```

Writes matching JPEGs under `tools/youcam-cache/` and `app/src/main/assets/youcam-cache/` (`person1__g12.jpg` plus a content-hash twin). Stack accessories with `g12+g5`. Dry-run: add `--dry-run`.

On cache hit the app shows the stored bitmap without calling YouCam. On miss it runs `/v1/try-on` and writes memory + disk (also under the `person1` alias when the body came from the bundled sample). A new user body upload clears the `person1` alias and invalidates that person’s try-on entries so old seeds are not reused.

## Security boundary

The Android client must not contain the YouCam bearer token. A small backend should:

- authenticate the LookWeave user;
- authorize a narrowly scoped task;
- call YouCam with the server-held credential;
- validate callback/poll responses;
- enforce image retention and deletion;
- redact URLs and tokens from logs;
- return only the fields the app needs.

The existing `VirtualTryOnService` contract in `integration/ServiceContracts.kt` is the Android boundary for this implementation.
