package com.vizeno.loomie.data

import androidx.compose.ui.graphics.Color
import com.vizeno.loomie.model.Retailer
import com.vizeno.loomie.model.NearbyPlace
import com.vizeno.loomie.model.WearabilityPreferences

object RetailerDirectory {
    const val EASY_CLOSURE = "Easy closure"
    const val SEATED = "Seated wear"
    const val SENSORY = "Low sensory"
    const val FOOTWEAR = "Easy footwear"
    const val DEVICE_ACCESS = "Device access"
    const val ASSISTED = "Assisted dressing"
    const val EXTENDED_SIZE = "Extended sizing"
    const val KIDS = "Kids"

    const val ANIMAL_FREE = "Animal-free"
    const val HEAT_AWARE = "Heat-aware"

    val filters = listOf(EASY_CLOSURE, SEATED, SENSORY, FOOTWEAR, DEVICE_ACCESS, ASSISTED, EXTENDED_SIZE, KIDS, ANIMAL_FREE, HEAT_AWARE)

    val retailers = listOf(
        Retailer(
            "tommy", "Tommy Adaptive", "Classic apparel with discreet adaptations", "US · online",
            "Mainstream tailoring with magnetic closures, one-handed zippers, adjustable hems and seated-wear construction.",
            setOf(EASY_CLOSURE, SEATED, SENSORY, DEVICE_ACCESS, KIDS),
            listOf("Magnetic button shirts", "Seated-wear denim", "One-hand zipper layers"),
            "https://usa.tommy.com/en/tommy-adaptive", Color(0xFF263E56)
        ),
        Retailer(
            "zappos", "Zappos Adaptive", "Multi-brand adaptive marketplace", "US · online",
            "A broad destination for easy-on footwear, AFO-friendly options, adaptive apparel and multiple width choices.",
            setOf(FOOTWEAR, EASY_CLOSURE, DEVICE_ACCESS, EXTENDED_SIZE, KIDS),
            listOf("AFO-friendly footwear", "Easy-on sneakers", "Wide and extra-wide shoes"),
            "https://www.zappos.com/c/adaptive", Color(0xFF315D85)
        ),
        Retailer(
            "nike", "Nike EasyOn", "Sport footwear with fewer dressing steps", "Global · online + stores",
            "Athletic footwear designed to go on and off quickly, informed by athletes, families and the disability community.",
            setOf(FOOTWEAR, EASY_CLOSURE, KIDS),
            listOf("Hands-free shoes", "Easy-entry sneakers", "Adult and kids styles"),
            "https://www.nike.com/easyon", Color(0xFF292A27)
        ),
        Retailer(
            "billy", "BILLY Footwear", "Universal-design zipper shoes", "US · online + stockists",
            "Wraparound zipper construction opens the upper widely for step-in access, with removable inserts on selected lines.",
            setOf(FOOTWEAR, EASY_CLOSURE, SENSORY, KIDS),
            listOf("Wraparound zipper sneakers", "Easy-grab zipper loops", "Customizable insert systems"),
            "https://billyfootwear.com", Color(0xFF47706B)
        ),
        Retailer(
            "friendly", "Friendly Shoes", "Clinician-founded accessible footwear", "US · online",
            "Easy Shoe Access zipper systems create a larger entry portal while retaining supportive, everyday silhouettes.",
            setOf(FOOTWEAR, EASY_CLOSURE, SENSORY, DEVICE_ACCESS, KIDS),
            listOf("Rear-zip wide sneakers", "Removable supportive footbeds", "Smooth sensory-friendly linings"),
            "https://friendlyshoes.com/adaptive-footwear-shop", Color(0xFF3D6570)
        ),
        Retailer(
            "kizik", "Kizik", "Hands-free everyday footwear", "Global · online + stores",
            "Step-in casual, sport and work silhouettes with a structured heel system that rebounds after entry.",
            setOf(FOOTWEAR, EASY_CLOSURE, EXTENDED_SIZE),
            listOf("Hands-free lifestyle shoes", "Hands-free sport shoes", "Slip-resistant work styles"),
            "https://kizik.com/collections/shop-all-kizik", Color(0xFF5C4B42)
        ),
        Retailer(
            "unhidden", "Unhidden", "Fashion-led seated design", "UK · worldwide online",
            "Purpose-built seated pieces use longer trouser seats and hi-lo tops for coverage and comfort in a chair.",
            setOf(SEATED, EASY_CLOSURE, DEVICE_ACCESS, ANIMAL_FREE, HEAT_AWARE),
            listOf("Seated wrap trousers", "Hi-lo shirts", "Jersey culottes"),
            "https://unhidden.com/collections/seated", Color(0xFF75564B)
        ),
        Retailer(
            "iz", "IZ Adaptive", "Designer-led seated wardrobe", "Canada + US · online",
            "Timeless clothing cut around a seated frame, with reduced bunching and ease-of-dress details.",
            setOf(SEATED, EASY_CLOSURE, DEVICE_ACCESS, EXTENDED_SIZE),
            listOf("Seamless-back pants", "Seated coats", "Adaptive denim"),
            "https://izadaptive.com", Color(0xFF3D4C5C)
        ),
        Retailer(
            "slick", "Slick Chicks", "Accessible intimates and lounge layers", "US · online",
            "Independent- and assisted-dressing intimates with side openings, front fastenings and flexible extended-size options.",
            setOf(EASY_CLOSURE, SEATED, ASSISTED, EXTENDED_SIZE),
            listOf("Side-fastening underwear", "Front-fastening bras", "Side-zip joggers"),
            "https://slickchicksonline.com", Color(0xFF8B5363)
        ),
        Retailer(
            "silverts", "Silverts", "Deep adaptive feature catalog", "US + Canada · online",
            "A wide catalog organized around magnetic, open-back, side-opening, pull-on and assisted-dressing needs.",
            setOf(EASY_CLOSURE, SEATED, ASSISTED, FOOTWEAR, DEVICE_ACCESS, EXTENDED_SIZE),
            listOf("Open-back tops", "Side-opening trousers", "Easy-touch footwear"),
            "https://www.silverts.com/pages/shop-by-feature", Color(0xFF58634E)
        ),
        Retailer(
            "kohls", "Kohl’s Adaptive", "Everyday multi-category assortment", "US · online + stores",
            "Accessible everyday styles spanning closures, sensory construction, abdominal access and seated comfort.",
            setOf(EASY_CLOSURE, SEATED, SENSORY, DEVICE_ACCESS, FOOTWEAR, KIDS),
            listOf("Sensory-friendly layers", "Adaptive activewear", "Abdominal-access clothing"),
            "https://www.kohls.com/catalog/adaptive.jsp?CN=Feature%3AAdaptive", Color(0xFF6E4D54)
        ),
        Retailer(
            "jcpenney", "JCPenney Adaptive", "Accessible wardrobe essentials", "US · online + stores",
            "Women’s and men’s lines include seated cuts, flat seams, hook-and-loop fasteners, pull loops and access openings.",
            setOf(EASY_CLOSURE, SEATED, SENSORY, DEVICE_ACCESS, ASSISTED, EXTENDED_SIZE),
            listOf("Seated jeans", "Hook-and-loop shirts", "Adaptive intimates"),
            "https://www.jcpenney.com/g/shops/adaptive-clothing-accessories", Color(0xFF71433E)
        ),
        Retailer(
            "target", "Target Adaptive", "Sensory-aware family essentials", "US · online + stores",
            "Kids-focused functional clothing with flat seams, tag-free construction, abdominal access and side openings.",
            setOf(SENSORY, DEVICE_ACCESS, EASY_CLOSURE, KIDS),
            listOf("Tag-free basics", "Abdominal-access tops", "Side-opening styles"),
            "https://www.target.com/c/kids-adaptive-clothing/-/N-1laue", Color(0xFF9B423E)
        ),
        Retailer(
            "ablelabel", "The Able Label", "Easy-dressing wardrobe and nightwear", "UK · worldwide online",
            "A large adaptive range centered on touch fastenings, pull-on construction, open backs and easy-care fabrics.",
            setOf(EASY_CLOSURE, ASSISTED, SEATED, EXTENDED_SIZE),
            listOf("Touch-fastening shirts", "Open-back nightwear", "Pull-on trousers"),
            "https://www.theablelabel.com/collections/adaptive-clothing", Color(0xFF7C6951)
        ),
        Retailer(
            "house", "House of Adaptive", "Multi-brand adaptive showroom", "Canada · showroom + online",
            "A Toronto-based destination bringing designer-led adaptive clothing, footwear and daily-living pieces together.",
            setOf(EASY_CLOSURE, SEATED, FOOTWEAR, DEVICE_ACCESS),
            listOf("IZ Adaptive collections", "Adaptive footwear", "Seated wardrobe pieces"),
            "https://houseofadaptive.com", Color(0xFF4F5666)
        )
    )

    val nearbyPlaces = listOf(
        NearbyPlace(
            "target-atlantic", "target", "Target Atlantic Terminal", "Fort Greene", "139 Flatbush Ave, Brooklyn",
            1.2, 40.6842, -73.9777, 96, "Strong match for sensory-aware basics and easy closures.",
            listOf(SENSORY, EASY_CLOSURE, KIDS), "https://www.google.com/maps/dir/?api=1&destination=139+Flatbush+Ave+Brooklyn+NY", Color(0xFF60786F)
        ),
        NearbyPlace(
            "target-fulton", "target", "Target Brooklyn Fulton St", "Downtown Brooklyn", "445 Albee Square W, Brooklyn",
            1.8, 40.6911, -73.9826, 93, "Useful nearby stop for tag-free layers and pickup.",
            listOf(SENSORY, DEVICE_ACCESS, KIDS), "https://www.google.com/maps/dir/?api=1&destination=445+Albee+Square+W+Brooklyn+NY", Color(0xFF71877E)
        ),
        NearbyPlace(
            "target-soho", "target", "Target Manhattan SoHo", "SoHo", "600 Broadway, New York",
            3.4, 40.7240, -73.9968, 91, "A central option for adaptive family essentials and pickup.",
            listOf(SENSORY, EASY_CLOSURE, DEVICE_ACCESS), "https://www.google.com/maps/dir/?api=1&destination=600+Broadway+New+York+NY", Color(0xFF4D665E)
        ),
        NearbyPlace(
            "nike-hoi", "nike", "Nike House of Innovation", "Midtown", "650 Fifth Ave, New York",
            6.1, 40.7597, -73.9762, 89, "Best match for hands-free and easy-entry sport footwear.",
            listOf(FOOTWEAR, EASY_CLOSURE), "https://www.google.com/maps/dir/?api=1&destination=650+5th+Ave+New+York+NY", Color(0xFF242625)
        ),
        NearbyPlace(
            "macys-herald", "macys", "Macy's Herald Square", "Herald Square", "151 W 34th St, New York",
            5.3, 40.7508, -73.9895, 84, "Broad size and footwear departments make comparison easier.",
            listOf(EXTENDED_SIZE, FOOTWEAR), "https://www.google.com/maps/dir/?api=1&destination=151+W+34th+St+New+York+NY", Color(0xFF7B4C51)
        )
    )

    fun wantedFeatures(preferences: WearabilityPreferences): Set<String> = buildSet {
        if (preferences.oneHandClosure) add(EASY_CLOSURE)
        if (preferences.seatedComfort) add(SEATED)
        if (preferences.lowSensoryLoad) add(SENSORY)
        if (preferences.easyFootwear) add(FOOTWEAR)
        if (preferences.deviceClearance) add(DEVICE_ACCESS)
        if (preferences.easyRemoval || preferences.limitedOverheadReach || preferences.gripStrength) add(ASSISTED)
        if (preferences.mobilityAidCompatibility) { add(SEATED); add(DEVICE_ACCESS) }
        preferences.customRules.filter { it.enabled && it.confirmed }.forEach { rule ->
            when (rule.key) {
                com.vizeno.loomie.model.WearabilityRuleKey.ONE_HAND_CLOSURE,
                com.vizeno.loomie.model.WearabilityRuleKey.GRIP_STRENGTH -> add(EASY_CLOSURE)
                com.vizeno.loomie.model.WearabilityRuleKey.SEATED_COMFORT -> add(SEATED)
                com.vizeno.loomie.model.WearabilityRuleKey.LOW_SENSORY_LOAD,
                com.vizeno.loomie.model.WearabilityRuleKey.SEAM_PRESSURE_SENSITIVITY -> add(SENSORY)
                com.vizeno.loomie.model.WearabilityRuleKey.EASY_FOOTWEAR -> add(FOOTWEAR)
                com.vizeno.loomie.model.WearabilityRuleKey.DEVICE_CLEARANCE,
                com.vizeno.loomie.model.WearabilityRuleKey.MOBILITY_AID_COMPATIBILITY -> add(DEVICE_ACCESS)
                com.vizeno.loomie.model.WearabilityRuleKey.EASY_REMOVAL,
                com.vizeno.loomie.model.WearabilityRuleKey.LIMITED_OVERHEAD_REACH -> add(ASSISTED)
                else -> Unit
            }
        }
    }

    fun isOnline(retailer: Retailer) = retailer.region.contains("online", ignoreCase = true)

    fun matchesPreferences(retailer: Retailer, preferences: WearabilityPreferences): Boolean {
        val wanted = wantedFeatures(preferences)
        val custom = preferences.customRules.filter { it.enabled && it.confirmed }
        if (wanted.isEmpty() && custom.isEmpty()) return true
        val featureHit = retailer.features.any { it in wanted }
        val customHit = custom.any { rule ->
            val label = rule.label.lowercase()
            retailer.name.lowercase().contains(label) || retailer.description.lowercase().contains(label) ||
                retailer.features.any { label.contains(it.lowercase()) } || retailer.notableFinds.any { it.lowercase().contains(label) }
        }
        return featureHit || customHit
    }
}
