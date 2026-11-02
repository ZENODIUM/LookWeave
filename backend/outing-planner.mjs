// Decisions are grounded in source records; a language model may explain them,
// but cannot upgrade unknown accessibility to a confirmed match.
export function planOuting({ preferences, observations = [], nearby, day, failures = [] }) {
  const trace = [
    { tool: "profile_memory", status: "complete", detail: `${observations.length} recorded garment observations; selected preferences loaded.` },
    { tool: "postal_places", status: nearby ? "complete" : "unavailable", detail: nearby ? `${nearby.places.length} places around ${nearby.area}. ZIP centroid distances are not walking routes.` : "Place search unavailable; no destination inferred." },
    { tool: "environment_forecast", status: day ? "complete" : "unavailable", detail: day ? `${day.location}; forecast time ${day.observedAt}.` : "Forecast unavailable; no weather adjustment inferred." }
  ];
  const decisions = [];
  const rejected = [];
  const candidates = (nearby?.places || []).filter(place => {
    if (preferences.stepFreeAccess && place.accessibility === "no") {
      rejected.push(`${place.name}: OpenStreetMap reports wheelchair access as no.`);
      return false;
    }
    return true;
  }).sort((a, b) => {
    if (preferences.stepFreeAccess) {
      const access = Number(b.accessibility === "yes") - Number(a.accessibility === "yes");
      if (access) return access;
    }
    return a.distanceMiles - b.distanceMiles;
  });
  const destination = candidates[0];
  if (destination) {
    const requiresConfirmation = preferences.stepFreeAccess && destination.accessibility !== "yes";
    decisions.push({ title: requiresConfirmation ? "Confirm access before leaving" : "First stop", detail: `${destination.name}, ${destination.distanceMiles} miles from the ZIP centre. ${requiresConfirmation ? "Access is undocumented or limited. Call to confirm entrance, fitting-room access and seating." : preferences.stepFreeAccess ? "OSM reports wheelchair access; confirm today's conditions with the venue." : "Nearest returned candidate."}`, status: requiresConfirmation ? "confirm" : "ready" });
  } else decisions.push({ title: "Keep the trip optional", detail: "No destination meeting the available access evidence was found. Use a home preview and verify a store before travelling.", status: "confirm" });
  if (preferences.conserveEnergy) decisions.push({ title: "One stop, one preview", detail: "Limit this plan to one destination. Preview the selected garment at home first; a visual preview cannot establish physical comfort or fit.", status: "ready" });
  if (preferences.heatSensitive && day) {
    const temperature = day.signals?.apparentTemperatureF;
    decisions.push({ title: "Your heat preference", detail: Number.isFinite(temperature) ? `Forecast feels like ${temperature}°F. ${temperature >= 78 ? "Choose a removable layer and confirm indoor seating before the trip." : "No heat-driven clothing change is justified by this reading."}` : "Temperature is unavailable; keep your usual heat-management choices.", status: Number.isFinite(temperature) ? "ready" : "confirm" });
  }
  const effort = observations.filter(item => ["Difficult", "Couldn’t finish", "Couldn't finish"].includes(item.difficulty));
  for (const observation of effort.slice(-3)) decisions.push({ title: "Use your garment evidence", detail: `${observation.garmentId}: ${observation.task} felt ${observation.difficulty.toLowerCase()}. Review that interaction before including the garment; elapsed time alone does not define difficulty.`, status: "confirm" });
  const questions = ["Is there a step-free entrance and an accessible fitting room?", "Can a member of staff set aside the garment and provide a seat?"];
  trace.push({ tool: "constraint_review", status: "complete", detail: `${rejected.length} destinations excluded; ${decisions.filter(d => d.status === "confirm").length} confirmations remain.` });
  trace.push({ tool: "next_action", status: "complete", detail: "Preview at home, resolve access questions, then decide whether the trip is worthwhile." });
  return { title: "Your wear-and-go plan", destination: destination || null, decisions, rejected, questions, trace, failures, source: "OpenStreetMap contributors · Zippopotam.us · Open-Meteo / CAMS", createdAt: new Date().toISOString() };
}
