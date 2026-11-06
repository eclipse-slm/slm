# Regenerating backend OpenAPI specifications

Each backend has an opt-in generation test that starts an isolated Spring MVC context with its REST controllers and mocked collaborators. It does not start the backend application or connect to its infrastructure. Java 21, Maven, and the project's Maven dependencies are required; Docker and running SLM services are not.

In VS Code, choose **Tasks: Run Task** and select the `OpenAPI:` task for the desired service. The generated `api-docs-for-ui.yaml` is written to that service's `*.service.client/src/main/resources` directory. Catalog and information service have client resource modules but no generated TypeScript clients. Information service currently has no scanned REST endpoints, so its `paths` is empty.

To run a task from the repository root without VS Code, substitute the application path and test class from the table:

```sh
mvn -pl <application-path> -am -Dtest=<test-class> \
  -Dsurefire.failIfNoSpecifiedTests=false -DgenerateOpenApiSpec=true test
```

| Service | Application path | Test class |
| --- | --- | --- |
| Platform management | `platform_management/platform_management.service/platform_management.service.app` | `org.eclipse.slm.platform_management.service.app.OpenApiSpecGenerationTest` |
| Resource management | `resource_management/resource_management.service/resource_management.service.app` | `org.eclipse.slm.resource_management.service.app.OpenApiSpecGenerationTest` |
| Service management | `service_management/service_management.service/service_management.service.app` | `org.eclipse.slm.service_management.service.app.OpenApiSpecGenerationTest` |
| Notification service | `notification_service/notification_service.service/notification_service.service.app` | `org.eclipse.slm.notification_service.service.app.OpenApiSpecGenerationTest` |
| Catalog | `catalog/catalog.service/catalog.service.app` | `org.eclipse.slm.catalog.service.app.OpenApiSpecGenerationTest` |
| Information service | `information_service/information_service.service/information_service.service.app` | `org.eclipse.slm.information_service.service.app.OpenApiSpecGenerationTest` |

The fully qualified test name ensures that building upstream dependencies does not regenerate their specifications. Normal test runs skip generation unless `-DgenerateOpenApiSpec=true` is supplied. Review the resulting changes before using them to regenerate clients.

OAuth endpoint URLs are normally discovered from the running Keycloak configuration. Since generation mocks that dependency, those URLs are empty in generated specifications; supply the appropriate values for your deployment if you need to use the specifications for Swagger UI authentication.

## Troubleshooting

If the build fails with `cannot access <SomeException>` or `class file for <SomeClass> not found` for a class that clearly exists, and cleaning the module does not help, the VS Code Java extension is writing its own compiler output into the same `target/classes` directories. Those compiler stubs are newer than the sources, so Maven's incremental build accepts them instead of recompiling. This affects any Maven build in the workspace, not just spec generation.

The reliable workaround is to run the command from a checkout outside the VS Code workspace, for example:

```sh
rsync -a --exclude target --exclude .git ./ /tmp/slm-verify/
cd /tmp/slm-verify
mvn -pl <application-path> -am -Dtest=<test-class> \
  -Dsurefire.failIfNoSpecifiedTests=false -DgenerateOpenApiSpec=true test
```

Then copy the regenerated `api-docs-for-ui.yaml` files back. Cleaning the affected module (`mvn -pl <application-path> -am clean`) sometimes helps, but the extension may poison another module before the build finishes.

Generated specifications are written with sorted object keys because OpenAPI objects are unordered. Regenerating an unchanged API therefore produces byte-identical files instead of diff noise.
