# service_management Package-by-Feature Konsolidierung — Implementierungsplan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Das Modul `service_management` vollständig auf Package-by-Feature ausrichten: falsche Package-Deklarationen, inkohärente Sub-Pakete, tote Exceptions und fehlerhafte Maven-Abhängigkeiten bereinigen.

**Architecture:** Reines Refactoring — keine Logikänderungen. Alle Änderungen sind rein struktureller Natur (Package-Deklarationen, Ordnerpfade, pom.xml-Abhängigkeiten). Die Maven-Modul-Hierarchie bleibt unverändert.

**Tech Stack:** Java 21, Kotlin 2.2, Maven Multi-Module, Spring Boot, JUnit 5

---

## Dateien-Übersicht

**Modifiziert (pom.xml):**
- `service_management/service_management.service/service_management.service.initializer/pom.xml`
- `service_management/service_management.service/service_management.service.client/pom.xml`
- `service_management/service_management.common/service_management.common.api/pom.xml`
- `service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.impl/pom.xml`
- `service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/pom.xml`

**Verschoben + Package-Deklaration geändert:**
- `common.impl/src/main/java/.../AbstractRestControllerIT.java` → `src/test/java/org/eclipse/slm/service_management/common/impl/`
- `common.impl/src/main/java/.../MultiTenancyUtil.java` → `org/eclipse/slm/service_management/common/impl/`
- `common.impl/src/test/java/.../features/service_offerings/api/common/impl/DTOTest.java` → `org/eclipse/slm/service_management/common/impl/`
- 14 Tests in `service_deployment.impl/src/test/java/.../features/service_offerings/api/features/service_deployment/impl/` → `org/eclipse/slm/service_management/features/service_deployment/impl/`
- 6 Persistence-Tests in `service_offerings.impl/src/test/java/.../api/persistence/*/` → `.../impl/persistence/*/`
- 2 Tests in `service.initializer/src/test/java/.../features/service_offerings/api/service/initializer/` → `.../service/initializer/`

**Verschoben (service_deployment.api Konsolidierung):**
- `api/services/ServiceInstance.kt` → `api/serviceinstances/`
- `api/services/ServiceInstanceDetails.kt` → `api/serviceinstances/`
- `api/services/ServiceInstanceGroup.kt` → `api/serviceinstances/`
- `api/services/ServiceLifecycleAction.kt` → `api/serviceinstances/`
- `api/services/ServiceLifecycleState.kt` → `api/serviceinstances/`
- `api/services/exceptions/ServiceInstanceUpdateException.java` → `api/serviceinstances/`
- `api/AvailableServiceInstanceVersionChange.kt` → `api/serviceinstances/`
- `api/AvailableServiceInstanceVersionChangeType.kt` → `api/serviceinstances/`

**Gelöscht:**
- `api/serviceinstances/ServiceInstanceUpdateException.java` (totes Duplikat)
- `api/update/ServiceInstanceUpdateException.kt` (totes Duplikat)

**Import-Aktualisierungen (nach Konsolidierung):**
- `service_deployment.api`: `ServiceInstancesRestApi.java`, `ServiceInstancesGroupsRestApi.java`
- `service_deployment.impl`: `ServiceInstancesRestController.java`, `ServiceInstancesHandler.java`, `ServiceInstancesGroupsRestController.java`, `ServiceInstanceEventMessageSender.java`, `ServiceInstanceGroupJpaRepository.java`, `ServiceInstancesConsulClient.java`, `DeploymentJobRun.kt`, `ServiceInstanceEventMessage.kt`, `ServiceDeploymentHandler.java`, `ServiceUndeploymentHandler.java`, `ServiceUpdateHandler.java`

---

## Task 1: Maven — `service.initializer` mainClass korrigieren

**Files:**
- Modify: `service_management/service_management.service/service_management.service.initializer/pom.xml`

- [ ] **Schritt 1: mainClass in pom.xml korrigieren**

In `service_management/service_management.service/service_management.service.initializer/pom.xml` den falschen `mainClass`-Eintrag ersetzen:

```xml
<!-- Alt (falsch): -->
<mainClass>org.eclipse.slm.service_management.features.service_offerings.api.service.initializer.Application</mainClass>

<!-- Neu (korrekt): -->
<mainClass>org.eclipse.slm.service_management.service.initializer.Application</mainClass>
```

- [ ] **Schritt 2: Kompilierung prüfen**

```bash
mvn compile -f service_management/service_management.service/service_management.service.initializer/pom.xml
```

Erwartet: `BUILD SUCCESS`

- [ ] **Schritt 3: Commit**

```bash
git add service_management/service_management.service/service_management.service.initializer/pom.xml
git commit -m "fix: correct mainClass in service.initializer pom.xml"
```

---

## Task 2: Maven — `service.client` hängt von `service.app` ab (falsch)

**Files:**
- Modify: `service_management/service_management.service/service_management.service.client/pom.xml`

`ServiceManagementClient` importiert ausschließlich aus `service_offerings.api`. Die Dependency auf `service.app` zieht die gesamte Anwendung als Compile-Abhängigkeit rein.

- [ ] **Schritt 1: Dependency in service.client/pom.xml ersetzen**

In `service_management/service_management.service/service_management.service.client/pom.xml`:

```xml
<!-- Entfernen: -->
<dependency>
    <groupId>org.eclipse.slm</groupId>
    <artifactId>service_management.service.app</artifactId>
    <version>${project.version}</version>
    <scope>compile</scope>
</dependency>

<!-- Hinzufügen: -->
<dependency>
    <groupId>org.eclipse.slm</groupId>
    <artifactId>service_management.features.service_offerings.api</artifactId>
    <version>${project.version}</version>
</dependency>
```

- [ ] **Schritt 2: Kompilierung prüfen**

```bash
mvn compile -f service_management/pom.xml -pl service_management.service.client --also-make
```

Erwartet: `BUILD SUCCESS`

- [ ] **Schritt 3: Commit**

```bash
git add service_management/service_management.service/service_management.service.client/pom.xml
git commit -m "fix: replace service.app dependency with service_offerings.api in service.client"
```

---

## Task 3: Maven — Kubernetes-Dependency ins richtige Modul verschieben

**Files:**
- Modify: `service_management/service_management.common/service_management.common.api/pom.xml`
- Modify: `service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.impl/pom.xml`

`io.kubernetes:client-java` wird nur in `service_deployment.impl` genutzt (`KubernetesManifestFileParser`, `KubernetesGenericObject`), liegt aber in `common.api` — wird dadurch transitiv in alle abhängigen Module gezogen.

- [ ] **Schritt 1: Dependency aus `common.api/pom.xml` entfernen**

In `service_management/service_management.common/service_management.common.api/pom.xml` folgendes entfernen:

```xml
<dependency>
    <groupId>io.kubernetes</groupId>
    <artifactId>client-java</artifactId>
    <version>17.0.1</version>
</dependency>
```

- [ ] **Schritt 2: Dependency in `service_deployment.impl/pom.xml` hinzufügen**

In `service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.impl/pom.xml` hinzufügen:

```xml
<dependency>
    <groupId>io.kubernetes</groupId>
    <artifactId>client-java</artifactId>
    <version>17.0.1</version>
</dependency>
```

- [ ] **Schritt 3: Kompilierung prüfen**

```bash
mvn compile -f service_management/pom.xml -pl service_management.features.service_deployment.impl --also-make
```

Erwartet: `BUILD SUCCESS`

- [ ] **Schritt 4: Commit**

```bash
git add service_management/service_management.common/service_management.common.api/pom.xml
git add service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.impl/pom.xml
git commit -m "fix: move kubernetes client dependency to service_deployment.impl"
```

---

## Task 4: Maven — Ungenutzten Cross-Feature-Import entfernen

**Files:**
- Modify: `service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/pom.xml`

`service_offerings.impl` deklariert eine Dependency auf `service_deployment.api`, obwohl kein Code daraus importiert.

- [ ] **Schritt 1: Dependency entfernen**

In `service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/pom.xml` folgendes entfernen:

```xml
<dependency>
    <groupId>org.eclipse.slm</groupId>
    <artifactId>service_management.features.service_deployment.api</artifactId>
    <version>${project.version}</version>
</dependency>
```

- [ ] **Schritt 2: Kompilierung prüfen**

```bash
mvn compile -f service_management/pom.xml -pl service_management.features.service_offerings.impl --also-make
```

Erwartet: `BUILD SUCCESS`

- [ ] **Schritt 3: Commit**

```bash
git add service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/pom.xml
git commit -m "fix: remove unused service_deployment.api dependency from service_offerings.impl"
```

---

## Task 5: Package — `AbstractRestControllerIT` und `MultiTenancyUtil` korrigieren

**Files:**
- Delete + Create: `common.impl/src/main/java/.../service_management/AbstractRestControllerIT.java` → `src/test/java/org/eclipse/slm/service_management/common/impl/AbstractRestControllerIT.java`
- Delete + Create: `common.impl/src/main/java/.../service_management/MultiTenancyUtil.java` → `src/main/java/org/eclipse/slm/service_management/common/impl/MultiTenancyUtil.java`
- Modify: `common.impl/src/main/java/.../common/impl/users/UsersRestController.java`
- Modify: 4 Testdateien in `service_offerings.impl/src/test/`

Basispfad `common.impl`: `service_management/service_management.common/service_management.common.impl`

- [ ] **Schritt 1: `MultiTenancyUtil` verschieben und Package korrigieren**

Neuen Ordner anlegen und Datei erstellen:
```
common.impl/src/main/java/org/eclipse/slm/service_management/common/impl/MultiTenancyUtil.java
```

Inhalt (nur Package-Deklaration ändert sich):
```java
package org.eclipse.slm.service_management.common.impl;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.UUID;

public class MultiTenancyUtil {

    public static String getKeycloakUserNameFromAuthenticationToken(JwtAuthenticationToken jwtAuthenticationToken)
    {
        var username = jwtAuthenticationToken.getToken().getClaims().get("preferred_username").toString();
        return username;
    }

    public static UUID getKeycloakUserIdFromAuthenticationToken(JwtAuthenticationToken jwtAuthenticationToken)
    {
        var subject = jwtAuthenticationToken.getToken().getSubject();
        return UUID.fromString(subject);
    }
}
```

Alte Datei löschen:
```bash
rm service_management/service_management.common/service_management.common.impl/src/main/java/org/eclipse/slm/service_management/MultiTenancyUtil.java
```

- [ ] **Schritt 2: `AbstractRestControllerIT` ins Test-Source-Set verschieben**

Neuen Ordner anlegen und Datei erstellen:
```
common.impl/src/test/java/org/eclipse/slm/service_management/common/impl/AbstractRestControllerIT.java
```

Inhalt:
```java
package org.eclipse.slm.service_management.common.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractRestControllerIT {

    @Autowired
    protected ObjectMapper objectMapper;

    protected static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
```

Alte Datei löschen:
```bash
rm service_management/service_management.common/service_management.common.impl/src/main/java/org/eclipse/slm/service_management/AbstractRestControllerIT.java
```

Leeren alten Ordner entfernen (falls leer):
```bash
rmdir service_management/service_management.common/service_management.common.impl/src/main/java/org/eclipse/slm/service_management/ 2>/dev/null || true
```

- [ ] **Schritt 3: Import in `UsersRestController.java` aktualisieren**

In `common.impl/src/main/java/org/eclipse/slm/service_management/common/impl/users/UsersRestController.java`:

```java
// Alt:
import org.eclipse.slm.service_management.MultiTenancyUtil;

// Neu:
import org.eclipse.slm.service_management.common.impl.MultiTenancyUtil;
```

- [ ] **Schritt 4: Import in 4 Test-Dateien in `service_offerings.impl` aktualisieren**

In allen folgenden Dateien unter `service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/impl/`:

- `ServiceOfferingCategoriesRestControllerTest.java`
- `ServiceOfferingRestControllerIT.java`
- `ServiceRepositoriesRestControllerIT.java`
- `ServiceVendorsRestControllerTest.java`

In jeder Datei:
```java
// Alt:
import org.eclipse.slm.service_management.AbstractRestControllerIT;

// Neu:
import org.eclipse.slm.service_management.common.impl.AbstractRestControllerIT;
```

- [ ] **Schritt 5: Kompilierung prüfen**

```bash
mvn compile test-compile -f service_management/pom.xml -pl service_management.common.impl,service_management.features.service_offerings.impl --also-make
```

Erwartet: `BUILD SUCCESS`

- [ ] **Schritt 6: Commit**

```bash
git add service_management/service_management.common/
git add service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/impl/
git commit -m "refactor: move AbstractRestControllerIT to test source set and fix packages for common.impl classes"
```

---

## Task 6: Package — `service_deployment.api` konsolidieren

**Files:**
Basispfad: `service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.api/src/main/java/org/eclipse/slm/service_management/features/service_deployment/api/`

Alle 5 Domain-Modelle aus `services/` und die 2 Root-Klassen werden nach `serviceinstances/` verschoben. Package-Deklarationen werden entsprechend angepasst. Tote Exceptions werden gelöscht. Alle Imports in `api` und `impl` werden aktualisiert.

- [ ] **Schritt 1: Tote Exception-Kopien löschen**

```bash
rm service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.api/src/main/java/org/eclipse/slm/service_management/features/service_deployment/api/serviceinstances/ServiceInstanceUpdateException.java

rm service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.api/src/main/java/org/eclipse/slm/service_management/features/service_deployment/api/update/ServiceInstanceUpdateException.kt
rmdir service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.api/src/main/java/org/eclipse/slm/service_management/features/service_deployment/api/update/ 2>/dev/null || true
```

- [ ] **Schritt 2: `ServiceInstanceUpdateException` nach `serviceinstances` verschieben**

Neue Datei anlegen in `api/serviceinstances/ServiceInstanceUpdateException.java` (package von `services.exceptions` nach `serviceinstances`):

```java
package org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ServiceInstanceUpdateException extends Exception {

    public ServiceInstanceUpdateException(String message) {
        super(message);
    }
}
```

Alte Datei und leeren Ordner entfernen:
```bash
rm service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.api/src/main/java/org/eclipse/slm/service_management/features/service_deployment/api/services/exceptions/ServiceInstanceUpdateException.java
rmdir service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.api/src/main/java/org/eclipse/slm/service_management/features/service_deployment/api/services/exceptions/ 2>/dev/null || true
```

- [ ] **Schritt 3: Domain-Modelle von `services/` nach `serviceinstances/` verschieben**

Für jede der folgenden 5 Dateien: Package-Deklaration von `...api.services` auf `...api.serviceinstances` ändern und Datei in den Ordner `serviceinstances/` verschieben.

**ServiceInstance.kt** — Package-Deklaration ändern:
```kotlin
// Alt:
package org.eclipse.slm.service_management.features.service_deployment.api.services

// Neu:
package org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances
```
Datei verschieben: `api/services/ServiceInstance.kt` → `api/serviceinstances/ServiceInstance.kt`

Analog für alle weiteren:
- `api/services/ServiceInstanceDetails.kt` → `api/serviceinstances/ServiceInstanceDetails.kt`
- `api/services/ServiceInstanceGroup.kt` → `api/serviceinstances/ServiceInstanceGroup.kt`
- `api/services/ServiceLifecycleAction.kt` → `api/serviceinstances/ServiceLifecycleAction.kt`
- `api/services/ServiceLifecycleState.kt` → `api/serviceinstances/ServiceLifecycleState.kt`

Nach dem Verschieben: Ordner `api/services/` löschen:
```bash
rmdir service_management/.../api/src/main/java/org/eclipse/slm/service_management/features/service_deployment/api/services/ 2>/dev/null || true
```

- [ ] **Schritt 4: `AvailableServiceInstanceVersionChange/Type` nach `serviceinstances` verschieben**

**AvailableServiceInstanceVersionChange.kt** — Package-Deklaration ändern:
```kotlin
// Alt:
package org.eclipse.slm.service_management.features.service_deployment.api

// Neu:
package org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances
```
Datei verschieben: `api/AvailableServiceInstanceVersionChange.kt` → `api/serviceinstances/AvailableServiceInstanceVersionChange.kt`

Analog für `AvailableServiceInstanceVersionChangeType.kt`.

- [ ] **Schritt 5: Imports in `service_deployment.api` aktualisieren**

**`api/serviceinstances/ServiceInstancesRestApi.java`** — folgende Imports ersetzen:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.AvailableServiceInstanceVersionChange;
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstanceDetails;
import org.eclipse.slm.service_management.features.service_deployment.api.services.exceptions.ServiceInstanceUpdateException;

// Neu (alle vier ersetzen durch):
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.AvailableServiceInstanceVersionChange;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceDetails;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceUpdateException;
```

**`api/serviceinstances/ServiceInstancesGroupsRestApi.java`**:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstanceGroup;

// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceGroup;
```

- [ ] **Schritt 6: Imports in `service_deployment.impl` aktualisieren**

In allen betroffenen Dateien unter `service_deployment.impl/src/main/java/` alle Imports von `...api.services.*` und `...api.services.exceptions.*` auf `...api.serviceinstances.*` umschreiben.

Betroffene Dateien und ihre konkreten Import-Änderungen:

**`impl/DeploymentJobRun.kt`**:
```kotlin
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstance
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance
```

**`impl/ServiceInstanceEventMessage.kt`**:
```kotlin
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstance
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance
```

**`impl/deployment/ServiceDeploymentHandler.java`**:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstance;
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
```

**`impl/serviceinstances/ServiceInstanceEventMessageSender.java`**:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstance;
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
```

**`impl/serviceinstances/ServiceInstanceGroupJpaRepository.java`**:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstanceGroup;
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceGroup;
```

**`impl/serviceinstances/ServiceInstancesConsulClient.java`**:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstance;
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
```

**`impl/serviceinstances/ServiceInstancesGroupsRestController.java`**:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstanceGroup;
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceGroup;
```

**`impl/serviceinstances/ServiceInstancesHandler.java`**:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstanceDetails;
import org.eclipse.slm.service_management.features.service_deployment.api.services.exceptions.ServiceInstanceUpdateException;
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceDetails;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceUpdateException;
```

**`impl/serviceinstances/ServiceInstancesRestController.java`**:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstanceDetails;
import org.eclipse.slm.service_management.features.service_deployment.api.services.exceptions.ServiceInstanceUpdateException;
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceDetails;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstanceUpdateException;
```

**`impl/serviceinstances/ServiceInstancesRestController.java`** — auch `AvailableServiceInstanceVersionChange`:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.AvailableServiceInstanceVersionChange;
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.AvailableServiceInstanceVersionChange;
```

**`impl/serviceinstances/ServiceInstancesHandler.java`** — auch `AvailableServiceInstanceVersionChange/Type`:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.AvailableServiceInstanceVersionChange;
import org.eclipse.slm.service_management.features.service_deployment.api.AvailableServiceInstanceVersionChangeType;
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.AvailableServiceInstanceVersionChange;
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.AvailableServiceInstanceVersionChangeType;
```

**`impl/undeployment/ServiceUndeploymentHandler.java`**:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstance;
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
```

**`impl/update/ServiceUpdateHandler.java`**:
```java
// Alt:
import org.eclipse.slm.service_management.features.service_deployment.api.services.ServiceInstance;
// Neu:
import org.eclipse.slm.service_management.features.service_deployment.api.serviceinstances.ServiceInstance;
```

- [ ] **Schritt 7: Kompilierung prüfen**

```bash
mvn compile -f service_management/pom.xml -pl service_management.features.service_deployment.impl --also-make
```

Erwartet: `BUILD SUCCESS`

- [ ] **Schritt 8: Commit**

```bash
git add service_management/service_management.features/service_management.features.service_deployment/
git commit -m "refactor: consolidate service_deployment.api.services into serviceinstances, remove dead exceptions"
```

---

## Task 7: Package — Tests in `service_deployment.impl` korrigieren

**Files:**
Basispfad: `service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.impl/src/test/java/`

16 Testklassen liegen im Ordner `.../features/service_offerings/api/features/service_deployment/impl/` und deklarieren das falsche Package.

- [ ] **Schritt 1: Neuen Ziel-Ordner anlegen**

```bash
mkdir -p service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.impl/src/test/java/org/eclipse/slm/service_management/features/service_deployment/impl/
```

- [ ] **Schritt 2: Package-Deklaration in allen 14 Dateien korrigieren**

In **jeder** der folgenden Dateien die Package-Deklaration ändern:

```java
// Alt:
package org.eclipse.slm.service_management.features.service_offerings.api.features.service_deployment.impl;

// Neu:
package org.eclipse.slm.service_management.features.service_deployment.impl;
```

Dateien (alle im alten Pfad `features/service_offerings/api/features/service_deployment/impl/`):
1. `DockerComposeFileParserCommandTest.java`
2. `DockerComposeFileParserDependsOnTest.java`
3. `DockerComposeFileParserDevicesTest.java`
4. `DockerComposeFileParserEnvFileTest.java`
5. `DockerComposeFileParserEnvironmentTest.java`
6. `DockerComposeFileParserExtraHostsTest.java`
7. `DockerComposeFileParserLabelsTest.java`
8. `DockerComposeFileParserNetworkModeTest.java`
9. `DockerComposeFileParserPortMappingsTest.java`
10. `DockerComposeFileParserTest.java`
11. `DockerComposeFileParserTestUtil.java`
12. `DockerComposeFileParserVolumesTest.java`
13. `DockerComposeFileTestSuite.java`
14. `DockerComposeFileVolumeTest.java`
15. `KubernetesManifestFileParserTest.java`
16. `ServiceDeploymentHandlerTest.java`

- [ ] **Schritt 3: `DockerComposeFileParserPortMappingsTest.java` — kaputten Import korrigieren**

Diese Datei hat zusätzlich einen alten Import:
```java
// Alt:
import org.eclipse.slm.service_management.offerings.options.*;

// Neu:
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.options.*;
```

- [ ] **Schritt 4: Dateien in neuen Ordner verschieben**

```bash
OLD=service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/api/features/service_deployment/impl
NEW=service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.impl/src/test/java/org/eclipse/slm/service_management/features/service_deployment/impl
git mv "$OLD"/DockerComposeFileParserCommandTest.java    "$NEW"/
git mv "$OLD"/DockerComposeFileParserDependsOnTest.java  "$NEW"/
git mv "$OLD"/DockerComposeFileParserDevicesTest.java    "$NEW"/
git mv "$OLD"/DockerComposeFileParserEnvFileTest.java    "$NEW"/
git mv "$OLD"/DockerComposeFileParserEnvironmentTest.java "$NEW"/
git mv "$OLD"/DockerComposeFileParserExtraHostsTest.java "$NEW"/
git mv "$OLD"/DockerComposeFileParserLabelsTest.java     "$NEW"/
git mv "$OLD"/DockerComposeFileParserNetworkModeTest.java "$NEW"/
git mv "$OLD"/DockerComposeFileParserPortMappingsTest.java "$NEW"/
git mv "$OLD"/DockerComposeFileParserTest.java           "$NEW"/
git mv "$OLD"/DockerComposeFileParserTestUtil.java       "$NEW"/
git mv "$OLD"/DockerComposeFileParserVolumesTest.java    "$NEW"/
git mv "$OLD"/DockerComposeFileTestSuite.java            "$NEW"/
git mv "$OLD"/DockerComposeFileVolumeTest.java           "$NEW"/
git mv "$OLD"/KubernetesManifestFileParserTest.java      "$NEW"/
git mv "$OLD"/ServiceDeploymentHandlerTest.java          "$NEW"/
```

- [ ] **Schritt 5: Test-Kompilierung prüfen**

```bash
mvn test-compile -f service_management/pom.xml -pl service_management.features.service_deployment.impl --also-make
```

Erwartet: `BUILD SUCCESS`

- [ ] **Schritt 6: Commit**

```bash
git add service_management/service_management.features/service_management.features.service_deployment/service_management.features.service_deployment.impl/src/test/
git commit -m "refactor: fix package declarations for service_deployment.impl test classes"
```

---

## Task 8: Package — `DTOTest` in `common.impl` korrigieren

**Files:**
Basispfad: `service_management/service_management.common/service_management.common.impl/src/test/java/`

- [ ] **Schritt 1: Neuen Ziel-Ordner anlegen**

```bash
mkdir -p service_management/service_management.common/service_management.common.impl/src/test/java/org/eclipse/slm/service_management/common/impl/
```

- [ ] **Schritt 2: Package-Deklaration und Imports in `DTOTest.java` korrigieren**

In `...features/service_offerings/api/common/impl/DTOTest.java`:

```java
// Package (alt):
package org.eclipse.slm.service_management.features.service_offerings.api.common.impl;

// Package (neu):
package org.eclipse.slm.service_management.common.impl;
```

Broken Imports korrigieren:
```java
// Alt:
import org.eclipse.slm.service_management.offerings.ServiceOfferingCategory;
import org.eclipse.slm.service_management.offerings.ServiceOffering;
import org.eclipse.slm.service_management.offerings.ServiceOfferingDTOApi;
import org.eclipse.slm.service_management.model.vendors.ServiceVendor;

// Neu:
import org.eclipse.slm.service_management.features.service_offerings.api.categories.ServiceOfferingCategory;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.ServiceOffering;
import org.eclipse.slm.service_management.features.service_offerings.api.offerings.ServiceOfferingDTOApi;
import org.eclipse.slm.service_management.features.service_offerings.api.vendors.ServiceVendor;
```

- [ ] **Schritt 3: Datei verschieben**

```bash
OLD=service_management/service_management.common/service_management.common.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/api/common/impl
NEW=service_management/service_management.common/service_management.common.impl/src/test/java/org/eclipse/slm/service_management/common/impl
git mv "$OLD/DTOTest.java" "$NEW/DTOTest.java"
```

- [ ] **Schritt 4: Test-Kompilierung prüfen**

```bash
mvn test-compile -f service_management/pom.xml -pl service_management.common.impl --also-make
```

Erwartet: `BUILD SUCCESS`

- [ ] **Schritt 5: Commit**

```bash
git add service_management/service_management.common/service_management.common.impl/src/test/
git commit -m "refactor: fix package and broken imports for DTOTest in common.impl"
```

---

## Task 9: Package — Persistence-Tests in `service_offerings.impl` korrigieren

**Files:**
Basispfad: `service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/java/`

6 Tests liegen in `...api/persistence/` und deklarieren `...api.persistence.*` — sie gehören in `...impl.persistence.*`.

- [ ] **Schritt 1: Neue Ziel-Ordner anlegen**

```bash
mkdir -p service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/impl/persistence/mariadb/
mkdir -p service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/impl/persistence/keycloak/
```

- [ ] **Schritt 2: Package-Deklaration und Imports in MariaDB-Tests korrigieren**

In folgenden 5 Dateien unter `.../api/persistence/mariadb/test/`:

```java
// Package (alt):
package org.eclipse.slm.service_management.features.service_offerings.api.persistence.mariadb.test;

// Package (neu):
package org.eclipse.slm.service_management.features.service_offerings.impl.persistence.mariadb;
```

Broken Import in `ServiceOfferingCategoryIT.java`, `ServiceOfferingRepositoryIT.java`, `ServiceOfferingVersionRepositoryIT.java` korrigieren:
```java
// Alt:
import org.eclipse.slm.service_management.offerings.ServiceOfferingCategory;

// Neu:
import org.eclipse.slm.service_management.features.service_offerings.api.categories.ServiceOfferingCategory;
```

**Hinweis zu String-Assertions in `ServiceOfferingRepositoryIT` und `ServiceOfferingVersionRepositoryIT`:** Diese Tests prüfen Hibernate-Fehlermeldungen, die den voll-qualifizierten Klassennamen enthalten. Die Strings mit `org.eclipse.slm.service_management.offerings.ServiceOffering` und `org.eclipse.slm.service_management.offerings.ServiceOfferingVersion` müssen auf die neuen Pfade aktualisiert werden:

In `ServiceOfferingRepositoryIT.java`:
```java
// Alt:
.contains("import org.eclipse.slm.service_management.offerings.ServiceOffering.serviceVendor")
// Neu:
.contains("org.eclipse.slm.service_management.features.service_offerings.api.offerings.ServiceOffering.serviceVendor")

// Alt:
.contains("import org.eclipse.slm.service_management.offerings.ServiceOffering.serviceCategory")
// Neu:
.contains("org.eclipse.slm.service_management.features.service_offerings.api.offerings.ServiceOffering.serviceCategory")
```

In `ServiceOfferingVersionRepositoryIT.java`:
```java
// Alt:
.contains("import org.eclipse.slm.service_management.offerings.ServiceOfferingVersion.deploymentDefinition")
// Neu:
.contains("org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOfferingVersion.deploymentDefinition")

// Alt:
.contains("import org.eclipse.slm.service_management.offerings.ServiceOfferingVersion.serviceOffering")
// Neu:
.contains("org.eclipse.slm.service_management.features.service_offerings.api.offeringversions.ServiceOfferingVersion.serviceOffering")
```

- [ ] **Schritt 3: Package und Import in `ServiceVendorRepositoryTest.java` korrigieren**

In `.../api/persistence/keycloak/ServiceVendorRepositoryTest.java`:
```java
// Package (alt):
package org.eclipse.slm.service_management.features.service_offerings.api.persistence.keycloak;

// Package (neu):
package org.eclipse.slm.service_management.features.service_offerings.impl.persistence.keycloak;
```

- [ ] **Schritt 4: Dateien in neue Ordner verschieben**

```bash
OLD_MARIADB=service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/api/persistence/mariadb/test
NEW_MARIADB=service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/impl/persistence/mariadb
git mv "$OLD_MARIADB/ServiceOfferingCategoryIT.java"        "$NEW_MARIADB/"
git mv "$OLD_MARIADB/ServiceOfferingRepositoryIT.java"      "$NEW_MARIADB/"
git mv "$OLD_MARIADB/ServiceOfferingVersionRepositoryIT.java" "$NEW_MARIADB/"
git mv "$OLD_MARIADB/ServiceVendorIT.java"                  "$NEW_MARIADB/"
git mv "$OLD_MARIADB/SpringTestConfiguration.java"          "$NEW_MARIADB/"

OLD_KC=service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/api/persistence/keycloak
NEW_KC=service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/impl/persistence/keycloak
git mv "$OLD_KC/ServiceVendorRepositoryTest.java" "$NEW_KC/"
```

- [ ] **Schritt 5: Test-Kompilierung prüfen**

```bash
mvn test-compile -f service_management/pom.xml -pl service_management.features.service_offerings.impl --also-make
```

Erwartet: `BUILD SUCCESS`

- [ ] **Schritt 6: Commit**

```bash
git add service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/
git commit -m "refactor: fix package declarations and broken imports for persistence tests in service_offerings.impl"
```

---

## Task 10: Package — Tests in `service_offerings.impl` und `service.initializer` korrigieren

**Files:**
- `service_offerings.impl/test`: `ServiceOfferingCategoriesRestControllerTest.java`, `ServiceOfferingCategoryHandlerTest.java` — haben alte Imports
- `service.initializer/test`: `DTOTest.java`, `GitRepoTests.java` — haben falsche Package-Deklarationen

- [ ] **Schritt 1: Broken Import in `ServiceOfferingCategoriesRestControllerTest.java` korrigieren**

In `service_offerings.impl/src/test/java/.../impl/ServiceOfferingCategoriesRestControllerTest.java`:
```java
// Alt:
import org.eclipse.slm.service_management.offerings.ServiceOfferingCategory;

// Neu:
import org.eclipse.slm.service_management.features.service_offerings.api.categories.ServiceOfferingCategory;
```

- [ ] **Schritt 2: Broken Import in `ServiceOfferingCategoryHandlerTest.java` korrigieren**

In `service_offerings.impl/src/test/java/.../impl/ServiceOfferingCategoryHandlerTest.java`:
```java
// Alt:
import org.eclipse.slm.service_management.offerings.ServiceOfferingCategory;

// Neu:
import org.eclipse.slm.service_management.features.service_offerings.api.categories.ServiceOfferingCategory;
```

- [ ] **Schritt 3: Package-Deklaration in `service.initializer`-Tests korrigieren**

Neuen Ordner anlegen:
```bash
mkdir -p service_management/service_management.service/service_management.service.initializer/src/test/java/org/eclipse/slm/service_management/service/initializer/
```

In `service.initializer/src/test/java/.../features/service_offerings/api/service/initializer/DTOTest.java`:
```java
// Alt:
package org.eclipse.slm.service_management.features.service_offerings.api.service.initializer;

// Neu:
package org.eclipse.slm.service_management.service.initializer;
```

In `service.initializer/src/test/java/.../features/service_offerings/api/service/initializer/GitRepoTests.java`:
```java
// Alt:
package org.eclipse.slm.service_management.features.service_offerings.api.service.initializer;

// Neu:
package org.eclipse.slm.service_management.service.initializer;
```

- [ ] **Schritt 4: Initializer-Tests in neuen Ordner verschieben**

```bash
OLD=service_management/service_management.service/service_management.service.initializer/src/test/java/org/eclipse/slm/service_management/features/service_offerings/api/service/initializer
NEW=service_management/service_management.service/service_management.service.initializer/src/test/java/org/eclipse/slm/service_management/service/initializer
git mv "$OLD/DTOTest.java"      "$NEW/"
git mv "$OLD/GitRepoTests.java" "$NEW/"
```

- [ ] **Schritt 5: Test-Kompilierung Gesamtmodul prüfen**

```bash
mvn test-compile -f service_management/pom.xml
```

Erwartet: `BUILD SUCCESS`

- [ ] **Schritt 6: Commit**

```bash
git add service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/impl/ServiceOfferingCategoriesRestControllerTest.java
git add service_management/service_management.features/service_management.features.service_offerings/service_management.features.service_offerings.impl/src/test/java/org/eclipse/slm/service_management/features/service_offerings/impl/ServiceOfferingCategoryHandlerTest.java
git add service_management/service_management.service/service_management.service.initializer/src/test/
git commit -m "refactor: fix remaining broken imports and package declarations in tests"
```

---

## Abschließende Verifikation

- [ ] **Gesamtes `service_management`-Modul kompilieren**

```bash
mvn compile test-compile -f service_management/pom.xml
```

Erwartet: `BUILD SUCCESS`

- [ ] **Keine alten Package-Pfade mehr vorhanden**

```bash
grep -rn "import org.eclipse.slm.service_management\.offerings\.\|import org.eclipse.slm.service_management\.model\." \
  service_management --include="*.java" --include="*.kt" | grep -v target
# Erwartet: kein Output
grep -rn "import org.eclipse.slm.service_management\.features\.service_deployment\.api\.services\." \
  service_management --include="*.java" --include="*.kt" | grep -v target
# Erwartet: kein Output
grep -rn "package org.eclipse.slm.service_management;" \
  service_management --include="*.java" --include="*.kt" | grep -v target
# Erwartet: kein Output
```
