package com.xinecraft.utils;

import java.util.HashMap;
import java.util.Map;

public class VersionUtil {
    private static final Map<Integer, String> protocolToVersion = new HashMap<>();
    static {
        protocolToVersion.put(765, "1.20");
        protocolToVersion.put(764, "1.20");
        protocolToVersion.put(763, "1.20");

        protocolToVersion.put(762, "1.19");
        protocolToVersion.put(761, "1.19");
        protocolToVersion.put(760, "1.19");
        protocolToVersion.put(759, "1.19");

        protocolToVersion.put(758, "1.18");
        protocolToVersion.put(757, "1.18");

        protocolToVersion.put(756, "1.17");
        protocolToVersion.put(755, "1.17");

        protocolToVersion.put(754, "1.16");
        protocolToVersion.put(753, "1.16");
        protocolToVersion.put(752, "1.16");
        protocolToVersion.put(751, "1.16");
        protocolToVersion.put(750, "1.16");
        protocolToVersion.put(749, "1.16");
        protocolToVersion.put(748, "1.16");
        protocolToVersion.put(746, "1.16");
        protocolToVersion.put(744, "1.16");
        protocolToVersion.put(736, "1.16");
        protocolToVersion.put(735, "1.16");
        protocolToVersion.put(734, "1.16");
        protocolToVersion.put(733, "1.16");
        protocolToVersion.put(732, "1.16");
        protocolToVersion.put(730, "1.16");
        protocolToVersion.put(729, "1.16");
        protocolToVersion.put(727, "1.16");
        protocolToVersion.put(725, "1.16");
        protocolToVersion.put(722, "1.16");
        protocolToVersion.put(721, "1.16");

        protocolToVersion.put(578, "1.15");
        protocolToVersion.put(577, "1.15");
        protocolToVersion.put(576, "1.15");
        protocolToVersion.put(575, "1.15");
        protocolToVersion.put(574, "1.15");
        protocolToVersion.put(573, "1.15");
        protocolToVersion.put(572, "1.15");
        protocolToVersion.put(571, "1.15");
        protocolToVersion.put(570, "1.15");
        protocolToVersion.put(569, "1.15");
        protocolToVersion.put(567, "1.15");
        protocolToVersion.put(566, "1.15");
        protocolToVersion.put(565, "1.15");

        protocolToVersion.put(500, "1.14");
        protocolToVersion.put(498, "1.14");
        protocolToVersion.put(497, "1.14");
        protocolToVersion.put(496, "1.14");
        protocolToVersion.put(495, "1.14");
        protocolToVersion.put(494, "1.14");
        protocolToVersion.put(493, "1.14");
        protocolToVersion.put(492, "1.14");
        protocolToVersion.put(491, "1.14");
        protocolToVersion.put(490, "1.14");
        protocolToVersion.put(489, "1.14");
        protocolToVersion.put(488, "1.14");
        protocolToVersion.put(487, "1.14");
        protocolToVersion.put(486, "1.14");
        protocolToVersion.put(485, "1.14");
        protocolToVersion.put(484, "1.14");
        protocolToVersion.put(483, "1.14");
        protocolToVersion.put(482, "1.14");
        protocolToVersion.put(481, "1.14");
        protocolToVersion.put(480, "1.14");
        protocolToVersion.put(479, "1.14");
        protocolToVersion.put(478, "1.14");
        protocolToVersion.put(477, "1.14");
        protocolToVersion.put(476, "1.14");
        protocolToVersion.put(475, "1.14");
        protocolToVersion.put(474, "1.14");
        protocolToVersion.put(473, "1.14");
        protocolToVersion.put(472, "1.14");

        protocolToVersion.put(404, "1.13");
        protocolToVersion.put(403, "1.13");
        protocolToVersion.put(402, "1.13");
        protocolToVersion.put(401, "1.13");
        protocolToVersion.put(400, "1.13");
        protocolToVersion.put(399, "1.13");
        protocolToVersion.put(393, "1.13");
        protocolToVersion.put(392, "1.13");
        protocolToVersion.put(391, "1.13");
        protocolToVersion.put(390, "1.13");
        protocolToVersion.put(389, "1.13");
        protocolToVersion.put(388, "1.13");
        protocolToVersion.put(387, "1.13");
        protocolToVersion.put(386, "1.13");
        protocolToVersion.put(385, "1.13");
        protocolToVersion.put(384, "1.13");
        protocolToVersion.put(383, "1.13");

        protocolToVersion.put(340, "1.12");
        protocolToVersion.put(339, "1.12");
        protocolToVersion.put(338, "1.12");
        protocolToVersion.put(337, "1.12");
        protocolToVersion.put(336, "1.12");
        protocolToVersion.put(335, "1.12");
        protocolToVersion.put(334, "1.12");
        protocolToVersion.put(333, "1.12");
        protocolToVersion.put(332, "1.12");
        protocolToVersion.put(331, "1.12");
        protocolToVersion.put(330, "1.12");
        protocolToVersion.put(329, "1.12");
        protocolToVersion.put(328, "1.12");

        protocolToVersion.put(316, "1.11");
        protocolToVersion.put(315, "1.11");
        protocolToVersion.put(210, "1.10");
        protocolToVersion.put(110, "1.9");
        protocolToVersion.put(109, "1.9");
        protocolToVersion.put(108, "1.9");
        protocolToVersion.put(107, "1.9");
        protocolToVersion.put(47, "1.8");
    }

    public static String getMinecraftVersionFromProtoId(int protocolVersion) {
        return protocolToVersion.get(protocolVersion);
    }
}
