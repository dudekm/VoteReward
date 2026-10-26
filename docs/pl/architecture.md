# Jak to działa

[← Spis treści](../../README-PL.md) · [English](../architecture.md)

## Przepływ

```
co polling.interval (wątek asynchroniczny)
  └─ VotePoller: dla każdej listy z settings.yml
       ├─ VoteLedger.checkFrom()       → od kiedy pytać
       ├─ ListApiVoteSource            → GET /api/v1/servers/{uuid}/votes?createdAt[after]=…
       ├─ VoteLedger.record()          → nowe głosy → nagrody oczekujące
       └─ RewardService.rewardsEarned()→ gracze online dostają nagrody od razu
wejście gracza      → RewardService.deliver()   → zaległe nagrody
/votereward         → RewardService.deliver()
```

## Odpytywanie API

Endpoint: `GET {address}/api/v1/servers/{uuid}/votes` (API Platform, format JSON-LD).

Parametry wysyłane przez plugin:

- `createdAt[after]=<moment>` — tylko głosy od podanego momentu,
- `order[createdAt]=asc` — od najstarszego, żeby strony dało się czytać po kolei.

Kolejne strony są pobierane z `view.next` (maks. 20 stron na sprawdzenie; resztę pobierze następne
sprawdzenie, bo zacznie od ostatniego przeczytanego głosu). Linki do innej domeny są ignorowane.

Od kiedy pytać:

- pierwsze sprawdzenie listy: ostatnia **1 godzina**,
- kolejne: od poprzedniego sprawdzenia **minus 5 minut zakładki** — głos zapisany z lekkim opóźnieniem
  lub przy różnicy zegarów nie przepadnie; głosy przeczytane drugi raz są rozpoznawane po `uuid`,
- po dłuższej przerwie (np. serwer był wyłączony): maks. **24 godziny** wstecz.

W praktyce każde zapytanie obejmuje kilka minut, więc obciążenie listy jest minimalne.
Lista, która nie odpowiada, jest logowana raz (i raz po powrocie), a nie co 30 sekund.

## Stan (`data/ledger.yml`)

`VoteLedger` pamięta: pozycję odczytu każdej listy, kto dostał nagrodę danego dnia oraz nagrody
czekające na graczy. Zapisywany po każdej zmianie (zapis do pliku tymczasowego i podmiana —
awaria w trakcie zapisu nie psuje pliku).

## Struktura kodu

Pakiety bez zależności od Bukkita (testowane jednostkowo):

| Pakiet | Zawartość |
|---|---|
| `vote` | Value Objecty: `ServerId`, `Nickname`, `Vote`, `VoteDay` |
| `site` | konfiguracja listy: `VoteSite`, `ListAddress`, `RewardCommand` |
| `config` | `Settings`, `SettingsParser`, `Durations` |
| `api` | klient API list: `ListApiVoteSource`, `VotePageParser`, `HttpTransport` |
| `ledger` | `VoteLedger` (zasady nagród), `YamlLedgerRepository` |
| `reward` | `VotePoller`, `RewardService` i porty `RewardDispatcher`, `PlayerDirectory` |

Warstwa serwera — pakiet `bukkit` i klasa `VoteReward` (punkt wejścia, składa wszystko w całość):
komenda, listener wejścia, wydawanie nagród, wiadomości oraz `TaskScheduler` z dwiema implementacjami
(Bukkit oraz Folia przez refleksję — API Folii nie jest częścią Spigot API).

Zależności idą w jedną stronę: `bukkit` → logika, nigdy odwrotnie. Logika rozmawia ze światem przez
interfejsy (`VoteSource`, `HttpTransport`, `LedgerRepository`, `RewardDispatcher`, `PlayerDirectory`,
`ConfigView`), więc w testach podstawia się proste atrapy.
