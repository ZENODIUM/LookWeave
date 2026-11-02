package com.vizeno.loomie.integration

/** Boundaries for hosted services. Secrets belong on a backend, never in the APK. */
interface FashionAgentService {
    suspend fun respond(request: AgentRequest): AgentResponse
}

data class AgentRequest(
    val message: String,
    val garmentEvidence: List<GarmentEvidencePayload>,
    val hardConstraintIds: Set<String>,
    val dayContextNotes: String? = null,
    val priceLimit: Double? = null,
    val budgetRemaining: Double? = null,
    val events: List<String> = emptyList(),
    val preferences: Map<String, Any?> = emptyMap(),
    val style: Map<String, String> = emptyMap(),
    val sizes: Map<String, String> = emptyMap(),
    /** Resolved gender label (preset or custom) for gendered suggestions. */
    val gender: String? = null,
    /** Private ZIP used only for location-aware agent context. */
    val postalCode: String? = null,
    val wardrobe: List<String> = emptyList(),
    val accessoriesPreferences: Map<String, String> = emptyMap(),
    val discoveryLikes: List<String> = emptyList(),
    val fitObservations: List<String> = emptyList(),
    val activeFilters: List<String> = emptyList(),
    val customRules: List<String> = emptyList(),
    val purchases: List<String> = emptyList(),
    val plannedOutfits: List<String> = emptyList()
)

data class AgentResponse(
    val message: String,
    val proposedToolCalls: List<ToolCall>,
    val executedActions: List<AgentAction> = emptyList()
)

data class ToolCall(val name: String, val arguments: Map<String, String>)
data class AgentAction(val tool: String, val status: String, val title: String, val detail: String)

data class GarmentEvidencePayload(
    val garmentId: String,
    val facts: Map<String, String>,
    val sources: Map<String, String>
)

interface VirtualTryOnService {
    suspend fun createPreview(request: TryOnRequest): TryOnResult
}

data class TryOnRequest(
    val personImageUri: String,
    val garmentImageUris: List<String>,
    val category: String
)

sealed interface TryOnResult {
    data class Ready(val imageUrl: String, val taskId: String) : TryOnResult
    data class Failed(val recoverable: Boolean, val userMessage: String) : TryOnResult
}

interface VoiceAgentService {
    suspend fun beginSession(context: VoiceContext): VoiceSession
    suspend fun endSession(sessionId: String)
}

data class VoiceContext(val userName: String, val activeLookId: String?, val hardConstraintIds: Set<String>)
data class VoiceSession(val sessionId: String, val signedConnectionUrl: String)
