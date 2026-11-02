# Nearby discovery and live access map

## Flow

1. Open Wardrobe → **Adaptive store directory**.
2. Enter a five-digit ZIP. LookWeave geocodes only the ZIP centroid—never GPS or a street address.
3. Review real nearby destinations ranked against saved wearability priorities.
4. Swipe right or use the heart to save; swipe left or use close to pass.
5. Switch to **Map**. Saved destinations use a blue heart pin while other matches remain comparable.
6. Pan, pinch, zoom, select a pin, and open system directions.
7. Likes and passes sync between Android and desktop through `/v1/sync/state`. The ZIP does not.

The complete adaptive retailer directory remains available under **All stores**. Matching uses task preferences rather than diagnosis labels.

## Data and privacy boundary

- Zippopotam resolves the ZIP to an approximate public centroid and city/state.
- Nominatim receives retailer name plus city/state, not the user's ZIP, GPS position, or address.
- Results are held in a bounded 12-hour in-memory server cache to respect public-service limits. They are not written to the shared profile or disk.
- Place names, coordinates, and addresses come from OpenStreetMap. LookWeave's match, reason, and feature labels are its own decision layer.
- The app asks users to confirm local inventory before visiting; a nearby branch is not proof of branch-level stock.
- Public Nominatim requests are serialized at no more than one per second and identify the application. Production scale should use a contracted or self-hosted geocoder/POI service.

## Motion and accessibility

- Gesture-driven cards use draw-layer translation/rotation and an interruptible return animation.
- Buttons provide a one-handed and switch-access alternative to every swipe.
- Deck/map changes remain understandable without motion.
- Pins have spoken place names; liked state is not conveyed by color alone.
- Android uses MapLibre Native; desktop uses Leaflet. Both render real OpenStreetMap raster tiles with visible attribution and native pan/zoom controls.
- Desktop honors `prefers-reduced-motion`.

## Sources

- [MapLibre Native Android documentation](https://maplibre.org/maplibre-native/android/api/)
- [Leaflet quick start](https://leafletjs.com/examples/quick-start/)
- [OpenStreetMap tile usage policy](https://operations.osmfoundation.org/policies/tiles/)
- [Nominatim usage policy](https://operations.osmfoundation.org/policies/nominatim/)
- [Zippopotam API](https://api.zippopotam.us/)
- [Android Compose animation performance guidance](https://developer.android.com/develop/ui/compose/animation/quick-guide)
- [Android scalable content guidance](https://developer.android.com/develop/ui/compose/accessibility/scalable-content)
