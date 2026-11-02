import test from 'node:test';
import assert from 'node:assert/strict';
import { mkdtemp, readFile } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { normalizeAnalysis, recommendStyle, cleanChoices } from './style-profile.mjs';
import { createStyleService } from './style-service.mjs';

test('documented provider results keep only relevant evidence and no ideal scores', () => {
  const face = normalizeAnalysis('face', {faceshape:'Round',facialratio:{face_aspect_ratio:[1,1.42],vertical_fifth:[20,18,22,19,21]}, agegender:{age:24,gender:'female'}});
  assert.equal(face.faceShape,'Round'); assert.deepEqual(face.ratios.face_aspect_ratio,[1,1.42]); assert.equal(face.agegender,undefined);
  assert.throws(()=>normalizeAnalysis('colour',{color:{skin_color:'invalid'}}));
  assert.throws(()=>normalizeAnalysis('face',{face_quality:{has_face:false}}));
  assert.equal(cleanChoices({skinColour:'#a97852',ethnicity:'anything'}).skinColour,'#A97852');
});
test('manual choices override estimates, functional checks stay separate from colour', () => {
  const profile={face:{faceShape:'Round'},colour:{colours:{skin_color:'#52382C'}},choices:{faceShape:'Square',metal:'Silver',colourDirection:'Warm',nickelAvoidance:true,hearingDevice:true,lowBridge:true}};
  const result=recommendStyle(profile,{oneHandClosure:true});
  assert.equal(result.eyewear.title,'Soft oval');assert.equal(result.jewellery.title,'Silver finishes');
  assert.equal(result.eyewear.checks.length,4);assert.match(result.jewellery.closure,/easy-grip/);
  assert.equal(result.palette[0].name,'Light contrast');
  assert.equal(recommendStyle({}).eyewear.shape,'Unknown');
  assert.notDeepEqual(recommendStyle({colour:{colours:{skin_color:'#52382C'}}}).palette,recommendStyle({colour:{colours:{skin_color:'#CBB5A3'}}}).palette);
});
test('asynchronous analysis deduplicates, persists, clears evidence and preserves spend', async () => {
  const path=join(await mkdtemp(join(tmpdir(),'lookweave-style-')),'state.json');
  let calls=0;
  class HttpError extends Error {constructor(status,message){super(message);this.status=status;}}
  const config={path,root:'https://provider.test',key:()=> 'test-key',preferences:async()=>({}),HttpError,
    decode:photo=>({bytes:Buffer.from(photo.base64,'base64')}),upload:async()=> 'file-1',
    providerFetch:async(url,options)=>{
      if(options.method==='POST'){calls++;const body=JSON.parse(options.body);if(url.includes('face-attr'))assert.deepEqual(body.features,['faceShape','faceAspectRatio','horizontalThird','verticalFifth','eyeDistance']);return {data:{task_id:'task-'+calls}};}
      return {data:{task_status:'success',results:url.includes('skin-tone')?{color:{skin_color:'#AA8877'}}:{faceshape:'Oval',facialratio:{face_aspect_ratio:[1,1.5]}}}};
    }};
  const service=createStyleService(config);const payload={consent:true,mode:'both',photo:{base64:Buffer.from('portrait').toString('base64')}};
  await assert.rejects(service.start({...payload,consent:false}),/Confirm/);
  const job=await service.start(payload);assert.equal((await service.start(payload)).id,job.id);
  for(let i=0;i<100&&(await service.status(job.id)).status==='running';i++)await new Promise(resolve=>setTimeout(resolve,10));
  assert.equal((await service.status(job.id)).status,'complete');assert.equal(calls,2);
  assert.equal((await service.view()).colour.colours.skin_color,'#AA8877');
  const restarted=createStyleService(config);assert.equal((await restarted.start(payload)).id,job.id);assert.equal(calls,2);
  await restarted.save({metal:'Silver'});assert.equal((await restarted.view()).recommendations.jewellery.title,'Silver finishes');
  await restarted.clear();assert.equal((await restarted.view()).colour,undefined);
  const disk=JSON.parse(await readFile(path,'utf8'));assert.equal(disk.reservedUnits,30);assert.deepEqual(disk.jobs,[]);
});
