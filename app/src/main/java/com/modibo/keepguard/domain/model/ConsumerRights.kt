package com.modibo.keepguard.domain.model

object ConsumerRights {

    fun forCategory(
        category: AssetCategory,
        condition: AssetCondition,
        purchaseMode: PurchaseMode,
        purchaseDate: String? = null
    ): List<ConsumerRight> {
        val all = rights[category] ?: rights[AssetCategory.OTHER]!!
        val afterAgec = purchaseDate != null && purchaseDate >= "2021-01-01"

        return all.filter { right ->
            if (right.minPurchaseDate != null && !afterAgec) return@filter false
            if (right.modes != PurchaseMode.entries && purchaseMode !in right.modes) return@filter false
            if (right.conditions != AssetCondition.entries && condition !in right.conditions) return@filter false
            if (right.durationMonths == 24 && right.type == RightType.CONFIRMED
                && condition == AssetCondition.RECONDITIONED
                && category != AssetCategory.VEHICLE
            ) return@filter false
            true
        }
    }

    private val rights = mapOf(
        AssetCategory.APPLIANCE to listOf(
            ConsumerRight(
                name = "Garantie légale — 2 ans",
                description = "Réparation ou remplacement sans frais.",
                source = "Art. L217-4 Code conso",
                type = RightType.CONFIRMED,
                durationMonths = 24,
                conditions = listOf(AssetCondition.NEW)
            ),
            ConsumerRight(
                name = "Garantie réduite — 1 an",
                description = "Pour les biens reconditionnés si mentionné au contrat.",
                source = "Art. L217-7",
                type = RightType.WARNING,
                durationMonths = 12,
                conditions = listOf(AssetCondition.RECONDITIONED)
            ),
            ConsumerRight(
                name = "Rétractation — 14 jours",
                description = "Uniquement pour les achats en ligne.",
                source = "Art. L221-18",
                type = RightType.CONFIRMED,
                modes = listOf(PurchaseMode.ONLINE)
            ),
            ConsumerRight(
                name = "Pièces détachées — 5 à 10 ans",
                description = "Loi AGEC — achats après janvier 2021.",
                source = "Loi AGEC 2020",
                type = RightType.INFORMATIVE,
                conditions = listOf(AssetCondition.NEW),
                minPurchaseDate = "2021-01-01"
            ),
            ConsumerRight(
                name = "Vices cachés — 2 ans",
                description = "À compter de la découverte du défaut, quel que soit le mode ou l'état.",
                source = "Art. 1641 Code civil",
                type = RightType.INFORMATIVE
            )
        ),

        AssetCategory.TECH to listOf(
            ConsumerRight(
                name = "Garantie légale — 2 ans",
                description = "Pour tout matériel neuf chez un professionnel.",
                source = "Art. L217-4",
                type = RightType.CONFIRMED,
                durationMonths = 24,
                conditions = listOf(AssetCondition.NEW)
            ),
            ConsumerRight(
                name = "Garantie réduite — 1 an",
                description = "Reconditionné si mentionné au contrat.",
                source = "Art. L217-7",
                type = RightType.WARNING,
                durationMonths = 12,
                conditions = listOf(AssetCondition.RECONDITIONED)
            ),
            ConsumerRight(
                name = "Rétractation — 14 jours",
                description = "Achats en ligne uniquement.",
                source = "Art. L221-18",
                type = RightType.CONFIRMED,
                modes = listOf(PurchaseMode.ONLINE)
            ),
            ConsumerRight(
                name = "Pièces détachées — 5 ans min.",
                description = "Smartphones, PC, tablettes après janvier 2021.",
                source = "Loi AGEC 2020",
                type = RightType.INFORMATIVE,
                conditions = listOf(AssetCondition.NEW),
                minPurchaseDate = "2021-01-01"
            ),
            ConsumerRight(
                name = "Logiciels & consommables exclus",
                description = "Non couverts par certaines protections.",
                source = "Code conso",
                type = RightType.WARNING
            ),
            ConsumerRight(
                name = "Vices cachés — 2 ans",
                description = "À compter de la découverte du défaut.",
                source = "Art. 1641 Code civil",
                type = RightType.INFORMATIVE
            )
        ),

        AssetCategory.VEHICLE to listOf(
            ConsumerRight(
                name = "Garantie légale — 2 ans",
                description = "Véhicule neuf chez un professionnel.",
                source = "Art. L217-4",
                type = RightType.CONFIRMED,
                durationMonths = 24,
                conditions = listOf(AssetCondition.NEW)
            ),
            ConsumerRight(
                name = "Garantie constructeur",
                description = "Généralement 2 à 5 ans selon la marque. Vérifiez le carnet.",
                source = "Contrat constructeur",
                type = RightType.INFORMATIVE,
                conditions = listOf(AssetCondition.NEW)
            ),
            ConsumerRight(
                name = "Rétractation — 14 jours",
                description = "Achats à distance uniquement (en ligne, téléphone).",
                source = "Art. L221-18",
                type = RightType.CONFIRMED,
                modes = listOf(PurchaseMode.ONLINE)
            ),
            ConsumerRight(
                name = "Vices cachés — 2 ans",
                description = "À compter de la découverte, quel que soit l'âge du véhicule.",
                source = "Art. 1641 Code civil",
                type = RightType.INFORMATIVE
            )
        ),

        AssetCategory.FURNITURE to listOf(
            ConsumerRight(
                name = "Garantie légale — 2 ans",
                description = "Meubles neufs chez un professionnel.",
                source = "Art. L217-4",
                type = RightType.CONFIRMED,
                durationMonths = 24,
                conditions = listOf(AssetCondition.NEW)
            ),
            ConsumerRight(
                name = "Rétractation — 14 jours",
                description = "En ligne. Exclu : sur mesure.",
                source = "Art. L221-18",
                type = RightType.CONFIRMED,
                modes = listOf(PurchaseMode.ONLINE)
            ),
            ConsumerRight(
                name = "Vices cachés — 2 ans",
                description = "À compter de la découverte du défaut.",
                source = "Art. 1641 Code civil",
                type = RightType.INFORMATIVE
            )
        ),

        AssetCategory.OTHER to listOf(
            ConsumerRight(
                name = "Garantie légale — 2 ans",
                description = "Pour tout bien neuf chez un professionnel.",
                source = "Art. L217-4",
                type = RightType.CONFIRMED,
                durationMonths = 24,
                conditions = listOf(AssetCondition.NEW)
            ),
            ConsumerRight(
                name = "Rétractation — 14 jours",
                description = "Achats en ligne uniquement.",
                source = "Art. L221-18",
                type = RightType.CONFIRMED,
                modes = listOf(PurchaseMode.ONLINE)
            ),
            ConsumerRight(
                name = "Vices cachés — 2 ans",
                description = "À compter de la découverte du défaut.",
                source = "Art. 1641 Code civil",
                type = RightType.INFORMATIVE
            )
        )
    )
}
