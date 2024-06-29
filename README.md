# MineTrax Java Plugin
This plugin is part of minetrax suite and is required to let the suite function.

Project use Gradle to build. Clone the project and open in IntelliJ IDEA to work on it and build.

# Third Party Plugin supports:
### PlaceholderAPI
Supported placeholder-api supported variables:
```
%minetrax_player_id%
%minetrax_player_session_uuid%
%minetrax_player_is_verified%
%minetrax_player_country_id%
%minetrax_player_country_name%
%minetrax_player_country_iso_code%
%minetrax_player_rank_id%
%minetrax_player_rank_shortname%
%minetrax_player_rank_name%
%minetrax_player_rating%
%minetrax_player_total_score%
%minetrax_player_total_mob_kills%
%minetrax_player_total_player_kills%
%minetrax_player_total_deaths%
%minetrax_player_play_time%
%minetrax_player_afk_time%
%minetrax_player_position%
%minetrax_player_first_seen_at%
%minetrax_player_last_seen_at%
%minetrax_player_profile_link%
```

# Links
https://www.spigotmc.org/resources/minetrax-suite.102378/

https://www.spigotmc.org/resources/minetrax-suite-plugin.102635/

# Tested with
- Java 17
- Minecraft 1.18

# Build
```
./gradlew shadowJar
```
The jar is created in `assembly/build/libs`.
