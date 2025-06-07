Angielska wersja znajduje się [tutaj](README.md).

# VoteReward

VoteReward to plugin pod Spigot pozwalający na przyznawanie nagród graczom za głosowanie na serwer. Został stworzony pod
wersje powyżej 1.18. Statystyki projektu dostępne na
stronie [bStats](https://bstats.org/plugin/bukkit/VoteReward/20120).

# Instalacja

Pobierz najnowszą wersję pluginu [tutaj](https://github.com/dudekm/VoteReward/releases/latest).

Po pobraniu pliku `.jar`, należy umieścić go w folderze "plugins" w głównym katalogu serwera, a następnie zrestartować
serwer. Po ponownym uruchomieniu powinien zostać utworzony folder "VoteReward" zawierający plik konfiguracyjny.

Teraz w `settings.yml` należy skonfigurować serwery, na które mogą głosować gracze. W tym celu przechodzimy do
sekcji `servers`. Jej domyślna forma wygląda następująco:

```yaml
servers:
  lista-minecraft:
    address: https://lista-minecraft.pl
    uuid: 183413bc-4588-4265-8f06-346963e5d7df
    reward-commands:
      - xp add %player_name% 100
      - give %player_name% diamond
  lista-serwerow-emecz:
    address: https://lista-serwerow.emecz.pl
    uuid: 264acafc-136c-4def-a363-088587c7a405
    reward-commands: []
  minecraft-list:
    address: https://minecraft-list.info
    uuid: d6dfe480-da3a-495a-a92d-5a198a7f2f83
    reward-commands: []
```

Wartości "lista-minecraft" czy "lista-serwerow-emecz" mogą być czymkolwiek - są to tylko nazwy. Nazwy powinny być
unikatowe dla każdego serwera. Można dodać tyle serwerów, ile jest potrzebne.

`address` odnosi się do adresu strony, na której dodano serwer. Wspierane adresy to:

- https://lista-minecraft.pl,
- https://lista-serwerow.emecz.pl,
- https://minecraft-list.info.

`uuid` oznacza unikatowe ID serwera przypisane wyłącznie do niego. Aby sprawdzić ID, należy wejść na stronę serwera i
skopiować ostatni element adresu strony. Przykładowo, jeżeli wygląda on
następująco: https://lista-minecraft.pl/serwery/minecraft/183413bc-4588-4265-8f06-346963e5d7df - ID będzie
równe `183413bc-4588-4265-8f06-346963e5d7df`.

`reward-commands` to lista komend, które zostaną wysłane do konsoli serwera po pomyślnym zweryfikowaniu głosu gracza.
Dostępne są następujące symbole zastępcze (placeholdery):

- `%player_name%` - nazwa gracza,
- `%player_display_name%` - wyświetlana nazwa gracza,
- `%player_uuid%` - unikatowe ID gracza,
- `%server_name%` - nazwa serwera,
- `%server_address%` - adres strony serwera.

Jeżeli plugin został poprawnie skonfigurowany, gracz po oddaniu głosu może użyć komendy `/votereward`, aby otrzymać
nagrodę za głosowanie.