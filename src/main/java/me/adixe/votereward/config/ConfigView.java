package me.adixe.votereward.config;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Read-only view of a configuration file, so settings can be parsed and tested
 * without the server's YAML classes. Paths use dots: {@code polling.interval}.
 */
public interface ConfigView {
    Optional<String> string(String path);

    List<String> stringList(String path);

    /** Direct child keys of the section at {@code path}, in file order; empty when there is no such section. */
    Set<String> childKeys(String path);
}
