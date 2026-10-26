# Instalacja

[← Spis treści](../../README-PL.md) · [English](../installation.md)

## Wymagania

- Serwer Minecraft **1.18 – 26.3** oparty na Bukkit API (zob. niżej).
- **Java 17 lub nowsza** — w praktyce taka, jakiej i tak wymaga wersja Minecrafta
  (1.18–1.20.4: Java 17, 1.20.5–1.21.x: Java 21, 26.x: Java 25).
- Wyjście do internetu (HTTPS) do adresów list, na których jest serwer.

Plugin nie potrzebuje żadnych dodatkowych bibliotek ani pluginów.

## Obsługiwane serwery

| Silnik | Wsparcie | Uwagi |
|---|---|---|
| Spigot / CraftBukkit | ✅ | |
| Paper i forki (Purpur, Pufferfish…) | ✅ | |
| Folia | ✅ | wykrywana automatycznie, używa schedulerów regionów |
| Hybrydy (Mohist, Arclight…) | ⚠️ | powinny działać, bo ładują pluginy Bukkit — nietestowane |
| Vanilla (serwer od Mojang) | ❌ | vanilla nie ładuje pluginów — trzeba użyć np. Paper (drop-in zamiennik vanilli) |
| Fabric / NeoForge / Sponge | ❌ | inne API modów; poza zakresem tego pluginu |

Plugin jest kompilowany na najstarszym wspieranym API (Spigot 1.18.2) i używa wyłącznie metod
dostępnych we wszystkich wersjach do 26.3, więc **jeden plik `.jar` działa na każdej wersji**.

## Instalacja krok po kroku

1. Pobierz najnowszy `VoteReward-<wersja>.jar` z
   [Releases](https://github.com/dudekm/VoteReward/releases/latest).
2. Wrzuć plik do folderu `plugins/` serwera i uruchom (zrestartuj) serwer.
3. Powstanie folder `plugins/VoteReward/` z plikami `settings.yml` i `messages.yml`.
4. W `settings.yml` w sekcji `servers` ustaw `uuid` swojego serwera na każdej liście i komendy nagród
   — opis w [konfiguracji](configuration.md).
5. Wpisz `/votereward reload` (albo zrestartuj serwer). W konsoli pojawi się
   `Checking N vote list(s) every 30s.`

## Aktualizacja z wersji 3.x

- `settings.yml` z 3.x działa bez zmian (sekcja `servers` ma ten sam format).
  Brakujące `polling.interval` przyjmuje domyślnie `30s`.
- **Nagrody są teraz przyznawane automatycznie** — gracz nie musi wpisywać `/votereward` po głosowaniu.
  Komenda nadal istnieje i odbiera ewentualne zaległe nagrody.
- `messages.yml` ma nowe klucze i kolory w formacie `&` (zamiast `<red>`). Najprościej usunąć stary plik —
  przy starcie powstanie nowy. Brakujące klucze i tak biorą się z domyślnego pliku w `.jar`.
- Plik `data/votes.yml` z 3.x nie jest już używany i można go usunąć; nowe dane są w `data/ledger.yml`.
- `votereward.check` jest teraz domyślnie dostępne dla graczy (podgląd własnych głosów);
  podgląd innych graczy wymaga `votereward.check.others`.
