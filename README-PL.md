Angielska wersja znajduje się [tutaj](README.md).

# VoteReward

VoteReward to plugin pod Spigot pozwalający na przyznawanie nagród graczom za głosowanie na serwer. Został stworzony pod wersje powyżej 1.13. Statystyki projektu dostępne na stronie [bStats](https://bstats.org/plugin/bukkit/VoteReward/20120).

# Instalacja

Pobierz najnowszą wersję pluginu [tutaj](https://github.com/dudekm/VoteReward/releases/latest).

Po pobraniu pliku `.jar`, należy umieścić go w folderze "plugins" w głównym katalogu serwera, a następnie zrestartować serwer. Po ponownym uruchomieniu powinien zostać utworzony folder "VoteReward" zawierający plik konfiguracyjny.

Teraz w `settings.yml` należy skonfigurować serwery, na które mogą głosować gracze. W tym celu przechodzimy do sekcji `Servers`. Jej domyślna forma wygląda następująco:

```yaml
Servers:
  Server1:
    Address: https://lista-serwerow.emecz.pl
    Uuid: b23d452d-fbf1-47cf-a1d9-801a3e0bb516
    RewardCommands:
      - xp add %player_name% 100
      - give %player_name% diamond
  Server2:
    Address: https://lista-minecraft.pl
    Uuid: a5a29c24-6514-472d-add0-0b2be060e277
    RewardCommands:
      - xp add %player_name% 100
      - give %player_name% emerald
```

Wartości "Server1" czy "Server2" mogą być czymkolwiek - są to tylko nazwy.

`Address` odnosi się do adresu strony, na której dodano serwer. Wspierane adresy to:
- https://lista-serwerow.emecz.pl,
- https://lista-minecraft.pl,
- https://minecraft-list.info.

`Uuid` oznacza unikatowe ID serwera przypisane wyłącznie do niego. Aby sprawdzić ID, należy wejść na stronę serwera i skopiować ostatni element adresu strony. Przykładowo, jeżeli wygląda on następująco: https://lista-minecraft.pl/serwery/minecraft/b23d452d-fbf1-47cf-a1d9-801a3e0bb516 - ID będzie równe `b23d452d-fbf1-47cf-a1d9-801a3e0bb516`.

`RewardCommands` to lista komend, które zostaną wysłane do konsoli serwera po pomyślnym zweryfikowaniu głosu gracza. Dostępne są następujące symbole zastępcze (placeholdery):
- `%player_name%` - nazwa gracza,
- `%player_display_name%` - wyświetlana nazwa gracza,
- `%player_uuid%` - unikatowe ID gracza,
- `%server_name%` - nazwa serwera,
- `%server_address%` - adres strony serwera,
- `%server_uuid%` - unikatowe ID serwera.

Jeżeli plugin został poprawnie skonfigurowany, gracz po oddaniu głosu może użyć komendy `/votereward`, aby otrzymać nagrodę za głosowanie.