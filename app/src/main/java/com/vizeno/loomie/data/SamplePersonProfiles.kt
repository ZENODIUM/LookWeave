package com.vizeno.loomie.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.vizeno.loomie.model.FilterMode
import com.vizeno.loomie.model.GenderOptions
import com.vizeno.loomie.model.UserFilter
import com.vizeno.loomie.model.WearabilityPreferences
import org.json.JSONArray
import org.json.JSONObject
import com.vizeno.loomie.model.GarmentCategory
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.UUID

/**
 * Built-in sample people (assets/sample/person1…6 from tools/Input Images)
 * plus optional custom user-uploaded profiles. Selecting a profile writes
 * active body/face into LocalProfileStore for Fit / Face / Today — and seeds
 * a distinct personalization world (gender, ZIP, colors, capsule, wearability).
 *
 * Asset paths are identity-mapped: personN → sample/personN/{body,face,thumb}.jpg
 * (no cross-person remaps). Capsule IDs stay gender-correct placeholders until pN_01..10.
 */
object SamplePersonProfiles {
    private const val KEY_CUSTOM = "customProfiles"
    private const val PREFS = "lookweave"
    /** Bump when built-in persona worlds / asset paths change so devices re-seed. */
    private const val KEY_WORLD_VERSION = "personaWorldVersion"
    private const val WORLD_VERSION = 8

    data class WearabilitySeed(
        val oneHandClosure: Boolean = false,
        val seatedComfort: Boolean = false,
        val easyFootwear: Boolean = false,
        val lowSensoryLoad: Boolean = false,
        val deviceClearance: Boolean = false,
        val avoidWool: Boolean = false,
        val animalFree: Boolean = false,
        val mobilityAidCompatibility: Boolean = false,
        val limitedOverheadReach: Boolean = false,
        val gripStrength: Boolean = false,
        val easyRemoval: Boolean = false,
        val conserveEnergy: Boolean = false,
        val heatSensitive: Boolean = false,
        val seamPressureSensitivity: Boolean = false,
        val stepFreeAccess: Boolean = false,
        val filterIds: List<Pair<String, FilterMode>> = emptyList()
    ) {
        fun toPreferences(base: WearabilityPreferences = WearabilityPreferences()): WearabilityPreferences {
            val filters = filterIds.map { (id, mode) -> UserFilter(id, mode = mode, enabled = true) }
            return base.copy(
                oneHandClosure = oneHandClosure,
                seatedComfort = seatedComfort,
                easyFootwear = easyFootwear,
                lowSensoryLoad = lowSensoryLoad,
                deviceClearance = deviceClearance,
                avoidWool = avoidWool,
                animalFree = animalFree,
                mobilityAidCompatibility = mobilityAidCompatibility,
                limitedOverheadReach = limitedOverheadReach,
                gripStrength = gripStrength,
                easyRemoval = easyRemoval,
                conserveEnergy = conserveEnergy,
                heatSensitive = heatSensitive,
                seamPressureSensitivity = seamPressureSensitivity,
                stepFreeAccess = stepFreeAccess,
                userFilters = filters,
                customRules = base.customRules
            )
        }
    }

    data class Profile(
        val id: String,
        val displayName: String,
        val bodyAssetPath: String? = null,
        val faceAssetPath: String? = null,
        val bodyFileName: String? = null,
        val faceFileName: String? = null,
        val builtIn: Boolean,
        val preferredGarmentIds: List<String> = emptyList(),
        val gender: String = GenderOptions.PREFER_NOT,
        val postalCode: String = "11217",
        val preferredColors: List<String> = emptyList(),
        val avoidColors: List<String> = emptyList(),
        val preferredStyles: List<String> = emptyList(),
        val interests: List<String> = emptyList(),
        val wearabilitySeed: WearabilitySeed = WearabilitySeed(),
        val colourDirection: String = "Explore",
        val defaultFaceShape: String = "",
        val metalPreference: String = "Explore",
        val age: Int? = null,
        val occupation: String = "",
        val personaSummary: String = "",
        val exampleRequest: String = "",
        val monthlyBudget: Double? = null,
        val priceLimit: Double? = null
    )

    /** Permanent demo people — never removed by "Add profile". Each has a distinct world. */
    val builtIn: List<Profile> = listOf(
        // Person 1 — Sensory male. Assets: sample/person1.
        Profile(
            id = "person1",
            displayName = "Noah · Sensory",
            bodyAssetPath = "sample/person1/body.jpg",
            faceAssetPath = "sample/person1/face.jpg",
            builtIn = true,
            preferredGarmentIds = (1..10).map { "p1_${it.toString().padStart(2, '0')}" },
            gender = GenderOptions.MALE,
            postalCode = "78701",
            preferredColors = listOf("Sage green", "Muted blue", "Cream", "Earthy tones"),
            avoidColors = listOf("Neon", "High-contrast patterns"),
            preferredStyles = listOf("minimalist streetwear", "relaxed fit", "tagless", "soft cotton"),
            interests = listOf("campus walking", "concerts", "familiar combinations", "comfortable sneakers"),
            wearabilitySeed = WearabilitySeed(
                lowSensoryLoad = true,
                avoidWool = true,
                seamPressureSensitivity = true,
                easyFootwear = true,
                seatedComfort = true,
                oneHandClosure = false,
                filterIds = listOf(
                    "sn_low_load" to FilterMode.MUST_AVOID,
                    "sn_tagless" to FilterMode.PREFER,
                    "sn_soft_handfeel" to FilterMode.PREFER,
                    "sn_no_scratchy_label" to FilterMode.MUST_AVOID,
                    "sn_no_stiff_collar" to FilterMode.MUST_AVOID,
                    "mt_avoid_wool" to FilterMode.MUST_AVOID,
                    "mt_soft_knit" to FilterMode.PREFER,
                    "pr_flat_seams" to FilterMode.PREFER,
                    "ft_easy" to FilterMode.PREFER,
                    "oc_casual" to FilterMode.PREFER
                )
            ),
            colourDirection = "Cool",
            defaultFaceShape = "Oval",
            metalPreference = "Silver",
            age = 24,
            occupation = "University student",
            personaSummary = "Autistic adult with self-reported sensory discomfort from scratchy fabrics, wool, tags, tight collars and restrictive cuffs. Prefers soft cotton, relaxed fits and tagless garments. Not a diagnosis inference for others.",
            exampleRequest = "I'm going to university in the morning and a concert tonight. I want something stylish, but nothing scratchy or restrictive. I love sage green and relaxed streetwear.",
            monthlyBudget = 120.0,
            priceLimit = 60.0
        ),
        // Person 2 — Jules brace / non-binary. Assets: sample/person2. Capsule = UNISEX only.
        Profile(
            id = "person2",
            displayName = "Jules · Brace",
            bodyAssetPath = "sample/person2/body.jpg",
            faceAssetPath = "sample/person2/face.jpg",
            builtIn = true,
            preferredGarmentIds = (1..10).map { "p2_${it.toString().padStart(2, '0')}" },
            gender = GenderOptions.NON_BINARY,
            postalCode = "60614",
            preferredColors = listOf("Navy", "Forest green", "Beige", "Neutrals"),
            avoidColors = listOf("Loud neons"),
            preferredStyles = listOf("smart casual", "elegant", "stretch", "adjustable waist", "flexible layers"),
            interests = listOf("office meetings", "commute walking", "brace-friendly trousers", "stable footwear"),
            wearabilitySeed = WearabilitySeed(
                seatedComfort = true,
                easyFootwear = true,
                mobilityAidCompatibility = true,
                deviceClearance = true,
                easyRemoval = true,
                conserveEnergy = true,
                filterIds = listOf(
                    "mt_stretch" to FilterMode.PREFER,
                    "se_soft_waist" to FilterMode.PREFER,
                    "se_stretch_seat" to FilterMode.PREFER,
                    "mt_no_stiff_denim" to FilterMode.MUST_AVOID,
                    "ft_easy" to FilterMode.MUST_AVOID,
                    "ft_stable_heel" to FilterMode.PREFER,
                    "ft_no_backless_unstable" to FilterMode.MUST_AVOID,
                    "oc_no_formal_shoes" to FilterMode.MUST_AVOID,
                    "mb_mobility_aid" to FilterMode.CONDITIONAL,
                    "oc_work" to FilterMode.PREFER,
                    "oc_smart" to FilterMode.PREFER
                )
            ),
            colourDirection = "Cool",
            defaultFaceShape = "Oblong",
            metalPreference = "Silver",
            age = 32,
            occupation = "Marketing professional",
            personaSummary = "Joint hypermobility; sometimes wears a knee brace. Prefers stretch, adjustable waists and flexible layers. Brace compatibility is user-confirmed, never inferred from a product photo.",
            exampleRequest = "I'm wearing my knee brace today, and I have an important meeting. Can you make my outfit professional without choosing anything too restrictive?",
            monthlyBudget = 180.0,
            priceLimit = 90.0
        ),
        // Person 3 — Kai prosthetic. Assets: sample/person3. Women + unisex capsule.
        Profile(
            id = "person3",
            displayName = "Kai · Prosthetic",
            bodyAssetPath = "sample/person3/body.jpg",
            faceAssetPath = "sample/person3/face.jpg",
            builtIn = true,
            preferredGarmentIds = (1..10).map { "p3_${it.toString().padStart(2, '0')}" },
            gender = GenderOptions.FEMALE,
            postalCode = "98109",
            preferredColors = listOf("Mustard", "Charcoal", "Olive", "Black", "Earthy"),
            avoidColors = emptyList(),
            preferredStyles = listOf("contemporary", "minimalist", "front closures", "relaxed shirts", "layered"),
            interests = listOf("client presentations", "creative meetings", "independent dressing", "prosthesis-friendly sleeves"),
            wearabilitySeed = WearabilitySeed(
                oneHandClosure = true,
                limitedOverheadReach = true,
                gripStrength = true,
                easyRemoval = true,
                deviceClearance = true,
                easyFootwear = true,
                filterIds = listOf(
                    "cl_one_hand" to FilterMode.MUST_AVOID,
                    "cl_magnetic" to FilterMode.PREFER,
                    "cl_front_access" to FilterMode.PREFER,
                    "cl_no_small_buttons" to FilterMode.MUST_AVOID,
                    "rc_limited_overhead" to FilterMode.MUST_AVOID,
                    "rc_front_entry" to FilterMode.PREFER,
                    "rc_sleeve_ease" to FilterMode.PREFER,
                    "gp_grip" to FilterMode.PREFER,
                    "rc_easy_removal" to FilterMode.PREFER,
                    "mb_device" to FilterMode.CONDITIONAL,
                    "oc_smart" to FilterMode.PREFER
                )
            ),
            colourDirection = "Warm",
            defaultFaceShape = "Diamond",
            metalPreference = "Gold",
            age = 27,
            occupation = "Graphic designer",
            personaSummary = "Right upper-limb difference; sometimes uses a prosthetic forearm. Prefers front-facing closures, easy sleeves and independent dressing. Agent should ask whether the prosthesis is worn that day.",
            exampleRequest = "I have a client presentation tomorrow. I want something creative but professional, and I don't want complicated buttons or difficult sleeves.",
            monthlyBudget = 150.0,
            priceLimit = 85.0
        ),
        // Person 4 — Office male. Assets: sample/person4.
        Profile(
            id = "person4",
            displayName = "Ethan · Office",
            bodyAssetPath = "sample/person4/body.jpg",
            faceAssetPath = "sample/person4/face.jpg",
            builtIn = true,
            preferredGarmentIds = (1..10).map { "p4_${it.toString().padStart(2, '0')}" },
            gender = GenderOptions.MALE,
            postalCode = "10013",
            preferredColors = listOf("Navy", "White", "Black", "Beige", "Muted brown"),
            avoidColors = emptyList(),
            preferredStyles = listOf("business casual", "clean", "sophisticated", "minimal", "easy-care"),
            interests = listOf("long office days", "commute walking", "laptop backpack", "dinner after work", "air-conditioned offices"),
            wearabilitySeed = WearabilitySeed(
                easyFootwear = true,
                seatedComfort = true,
                conserveEnergy = true,
                oneHandClosure = false,
                filterIds = listOf(
                    "mt_easy_care" to FilterMode.PREFER,
                    "oc_work" to FilterMode.PREFER,
                    "oc_whole_day" to FilterMode.PREFER,
                    "oc_dinner" to FilterMode.PREFER,
                    "clm_light_layers" to FilterMode.PREFER,
                    "ft_easy" to FilterMode.PREFER,
                    "sz_price_aware" to FilterMode.MUST_AVOID,
                    "se_seated" to FilterMode.PREFER
                )
            ),
            colourDirection = "Cool",
            defaultFaceShape = "Square",
            metalPreference = "Silver",
            age = 29,
            occupation = "Business analyst",
            personaSummary = "Five-day office schedule, ~25 min walking commute, air-conditioned workplace. Prefers polished business casual and easy-care fabrics. Monthly clothing budget about $100.",
            exampleRequest = "I have work tomorrow, followed by dinner with colleagues. Check the weather and plan something I can wear throughout the day without needing a complete change.",
            monthlyBudget = 100.0,
            priceLimit = 100.0
        ),
        // Person 5 — Modest Amara. Assets: sample/person5.
        Profile(
            id = "person5",
            displayName = "Amara · Modest",
            bodyAssetPath = "sample/person5/body.jpg",
            faceAssetPath = "sample/person5/face.jpg",
            builtIn = true,
            preferredGarmentIds = (1..10).map { "p5_${it.toString().padStart(2, '0')}" },
            gender = GenderOptions.FEMALE,
            postalCode = "33130", // warm / humid Miami
            preferredColors = listOf("Emerald", "Burgundy", "Cream", "Pastels"),
            avoidColors = emptyList(),
            preferredStyles = listOf("modest", "elegant", "contemporary", "loose silhouette", "coverage", "breathable"),
            interests = listOf("lectures", "family gatherings", "cultural celebrations", "coordinated jewellery", "heat-ready layers"),
            wearabilitySeed = WearabilitySeed(
                heatSensitive = true,
                easyRemoval = true,
                seatedComfort = true,
                easyFootwear = true,
                filterIds = listOf(
                    "mt_breathable" to FilterMode.PREFER,
                    "clm_heat" to FilterMode.MUST_AVOID,
                    "clm_light_layers" to FilterMode.PREFER,
                    "clm_uv_cover" to FilterMode.PREFER,
                    "oc_work" to FilterMode.PREFER,
                    "oc_dinner" to FilterMode.PREFER,
                    "oc_whole_day" to FilterMode.PREFER,
                    "gp_easy_jewellery_clasps" to FilterMode.PREFER
                )
            ),
            colourDirection = "Warm",
            defaultFaceShape = "Heart",
            metalPreference = "Gold",
            age = 26,
            occupation = "University lecturer",
            personaSummary = "Prefers coverage of shoulders, chest and knees; loose flowing silhouettes; breathable cotton/linen. Warm humid climate. Stated personal coverage choices — no religion or culture inferred.",
            exampleRequest = "I have lectures tomorrow and a family celebration in the evening. I want something modest and elegant, but it will be quite hot outside.",
            monthlyBudget = 160.0,
            priceLimit = 80.0
        ),
        // Person 6 — Budget lavender. Assets: sample/person6. Women + unisex capsule.
        Profile(
            id = "person6",
            displayName = "Maya · Budget",
            bodyAssetPath = "sample/person6/body.jpg",
            faceAssetPath = "sample/person6/face.jpg",
            builtIn = true,
            preferredGarmentIds = (1..10).map { "p6_${it.toString().padStart(2, '0')}" },
            gender = GenderOptions.FEMALE,
            postalCode = "80202",
            preferredColors = listOf("Black", "White", "Lavender", "Bright blue"),
            avoidColors = emptyList(),
            preferredStyles = listOf("casual", "sporty", "sustainable", "oversized", "versatile", "secondhand"),
            interests = listOf("reuse wardrobe first", "cycling", "walking", "machine-washable", "smart-casual work shift"),
            wearabilitySeed = WearabilitySeed(
                easyFootwear = true,
                conserveEnergy = true,
                seatedComfort = true,
                easyRemoval = true,
                filterIds = listOf(
                    "sz_price_aware" to FilterMode.MUST_AVOID,
                    "mt_easy_care" to FilterMode.PREFER,
                    "oc_casual" to FilterMode.PREFER,
                    "oc_whole_day" to FilterMode.PREFER,
                    "oc_smart" to FilterMode.CONDITIONAL,
                    "ft_easy" to FilterMode.PREFER,
                    "clm_conserve_energy" to FilterMode.PREFER
                )
            ),
            colourDirection = "Cool",
            defaultFaceShape = "Oval",
            metalPreference = "Silver",
            age = 23,
            occupation = "Part-time employee and postgraduate student",
            personaSummary = "Limited $50 monthly clothing budget. Prefers reusing existing pieces, oversized tees, jeans, hoodies and sneakers. Versatile small wardrobe over new purchases.",
            exampleRequest = "I need something for university and my part-time job tomorrow. Don't recommend buying anything unless I genuinely need it. Use what I already own.",
            monthlyBudget = 50.0,
            priceLimit = 50.0
        )
    )

    fun activeId(context: Context): String =
        LocalProfileStore.activeProfileId(context)?.takeIf { it.isNotBlank() } ?: "person1"

    fun saveActiveId(context: Context, id: String) {
        LocalProfileStore.saveActiveProfileId(context, id)
    }

    fun customProfiles(context: Context): List<Profile> {
        val raw = prefs(context).getString(KEY_CUSTOM, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                Profile(
                    id = o.optString("id"),
                    displayName = o.optString("displayName").ifBlank { "Custom" },
                    bodyFileName = o.optString("bodyFileName").takeIf { it.isNotBlank() },
                    faceFileName = o.optString("faceFileName").takeIf { it.isNotBlank() },
                    builtIn = false,
                    preferredGarmentIds = emptyList(),
                    gender = o.optString("gender").ifBlank { GenderOptions.PREFER_NOT },
                    postalCode = o.optString("postalCode").ifBlank { "11217" },
                    preferredColors = o.optJSONArray("preferredColors")?.toStringList().orEmpty(),
                    preferredStyles = o.optJSONArray("preferredStyles")?.toStringList().orEmpty(),
                    interests = o.optJSONArray("interests")?.toStringList().orEmpty()
                ).takeIf { it.id.isNotBlank() }
            }
        }.getOrDefault(emptyList())
    }

    fun allProfiles(context: Context): List<Profile> {
        val raw = builtIn + customProfiles(context)
        return raw.map { p ->
            val custom = LocalProfileStore.displayNameForProfile(context, p.id)
            if (!custom.isNullOrBlank()) {
                val tag = p.displayName.substringAfter(" ·", "")
                val newDisplay = if (tag.isNotBlank() && tag != p.displayName) "$custom · $tag" else custom
                p.copy(displayName = newDisplay)
            } else {
                p
            }
        }
    }

    fun find(context: Context, id: String): Profile? =
        allProfiles(context).firstOrNull { it.id == id }

    fun activeProfile(context: Context): Profile =
        find(context, activeId(context)) ?: builtIn.first()

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, 0)

    private fun profilesDir(context: Context): File =
        File(context.filesDir, "profiles").also { it.mkdirs() }

    private fun JSONArray.toStringList(): List<String> =
        (0 until length()).mapNotNull { optString(it).takeIf { s -> s.isNotBlank() } }

    /**
     * Write this person's personalization world into LocalProfileStore so Discover,
     * ConstraintSolver, Day, Home, and Loomie all see distinct prefs.
     */
    fun applyWorld(context: Context, profile: Profile, priorWearability: WearabilityPreferences? = null) {
        LocalProfileStore.saveGender(context, profile.gender)
        LocalProfileStore.savePostalCode(context, profile.postalCode)
        LocalProfileStore.savePreferredColors(context, profile.preferredColors)
        LocalProfileStore.savePreferredStyles(context, profile.preferredStyles)
        LocalProfileStore.saveInterests(context, profile.interests)
        LocalProfileStore.saveAvoidColors(context, profile.avoidColors)
        LocalProfileStore.saveAge(context, profile.age)
        LocalProfileStore.saveOccupation(context, profile.occupation)
        LocalProfileStore.savePersonaSummary(context, profile.personaSummary)
        LocalProfileStore.saveExampleRequest(context, profile.exampleRequest)
        LocalProfileStore.savePersonaMeta(
            context,
            age = profile.age,
            occupation = profile.occupation,
            summary = profile.personaSummary,
            dayNotes = listOfNotNull(
                profile.age?.let { "Age $it" },
                profile.occupation.takeIf { it.isNotBlank() },
                profile.personaSummary.takeIf { it.isNotBlank() }?.take(160)
            ),
            loomiePrompts = listOfNotNull(profile.exampleRequest.takeIf { it.isNotBlank() })
        )
        LocalProfileStore.saveStyleChoice(context, "faceShape", profile.defaultFaceShape)
        LocalProfileStore.saveStyleChoice(context, "colourDirection", profile.colourDirection)
        LocalProfileStore.saveStyleChoice(context, "metal", profile.metalPreference)
        if (!LocalProfileStore.hasScopedWearabilitySeed(context)) {
            val prefsSeed = profile.wearabilitySeed.toPreferences(priorWearability ?: WearabilityPreferences())
            LocalProfileStore.saveWearabilitySeed(context, prefsSeed)
        }
        profile.monthlyBudget?.let { limit ->
            val existing = LocalProfileStore.loadBudget(context)
            LocalProfileStore.saveBudget(context, existing.copy(monthlyLimit = limit))
        }
        LocalProfileStore.savePriceLimit(context, profile.priceLimit)
    }

    /**
     * Activate a built-in or custom profile: copy body+face into the active
     * LocalProfileStore slots used by Fit / Face / Today, and seed their world.
     */
    fun activate(context: Context, profileId: String): Boolean {
        val profile = find(context, profileId) ?: return false
        val priorKey = TryOnCompositeCache.personKey(context, LocalProfileStore.bodyUri(context))
        val priorSource = LocalProfileStore.bodySource(context)

        val bodyUri = when {
            profile.builtIn && !profile.bodyAssetPath.isNullOrBlank() ->
                copyAssetFile(context, profile.bodyAssetPath, File(context.filesDir, "body.jpg"))
            !profile.bodyFileName.isNullOrBlank() -> {
                val src = File(profilesDir(context), profile.bodyFileName)
                if (!src.exists()) null
                else copyFile(src, File(context.filesDir, "body.jpg"))
            }
            else -> null
        } ?: return false

        val faceUri = when {
            profile.builtIn && !profile.faceAssetPath.isNullOrBlank() ->
                copyAssetFile(context, profile.faceAssetPath, File(context.filesDir, "face.jpg"))
            !profile.faceFileName.isNullOrBlank() -> {
                val src = File(profilesDir(context), profile.faceFileName)
                if (!src.exists()) null
                else copyFile(src, File(context.filesDir, "face.jpg"))
            }
            else -> null
        }

        LocalProfileStore.saveBodyUri(context, bodyUri)
        if (profile.builtIn) {
            LocalProfileStore.saveBodySource(context, profile.id)
        } else {
            LocalProfileStore.saveBodySource(context, null)
        }
        faceUri?.let { LocalProfileStore.saveFaceUri(context, it) }
        // Identity must switch before saving the display name; otherwise the outgoing
        // profile receives the incoming profile's name.
        saveActiveId(context, profile.id)
        val customName = LocalProfileStore.displayNameForProfile(context, profile.id)
        val nameToSave = customName ?: profile.displayName.substringBefore(" ·").trim()
        if (nameToSave.isNotBlank()) {
            LocalProfileStore.saveDisplayName(context, nameToSave)
        }
        applyWorld(context, profile)
        seedAppliedLooks(context, profile)

        // Don't wipe cache when re-selecting the same built-in person.
        if (priorSource != profile.id) {
            TryOnCompositeCache.invalidatePerson(context, priorKey)
            if (!priorSource.isNullOrBlank() && priorSource != profile.id) {
                TryOnCompositeCache.invalidatePerson(context, priorSource)
            }
        }
        return true
    }

    /**
     * Seed 2–3 AppliedLooks from this persona's preferredGarmentIds so Today
     * has saved looks immediately after activate (per-profile scoped store).
     */
    fun seedAppliedLooks(context: Context, profile: Profile) {
        val day = LocalDate.now().toEpochDay()
        val looks = when (profile.id) {
            "person1" -> listOf(
                LocalProfileStore.AppliedLook(
                    id = "seed-person1-1",
                    title = "Relaxed knit day",
                    garmentIds = listOf("p1_01", "p1_08", "p1_05"),
                    occasion = "Today",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person1-2",
                    title = "Cotton tee alternate",
                    garmentIds = listOf("p1_02", "p1_09", "p1_05"),
                    occasion = "Casual",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person1-3",
                    title = "Layered street style",
                    garmentIds = listOf("p1_06", "p1_04", "p1_09"),
                    occasion = "Evening",
                    savedAtEpochDay = day
                )
            )
            "person2" -> listOf(
                LocalProfileStore.AppliedLook(
                    id = "seed-person2-1",
                    title = "Smart casual day",
                    garmentIds = listOf("p2_03", "p2_04", "p2_01"),
                    occasion = "Today",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person2-2",
                    title = "Low-strain alternate",
                    garmentIds = listOf("p2_06", "p2_05", "p2_07"),
                    occasion = "Casual",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person2-3",
                    title = "Polished trench layer",
                    garmentIds = listOf("p2_02", "p2_09", "p2_04"),
                    occasion = "Work",
                    savedAtEpochDay = day
                )
            )
            "person3" -> listOf(
                LocalProfileStore.AppliedLook(
                    id = "seed-person3-1",
                    title = "Minimalist day",
                    garmentIds = listOf("p3_05", "p3_08", "p3_04"),
                    occasion = "Today",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person3-2",
                    title = "Mustard streetwear",
                    garmentIds = listOf("p3_02", "p3_09", "p3_04"),
                    occasion = "Casual",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person3-3",
                    title = "Easy zip utility",
                    garmentIds = listOf("p3_01", "p3_05", "p3_08"),
                    occasion = "Presentation",
                    savedAtEpochDay = day
                )
            )
            "person4" -> listOf(
                LocalProfileStore.AppliedLook(
                    id = "seed-person4-1",
                    title = "Executive blazer",
                    garmentIds = listOf("p4_01", "p4_03", "p4_10", "p4_08"),
                    occasion = "Today",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person4-2",
                    title = "Smart polo commute",
                    garmentIds = listOf("p4_02", "p4_09", "p4_08"),
                    occasion = "Casual",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person4-3",
                    title = "Camel overcoat",
                    garmentIds = listOf("p4_04", "p4_07", "p4_09"),
                    occasion = "Dinner",
                    savedAtEpochDay = day
                )
            )
            "person5" -> listOf(
                LocalProfileStore.AppliedLook(
                    id = "seed-person5-1",
                    title = "Emerald flowing abaya",
                    garmentIds = listOf("p5_01", "p5_04"),
                    occasion = "Today",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person5-2",
                    title = "Shoulder poncho polish",
                    garmentIds = listOf("p5_02", "p5_03"),
                    occasion = "Lectures",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person5-3",
                    title = "Burgundy modest dress",
                    garmentIds = listOf("p5_06", "p5_10"),
                    occasion = "Celebration",
                    savedAtEpochDay = day
                )
            )
            "person6" -> listOf(
                LocalProfileStore.AppliedLook(
                    id = "seed-person6-1",
                    title = "Campus graphic tee",
                    garmentIds = listOf("p6_02", "p6_03", "p6_05"),
                    occasion = "Today",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person6-2",
                    title = "Lavender hoodie relaxed",
                    garmentIds = listOf("p6_01", "p6_07", "p6_09"),
                    occasion = "Campus",
                    savedAtEpochDay = day
                ),
                LocalProfileStore.AppliedLook(
                    id = "seed-person6-3",
                    title = "Minimal streetwear",
                    garmentIds = listOf("p6_04", "p6_10", "p6_05"),
                    occasion = "Work shift",
                    savedAtEpochDay = day
                )
            )
            else -> emptyList()
        }
        if (looks.isNotEmpty()) LocalProfileStore.replaceAppliedLooks(context, looks)
    }

    /** Ensure a built-in profile is active when stores are empty. */
    fun seedDefaultIfNeeded(context: Context, force: Boolean = false): Boolean {
        val repairedNames = LocalProfileStore.repairLegacyBuiltInNameLeak(context)
        val storedVersion = prefs(context).getInt(KEY_WORLD_VERSION, 0)
        val needsWorldBump = storedVersion < WORLD_VERSION
        val bodyMissing = LocalProfileStore.bodyUri(context).isNullOrBlank() ||
            LocalProfileStore.bodyUri(context)?.let { uri ->
                File(Uri.parse(uri).path ?: "").exists().not()
            } == true
        if (!force && !needsWorldBump && !bodyMissing && LocalProfileStore.bodySource(context) != null) {
            // Still refresh face if missing; re-apply world so gender/zip stay seeded.
            val faceMissing = LocalProfileStore.faceUri(context).isNullOrBlank() ||
                LocalProfileStore.faceUri(context)?.let { uri ->
                    File(Uri.parse(uri).path ?: "").exists().not()
                } == true
            val active = activeProfile(context)
            if (LocalProfileStore.genderPreset(context).isBlank()) applyWorld(context, active)
            if (LocalProfileStore.postalCode(context).isBlank()) {
                LocalProfileStore.savePostalCode(context, active.postalCode)
            }
            if (repairedNames) return activate(context, active.id)
            if (!faceMissing) return false
        }
        val id = if (force || bodyMissing || needsWorldBump) {
            activeId(context).takeIf { find(context, it) != null } ?: "person1"
        } else {
            LocalProfileStore.bodySource(context) ?: activeId(context)
        }
        val ok = activate(context, id)
        if (ok) prefs(context).edit().putInt(KEY_WORLD_VERSION, WORLD_VERSION).apply()
        return ok
    }

    /**
     * Persist a custom profile from gallery URIs. Does not replace the 6 built-ins.
     * Returns the new profile id.
     */
    fun addCustomProfile(
        context: Context,
        displayName: String,
        bodyUri: Uri,
        faceUri: Uri?
    ): String? {
        val id = "custom-${UUID.randomUUID().toString().take(8)}"
        val bodyName = "$id-body.jpg"
        val faceName = "$id-face.jpg"
        val bodyDest = File(profilesDir(context), bodyName)
        val savedBody = copyUriAsJpeg(context, bodyUri, bodyDest) ?: return null
        var savedFaceName: String? = null
        if (faceUri != null) {
            val faceDest = File(profilesDir(context), faceName)
            if (copyUriAsJpeg(context, faceUri, faceDest) != null) savedFaceName = faceName
        } else {
            // Reuse body as face thumbnail when only one photo is provided.
            copyFile(bodyDest, File(profilesDir(context), faceName))
            savedFaceName = faceName
        }
        val next = customProfiles(context) + Profile(
            id = id,
            displayName = displayName.ifBlank { "Custom profile" },
            bodyFileName = bodyName,
            faceFileName = savedFaceName,
            builtIn = false
        )
        saveCustomList(context, next)
        // Activate immediately for testing.
        activate(context, id)
        return id.also { savedBody }
    }

    fun deleteCustomProfile(context: Context, profileId: String): Boolean {
        if (builtIn.any { it.id == profileId }) return false
        val existing = customProfiles(context)
        val target = existing.firstOrNull { it.id == profileId } ?: return false
        target.bodyFileName?.let { runCatching { File(profilesDir(context), it).delete() } }
        target.faceFileName?.let { runCatching { File(profilesDir(context), it).delete() } }
        saveCustomList(context, existing.filterNot { it.id == profileId })
        if (activeId(context) == profileId) {
            activate(context, "person1")
        }
        return true
    }

    fun wardrobeForActiveProfile(context: Context): List<com.vizeno.loomie.model.Garment> {
        val profile = activeProfile(context)
        val gender = LocalProfileStore.genderLabel(context) ?: profile.gender
        val all = LookWeaveRepository.allGarments.filter { it.audience.matchesGenderLabel(gender) }
        if (profile.preferredGarmentIds.isEmpty()) return all
        val preferred = profile.preferredGarmentIds.mapNotNull { id -> all.firstOrNull { it.id == id } }
        val rest = all.filter { g -> g.id !in profile.preferredGarmentIds }
        // Prefer capsule first, then remaining gender-appropriate pieces.
        return preferred + rest
    }

    private fun saveCustomList(context: Context, profiles: List<Profile>) {
        val arr = JSONArray(profiles.map { p ->
            JSONObject()
                .put("id", p.id)
                .put("displayName", p.displayName)
                .put("bodyFileName", p.bodyFileName)
                .put("faceFileName", p.faceFileName)
                .put("gender", p.gender)
                .put("postalCode", p.postalCode)
                .put("preferredColors", JSONArray(p.preferredColors))
                .put("preferredStyles", JSONArray(p.preferredStyles))
                .put("interests", JSONArray(p.interests))
        })
        prefs(context).edit().putString(KEY_CUSTOM, arr.toString()).apply()
    }

    private fun copyAssetFile(context: Context, assetPath: String, dest: File): String? = runCatching {
        context.assets.open(assetPath).use { input ->
            FileOutputStream(dest).use { output -> input.copyTo(output) }
        }
        if (!dest.exists() || dest.length() <= 0) return null
        Uri.fromFile(dest).toString()
    }.getOrNull()

    private fun copyFile(src: File, dest: File): String? = runCatching {
        src.inputStream().use { input -> FileOutputStream(dest).use { output -> input.copyTo(output) } }
        if (!dest.exists() || dest.length() <= 0) return null
        Uri.fromFile(dest).toString()
    }.getOrNull()

    private fun copyUriAsJpeg(context: Context, sourceUri: Uri, dest: File): String? {
        val bitmap = context.contentResolver.openInputStream(sourceUri)?.use { input ->
            BitmapFactory.decodeStream(input)
        } ?: return null
        FileOutputStream(dest).use { output ->
            if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 92, output)) return null
        }
        if (!bitmap.isRecycled) bitmap.recycle()
        return Uri.fromFile(dest).toString()
    }
}
