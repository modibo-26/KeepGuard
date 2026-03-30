package com.modibo.keepguard.data.scanner

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import com.modibo.keepguard.domain.model.ScannedData
import kotlinx.serialization.json.Json
import javax.inject.Inject


class GeminiService @Inject constructor() {

    private val jsonSerializer = Json { ignoreUnknownKeys = true }

    private val documentSchema = Schema.obj(
        mapOf(
            // Document
            "name" to Schema.string(),
            "type" to Schema.enumeration(listOf("INVOICE", "WARRANTY_CERT", "MAINTENANCE_REPORT", "INSURANCE", "MANUAL", "OTHER")),
            "date" to Schema.string(),
            "amount" to Schema.string(),
            "merchant" to Schema.string(),
            // Asset
            "brand" to Schema.string(),
            "model" to Schema.string(),
            "serialNumber" to Schema.string(),
            "warrantyMonths" to Schema.integer(),
            "category" to Schema.enumeration(listOf("APPLIANCE", "VEHICLE", "TECH", "FURNITURE", "OTHER")),
            "purchasePrice" to Schema.string(),
            "purchasePlace" to Schema.string(),
            "purchaseDate" to Schema.string(),
            // Warranty
            "warrantyType" to Schema.enumeration(listOf("MANUFACTURER", "EXTENDED", "INSURANCE", "SELLER", "LEGAL", "RETRACTATION")),
            "durationMonths" to Schema.integer(),
            "warrantyProvider" to Schema.string(),
            "conditions" to Schema.string(),
            // Maintenance
            "maintenanceTitle" to Schema.string(),
            "maintenanceDescription" to Schema.string(),
            "maintenanceType" to Schema.enumeration(listOf("ONE_TIME", "RECURRING")),
            "cost" to Schema.string(),
            "maintenanceProvider" to Schema.string(),
        )
    )

    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = documentSchema
            }
        )

    suspend fun parseText(ocrText: String): ScannedData {
        val prompt = """
            Analyse ce texte extrait d'un document scanné et extrais le maximum d'informations.
            Retourne un JSON avec les champs suivants (laisse vide si non trouvé) :

            DOCUMENT :
            - name : nom du document ou du commerce/marque
            - type : INVOICE, WARRANTY_CERT, MAINTENANCE_REPORT, INSURANCE, MANUAL ou OTHER
            - date : date du document (format dd/MM/yyyy)
            - amount : montant total (avec devise)
            - merchant : nom du commerce ou de l'entreprise

            BIEN :
            - brand : marque du produit
            - model : modèle du produit
            - serialNumber : numéro de série
            - warrantyMonths : durée de garantie constructeur en mois (entier, 0 si non trouvé)
            - category : APPLIANCE, VEHICLE, TECH, FURNITURE ou OTHER
            - purchasePrice : prix d'achat (avec devise)
            - purchasePlace : lieu ou vendeur
            - purchaseDate : date d'achat (format dd/MM/yyyy)

            GARANTIE :
            - warrantyType : MANUFACTURER, EXTENDED, LEGAL ou RETRACTATION
            - durationMonths : durée en mois (entier, 0 si non trouvé)
            - warrantyProvider : fournisseur de la garantie
            - conditions : conditions de la garantie

            ENTRETIEN :
            - maintenanceTitle : titre de l'intervention
            - maintenanceDescription : description de l'intervention
            - maintenanceType : ONE_TIME ou RECURRING
            - cost : coût de l'intervention (avec devise)
            - maintenanceProvider : prestataire

            Texte OCR :
            $ocrText
        """.trimIndent()

        val response = model.generateContent(prompt)

        val jsonString = response.text ?: run {
            println("Réponse Gemini vide. Retour d'un ScannedData vide.")
            return ScannedData()
        }

        return try {
            jsonSerializer.decodeFromString<ScannedData>(jsonString)
        } catch (e: Exception) {
            println("Erreur de désérialisation JSON : $e. JSON reçu: $jsonString")
            ScannedData()
        }
    }
}
