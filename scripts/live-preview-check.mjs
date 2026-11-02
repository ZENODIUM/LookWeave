import { readFile, writeFile, mkdir } from 'node:fs/promises';
// One explicit live task. Never automatically retries a paid submission.
const image = async path => ({mimeType:'image/png',base64:(await readFile(path)).toString('base64')});
const body = {person:await image('app/src/main/res/drawable-nodpi/lookweave_editorial.png'),garment:await image('app/src/main/res/drawable-nodpi/garment_g12.png'),category:'outer'};
const startedAt = new Date().toISOString();
const response = await fetch('http://127.0.0.1:8787/v1/try-on',{method:'POST',headers:{'content-type':'application/json'},body:JSON.stringify(body),signal:AbortSignal.timeout(230000)});
const result = await response.json();
await mkdir('artifacts/live-validation',{recursive:true});
await writeFile('artifacts/live-validation/youcam-task.json', JSON.stringify({startedAt,finishedAt:new Date().toISOString(),status:response.status,taskId:result.taskId,cached:result.cached||false,error:result.error},null,2));
if (!response.ok) throw new Error(`Preview failed (${response.status}): ${result.error}`);
const output = await fetch(result.imageUrl,{signal:AbortSignal.timeout(60000)});
if (!output.ok) throw new Error('Result download failed');
await writeFile('artifacts/live-validation/youcam-outer-result.jpg',Buffer.from(await output.arrayBuffer()));
console.log(JSON.stringify({taskId:result.taskId,cached:result.cached||false,saved:'artifacts/live-validation/youcam-outer-result.jpg'}));
