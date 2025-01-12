package com.xinecraft.minetrax.common.banwarden.hooks;

import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.actions.ReportPlayerPunishment;
import com.xinecraft.minetrax.common.data.PunishmentData;
import com.xinecraft.minetrax.common.enums.BanWardenPluginType;
import com.xinecraft.minetrax.common.enums.BanWardenPunishmentType;
import com.xinecraft.minetrax.common.enums.BanWardenSyncType;
import com.xinecraft.minetrax.common.interfaces.banwarden.BanWardenHook;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import litebans.api.Database;
import litebans.api.Entry;
import litebans.api.Events;
import space.arim.omnibus.util.concurrent.ReactionStage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class LitebansHook implements BanWardenHook {
    public static final MinetraxCommon common = MinetraxCommon.getInstance();
    private static final int RATE_LIMIT_DELAY_SECONDS = 1;
    private static final int CHUNK_SIZE = 50;

    public LitebansHook() {
        registerEventListeners();
    }

    @Override
    public String punish(BanWardenPunishmentType type, String punishmentString) {
        return "";
    }

    @Override
    public boolean pardon(BanWardenPunishmentType type, String victim, String reason, String admin) {
        if (reason == null || reason.isBlank()) {
            reason = "-";
        }
        String parsedAdmin = admin == null || admin.isBlank() ? "--sender=BanWarden" : "--sender=" + admin;
        switch (type) {
            case BAN -> common.getCommander().dispatchCommand("litebans:unban " + victim + " " + reason + " " + parsedAdmin);
            case MUTE -> common.getCommander().dispatchCommand("litebans:unmute " + victim + " " + reason + " " + parsedAdmin);
            case WARN -> common.getCommander().dispatchCommand("litebans:unwarn " + victim + " " + reason + " " + parsedAdmin);
            default -> LoggingUtil.error("[LitebansHook] Pardon -> Unknown punishment type: " + type);
        }
        return true;
    }

    @Override
    public PunishmentData getPunishment(String punishmentId) {
        return null;
    }

    @Override
    public void sync(BanWardenSyncType type) {
        syncAllPunishments().thenAccept(aVoid -> {
            LoggingUtil.debug("[BanWarden] Syncing of punishments from LiteBans completed.");
        });
    }

    private void registerEventListeners() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                try {
                    PunishmentData data = convertEntryToData(entry, false, "punish");
                    ReportPlayerPunishment.reportSync(data);
                } catch (Exception e) {
                    LoggingUtil.error("[BanWarden] EntryAdded -> Error reporting event to Minetrax: " + e.getMessage());
                }
            }

            @Override
            public void entryRemoved(Entry entry) {
                try {
                    PunishmentData data = convertEntryToData(entry, true, "pardon");
                    ReportPlayerPunishment.reportSync(data);
                } catch (Exception e) {
                    LoggingUtil.error("[BanWarden] EntryRemoved -> Error reporting event to Minetrax: " + e.getMessage());
                }
            }
        });
    }

    private CompletableFuture<Void> syncAllPunishments() {
        AtomicInteger totalPunishments = new AtomicInteger(0);
        AtomicInteger offset = new AtomicInteger(0);

        return CompletableFuture.<Void>completedFuture(null).thenCompose(new Function<Void, CompletionStage<Void>>() {
            @Override
            public CompletionStage<Void> apply(Void unused) {
                return fetchPunishmentChunk(CHUNK_SIZE, offset.get())
                        .thenCompose(punishmentList -> {
                            int fetchedCount = punishmentList.size();
                            totalPunishments.addAndGet(fetchedCount);

                            // Process the current chunk
                            List<PunishmentData> punishmentDataList = new ArrayList<>(punishmentList);

                            // Report to Minetrax with API
                            try {
                                if (!punishmentDataList.isEmpty()) {
                                    LoggingUtil.info("Syncing batch of " + punishmentDataList.size() + " punishments to Minetrax...");
                                    ReportPlayerPunishment.syncSync(punishmentDataList);
                                }
                            } catch (Exception e) {
                                LoggingUtil.error("[BanWarden] Error syncing punishments to Minetrax: " + e.getMessage());
                            }

                            if (fetchedCount == CHUNK_SIZE) {
                                // If we fetched a full chunk, there might be more bans
                                offset.addAndGet(CHUNK_SIZE);

                                // some delay to avoid rate limit.
                                try {
                                    Thread.sleep(TimeUnit.SECONDS.toMillis(RATE_LIMIT_DELAY_SECONDS));
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }

                                return apply(null); // Recurse to fetch the next chunk
                            } else {
                                // We've fetched all bans
                                LoggingUtil.info("[BanWarden] Finished syncing total of " + totalPunishments.get() + " punishments from LiteBans.");
                                return CompletableFuture.completedFuture(null);
                            }
                        });
            }
        });
    }

    private CompletionStage<List<PunishmentData>> fetchPunishmentChunk(int limit, int offset) {
        return CompletableFuture.supplyAsync(() -> {
            List<PunishmentData> punishmentDataList = new ArrayList<>();
            String sql = "SELECT id, uuid, ip, reason, banned_by_uuid, banned_by_name, removed_by_uuid, removed_by_name, removed_by_reason, removed_by_date, time, until, template, server_scope, server_origin, silent, ipban, ipban_wildcard, active,'ban' as type FROM litebans_bans UNION ALL SELECT id, uuid, ip, reason, banned_by_uuid, banned_by_name, removed_by_uuid, removed_by_name, removed_by_reason, removed_by_date, time, until, template, server_scope, server_origin, silent, ipban, ipban_wildcard, active,'mute' as type FROM litebans_mutes UNION ALL SELECT id, uuid, ip, reason, banned_by_uuid, banned_by_name, removed_by_uuid, removed_by_name, removed_by_reason, removed_by_date, time, until, template, server_scope, server_origin, silent, ipban, ipban_wildcard, active,'warn' as type FROM litebans_warnings ORDER BY TIME ASC, id ASC LIMIT ? OFFSET ?";

            try (PreparedStatement stmt = Database.get().prepareStatement(sql)) {
                stmt.setInt(1, limit);
                stmt.setInt(2, offset);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    PunishmentData data = convertResultSetToData(rs, "sync");
                    punishmentDataList.add(data);
                }
            } catch (SQLException e) {
                LoggingUtil.error("[BanWarden] Error fetching punishments from LiteBans: " + e.getMessage());
            }

            return punishmentDataList;
        });
    }

    private PunishmentData convertEntryToData(Entry entry, boolean isRemoved, String fromEvent) {
        String type = getBanWardenPunishmentType(entry.getType()).name().toLowerCase();
        PunishmentData punishmentData = new PunishmentData();
        punishmentData.plugin_name = BanWardenPluginType.LITEBANS.name().toLowerCase();
        punishmentData.plugin_punishment_id = String.valueOf(entry.getId());
        punishmentData.type = type;
        punishmentData.start_at = entry.getDateStart();
        punishmentData.end_at = entry.getDateEnd();
        punishmentData.reason = entry.getReason();
        punishmentData.is_active = entry.isActive();
        punishmentData.server_scope = Objects.equals(entry.getServerScope(), "global") ? "*" : entry.getServerScope();
        punishmentData.origin_server_name = entry.getServerOrigin();
        punishmentData.uuid = entry.getUuid();
        punishmentData.ip_address = Objects.equals(entry.getIp(), "#") ? null : entry.getIp();
        punishmentData.is_ipban = entry.isIpban();
        punishmentData.creator_uuid = entry.getExecutorUUID();
        punishmentData.creator_username = entry.getExecutorName();
        punishmentData.from_event = fromEvent;

        if (isRemoved) {
            punishmentData.removed_at = System.currentTimeMillis();
            punishmentData.remover_uuid = entry.getRemovedByUUID();
            punishmentData.remover_username = entry.getRemovedByName();
            punishmentData.removed_reason = entry.getRemovalReason();
        }

        return punishmentData;
    }

    private PunishmentData convertResultSetToData(ResultSet rs, String fromEvent) throws SQLException {
        PunishmentData punishmentData = new PunishmentData();
        punishmentData.plugin_name = BanWardenPluginType.LITEBANS.name().toLowerCase();
        punishmentData.plugin_punishment_id = rs.getString("id");
        punishmentData.type = rs.getString("type").toLowerCase();
        punishmentData.start_at = rs.getLong("time");
        punishmentData.end_at = rs.getLong("until");
        punishmentData.reason = rs.getString("reason");
        punishmentData.is_active = rs.getBoolean("active");
        punishmentData.server_scope = rs.getString("server_scope");
        punishmentData.origin_server_name = rs.getString("server_origin");
        punishmentData.uuid = rs.getString("uuid");
        punishmentData.ip_address = Objects.equals(rs.getString("ip"), "#") ? null : rs.getString("ip");
        punishmentData.is_ipban = rs.getBoolean("ipban");
        punishmentData.creator_uuid = rs.getString("banned_by_uuid");
        punishmentData.creator_username = rs.getString("banned_by_name");
        punishmentData.from_event = fromEvent;

        if (rs.getTimestamp("removed_by_date") != null && !rs.getBoolean("active") && rs.getString("removed_by_name") != null && !rs.getString("removed_by_name").startsWith("#")) {
            punishmentData.removed_at = rs.getTimestamp("removed_by_date").getTime();
            punishmentData.remover_uuid = rs.getString("removed_by_uuid");
            punishmentData.remover_username = rs.getString("removed_by_name");
            punishmentData.removed_reason = rs.getString("removed_by_reason");
        }

        return punishmentData;
    }

    private BanWardenPunishmentType getBanWardenPunishmentType(String type) {
        return switch (type.toLowerCase()) {
            case "ban" -> BanWardenPunishmentType.BAN;
            case "mute" -> BanWardenPunishmentType.MUTE;
            case "warning", "warn" -> BanWardenPunishmentType.WARN;
            case "kick" -> BanWardenPunishmentType.KICK;
            default -> BanWardenPunishmentType.UNKNOWN;
        };
    }
}
