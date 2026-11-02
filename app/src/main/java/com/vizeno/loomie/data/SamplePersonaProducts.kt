package com.vizeno.loomie.data

import com.vizeno.loomie.integration.ProductCard

/**
 * Curated, prebaked live-style product deck items tailored specifically for each
 * of the 6 sample personas (Person 1..6) reflecting their exact accessibility priorities,
 * style preferences, gender, and home locations.
 *
 * These items load instantly with 0ms latency from local drawables (drawable://garment_pX_YY)
 * and provide a seamless, rich Tinder-style discovery experience even before or without
 * a live gateway network round.
 */
object SamplePersonaProducts {

    private val liveCache = mutableMapOf<String, List<ProductCard>>()

    fun getCached(profileId: String, category: String? = null): List<ProductCard>? {
        val key = "${profileId}_${category ?: "all"}"
        return liveCache[key]
    }

    fun cache(profileId: String, category: String? = null, items: List<ProductCard>) {
        if (items.isNotEmpty()) {
            val key = "${profileId}_${category ?: "all"}"
            liveCache[key] = items
        }
    }

    fun getOrPrebaked(profileId: String, category: String? = null): List<ProductCard> {
        return getCached(profileId, category) ?: prebakedProductsFor(profileId, category)
    }

    fun prebakedProductsFor(profileId: String, categoryFilter: String? = null): List<ProductCard> {
        val all = when (profileId) {
            "person1" -> person1Products
            "person2" -> person2Products
            "person3" -> person3Products
            "person4" -> person4Products
            "person5" -> person5Products
            "person6" -> person6Products
            else -> person1Products
        }
        if (categoryFilter.isNullOrBlank() || categoryFilter.equals("all", ignoreCase = true)) {
            return all
        }
        val target = categoryFilter.lowercase()
        return all.filter { card ->
            when (target) {
                "tops" -> card.kind.contains("top", ignoreCase = true)
                "bottoms" -> card.kind.contains("bottom", ignoreCase = true)
                "layers", "outerwear" -> card.kind.contains("layer", ignoreCase = true) || card.kind.contains("outer", ignoreCase = true)
                "shoes" -> card.kind.contains("shoe", ignoreCase = true)
                "accessories", "jewellery", "glasses", "watches", "scarf", "hat" ->
                    card.kind.contains("acc", ignoreCase = true) || card.kind.contains(target, ignoreCase = true)
                else -> true
            }
        }.ifEmpty { all }
    }

    private val person1Products = listOf(
        ProductCard(
            id = "p1_prod_01",
            name = "Cream Knit Seamless Crewneck",
            retailer = "Sensory Comfort Wear · Austin, TX",
            productUrl = "https://lookweave.local/products/p1_01",
            imageUrl = "drawable://garment_p1_01",
            match = 99,
            reason = "Ultra-soft knit with seamless construction and flat stitching to prevent sensory irritation.",
            evidence = "100% fine cotton knit · Zero internal neck tags · Soft ribbed cuffs",
            unknowns = emptyList(),
            source = "Sensory Comfort Wear",
            price = "$42.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p1_prod_02",
            name = "Tagless Soft White Cotton Tee",
            retailer = "Basics Studio",
            productUrl = "https://lookweave.local/products/p1_02",
            imageUrl = "drawable://garment_p1_02",
            match = 98,
            reason = "Pre-washed combed cotton with heat-sealed tagless label for low sensory load.",
            evidence = "100% ring-spun combed cotton · Flatlock side seams",
            unknowns = emptyList(),
            source = "Basics Studio",
            price = "$22.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p1_prod_03",
            name = "Sage Green Heavyweight Relaxed Tee",
            retailer = "Austin Streetwear Co.",
            productUrl = "https://lookweave.local/products/p1_03",
            imageUrl = "drawable://garment_p1_03",
            match = 97,
            reason = "Soft sage green jersey with relaxed neck opening that avoids throat compression.",
            evidence = "Heavyweight organic cotton · Natural enzyme wash for softness",
            unknowns = emptyList(),
            source = "Austin Streetwear Co.",
            price = "$28.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p1_prod_04",
            name = "Muted Navy Cotton Overshirt",
            retailer = "Nordic Minimal",
            productUrl = "https://lookweave.local/products/p1_04",
            imageUrl = "drawable://garment_p1_04",
            match = 96,
            reason = "Breathable cotton overshirt with smooth non-metal buttons and soft brushed interior.",
            evidence = "100% brushed cotton twill · Resin buttons · Relaxed armscye",
            unknowns = emptyList(),
            source = "Nordic Minimal",
            price = "$48.00",
            compatibility = "Verified Match",
            kind = "layers"
        ),
        ProductCard(
            id = "p1_prod_05",
            name = "Olive Field Lightweight Jacket",
            retailer = "Urban Canvas",
            productUrl = "https://lookweave.local/products/p1_05",
            imageUrl = "drawable://garment_p1_05",
            match = 95,
            reason = "Lightweight weather layer with soft lining and smooth zip pull that avoids throat chafing.",
            evidence = "Water-resistant cotton blend · Soft jersey lining · Covered zip flap",
            unknowns = emptyList(),
            source = "Urban Canvas",
            price = "$65.00",
            compatibility = "Verified Match",
            kind = "layers"
        ),
        ProductCard(
            id = "p1_prod_06",
            name = "Stretch Chino Trousers (Beige)",
            retailer = "Everyday Ease",
            productUrl = "https://lookweave.local/products/p1_06",
            imageUrl = "drawable://garment_p1_06",
            match = 98,
            reason = "Elastic-backed comfort waistband with soft pocket bags and zero scratchy inner tags.",
            evidence = "98% cotton, 2% elastane · Smooth pocket lining · Flat waist seam",
            unknowns = emptyList(),
            source = "Everyday Ease",
            price = "$38.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p1_prod_07",
            name = "Black Straight Stretch Denim",
            retailer = "Denim Co.",
            productUrl = "https://lookweave.local/products/p1_07",
            imageUrl = "drawable://garment_p1_07",
            match = 96,
            reason = "Washed stretch denim with smooth flatlocked inner seams and non-pinching fly.",
            evidence = "Comfort stretch denim · Enzyme-softened finish · Tagless waistband",
            unknowns = emptyList(),
            source = "Denim Co.",
            price = "$44.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p1_prod_08",
            name = "Sage Green Fleece Lounge Joggers",
            retailer = "Campus Cozy",
            productUrl = "https://lookweave.local/products/p1_08",
            imageUrl = "drawable://garment_p1_08",
            match = 99,
            reason = "Buttery soft brushed fleece interior in preferred sage green with gentle non-constricting cuffs.",
            evidence = "Brushed fleece · Flat drawcord · Encased soft elastic waist",
            unknowns = emptyList(),
            source = "Campus Cozy",
            price = "$32.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p1_prod_09",
            name = "White Streetwear Low-Top Sneakers",
            retailer = "Footwear Lab",
            productUrl = "https://lookweave.local/products/p1_09",
            imageUrl = "drawable://garment_p1_09",
            match = 97,
            reason = "Cushioned memory foam footbed with wide toe box to minimize sensory pressure points.",
            evidence = "Genuine leather & breathable canvas · Padded heel collar · EVA midsole",
            unknowns = emptyList(),
            source = "Footwear Lab",
            price = "$55.00",
            compatibility = "Verified Match",
            kind = "shoes"
        ),
        ProductCard(
            id = "p1_prod_10",
            name = "Breathable Mesh Knit Slip-On Runners",
            retailer = "Motion Active",
            productUrl = "https://lookweave.local/products/p1_10",
            imageUrl = "drawable://garment_p1_10",
            match = 96,
            reason = "Seamless one-piece knit upper with slip-on collar for fast, frictionless wear.",
            evidence = "Seamless knit upper · Stretch sock collar · Flexible phylon sole",
            unknowns = emptyList(),
            source = "Motion Active",
            price = "$49.00",
            compatibility = "Verified Match",
            kind = "shoes"
        )
    )

    private val person2Products = listOf(
        ProductCard(
            id = "p2_prod_01",
            name = "White Floral Camp Collar Blouse",
            retailer = "Joe & Bella Adaptive · Chicago, IL",
            productUrl = "https://lookweave.local/products/p2_01",
            imageUrl = "drawable://garment_p2_01",
            match = 99,
            reason = "Lightweight flowing cut with easy magnetic buttons ideal for low-effort dressing.",
            evidence = "Rayon challis · Concealed magnetic buttons · Generous armhole",
            unknowns = emptyList(),
            source = "Joe & Bella Adaptive",
            price = "$45.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p2_prod_02",
            name = "Mauve Wide-Leg Stretch Joggers",
            retailer = "Silverts Adaptive",
            productUrl = "https://lookweave.local/products/p2_02",
            imageUrl = "drawable://garment_p2_02",
            match = 99,
            reason = "Wide leg silhouette with generous stretch that easily glides over a knee brace without bunching.",
            evidence = "95% modal, 5% spandex · 18-inch leg opening · Easy-pull elastic waist",
            unknowns = emptyList(),
            source = "Silverts Adaptive",
            price = "$38.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p2_prod_03",
            name = "White Flexible Support Flats",
            retailer = "Zappos Adaptive",
            productUrl = "https://lookweave.local/products/p2_03",
            imageUrl = "drawable://garment_p2_03",
            match = 98,
            reason = "Wide opening with supportive non-slip sole engineered for stable, brace-assisted walking.",
            evidence = "Breathable perforated leather · High-traction rubber sole · Cushioned arch",
            unknowns = emptyList(),
            source = "Zappos Adaptive",
            price = "$52.00",
            compatibility = "Verified Match",
            kind = "shoes"
        ),
        ProductCard(
            id = "p2_prod_04",
            name = "Navy Relaxed Overshirt",
            retailer = "Universal Standard",
            productUrl = "https://lookweave.local/products/p2_04",
            imageUrl = "drawable://garment_p2_04",
            match = 96,
            reason = "Inclusive unisex cut with room through elbows and waist for effortless mobility.",
            evidence = "Stretch cotton twill · Smooth finish · Relaxed boxy drape",
            unknowns = emptyList(),
            source = "Universal Standard",
            price = "$58.00",
            compatibility = "Verified Match",
            kind = "layers"
        ),
        ProductCard(
            id = "p2_prod_05",
            name = "Muted Teal Stretch Blouse",
            retailer = "MagnaReady",
            productUrl = "https://lookweave.local/products/p2_05",
            imageUrl = "drawable://garment_p2_05",
            match = 97,
            reason = "Hidden patented magnetic closures behind faux button placket for one-hand fastening.",
            evidence = "Easy-care modal blend · Strong neodymium magnets · Spread collar",
            unknowns = emptyList(),
            source = "MagnaReady",
            price = "$49.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p2_prod_06",
            name = "Charcoal Easy-Wear Stretch Trousers",
            retailer = "Target Adaptive",
            productUrl = "https://lookweave.local/products/p2_06",
            imageUrl = "drawable://garment_p2_06",
            match = 98,
            reason = "Concealed side-zipper leg openings allow quick access to knee brace adjustments.",
            evidence = "Four-way stretch ponte · 12-inch hidden side zippers · Flat front",
            unknowns = emptyList(),
            source = "Target Adaptive",
            price = "$36.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p2_prod_07",
            name = "Plum Cotton Casual Button-Down",
            retailer = "Tommy Adaptive",
            productUrl = "https://lookweave.local/products/p2_07",
            imageUrl = "drawable://garment_p2_07",
            match = 95,
            reason = "Magnetic closures at placket and cuffs with reinforced pull loops for independent dressing.",
            evidence = "100% breathable cotton · Magnetic button placket · Internal hanger loop",
            unknowns = emptyList(),
            source = "Tommy Adaptive",
            price = "$54.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p2_prod_08",
            name = "Tan Chino Adaptive Pants",
            retailer = "Silverts",
            productUrl = "https://lookweave.local/products/p2_08",
            imageUrl = "drawable://garment_p2_08",
            match = 97,
            reason = "High back rise for seated comfort with elasticized waist and knee brace room.",
            evidence = "Wrinkle-resistant twill · Higher back rise · Generous knee allowance",
            unknowns = emptyList(),
            source = "Silverts",
            price = "$42.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p2_prod_09",
            name = "Navy Walking Sneaker with BOA Dial",
            retailer = "Zappos Adaptive",
            productUrl = "https://lookweave.local/products/p2_09",
            imageUrl = "drawable://garment_p2_09",
            match = 96,
            reason = "Single dial closure eliminates lace tying while securing foot firmly for knee alignment.",
            evidence = "BOA dial closure · Orthopedic insole · Anti-slip rubber tread",
            unknowns = emptyList(),
            source = "Zappos Adaptive",
            price = "$68.00",
            compatibility = "Verified Match",
            kind = "shoes"
        ),
        ProductCard(
            id = "p2_prod_10",
            name = "Grey Everyday Knit Cardigan",
            retailer = "Inclusive Wear",
            productUrl = "https://lookweave.local/products/p2_10",
            imageUrl = "drawable://garment_p2_10",
            match = 95,
            reason = "Open front design with easy drape that requires no overhead reach or difficult fasteners.",
            evidence = "Soft viscose knit · Open drape front · Deep patch pockets",
            unknowns = emptyList(),
            source = "Inclusive Wear",
            price = "$46.00",
            compatibility = "Verified Match",
            kind = "layers"
        )
    )

    private val person3Products = listOf(
        ProductCard(
            id = "p3_prod_01",
            name = "Black Sleeve-Ease Graphic Tee",
            retailer = "Independent Apparel · Seattle, WA",
            productUrl = "https://lookweave.local/products/p3_01",
            imageUrl = "drawable://garment_p3_01",
            match = 99,
            reason = "Wide armhole and sleeve circumference accommodate prosthetic forearm without catching.",
            evidence = "100% combed cotton · 9-inch flat sleeve opening · Screenprint graphic",
            unknowns = emptyList(),
            source = "Independent Apparel",
            price = "$29.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p3_prod_02",
            name = "Black Relaxed Easy-Pull Pants",
            retailer = "Seattle Studio",
            productUrl = "https://lookweave.local/products/p3_02",
            imageUrl = "drawable://garment_p3_02",
            match = 98,
            reason = "Flat elastic waistband with one-hand drawcord lock and deep secure pockets.",
            evidence = "Cotton ripstop blend · One-hand cord stopper · Relaxed straight leg",
            unknowns = emptyList(),
            source = "Seattle Studio",
            price = "$46.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p3_prod_03",
            name = "Black Contemporary Skate Sneaker",
            retailer = "Footwear Co.",
            productUrl = "https://lookweave.local/products/p3_03",
            imageUrl = "drawable://garment_p3_03",
            match = 98,
            reason = "Elastic no-tie laces allow one-handed slip-on and secure fit.",
            evidence = "Suede & canvas upper · Elastic stretch laces · Vulcanized rubber sole",
            unknowns = emptyList(),
            source = "Footwear Co.",
            price = "$59.00",
            compatibility = "Verified Match",
            kind = "shoes"
        ),
        ProductCard(
            id = "p3_prod_04",
            name = "Charcoal Oversized Poplin Shirt",
            retailer = "Minimal Design",
            productUrl = "https://lookweave.local/products/p3_04",
            imageUrl = "drawable://garment_p3_04",
            match = 97,
            reason = "Magnetic cuff closures allow effortless one-handed fastening on either wrist.",
            evidence = "Crisp cotton poplin · Magnetic cuff closures · Drop shoulder",
            unknowns = emptyList(),
            source = "Minimal Design",
            price = "$48.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p3_prod_05",
            name = "Olive Utility Zip Overshirt",
            retailer = "Urban Adaptive",
            productUrl = "https://lookweave.local/products/p3_05",
            imageUrl = "drawable://garment_p3_05",
            match = 96,
            reason = "Large ring-pull zipper slider designed for easy grasp by prosthesis or single hand.",
            evidence = "Heavyweight cotton canvas · Oversized ring zip pull · Ergonomic angle pockets",
            unknowns = emptyList(),
            source = "Urban Adaptive",
            price = "$62.00",
            compatibility = "Verified Match",
            kind = "layers"
        ),
        ProductCard(
            id = "p3_prod_06",
            name = "Dark Grey Cropped Chinos",
            retailer = "Pacific Studio",
            productUrl = "https://lookweave.local/products/p3_06",
            imageUrl = "drawable://garment_p3_06",
            match = 95,
            reason = "Hook-and-bar closure replaced with magnetic slider for easy front closure.",
            evidence = "Stretch cotton twill · Magnetic fly closure · Cropped ankle cut",
            unknowns = emptyList(),
            source = "Pacific Studio",
            price = "$42.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p3_prod_07",
            name = "Mustard Boxy Minimalist Tee",
            retailer = "Color Theory",
            productUrl = "https://lookweave.local/products/p3_07",
            imageUrl = "drawable://garment_p3_07",
            match = 97,
            reason = "Generous sleeve cut in Kai's preferred mustard palette with soft pre-shrunk cotton.",
            evidence = "100% pre-shrunk cotton · Extra wide sleeves · Double stitched hem",
            unknowns = emptyList(),
            source = "Color Theory",
            price = "$26.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p3_prod_08",
            name = "Black Technical Windbreaker",
            retailer = "Seattle Rainwear",
            productUrl = "https://lookweave.local/products/p3_08",
            imageUrl = "drawable://garment_p3_08",
            match = 96,
            reason = "Magnetic storm flap and wide raglan sleeves slide easily over assistive device.",
            evidence = "Waterproof nylon · Magnetic storm flap · Elasticated one-hand cuff",
            unknowns = emptyList(),
            source = "Seattle Rainwear",
            price = "$75.00",
            compatibility = "Verified Match",
            kind = "layers"
        ),
        ProductCard(
            id = "p3_prod_09",
            name = "White Minimalist Leather Trainer",
            retailer = "Nordic Footwear",
            productUrl = "https://lookweave.local/products/p3_09",
            imageUrl = "drawable://garment_p3_09",
            match = 96,
            reason = "Padded tongue and flexible heel counter enable quick step-in without using hands.",
            evidence = "Soft nappa leather · Collapsible step-in heel · Memory foam insole",
            unknowns = emptyList(),
            source = "Nordic Footwear",
            price = "$64.00",
            compatibility = "Verified Match",
            kind = "shoes"
        ),
        ProductCard(
            id = "p3_prod_10",
            name = "Minimalist Crossbody Gear Bag",
            retailer = "Tech Pack Co.",
            productUrl = "https://lookweave.local/products/p3_10",
            imageUrl = "drawable://garment_p3_10",
            match = 95,
            reason = "Quick-release magnetic buckle on strap enables instant one-handed release.",
            evidence = "Waterproof cordura · Fidlock magnetic buckle · Padded strap",
            unknowns = emptyList(),
            source = "Tech Pack Co.",
            price = "$38.00",
            compatibility = "Verified Match",
            kind = "accessories"
        )
    )

    private val person4Products = listOf(
        ProductCard(
            id = "p4_prod_01",
            name = "Navy Tailored Business Blazer",
            retailer = "Brooks & Co. · New York, NY",
            productUrl = "https://lookweave.local/products/p4_01",
            imageUrl = "drawable://garment_p4_01",
            match = 99,
            reason = "Professional wool-blend blazer with tailored shoulder and discreet stretch armscye.",
            evidence = "Tropical wool blend · Viscose lining · Functional inner chest pockets",
            unknowns = emptyList(),
            source = "Brooks & Co.",
            price = "$98.00",
            compatibility = "Verified Match",
            kind = "layers"
        ),
        ProductCard(
            id = "p4_prod_02",
            name = "White Oxford Button Shirt",
            retailer = "MagnaReady Menswear",
            productUrl = "https://lookweave.local/products/p4_02",
            imageUrl = "drawable://garment_p4_02",
            match = 99,
            reason = "Crisp non-iron cotton shirt with concealed magnetic closures behind genuine buttons.",
            evidence = "100% pinpoint oxford cotton · Magnetic placket closures · Button-down collar",
            unknowns = emptyList(),
            source = "MagnaReady Menswear",
            price = "$52.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p4_prod_03",
            name = "Navy Tailored Dress Trousers",
            retailer = "Wall Street Tailors",
            productUrl = "https://lookweave.local/products/p4_03",
            imageUrl = "drawable://garment_p4_03",
            match = 98,
            reason = "Sharp pressed crease with hidden expanding waistband for all-day desk comfort.",
            evidence = "Wool-poly blend · Concealed stretch waist tabs · Slanted front pockets",
            unknowns = emptyList(),
            source = "Wall Street Tailors",
            price = "$58.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p4_prod_04",
            name = "Polished Cognac Derby Shoes",
            retailer = "Heritage Leather",
            productUrl = "https://lookweave.local/products/p4_04",
            imageUrl = "drawable://garment_p4_04",
            match = 98,
            reason = "Classic cap-toe derby with hidden elastic gussets for easy slip-in entry.",
            evidence = "Full-grain calfskin · Hidden elastic side gore · Goodyear welted sole",
            unknowns = emptyList(),
            source = "Heritage Leather",
            price = "$85.00",
            compatibility = "Verified Match",
            kind = "shoes"
        ),
        ProductCard(
            id = "p4_prod_05",
            name = "Light Blue Micro-Check Dress Shirt",
            retailer = "Tommy Adaptive",
            productUrl = "https://lookweave.local/products/p4_05",
            imageUrl = "drawable://garment_p4_05",
            match = 97,
            reason = "Faux-button magnetic placket designed for fast, crisp professional presentation.",
            evidence = "Non-iron stretch cotton · Magnetic closures · Spread collar",
            unknowns = emptyList(),
            source = "Tommy Adaptive",
            price = "$49.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p4_prod_06",
            name = "Charcoal Stretch Wool Dress Slacks",
            retailer = "Executive Wear",
            productUrl = "https://lookweave.local/products/p4_06",
            imageUrl = "drawable://garment_p4_06",
            match = 96,
            reason = "Wrinkle-resistant stretch wool blend with smooth viscose lining down to knees.",
            evidence = "96% wool, 4% elastane · Smooth knee lining · Button-through back pockets",
            unknowns = emptyList(),
            source = "Executive Wear",
            price = "$65.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p4_prod_07",
            name = "Merino Wool V-Neck Sweater (Navy)",
            retailer = "Savile Row",
            productUrl = "https://lookweave.local/products/p4_07",
            imageUrl = "drawable://garment_p4_07",
            match = 97,
            reason = "Fine-gauge lightweight merino layer that fits neatly over dress shirts.",
            evidence = "100% extra-fine merino wool · Ribbed V-neck · Clean cuffs",
            unknowns = emptyList(),
            source = "Savile Row",
            price = "$55.00",
            compatibility = "Verified Match",
            kind = "layers"
        ),
        ProductCard(
            id = "p4_prod_08",
            name = "Khaki Slim-Straight Office Chino",
            retailer = "J.Crew Men",
            productUrl = "https://lookweave.local/products/p4_08",
            imageUrl = "drawable://garment_p4_08",
            match = 96,
            reason = "Mid-weight cotton twill with 2% elastane for seamless transition from transit to desk.",
            evidence = "Broken-in stretch twill · Coin pocket · Zip fly with button closure",
            unknowns = emptyList(),
            source = "J.Crew Men",
            price = "$48.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p4_prod_09",
            name = "Dark Brown Penny Loafers",
            retailer = "Cole Haan",
            productUrl = "https://lookweave.local/products/p4_09",
            imageUrl = "drawable://garment_p4_09",
            match = 97,
            reason = "Slip-on construction with Grand.OS foam cushioning for long city commutes.",
            evidence = "Hand-burnished leather · Grand.OS foam cushioning · Leather sole with rubber pod",
            unknowns = emptyList(),
            source = "Cole Haan",
            price = "$79.00",
            compatibility = "Verified Match",
            kind = "shoes"
        ),
        ProductCard(
            id = "p4_prod_10",
            name = "Silk Grenadine Navy Tie & Pocket Square",
            retailer = "Gentleman's Edit",
            productUrl = "https://lookweave.local/products/p4_10",
            imageUrl = "drawable://garment_p4_10",
            match = 95,
            reason = "High-density weave navy silk tie with matching clean pocket square.",
            evidence = "100% Italian silk grenadine · Hand-rolled pocket square",
            unknowns = emptyList(),
            source = "Gentleman's Edit",
            price = "$28.00",
            compatibility = "Verified Match",
            kind = "accessories"
        )
    )

    private val person5Products = listOf(
        ProductCard(
            id = "p5_prod_01",
            name = "Emerald Flowing Modest Abaya",
            retailer = "Modanisa · Miami, FL",
            productUrl = "https://lookweave.local/products/p5_01",
            imageUrl = "drawable://garment_p5_01",
            match = 99,
            reason = "Floor-length breathable georgette abaya with full coverage and elegant drape.",
            evidence = "Double georgette crepe · Fully opaque lining · Gold cord piping",
            unknowns = emptyList(),
            source = "Modanisa",
            price = "$68.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p5_prod_02",
            name = "Cream Silk-Blend Hijab Scarf",
            retailer = "Haute Hijab",
            productUrl = "https://lookweave.local/products/p5_02",
            imageUrl = "drawable://garment_p5_02",
            match = 99,
            reason = "Non-slip modal silk blend with gentle breathability suitable for warm Miami climate.",
            evidence = "60% modal, 40% mulberry silk · Breathable weave · Generous 72x30 inch dimensions",
            unknowns = emptyList(),
            source = "Haute Hijab",
            price = "$24.00",
            compatibility = "Verified Match",
            kind = "accessories"
        ),
        ProductCard(
            id = "p5_prod_03",
            name = "Gold Embroidered Kimono Cardigan",
            retailer = "East & Grace",
            productUrl = "https://lookweave.local/products/p5_03",
            imageUrl = "drawable://garment_p5_03",
            match = 97,
            reason = "Maxi-length layering piece with modest full sleeve coverage and intricate trim.",
            evidence = "Flowing crepe fabric · Gold thread embroidery · Full sleeve coverage",
            unknowns = emptyList(),
            source = "East & Grace",
            price = "$58.00",
            compatibility = "Verified Match",
            kind = "layers"
        ),
        ProductCard(
            id = "p5_prod_04",
            name = "Pleated Satin Midi Skirt (Emerald)",
            retailer = "Aab Collection",
            productUrl = "https://lookweave.local/products/p5_04",
            imageUrl = "drawable://garment_p5_04",
            match = 98,
            reason = "High-waisted opaque pleated satin skirt offering full knee and calf coverage.",
            evidence = "Heavyweight satin · Accordion pleats · Elasticated waistband",
            unknowns = emptyList(),
            source = "Aab Collection",
            price = "$49.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p5_prod_05",
            name = "Longline Modest Button Tunic",
            retailer = "Inayah",
            productUrl = "https://lookweave.local/products/p5_05",
            imageUrl = "drawable://garment_p5_05",
            match = 96,
            reason = "High-neck tunic with knee-length hem and modest side vents for ease of walking.",
            evidence = "Soft viscose linen blend · Mandarin collar · Modest side vents",
            unknowns = emptyList(),
            source = "Inayah",
            price = "$45.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p5_prod_06",
            name = "Wide-Leg Palazzo Trousers (Ivory)",
            retailer = "Modest Theory",
            productUrl = "https://lookweave.local/products/p5_06",
            imageUrl = "drawable://garment_p5_06",
            match = 97,
            reason = "Fluid lightweight viscose trousers with opaque lining and modest loose silhouette.",
            evidence = "100% viscose · Full interior modesty lining · Wide palazzo leg",
            unknowns = emptyList(),
            source = "Modest Theory",
            price = "$39.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p5_prod_07",
            name = "Dusty Rose Modal Jersey Hijab",
            retailer = "Modanisa",
            productUrl = "https://lookweave.local/products/p5_07",
            imageUrl = "drawable://garment_p5_07",
            match = 98,
            reason = "Instant pin-free premium jersey hijab with zero need for sharp magnets or pins.",
            evidence = "95% natural modal, 5% elastane · Anti-static · Breathable stretch",
            unknowns = emptyList(),
            source = "Modanisa",
            price = "$18.00",
            compatibility = "Verified Match",
            kind = "accessories"
        ),
        ProductCard(
            id = "p5_prod_08",
            name = "Tiered Modest Linen Maxi Dress",
            retailer = "Linen Botanica",
            productUrl = "https://lookweave.local/products/p5_08",
            imageUrl = "drawable://garment_p5_08",
            match = 96,
            reason = "Cooling 100% natural linen with long bishop sleeves and crew neckline.",
            evidence = "Pure European linen · Long elastic bishop cuffs · Tiered A-line skirt",
            unknowns = emptyList(),
            source = "Linen Botanica",
            price = "$72.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p5_prod_09",
            name = "Pointed-Toe Neutral Leather Mules",
            retailer = "Clarks Modest",
            productUrl = "https://lookweave.local/products/p5_09",
            imageUrl = "drawable://garment_p5_09",
            match = 96,
            reason = "Low block heel with cushioned arch support for lecture hall comfort.",
            evidence = "Soft glove leather · 1-inch stacked heel · OrthoLite foam footbed",
            unknowns = emptyList(),
            source = "Clarks Modest",
            price = "$54.00",
            compatibility = "Verified Match",
            kind = "shoes"
        ),
        ProductCard(
            id = "p5_prod_10",
            name = "Gold Filigree Pendant Necklace",
            retailer = "Miami Artisan",
            productUrl = "https://lookweave.local/products/p5_10",
            imageUrl = "drawable://garment_p5_10",
            match = 95,
            reason = "Long 30-inch gold chain sits elegantly over high-neck abayas and tunics.",
            evidence = "18k gold vermeil over sterling silver · 30-inch adjustable rolo chain",
            unknowns = emptyList(),
            source = "Miami Artisan",
            price = "$32.00",
            compatibility = "Verified Match",
            kind = "accessories"
        )
    )

    private val person6Products = listOf(
        ProductCard(
            id = "p6_prod_01",
            name = "Peace Sign Oversized Graphic Tee",
            retailer = "Thrift & Vintage · Denver, CO",
            productUrl = "https://lookweave.local/products/p6_01",
            imageUrl = "drawable://garment_p6_01",
            match = 99,
            reason = "Affordable vintage-washed cotton graphic tee with relaxed campus fit.",
            evidence = "100% cotton · Vintage enzyme wash · Relaxed drop shoulder",
            unknowns = emptyList(),
            source = "Thrift & Vintage",
            price = "$16.99",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p6_prod_02",
            name = "Classic Distressed Blue Denim",
            retailer = "Target Wild Fable",
            productUrl = "https://lookweave.local/products/p6_02",
            imageUrl = "drawable://garment_p6_02",
            match = 98,
            reason = "Budget-friendly straight leg denim with comfort stretch and lived-in wash.",
            evidence = "99% cotton, 1% spandex · Five-pocket styling · Light destruction details",
            unknowns = emptyList(),
            source = "Target Wild Fable",
            price = "$24.50",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p6_prod_03",
            name = "White Chunky Platform Sneakers",
            retailer = "ASOS Design",
            productUrl = "https://lookweave.local/products/p6_03",
            imageUrl = "drawable://garment_p6_03",
            match = 97,
            reason = "Lightweight foam platform sole adds height and all-day walking cushioning on a budget.",
            evidence = "Synthetic leather & mesh · 1.5-inch platform sole · Cushioned ankle collar",
            unknowns = emptyList(),
            source = "ASOS Design",
            price = "$32.00",
            compatibility = "Verified Match",
            kind = "shoes"
        ),
        ProductCard(
            id = "p6_prod_04",
            name = "Lavender Washed Fleece Hoodie",
            retailer = "H&M Basics",
            productUrl = "https://lookweave.local/products/p6_04",
            imageUrl = "drawable://garment_p6_04",
            match = 98,
            reason = "Soft pastel lavender hoodie with kangaroo pocket and relaxed drop shoulders.",
            evidence = "Cotton-blend fleece · Brushed interior · Double-layer hood",
            unknowns = emptyList(),
            source = "H&M Basics",
            price = "$22.99",
            compatibility = "Verified Match",
            kind = "layers"
        ),
        ProductCard(
            id = "p6_prod_05",
            name = "Striped Rib-Knit Long Sleeve",
            retailer = "Cotton On",
            productUrl = "https://lookweave.local/products/p6_05",
            imageUrl = "drawable://garment_p6_05",
            match = 96,
            reason = "Breathable ribbed jersey top great for layering under thrifted overalls or jackets.",
            evidence = "95% cotton, 5% elastane · Ribbed texture · Lettuce-edge trim",
            unknowns = emptyList(),
            source = "Cotton On",
            price = "$18.00",
            compatibility = "Verified Match",
            kind = "tops"
        ),
        ProductCard(
            id = "p6_prod_06",
            name = "Black High-Rise Cargo Pants",
            retailer = "Forever 21",
            productUrl = "https://lookweave.local/products/p6_06",
            imageUrl = "drawable://garment_p6_06",
            match = 97,
            reason = "Multi-pocket durable cotton cargo pants with adjustable ankle toggle ties.",
            evidence = "100% cotton twill · 6 utility pockets · Bungee cord hem cinch",
            unknowns = emptyList(),
            source = "Forever 21",
            price = "$27.00",
            compatibility = "Verified Match",
            kind = "bottoms"
        ),
        ProductCard(
            id = "p6_prod_07",
            name = "Cropped Denim Jacket (Vintage Wash)",
            retailer = "Target Universal Thread",
            productUrl = "https://lookweave.local/products/p6_07",
            imageUrl = "drawable://garment_p6_07",
            match = 96,
            reason = "Versatile transitional layer with sturdy metal shank buttons and welt pockets.",
            evidence = "100% cotton denim · Vintage medium wash · Flap chest pockets",
            unknowns = emptyList(),
            source = "Target Universal Thread",
            price = "$29.99",
            compatibility = "Verified Match",
            kind = "layers"
        ),
        ProductCard(
            id = "p6_prod_08",
            name = "Checkered Slip-On Canvas Shoes",
            retailer = "Vans Outlet",
            productUrl = "https://lookweave.local/products/p6_08",
            imageUrl = "drawable://garment_p6_08",
            match = 97,
            reason = "Classic slip-on canvas shoes with vulcanized waffle rubber outsole.",
            evidence = "Durable canvas upper · Elastic side accents · Signature waffle outsole",
            unknowns = emptyList(),
            source = "Vans Outlet",
            price = "$28.00",
            compatibility = "Verified Match",
            kind = "shoes"
        ),
        ProductCard(
            id = "p6_prod_09",
            name = "Corduroy Bucket Hat (Lavender)",
            retailer = "Urban Outfitters Sale",
            productUrl = "https://lookweave.local/products/p6_09",
            imageUrl = "drawable://garment_p6_09",
            match = 95,
            reason = "Textured cotton corduroy bucket hat matching Persona 6's color palette.",
            evidence = "100% cotton corduroy · Soft unstructured crown · 2-inch brim",
            unknowns = emptyList(),
            source = "Urban Outfitters Sale",
            price = "$14.00",
            compatibility = "Verified Match",
            kind = "accessories"
        ),
        ProductCard(
            id = "p6_prod_10",
            name = "Canvas Student Tote Bag",
            retailer = "Campus Bookstore",
            productUrl = "https://lookweave.local/products/p6_10",
            imageUrl = "drawable://garment_p6_10",
            match = 95,
            reason = "Heavy-duty canvas tote with reinforced handles fits textbooks and laptop.",
            evidence = "12 oz cotton canvas · Reinforced cross-stitching · Interior zip pocket",
            unknowns = emptyList(),
            source = "Campus Bookstore",
            price = "$12.00",
            compatibility = "Verified Match",
            kind = "accessories"
        )
    )
}
