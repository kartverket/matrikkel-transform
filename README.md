# Publisering
Pakken ble tidligere publisert til Nexus. Gamle versjoner av pakken er migrert til GitHub Packages.

Nye pakker publiseres til [GitHub Packages](https://github.com/orgs/kartverket/packages?repo_name=matrikkel-transform) via [build-push.yml](.github/workflows/build-publish.yml) workflowen.
Ved hver push til `develop` så vil det bygges og publiseres en ny versjon av pakkene.


## Versjonering
Pakken har versjonsnummer som er av formatet `[Major version].[Date].[SHA]`
Versjonsnummeret oppdateres ved hver publisering.

`[Major version]` oppdateres ved breaking changes og kan endres i [build-push.yml](.github/workflows/build-publish.yml) workflowen.
