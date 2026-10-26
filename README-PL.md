# VoteReward

[English](README.md)

Plugin nagradzający graczy za głosowanie na serwer na listach serwerów
([lista-minecraft.pl](https://lista-minecraft.pl), [lista-serwerow.emecz.pl](https://lista-serwerow.emecz.pl),
[minecraft-list.info](https://minecraft-list.info)). Co 30 sekund (konfigurowalne) sprawdza nowe głosy przez API
list i automatycznie wykonuje komendy nagród — od razu, albo przy najbliższym wejściu gracza na serwer.

- **Minecraft:** 1.18 – 26.3
- **Silniki:** Spigot, Paper, Purpur, Folia i inne zgodne z Bukkit API (szczegóły w [instalacji](docs/pl/installation.md#obsługiwane-serwery))
- **Java:** 17+ (taka, jakiej wymaga wersja Minecrafta)
- Statystyki: [bStats](https://bstats.org/plugin/bukkit/VoteReward/20120)

## Dokumentacja

| | |
|---|---|
| [Instalacja](docs/pl/installation.md) | wymagania, obsługiwane serwery, aktualizacja z 3.x |
| [Konfiguracja](docs/pl/configuration.md) | `settings.yml`, `messages.yml`, komendy i uprawnienia |
| [Jak to działa](docs/pl/architecture.md) | odpytywanie API, zasady nagród, struktura kodu |
| [Development](docs/pl/development.md) | budowanie, Docker, serwer testowy z atrapą API |
| [Testy](docs/pl/testing.md) | uruchamianie testów, co jest testowane, jak pisać nowe |
| [Release](docs/pl/release.md) | wydawanie nowej wersji |

## Szybki start

1. Pobierz `VoteReward-<wersja>.jar` z [Releases](https://github.com/dudekm/VoteReward/releases/latest) i wrzuć do `plugins/`.
2. Uruchom serwer, a potem w `plugins/VoteReward/settings.yml` wpisz `uuid` swojego serwera z każdej listy i komendy nagród.
3. `/votereward reload`.
