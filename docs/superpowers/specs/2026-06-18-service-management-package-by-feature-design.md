# Design: service_management Package-by-Feature Konsolidierung

**Datum:** 2026-06-18  
**Branch:** feature/refactor-service-mgmt  
**Status:** Approved

## Ziel

Das Modul `service_management` wurde von Package-by-Layer auf Package-by-Feature umgebaut. Dieser Spec beschreibt die Bereinigung verbleibender Inkonsistenzen: falsche Package-Deklarationen, inkohärente Sub-Pakete in `service_deployment.api`, tote Duplikat-Exceptions sowie fehlerhafte Maven-Abhängigkeiten.

## Scope

**In Scope:**
- Package-Deklarationen und Ordnerstrukturen aller falsch benannten Klassen korrigieren
- `service_deployment.api`: `api.services` + `api.serviceinstances` zu `api.serviceinstances` zusammenführen
- Tote Duplikat-Exceptions entfernen
- `AbstractRestControllerIT` vom falschen Source-Set (`src/main/java`) in `src/test/java` verschieben
- Maven-Abhängigkeiten korrigieren (4 Stellen)

**Außerhalb Scope:**
- `service.initializer` strukturell umordnen (eigenständige Komponente)
- Inhaltliche Änderungen an Business-Logik
- Maven-Modul-Grenzen (pom-Hierarchie) ändern

---

## Abschnitt 1: Package-Deklarationen korrigieren

### 1a — Root-Package-Klassen in `common.impl`

Zwei Klassen liegen physisch in `service_management.common.impl`, deklarieren aber das alte Root-Package `org.eclipse.slm.service_management`.

| Klasse | Aktuelles Package | Ziel-Package |
|---|---|---|
| `AbstractRestControllerIT` | `org.eclipse.slm.service_management` | `org.eclipse.slm.service_management.common.impl` |
| `MultiTenancyUtil` | `org.eclipse.slm.service_management` | `org.eclipse.slm.service_management.common.impl` |

Vorgehen: Package-Deklaration anpassen, Ordner gemäß neuem Package-Pfad anlegen, alte leere Ordner entfernen. Alle Importe dieser Klassen in anderen Modulen aktualisieren.

### 1b — `AbstractRestControllerIT` ins Test-Source-Set verschieben

`AbstractRestControllerIT` ist eine Test-Basisklasse und liegt fälschlicherweise in `src/main/java`. Ziel: `src/test/java` im Modul `service_management.common.impl`.

### 1c — Tests in `service_deployment.impl` mit falschen Packages

14 Testklassen in `service_management.features.service_deployment.impl/src/test/java/` sind in Ordnern unter dem alten Pfad `org/eclipse/slm/service_management/features/service_offerings/api/features/service_deployment/impl/` abgelegt und deklarieren das entsprechend falsche Package.

**Aktuell:** `org.eclipse.slm.service_management.features.service_offerings.api.features.service_deployment.impl`  
**Soll:** `org.eclipse.slm.service_management.features.service_deployment.impl`

Betroffene Klassen:
- `DockerComposeFileParser*Test` (9 Klassen)
- `DockerComposeFileTestSuite`
- `DockerComposeFileVolumeTest`
- `KubernetesManifestFileParserTest`
- `ServiceDeploymentHandlerTest`
- `DockerComposeFileParserTestUtil`

### 1d — `DTOTest` in `common.impl/test`

**Aktuell:** `org.eclipse.slm.service_management.features.service_offerings.api.common.impl`  
**Soll:** `org.eclipse.slm.service_management.common.impl`

### 1e — Persistence-Tests in `service_offerings.impl/test`

5 Testklassen sind im Ordner `api/persistence/...` abgelegt und deklarieren `...api.persistence.*` — sie liegen aber im `impl`-Modul und gehören nicht in den `api`-Namensraum.

| Klasse | Aktuelles Package | Ziel-Package |
|---|---|---|
| `ServiceOfferingCategoryIT` | `...api.persistence.mariadb.test` | `...impl.persistence.mariadb` |
| `ServiceOfferingRepositoryIT` | `...api.persistence.mariadb.test` | `...impl.persistence.mariadb` |
| `ServiceOfferingVersionRepositoryIT` | `...api.persistence.mariadb.test` | `...impl.persistence.mariadb` |
| `ServiceVendorIT` | `...api.persistence.mariadb.test` | `...impl.persistence.mariadb` |
| `SpringTestConfiguration` | `...api.persistence.mariadb.test` | `...impl.persistence.mariadb` |
| `ServiceVendorRepositoryTest` | `...api.persistence.keycloak` | `...impl.persistence.keycloak` |

### 1f — Tests in `service.initializer/test`

2 Testklassen deklarieren `...features.service_offerings.api.service.initializer`.

| Klasse | Aktuelles Package | Ziel-Package |
|---|---|---|
| `DTOTest` | `...features.service_offerings.api.service.initializer` | `...service.initializer` |
| `GitRepoTests` | `...features.service_offerings.api.service.initializer` | `...service.initializer` |

---

## Abschnitt 2: `service_deployment.api` Konsolidierung

### Ist-Zustand

In `service_deployment.api` existieren zwei Sub-Pakete für dasselbe Konzept (Service-Instanzen):

- **`api.services`**: Domain-Modelle (`ServiceInstance`, `ServiceInstanceDetails`, `ServiceInstanceGroup`, `ServiceLifecycleAction`, `ServiceLifecycleState`) + tote Exception
- **`api.serviceinstances`**: REST-API-Interfaces (`ServiceInstancesRestApi`, `ServiceInstancesGroupsRestApi` + Config-Klassen) + Exceptions
- **`api` Root**: `AvailableServiceInstanceVersionChange`, `AvailableServiceInstanceVersionChangeType` (ohne Sub-Paket)

### Ziel-Zustand: Alles in `api.serviceinstances`

```
service_deployment.api/
├── deployment/
│   ├── CapabilityServiceNotFoundException.java
│   ├── ServiceOfferingOrderService.java
│   ├── ServiceOrder.kt
│   └── ServiceOrderResult.kt
└── serviceinstances/
    ├── AvailableServiceInstanceVersionChange.kt        (verschoben aus api Root)
    ├── AvailableServiceInstanceVersionChangeType.kt    (verschoben aus api Root)
    ├── ServiceInstance.kt                              (verschoben aus api.services)
    ├── ServiceInstanceDetails.kt                       (verschoben aus api.services)
    ├── ServiceInstanceGroup.kt                         (verschoben aus api.services)
    ├── ServiceLifecycleAction.kt                       (verschoben aus api.services)
    ├── ServiceLifecycleState.kt                        (verschoben aus api.services)
    ├── ServiceInstanceUpdateException.java             (verschoben aus api.services.exceptions, ist die einzig genutzte Kopie)
    ├── ServiceInstanceNotFoundException.java
    ├── ServiceInstanceGroupNotFoundException.java
    ├── ServiceInstanceRuntimeException.java
    ├── ServiceInstancesRestApi.java
    ├── ServiceInstancesRestApiConfig.java
    ├── ServiceInstancesGroupsRestApi.java
    └── ServiceInstancesGroupsRestApiConfig.java
```

### Exception-Bereinigung

`ServiceInstanceUpdateException` existiert 3× — nur eine wird tatsächlich genutzt:

| Kopie | Package | Status | Aktion |
|---|---|---|---|
| Java, `@ResponseStatus(BAD_REQUEST)` | `api.services.exceptions` | **Live** — importiert von `ServiceInstancesRestApi`, `ServiceInstancesHandler`, `ServiceInstancesRestController` | Verschieben nach `api.serviceinstances`, Imports aktualisieren |
| Java, `@ResponseStatus(BAD_REQUEST)` | `api.serviceinstances` | Tot — kein Import | Löschen |
| Kotlin, `@ResponseStatus(INTERNAL_SERVER_ERROR)` | `api.update` | Tot — kein Import, anderer HTTP-Status | Löschen (gesamtes `api.update`-Paket) |

### Import-Aktualisierungen (nach Konsolidierung)

Alle Imports von `...api.services.*` und `...api.services.exceptions.*` → `...api.serviceinstances.*` in:
- `service_deployment.impl`: `ServiceInstancesRestController`, `ServiceInstancesHandler`, `ServiceInstancesGroupsRestController`, `ServiceInstanceEventMessageSender`, `ServiceInstanceGroupJpaRepository`, `ServiceInstancesConsulClient`, `DeploymentJobRun`, `ServiceInstanceEventMessage`, `ServiceDeploymentHandler`, `ServiceUndeploymentHandler`, `ServiceUpdateHandler`
- `service_deployment.api`: `ServiceInstancesRestApi`, `ServiceInstancesGroupsRestApi`

---

## Abschnitt 3: Maven-Korrekturen

### 3a — `service.client` hängt von `service.app` ab (falsch)

`ServiceManagementClient` importiert ausschließlich aus `service_management.features.service_offerings.api.*`. Die Dependency auf `service_management.service.app` ist falsch und zieht die gesamte Anwendung als Compile-Abhängigkeit rein.

**Aktion:** In `service_management.service.client/pom.xml`:
- Dependency auf `service_management.service.app` entfernen
- Dependency auf `service_management.features.service_offerings.api` hinzufügen

### 3b — `io.kubernetes:client-java` im falschen Modul

`io.kubernetes:client-java` liegt in `service_management.common.api/pom.xml`, wird aber ausschließlich in `service_management.features.service_deployment.impl` genutzt (`KubernetesManifestFileParser`, `KubernetesGenericObject`).

**Aktion:**
- Dependency aus `service_management.common.api/pom.xml` entfernen
- Dependency explizit in `service_management.features.service_deployment.impl/pom.xml` hinzufügen

### 3c — Ungenutzter Cross-Feature-Import in `service_offerings.impl`

`service_management.features.service_offerings.impl/pom.xml` deklariert eine Dependency auf `service_management.features.service_deployment.api`, ohne dass irgendeine Klasse in `service_offerings.impl` etwas daraus importiert.

**Aktion:** Dependency aus `service_management.features.service_offerings.impl/pom.xml` entfernen.

### 3d — Falscher `mainClass` in `service.initializer`

Das exec-maven-plugin in `service_management.service.initializer/pom.xml` referenziert eine nicht existierende Klasse:

```xml
<!-- Falsch -->
<mainClass>org.eclipse.slm.service_management.features.service_offerings.api.service.initializer.Application</mainClass>

<!-- Richtig -->
<mainClass>org.eclipse.slm.service_management.service.initializer.Application</mainClass>
```

**Aktion:** `mainClass` in `service_management.service.initializer/pom.xml` korrigieren.

---

## Implementierungsreihenfolge

1. **Maven 3d** — `mainClass` in `service.initializer` korrigieren (isolierter Bug, keine Abhängigkeiten)
2. **Maven 3a** — `service.client` Dependency auf `service.app` → `service_offerings.api`
3. **Maven 3b** — Kubernetes-Dependency nach `service_deployment.impl` verschieben
4. **Maven 3c** — Ungenutzten Cross-Feature-Import in `service_offerings.impl` entfernen
5. **Package 1a/1b** — `AbstractRestControllerIT` + `MultiTenancyUtil`: Package + Source-Set korrigieren, alle Imports aktualisieren
6. **Package 2** — `service_deployment.api` konsolidieren: Exceptions bereinigen, `services` → `serviceinstances` zusammenführen, Imports in `impl` aktualisieren
7. **Package 1c** — Tests in `service_deployment.impl`: Ordner umbenennen, Package-Deklarationen korrigieren
8. **Package 1d/1e/1f** — Restliche Tests: `common.impl/DTOTest`, Persistence-Tests in `service_offerings.impl`, Tests in `service.initializer`
