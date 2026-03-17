package com.modibo.keepguard.domain.model

enum class AssetSubCategory(val label: String, val category: AssetCategory) {
    // Électroménager
    WASHER("Lave-linge / Sèche-linge", AssetCategory.APPLIANCE),
    FRIDGE("Réfrigérateur / Congélateur", AssetCategory.APPLIANCE),
    DISHWASHER("Lave-vaisselle", AssetCategory.APPLIANCE),
    OVEN("Four / Micro-ondes / Plaque", AssetCategory.APPLIANCE),
    VACUUM("Aspirateur / Nettoyeur", AssetCategory.APPLIANCE),
    CLIMATE("Climatisation / Chauffage", AssetCategory.APPLIANCE),
    OTHER_APPLIANCE("Autre électroménager", AssetCategory.APPLIANCE),

    // Automobile
    CAR("Voiture", AssetCategory.VEHICLE),
    MOTO("Moto / Scooter", AssetCategory.VEHICLE),
    BIKE("Vélo / VAE", AssetCategory.VEHICLE),
    VAN("Camionnette / Utilitaire", AssetCategory.VEHICLE),
    CAMPER("Camping-car / Caravane", AssetCategory.VEHICLE),
    OTHER_VEHICLE("Autre véhicule", AssetCategory.VEHICLE),

    // Informatique
    LAPTOP("Ordinateur portable", AssetCategory.TECH),
    DESKTOP("Ordinateur fixe", AssetCategory.TECH),
    PHONE("Smartphone", AssetCategory.TECH),
    TABLET("Tablette", AssetCategory.TECH),
    CONSOLE("Console de jeux", AssetCategory.TECH),
    TV("TV / Écran", AssetCategory.TECH),
    PRINTER("Imprimante", AssetCategory.TECH),
    OTHER_TECH("Autre high-tech", AssetCategory.TECH),

    // Mobilier
    SOFA("Canapé / Salon", AssetCategory.FURNITURE),
    BED("Chambre / Literie", AssetCategory.FURNITURE),
    KITCHEN_FURNITURE("Cuisine / Salle de bain", AssetCategory.FURNITURE),
    DESK("Bureau / Chaise", AssetCategory.FURNITURE),
    GARDEN("Jardin / Extérieur", AssetCategory.FURNITURE),
    DECO("Décoration", AssetCategory.FURNITURE),
    OTHER_FURNITURE("Autre mobilier", AssetCategory.FURNITURE),

    // Autre
    SPORT("Sport / Loisirs", AssetCategory.OTHER),
    TOOLS("Outils / Bricolage", AssetCategory.OTHER),
    CLOTHES("Vêtements / Accessoires", AssetCategory.OTHER),
    BOOKS("Livres / Musique / Films", AssetCategory.OTHER),
    JEWELRY("Bijoux / Montres", AssetCategory.OTHER),
    KIDS("Puériculture / Jouets", AssetCategory.OTHER),
    OTHER_OTHER("Autre", AssetCategory.OTHER);

    companion object {
        fun forCategory(category: AssetCategory): List<AssetSubCategory> =
            entries.filter { it.category == category }
    }
}
