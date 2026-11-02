import test from 'node:test';
import assert from 'node:assert/strict';
import { planOuting } from './outing-planner.mjs';
test('excludes inaccessible places, prioritizes documented access, leaves unknowns conditional', () => {
  const result = planOuting({preferences:{stepFreeAccess:true,conserveEnergy:true}, nearby:{area:'Test',places:[{name:'No',accessibility:'no',distanceMiles:.1},{name:'Unknown',accessibility:'unknown',distanceMiles:.2},{name:'Yes',accessibility:'yes',distanceMiles:1}]}});
  assert.equal(result.destination.name, 'Yes');
  assert.equal(result.rejected.length, 1);
  assert.ok(result.decisions.some(d=>d.title==='One stop, one preview'));
  const unknown = planOuting({preferences:{stepFreeAccess:true},nearby:{area:'Test',places:[{name:'Unknown',accessibility:'unknown',distanceMiles:1}]}});
  assert.equal(unknown.decisions[0].status,'confirm');
});
test('missing data never becomes a fabricated successful forecast or destination', () => {
  const result = planOuting({preferences:{heatSensitive:true}, failures:['offline']});
  assert.equal(result.destination,null);
  assert.equal(result.trace.find(t=>t.tool==='environment_forecast').status,'unavailable');
  assert.equal(result.decisions[0].status,'confirm');
});
