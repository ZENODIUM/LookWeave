import { createHash, randomUUID } from 'node:crypto';
import { mkdir, readFile, writeFile, rename } from 'node:fs/promises';
import { dirname } from 'node:path';
import { cleanChoices, normalizeAnalysis, recommendStyle } from './style-profile.mjs';

export function createStyleService({ path, root, key, upload, decode, providerFetch, HttpError, preferences }) {
  let store;
  let busy = false;
  let writes = Promise.resolve();
  async function load() {
    if (!store) {
      try { store = JSON.parse(await readFile(path, 'utf8')); }
      catch (error) { if (error.code !== 'ENOENT') throw error; store = { profile: {}, jobs: [], reservedUnits: 0 }; }
      // Never resubmit an ambiguously interrupted paid task.
      for (const job of store.jobs) if (job.status === 'running') { job.status = 'interrupted'; job.message = 'The gateway restarted. This analysis will not be automatically charged again.'; }
    }
    return store;
  }
  async function persist() {
    const snapshot = JSON.stringify(store, null, 2);
    const write = writes.catch(() => {}).then(async () => {
      await mkdir(dirname(path), { recursive: true });
      const temporaryPath = `${path}.${randomUUID()}.tmp`;
      await writeFile(temporaryPath, snapshot);
      await rename(temporaryPath, path);
    });
    writes = write;
    await write;
  }
  const publicJob = job => ({ id: job.id, status: job.status, message: job.message, steps: job.steps, reservedUnits: job.units });
  async function view() {
    await load();
    return { ...store.profile, recommendations: recommendStyle(store.profile, await preferences()), activeJob: store.jobs.length ? publicJob(store.jobs.at(-1)) : null };
  }
  async function save(choices) {
    await load();
    if (busy) throw new HttpError(409, 'Wait for the current analysis before editing your profile.');
    store.profile.choices = cleanChoices(choices);
    store.profile.updatedAt = new Date().toISOString();
    await persist(); return view();
  }
  async function clear() {
    await load();
    if (busy) throw new HttpError(409, 'Wait for the current analysis before deleting its results.');
    store.profile = {};
    // Preserve only spend accounting; remove face data, fingerprints and provider task identifiers.
    store.jobs = [];
    await persist(); return view();
  }
  async function run(job, image) {
    try {
      const fileId = await upload(image, 'lookweave-style-portrait');
      for (const kind of job.kinds) {
        const endpoint = kind === 'colour' ? 'skin-tone-analysis' : 'face-attr-analysis';
        const body = { src_file_id: fileId, face_angle_strictness_level: 'high' };
        if (kind === 'face') body.features = ['faceShape', 'faceAspectRatio', 'horizontalThird', 'verticalFifth', 'eyeDistance'];
        job.message = kind === 'colour' ? 'Reading your colour swatches' : 'Reading face proportions';
        await persist();
        const task = await providerFetch(`${root}/task/${endpoint}`, { method: 'POST', headers: { authorization: `Bearer ${key()}`, 'content-type': 'application/json' }, body: JSON.stringify(body) });
        const taskId = task?.data?.task_id;
        if (!taskId) throw new Error('The provider did not return a task identifier. No automatic retry will be made.');
        job.steps.push({ kind, taskId, status: 'running' }); await persist();
        let result;
        for (let attempt = 0; attempt < 24; attempt++) {
          if (attempt) await new Promise(resolve => setTimeout(resolve, 10000));
          const response = await providerFetch(`${root}/task/${endpoint}/${encodeURIComponent(taskId)}`, { headers: { authorization: `Bearer ${key()}` } });
          const data = response.data;
          if (data?.task_status === 'error') throw new Error('The portrait could not be analysed. Use even daylight, one face filling most of the frame, and look straight ahead.');
          if (data?.task_status === 'success') { result = normalizeAnalysis(kind, data.results); break; }
        }
        if (!result) throw new Error('Analysis is taking too long. It will not be submitted again automatically.');
        store.profile[kind] = result;
        store.profile.updatedAt = new Date().toISOString();
        job.steps.at(-1).status = 'complete'; await persist();
      }
      job.status = 'complete'; job.message = 'Your style evidence is ready';
    } catch (error) {
      job.status = 'error';
      job.message = error instanceof HttpError ? (error.status === 429 ? 'The provider is busy. Please wait; no automatic resubmission was made.' : 'The analysis service could not complete this portrait. You can still choose your preferences manually.') : error.message;
    } finally { busy = false; await persist(); }
  }
  async function start(payload) {
    await load();
    if (payload.consent !== true) throw new HttpError(400, 'Confirm that you want this photo sent to YouCam for style analysis.');
    if (!key()) throw new HttpError(503, 'Photo analysis is not configured. You can choose preferences manually.');
    const kinds = payload.mode === 'colour' ? ['colour'] : payload.mode === 'face' ? ['face'] : payload.mode === 'both' ? ['colour', 'face'] : null;
    if (!kinds) throw new HttpError(400, 'Choose colour, face or both analyses.');
    const image = decode(payload.photo, 'Portrait');
    const fingerprint = createHash('sha256').update(image.bytes).update(kinds.join()).digest('hex');
    const prior = store.jobs.find(job => job.fingerprint === fingerprint);
    if (prior) return publicJob(prior);
    if (busy) throw new HttpError(409, 'Another style analysis is already processing.');
    const units = kinds.reduce((sum, kind) => sum + (kind === 'colour' ? 20 : 10), 0);
    if (store.reservedUnits + units > Number(process.env.YOUCAM_STYLE_UNIT_LIMIT || 120)) throw new HttpError(429, 'The style-analysis spending limit has been reached. Manual preferences remain available.');
    busy = true;
    const job = { id: randomUUID(), fingerprint, kinds, units, status: 'running', message: 'Preparing your portrait', steps: [], startedAt: new Date().toISOString() };
    store.reservedUnits += units; store.jobs.push(job);
    try { await persist(); } catch (error) { busy = false; throw error; }
    void run(job, image).catch(() => { busy = false; });
    return publicJob(job);
  }
  async function status(id) {
    await load(); const job = store.jobs.find(item => item.id === id);
    if (!job) throw new HttpError(404, 'Analysis not found.');
    return publicJob(job);
  }
  return { view, save, clear, start, status };
}
