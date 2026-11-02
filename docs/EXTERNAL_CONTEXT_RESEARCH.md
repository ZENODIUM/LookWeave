# External context that makes a fashion agent meaningfully agentic

Research snapshot: 2026-10-27.

The useful question is not “what APIs can LookWeave display?” It is “what changing fact can alter a clothing decision and cannot be represented by a permanent preference toggle?”

## Implemented: Day Signals

Day Signals is a live decision layer, not a weather card. A user enters a city or postal code; the gateway resolves the place and retrieves current/daily forecast plus air-quality data. It translates external facts into bounded proposals:

- rain probability can justify water-resistant fabric and stable-traction footwear;
- wind gusts can rule out loose scarves or unsecured layers;
- apparent temperature and humidity can justify removable warmth or breathable, low-friction fabric;
- UV can justify lightweight coverage for outdoor exposure;
- elevated air quality or pollen never becomes a personal requirement automatically—it creates an “ask first” check-in.

This distinction is central to Adaptive Wearability Intelligence: observable environmental facts may be used as evidence, while personal sensitivity remains user-confirmed. Day Signals are added to the same evidence payload as garment facts, so Gemini can explain the smallest useful change without replacing the deterministic accessibility constraints.

Data comes from Open-Meteo’s [Geocoding API](https://open-meteo.com/en/docs/geocoding-api), [Forecast API](https://open-meteo.com/en/docs), and [Air Quality API](https://open-meteo.com/en/docs/air-quality-api). The air-quality endpoint provides AQI, particulate matter, UV and region-dependent pollen based on CAMS forecasts. Open-Meteo permits keyless non-commercial access; commercial deployment requires the appropriate licence and customer endpoint.

## Highest-value next layer: Journey Load

This is the strongest post-hackathon differentiator because the same outfit can be feasible at the destination but fail during the journey.

Inputs:

- walking distance and duration;
- stairs, slope, surface and curb conditions;
- transfers and wait time outdoors;
- live elevator/escalator outage or stop closure;
- route alternatives with less walking;
- venue entrance, toilet, lift and seating accessibility.

Agent decisions enabled by this data:

- stable footwear instead of occasion-first footwear when the route changes;
- hands-free bag placement when transfers or mobility aids require clear hands;
- a removable layer when outdoor waiting time increases;
- reduced garment bulk where a route or venue has tight transfer space;
- an alternate route or arrival plan before changing the user’s style.

GTFS Realtime service alerts explicitly support an `ACCESSIBILITY_ISSUE` effect for problems affecting step-free access, including elevator failures and movable ramps. The standard also identifies affected stops and routes. See the official [GTFS Realtime service-alert specification](https://github.com/google/transit/blob/master/gtfs-realtime/spec/en/service-alerts.md) and [GTFS overview](https://gtfs.org/documentation/realtime/feed-entities/overview/). Google’s Routes API can prefer less walking or fewer transfers, but coverage, billing and the lack of a universal accessibility guarantee require careful product wording: [Transit routes](https://developers.google.com/maps/documentation/routes/transit-route).

Recommended implementation order:

1. Consume agency GTFS Realtime alerts for the demo city and detect `ACCESSIBILITY_ISSUE`.
2. Join alerts to a user-selected route and stop, never all-city surveillance.
3. Add static GTFS Pathways/OSM surface and incline evidence with provenance.
4. Ask the user which route factors matter before making them hard constraints.
5. Let the agent choose between an outfit adjustment and a route adjustment; do not assume clothes must absorb an inaccessible environment.

## Other external signals ranked by decision value

| Signal | Why filters cannot replace it | Fashion-agent decision | Priority |
|---|---|---|---|
| Transit accessibility outage | Changes by minute and route | Footwear, hands-free carry, removable layer, alternate route | P0 |
| Route distance, grade and surface | Depends on today’s origin/destination | Traction, hem clearance, bag placement, exertion-aware layering | P0 |
| Venue accessibility facts | Specific to today’s place and entrance | Device clearance, seated comfort, coat/bag strategy | P0 |
| Official hazard alerts | Time- and place-specific, more serious than forecast | Recommend postponement or safety action before styling | P1 |
| Outdoor waiting/exposure time | Combines route and schedule | Packable warmth, rain layer, sun coverage | P1 |
| Live crowding | Changes by vehicle/event | Lower sensory load, secure accessories, device clearance | P1, coverage-limited |
| Laundry/repair availability | Changes as garments are worn or repaired | Avoid unavailable pieces and plan minimum-change alternatives | P1, first-party data |
| Product recall/safety notice | Changes after purchase | Block recalled garment and explain why | P2 |
| Indoor venue climate | Often unavailable and unreliable | Layering strategy | P2 |

For U.S. hazard alerts, the National Weather Service exposes active alerts by geographic point through `api.weather.gov/alerts/active?point=lat,lon`; its [official API primer](https://www.weather.gov/media/documentation/docs/NWS_Geolocation.pdf) explains point and zone queries. This should trigger safety-first guidance, never a cute outfit optimization during a serious warning.

## Product guardrails

- External facts carry source and observation time.
- A provider estimate is not proof of comfort, fit, safety or accessibility.
- Health-adjacent signals such as pollen and AQI require relevance confirmation; no diagnosis is inferred.
- The solver distinguishes hard user constraints, temporary day conditions, and unconfirmed suggestions.
- If the environment is inaccessible, the agent should consider route/venue alternatives before treating the user’s clothing as the problem.
- Cache geocoding and forecasts, minimize exact-location retention, and allow city/postal-code entry without device location permission.

## Hackathon framing

The pitch is not “AI chooses an outfit based on weather.” It is:

> LookWeave compiles a person’s confirmed wearability needs, garment evidence, and the real demands of their day into the smallest style-preserving change—and explains every decision.

That is harder to reproduce with a prompt and a row of filters because it relies on evidence provenance, consent states, live context, deterministic feasibility, and minimum-change planning.
