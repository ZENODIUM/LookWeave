import { readFile } from 'node:fs/promises';
for (const file of ['.env', 'backend/.env']) {
  try { for (const line of (await readFile(file, 'utf8')).split(/\r?\n/)) {
    const match = line.match(/^\s*([A-Z_]+)\s*=\s*(.*?)\s*$/);
    if (match && !process.env[match[1]]) process.env[match[1]] = match[2].replace(/^['"]|['"]$/g, '');
  } } catch (error) { if (error.code !== 'ENOENT') throw error; }
}
if (!process.env.YOUCAM_API_KEY) throw new Error('YouCam key is not configured');
let token = '';
for (let page = -1; page < 10; page++) {
  const path = page === -1 ? '/s2s/v1.0/client/credit' : '/s2s/v2.0/credit/feature-cost' + (token ? '?starting_token=' + encodeURIComponent(token) : '');
  const response = await fetch('https://yce-api-01.makeupar.com' + path, {headers: {authorization: `Bearer ${process.env.YOUCAM_API_KEY}`}, signal: AbortSignal.timeout(20000)});
  const body = await response.json();
  if (page === -1) console.log(JSON.stringify({status: response.status, remainingUnits: body.results?.reduce((sum, item) => sum + item.amount_dec, 0)}));
  else {
    console.log(JSON.stringify({page, status: response.status, relevantCosts: body.result?.skus?.filter(item => /cloth|skin-tone|face-attr/i.test(item.run_task_url))}));
    token = body.result?.next_token;
    if (!token) break;
  }
}
