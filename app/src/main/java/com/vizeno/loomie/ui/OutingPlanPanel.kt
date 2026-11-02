package com.vizeno.loomie.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vizeno.loomie.integration.LookWeaveGateway
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
internal fun OutingPlanPanel() {
    var zip by rememberSaveable { mutableStateOf("") }
    var working by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<JSONObject?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    Column(Modifier.fillMaxWidth().background(Color(0xFFE4ECE7), RoundedCornerShape(24.dp)).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("WEAR & GO", style = MaterialTheme.typography.labelLarge)
        Text("Make the outing worth it.", style = MaterialTheme.typography.headlineSmall)
        Text("Your garment evidence, access needs and local conditions — one considered plan.", style = MaterialTheme.typography.bodyMedium)
        OutlinedTextField(zip, { zip = it.filter(Char::isDigit).take(5) }, label = { Text("US ZIP code") }, supportingText = { Text("Area only. Your home address is never needed.") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Button(onClick = {
            working = true; error = null; result = null
            scope.launch {
                runCatching { LookWeaveGateway.outingPlan(zip) }
                    .onSuccess { result = it }
                    .onFailure { error = it.message ?: "The plan could not be completed. Try again." }
                working = false
            }
        }, enabled = zip.length == 5 && !working, modifier = Modifier.fillMaxWidth()) {
            Text(if (working) "Checking places, conditions & evidence…" else "Plan my outing")
        }
        if (working) LinearProgressIndicator(Modifier.fillMaxWidth())
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        result?.let { plan ->
            Text(plan.optString("summary"), style = MaterialTheme.typography.bodyLarge)
            val decisions = plan.optJSONArray("decisions")
            repeat(decisions?.length() ?: 0) { index ->
                val decision = decisions!!.getJSONObject(index)
                Column(Modifier.fillMaxWidth().background(Color.White.copy(alpha = .8f), RoundedCornerShape(16.dp)).padding(14.dp)) {
                    Text(if (decision.optString("status") == "confirm") "CONFIRM FIRST" else "IN YOUR PLAN", style = MaterialTheme.typography.labelSmall)
                    Text(decision.optString("title"), style = MaterialTheme.typography.titleMedium)
                    Text(decision.optString("detail"), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text("How this was decided", style = MaterialTheme.typography.titleMedium)
            val trace = plan.optJSONArray("trace")
            repeat(trace?.length() ?: 0) { index ->
                val step = trace!!.getJSONObject(index)
                Text("${index + 1}. ${step.optString("detail")}", style = MaterialTheme.typography.bodySmall)
            }
            Text(plan.optString("source"), style = MaterialTheme.typography.labelSmall)
        }
    }
}
