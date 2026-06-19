# Resource Management Part 2: CapabilityServices Consul → Datenbank

**Datum:** 2026-06-19
**Branch:** `feature/refactor-resource-mgmt-consul`
**Status:** Design abgestimmt
**Vorgänger:** Part 1 (`2026-06-18-resource-mgmt-db-persistence-design.md`) — Resources + Access-Control-Komponente bereits migriert.

## Problemstellung

CapabilityServices (Instanzen einer Capability auf einer Ressource) werden derzeit als Consul-**Services** gespeichert — SingleHost als Service am Consul-Node der Ressource, MultiHost/Cluster als Services über mehrere Member-Nodes. Nachdem Part 1 die Ressourcen aus Consul entfernt hat, sind die SingleHost-Lesepfade bereits fragil (sie rufen `getNodeByIdOrThrow` auf nicht mehr existierende Nodes). Der MultiHost/Cluster-Code ist zudem weitgehend funktionsunfähig (auskommentierte Kernpfade, hartcodiert leere Service-Map, garantierte NPE). CapabilityServices sollen wie Ressourcen in die bestehende Datenbank wandern und die in Part 1 eingeführte DB-Berechtigung (`AccessControlService`) nutzen.

## Befunde aus der Analyse

- **Reine Metadaten-Speicherung, keine Laufzeit-Service-Discovery:** `service_management` bezieht CapabilityServices ausschließlich über die REST-API von `resource_management` (`providers().getServiceHosters(...)`), nicht direkt aus Consul. Es gibt keine Health-Checks an der Service-Registrierung. Die Migration in die DB ist daher sicher — gleiches Muster wie bei Ressourcen.
- Der MultiHost/Cluster-Code ist faktisch tot: `getClusters()` hat die `Cluster`-Konstruktion auskommentiert (liefert immer leer), `getMultiHostCapabilitiesServicesOfUser()` nutzt eine hartcodiert leere Map, `createMultiHostCapabilityServiceFromConsulService()` läuft in eine NPE.

## Ziele

- SingleHost-CapabilityServices vollständig auf DB + `AccessControlService` umstellen.
- MultiHost/Cluster-**Storage-Modell** (Entität, memberMapping, CRUD/Lese-Pfade) auf DB umstellen.
- Die toten Lese-Pfade (`getClusters`, `getClusterMembers`) als Nebeneffekt der DB-Migration wieder funktionsfähig machen.
- Modellklassen von Consul `NodeService` entkoppeln.

## Nicht-Ziele (separat, spätere Teile)

- Die AWX/Ansible-**Orchestrierung** der Cluster-Erstellung/-Skalierung (eigentliche Provisionierung) wird **nicht** repariert. Nur ihre **Storage-Seiteneffekte** (Persistieren von MultiHostCapabilityService + memberMapping) werden auf DB umgestellt; die Job-Ausführungslogik bleibt unverändert.
- RemoteAccess (eigener späterer Part).
- Entfernen der `common.consul`-Dependency.

## Scope-Grenze (Storage/Read vs. Orchestrierung)

**IN Part 2:**
- Entität + Repository für CapabilityServices.
- Persistenz von SingleHost und MultiHost.
- Alle Lese-Pfade: `getCapabilityServices`, `getCapabilityServicesOfResource`, `getCapabilityServicesByCapabilityClass`, `getClusters`, `getClusterMembers`.
- `add`/`remove` SingleHost; Cluster-Storage-Cleanup bei `delete`.
- DB-Berechtigung (Objekttyp `CAPABILITY_SERVICE`).
- Entkopplung der Modellklassen von `NodeService`.

**NICHT in Part 2:** die AWX-Job-Ausführungslogik für Cluster create/scale. Wo diese Operationen heute `registerService(...)` aufrufen, wird der Aufruf auf `repository.save(...)` umgestellt (Storage-Seiteneffekt), aber das Triggern/Beobachten der AWX-Jobs bleibt unangetastet.

## Datenmodell

Eine **flache** JPA-Entität mit Diskriminator (keine JPA-Vererbung), Konvention wie `Capability`/`Location` (Kotlin `@Entity`, `@Id @Column("uuid")`, JSON via `@JdbcTypeCode(SqlTypes.JSON)`):

`CapabilityServiceEntity`:
- `id: UUID` (= serviceId, `@Id`)
- `resourceId: UUID`
- `capabilityId: UUID` (Verweis auf die bestehende `Capability`-Entität)
- `status: CapabilityServiceStatus` (Enum, `@Enumerated(STRING)`)
- `managed: Boolean`
- `port: Int?`
- `customMeta: Map<String,String>` (JSON-Spalte)
- `serviceClass: CapabilityServiceClass` (Enum `SINGLE_HOST | MULTI_HOST`, Diskriminator)
- `memberMapping: Map<UUID,String>?` (JSON-Spalte; nur für `MULTI_HOST` befüllt — `<resourceId, clusterMemberTypeName>`)

`CapabilityServiceJpaRepository extends JpaRepository<CapabilityServiceEntity, UUID>`:
- `List<CapabilityServiceEntity> findByResourceId(UUID resourceId)`
- `List<CapabilityServiceEntity> findByServiceClass(CapabilityServiceClass serviceClass)`
- `List<CapabilityServiceEntity> findByIdIn(Set<UUID> ids)` (für Access-Filterung)
- Join-Query `findByCapabilityClass(String capabilityClass)` — verbindet `CapabilityServiceEntity.capabilityId` mit `Capability.capabilityClass` (für den DeploymentCapability-Filter der ServiceHoster).

### Entkopplung von `NodeService`

Die Domänen-/DTO-Klassen `CapabilityService`, `SingleHostCapabilityService`, `MultiHostCapabilityService` erben nicht länger von `org.eclipse.slm.common.consul.model.catalog.NodeService`. Die von Konsumenten genutzten Eigenschaften (`serviceId`, `resourceId`, `capability`, `status`, `managed`, `customMeta`, `port`, `serviceName`, `tags`, und für MultiHost `memberMapping`) bleiben als reine Felder bzw. berechnete Getter erhalten. `serviceName`/`tags` wurden bisher zur Consul-Registrierung abgeleitet und bleiben als berechnete Werte verfügbar, soweit Konsumenten sie nutzen.

Eine Mapper-Schicht (MapStruct, analog `CapabilityServiceMapper`) übersetzt zwischen `CapabilityServiceEntity` und den Domänenobjekten (inkl. Nachladen der `Capability` über `CapabilityJpaRepository`).

## Persistenz & Berechtigung

Die drei Consul-Clients entfallen: `CapabilitiesConsulClient`, `SingleHostCapabilitiesConsulClient`, `MultiHostCapabilitiesConsulClient`. Ihre Aufrufer (`CapabilitiesManager`, `ServiceHosterHandler`/`ProviderHandler`, Cluster-Handler) nutzen künftig `CapabilityServiceJpaRepository` + `AccessControlService`.

- **add** (SingleHost; MultiHost-Storage): `repository.save(entity)` + `accessControlService.createSingleObjectPolicy("capability-service_" + serviceId, fullPathOwnerGroupId, AccessControlObjectType.CAPABILITY_SERVICE, serviceId)`.
- **remove/delete:** `repository.deleteById(serviceId)` + `accessControlService.removeObjectFromAllPolicies(CAPABILITY_SERVICE, serviceId)`.
- **Sichtbarkeit (mit UserContext):** `getAccessibleObjectIds(userContext, CAPABILITY_SERVICE)` → `findByIdIn(...)`; Admin → ungefiltert.
- **Interner Enrichment-Pfad ohne Token:** `ICapabilitiesManager.getCapabilityServiceIdsOfResource(UUID resourceId)` und `isResourceClusterMember(UUID)` (von `ResourcesManagerImpl` genutzt) lesen ungefiltert (admin), da die Resource-Sichtbarkeit dort bereits durchgesetzt ist — gleiches Muster wie der RemoteAccess-Admin-Read in Part 1.
- Die statische `ResourcesConsulClient.getResourcePolicyName`-Nutzung in `ClusterCreateFunctions` entfällt (durch `AccessControlService` ersetzt). `notification_service` bleibt der verbleibende Nutzer des statischen Helpers.

## Konsumenten

- `service_management` konsumiert CapabilityServices weiterhin über die REST-API (`providers().getServiceHosters(...)`) — **keine Änderung an service_management nötig** (anders als bei Ressourcen in Part 1).
- `ResourcesManagerImpl.addDetailsToResource` nutzt weiterhin `ICapabilitiesManager.getCapabilityServiceIdsOfResource`/`isResourceClusterMember` — Signaturen unverändert, Implementierung DB-basiert.

## Teststrategie

- JPA-Tests für `CapabilityServiceJpaRepository` (Persistenz inkl. `customMeta`- und `memberMapping`-JSON, `findByResourceId`, `findByServiceClass`, `findByCapabilityClass`).
- Service-/Manager-Tests: add/remove SingleHost, MultiHost-Storage, Sichtbarkeitsfilterung über `AccessControlService`, Policy-Cleanup; Cluster-Lese-Pfade (`getClusters`/`getClusterMembers`) liefern jetzt echte DB-Daten.
- Bestehende Consul-basierte Tests der drei Clients werden entfernt bzw. durch JPA-/Service-Tests ersetzt.
- Vorab-Verifikation analog Part 1: prüfen, dass kein externer Konsument CapabilityServices direkt aus Consul liest (Befund: `service_management` nutzt REST) — vor dem Entfernen der Consul-Clients bestätigen.

## Implementierungsphasen

1. **Vorab-Verifikation:** Bestätigen, dass keine externen Module CapabilityServices direkt aus Consul lesen.
2. **Entität + Repository** (`CapabilityServiceEntity`, `CapabilityServiceClass`, `CapabilityServiceJpaRepository`) inkl. JPA-Tests.
3. **Modell-Entkopplung von `NodeService`** + Mapper (Entität ↔ Domänenobjekt).
4. **SingleHost-Persistenz** auf DB + `AccessControlService`; `CapabilitiesManager`/`ServiceHosterHandler` umstellen; `SingleHostCapabilitiesConsulClient`/`CapabilitiesConsulClient` entfernen.
5. **MultiHost-Storage** auf DB (Entität mit memberMapping, Lese-/CRUD-Pfade, `getClusters`/`getClusterMembers` reaktiviert); Cluster-Handler-Storage-Aufrufe (`registerService` → `save`) umstellen, AWX-Orchestrierung unangetastet; `MultiHostCapabilitiesConsulClient` entfernen.
6. **Cleanup & Gesamttests:** Cluster-ACL-Nutzung des statischen Consul-Helpers durch `AccessControlService` ersetzen; modulweite Tests grün.
