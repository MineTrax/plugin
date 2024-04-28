package com.xinecraft.minetrax.common.utils;

import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.actions.WhoisQuery;
import com.xinecraft.minetrax.common.responses.PlayerWhoisApiResponse;

import java.util.ArrayList;
import java.util.List;

public class WhoisUtil {
    private static final MinetraxCommon common = MinetraxCommon.getInstance();

    public static List<String> forPlayer(
            String username,
            String uuid,
            String ipAddress,
            Boolean shouldBroadcast,
            Boolean isFromJoinEvent,
            Boolean isRanByAdminPlayer,
            String noMatchFoundMessage,
            List<String> onFirstJoinMessage,
            List<String> onJoinMessage,
            List<String> onCommandMessage,
            List<String> onAdminCommandMessage,
            String multiplePlayersTitleMessage,
            String multiplePlayersListMessage
            ) {
        List<String> returnList = new ArrayList<>();
        try {
            PlayerWhoisApiResponse whoisResponseData = WhoisQuery.player(uuid, username, ipAddress, isFromJoinEvent);

            if (whoisResponseData.getCode() != 200) {
                returnList.add("&c Error making whois request: " + whoisResponseData.getMessage());
                return returnList;
            }

            if (isFromJoinEvent && !shouldBroadcast) {
                return null;
            }

            // Get country, state and city from geodata
            String playerCountry = whoisResponseData.getData().geo != null && whoisResponseData.getData().geo.country != null ? whoisResponseData.getData().geo.country : "&r&oTerra Incognita&r";
            String playerStateName = whoisResponseData.getData().geo != null && whoisResponseData.getData().geo.state_name != null ? whoisResponseData.getData().geo.state_name : "&r&oUnknown State&r";
            String playerCity = whoisResponseData.getData().geo != null && whoisResponseData.getData().geo.city != null ? whoisResponseData.getData().geo.city : "&r&oUnknown City&r";

            // If count is 0 and is not a join event then no player found message.
            if (whoisResponseData.getData().count <= 0 && !isFromJoinEvent) {
                returnList.add(noMatchFoundMessage);
            }

            // If count is 0 but its a join event then tell geo without player info
            else if (whoisResponseData.getData().count <= 0 && isFromJoinEvent) {
                String geoString;
                assert whoisResponseData.getData().geo != null;
                geoString = whoisResponseData.getData().geo.city != null ? whoisResponseData.getData().geo.city + ", " : "";
                geoString += whoisResponseData.getData().geo.state_name != null ? whoisResponseData.getData().geo.state_name + ", " : "";
                geoString += whoisResponseData.getData().geo.country != null ? whoisResponseData.getData().geo.country : "Terra Incognita";

                for (String line : onFirstJoinMessage) {
                    line = line.replace("{USERNAME}", username);
                    line = line.replace("{GEO}", geoString);
                    line = line.replace("{COUNTRY}", playerCountry);
                    line = line.replace("{STATE}", playerStateName);
                    line = line.replace("{CITY}", playerCity);
                    returnList.add(line);
                }
            }

            // If count is 1 then show details
            else if (whoisResponseData.getData().count == 1) {
                PlayerWhoisApiResponse.Player player = whoisResponseData.getData().players.get(0);

                // Make {GEO} Data
                String geoString;
                if (whoisResponseData.getData().geo != null) {
                    geoString = whoisResponseData.getData().geo.city != null ? whoisResponseData.getData().geo.city + ", " : "";
                    geoString += whoisResponseData.getData().geo.state_name != null ? whoisResponseData.getData().geo.state_name + ", " : "";
                    geoString += whoisResponseData.getData().geo.country != null ? whoisResponseData.getData().geo.country : "Terra Incognita";
                } else {
                    geoString = player.country;
                }

                List<String> whoisMessageStringList = isFromJoinEvent ? onJoinMessage : onCommandMessage;
                if (!isFromJoinEvent && isRanByAdminPlayer) {
                    whoisMessageStringList = onAdminCommandMessage;
                }

                for (String line : whoisMessageStringList) {
                    line = line.replace("{COUNTRY}", playerCountry);
                    line = line.replace("{STATE}", playerStateName);
                    line = line.replace("{CITY}", playerCity);
                    line = line.replace("{USERNAME}", player.username != null ? player.username : "&r&oUnknown&r");
                    line = line.replace("{GEO}", geoString != null ? geoString : "&r&oUnknown&r");
                    line = line.replace("{POSITION}", player.position != null ? player.position : "&r&oNone&r");
                    line = line.replace("{RANK}", player.rank != null ? player.rank : "&r&oNone&r");
                    line = line.replace("{RATING}", player.rating != null ? player.rating : "&r&oNone&r");
                    line = line.replace("{SCORE}", player.total_score != null ? player.total_score : "0");
                    line = line.replace("{USER}", player.user != null ? player.user : "&r&oNone&r");
                    line = line.replace("{URL}", player.url != null ? player.url : "&r&oUnknown&r");
                    line = line.replace("{LAST_SEEN}", player.last_seen_at != null ? player.last_seen_at : "&r&oUnknown&r");
                    returnList.add(line);
                }
            }

            // if count is more than 1 then show list
            else if (whoisResponseData.getData().count > 1) {
                String titleMessage = multiplePlayersTitleMessage.replace("{COUNT}", String.valueOf(whoisResponseData.getData().count));
                returnList.add(titleMessage);
                for (PlayerWhoisApiResponse.Player player : whoisResponseData.getData().players) {
                    String line = multiplePlayersListMessage;
                    line = line.replace("{POSITION}", player.position);
                    line = line.replace("{USERNAME}", player.username);
                    returnList.add(line);
                }
            } else {
                returnList.add("&cOsho: WTF is going on here?");
            }
        } catch (Exception e) {
            common.getLogger().warning(e.getMessage());
            returnList.add("&cError making whois request: " + e.getMessage());
        }
        return returnList;
    }
}
