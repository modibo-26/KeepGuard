# 🛡️ KeepGuard — Architecture Technique Finale

> **Version :** 1.0
> **Date :** Février 2026
> **Auteur :** Auber Modibo
> **Nom initial CDC :** GarantiScan → renommé **KeepGuard**

---

## 1. Vue d'ensemble

**KeepGuard** est une application Android de gestion des garanties, révisions et archivage de documents. C'est un coffre-fort numérique intelligent qui permet de centraliser, scanner et suivre tous les documents liés aux biens d'un utilisateur.

### Décisions d'architecture

| Décision | Choix | Justification |
|----------|-------|---------------|
| Architecture | Clean Architecture | Séparation claire des couches, préparation KMP/iOS |
| UI | Kotlin + Jetpack Compose | UI déclarative native Android |
| Backend | Firebase (Auth, Firestore, Storage) | BaaS rapide à mettre en place |
| Cache offline | Firestore SDK cache natif | Pas de Room — le cache intégré Firestore suffit |
| Auth MVP | Connexion anonyme | Zéro friction pour tester l'app |
| Auth V2 | Email/Google Sign-In | Upgrade depuis compte anonyme |
| Scan | CameraX + ML Kit | Détection de bords + OCR |
| Notifications | WorkManager + FCM | Rappels locaux + push |
| DI | Hilt | Standard Android pour injection de dépendances |
| Évolution | KMP/iOS | Refacto Clean → commonMain/androidMain/iosMain |

---

## 2. Stack technique

| Couche | Technologie | Rôle |
|--------|-------------|------|
| Frontend | Kotlin + Jetpack Compose | UI native Android |
| Architecture | Clean Architecture | Séparation domain/data/presentation |
| Backend cloud | Firebase | Auth, Firestore, Storage, FCM |
| Scan documents | CameraX + ML Kit | Capture, détection bords, OCR |
| Notifications | WorkManager + FCM | Rappels locaux et push |
| Injection | Hilt (Dagger) | DI |
| Images | Coil | Chargement asynchrone d'images |
| Préférences | DataStore | Paramètres utilisateur |

---

## 3. Structure des packages

```
com.modibo.keepguard
│
├── app/
│   └── KeepGuardApp.kt              # Application class (Hilt)
│
├── di/                               # Injection de dépendances
│   ├── AppModule.kt
│   ├── FirebaseModule.kt
│   └── RepositoryModule.kt
│
├── domain/                           # 🟢 COUCHE DOMAIN (Kotlin pur — aucune dépendance Android/Firebase)
│   │
│   ├── model/
│   │   ├── Asset.kt                  # Bien (frigo, voiture, téléphone...)
│   │   ├── AssetCategory.kt          # Catégorie (enum prédéfinies + CUSTOM)
│   │   ├── Warranty.kt               # Garantie liée à un bien
│   │   ├── WarrantyStatus.kt         # Statut calculé (Active, ExpireSoon, Expired)
│   │   ├── Maintenance.kt            # Révision / entretien
│   │   ├── Document.kt               # Document scanné (facture, certificat)
│   │   ├── Reminder.kt               # Rappel d'échéance
│   │   └── User.kt                   # Utilisateur
│   │
│   ├── repository/                   # Interfaces (contrats)
│   │   ├── AssetRepository.kt
│   │   ├── WarrantyRepository.kt
│   │   ├── MaintenanceRepository.kt
│   │   ├── DocumentRepository.kt
│   │   ├── ReminderRepository.kt
│   │   └── AuthRepository.kt
│   │
│   ├── scanner/
│   │   └── DocumentScanner.kt       # Interface (contrat) — implémentation dans data
│   │
│   └── usecase/
│       ├── asset/
│       │   ├── GetAssetsUseCase.kt
│       │   ├── GetAssetByIdUseCase.kt
│       │   ├── AddAssetUseCase.kt
│       │   ├── UpdateAssetUseCase.kt
│       │   └── DeleteAssetUseCase.kt
│       ├── warranty/
│       │   ├── GetWarrantiesByAssetUseCase.kt
│       │   ├── AddWarrantyUseCase.kt
│       │   ├── GetExpiringWarrantiesUseCase.kt
│       │   └── DeleteWarrantyUseCase.kt
│       ├── maintenance/
│       │   ├── GetMaintenancesByAssetUseCase.kt
│       │   ├── AddMaintenanceUseCase.kt
│       │   ├── GetUpcomingMaintenancesUseCase.kt
│       │   └── DeleteMaintenanceUseCase.kt
│       ├── document/
│       │   ├── ScanDocumentUseCase.kt
│       │   ├── UploadDocumentUseCase.kt
│       │   ├── GetDocumentsByAssetUseCase.kt
│       │   └── DeleteDocumentUseCase.kt
│       ├── reminder/
│       │   ├── ScheduleReminderUseCase.kt
│       │   ├── GetRemindersUseCase.kt
│       │   └── CancelReminderUseCase.kt
│       └── auth/
│           ├── SignInAnonymouslyUseCase.kt
│           ├── LinkAccountUseCase.kt   # Upgrade anonyme → email/Google
│           ├── GetCurrentUserUseCase.kt
│           └── SignOutUseCase.kt
│
├── data/                             # 🟡 COUCHE DATA (implémentations Firebase)
│   │
│   ├── repository/
│   │   ├── AssetRepositoryImpl.kt
│   │   ├── WarrantyRepositoryImpl.kt
│   │   ├── MaintenanceRepositoryImpl.kt
│   │   ├── DocumentRepositoryImpl.kt
│   │   ├── ReminderRepositoryImpl.kt
│   │   └── AuthRepositoryImpl.kt
│   │
│   ├── remote/
│   │   ├── dto/                      # Data Transfer Objects (modèles Firestore)
│   │   │   ├── AssetDto.kt
│   │   │   ├── WarrantyDto.kt
│   │   │   ├── MaintenanceDto.kt
│   │   │   ├── DocumentDto.kt
│   │   │   └── ReminderDto.kt
│   │   │
│   │   └── mapper/                   # Mappers DTO ↔ Domain
│   │       ├── AssetMapper.kt
│   │       ├── WarrantyMapper.kt
│   │       ├── MaintenanceMapper.kt
│   │       ├── DocumentMapper.kt
│   │       └── ReminderMapper.kt
│   │
│   └── scanner/
│       └── DocumentScannerImpl.kt    # Implémente domain/scanner/DocumentScanner (CameraX + ML Kit)
│
├── presentation/                     # 🔵 COUCHE PRÉSENTATION (Compose + ViewModels)
│   │
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   ├── Screen.kt                # Sealed class des routes
│   │   └── BottomNavBar.kt
│   │
│   ├── screen/
│   │   ├── splash/
│   │   │   └── SplashScreen.kt
│   │   ├── home/
│   │   │   ├── HomeScreen.kt
│   │   │   └── HomeViewModel.kt
│   │   ├── assets/
│   │   │   ├── list/
│   │   │   │   ├── AssetListScreen.kt
│   │   │   │   └── AssetListViewModel.kt
│   │   │   ├── detail/
│   │   │   │   ├── AssetDetailScreen.kt
│   │   │   │   └── AssetDetailViewModel.kt
│   │   │   └── form/
│   │   │       ├── AssetFormScreen.kt
│   │   │       └── AssetFormViewModel.kt
│   │   ├── warranty/
│   │   │   ├── WarrantyFormScreen.kt
│   │   │   └── WarrantyFormViewModel.kt
│   │   ├── maintenance/
│   │   │   ├── MaintenanceListScreen.kt
│   │   │   ├── MaintenanceFormScreen.kt
│   │   │   └── MaintenanceViewModel.kt
│   │   ├── scanner/
│   │   │   ├── ScannerScreen.kt
│   │   │   └── ScannerViewModel.kt
│   │   └── settings/
│   │       ├── SettingsScreen.kt
│   │       └── SettingsViewModel.kt
│   │
│   ├── component/                    # Composables réutilisables
│   │   ├── AssetCard.kt
│   │   ├── WarrantyBadge.kt          # Badge coloré : Active/ExpireSoon/Expired
│   │   ├── MaintenanceTimeline.kt
│   │   ├── DocumentThumbnail.kt
│   │   ├── ReminderChip.kt
│   │   ├── SearchBar.kt
│   │   ├── CategoryChip.kt
│   │   ├── EmptyState.kt
│   │   ├── LoadingState.kt
│   │   └── ErrorState.kt
│
├── ui/                              # 🎨 THÈME (convention Android standard)
│   └── theme/
│       ├── Color.kt
│       ├── Type.kt
│       └── Theme.kt
│
└── core/                             # 🔴 UTILITAIRES TRANSVERSAUX
    ├── util/
    │   ├── Resource.kt               # Sealed class Success/Error/Loading
    │   ├── DateUtils.kt
    │   ├── WarrantyStatusCalculator.kt  # Calcule le statut depuis startDate + durationMonths
    │   └── Constants.kt
    ├── notification/
    │   ├── ReminderNotificationService.kt
    │   └── ReminderWorker.kt         # WorkManager pour rappels programmés
    └── security/
        └── BiometricHelper.kt        # PIN / biométrie (V2)
```

---

## 4. Modèle de données — Entités Domain

### Asset (Bien)
```kotlin
data class Asset(
    val id: String = "",
    val userId: String = "",
    val name: String = "",                // "Lave-linge Samsung"
    val description: String = "",
    val category: AssetCategory = AssetCategory.OTHER,
    val brand: String = "",               // Marque
    val model: String = "",               // Modèle
    val serialNumber: String = "",        // Numéro de série
    val purchaseDate: Long? = null,       // Timestamp
    val purchasePrice: Double? = null,
    val purchasePlace: String = "",       // Où acheté
    val imageUrl: String = "",            // Photo du bien
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

enum class AssetCategory(val label: String) {
    APPLIANCE("Électroménager"),      // Frigo, lave-linge, four...
    VEHICLE("Automobile"),            // Voiture, moto, scooter
    TECH("Informatique"),             // PC, téléphone, console
    FURNITURE("Mobilier"),            // Canapé, lit, bureau
    PROPERTY("Immobilier"),           // Chaudière, toiture, portail
    OTHER("Autre")
}
```

### Warranty (Garantie)
```kotlin
data class Warranty(
    val id: String = "",
    val assetId: String = "",
    val userId: String = "",
    val type: WarrantyType = WarrantyType.MANUFACTURER,
    val startDate: Long = 0,              // Début de garantie
    val durationMonths: Int = 24,         // Durée en mois
    val endDate: Long = 0,                // Calculé : startDate + durationMonths
    val provider: String = "",            // Nom du garant (constructeur, assureur...)
    val conditions: String = "",          // Conditions particulières
    val documentIds: List<String> = emptyList(),
    val createdAt: Long = 0
)

enum class WarrantyType(val label: String) {
    MANUFACTURER("Constructeur"),
    EXTENDED("Extension"),
    INSURANCE("Assurance"),
    SELLER("Vendeur")
}

enum class WarrantyStatus(val label: String) {
    ACTIVE("Active"),                     // > 30 jours restants
    EXPIRING_SOON("Expire bientôt"),      // ≤ 30 jours restants
    EXPIRED("Expirée")                    // Passée
}

// Statut calculé dynamiquement — jamais stocké dans la data class
val Warranty.status: WarrantyStatus
    get() = WarrantyStatusCalculator.calculate(endDate)
```

### Maintenance (Révision / Entretien)
```kotlin
data class Maintenance(
    val id: String = "",
    val assetId: String = "",
    val userId: String = "",
    val title: String = "",               // "Vidange", "Détartrage", "Contrôle technique"
    val description: String = "",
    val type: MaintenanceType = MaintenanceType.ONE_TIME,
    val date: Long = 0,                   // Date effectuée ou prévue
    val cost: Double? = null,
    val provider: String = "",            // Garagiste, technicien...
    val mileage: Int? = null,             // Kilométrage (spécifique véhicules)
    val isCompleted: Boolean = false,
    val recurrenceMonths: Int? = null,    // Pour entretiens récurrents
    val nextDueDate: Long? = null,        // Prochaine échéance
    val nextDueMileage: Int? = null,      // Prochain km (véhicules)
    val documentIds: List<String> = emptyList(),
    val createdAt: Long = 0
)

enum class MaintenanceType(val label: String) {
    ONE_TIME("Ponctuel"),
    RECURRING("Récurrent"),               // Vidange tous les 12 mois
    REPAIR("Réparation")
}
```

### Document
```kotlin
data class Document(
    val id: String = "",
    val assetId: String = "",
    val userId: String = "",
    val name: String = "",                // "Facture lave-linge"
    val type: DocumentType = DocumentType.INVOICE,
    val fileUrl: String = "",             // URL Firebase Storage
    val thumbnailUrl: String = "",
    val mimeType: String = "",            // image/jpeg, application/pdf
    val fileSize: Long = 0,
    val ocrText: String? = null,          // Texte extrait par OCR
    val scannedAt: Long = 0,
    val createdAt: Long = 0
)

enum class DocumentType(val label: String) {
    INVOICE("Facture"),
    WARRANTY_CERT("Bon de garantie"),
    MAINTENANCE_REPORT("Facture entretien"),
    INSURANCE("Attestation assurance"),
    MANUAL("Notice / Manuel"),
    OTHER("Autre")
}
```

### Reminder (Rappel)
```kotlin
data class Reminder(
    val id: String = "",
    val userId: String = "",
    val assetId: String = "",
    val referenceId: String = "",         // ID garantie ou maintenance liée
    val referenceType: ReminderRefType = ReminderRefType.WARRANTY,
    val title: String = "",
    val message: String = "",
    val triggerDate: Long = 0,            // Quand déclencher
    val isTriggered: Boolean = false,
    val daysBefore: Int = 30,             // J-30, J-7
    val createdAt: Long = 0
)

enum class ReminderRefType {
    WARRANTY,
    MAINTENANCE
}
```

### User
```kotlin
data class User(
    val id: String = "",
    val displayName: String = "",
    val email: String = "",
    val isAnonymous: Boolean = true,
    val householdId: String? = null,      // V2 : partage foyer
    val createdAt: Long = 0
)
```

---

## 5. Structure Firestore

```
users/{userId}
  ├── displayName: String
  ├── email: String
  ├── isAnonymous: Boolean
  ├── householdId: String?               ← V2
  └── createdAt: Timestamp

assets/{assetId}
  ├── userId: String
  ├── name: String
  ├── description: String
  ├── category: String                   (APPLIANCE | VEHICLE | TECH | FURNITURE | PROPERTY | OTHER)
  ├── brand: String
  ├── model: String
  ├── serialNumber: String
  ├── purchaseDate: Timestamp
  ├── purchasePrice: Number
  ├── purchasePlace: String
  ├── imageUrl: String
  ├── createdAt: Timestamp
  └── updatedAt: Timestamp

warranties/{warrantyId}
  ├── assetId: String
  ├── userId: String
  ├── type: String                       (MANUFACTURER | EXTENDED | INSURANCE | SELLER)
  ├── startDate: Timestamp
  ├── durationMonths: Number
  ├── endDate: Timestamp                 (calculé à la création)
  ├── provider: String
  ├── conditions: String
  ├── documentIds: Array<String>
  └── createdAt: Timestamp

maintenances/{maintenanceId}
  ├── assetId: String
  ├── userId: String
  ├── title: String
  ├── description: String
  ├── type: String                       (ONE_TIME | RECURRING | REPAIR)
  ├── date: Timestamp
  ├── cost: Number
  ├── provider: String
  ├── mileage: Number?
  ├── isCompleted: Boolean
  ├── recurrenceMonths: Number?
  ├── nextDueDate: Timestamp?
  ├── nextDueMileage: Number?
  ├── documentIds: Array<String>
  └── createdAt: Timestamp

documents/{documentId}
  ├── assetId: String
  ├── userId: String
  ├── name: String
  ├── type: String                       (INVOICE | WARRANTY_CERT | MAINTENANCE_REPORT | ...)
  ├── fileUrl: String
  ├── thumbnailUrl: String
  ├── mimeType: String
  ├── fileSize: Number
  ├── ocrText: String?
  └── createdAt: Timestamp

reminders/{reminderId}
  ├── userId: String
  ├── assetId: String
  ├── referenceId: String
  ├── referenceType: String              (WARRANTY | MAINTENANCE)
  ├── title: String
  ├── message: String
  ├── triggerDate: Timestamp
  ├── isTriggered: Boolean
  ├── daysBefore: Number
  └── createdAt: Timestamp
```

### Index Firestore composites
```
assets       → (userId ASC, createdAt DESC)
assets       → (userId ASC, category ASC, createdAt DESC)
warranties   → (userId ASC, endDate ASC)
warranties   → (assetId ASC, createdAt DESC)
maintenances → (userId ASC, nextDueDate ASC)
maintenances → (assetId ASC, date DESC)
maintenances → (userId ASC, isCompleted ASC, nextDueDate ASC)
documents    → (assetId ASC, createdAt DESC)
reminders    → (userId ASC, triggerDate ASC, isTriggered ASC)
```

### Règles de sécurité Firestore
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    function isAuth() {
      return request.auth != null;
    }

    function isOwner(userId) {
      return isAuth() && request.auth.uid == userId;
    }

    // Vérifie que le userId dans le document correspond à l'utilisateur connecté
    function isDocOwner() {
      return isAuth() && resource.data.userId == request.auth.uid;
    }

    function isNewDocOwner() {
      return isAuth() && request.resource.data.userId == request.auth.uid;
    }

    match /users/{userId} {
      allow read, write: if isOwner(userId);
    }

    match /assets/{assetId} {
      allow read, update, delete: if isDocOwner();
      allow create: if isNewDocOwner();
      allow list: if isAuth();
    }

    match /warranties/{warrantyId} {
      allow read, update, delete: if isDocOwner();
      allow create: if isNewDocOwner();
      allow list: if isAuth();
    }

    match /maintenances/{maintenanceId} {
      allow read, update, delete: if isDocOwner();
      allow create: if isNewDocOwner();
      allow list: if isAuth();
    }

    match /documents/{documentId} {
      allow read, update, delete: if isDocOwner();
      allow create: if isNewDocOwner();
      allow list: if isAuth();
    }

    match /reminders/{reminderId} {
      allow read, update, delete: if isDocOwner();
      allow create: if isNewDocOwner();
      allow list: if isAuth();
    }
  }
}
```

---

## 6. Firebase Storage — Structure

```
users/{userId}/
  ├── assets/{assetId}/
  │   └── photo.jpg
  └── documents/{documentId}/
      ├── original.pdf
      └── thumbnail.jpg
```

### Règles de sécurité Storage
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

## 7. Dépendances (build.gradle.kts)

```kotlin
// === Compose ===
implementation(platform("androidx.compose:compose-bom:2024.12.01"))
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.material:material-icons-extended")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.activity:activity-compose:1.9.3")
implementation("androidx.navigation:navigation-compose:2.8.5")
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

// === Firebase ===
implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-storage-ktx")
implementation("com.google.firebase:firebase-messaging-ktx")

// === Hilt ===
implementation("com.google.dagger:hilt-android:2.51.1")
ksp("com.google.dagger:hilt-android-compiler:2.51.1")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
implementation("androidx.hilt:hilt-work:1.2.0")
ksp("androidx.hilt:hilt-compiler:1.2.0")

// === CameraX ===
implementation("androidx.camera:camera-core:1.4.1")
implementation("androidx.camera:camera-camera2:1.4.1")
implementation("androidx.camera:camera-lifecycle:1.4.1")
implementation("androidx.camera:camera-view:1.4.1")

// === ML Kit ===
implementation("com.google.mlkit:document-scanner:16.0.0-beta1")
implementation("com.google.mlkit:text-recognition:16.0.1")

// === Images ===
implementation("io.coil-kt:coil-compose:2.7.0")

// === WorkManager ===
implementation("androidx.work:work-runtime-ktx:2.10.0")

// === DataStore ===
implementation("androidx.datastore:datastore-preferences:1.1.1")

// === Tests ===
testImplementation("junit:junit:4.13.2")
testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
androidTestImplementation("androidx.compose.ui:ui-test-junit4")
```

---

## 8. Navigation

```
SplashScreen (auth anonyme auto)
    │
    ▼
HomeScreen (Dashboard)
    ├── Garanties qui expirent bientôt (J-30, J-7)
    ├── Prochaines révisions programmées
    ├── Nombre total de biens
    └── Actions rapides : ➕ Ajouter bien, 📸 Scanner
    │
    ├──→ AssetListScreen
    │       ├── Recherche par nom
    │       ├── Filtres par catégorie
    │       └── Tri (date achat, nom, prix)
    │            │
    │            └──→ AssetDetailScreen
    │                    ├── Infos du bien
    │                    ├── Garantie(s) avec badge statut coloré
    │                    ├── Historique entretiens (timeline)
    │                    ├── Documents scannés (thumbnails)
    │                    └── Actions : Modifier, Supprimer
    │                         │
    │                         ├──→ WarrantyFormScreen
    │                         ├──→ MaintenanceFormScreen
    │                         └──→ ScannerScreen
    │
    ├──→ ScannerScreen
    │       ├── Caméra avec détection de bords
    │       ├── Recadrage automatique
    │       ├── OCR (extraction texte)
    │       └── Association à un bien existant ou nouveau
    │
    └──→ SettingsScreen
            ├── Profil (upgrade anonyme → compte)
            ├── Paramétrage des rappels
            ├── Export données (V2)
            └── Déconnexion
```

### Bottom Navigation
```
🏠 Accueil  |  📦 Mes Biens  |  📸 Scanner  |  ⚙️ Paramètres
```

---

## 9. Flux principaux

### Flux 1 — Premier lancement
```
1. SplashScreen → Firebase Auth signInAnonymously()
2. Création document User dans Firestore
3. Redirection → HomeScreen (vide, avec onboarding)
```

### Flux 2 — Ajout d'un bien avec garantie
```
1. HomeScreen → clic "+"
2. AssetFormScreen : nom, catégorie, marque, modèle, date achat, prix
3. Option : photo du bien (CameraX ou galerie)
4. Sauvegarde Asset dans Firestore
5. Proposition : "Ajouter une garantie ?"
6. WarrantyFormScreen : type, startDate, durationMonths, garant
7. Option : scanner le bon de garantie
8. Sauvegarde Warranty + Document
9. Création auto Reminder J-30 et J-7 avant endDate
10. Retour AssetDetailScreen
```

### Flux 3 — Scan de document
```
1. ScannerScreen (bottom nav ou depuis AssetDetail)
2. CameraX avec ML Kit Document Scanner
3. Détection de bords + recadrage auto
4. OCR : extraction du texte
5. Aperçu du scan
6. Choix : associer à un bien existant ou créer un nouveau
7. Type de document : Facture, Garantie, Entretien...
8. Upload Firebase Storage → création Document Firestore
```

### Flux 4 — Notification de rappel
```
1. WorkManager vérifie quotidiennement les échéances
2. Si warranty.endDate - 30j = aujourd'hui → notification J-30
3. Si warranty.endDate - 7j = aujourd'hui → notification J-7
4. Si maintenance.nextDueDate = aujourd'hui → notification révision
5. Clic sur notification → AssetDetailScreen du bien concerné
```

---

## 10. Planning prévisionnel

| Semaine | Tâches | Livrables |
|---------|--------|-----------|
| S1-S2 | Setup projet, Firebase, Auth anonyme, modèles domain, structure Clean | Squelette app + auth fonctionnel |
| S3-S4 | CRUD biens (Asset), liste avec recherche/filtres, détail | Gestion complète des biens |
| S5-S6 | Garanties : CRUD, calcul statut, badges, liaison avec Asset | Module garanties fonctionnel |
| S7-S8 | Scanner CameraX + ML Kit, upload Storage, OCR | Scan et archivage opérationnels |
| S9-S10 | Révisions/entretiens, notifications WorkManager + FCM | Suivi révisions + rappels |
| S11-S12 | Dashboard Home, design/polish, tests, publication Play Store | App V1 publiée |

---

## 11. Évolutions V2+

| Feature | Détail |
|---------|--------|
| **Upgrade auth** | Email/Google Sign-In, liaison compte anonyme |
| **PIN / Biométrie** | Verrouillage app par empreinte ou code |
| **Foyer partagé** | householdId, partage de biens entre utilisateurs |
| **Export PDF** | Récapitulatif de tous les biens + garanties |
| **OCR intelligent** | Extraction auto des dates, montants, noms depuis factures |
| **Migration KMP/iOS** | Refacto commonMain/androidMain/iosMain |
| **Mode multi-foyer** | Gérer plusieurs logements / véhicules |
| **Widget Android** | Garanties expirantes sur l'écran d'accueil |
| **Intégration assurances** | Envoi automatique de justificatifs |

---

## 12. Classe utilitaire — Resource

```kotlin
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
```

## 13. Calcul du statut de garantie

```kotlin
object WarrantyStatusCalculator {

    fun calculate(endDate: Long): WarrantyStatus {
        val now = System.currentTimeMillis()
        val daysRemaining = TimeUnit.MILLISECONDS.toDays(endDate - now)

        return when {
            daysRemaining < 0 -> WarrantyStatus.EXPIRED
            daysRemaining <= 30 -> WarrantyStatus.EXPIRING_SOON
            else -> WarrantyStatus.ACTIVE
        }
    }
}
```
