# Resource Management: Migration von Consul-Persistenz zu Datenbank

**Datum:** 2026-06-18
**Branch:** `feature/refactor-resource-mgmt-consul`
**Status:** Design abgestimmt

## Problemstellung

Die Komponente Resource Management nutzt Consul, um angelegte Ressourcen und
Capability Services zu speichern (als Nodes bzw. Services). Consul dient dabei
faktisch als Datenbank. Dieser Ansatz hat sich nicht bewährt. Die Speicherung
soll künftig in der klassischen Datenbank erfolgen, für die im Modul bereits
eine JPA-Integration (MariaDB) besteht (z.B. `Capability`, `Location`,
diverse Job-Entitäten).

Zusätzlich übernimmt Consul aktuell die **feingranulare Zugriffskontrolle**:
Pro Ressource/Capability Service wird eine ACL-Policy erzeugt und über
Binding-Rules der Keycloak-Gruppe des Owners zugeordnet. Beim Lesen wird das
JWT des Users an Consul weitergereicht, das anhand der Gruppenmitgliedschaft
filtert. Diese Consul-basierte Zugriffskontrolle wird ebenfalls entfernt und
durch ein DB-basiertes Berechtigungssystem ersetzt.

## Ziele

- Ressourcen, SingleHost- und MultiHost-/Cluster-Capability-Services sowie
  RemoteAccess werden in der DB statt in Consul gespeichert.
- Ein DB-basiertes, Consul-ähnliches Berechtigungssystem (Policies) ersetzt die
  Consul-ACL-Filterung.
- Das nach außen sichtbare Verhalten (REST-API, Sichtbarkeit pro Gruppe) bleibt
  erhalten.

## Nicht-Ziele

- Keine Migration von Bestandsdaten aus Consul (Greenfield-Neustart akzeptiert).
- Kein explizites Policy-Management-API (mehrere Objekte/Gruppen pro Policy
  manuell verwalten). Das Datenmodell lässt dies zu, die Funktionalität wird in
  diesem Refactoring aber nicht gebaut.
- Keine Erweiterung des Berechtigungsmodells um Rechtearten (read/write/delete);
  Zugriff ist binär (wie heute in Consul: Sichtbarkeit = Zugriff).

## Architektur

Gewählter Ansatz: **Consul-Policy-Modell getreu in der DB nachbilden**, als
wiederverwendbare Access-Control-Komponente, die von allen vier Persistenz-
bereichen genutzt wird.

Die bisherige Consul-Kette *Gruppe → Role → Policy → Node* wird ersetzt durch
*Gruppe (Subject) → Policy → Objekt*, vollständig in der DB.

### Access-Control-Komponente (`…resource_management.common.access`)

**Entitäten:**

- `AccessControlPolicy`: `id: UUID`, `name: String`, `description: String`
- `policy_subjects` (`policyId, subject: String`): die **Gruppen**, für die die
  Policy gilt. `subject` ist ein Gruppen-Pfad, identisch zum bisherigen
  `fullPathOwnerGroupId` bzw. einem Eintrag des `groups`-Claims im JWT.
- `policy_objects` (`policyId, objectType, objectId: UUID`): die **zugreifbaren
  Objekte**, generisch referenziert. `objectType ∈ {RESOURCE,
  CAPABILITY_SERVICE, REMOTE_ACCESS}`.

**Beziehungen:** Policy ↔ Subjects (1:n), Policy ↔ Objekte (n:m — ein Objekt
kann theoretisch in mehreren Policies vorkommen).

**Wichtig:** Die geschützten Entitäten (Resource, CapabilityService,
RemoteAccess) enthalten **keinerlei** Verweis auf Policies. Die Verknüpfung
erfolgt ausschließlich über `policy_objects`.

**`AccessControlService` (Spring `@Component`):**

- `createPolicy(name, subjects, objects) → policyId`
- `addObject / removeObject(policyId, objectType, objectId)`
- `addSubject / removeSubject(policyId, group)`
- `deletePolicy(policyId)`
- `getAccessibleObjectIds(userGroups, objectType) → Set<UUID>`
- `hasAccess(objectType, objectId, userContext) → Boolean`
- **Admin-Bypass:** Rolle `slm-admin` erhält ungefilterten Zugriff (entspricht
  dem heutigen Admin-Consul-Client).

### Datenmodell je Bereich

JPA-Konvention wie `Location`/`Capability` (Kotlin `@Entity`, `@Id
@Column("uuid")`, JSON-Spalten via `@JdbcTypeCode(SqlTypes.JSON)`).

**Resources** — `BasicResource` wird zur JPA-Entität:

- Persistente Spalten: `id, hostname, ip, assetId, firmwareVersion, driverId,
  locationId`
- Abgeleitete Felder (`capabilityServiceIds, remoteAccessIds, clusterMember`)
  → `@Transient`, werden zur Laufzeit befüllt
- `ResourceJpaRepository extends JpaRepository<BasicResource, UUID>`
- `ResourcesConsulClient`, `ResourcesConsulClientFactory` und
  `ResourceConsulNode` entfallen.

**CapabilityServices** — Entkopplung von Consul `NodeService`:

- `CapabilityServiceEntity`: `id (serviceId), resourceId, capabilityId
  (FK→Capability), status, managed, customMeta (JSON), port, serviceClass`
- MultiHost-Spezifika (`memberMapping: Map<UUID,String>`) als JSON-Spalte
- `CapabilityServiceJpaRepository`
- Die Modellklassen `CapabilityService`, `SingleHostCapabilityService`,
  `MultiHostCapabilityService` erben **nicht länger** von
  `org.eclipse.slm.common.consul.model.catalog.NodeService`.
- `CapabilitiesConsulClient`, `SingleHostCapabilitiesConsulClient`,
  `MultiHostCapabilitiesConsulClient` entfallen.

**RemoteAccess** — `RemoteAccessConsulService` → `RemoteAccessEntity` +
`RemoteAccessJpaRepository`. `RemoteAccessConsulClient`,
`RemoteAccessConsulClientFactory`, `RemoteAccessConsulMapper` entfallen.

Alle neuen Entitäten/Repositories liegen in bereits gescannten Paketen
(`resource_management.common`, `…features.capabilities`); keine Änderung an
`@EntityScan`/`@EnableJpaRepositories` nötig (RemoteAccess unter
`…common.remote_access` ist abgedeckt).

## Berechtigungs-Enforcement (Request-Flow)

Heute filtert Consul transparent über das User-JWT. Künftig übernimmt das die
Applikation:

- **Neuer `KeycloakTokenUtil.getGroups(jwtAuthenticationToken)`** liest den
  `groups`-Claim (im JWT bereits vorhanden, vgl. `groups_claim: "groups"` in der
  Keycloak-/Consul-Konfiguration).
- Controller bauen einen schlanken **`UserContext`** (`groups: List<String>`,
  `isAdmin: Boolean`) und reichen ihn an die Manager — ersetzt das bisherige
  Durchreichen des rohen JWT-Token-Strings.
- **Lesen (Liste)** (`getResources` / `getCapabilityServices` /
  `getRemoteAccesses`): Manager ermittelt `getAccessibleObjectIds(groups,
  <Typ>)` und filtert die Repository-Abfrage darauf (`findByIdIn(...)`); Admin →
  ungefiltert.
- **Einzelzugriff/Mutation** (`getById`, `update`, `delete`):
  `hasAccess(<Typ>, id, userContext)` als Gate. Kein Zugriff ⇒
  `…NotFoundException` (gleiches Außenverhalten wie heute, wo für den User
  unsichtbare Nodes „nicht existieren").
- **Anlegen:** Es wird automatisch eine **Ein-Objekt-Policy pro Ressource**
  erzeugt — eine Policy, die genau das neue Objekt enthält und der
  `fullPathOwnerGroupId`-Gruppe als Subject zugewiesen ist (Parität zum
  bisherigen `resource_<id>`-Consul-Policy-Verhalten). Der Parameter
  `fullPathOwnerGroupId` bleibt in den Create-Requests unverändert.
- **Löschen:** Das Objekt wird aus allen `policy_objects`-Einträgen entfernt;
  Policies, die dadurch ohne Objekte zurückbleiben, werden aufgeräumt.

## Consul-Entfernung & Auswirkungen

- **Entfällt** in `resource_management`: `ResourcesConsulClient(+Factory)`,
  `ResourceConsulNode`, `CapabilitiesConsulClient`,
  `SingleHost-/MultiHostCapabilitiesConsulClient`,
  `RemoteAccessConsulClient(+Factory/Mapper)` sowie alle ACL-Aufrufe
  (Policy/Role/BindingRule-Erzeugung).
- Manager-Klassen (`ResourcesManagerImpl`, `CapabilitiesManager`,
  `RemoteAccessManagerImpl`, Cluster-Handler) nutzen künftig Repositories +
  `AccessControlService` statt der Consul-Clients.
- **Zu verifizierende Annahme (Risiko):** Andere Module (z.B.
  `service_management`) lesen Resources/CapabilityServices ausschließlich über
  die REST-API von `resource_management`, **nicht** direkt aus Consul. Falls
  doch direkte Consul-Zugriffe existieren, brechen diese. Dies wird **zu Beginn
  der Implementierung verifiziert und gemeldet, bevor Consul-Code entfernt
  wird.**
- Die `common.consul`-Dependency bleibt zunächst im POM (geringes Risiko); ein
  vollständiges Entfernen ist optionaler Cleanup am Ende, sofern kein
  Consul-Code mehr referenziert wird.

## Teststrategie

- Consul-basierte Tests (`*ConsulClientTest`, `*ConsulMapperTest`, betroffene
  Integrationstests) werden entfernt bzw. ersetzt.
- Neue JPA-Tests im Stil von `LocationJpaTest` / `ProfilerJpaRepositoryTest` für
  die neuen Repositories.
- `AccessControlService`-Unit-Tests: Subject-/Objekt-Zuordnung, Admin-Bypass,
  Filterung über `getAccessibleObjectIds`, Aufräumen leerer Policies.
- Manager-Tests mit gemocktem Repository + `AccessControlService` für
  Lese-Filterung und Mutations-Gating.

## Implementierungsphasen

Die Implementierung erfolgt in Phasen mit dem Berechtigungssystem als Fundament:

1. **Vorab-Verifikation:** Prüfen, ob externe Module Resources/CapabilityServices
   direkt aus Consul lesen (Risiko-Annahme oben).
2. **Access-Control-Komponente:** Entitäten, Repository, `AccessControlService`,
   `UserContext`, `KeycloakTokenUtil.getGroups` inkl. Tests.
3. **Resources → DB:** Entity, Repository, `ResourcesManagerImpl` umstellen,
   Consul-Resource-Code entfernen.
4. **SingleHost CapabilityServices → DB:** Entity/Repository, Modell von
   `NodeService` entkoppeln, Manager umstellen.
5. **MultiHost/Cluster Services → DB.**
6. **RemoteAccess → DB.**
7. **Cleanup:** ungenutzten Consul-Code/Dependencies entfernen, Gesamttests.
