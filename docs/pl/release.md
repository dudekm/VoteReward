# Release

[← Spis treści](../../README-PL.md) · [English](../release.md)

Wersjonowanie: [SemVer](https://semver.org/lang/pl/) — `MAJOR` przy niekompatybilnych zmianach konfiguracji
lub zachowania, `MINOR` przy nowych funkcjach, `PATCH` przy poprawkach.

## Kroki

1. Ustaw wersję w `pom.xml` (`<version>`). `plugin.yml` bierze ją automatycznie z Mavena.
2. Zbuduj i przetestuj:
   ```bash
   mvn clean verify          # albo: docker compose run --rm build
   ```
3. Wykonaj [testy ręczne](testing.md#testy-ręczne-przed-wydaniem).
4. Commit, tag i push:
   ```bash
   git commit -am "Release 4.0.0"
   git tag v4.0.0
   git push origin main --tags
   ```
5. Na GitHubie: **Releases → Draft a new release**, wybierz tag, dodaj opis zmian
   (przy wersji MAJOR — sekcję o aktualizacji, jak w [instalacji](installation.md#aktualizacja-z-wersji-3x))
   i załącz `target/VoteReward-<wersja>.jar` (nie `original-…jar`).
6. Opcjonalnie: zaktualizuj stronę pluginu na SpigotMC.
