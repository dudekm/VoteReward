# Release

[← Contents](../README.md) · [Polski](pl/release.md)

Versioning: [SemVer](https://semver.org/) — `MAJOR` for incompatible configuration or behaviour changes,
`MINOR` for new features, `PATCH` for fixes.

## Steps

1. Set the version in `pom.xml` (`<version>`). `plugin.yml` picks it up from Maven automatically.
2. Build and test:
   ```bash
   mvn clean verify          # or: docker compose run --rm build
   ```
3. Run the [manual tests](testing.md#manual-tests-before-a-release).
4. Commit, tag and push:
   ```bash
   git commit -am "Release 4.0.0"
   git tag v4.0.0
   git push origin main --tags
   ```
5. On GitHub: **Releases → Draft a new release**, choose the tag, add release notes
   (for a MAJOR version — an upgrade section, as in [installation](installation.md#upgrading-from-3x))
   and attach `target/VoteReward-<version>.jar` (not `original-…jar`).
6. Optionally: update the plugin page on SpigotMC.
