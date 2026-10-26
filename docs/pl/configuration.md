# Konfiguracja

[← Spis treści](../../README-PL.md) · [English](../configuration.md)

Pliki są w `plugins/VoteReward/`. Po zmianach: `/votereward reload`.

## settings.yml

```yaml
polling:
  interval: 30s

servers:
  lista-minecraft:
    address: https://lista-minecraft.pl
    uuid: 183413bc-4588-4265-8f06-346963e5d7df
    reward-commands:
      - xp add %player_name% 100
      - give %player_name% diamond
```

### `polling.interval`

Jak często plugin sprawdza nowe głosy na każdej liście. Format: `30s`, `1m`, `2m30s` albo sama liczba sekund.
Domyślnie **30s**, minimum **10s** (niższa wartość zostanie podniesiona, a w konsoli pojawi się ostrzeżenie).

Każde sprawdzenie pobiera **tylko głosy od poprzedniego sprawdzenia** (filtr `createdAt[after]`),
a nie całą historię — patrz [jak to działa](architecture.md#odpytywanie-api).

### `servers`

Listy, na których jest serwer. Nazwy kluczy (`lista-minecraft` itd.) są dowolne, ale muszą być unikalne
— pojawiają się w wiadomościach jako `%server_name%`.

| Klucz | Opis |
|---|---|
| `address` | adres strony listy, np. `https://lista-minecraft.pl` |
| `uuid` | ID serwera na liście — ostatni fragment adresu strony serwera, np. `…/serwery/minecraft/183413bc-4588-4265-8f06-346963e5d7df` |
| `reward-commands` | komendy wykonywane z konsoli za każdy głos (bez `/`) |

Błędny wpis (zły adres, brak `uuid`) jest pomijany z ostrzeżeniem w konsoli — pozostałe listy działają dalej.

Placeholdery w komendach:

| Placeholder | Wartość |
|---|---|
| `%player_name%` | nick gracza |
| `%player_display_name%` | wyświetlana nazwa gracza |
| `%player_uuid%` | UUID gracza |
| `%server_name%` | nazwa listy z `settings.yml` |
| `%server_address%` | adres listy |

### Zasady nagradzania

- Gracz dostaje nagrodę **raz dziennie za każdą listę** (dzień liczony w UTC — tak jak liczą go listy).
- Jeśli gracz jest online, nagroda jest wydawana od razu po wykryciu głosu.
- Jeśli jest offline — dostaje ją przy najbliższym wejściu na serwer.
- Nieodebrana nagroda przepada po 7 dniach.
- Głos oddany bez nicku nie daje nagrody.

## messages.yml

Kolory w formacie `&` (`&a` zielony, `&c` czerwony, `&e` żółty, `&l` pogrubienie…).
Pusta wiadomość (`''`) nie jest wysyłana. Brakujące klucze są brane z domyślnego pliku w `.jar`.

## Komendy i uprawnienia

Komenda: `/votereward` (alias `/vr`).

| Komenda | Uprawnienie | Domyślnie | Opis |
|---|---|---|---|
| `/vr` lub `/vr reward` | `votereward.reward` | wszyscy | odbiera zaległe nagrody |
| `/vr check` | `votereward.check` | wszyscy | dzisiejszy status głosów na każdej liście |
| `/vr check <gracz>` | `votereward.check.others` | OP | status innego gracza |
| `/vr reload` | `votereward.reload` | OP | przeładowuje oba pliki i interwał |
| `/vr poll` | `votereward.poll` | OP | sprawdza listy natychmiast (np. do testów) |
