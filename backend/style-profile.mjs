// Styling rules are preferences, not measurements of beauty or physical fit.
export const SHAPES = ['Unknown', 'Oval', 'Round', 'Square', 'Heart', 'Oblong', 'Diamond', 'Triangle', 'InvTriangle'];
const hex = value => /^#[0-9a-f]{6}$/i.test(value || '') ? value.toUpperCase() : null;
function samplePalette(skin) {
  const [r,g,b]=skin.slice(1).match(/../g).map(n=>parseInt(n,16)/255);
  const max=Math.max(r,g,b),min=Math.min(r,g,b),d=max-min;
  const hue=d===0?0:((max===r?(g-b)/d+(g<b?6:0):max===g?(b-r)/d+2:(r-g)/d+4)*60);
  function hsl(h,s,l){const a=s*Math.min(l,1-l);const f=n=>{const k=(n+h/30)%12;return Math.round(255*(l-a*Math.max(-1,Math.min(k-3,9-k,1)))).toString(16).padStart(2,'0');};return ('#'+f(0)+f(8)+f(4)).toUpperCase();}
  return [['Complement',hsl((hue+180)%360,.36,.36)],['Soft accent',hsl((hue+150)%360,.27,.62)],['Earth accent',hsl(hue,.32,.45)],['Quiet neutral',hsl(hue,.16,.83)]];
}
export function cleanChoices(input = {}) {
  return {
    colourDirection: ['Explore', 'Warm', 'Cool', 'Mixed'].includes(input.colourDirection) ? input.colourDirection : 'Explore',
    faceShape: SHAPES.includes(input.faceShape) ? input.faceShape : 'Unknown',
    skinColour: hex(input.skinColour),
    metal: ['Explore', 'Gold', 'Silver', 'Mixed'].includes(input.metal) ? input.metal : 'Explore',
    nickelAvoidance: input.nickelAvoidance === true,
    lightweightFrames: input.lightweightFrames === true,
    hearingDevice: input.hearingDevice === true,
    lowBridge: input.lowBridge === true,
    easyClasps: input.easyClasps === true
  };
}
export function normalizeAnalysis(kind, raw) {
  if (raw?.face_quality?.has_face === false) throw new Error('No face was detected. Choose one clear, front-facing portrait.');
  if (kind === 'colour') {
    const colours = Object.fromEntries(['skin_color', 'eye_color', 'hair_color', 'eyebrow_color', 'lip_color'].map(key => [key, hex(raw?.color?.[key])]).filter(([,value]) => value));
    if (!colours.skin_color) throw new Error('The provider did not return a usable skin colour. Try even daylight or choose your colours manually.');
    return { colours, source: 'YouCam Facial Color Tones', measuredAt: new Date().toISOString() };
  }
  const ratios = {};
  for (const [key, length] of [['face_aspect_ratio', 2], ['horizontal_third', 3], ['vertical_fifth', 5]]) {
    const value = raw?.facialratio?.[key];
    if (Array.isArray(value) && value.length === length && value.every(n => typeof n === 'number' && Number.isFinite(n) && n >= 0)) ratios[key] = value;
  }
  const shape = SHAPES.includes(raw?.faceshape) ? raw.faceshape : 'Unknown';
  if (shape === 'Unknown' && !Object.keys(ratios).length) throw new Error('The provider did not return usable face proportions. Try a centred portrait or choose a shape yourself.');
  return { faceShape: shape, ratios, eyeDistance: ['Close-set','Wide-Set','Average'].includes(raw?.eyelid?.setting) ? raw.eyelid.setting : null, source: 'YouCam Face Attributes & Ratio', measuredAt: new Date().toISOString() };
}
export function recommendStyle(profile = {}, wearability = {}) {
  const choices = cleanChoices(profile.choices);
  const shape = choices.faceShape !== 'Unknown' ? choices.faceShape : profile.face?.faceShape || 'Unknown';
  const skin = choices.skinColour || profile.colour?.colours?.skin_color;
  const direction = choices.colourDirection;
  let palette = direction === 'Warm' ? [['Terracotta','#AA5944'],['Olive','#69764B'],['Cream','#F1E4CD'],['Petrol','#28575E']]
    : direction === 'Cool' ? [['Berry','#873C63'],['Slate blue','#526781'],['Pearl','#E6E5E0'],['Forest','#315C53']]
    : [['Teal','#286B70'],['Rose','#AD6E7E'],['Ink','#283442'],['Oat','#E1D3BC']];
  if (direction === 'Explore' && skin) palette = samplePalette(skin);
  // Pair a contrasting neutral with the sampled swatch, without inferring ethnicity or undertone.
  if (skin) {
    const channels = skin.slice(1).match(/../g).map(n => parseInt(n,16)/255);
    const brightness = channels[0]*.2126 + channels[1]*.7152 + channels[2]*.0722;
    palette = [brightness < .5 ? ['Light contrast','#F1E4CD'] : ['Deep contrast','#283442'], ...palette];
  }
  const frameMap = {
    Round: ['Angular acetate', 'A rectangular line offers a deliberate contrast to a rounded outline.'],
    Square: ['Soft oval', 'Rounded edges offer a softer contrast to an angular outline.'],
    Heart: ['Fine oval or bottom-accented', 'A light upper rim keeps the brow area visually open.'],
    InvTriangle: ['Fine oval or bottom-accented', 'Try a lighter upper rim and compare the silhouette.'],
    Oblong: ['Deeper round or square', 'A deeper lens adds a horizontal accent; check cheek clearance.'],
    Diamond: ['Oval or softly lifted', 'An open, soft rim gives room around the cheek line.'],
    Triangle: ['Browline or softly lifted', 'A stronger upper line is one way to add visual emphasis.'],
    Oval: ['Panto or rectangular', 'Compare a curved and an angular silhouette; either can suit your taste.'],
    Unknown: ['Compare round and rectangular', 'Choose the silhouette you like; a face scan is optional.']
  };
  const [frame, frameReason] = frameMap[shape] || frameMap.Unknown;
  const checks = ['Check printed lens width, bridge width and temple length against a comfortable existing pair. A photo cannot supply millimetre fit or prescription PD.'];
  if (choices.lowBridge) checks.push('Ask for low-bridge-fit frames or adjustable nose pads; check slipping and cheek contact in person.');
  if (choices.hearingDevice) checks.push('Try the frame with your hearing device. Confirm temple clearance and pressure behind the ears.');
  if (choices.lightweightFrames || wearability.lowSensoryLoad) checks.push('Compare listed frame weight and temple pressure. Lightweight does not guarantee comfort.');
  if (choices.nickelAvoidance) checks.push('Require manufacturer-confirmed composition for all skin-contact parts; a gold or silver colour does not establish nickel content.');
  const metal = choices.metal !== 'Explore' ? choices.metal : direction === 'Warm' ? 'Gold' : direction === 'Cool' ? 'Silver' : 'Mixed';
  return {
    palette: palette.map(([name, colour]) => ({ name, colour })),
    paletteReason: `${direction === 'Explore' ? (skin ? 'Colour-wheel companion hues from your skin sample' : 'An exploratory palette') : `Your ${direction.toLowerCase()} colour direction`}${skin ? ' with a contrasting neutral' : ''}. These are styling options, not an undertone diagnosis or colours you must wear.`,
    eyewear: { title: frame, reason: frameReason, shape, checks },
    jewellery: { title: `${metal} finishes`, reason: choices.metal !== 'Explore' ? 'Your selected finish takes priority over colour rules.' : 'A starting point from your colour direction; compare finishes in daylight.', closure: choices.easyClasps || wearability.oneHandClosure ? 'Explore slip-on or large easy-grip closures; confirm handling and material composition before buying.' : 'Choose the clasp and weight you find comfortable.' },
    accessories: ['Repeat one palette colour in a scarf, bag or hat to connect the outfit.', shape === 'Oblong' ? 'Explore a lower-crown hat or wider brim if you want a horizontal accent.' : 'Compare hat crown and brim shapes with your chosen glasses.'],
    evidence: { shape: choices.faceShape !== 'Unknown' ? 'User selected' : profile.face ? 'Photo estimate' : 'Not analysed', skin: choices.skinColour ? 'User corrected' : skin ? 'Photo sample' : 'Not analysed' }
  };
}
