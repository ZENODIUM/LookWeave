# LookWeave gateway for personalized discover

Product Picks, Nearby, and Accessories need the LookWeave backend (`backend/server.mjs`) running with Tavily configured. The Android app does **not** ship mock product lists for the main feed.

## Run

```bash
cd backend
# requires TAVILY_API_KEY (and optional GEMINI/GROQ for Loomie)
node server.mjs
```

Point `LOOKWEAVE_BACKEND_URL` in `local.properties` / BuildConfig at that host.

## What personalization is sent

`POST /v1/products/discover` accepts:

- `gender` — men's / women's / unisex query term + opposite-gender reject
- `postalCode` — biases “available near …” phrasing
- `colors` / `styles` / `interests` — from Preference Hub / sample person worlds
- wearability flags + sizes

The gateway runs several query variants and returns up to **12** distinct retailer product cards (not a fixed 3-item dump).

## Without the gateway

Discover shows an error that asks you to start the gateway. Nearby ZIP search and Loomie agent calls likewise need the same service.
