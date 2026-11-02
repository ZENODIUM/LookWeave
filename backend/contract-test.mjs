import { createServer } from "node:http";
import { spawn } from "node:child_process";
import { once } from "node:events";
import { setTimeout as wait } from "node:timers/promises";
import { tmpdir } from "node:os";
import { join } from "node:path";

const providerPort = 18902;
const gatewayPort = 18901;
let uploadCount = 0;
let failGemini = false;
let failPrimaryVoice = false;
const groqKeysSeen = [];

function json(res, status, body) {
  res.writeHead(status, { "content-type": "application/json" });
  res.end(JSON.stringify(body));
}

const provider = createServer(async (req, res) => {
  const url = new URL(req.url, `http://127.0.0.1:${providerPort}`);
  if (url.pathname === "/v1beta/models") {
    return json(res, 200, { models: [{ name: "models/gemini-2.5-flash", supportedGenerationMethods: ["generateContent"] }] });
  }
  if (url.pathname.endsWith(":generateContent")) {
    if (failGemini) return json(res, 429, { error: { message: "Gemini quota pause" } });
    return json(res, 200, { candidates: [{ content: { parts: [{ text: JSON.stringify({ message: "Keep the silhouette; change only the closure.", proposedToolCalls: [{ name: "retrieve_rules", arguments: {} }] }) }] } }] });
  }
  if (url.pathname === "/groq/models") {
    return json(res, 200, { data: [{ id: "qwen/qwen3.8-27b" }] });
  }
  if (url.pathname === "/groq/chat/completions") {
    groqKeysSeen.push(req.headers.authorization);
    return json(res, 200, { choices: [{ message: { content: JSON.stringify({ message: "The backup kept the same constraints.", proposedToolCalls: [] }) } }] });
  }
  if (url.pathname === "/v1/user/subscription") {
    return json(res, 200, { character_count: 100, character_limit: 10000, can_extend_character_limit: false });
  }
  if (url.pathname === "/v1/convai/conversation/token") {
    if (failPrimaryVoice && req.headers["xi-api-key"] === "test-eleven-key") return json(res, 402, { error: "Credits exhausted" });
    return json(res, 200, { token: req.headers["xi-api-key"] === "test-eleven-backup" ? "signed-backup-token" : "signed-conversation-token", conversation_id: "conversation-1" });
  }
  if (url.pathname.startsWith("/v1/convai/agents/") && req.method === "PATCH") {
    let body = "";
    for await (const chunk of req) body += chunk;
    const prompt = JSON.parse(body).conversation_config.agent.prompt.prompt;
    if (prompt.includes("Return concise JSON only")) return json(res, 400, { error: "Voice prompt must be conversational" });
    return json(res, 200, { agent_id: "test-agent" });
  }
  if (url.pathname === "/open-meteo/search") {
    return json(res, 200, { results: [{ name: "Brooklyn", admin1: "New York", country_code: "US", latitude: 40.65, longitude: -73.95 }] });
  }
  if (url.pathname === "/open-meteo/forecast") {
    return json(res, 200, { timezone: "America/New_York", current: { time: "2026-10-27T12:00", apparent_temperature: 42, relative_humidity_2m: 70, precipitation: 0, wind_gusts_10m: 29 }, daily: { uv_index_max: [6.4], precipitation_probability_max: [62], temperature_2m_max: [49], temperature_2m_min: [38] } });
  }
  if (url.pathname === "/open-meteo/air-quality") {
    return json(res, 200, { current: { us_aqi: 48, pm2_5: 6, uv_index: 3, grass_pollen: null } });
  }
  if (url.pathname === "/postal/us/11217") {
    return json(res, 200, { "post code": "11217", places: [{ "place name": "Brooklyn", "state abbreviation": "NY", latitude: "40.6816", longitude: "-73.9798" }] });
  }
  if (url.pathname === "/nominatim/search") {
    const brand = String(url.searchParams.get("q") || "Store").split(",")[0];
    return json(res, 200, [{ osm_type: "node", osm_id: brand.length, lat: "40.6906", lon: "-73.9830", name: brand, address: { house_number: "445", road: "Albee Square", neighbourhood: "Downtown Brooklyn", state: "New York" } }]);
  }
  if (url.pathname === "/s2s/v2.0/file" && req.method === "POST") {
    uploadCount += 1;
    return json(res, 200, { data: { files: [{ file_id: `file-${uploadCount}`, requests: [{ method: "PUT", url: `http://127.0.0.1:${providerPort}/upload/${uploadCount}`, headers: { "Content-Type": "image/jpeg" } }] }] } });
  }
  if (url.pathname.startsWith("/upload/") && req.method === "PUT") {
    if (req.headers['content-type'] !== 'image/jpeg') return json(res, 403, {error:'Signed Content-Type changed or duplicated'});
    for await (const _ of req) {}
    res.writeHead(200); return res.end();
  }
  if (url.pathname === "/s2s/v2.0/task/cloth-v4" && req.method === "POST") {
    return json(res, 200, { data: { task_id: "try-on-1" } });
  }
  if (url.pathname === "/s2s/v2.0/task/cloth-v4/try-on-1") {
    return json(res, 200, { data: { task_status: "success", results: { url: "https://example.test/preview.jpg" } } });
  }
  return json(res, 404, { error: "Unexpected provider request", path: url.pathname });
});

async function request(path, body, method = body === undefined ? "GET" : "POST") {
  const response = await fetch(`http://127.0.0.1:${gatewayPort}${path}`, {
    method,
    headers: { "content-type": "application/json" },
    body: body === undefined ? undefined : JSON.stringify(body)
  });
  const payload = await response.json();
  if (!response.ok) throw new Error(`${path} returned ${response.status}: ${JSON.stringify(payload)}`);
  return payload;
}

function assert(value, message) {
  if (!value) throw new Error(message);
}

await new Promise(resolve => provider.listen(providerPort, "127.0.0.1", resolve));
const gateway = spawn(process.execPath, ["server.mjs"], {
  cwd: new URL(".", import.meta.url),
  env: {
    ...process.env,
    PORT: String(gatewayPort),
    SYNC_STATE_PATH: join(tmpdir(), `lookweave-contract-${process.pid}.json`),
    TRY_ON_CACHE_PATH: join(tmpdir(), `lookweave-tryon-contract-${process.pid}.json`),
    GEMINI_API_KEY: "test-gemini-key",
    GROQ_API_KEY: "test-groq-key",
    GROQ_API_KEY_2: "test-groq-backup",
    ELEVENLABS_API_KEY: "test-eleven-key",
    ELEVENLABS_AGENT_ID: "test-agent",
    ELEVENLABS_API_KEY_2: "test-eleven-backup",
    ELEVENLABS_AGENT_ID_2: "test-backup-agent",
    ELEVENLABS_API_KEY_3: "",
    ELEVENLABS_API_KEY_4: "",
    YOUCAM_API_KEY: "test-youcam-key",
    GEMINI_API_ROOT: `http://127.0.0.1:${providerPort}/v1beta`,
    GROQ_API_ROOT: `http://127.0.0.1:${providerPort}/groq`,
    ELEVENLABS_API_ROOT: `http://127.0.0.1:${providerPort}/v1`,
    YOUCAM_API_ROOT: `http://127.0.0.1:${providerPort}/s2s/v2.0`,
    GEOCODING_API_ROOT: `http://127.0.0.1:${providerPort}/open-meteo`,
    WEATHER_API_ROOT: `http://127.0.0.1:${providerPort}/open-meteo`,
    AIR_QUALITY_API_ROOT: `http://127.0.0.1:${providerPort}/open-meteo`
    ,POSTAL_API_ROOT: `http://127.0.0.1:${providerPort}/postal`
    ,NOMINATIM_API_ROOT: `http://127.0.0.1:${providerPort}/nominatim`
  },
  stdio: "ignore"
});

try {
  let ready = false;
  for (let attempt = 0; attempt < 40; attempt++) {
    try { await request("/health"); ready = true; break; } catch { await wait(100); }
  }
  assert(ready, "Gateway did not start");
  const health = await request("/health");
  assert(health.services.gemini && health.services.groq && health.services.agent && health.services.elevenlabs && health.services.youcam, "Health configuration is incomplete");

  const agent = await request("/v1/agent/respond", { message: "Make the closure easier", garmentEvidence: [], hardConstraintIds: ["easy_closure"] });
  assert(agent.message.includes("closure") && agent.model === "gemini-2.5-flash" && agent.executedActions?.[0]?.status === "complete", "Gemini orchestration contract failed");
  failGemini = true;
  const fallback = await request("/v1/agent/respond", { message: "Keep my constraints", garmentEvidence: [], hardConstraintIds: ["easy_closure"] });
  assert(fallback.provider === "groq" && fallback.message.includes("backup"), "Groq fallback contract failed");
  await request("/v1/agent/respond", { message: "Keep my constraints", garmentEvidence: [], hardConstraintIds: ["easy_closure"] });
  assert(groqKeysSeen.includes("Bearer test-groq-key") && groqKeysSeen.includes("Bearer test-groq-backup"), "Groq keys did not rotate");

  const voice = await request("/v1/voice/session", {});
  assert(voice.token === "signed-conversation-token" && voice.expiresInSeconds === 600, "ElevenLabs token contract failed");
  failPrimaryVoice = true;
  const backupVoice = await request("/v1/voice/session", {});
  assert(backupVoice.token === "signed-backup-token" && backupVoice.agentId === "test-backup-agent", "ElevenLabs backup contract failed");

  const context = await request("/v1/context?place=Brooklyn");
  assert(context.location.includes("Brooklyn") && context.recommendations.some(item => item.id === "rain") && context.recommendations.some(item => item.id === "wind"), "Day Signals contract failed");
  const intervalContext = await request("/v1/context?place=Brooklyn&departure=08:00&return=18:00");
  assert(intervalContext.interval.departure === "08:00" && intervalContext.interval.returnTime === "18:00", "Whole Day interval contract failed");

  const nearby = await request("/v1/nearby?postalCode=11217");
  assert(nearby.area === "Brooklyn, NY" && nearby.places.length > 0 && nearby.privacy.includes("not stored"), "ZIP-only nearby discovery contract failed");

  const sync = await request("/v1/sync/state", { preferences: { seatedComfort: false }, selectedOccasion: "Museum opening" }, "PUT");
  const synced = await request("/v1/sync/state");
  assert(sync.revision > 0 && synced.preferences.seatedComfort === false && synced.selectedOccasion === "Museum opening", "Shared state contract failed");
  const garment = { id: "import-contract", name: "Verified source jacket", subtitle: "https://example.test/product", category: "OUTERWEAR", colorName: "Imported", closure: { value: "Magnet", source: "PRODUCT", confidence: 80 }, fabric: { value: "Unknown", source: "ESTIMATED", confidence: 0 }, wearability: 50, oneHandFriendly: false, seatedComfort: false, lowSensoryLoad: false, deviceClearance: false, available: true, note: "Unknown facts remain unknown." };
  await request("/v1/sync/state", { garments: [garment] }, "PUT");
  const restored = await request("/v1/sync/state");
  assert(restored.garments.some(item => item.id === "import-contract"), "Imported garment persistence contract failed");
  const draft = await request("/v1/rules/draft", { text: "I need clothing that is easier to remove" });
  assert(draft.rule.key === "EASY_REMOVAL" && draft.rule.confirmed === false, "Rule draft contract failed");
  const createdRule = await request("/v1/rules", { confirmed: true, rule: { ...draft.rule, confirmed: true } });
  const listedRules = await request("/v1/rules");
  assert(listedRules.rules.some(rule => rule.id === createdRule.rule.id && rule.confirmed), "Rule create/retrieve contract failed");
  await request(`/v1/rules/${encodeURIComponent(createdRule.rule.id)}`, undefined, "DELETE");
  const afterDelete = await request("/v1/rules");
  assert(!afterDelete.rules.some(rule => rule.id === createdRule.rule.id), "Rule delete contract failed");
  await request("/v1/sync/state", { preferences: { stepFreeAccess: true, conserveEnergy: true, avoidWool: true } }, "PUT");
  await request("/v1/observations", { garmentId: "g6", task: "Fasten closure", seconds: 9, difficulty: "Difficult" });
  const outing = await request("/v1/agent/outing", { postalCode: "11217" });
  assert(outing.trace.length >= 5 && outing.decisions.some(item => item.title === "Use your garment evidence"), "Observation-to-outing planning loop failed");
  assert(outing.decisions.some(item => item.title === "Confirm access before leaving"), "Unknown access must require confirmation");

  const image = { mimeType: "image/jpeg", base64: Buffer.from("jpeg-test").toString("base64") };
  const preview = await request("/v1/try-on", { person: image, garment: image, category: "auto" });
  assert(preview.taskId === "try-on-1" && preview.imageUrl.includes("preview.jpg"), "YouCam orchestration contract failed");
  const cachedPreview = await request("/v1/try-on", { person: image, garment: image, category: "auto" });
  assert(cachedPreview.cached === true && uploadCount === 2, "YouCam cache reuse contract failed");
  console.log("All LookWeave provider contracts passed.");
} finally {
  gateway.kill();
  provider.close();
  await Promise.race([once(gateway, "exit"), wait(2_000)]);
}
