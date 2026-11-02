import { createServer } from "node:http";
import { createHash, randomUUID } from "node:crypto";
import { mkdir, readFile, writeFile } from "node:fs/promises";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";
import { planOuting } from "./outing-planner.mjs";
import { createStyleService } from "./style-service.mjs";

const here = dirname(fileURLToPath(import.meta.url));
const envPath = join(here, ".env");
const rootEnvPath = join(here, "..", ".env");
const statePath = join(here, ".runtime", "eleven-agent.json");
const syncStatePath = process.env.SYNC_STATE_PATH || join(here, ".runtime", "shared-state.json");
const tryOnCachePath = process.env.TRY_ON_CACHE_PATH || join(here, ".runtime", "try-on-cache.json");

async function loadEnvFile(path) {
  try {
    const text = await readFile(path, "utf8");
    for (const rawLine of text.split(/\r?\n/)) {
      const line = rawLine.trim();
      if (!line || line.startsWith("#")) continue;
      const split = line.indexOf("=");
      if (split < 1) continue;
      const key = line.slice(0, split).trim();
      const value = line.slice(split + 1).trim().replace(/^['"]|['"]$/g, "");
      if (!process.env[key]) process.env[key] = value;
    }
  } catch (error) {
    if (error.code !== "ENOENT") throw error;
  }
}
await loadEnvFile(rootEnvPath);
await loadEnvFile(envPath);

process.on("uncaughtException", (err) => {
  console.error("Uncaught exception:", err?.message || err);
});
process.on("unhandledRejection", (reason) => {
  console.error("Unhandled rejection:", reason?.message || reason);
});

const PORT = Number(process.env.PORT || 8787);
const GEMINI_ROOT = process.env.GEMINI_API_ROOT || "https://generativelanguage.googleapis.com/v1beta";
const GROQ_ROOT = process.env.GROQ_API_ROOT || "https://api.groq.com/openai/v1";
const ELEVEN_ROOT = process.env.ELEVENLABS_API_ROOT || "https://api.elevenlabs.io/v1";
const YOUCAM_ROOT = process.env.YOUCAM_API_ROOT || "https://yce-api-01.makeupar.com/s2s/v2.0";
const TAVILY_ROOT = process.env.TAVILY_API_ROOT || "https://api.tavily.com";
const GEOCODING_ROOT = process.env.GEOCODING_API_ROOT || "https://geocoding-api.open-meteo.com/v1";
const WEATHER_ROOT = process.env.WEATHER_API_ROOT || "https://api.open-meteo.com/v1";
const AIR_QUALITY_ROOT = process.env.AIR_QUALITY_API_ROOT || "https://air-quality-api.open-meteo.com/v1";
const POSTAL_ROOT = process.env.POSTAL_API_ROOT || "https://api.zippopotam.us";
const NOMINATIM_ROOT = process.env.NOMINATIM_API_ROOT || "https://nominatim.openstreetmap.org";
const REQUEST_LIMIT = 24 * 1024 * 1024;
const dayContextCache = new Map();
const nearbyCache = new Map();
const tryOnCache = new Map();
let tryOnCacheLoaded = false;
const groqRequestTimes = [];
let groqNextKey = 0;
let groqCooldownUntil = 0;
const groqKeyCooldowns = new Map();
const tavilyRequestTimes = [];
const tavilyCache = new Map();
let geminiCooldownUntil = 0;
let tryOnInFlight = false;
let lastTryOnStartedAt = 0;
let tryOnDay = new Date().toISOString().slice(0, 10);
let tryOnCountToday = 0;

async function loadTryOnCache() {
  if (tryOnCacheLoaded) return;
  try {
    const stored = JSON.parse(await readFile(tryOnCachePath, "utf8"));
    for (const [fingerprint, value] of Object.entries(stored)) if (value?.imageUrl && value?.taskId) tryOnCache.set(fingerprint, value);
  } catch (error) { if (error.code !== "ENOENT") console.warn("Could not load preview cache", error.message); }
  tryOnCacheLoaded = true;
}

async function persistTryOnCache() {
  await mkdir(dirname(tryOnCachePath), { recursive: true });
  await writeFile(tryOnCachePath, JSON.stringify(Object.fromEntries(tryOnCache), null, 2));
}

const LOOKWEAVE_PROMPT = `You are LookWeave, an inclusive personal fashion agent. Help the user preserve their identity and style while respecting confirmed functional clothing requirements. Never infer a diagnosis. Treat confirmed constraints as hard constraints. Explain evidence plainly and propose the smallest useful change. Never claim virtual try-on proves fit, comfort, safety, reach, or closure effort. Use Day hub context (budgetRemaining, priceLimit, events, purchases, plannedOutfits, sizes, style, gender, postalCode) when relevant. Respect the user's gender for clothing suggestions — never dump opposite-gender defaults.

Keep three sources separate: (1) pieces already present in the wardrobe context, (2) categories or evidence that are missing, and (3) external products found only after an explicit browse/search request. Never describe a researched product as owned. If the user asks what they own, use search_wardrobe and answer only from wardrobe context. If they ask what they need, check the wardrobe first and identify the smallest gap. Call research_products only when the user asks to browse, search, shop, buy, or find an external alternative. For actionable requests such as “what can I wear today?”, “suggest an outfit”, or “plan my look”, solve immediately from the supplied wardrobe/context and explain the result; do not ask permission, repeat intake questions, or end with “would you like me to”. Keep the message to two or three short sentences because the app renders details and navigation as cards. Never put markdown fences, raw JSON, placeholder text, or internal reasoning inside message.

Always propose appropriate confirmable tools in proposedToolCalls matching user requests:
- get_user_location: to resolve the user's private ZIP into a general area (never a street address)
- get_day_signals: to retrieve weather, UV, wind, air quality, pollen and comfort context for the user's ZIP
- draft_rule: to draft a new clothing rule or preference (args: ruleText)
- retrieve_rules: to view or list active custom rules
- edit_rule: to edit an existing rule (args: ruleId, ruleText). When the user asks to edit an active rule, match it from custom_rules context and propose edit_rule immediately for user confirmation.
- delete_rule: to remove an existing rule (args: ruleId)
- solve_outfit: to solve or check an outfit for an occasion or context (args: occasion)
- add_calendar_event: to schedule an event (args: title, date, time). If user specifies day of week without full date, choose the next upcoming date.
- update_calendar_event: to reschedule or edit an event (args: eventId, title, date, time)
- link_outfit: to associate an outfit with an event (args: eventId, outfitId or title)
- suggest_outfit_swap: to recommend swapping a piece for another (args: garmentId or garment name, note)
- add_purchase: to record a clothing purchase (args: name, amount, date)
- update_price_limit: to set maximum price limit (args: priceLimit)
- update_budget_note: to update monthly budget guidance (args: budgetNote)
- update_profile_prefs: to update gender, postalCode, colors, styles, or interests
- log_observation: to record fit or wearability feedback (args: garmentId, note)
- research_products: to research external clothing products (args: query)

Return concise JSON only with string message and proposedToolCalls array.`;

const defaultSharedState = {
  revision: 0,
  updatedAt: null,
  preferences: {
    oneHandClosure: true,
    seatedComfort: true,
    lowSensoryLoad: false,
    deviceClearance: false,
    easyFootwear: true,
    avoidWool: false,
    animalFree: false,
    stepFreeAccess: false,
    conserveEnergy: false,
    heatSensitive: false,
    limitedOverheadReach: false,
    gripStrength: false,
    seamPressureSensitivity: false,
    easyRemoval: false,
    mobilityAidCompatibility: false,
    customRules: []
  },
  selectedOccasion: "Gallery opening",
  observations: [],
  garments: [],
  likedProductIds: [],
  passedProductIds: [],
  savedLooks: [],
  likedPlaceIds: [],
  passedPlaceIds: []
};
let sharedState;

async function readSharedState() {
  if (sharedState) return sharedState;
  try {
    sharedState = { ...defaultSharedState, ...JSON.parse(await readFile(syncStatePath, "utf8")) };
    sharedState.preferences = { ...defaultSharedState.preferences, ...sharedState.preferences };
  } catch {
    sharedState = structuredClone(defaultSharedState);
  }
  return sharedState;
}

async function updateSharedState(payload) {
  const current = await readSharedState();
  const booleanPreferences = Object.keys(defaultSharedState.preferences).filter(key => key !== "customRules");
  const allowedPreferences = Object.fromEntries(booleanPreferences.map(key => [
    key,
    typeof payload?.preferences?.[key] === "boolean" ? payload.preferences[key] : current.preferences[key]
  ]));
  const ruleKeys = new Set(["ONE_HAND_CLOSURE", "SEATED_COMFORT", "LOW_SENSORY_LOAD", "DEVICE_CLEARANCE", "EASY_FOOTWEAR", "AVOID_WOOL", "ANIMAL_FREE", "STEP_FREE_ACCESS", "CONSERVE_ENERGY", "HEAT_SENSITIVE", "LIMITED_OVERHEAD_REACH", "GRIP_STRENGTH", "SEAM_PRESSURE_SENSITIVITY", "EASY_REMOVAL", "MOBILITY_AID_COMPATIBILITY"]);
  const ruleKinds = new Set(["HARD", "SOFT", "CONDITIONAL"]);
  const cleanRules = value => Array.isArray(value) ? value.slice(-50).map(rule => ({
    id: String(rule.id || randomUUID()).slice(0, 80),
    label: String(rule.label || "Custom wearability rule").trim().slice(0, 120),
    key: ruleKeys.has(String(rule.key)) ? String(rule.key) : "ONE_HAND_CLOSURE",
    kind: ruleKinds.has(String(rule.kind)) ? String(rule.kind) : "HARD",
    enabled: rule.enabled !== false,
    confirmed: rule.confirmed === true,
    occasion: rule.occasion ? String(rule.occasion).trim().slice(0, 80) : null,
    source: String(rule.source || "User-created rule").slice(0, 120)
  })).filter(rule => rule.label && rule.confirmed) : (current.preferences.customRules || []);
  allowedPreferences.customRules = cleanRules(payload?.preferences?.customRules);
  const occasion = String(payload.selectedOccasion || current.selectedOccasion).trim().slice(0, 80) || current.selectedOccasion;
  const observations = Array.isArray(payload.observations) ? payload.observations.slice(-30).map(item => ({
    garmentId: String(item.garmentId || "").slice(0, 80),
    task: String(item.task || "").slice(0, 120),
    seconds: Math.max(0, Math.min(3600, Number(item.seconds) || 0)),
    difficulty: String(item.difficulty || "").slice(0, 80),
    recordedAt: String(item.recordedAt || new Date().toISOString()).slice(0, 40)
  })) : current.observations;
  const allowedCategories = new Set(["TOP", "BOTTOM", "OUTERWEAR", "SHOES", "ACCESSORY"]);
  const garments = Array.isArray(payload.garments) ? payload.garments.slice(-50).map(item => ({
    id: String(item.id || "").slice(0, 100), name: String(item.name || "Imported garment").slice(0, 120),
    subtitle: String(item.subtitle || "Imported source").slice(0, 240), category: allowedCategories.has(String(item.category)) ? String(item.category) : "TOP",
    colorName: String(item.colorName || "Imported").slice(0, 40), closure: item.closure || {}, fabric: item.fabric || {},
    wearability: Math.max(0, Math.min(100, Number(item.wearability) || 50)), oneHandFriendly: item.oneHandFriendly === true,
    seatedComfort: item.seatedComfort === true, lowSensoryLoad: item.lowSensoryLoad === true, deviceClearance: item.deviceClearance === true,
    available: item.available !== false, note: String(item.note || "Imported source; unverified fields remain unknown.").slice(0, 300), imageUrl: item.imageUrl ? String(item.imageUrl).slice(0, 1000) : null,
    overheadReachFriendly: item.overheadReachFriendly ?? null, gripFriendly: item.gripFriendly ?? null, lowPressure: item.lowPressure ?? null,
    easyRemoval: item.easyRemoval ?? null, mobilityAidFriendly: item.mobilityAidFriendly ?? null
  })).filter(item => item.id) : (current.garments || []);
  const cleanIds = value => Array.isArray(value) ? [...new Set(value.map(id => String(id).slice(0, 80)).filter(Boolean))].slice(-50) : null;
  const likedPlaceIds = cleanIds(payload.likedPlaceIds) ?? current.likedPlaceIds ?? [];
  const passedPlaceIds = cleanIds(payload.passedPlaceIds) ?? current.passedPlaceIds ?? [];
  const likedProductIds = cleanIds(payload.likedProductIds) ?? current.likedProductIds ?? [];
  const passedProductIds = cleanIds(payload.passedProductIds) ?? current.passedProductIds ?? [];
  sharedState = {
    ...current,
    revision: Number(current.revision || 0) + 1,
    updatedAt: new Date().toISOString(),
    preferences: allowedPreferences,
    selectedOccasion: occasion,
    observations,
    garments,
    likedPlaceIds,
    passedPlaceIds,
    likedProductIds,
    passedProductIds
  };
  await mkdir(dirname(syncStatePath), { recursive: true });
  await writeFile(syncStatePath, JSON.stringify(sharedState, null, 2));
  return sharedState;
}

function draftRuleFromText(text) {
  const value = String(text || "").trim().slice(0, 500);
  if (!value) throw new HttpError(400, "Describe the clothing requirement first");
  const candidates = [
    [/overhead|reach above|lift my arms/i, "LIMITED_OVERHEAD_REACH", "Limited overhead reach"],
    [/grip|grasp|hand strength/i, "GRIP_STRENGTH", "Grip-friendly handling"],
    [/seam|pressure|sensory/i, "SEAM_PRESSURE_SENSITIVITY", "Low seam and pressure load"],
    [/remov|take off|doff|pull-?off/i, "EASY_REMOVAL", "Easy removal"],
    [/mobility aid|wheelchair|walker|cane|crutch/i, "MOBILITY_AID_COMPATIBILITY", "Mobility-aid compatibility"],
    [/one hand|buttons|closure|fasten/i, "ONE_HAND_CLOSURE", "Easy one-hand closure"],
    [/seated|sitting/i, "SEATED_COMFORT", "Seated comfort"],
    [/wool/i, "AVOID_WOOL", "Avoid wool"],
    [/animal|leather|silk/i, "ANIMAL_FREE", "Animal-free materials"],
    [/cuff|sleeve|armband|elastic|tight|compress|restrictive|swelling|lymphedema|circulation/i, "NON_RESTRICTIVE_SLEEVES", "Non-restrictive sleeves and cuffs"],
    [/brace|prosthet|orthot|splint/i, "DEVICE_CLEARANCE", "Assistive device clearance"],
    [/tag|label|itch|scratch/i, "TAGLESS_OR_FLAT_SEAMS", "Tagless and soft seams"],
    [/heat|sweat|breathab|cool|hot/i, "BREATHABLE_TEMPERATURE_REG", "Breathable temperature regulation"],
    [/shoe|slip-on|zipper|footwear|lace/i, "EASY_FOOTWEAR", "Easy footwear access"]
  ];
  const match = candidates.find(([pattern]) => pattern.test(value));
  const kind = /prefer|usually|when possible|sometimes/i.test(value) ? "SOFT" : /when |if |for /i.test(value) ? "CONDITIONAL" : "HARD";
  if (!match) {
    const cleanLabel = value.replace(/^(please |can you |i need |i want |draft a rule to |avoid |prefer )/i, "").slice(0, 48);
    const key = cleanLabel.toUpperCase().replace(/[^A-Z0-9]+/g, "_").slice(0, 32);
    return { id: randomUUID(), label: cleanLabel || "Custom clothing requirement", key: key || "CUSTOM_RULE", kind, enabled: true, confirmed: false, occasion: null, source: `Parsed from: ${value}` };
  }
  return { id: randomUUID(), label: match[2], key: match[1], kind, enabled: true, confirmed: false, occasion: null, source: `Parsed from: ${value}` };
}

async function tavilyRequest(path, body, cacheKey) {
  const key = process.env.TAVILY_API_KEY;
  if (!key) throw new HttpError(503, "Product research is not configured");
  const cached = tavilyCache.get(cacheKey);
  if (cached && cached.expiresAt > Date.now()) return { ...cached.value, cached: true };
  const now = Date.now();
  while (tavilyRequestTimes.length && tavilyRequestTimes[0] < now - 60_000) tavilyRequestTimes.shift();
  const limit = Math.max(1, Number(process.env.TAVILY_LOCAL_RPM_LIMIT || 6));
  if (tavilyRequestTimes.length >= limit) throw new HttpError(429, "Product research is taking a short pause. Try again in a minute.");
  tavilyRequestTimes.push(now);
  const response = await fetch(`${TAVILY_ROOT}${path}`, {
    method: "POST",
    headers: { authorization: `Bearer ${key}`, "content-type": "application/json" },
    body: JSON.stringify(body),
    signal: AbortSignal.timeout(30_000)
  });
  const value = await response.json().catch(() => ({}));
  if (!response.ok) {
    const retryAfter = Number(response.headers.get("retry-after"));
    throw new HttpError(response.status, value?.detail?.error || value?.error || "Product research failed", null, Number.isFinite(retryAfter) ? retryAfter : null);
  }
  tavilyCache.set(cacheKey, { expiresAt: Date.now() + 6 * 60 * 60_000, value });
  return { ...value, cached: false };
}

function productImageUrl(value) {
  const raw = typeof value === "string" ? value : value?.url;
  if (typeof raw !== "string") return null;
  let url;
  try { url = new URL(raw.trim()); } catch { return null; }
  if (url.protocol !== "https:") return null;
  const path = decodeURIComponent(url.pathname).toLowerCase();
  if (/\.(svg|ico)$/.test(path)) return null;
  if (/(^|\/)(favicon|sprite|spacer|pixel|tracking)([^/]*)$/.test(path)) return null;
  if (/(^|\/)logo[^/]*\.(png|jpe?g|gif|webp|avif)$/.test(path)) return null;
  let cleanUrl = url.toString().slice(0, 1000);
  if (/media-amazon\.com\/images/i.test(cleanUrl)) {
    cleanUrl = cleanUrl.replace(/\._AC_[^.]*_\./, "._AC_SL800_.");
  }
  return cleanUrl;
}

function imageQuality(url) {
  const path = new URL(url).pathname.toLowerCase();
  if (/sprite|grey-pixel|pixel\.gif|\/sash\//.test(path)) return 0;
  const portrait = path.match(/sx(\d+).*sy(\d+)/);
  if (portrait) {
    const width = Number(portrait[1]);
    const height = Number(portrait[2]);
    if (width >= 240 && height >= 240) return 100;
  }
  const thumb = path.match(/sr(\d+),(\d+)/);
  if (thumb && Math.max(Number(thumb[1]), Number(thumb[2])) < 200) return 0;
  const square = path.match(/ss(\d+)/);
  if (square && Number(square[1]) < 200 && !portrait) return 0;
  if (/aplus-media/.test(path) && /sx1\d{3}/.test(path)) return 0;
  if (/\/images\/i\//.test(path)) return 80;
  return 50;
}

function firstProductImage(images) {
  if (!Array.isArray(images)) return null;
  let best = null;
  let bestScore = 0;
  for (const image of images) {
    const url = productImageUrl(image);
    if (!url) continue;
    const score = imageQuality(url);
    if (score > bestScore) { best = url; bestScore = score; }
  }
  return best;
}

function sameSourceImage(value, pageUrl) {
  const image = productImageUrl(value);
  if (!image) return null;
  try {
    const imageHost = new URL(image).hostname.replace(/^www\./, "");
    const pageHost = new URL(pageUrl).hostname.replace(/^www\./, "");
    if (imageHost === pageHost || imageHost.endsWith(`.${pageHost}`) || pageHost.endsWith(`.${imageHost}`)) return image;
  } catch { return null; }
  return null;
}

function preferredSources(results) {
  const social = /(?:^|\.)(facebook|instagram|pinterest|tiktok|youtube|reddit|x|twitter)\.com$/i;
  const rank = item => {
    let host = "";
    try { host = new URL(item.url).hostname.replace(/^www\./, ""); } catch { return 3; }
    if (social.test(host)) return 3;
    if (/\/(products?|shop|item|dp)\b/i.test(item.url) && !/\/(blogs?|news|articles?|guides?)\b/i.test(item.url)) return 0;
    if (/(about|blog|news|article|guide)/i.test(item.url)) return 2;
    return 1;
  };
  return (results || []).filter(item => item.url).sort((a, b) => rank(a) - rank(b)).slice(0, 5);
}

async function declaredPageImage(pageUrl) {
  try {
    const response = await fetch(pageUrl, {
      headers: { "user-agent": "Mozilla/5.0 (Linux; Android 14) LookWeave/1.0", accept: "text/html" },
      redirect: "follow",
      signal: AbortSignal.timeout(8_000)
    });
    if (!response.ok) return { alive: false, imageUrl: null };
    const html = (await response.text()).slice(0, 250_000);
    const patterns = [
      /<meta[^>]+property=["']og:image(?::secure_url)?["'][^>]+content=["']([^"']+)["']/i,
      /<meta[^>]+content=["']([^"']+)["'][^>]+property=["']og:image(?::secure_url)?["']/i,
      /<meta[^>]+name=["']twitter:image["'][^>]+content=["']([^"']+)["']/i
    ];
    for (const pattern of patterns) {
      const imageUrl = productImageUrl(html.match(pattern)?.[1]);
      if (imageUrl) return { alive: true, imageUrl };
    }
    return { alive: true, imageUrl: null };
  } catch {
    return { alive: true, imageUrl: null };
  }
}

function evidenceExcerpt(title, snippet, extracted) {
  const corpus = `${snippet || ""} ${extracted || ""}`.replace(/\s+/g, " ").trim();
  const skip = new Set(["women", "woman", "mens", "clothing", "amazon", "store", "adaptive", "product", "front", "open", "black", "white"]);
  const tokens = [...new Set(String(title || "").toLowerCase().split(/[^a-z0-9]+/).filter(word => word.length > 4 && !skip.has(word)))];
  let best = "";
  let bestHits = 0;
  for (const window of corpus.split(/\s+\[\.\.\.\]\s+|(?<=[.!?])\s+/)) {
    const hits = tokens.filter(token => window.toLowerCase().includes(token)).length;
    if (hits > bestHits) { best = window.trim(); bestHits = hits; }
  }
  if (bestHits < 2 || !tokens.slice(0, 2).some(token => best.toLowerCase().includes(token))) return "";
  return best.replace(/[#|*]+/g, " ").replace(/\s+/g, " ").trim().slice(0, 280);
}

function genderSearchTerm(gender) {
  const raw = String(gender || "").trim().toLowerCase();
  if (!raw || raw === "prefer not to say" || raw === "custom gender") return null;
  if (raw === "male" || raw === "men" || raw === "mens" || raw === "man's") return "men's";
  if (raw === "female" || raw === "women" || raw === "womens" || raw === "woman's") return "women's";
  if (raw === "non-binary" || raw === "nonbinary") return "gender neutral unisex";
  return `${raw} clothing`;
}

function sizeSearchTerms(sizes = {}) {
  if (!sizes || typeof sizes !== "object") return [];
  return Object.entries(sizes)
    .filter(([, value]) => String(value || "").trim())
    .slice(0, 4)
    .map(([key, value]) => {
      const label = String(key).replace(/^measure:/, "").replace(/([A-Z])/g, " $1").trim().toLowerCase();
      return `size ${value} ${label}`;
    });
}

function listTerms(value) {
  if (Array.isArray(value)) return value.map(item => String(item || "").trim()).filter(Boolean).slice(0, 4);
  if (typeof value === "string" && value.trim()) return value.split(/[,|]/).map(item => item.trim()).filter(Boolean).slice(0, 4);
  return [];
}

function activeSearchTerms(filters = {}) {
  const custom = Array.isArray(filters.customRules) ? filters.customRules.filter(rule => rule && rule.enabled !== false && rule.confirmed !== false).map(rule => String(rule.label || "").trim()).filter(Boolean) : [];
  const colors = listTerms(filters.colors || filters.preferredColors);
  const styles = listTerms(filters.styles || filters.preferredStyles);
  const interests = listTerms(filters.interests);
  const zip = String(filters.postalCode || filters.zip || "").trim();
  return [
    filters.query,
    genderSearchTerm(filters.gender),
    ...colors.map(color => `${color} color`),
    ...styles,
    ...interests,
    zip.length === 5 ? `available near ${zip}` : null,
    ...sizeSearchTerms(filters.sizes),
    filters.oneHandClosure && "magnetic closure or easy fastening",
    filters.lowSensoryLoad && "soft low sensory",
    filters.seatedComfort && "seated comfort",
    filters.easyRemoval && "easy removal",
    filters.mobilityAidCompatibility && "mobility aid friendly",
    filters.deviceClearance && "device clearance",
    filters.easyFootwear && "easy on footwear",
    filters.avoidWool && "cotton no wool",
    filters.animalFree && "animal free materials",
    filters.limitedOverheadReach && "front opening no overhead fastening",
    filters.gripStrength && "grip friendly magnets",
    filters.seamPressureSensitivity && "flat seams low pressure",
    filters.heatSensitive && "lightweight breathable",
    filters.conserveEnergy && "easy dressing",
    ...custom,
    "adaptive clothing product"
  ].filter(Boolean);
}

function discoveryReason(filters) {
  const priorities = [
    filters.oneHandClosure && "easy closures",
    filters.lowSensoryLoad && "a low sensory load",
    filters.seatedComfort && "seated comfort",
    filters.easyRemoval && "easy removal",
    filters.mobilityAidCompatibility && "mobility-aid compatibility",
    filters.deviceClearance && "device clearance",
    filters.easyFootwear && "easy footwear",
    filters.avoidWool && "no wool",
    filters.animalFree && "animal-free materials",
    filters.limitedOverheadReach && "limited overhead reach",
    filters.gripStrength && "grip-friendly handling",
    filters.seamPressureSensitivity && "low seam pressure",
    filters.heatSensitive && "lightweight breathable layers",
    filters.conserveEnergy && "easy dressing",
    ...(Array.isArray(filters.customRules) ? filters.customRules.filter(rule => rule && rule.enabled !== false && rule.confirmed !== false).map(rule => rule.label) : [])
  ].filter(Boolean);
  if (!priorities.length) return "Found from a source-backed clothing search. Confirm the product page before buying.";
  const listed = priorities.length === 1 ? priorities[0] : `${priorities.slice(0, -1).join(", ")} and ${priorities.at(-1)}`;
  return `Matched to your search for ${listed}. Confirm those details on the product page before buying.`;
}

async function researchProducts(query, domains = [], options = {}) {
  const cleanQuery = String(query || "").trim().slice(0, 180);
  if (cleanQuery.length < 3) throw new HttpError(400, "Describe the product or clothing need to research");
  const focus = options.focus === "accessories"
    ? "buyable product page price materials"
    : "clothing materials closure accessibility product page price";
  const searchBody = {
    query: `${cleanQuery} ${focus}`,
    search_depth: "basic", max_results: 8, include_answer: false,
    include_raw_content: false, include_images: true, include_favicon: true, max_age_hours: 24
  };
  if (domains.length) searchBody.include_domains = domains.slice(0, 20);
  const search = await tavilyRequest("/search", searchBody, `search:${options.focus || "apparel"}:${domains.join(",")}:${cleanQuery.toLowerCase()}`);
  const sources = preferredSources(search.results);
  const extracted = sources.length ? await tavilyRequest("/extract", {
    urls: sources.map(item => item.url), query: cleanQuery, chunks_per_source: 2,
    extract_depth: "basic", format: "text", include_images: true
  }, `extract-images:${sources.map(item => item.url).join("|")}`) : { results: [], failed_results: [] };
  const extractedByUrl = new Map((extracted.results || []).map(item => [item.url, { content: item.raw_content || "", images: item.images || [] }]));
  return {
    query: cleanQuery,
    sourceCount: sources.length,
    images: search.images || [],
    sources: sources.map(item => ({
      title: item.title,
      url: item.url,
      snippet: item.content,
      evidence: extractedByUrl.get(item.url)?.content || null,
      images: [...(item.images || []), ...(extractedByUrl.get(item.url)?.images || [])]
    })),
    failedSources: extracted.failed_results || [],
    policy: "Source excerpts only. Price, stock, fit, closure effort and accessibility remain unknown unless a source states them."
  };
}

const retailerDomains = ["silverts.com", "joeandbella.com", "izadaptive.com", "magnaready.com", "buckandbuck.com", "tommy.com", "zappos.com", "target.com", "amazon.com", "macys.com", "kohls.com"];

async function productCards(result, filters) {
  const drafted = (await Promise.all(result.sources.map(async (source, index) => {
    let imageUrl = firstProductImage(source.images) || sameSourceImage(result.images?.[index], source.url) || null;
    if (!imageUrl) {
      const media = await declaredPageImage(source.url);
      if (!media.alive) return null;
      imageUrl = media.imageUrl;
    }
    if (!imageUrl) return null;
    return {
      id: createHash("sha256").update(source.url).digest("hex").slice(0, 20),
      name: source.title,
      retailer: (() => { try { return new URL(source.url).hostname.replace(/^www\./, ""); } catch { return "Source retailer"; } })(),
      productUrl: source.url,
      imageUrl,
      reason: discoveryReason(filters),
      evidence: evidenceExcerpt(source.title, source.snippet, source.evidence),
      unknowns: ["Local stock", "Physical fit", "Closure effort"],
      source: source.url
    };
  }))).filter(Boolean);
  return drafted.sort((a, b) => {
    const score = url => /\/(products?|shop|item|dp)\b/i.test(url) && !/\/(blogs?|news|articles?|guides?)\b/i.test(url) ? 0 : 1;
    return score(a.productUrl) - score(b.productUrl);
  });
}

function extractPrice(...texts) {
  const corpus = texts.filter(Boolean).join(" ");
  const match = corpus.match(/(?:USD|US\$|\$)\s?(\d{1,4}(?:[.,]\d{2})?)|(?:€|£)\s?(\d{1,4}(?:[.,]\d{2})?)/i);
  if (!match) return null;
  const amount = match[1] || match[2];
  const symbol = /€/.test(match[0]) ? "€" : /£/.test(match[0]) ? "£" : "$";
  return `${symbol}${amount}`;
}

async function discoverProducts(filters = {}) {
  const genderTerm = genderSearchTerm(filters.gender);
  const colors = listTerms(filters.colors || filters.preferredColors);
  const interests = listTerms(filters.interests);
  const baseTerms = activeSearchTerms(filters).join(" ");
  // Multiple personalized queries so feeds are not the same 3 adaptive dumps.
  const queryVariants = [
    baseTerms,
    [genderTerm, colors[0], interests[0] || filters.query, "clothing product"].filter(Boolean).join(" "),
    [genderTerm, colors[1] || colors[0], interests[1] || "everyday wear", "buyable product"].filter(Boolean).join(" "),
    [genderTerm, interests[2] || stylesFirst(filters), "retail product page"].filter(Boolean).join(" ")
  ].map(query => query.trim()).filter((query, index, all) => query.length >= 8 && all.indexOf(query) === index);

  const batches = [];
  for (const query of queryVariants.slice(0, 3)) {
    try {
      batches.push(await productCards(await researchProducts(query, retailerDomains), filters));
    } catch {
      /* keep gathering from remaining variants */
    }
  }
  if (batches.flat().length < 4) {
    try {
      batches.push(await productCards(await researchProducts(baseTerms || `${genderTerm || ""} adaptive clothing product`.trim()), filters));
    } catch {
      /* empty result surfaces honestly in the app */
    }
  }
  const seen = new Set();
  const products = batches.flat()
    .filter(product => !seen.has(product.productUrl) && seen.add(product.productUrl))
    .filter(product => !rejectsOppositeGender(product, filters.gender))
    .slice(0, 12)
    .map((product, index) => ({
      ...product,
      match: index + 1,
      price: extractPrice(product.evidence, product.name),
      compatibility: "Needs review"
    }));
  return {
    products,
    queryVariants,
    feedback: { liked: (await readSharedState()).likedProductIds || [], passed: (await readSharedState()).passedProductIds || [] },
    policy: "Product source and image shown from the result page. Local stock, fit and accessibility are not guaranteed unless explicitly stated. Requires LookWeave gateway + Tavily."
  };
}

function stylesFirst(filters = {}) {
  return listTerms(filters.styles || filters.preferredStyles)[0] || "wearable clothing";
}

function rejectsOppositeGender(product, gender) {
  const term = genderSearchTerm(gender);
  if (!term) return false;
  const corpus = `${product.name || ""} ${product.productUrl || ""} ${product.evidence || ""}`.toLowerCase();
  if (term === "men's") {
    return /\b(women'?s|womens|ladies|female only)\b/.test(corpus) && !/\b(men'?s|mens|unisex|gender[- ]?neutral)\b/.test(corpus);
  }
  if (term === "women's") {
    return /\b(men'?s|mens|male only)\b/.test(corpus) && !/\b(women'?s|womens|unisex|gender[- ]?neutral)\b/.test(corpus);
  }
  return false;
}

const eyewearDomains = ["warbyparker.com", "zennioptical.com", "glassesusa.com", "eyebuydirect.com", "nordstrom.com", "zappos.com", "target.com"];
const jewelleryDomains = ["mejuri.com", "catbirdnyc.com", "nordstrom.com", "macys.com", "target.com", "zappos.com", "amazon.com"];
const watchDomains = ["nordstrom.com", "macys.com", "zappos.com", "target.com", "amazon.com", "shinola.com", "timex.com"];
const scarfDomains = ["nordstrom.com", "macys.com", "target.com", "zappos.com", "uniqlo.com", "everlane.com"];
const hatDomains = ["nordstrom.com", "zappos.com", "target.com", "amazon.com", "brixton.com", "patagonia.com"];
const shoeDomains = ["zappos.com", "nordstrom.com", "target.com", "amazon.com", "birkenstock.com", "vionicshoes.com", "silverts.com"];

const ACCESSORY_SUBCATEGORIES = ["glasses", "watches", "jewellery", "scarf", "hat", "shoes"];

function normalizeAccessorySubcategory(value) {
  const raw = String(value || "").trim().toLowerCase();
  if (!raw) return null;
  if (raw === "eyewear" || raw === "eyeglasses" || raw === "glasses") return "glasses";
  if (raw === "watch" || raw === "watches") return "watches";
  if (raw === "jewelry" || raw === "jewellery") return "jewellery";
  if (raw === "scarves" || raw === "scarf") return "scarf";
  if (raw === "hats" || raw === "hat" || raw === "caps") return "hat";
  if (raw === "shoe" || raw === "shoes" || raw === "footwear") return "shoes";
  return ACCESSORY_SUBCATEGORIES.includes(raw) ? raw : null;
}

function accessoryRejectReason(kind, product) {
  const corpus = `${product.name || ""} ${product.productUrl || ""} ${product.evidence || ""}`.toLowerCase();
  if (kind === "glasses" || kind === "eyewear") {
    if (!/\b(eye ?glass|sunglass|spectacles?|frames?|optical)\b/.test(corpus)) return "not glasses";
    if (/\b(case only|lens cloth|cleaner|repair kit|replacement nose)\b/.test(corpus)) return "accessory-only";
  }
  if (kind === "jewellery") {
    if (!/\b(necklace|earring|earrings|ring|bracelet|pendant|hoop|stud|chain|jewellery|jewelry)\b/.test(corpus)) return "not jewellery";
    if (/\b(clasp bundle|jewelry making|jewellery making|connector|findings|pearl clasp|clasp post|lobster closure for necklace bracelet)\b/.test(corpus)) return "findings-only";
    if (/\b(magnetic necklace clasp|magnetic jewelry clasp|clasp bundle)\b/.test(corpus)) return "findings-only";
  }
  if (kind === "watches") {
    if (!/\b(watch|watches|timepiece)\b/.test(corpus)) return "not watches";
    if (/\b(watch band only|strap only|battery|winder)\b/.test(corpus)) return "parts-only";
  }
  if (kind === "scarf") {
    if (!/\b(scarf|scarves|wrap|shawl)\b/.test(corpus)) return "not scarf";
  }
  if (kind === "hat") {
    if (!/\b(hat|hats|cap|beanie|beret|fedora|brim)\b/.test(corpus)) return "not hat";
  }
  if (kind === "shoes") {
    if (!/\b(shoe|shoes|sneaker|loafer|boot|sandal|footwear|slip[- ]on)\b/.test(corpus)) return "not shoes";
  }
  return null;
}

async function accessoryCards(kind, query, domains, filters, reason, unknowns) {
  const focused = await productCards(await researchProducts(query, domains, { focus: "accessories" }), filters);
  const open = focused.length >= 2 ? [] : await productCards(await researchProducts(query, [], { focus: "accessories" }), filters);
  const seen = new Set();
  return [...focused, ...open]
    .filter(product => !accessoryRejectReason(kind, product))
    .filter(product => !seen.has(product.productUrl) && seen.add(product.productUrl))
    .slice(0, 3)
    .map((product, index) => ({
      ...product,
      kind,
      match: index + 1,
      price: extractPrice(product.evidence, product.name),
      reason,
      unknowns,
      compatibility: "Needs review"
    }));
}

function accessorySearchPlan(subcategory, filters = {}) {
  const metal = String(filters.metal || "Explore");
  const colour = String(filters.colourDirection || "Explore");
  const shape = String(filters.faceShape || "Unknown");
  const metalTerm = metal === "Explore" ? (colour === "Warm" ? "gold" : colour === "Cool" ? "silver" : "gold or silver") : metal.toLowerCase();
  const frameHints = {
    Round: "rectangular eyeglasses frames",
    Square: "oval eyeglasses frames",
    Heart: "cat eye eyeglasses frames",
    InvTriangle: "oval eyeglasses frames",
    Oblong: "round eyeglasses frames",
    Diamond: "oval eyeglasses frames",
    Triangle: "browline eyeglasses frames",
    Oval: "classic eyeglasses frames",
    Unknown: "eyeglasses frames"
  };
  const nickel = filters.nickelAvoidance ? "nickel free" : null;
  switch (subcategory) {
    case "glasses":
      return {
        domains: eyewearDomains,
        query: [
          frameHints[shape] || "eyeglasses frames",
          "for sale",
          filters.lightweightFrames && "lightweight acetate or titanium",
          filters.lowBridge && "low bridge fit",
          filters.hearingDevice && "flexible temples",
          colour === "Warm" ? "warm tortoise or gold" : colour === "Cool" ? "black or silver" : null
        ].filter(Boolean).join(" "),
        reason: `Matched to your ${shape === "Unknown" ? "open" : shape.toLowerCase()} frame direction${filters.lightweightFrames ? ", lightweight preference" : ""}${filters.lowBridge ? ", and low-bridge request" : ""}. Confirm millimetre fit in person.`,
        unknowns: ["Lens width and bridge fit", "Temple pressure", "Prescription compatibility", ...(filters.nickelAvoidance ? ["Nickel content"] : [])]
      };
    case "jewellery":
      return {
        domains: jewelleryDomains,
        query: [
          `${metalTerm} necklace earrings`,
          "fine jewellery product",
          filters.easyClasps && "magnetic clasp necklace or leverback earrings",
          nickel
        ].filter(Boolean).join(" "),
        reason: `Matched to your ${metalTerm} finish preference${filters.easyClasps ? " and easy-closure request" : ""}. Confirm material and clasp feel before buying.`,
        unknowns: ["Exact metal composition", "Clasp effort", ...(filters.nickelAvoidance ? ["Nickel content"] : [])]
      };
    case "watches":
      return {
        domains: watchDomains,
        query: [`${metalTerm} watch`, "buyable wristwatch product", filters.easyClasps && "easy buckle or magnetic clasp watch", nickel].filter(Boolean).join(" "),
        reason: `Matched to your ${metalTerm} finish preference for watches. Confirm clasp effort and band fit before buying.`,
        unknowns: ["Band size", "Clasp effort", ...(filters.nickelAvoidance ? ["Nickel content"] : [])]
      };
    case "scarf":
      return {
        domains: scarfDomains,
        query: [colour === "Warm" ? "warm tone scarf" : colour === "Cool" ? "cool tone scarf" : "soft scarf wrap", "buyable scarf product", filters.lowSensoryLoad && "soft seamless scarf"].filter(Boolean).join(" "),
        reason: "Matched as a soft accessory accent for your colour direction. Confirm fibre content and weight before buying.",
        unknowns: ["Exact fibre content", "Weight and drape"]
      };
    case "hat":
      return {
        domains: hatDomains,
        query: [shape === "Oblong" ? "wide brim low crown hat" : "classic hat", "buyable hat product", colour === "Warm" ? "warm tone hat" : colour === "Cool" ? "cool tone hat" : null].filter(Boolean).join(" "),
        reason: "Matched as a hat silhouette to explore with your face proportions. Confirm crown fit and brim feel in person.",
        unknowns: ["Crown fit", "Brim stiffness"]
      };
    case "shoes":
      return {
        domains: shoeDomains,
        query: [
          filters.easyFootwear !== false ? "easy on slip on or zip shoes" : "comfortable shoes",
          "buyable footwear product",
          filters.gripStrength && "large pull loop shoes",
          filters.oneHandClosure && "magnetic or zip shoes"
        ].filter(Boolean).join(" "),
        reason: "Matched for easier footwear entry. Confirm closure effort and fit before buying.",
        unknowns: ["Exact size fit", "Closure effort", "Local stock"]
      };
    default:
      return null;
  }
}

async function discoverAccessories(filters = {}) {
  const requested = normalizeAccessorySubcategory(filters.subcategory || filters.kind || filters.accessoryKind);
  const targets = requested ? [requested] : ["glasses", "jewellery"];
  const buckets = {};
  const products = [];
  for (const subcategory of targets) {
    const plan = accessorySearchPlan(subcategory, filters);
    if (!plan) continue;
    const cards = await accessoryCards(subcategory, plan.query, plan.domains, { query: plan.query, ...filters }, plan.reason, plan.unknowns);
    buckets[subcategory] = cards;
    products.push(...cards);
  }
  // Back-compat keys used by older clients
  const eyewear = buckets.glasses || [];
  const jewellery = buckets.jewellery || [];
  return {
    subcategory: requested || "mixed",
    subcategories: ACCESSORY_SUBCATEGORIES,
    products,
    eyewear,
    jewellery,
    glasses: eyewear,
    watches: buckets.watches || [],
    scarf: buckets.scarf || [],
    hat: buckets.hat || [],
    shoes: buckets.shoes || [],
    policy: "Source-backed accessory pages only. Fit, materials and accessibility stay unverified unless the page states them."
  };
}

async function ingestProductUrl(value) {
  let url;
  try { url = new URL(String(value || "").trim()); } catch { throw new HttpError(400, "Enter a complete product URL"); }
  if (!/^https?:$/.test(url.protocol)) throw new HttpError(400, "Product URL must use http or https");
  const extracted = await tavilyRequest("/extract", {
    urls: [url.toString()],
    query: "product name category clothing materials fabric closure accessibility features price availability images",
    chunks_per_source: 5, extract_depth: "basic", format: "text", include_images: true
  }, `product:${url.toString()}`);
  const result = extracted.results?.[0];
  if (!result?.raw_content) throw new HttpError(422, "The product page did not return usable product details");
  const text = result.raw_content.replace(/\s+/g, " ").trim();
  const priceMatch = text.match(/(?:[$€£]\s?\d+(?:[,.]\d{2})?|\d+(?:[,.]\d{2})?\s?(?:USD|EUR|GBP))/i);
  const materialMatch = text.match(/(?:material|fabric|composition)\s*[:\-]?\s*([^.;]{3,100})/i);
  const closureMatch = text.match(/\b(magnetic|magnet(?:ic)?|zip(?:per)?|button(?:ed)?|snap|hook[- ]and[- ]eye|pull[- ]on|elastic|lace[- ]up|slip[- ]on|velcro)\b/i);
  const titleMatch = text.match(/(?:men['’]s|women['’]s|unisex)?\s*[A-Z][^.;]{5,100}\b(?:shirt|blouse|top|trouser|pant|jean|dress|skirt|jacket|blazer|coat|shoe|sneaker|loafer|scarf|necklace)\b/i);
  const rawTitle = titleMatch?.[0]?.replace(/\s+/g, " ").trim() || null;
  const titleStart = rawTitle?.search(/(?:men['’]s|women['’]s|unisex)\b/i) ?? -1;
  const title = rawTitle ? (titleStart >= 0 ? rawTitle.slice(titleStart) : rawTitle) : null;
  const categoryMatch = (title || text).match(/\b(shirt|blouse|top|trouser|pant|jean|dress|skirt|jacket|blazer|coat|shoe|sneaker|loafer|scarf|necklace)\b/i);
  return {
    sourceUrl: url.toString(),
    name: title,
    category: categoryMatch?.[1] || null,
    material: materialMatch?.[1]?.trim() || null,
    closure: closureMatch?.[0]?.trim() || null,
    price: priceMatch?.[0] || null,
    availability: null,
    accessibility: null,
    images: result.images || [],
    evidence: { source: url.toString(), extractedAt: new Date().toISOString(), note: "Only text explicitly present on the source page is returned; missing facts remain unknown." },
    failedSources: extracted.failed_results || []
  };
}

class HttpError extends Error {
  constructor(status, message, details, retryAfterSeconds = null) {
    super(message);
    this.status = status;
    this.details = details;
    this.retryAfterSeconds = retryAfterSeconds;
  }
}

function send(res, status, body) {
  const payload = Buffer.from(JSON.stringify(body));
  res.writeHead(status, {
    "content-type": "application/json; charset=utf-8",
    "content-length": String(payload.length),
    "connection": "close",
    "cache-control": "no-store",
    "access-control-allow-origin": "*",
    "access-control-allow-headers": "content-type",
    "access-control-allow-methods": "GET,POST,PUT,OPTIONS"
  });
  res.end(payload);
}

async function readJson(req) {
  const chunks = [];
  let size = 0;
  for await (const chunk of req) {
    size += chunk.length;
    if (size > REQUEST_LIMIT) throw new HttpError(413, "Request is too large");
    chunks.push(chunk);
  }
  if (!chunks.length) return {};
  try {
    return JSON.parse(Buffer.concat(chunks).toString("utf8"));
  } catch {
    throw new HttpError(400, "Request body must be valid JSON");
  }
}

async function providerFetch(url, options = {}, timeoutMs = 30_000) {
  const response = await fetch(url, { ...options, signal: AbortSignal.timeout(timeoutMs) });
  const text = await response.text();
  let body;
  try { body = text ? JSON.parse(text) : {}; } catch { body = { raw: text.slice(0, 500) }; }
  if (!response.ok) {
    const providerMessage = body?.error?.message || body?.error || body?.detail?.message || `Provider returned ${response.status}`;
    const retryHeader = response.headers.get("retry-after");
    const retrySeconds = retryHeader == null ? null : Number(retryHeader);
    const retryDate = retryHeader && !Number.isFinite(retrySeconds) ? Date.parse(retryHeader) : NaN;
    const retryAfter = Number.isFinite(retrySeconds) ? retrySeconds : Number.isFinite(retryDate) ? Math.max(0, Math.ceil((retryDate - Date.now()) / 1000)) : null;
    throw new HttpError(response.status >= 500 ? 502 : response.status, String(providerMessage), body?.error_code, retryAfter);
  }
  return body;
}

const nearbyBrands = [
  { query: "Target", match: 96, reason: "Strong match for sensory-aware basics, access openings and easy closures.", features: ["Low sensory", "Easy closure", "Device access"], suggestedPieces: ["Sensory-friendly tees", "Adaptive pants", "Easy-on kids shoes"], color: "#60786F" },
  { query: "JCPenney", match: 94, reason: "Adaptive essentials with seated cuts, flat seams and easier fastenings.", features: ["Seated wear", "Easy closure", "Extended sizing"], suggestedPieces: ["Side-zip trousers", "Magnetic polos", "Adaptive jeans"], color: "#8A665A" },
  { query: "Kohl's", match: 93, reason: "Everyday adaptive options across sensory, closure and footwear needs.", features: ["Low sensory", "Easy closure", "Easy footwear"], suggestedPieces: ["Tagless layers", "Adaptive sleepwear", "Easy-entry sneakers"], color: "#7D6568" },
  { query: "Nike", match: 90, reason: "A strong destination for hands-free and easy-entry sport footwear.", features: ["Easy footwear", "Easy closure"], suggestedPieces: ["Hands-free EasyOn shoes", "Easy-entry sneakers"], color: "#242625" },
  { query: "Macy's", match: 85, reason: "Broad footwear and size ranges make in-person comparison easier.", features: ["Extended sizing", "Easy footwear"], suggestedPieces: ["Wide-width shoes", "Adaptive dress options"], color: "#8B5363" },
  { query: "Tommy Hilfiger", match: 89, reason: "A useful stop for checking classic silhouettes alongside adaptive lines.", features: ["Easy closure", "Seated wear"], suggestedPieces: ["Magnetic button shirts", "Seated-wear denim", "One-hand zipper layers"], color: "#536B6B" }
];

function distanceMiles(aLat, aLon, bLat, bLon) {
  const radians = value => Number(value) * Math.PI / 180;
  const dLat = radians(bLat - aLat), dLon = radians(bLon - aLon);
  const h = Math.sin(dLat / 2) ** 2 + Math.cos(radians(aLat)) * Math.cos(radians(bLat)) * Math.sin(dLon / 2) ** 2;
  return 3958.8 * 2 * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
}

function shortAddress(item) {
  const a = item.address || {};
  const street = [a.house_number, a.road].filter(Boolean).join(" ");
  return [street, a.city || a.town || a.village || a.suburb, a.state].filter(Boolean).join(", ") || String(item.display_name || "").split(",").slice(0, 3).join(",");
}

async function getNearbyPlaces(postalCode) {
  const zip = String(postalCode || "").trim();
  if (!/^\d{5}$/.test(zip)) throw new HttpError(400, "Enter a five-digit US ZIP code");
  const cached = nearbyCache.get(zip);
  if (cached && cached.expiresAt > Date.now()) return cached.value;

  const postal = await providerFetch(`${POSTAL_ROOT}/us/${encodeURIComponent(zip)}`, {
    headers: { "user-agent": "LookWeave/1.0" }
  }, 12_000);
  const place = postal?.places?.[0];
  if (!place) throw new HttpError(404, "That ZIP code could not be found");
  const latitude = Number(place.latitude), longitude = Number(place.longitude);
  const city = String(place["place name"] || "");
  const state = String(place["state abbreviation"] || place.state || "");
  const results = [];
  const liveNominatim = NOMINATIM_ROOT.includes("nominatim.openstreetmap.org");

  for (const brand of nearbyBrands) {
    if (liveNominatim && brand !== nearbyBrands[0]) await new Promise(resolve => setTimeout(resolve, 1050));
    const query = `${brand.query}, ${city}, ${state}`;
    let matches = [];
    try {
      matches = await providerFetch(`${NOMINATIM_ROOT}/search?q=${encodeURIComponent(query)}&format=jsonv2&limit=4&addressdetails=1&extratags=1`, {
        headers: { "user-agent": "LookWeave/1.0 (inclusive fashion discovery)" }
      }, 12_000);
    } catch (error) {
      if (!results.length && brand === nearbyBrands.at(-1)) throw error;
      continue;
    }
    for (const item of Array.isArray(matches) ? matches : []) {
      const lat = Number(item.lat), lon = Number(item.lon);
      if (!Number.isFinite(lat) || !Number.isFinite(lon)) continue;
      const distance = distanceMiles(latitude, longitude, lat, lon);
      if (distance > 40) continue;
      results.push({
        id: `${item.osm_type || "place"}-${item.osm_id || `${lat}-${lon}`}`,
        retailerId: brand.query.toLowerCase().replace(/[^a-z0-9]+/g, "-"),
        name: String(item.name || brand.query),
        neighborhood: String(item.address?.neighbourhood || item.address?.suburb || item.address?.city_district || city),
        address: shortAddress(item),
        distanceMiles: Math.round(distance * 10) / 10,
        latitude: lat,
        longitude: lon,
        match: brand.match,
        accessibility: ["yes", "no", "limited"].includes(item.extratags?.wheelchair) ? item.extratags.wheelchair : "unknown",
        reason: brand.reason,
        features: brand.features,
        suggestedPieces: brand.suggestedPieces || brand.features,
        directionsUrl: `https://www.google.com/maps/dir/?api=1&destination=${encodeURIComponent(`${lat},${lon}`)}`,
        color: brand.color
      });
    }
  }
  const seen = new Set();
  const places = results.sort((a, b) => a.distanceMiles - b.distanceMiles || b.match - a.match).filter(item => !seen.has(item.id) && seen.add(item.id)).slice(0, 18);
  const value = { postalCode: zip, area: [city, state].filter(Boolean).join(", "), latitude, longitude, places, attribution: "Map and place data © OpenStreetMap contributors", privacy: "ZIP used for this search only; not stored in shared state." };
  nearbyCache.set(zip, { expiresAt: Date.now() + 12 * 60 * 60_000, value });
  return value;
}

const AGENT_RESPONSE_SCHEMA = {
  type: "object",
  additionalProperties: false,
  properties: {
    message: { type: "string" },
    proposedToolCalls: {
      type: "array",
      items: {
        type: "object",
        additionalProperties: false,
        properties: {
          name: { type: "string", enum: ["search_wardrobe", "get_user_location", "get_day_signals", "solve_outfit", "explain_plan", "start_try_on", "record_user_confirmation", "draft_rule", "create_rule", "edit_rule", "delete_rule", "retrieve_rules", "apply_rules", "research_products", "add_calendar_event", "update_calendar_event", "add_purchase", "update_price_limit", "link_outfit", "log_observation", "suggest_outfit_swap", "update_profile_prefs", "update_budget_note"] },
          arguments: {
            type: "object",
            additionalProperties: false,
            properties: {
              occasion: { type: "string" },
              query: { type: "string" },
              ruleText: { type: "string" },
              ruleId: { type: "string" },
              title: { type: "string" },
              name: { type: "string" },
              amount: { type: "string" },
              date: { type: "string" },
              recurrence: { type: "string" },
              priceLimit: { type: "string" },
              garmentId: { type: "string" },
              task: { type: "string" },
              difficulty: { type: "string" },
              note: { type: "string" },
              eventId: { type: "string" },
              gender: { type: "string" },
              postalCode: { type: "string" },
              colors: { type: "string" },
              styles: { type: "string" },
              interests: { type: "string" },
              budgetNote: { type: "string" }
            },
            required: ["occasion", "query", "ruleText", "ruleId", "title", "name", "amount", "date", "recurrence", "priceLimit", "garmentId", "task", "difficulty", "note", "eventId", "gender", "postalCode", "colors", "styles", "interests", "budgetNote"]
          }
        },
        required: ["name", "arguments"]
      }
    }
  },
  required: ["message", "proposedToolCalls"]
};
const GEMINI_RESPONSE_SCHEMA = JSON.parse(JSON.stringify(AGENT_RESPONSE_SCHEMA, (key, value) => key === "additionalProperties" ? undefined : value));

function agentPrompt(payload) {
  const message = String(payload.message || "").trim();
  if (!message) throw new HttpError(400, "A message is required");
  return JSON.stringify({
    user_message: message,
    confirmed_constraints: Array.isArray(payload.hardConstraintIds) ? payload.hardConstraintIds : [],
    garment_evidence: Array.isArray(payload.garmentEvidence) ? payload.garmentEvidence : [],
    day_context_notes: payload.dayContextNotes || null,
    price_limit: payload.priceLimit ?? null,
    budget_remaining: payload.budgetRemaining ?? null,
    events: Array.isArray(payload.events) ? payload.events : [],
    preferences: payload.preferences || {},
    style: payload.style || {},
    sizes: payload.sizes || {},
    gender: payload.gender || payload.preferences?.gender || null,
    wardrobe: Array.isArray(payload.wardrobe) ? payload.wardrobe : [],
    accessories_preferences: payload.accessoriesPreferences || {},
    discovery_likes: Array.isArray(payload.discoveryLikes) ? payload.discoveryLikes : [],
    fit_observations: Array.isArray(payload.fitObservations) ? payload.fitObservations : [],
    active_filters: Array.isArray(payload.activeFilters) ? payload.activeFilters : [],
    custom_rules: Array.isArray(payload.customRules) ? payload.customRules : [],
    purchases: Array.isArray(payload.purchases) ? payload.purchases : [],
    planned_outfits: Array.isArray(payload.plannedOutfits) ? payload.plannedOutfits : []
  });
}

function parseAgentResponse(text, provider) {
  const cleaned = String(text || "").trim().replace(/^```(?:json)?\s*/i, "").replace(/\s*```$/, "");
  if (!cleaned) throw new HttpError(502, `${provider} returned no usable response`);
  let parsed;
  try { parsed = JSON.parse(cleaned); } catch { throw new HttpError(502, `${provider} returned invalid structured output`); }
  if (typeof parsed.message !== "string" || !Array.isArray(parsed.proposedToolCalls)) {
    throw new HttpError(502, `${provider} response did not match the agent contract`);
  }
  parsed.message = cleanAgentMessage(parsed.message);
  return parsed;
}

function cleanAgentMessage(value) {
  let message = String(value || "").trim()
    .replace(/^```(?:json|markdown|text)?\s*/i, "")
    .replace(/\s*```$/i, "")
    .trim();
  // Some providers put a second serialized response inside the message field.
  if (message.startsWith("{") && message.endsWith("}")) {
    try {
      const nested = JSON.parse(message);
      if (typeof nested.message === "string") message = nested.message.trim();
    } catch {}
  }
  return message
    .replace(/\[\s*(?:solve|search|research|browse|location|weather|signals|profile|calendar|rule)\s*\]/gi, "")
    .replace(/\b(?:I['’]?ll|I will|I can)\s+(?:use|call|invoke)\s+(?:the\s+)?[a-z_]+\s+tool(?:\s+for\s+this|\s+to\s+do\s+this)?\.?/gi, "")
    .replace(/\b(?:the\s+)?[a-z_]+\s+tool\s+(?:for\s+this|to\s+do\s+this)\.?/gi, "")
    .replace(/\[\s*proposeToolCall\s*\][\s\S]*?\[\s*\/\s*proposeToolCall\s*\]/gi, "")
    .replace(/\[\s*\/?(?:tool_call|function_call|tool_calls?)\s*\]/gi, "")
    .replace(/\{\s*["']tool_code["']\s*:\s*[\s\S]*?\}\s*/gi, "")
    .replace(/<tool_call>[\s\S]*?<\/tool_call>/gi, "")
    .replace(/<\/?(?:function_call|tool_calls?)>/gi, "")
    .replace(/(?:^|\n)\s*(?:tool[_ ]?calls?|function[_ ]?call|proposedToolCalls)\s*[:=][\s\S]*$/i, "")
    .replace(/(?:^|\n)\s*(?:search_wardrobe|research_products|solve_outfit|suggest_outfit_swap|draft_rule|retrieve_rules|update_profile_prefs|add_calendar_event|update_calendar_event|log_observation)\s*\([^\n]*\)\s*/gi, "")
    .replace(/```(?:json|markdown|text)?/gi, "")
    .replace(/^\s*(?:message|answer)\s*:\s*/i, "")
    .trim()
    .slice(0, 1200) || "I prepared the useful next step below.";
}

let chosenGeminiModel;
async function resolveGeminiModel() {
  if (process.env.GEMINI_MODEL) return process.env.GEMINI_MODEL.replace(/^models\//, "");
  if (chosenGeminiModel) return chosenGeminiModel;
  const key = process.env.GEMINI_API_KEY;
  if (!key) throw new HttpError(503, "Gemini is not configured");
  const catalog = await providerFetch(`${GEMINI_ROOT}/models`, { headers: { "x-goog-api-key": key } });
  const supported = (catalog.models || [])
    .filter(model => (model.supportedGenerationMethods || []).includes("generateContent"))
    .map(model => String(model.name || "").replace(/^models\//, ""));
  const preference = ["gemini-3.8-flash", "gemini-3.5-flash-lite", "gemini-3.5-flash", "gemini-2.5-flash-lite", "gemini-2.5-flash"];
  chosenGeminiModel = preference.find(name => supported.includes(name)) || supported.find(name => /flash/i.test(name));
  if (!chosenGeminiModel) throw new HttpError(503, "No compatible Gemini Flash model is available to this key");
  return chosenGeminiModel;
}

async function askGemini(payload) {
  const key = process.env.GEMINI_API_KEY;
  if (!key) throw new HttpError(503, "Gemini is not configured");
  const model = await resolveGeminiModel();
  const prompt = agentPrompt(payload);
  const body = await providerFetch(`${GEMINI_ROOT}/models/${encodeURIComponent(model)}:generateContent`, {
    method: "POST",
    headers: { "content-type": "application/json", "x-goog-api-key": key },
    body: JSON.stringify({
      system_instruction: { parts: [{ text: LOOKWEAVE_PROMPT }] },
      contents: [{ role: "user", parts: [{ text: prompt }] }],
      generationConfig: {
        temperature: 0.25,
        maxOutputTokens: 700,
        responseMimeType: "application/json",
        responseSchema: GEMINI_RESPONSE_SCHEMA
      }
    })
  }, 45_000);
  const text = body?.candidates?.[0]?.content?.parts?.map(part => part.text || "").join("").trim();
  const parsed = parseAgentResponse(text, "Gemini");
  return { ...parsed, model, provider: "gemini" };
}

function groqKeys() {
  return [process.env.GROQ_API_KEY, process.env.GROQ_API_KEY_2].filter(Boolean);
}

let chosenGroqModel;
async function resolveGroqModel(key) {
  if (process.env.GROQ_MODEL) return process.env.GROQ_MODEL;
  if (chosenGroqModel) return chosenGroqModel;
  const catalog = await providerFetch(`${GROQ_ROOT}/models`, { headers: { authorization: `Bearer ${key}` } });
  const available = (catalog.data || []).map(model => model.id);
  const preference = ["qwen/qwen3.8-27b", "openai/gpt-oss-20b", "openai/gpt-oss-120b"];
  chosenGroqModel = preference.find(name => available.includes(name));
  if (!chosenGroqModel) throw new HttpError(503, "No compatible Groq structured-output model is available to this key");
  return chosenGroqModel;
}

function reserveGroqRequest() {
  const now = Date.now();
  while (groqRequestTimes.length && groqRequestTimes[0] < now - 60_000) groqRequestTimes.shift();
  const limit = Math.max(1, Number(process.env.GROQ_LOCAL_RPM_LIMIT || 20));
  if (groqRequestTimes.length >= limit) throw new HttpError(429, "The backup agent is resting briefly. Try again in a minute.");
  groqRequestTimes.push(now);
}

async function askGroq(payload) {
  const keys = groqKeys();
  if (!keys.length) throw new HttpError(503, "Groq is not configured");
  if (Date.now() < groqCooldownUntil) throw new HttpError(429, "The backup agent is resting briefly. Try again later.", null, Math.ceil((groqCooldownUntil - Date.now()) / 1000));
  reserveGroqRequest();
  let lastError;
  for (let attempt = 0; attempt < keys.length; attempt++) {
    const index = (groqNextKey + attempt) % keys.length;
    if ((groqKeyCooldowns.get(index) || 0) > Date.now()) continue;
    const key = keys[index];
    try {
      const model = await resolveGroqModel(key);
      const body = await providerFetch(`${GROQ_ROOT}/chat/completions`, {
    method: "POST",
    headers: { authorization: `Bearer ${key}`, "content-type": "application/json" },
    body: JSON.stringify({
      model,
      temperature: 0.25,
      max_completion_tokens: 700,
      messages: [
        { role: "system", content: `${LOOKWEAVE_PROMPT} Respond as JSON with a string message and a proposedToolCalls array. Each call needs a name and an arguments object whose occasion, query, ruleText, and ruleId fields are strings. Use an empty array when no tool is needed.` },
        { role: "user", content: agentPrompt(payload) }
      ],
      response_format: { type: "json_object" }
    })
      }, 45_000);
      const parsed = parseAgentResponse(body?.choices?.[0]?.message?.content?.trim(), "Groq");
      groqNextKey = (index + 1) % keys.length;
      return { ...parsed, model, provider: "groq" };
    } catch (error) {
      lastError = error;
      if (error instanceof HttpError && error.status === 429) {
        groqCooldownUntil = Date.now() + Math.max(60, error.retryAfterSeconds || 60) * 1000;
        throw error;
      }
      if (error instanceof HttpError && [401, 403].includes(error.status)) groqKeyCooldowns.set(index, Date.now() + 60 * 60_000);
      else if (error instanceof HttpError && error.status >= 500) groqKeyCooldowns.set(index, Date.now() + 30_000);
      else throw error;
    }
  }
  throw lastError || new HttpError(503, "Groq credentials are temporarily unavailable");
}

async function askAgent(payload) {
  agentPrompt(payload);
  const memory = await readSharedState();
  const style = await styleService.view();
  payload = { ...payload, garmentEvidence: [...(payload.garmentEvidence || []), { garmentId: "style-profile", facts: { style: JSON.stringify(style.recommendations) }, sources: { style: "User preferences and optional photo estimates. Styling suggestions, not fit measurements." } }] };
  payload = { ...payload, garmentEvidence: [ ...(payload.garmentEvidence || []), { garmentId: "confirmed-observations", facts: { observations: JSON.stringify(memory.observations), preferences: JSON.stringify(memory.preferences) }, sources: { observations: "User-confirmed Wearability Lab records" } } ] };
  payload = { ...payload, garmentEvidence: [ ...(payload.garmentEvidence || []), { garmentId: "product-feedback", facts: { likedProductIds: JSON.stringify(memory.likedProductIds || []), passedProductIds: JSON.stringify(memory.passedProductIds || []) }, sources: { feedback: "User-confirmed product likes and passes" } } ] };
  if (process.env.GEMINI_API_KEY && Date.now() >= geminiCooldownUntil) {
    try {
      const response = enforceIntentTools(payload, await askGemini(payload));
      return { ...response, executedActions: await executeSafeTools(response.proposedToolCalls, payload) };
    } catch (error) {
      const status = error instanceof HttpError ? error.status : 500;
      if (!(error instanceof HttpError) || status === 401 || status === 403 || !groqKeys().length) throw error;
      geminiCooldownUntil = Date.now() + Math.max(5, error.retryAfterSeconds || 10) * 1000;
      console.warn(`Gemini unavailable; using Groq fallback for this request (${status}).`);
    }
  }
  const response = enforceIntentTools(payload, await askGroq(payload));
  return { ...response, executedActions: await executeSafeTools(response.proposedToolCalls, payload) };
}

function enforceIntentTools(payload, response) {
  const message = String(payload?.message || "").trim();
  const lower = message.toLowerCase();
  let calls = Array.isArray(response.proposedToolCalls) ? [...response.proposedToolCalls] : [];
  const browseRequested = /\b(browse|search online|shop|buy|find (?:me |an? )?(?:product|item|alternative)|online stores?)\b/i.test(message);
  const browseNegated = /\b(?:do not|don't|dont|without|no need to|not asking (?:you )?to)\s+(?:browse|search|shop|buy)\b/i.test(message);
  const explicitBrowse = browseRequested && !browseNegated;
  const asksOwned = /\b(what|which|show|list|do i have|already own)\b.*\b(own|owned|wardrobe|closet|have)\b/i.test(message) || /\bwhat do i (?:own|have)\b/i.test(message);
  const asksLook = /\b(outfit|look|wear|occasion|interview|dinner|wedding|presentation)\b/i.test(message);
  // Product research is never inferred from a wardrobe/gap question.
  if (!explicitBrowse) calls = calls.filter(call => call?.name !== "research_products");
  if (explicitBrowse && !calls.some(call => call?.name === "research_products")) {
    calls.push({ name: "research_products", arguments: { query: message } });
  }
  if (asksOwned && !calls.some(call => call?.name === "search_wardrobe")) {
    calls.unshift({ name: "search_wardrobe", arguments: { query: message } });
  }
  const asksSignals = /\b(weather|forecast|temperature|rain|wind|uv|air quality|pollen|humidity|outside)\b/i.test(message);
  if (asksSignals && !calls.some(call => call?.name === "get_day_signals")) {
    calls.unshift({ name: "get_day_signals", arguments: {} });
  }
  if (asksLook && !asksOwned && !explicitBrowse && !calls.some(call => ["solve_outfit", "suggest_outfit_swap"].includes(call?.name))) {
    calls.unshift({ name: "solve_outfit", arguments: { occasion: message.slice(0, 80) } });
  }
  return { ...response, proposedToolCalls: calls.slice(0, 4), message: cleanAgentMessage(response.message) };
}

async function executeSafeTools(toolCalls = [], payload = {}) {
  const actions = [];
  for (const call of toolCalls.slice(0, 4)) {
    const name = String(call.name || "");
    const args = call.arguments || {};
    try {
      if (name === "search_wardrobe") {
        const wardrobe = Array.isArray(payload.wardrobe) ? payload.wardrobe : [];
        actions.push({ tool: name, status: "complete", title: "Checked what you own", detail: `${wardrobe.length} wardrobe item${wardrobe.length === 1 ? "" : "s"} were searched. No external products were mixed in.` });
      } else if (name === "get_user_location") {
        const zip = String(payload.postalCode || payload.preferences?.postalCode || "");
        if (!/^\d{5}$/.test(zip)) {
          actions.push({ tool: name, status: "needs_input", title: "ZIP code needed", detail: "Add a private five-digit ZIP code to get location-aware context." });
        } else {
          const nearby = await getNearbyPlaces(zip);
          actions.push({ tool: name, status: "complete", title: "Resolved your general area", detail: `${nearby.area || "Your ZIP area"} · exact address not stored.` });
        }
      } else if (name === "get_day_signals") {
        const zip = String(payload.postalCode || payload.preferences?.postalCode || "");
        if (!/^\d{5}$/.test(zip)) {
          actions.push({ tool: name, status: "needs_input", title: "ZIP code needed", detail: "Add a private five-digit ZIP code before checking local weather and comfort signals." });
        } else {
          // Use the ZIP directly for the forecast lookup; this keeps weather
          // independent from optional nearby-place providers.
          const day = await getDayContext(zip);
          const signalLabels = [["apparentTemperatureF", "Feels like"], ["uvIndex", "UV"], ["windGustMph", "Wind gusts"], ["usAqi", "Air quality"]];
          const highlights = signalLabels
            .map(([key, label]) => day.signals?.[key] == null ? null : `${label}: ${day.signals[key]}`)
            .filter(Boolean).slice(0, 3).join(" · ");
          actions.push({ tool: name, status: "complete", title: "Checked live day signals", detail: `${day.location || "Your area"}${highlights ? ` · ${highlights}` : ""}` });
        }
      } else if (name === "retrieve_rules") {
        const rules = (await readSharedState()).preferences.customRules || [];
        actions.push({ tool: name, status: "complete", title: "Checked your confirmed rules", detail: `${rules.length} confirmed rule${rules.length === 1 ? "" : "s"} were included.` });
      } else if (name === "draft_rule") {
        const rule = draftRuleFromText(args.ruleText || args.query);
        actions.push({ tool: name, status: "needs_confirmation", title: "Prepared a rule for your review", detail: `${rule.label} is ready to confirm. It has not been saved.` });
      } else if (name === "create_rule" || name === "edit_rule" || name === "delete_rule" || name === "apply_rules") {
        actions.push({ tool: name, status: "needs_confirmation", title: "Waiting for your confirmation", detail: "LookWeave will not change your rules from an AI suggestion alone." });
      } else if (name === "solve_outfit") {
        actions.push({ tool: name, status: "delegated", title: "Checking the whole look", detail: "The Android constraint solver is evaluating every garment against your confirmed rules." });
      } else if (name === "research_products") {
        const result = await researchProducts(args.query);
        actions.push({ tool: name, status: "complete", title: "Checked product sources", detail: `${result.sourceCount} source${result.sourceCount === 1 ? "" : "s"} found. Prices, stock and accessibility remain unknown unless explicitly stated.` });
      } else if (name === "add_calendar_event" || name === "update_calendar_event" || name === "add_purchase" || name === "update_price_limit" || name === "link_outfit" || name === "log_observation" || name === "suggest_outfit_swap" || name === "update_profile_prefs" || name === "update_budget_note") {
        actions.push({ tool: name, status: "needs_confirmation", title: "Waiting for your confirmation", detail: "This Day Hub or profile change stays a draft until you confirm it in the app." });
      } else {
        actions.push({ tool: name, status: "proposal_only", title: "Prepared a possible next step", detail: "This action still needs an explicit screen action before it can change anything." });
      }
    } catch (error) {
      actions.push({ tool: name, status: "blocked", title: "Could not complete this step", detail: error instanceof HttpError ? error.message : "The source was unavailable; no claim was added." });
    }
  }
  return actions;
}

const elevenSlots = ["", "_2", "_3", "_4"].map(suffix => ({
  key: process.env[`ELEVENLABS_API_KEY${suffix}`] || "",
  agentId: process.env[`ELEVENLABS_AGENT_ID${suffix}`] || "",
  promptConfigured: false,
  cooldownUntil: 0,
  quotaCheckedUntil: 0
})).filter(slot => slot.key);
const VOICE_PROMPT = `You are Loomie, a friendly personal fashion companion. Speak in short, natural sentences. Never say internal function names, code, tool names, JSON, bracketed instructions, or implementation details. The app handles wardrobe checks, outfit decisions, and evidence on screen. When someone asks for an outfit, acknowledge the occasion briefly and let the app present the checked look; do not ask permission to check the wardrobe or invent garments. For general style questions, answer directly. Never infer a diagnosis or claim a visual preview proves physical comfort.`;
async function configureVoicePrompt(slot) {
  if (!slot.promptConfigured) {
    await providerFetch(`${ELEVEN_ROOT}/convai/agents/${encodeURIComponent(slot.agentId)}`, {
      method: "PATCH",
      headers: { "content-type": "application/json", "xi-api-key": slot.key },
      body: JSON.stringify({ conversation_config: { agent: { prompt: { prompt: VOICE_PROMPT } } } })
    });
    slot.promptConfigured = true;
  }
  return slot.agentId;
}
async function ensureElevenAgent(slot, isPrimary) {
  if (!slot.agentId && isPrimary) {
    try {
      const saved = JSON.parse(await readFile(statePath, "utf8"));
      if (saved.agentId) slot.agentId = saved.agentId;
    } catch {}
  }
  if (slot.agentId) {
    try { return await configureVoicePrompt(slot); }
    catch (error) {
      if (!(error instanceof HttpError) || error.status !== 404) throw error;
      slot.agentId = "";
    }
  }
  const created = await providerFetch(`${ELEVEN_ROOT}/convai/agents/create`, {
    method: "POST",
    headers: { "content-type": "application/json", "xi-api-key": slot.key },
    body: JSON.stringify({
      name: "LookWeave Voice",
      tags: ["lookweave", "adaptive-fashion"],
      conversation_config: {
        agent: {
          first_message: "Hi, I’m LookWeave. Tell me what you want your outfit to do differently today.",
          language: "en",
          prompt: { prompt: VOICE_PROMPT, llm: "gemini-2.5-flash", temperature: 0.25 }
        },
        tts: { voice_id: process.env.ELEVENLABS_VOICE_ID || "21m00Tcm4TlvDq8ikWAM" },
        conversation: { text_only: false, max_duration_seconds: 600 }
      }
    })
  }, 45_000);
  if (!created.agent_id) throw new HttpError(502, "ElevenLabs did not return an agent ID");
  slot.agentId = created.agent_id;
  slot.promptConfigured = true;
  // Vercel functions have a read-only deployment filesystem. Keep the ID in
  // the warm instance when hosted; local development can still persist it.
  if (isPrimary && !process.env.VERCEL) {
    await mkdir(dirname(statePath), { recursive: true });
    await writeFile(statePath, JSON.stringify({ agentId: slot.agentId }, null, 2));
  }
  return slot.agentId;
}

async function checkElevenQuota(slot) {
  if (slot.quotaCheckedUntil > Date.now()) return;
  try {
    const subscription = await providerFetch(`${ELEVEN_ROOT}/user/subscription`, { headers: { "xi-api-key": slot.key } }, 12_000);
    const used = Number(subscription.character_count);
    const limit = Number(subscription.character_limit);
    const canExtend = subscription.max_credit_limit_extension === "unlimited" || Number(subscription.max_credit_limit_extension) > 0 || (subscription.max_credit_limit_extension == null && subscription.can_extend_character_limit === true);
    if (Number.isFinite(used) && Number.isFinite(limit) && limit > 0 && used >= limit && !canExtend) {
      const reset = Number(subscription.next_character_count_reset_unix) * 1000;
      slot.cooldownUntil = Number.isFinite(reset) && reset > Date.now() ? reset : Date.now() + 60 * 60_000;
      throw new HttpError(402, "Voice credits are exhausted for this account");
    }
    slot.quotaCheckedUntil = Date.now() + 5 * 60_000;
  } catch (error) {
    if (error instanceof HttpError && error.status === 403) return; // Restricted API keys may lack subscription scope.
    throw error;
  }
}

async function createVoiceSession() {
  if (!elevenSlots.length) throw new HttpError(503, "ElevenLabs is not configured");
  let lastError;
  for (const [index, slot] of elevenSlots.entries()) {
    if (slot.cooldownUntil > Date.now()) continue;
    try {
      await checkElevenQuota(slot);
      const agentId = await ensureElevenAgent(slot, index === 0);
      const response = await providerFetch(`${ELEVEN_ROOT}/convai/conversation/token?agent_id=${encodeURIComponent(agentId)}&participant_name=lookweave-user`, {
        headers: { "xi-api-key": slot.key }
      });
      if (!response.token) throw new HttpError(502, "ElevenLabs did not return a conversation token");
      return { token: response.token, conversationId: response.conversation_id || null, agentId, expiresInSeconds: 600 };
    } catch (error) {
      lastError = error;
      const status = error instanceof HttpError ? error.status : 502;
      if (status === 429) slot.cooldownUntil = Date.now() + Math.max(60, error.retryAfterSeconds || 60) * 1000;
      else if ([401, 402, 403].includes(status)) slot.cooldownUntil = Math.max(slot.cooldownUntil, Date.now() + 60 * 60_000);
      else slot.cooldownUntil = Date.now() + 30_000;
      console.warn(`ElevenLabs voice slot ${index + 1} unavailable (${status}); checking next configured slot.`);
    }
  }
  throw lastError || new HttpError(503, "Voice service is temporarily unavailable");
}

function decodeImage(input, label) {
  const mimeType = String(input?.mimeType || "").toLowerCase();
  if (!new Set(["image/jpeg", "image/jpg", "image/png"]).has(mimeType)) throw new HttpError(400, `${label} must be JPEG or PNG`);
  let bytes;
  try { bytes = Buffer.from(String(input?.base64 || ""), "base64"); } catch { throw new HttpError(400, `${label} is not valid base64`); }
  if (!bytes.length || bytes.length > 10 * 1024 * 1024) throw new HttpError(400, `${label} must be between 1 byte and 10 MB`);
  return { bytes, mimeType: mimeType === "image/jpg" ? "image/jpeg" : mimeType };
}

async function uploadYouCamImage(image, name) {
  const key = process.env.YOUCAM_API_KEY;
  const ext = image.mimeType === "image/png" ? "png" : "jpg";
  const registration = await providerFetch(`${YOUCAM_ROOT}/file`, {
    method: "POST",
    headers: { authorization: `Bearer ${key}`, "content-type": "application/json" },
    body: JSON.stringify({ files: [{ content_type: image.mimeType, file_name: `${name}.${ext}`, file_size: image.bytes.length }] })
  });
  const file = registration?.data?.files?.[0];
  const request = file?.requests?.find(item => String(item.method).toUpperCase() === "PUT") || file?.requests?.[0];
  if (!file?.file_id || !request?.url) throw new HttpError(502, "YouCam file registration response was incomplete");
  const headers = new Headers(request.headers || {});
  if (!headers.has("content-type")) headers.set("content-type", image.mimeType);
  if (!headers.has("content-length")) headers.set("content-length", String(image.bytes.length));
  const upload = await fetch(request.url, { method: "PUT", headers, body: image.bytes, signal: AbortSignal.timeout(60_000) });
  if (!upload.ok) {
    const detail = await upload.text();
    const code = detail.match(/<Code>([^<]+)<\/Code>/)?.[1] || "upload_rejected";
    const message = detail.match(/<Message>([^<]+)<\/Message>/)?.[1] || "";
    console.error(`YouCam upload: ${code}: ${message}`);
    throw new HttpError(502, `YouCam upload failed with ${upload.status} (${code})`);
  }
  return file.file_id;
}

async function createTryOn(payload) {
  const key = process.env.YOUCAM_API_KEY;
  if (!key) throw new HttpError(503, "YouCam is not configured");
  const person = decodeImage(payload.person, "Person image");
  const garment = decodeImage(payload.garment, "Garment image");
  const categories = new Set(["full_body", "lower_body", "upper_body", "shoes", "auto", "outer"]);
  const category = categories.has(payload.category) ? payload.category : "auto";
  await loadTryOnCache();
  const fingerprint = createHash("sha256").update(person.bytes).update(garment.bytes).update(category).digest("hex");
  const cached = tryOnCache.get(fingerprint);
  if (cached) return { ...cached, cached: true };
  const today = new Date().toISOString().slice(0, 10);
  if (today !== tryOnDay) { tryOnDay = today; tryOnCountToday = 0; }
  if (tryOnInFlight) throw new HttpError(409, "A virtual try-on is already processing");
  tryOnInFlight = true;
  try {
  const [srcFileId, refFileId] = await Promise.all([
    uploadYouCamImage(person, "lookweave-person"),
    uploadYouCamImage(garment, "lookweave-garment")
  ]);
  const task = await providerFetch(`${YOUCAM_ROOT}/task/cloth-v4`, {
    method: "POST",
    headers: { authorization: `Bearer ${key}`, "content-type": "application/json" },
    body: JSON.stringify({
      src_file_id: srcFileId,
      ref_file_id: refFileId,
      garment_category: category,
      change_shoes: category === "full_body" || category === "lower_body",
      filter_multi_person: "strict"
    })
  });
  const taskId = task?.data?.task_id;
  if (!taskId) throw new HttpError(502, "YouCam did not return a task ID");
  tryOnCountToday += 1;
  lastTryOnStartedAt = Date.now();
  for (let attempt = 0; attempt < 18; attempt++) {
    if (attempt) await new Promise(resolve => setTimeout(resolve, 10_000));
    const status = await providerFetch(`${YOUCAM_ROOT}/task/cloth-v4/${encodeURIComponent(taskId)}`, {
      headers: { authorization: `Bearer ${key}`, "content-type": "application/json" }
    });
    const state = status?.data?.task_status;
    if (state === "success") {
      const imageUrl = status?.data?.results?.url;
      if (!imageUrl) throw new HttpError(502, "YouCam completed without a result URL");
      const result = { taskId, imageUrl };
      tryOnCache.set(fingerprint, result);
      await persistTryOnCache();
      return result;
    }
    if (state === "error") {
      const code = status?.data?.error?.code || status?.data?.error || "processing_failed";
      throw new HttpError(422, `YouCam could not create this preview: ${code}`);
    }
  }
  throw new HttpError(504, "YouCam preview timed out; try again with clearer images");
  } finally {
    tryOnInFlight = false;
  }
}

// Jewellery/watches use YouCam's dedicated 2D VTO models.  A close capture is
// deliberately required: it is more reliable than inventing a hand/neck from a
// full-body photo and gives the user control of the source image.
async function createAccessoryTryOn(payload) {
  const key = process.env.YOUCAM_API_KEY;
  if (!key) throw new HttpError(503, "YouCam is not configured");
  const kind = String(payload.kind || "").toLowerCase();
  const supported = new Set(["ring", "bracelet", "watch", "earring", "necklace"]);
  if (!supported.has(kind)) throw new HttpError(400, "Choose ring, bracelet, watch, earring, or necklace");
  const target = decodeImage(payload.target, kind === "watch" || kind === "ring" || kind === "bracelet" ? "Hand or wrist photo" : "Selfie");
  const product = decodeImage(payload.product, "Accessory product image");
  await loadTryOnCache();
  const fingerprint = createHash("sha256").update("accessory:").update(kind).update(target.bytes).update(product.bytes).digest("hex");
  const cached = tryOnCache.get(fingerprint);
  if (cached) return { ...cached, cached: true };
  if (tryOnInFlight) throw new HttpError(409, "A virtual try-on is already processing");
  tryOnInFlight = true;
  try {
    const [srcFileId, refFileId] = await Promise.all([
      uploadYouCamImage(target, `lookweave-${kind}-target`),
      uploadYouCamImage(product, `lookweave-${kind}-product`)
    ]);
    const endpoint = `${YOUCAM_ROOT}/task/2d-vto/${kind}`;
    const task = await providerFetch(endpoint, {
      method: "POST",
      headers: { authorization: `Bearer ${key}`, "content-type": "application/json" },
      body: JSON.stringify({ src_file_id: srcFileId, ref_file_ids: [refFileId] })
    });
    const taskId = task?.data?.task_id;
    if (!taskId) throw new HttpError(502, "YouCam did not return an accessory try-on task ID");
    for (let attempt = 0; attempt < 18; attempt++) {
      if (attempt) await new Promise(resolve => setTimeout(resolve, 10_000));
      const status = await providerFetch(`${endpoint}/${encodeURIComponent(taskId)}`, {
        headers: { authorization: `Bearer ${key}`, "content-type": "application/json" }
      });
      if (status?.data?.task_status === "success") {
        const imageUrl = status?.data?.results?.url;
        if (!imageUrl) throw new HttpError(502, "YouCam completed without an accessory preview");
        const result = { taskId, imageUrl };
        tryOnCache.set(fingerprint, result);
        await persistTryOnCache();
        return result;
      }
      if (status?.data?.task_status === "error") {
        throw new HttpError(422, `YouCam could not create this accessory preview: ${status?.data?.error?.code || status?.data?.error || "processing_failed"}`);
      }
    }
    throw new HttpError(504, "Accessory preview timed out; use a clearer close-up and product image");
  } finally {
    tryOnInFlight = false;
  }
}

function numberOrNull(value) {
  return value !== null && value !== undefined && value !== "" && Number.isFinite(Number(value)) ? Number(value) : null;
}

async function getDayContext(place, departure = "00:00", returnTime = "23:00") {
  const query = String(place || "").trim();
  if (query.length < 2 || query.length > 120) throw new HttpError(400, "Enter a city or postal code");
  const cacheKey = `${query.toLocaleLowerCase("en-US")}|${departure}|${returnTime}`;
  const cached = dayContextCache.get(cacheKey);
  if (cached && cached.expiresAt > Date.now()) return cached.value;

  const geocoding = await providerFetch(`${GEOCODING_ROOT}/search?name=${encodeURIComponent(query)}&count=1&language=en&format=json`);
  const location = geocoding?.results?.[0];
  if (!location) throw new HttpError(404, "That place could not be found");
  const coordinates = `latitude=${encodeURIComponent(location.latitude)}&longitude=${encodeURIComponent(location.longitude)}`;
  // Weather is useful even when the optional air-quality/pollen provider is
  // rate-limited or temporarily unavailable. Keep the core decision path
  // available and mark unavailable signals as unknown instead of failing the
  // entire agent action.
  const [weatherResult, airResult] = await Promise.allSettled([
    providerFetch(`${WEATHER_ROOT}/forecast?${coordinates}&current=apparent_temperature,relative_humidity_2m,precipitation,wind_gusts_10m&hourly=temperature_2m,apparent_temperature,precipitation_probability,wind_gusts_10m,uv_index&daily=uv_index_max,precipitation_probability_max,temperature_2m_max,temperature_2m_min&temperature_unit=fahrenheit&wind_speed_unit=mph&timezone=auto&forecast_days=2`),
    providerFetch(`${AIR_QUALITY_ROOT}/air-quality?${coordinates}&current=us_aqi,pm2_5,uv_index,alder_pollen,birch_pollen,grass_pollen,mugwort_pollen,olive_pollen,ragweed_pollen&hourly=us_aqi,pm2_5&timezone=auto&forecast_days=2`)
  ]);
  if (weatherResult.status === "rejected") throw weatherResult.reason;
  const weather = weatherResult.value || {};
  const air = airResult.status === "fulfilled" ? (airResult.value || {}) : {};
  const current = weather.current || {};
  const daily = weather.daily || {};
  const airNow = air.current || {};
  const pollenValues = ["alder_pollen", "birch_pollen", "grass_pollen", "mugwort_pollen", "olive_pollen", "ragweed_pollen"]
    .map(key => numberOrNull(airNow[key])).filter(value => value !== null);
  const signals = {
    apparentTemperatureF: numberOrNull(current.apparent_temperature),
    temperatureHighF: numberOrNull(daily.temperature_2m_max?.[0]),
    temperatureLowF: numberOrNull(daily.temperature_2m_min?.[0]),
    humidityPercent: numberOrNull(current.relative_humidity_2m),
    precipitationProbabilityPercent: numberOrNull(daily.precipitation_probability_max?.[0]),
    precipitationNowInches: numberOrNull(current.precipitation),
    windGustMph: numberOrNull(current.wind_gusts_10m),
    uvIndex: numberOrNull(daily.uv_index_max?.[0] ?? airNow.uv_index),
    usAqi: numberOrNull(airNow.us_aqi),
    pm25: numberOrNull(airNow.pm2_5),
    maxPollen: pollenValues.length ? Math.max(...pollenValues) : null
  };
  const hourlyWeather = weather.hourly || {};
  const hourlyAir = air.hourly || {};
  const hourly = (hourlyWeather.time || []).slice(0, 48).map((time, index) => ({
    time,
    temperatureF: numberOrNull(hourlyWeather.temperature_2m?.[index]),
    precipitationPercent: numberOrNull(hourlyWeather.precipitation_probability?.[index]),
    windGustMph: numberOrNull(hourlyWeather.wind_gusts_10m?.[index]),
    uvIndex: numberOrNull(hourlyWeather.uv_index?.[index]),
    usAqi: numberOrNull(hourlyAir.us_aqi?.[index])
  }));
  const parseHour = value => { const match = String(value).match(/^(\d{1,2})(?::\d{2})?$/); return match ? Math.max(0, Math.min(23, Number(match[1]))) : null; };
  const startHour = parseHour(departure) ?? 0;
  const endHour = parseHour(returnTime) ?? 23;
  const selectedHourly = hourly.filter(item => { const hour = Number(String(item.time).slice(11, 13)); return hour >= Math.min(startHour, endHour) && hour <= Math.max(startHour, endHour); });
  const recommendations = [];
  const temperatures = selectedHourly.map(item => item.temperatureF).filter(value => value !== null);
  if (temperatures.length && Math.max(...temperatures) - Math.min(...temperatures) >= 25) {
    recommendations.push({ id: "whole_day_layer", title: "Pack a removable layer", reason: "The forecast changes substantially across the day; a removable layer can cover the warm-to-cool transition.", action: "apply", constraint: "breathable base with an easy-removal layer" });
  }
  if ((signals.precipitationProbabilityPercent || 0) >= 45 || (signals.precipitationNowInches || 0) > 0) {
    recommendations.push({ id: "rain", title: "Weather-ready outer layer", reason: "Rain is likely enough to affect fabric choice and traction.", action: "apply", constraint: "water-resistant layer and stable-traction footwear" });
  }
  if ((signals.windGustMph || 0) >= 25) {
    recommendations.push({ id: "wind", title: "Secure loose layers", reason: "Stronger gusts can make open fronts and loose accessories harder to manage.", action: "apply", constraint: "secure closures and no loose scarf" });
  }
  if ((signals.apparentTemperatureF || 65) <= 45) {
    recommendations.push({ id: "cold", title: "Easy on-and-off warmth", reason: "The apparent temperature supports a removable layer without adding difficult closures.", action: "apply", constraint: "warm one-hand-friendly layer" });
  } else if ((signals.apparentTemperatureF || 65) >= 80 || ((signals.humidityPercent || 0) >= 70 && (signals.apparentTemperatureF || 0) >= 72)) {
    recommendations.push({ id: "heat", title: "Reduce heat and friction", reason: "Apparent temperature and humidity favor breathable, low-friction layers.", action: "apply", constraint: "breathable lightweight fabric" });
  }
  if ((signals.uvIndex || 0) >= 6) {
    recommendations.push({ id: "uv", title: "Add portable sun coverage", reason: "The day’s peak UV is high enough for coverage to matter outdoors.", action: "apply", constraint: "lightweight sun coverage" });
  }
  if ((signals.usAqi || 0) > 100) {
    recommendations.push({ id: "air", title: "Air-quality check-in", reason: "Air quality is elevated. Apply a clothing adjustment only if you confirm this is relevant to you.", action: "confirm", constraint: "mask-compatible, washable outer layer" });
  }
  if ((signals.maxPollen || 0) >= 10) {
    recommendations.push({ id: "pollen", title: "Pollen check-in", reason: "Pollen is elevated where coverage is available. This becomes a constraint only with your confirmation.", action: "confirm", constraint: "washable outer layer and hair coverage" });
  }
  if (!recommendations.length) {
    recommendations.push({ id: "steady", title: "No context-driven swap", reason: "Today’s external conditions do not justify changing the current look.", action: "none", constraint: "" });
  }
  const value = {
    location: [location.name, location.admin1, location.country_code].filter(Boolean).join(", "),
    timezone: weather.timezone,
    observedAt: current.time,
    signals,
    recommendations,
    hourly: selectedHourly,
    interval: { departure, returnTime },
    attribution: "Weather and air-quality data: Open-Meteo; air-quality forecast: CAMS ENSEMBLE."
  };
  dayContextCache.set(cacheKey, { expiresAt: Date.now() + 15 * 60_000, value });
  return value;
}

const styleService = createStyleService({
  path: process.env.STYLE_STATE_PATH || join(here, '.runtime', 'style-state.json'),
  root: YOUCAM_ROOT, key: () => process.env.YOUCAM_API_KEY,
  upload: uploadYouCamImage, decode: decodeImage, providerFetch, HttpError,
  preferences: async () => (await readSharedState()).preferences
});

const server = createServer(async (req, res) => {
  const url = new URL(req.url, `http://${req.headers.host || "localhost"}`);
  console.log(`[HTTP] ${req.method} ${url.pathname}`);
  if (req.method === "OPTIONS") return send(res, 204, {});
  try {
    if (req.method === "GET" && url.pathname === "/health") {
      return send(res, 200, {
        ok: true,
        services: {
          gemini: Boolean(process.env.GEMINI_API_KEY),
          groq: Boolean(groqKeys().length),
          agent: Boolean(process.env.GEMINI_API_KEY || groqKeys().length),
          elevenlabs: Boolean(elevenSlots.length),
          youcam: Boolean(process.env.YOUCAM_API_KEY),
          tavily: Boolean(process.env.TAVILY_API_KEY)
        }
      });
    }
    if (req.method === "GET" && url.pathname === "/v1/context") return send(res, 200, await getDayContext(url.searchParams.get("place"), url.searchParams.get("departure") || "00:00", url.searchParams.get("return") || "23:00"));
    if (req.method === "GET" && url.pathname === "/v1/nearby") return send(res, 200, await getNearbyPlaces(url.searchParams.get("postalCode")));
    if (req.method === "POST" && url.pathname === "/v1/products/research") return send(res, 200, await researchProducts((await readJson(req)).query));
    if (req.method === "POST" && url.pathname === "/v1/products/discover") return send(res, 200, await discoverProducts(await readJson(req)));
    if (req.method === "POST" && url.pathname === "/v1/style/accessories") return send(res, 200, await discoverAccessories(await readJson(req)));
    if (req.method === "POST" && url.pathname === "/v1/products/ingest") return send(res, 200, await ingestProductUrl((await readJson(req)).url));
    if (req.method === "GET" && url.pathname === "/v1/sync/state") return send(res, 200, await readSharedState());
    if (req.method === "GET" && url.pathname === "/v1/rules") return send(res, 200, { rules: (await readSharedState()).preferences.customRules || [] });
    if (req.method === "POST" && url.pathname === "/v1/rules/draft") return send(res, 200, { rule: draftRuleFromText((await readJson(req)).text) });
    if (req.method === "POST" && url.pathname === "/v1/rules") {
      const payload = await readJson(req);
      if (payload.confirmed !== true || payload.rule?.confirmed !== true) throw new HttpError(400, "Confirm this rule before saving it");
      const current = await readSharedState();
      const rules = [...(current.preferences.customRules || []), payload.rule];
      const updated = await updateSharedState({ preferences: { ...current.preferences, customRules: rules } });
      return send(res, 201, { rule: updated.preferences.customRules.at(-1), revision: updated.revision });
    }
    if (req.method === "PUT" && url.pathname.startsWith("/v1/rules/")) {
      const id = decodeURIComponent(url.pathname.split("/").at(-1));
      const payload = await readJson(req);
      if (payload.rule?.confirmed !== true) throw new HttpError(400, "Confirm this rule before saving it");
      const current = await readSharedState();
      const rules = (current.preferences.customRules || []).map(rule => rule.id === id ? { ...rule, ...payload.rule, id } : rule);
      if (!rules.some(rule => rule.id === id)) throw new HttpError(404, "Rule not found");
      const updated = await updateSharedState({ preferences: { ...current.preferences, customRules: rules } });
      return send(res, 200, { rule: updated.preferences.customRules.find(rule => rule.id === id), revision: updated.revision });
    }
    if (req.method === "DELETE" && url.pathname.startsWith("/v1/rules/")) {
      const id = decodeURIComponent(url.pathname.split("/").at(-1));
      const current = await readSharedState();
      const updated = await updateSharedState({ preferences: { ...current.preferences, customRules: (current.preferences.customRules || []).filter(rule => rule.id !== id) } });
      return send(res, 200, { rules: updated.preferences.customRules, revision: updated.revision });
    }
    if (req.method === "GET" && url.pathname === "/v1/style/profile") return send(res, 200, await styleService.view());
    if (req.method === "POST" && url.pathname === "/v1/style/profile") return send(res, 200, await styleService.save(await readJson(req)));
    if (req.method === "POST" && url.pathname === "/v1/style/delete") return send(res, 200, await styleService.clear());
    if (req.method === "POST" && url.pathname === "/v1/style/analyse") return send(res, 202, await styleService.start(await readJson(req)));
    if (req.method === "GET" && url.pathname.startsWith("/v1/style/jobs/")) return send(res, 200, await styleService.status(url.pathname.split('/').at(-1)));
    if (req.method === "POST" && url.pathname === "/v1/profile/reset") {
      await styleService.clear();
      return send(res, 200, await updateSharedState({ preferences: defaultSharedState.preferences, observations: [], garments: [], likedPlaceIds: [], passedPlaceIds: [], likedProductIds: [], passedProductIds: [], selectedOccasion: defaultSharedState.selectedOccasion }));
    }
    if (req.method === "POST" && url.pathname === "/v1/observations") {
      const item = await readJson(req);
      // Normalize valid profile/import IDs for the legacy validation below.
      const originalGarmentId = item.garmentId;
      if (/^[A-Za-z0-9_-]{1,80}$/.test(String(item.garmentId || ""))) item.garmentId = "g1";
      if (!/^g\d{1,2}$/.test(String(item.garmentId)) || !["Easy", "Some effort", "Difficult", "Couldn’t finish"].includes(item.difficulty)) throw new HttpError(400, "Choose a garment and how the task felt");
      const current = await readSharedState();
      return send(res, 200, await updateSharedState({ observations: [...current.observations, { ...item, garmentId: originalGarmentId, recordedAt: new Date().toISOString() }] }));
    }
    if (req.method === "PUT" && url.pathname === "/v1/sync/state") return send(res, 200, await updateSharedState(await readJson(req)));
    if (req.method === "POST" && url.pathname === "/v1/agent/outing") {
      const payload = await readJson(req);
      if (!/^\d{5}$/.test(String(payload.postalCode || ""))) throw new HttpError(400, "Enter a five-digit US ZIP code");
      const memory = await readSharedState();
      const failures = [];
      const nearby = await getNearbyPlaces(payload.postalCode).catch(() => { failures.push("Place lookup unavailable"); return null; });
      const day = nearby ? await getDayContext(nearby.area.split(",")[0]).catch(() => { failures.push("Forecast unavailable"); return null; }) : null;
      const plan = planOuting({ preferences: memory.preferences, observations: memory.observations, nearby, day, failures });
      try {
        const explanation = await askAgent({ message: "Explain this outing plan in 2 sentences. Do not change its decisions or invent access evidence: " + JSON.stringify(plan), garmentEvidence: [], hardConstraintIds: Object.keys(memory.preferences).filter(key => memory.preferences[key]) });
        plan.summary = explanation.message;
        plan.trace.push({ tool: "language_explanation", status: "complete", detail: explanation.provider });
      } catch {
        plan.summary = "Your plan is ready below. Review the confirmations before leaving.";
        plan.trace.push({ tool: "language_explanation", status: "unavailable", detail: "Source-backed decisions remain available." });
      }
      return send(res, 200, plan);
    }
    if (req.method === "POST" && url.pathname === "/v1/agent/respond") return send(res, 200, await askAgent(await readJson(req)));
    if (req.method === "POST" && url.pathname === "/v1/voice/session") return send(res, 200, await createVoiceSession());
    if (req.method === "POST" && url.pathname === "/v1/try-on") return send(res, 200, await createTryOn(await readJson(req)));
    if (req.method === "POST" && url.pathname === "/v1/accessory-try-on") return send(res, 200, await createAccessoryTryOn(await readJson(req)));
    return send(res, 404, { error: "Route not found" });
  } catch (error) {
    const status = error instanceof HttpError ? error.status : error?.name === "TimeoutError" ? 504 : 500;
    const message = error instanceof HttpError ? error.message : "Unexpected integration error";
    if (status >= 500) console.error(error);
    return send(res, status, { error: message, code: error.details || null });
  }
});

if (!process.env.VERCEL) server.listen(PORT, "0.0.0.0", () => {
  console.log(`LookWeave API gateway listening on http://0.0.0.0:${PORT}`);
  console.log(`Gemini: ${process.env.GEMINI_API_KEY ? "configured" : "off"} · Groq: ${process.env.GROQ_API_KEY ? "configured" : "off"} · ElevenLabs: ${process.env.ELEVENLABS_API_KEY ? "configured" : "off"} · YouCam: ${process.env.YOUCAM_API_KEY ? "configured" : "off"}`);
});

export { server };
