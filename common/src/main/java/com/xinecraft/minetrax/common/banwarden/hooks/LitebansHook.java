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
    public void upsert(PunishmentData punishmentData) {

    }

    @Override
    public void remove(String punishmentId) {

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
                    PunishmentData data = convertEntryToData(entry);
                    ReportPlayerPunishment.reportSync(data);
                } catch (Exception e) {
                    LoggingUtil.error("[BanWarden] EntryAdded -> Error reporting event to Minetrax: " + e.getMessage());
                }
            }

            @Override
            public void entryRemoved(Entry entry) {
                try {
                    PunishmentData data = convertEntryToData(entry);
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
            String sql = "SELECT id, uuid, ip, reason, banned_by_uuid, banned_by_name, removed_by_uuid, removed_by_name, removed_by_reason, removed_by_date, time, until, template, server_scope, server_origin, silent, ipban, ipban_wildcard, active,'ban'as type FROM litebans_bans UNION ALL SELECT id, uuid, ip, reason, banned_by_uuid, banned_by_name, removed_by_uuid, removed_by_name, removed_by_reason, removed_by_date, time, until, template, server_scope, server_origin, silent, ipban, ipban_wildcard, active,'mute'as type FROM litebans_mutes UNION ALL SELECT id, uuid, ip, reason, banned_by_uuid, banned_by_name, removed_by_uuid, removed_by_name, removed_by_reason, removed_by_date, time, until, template, server_scope, server_origin, silent, ipban, ipban_wildcard, active,'warn'as type FROM litebans_warnings ORDER BY id ASC LIMIT ? OFFSET ?";

            try (PreparedStatement stmt = Database.get().prepareStatement(sql)) {
                stmt.setInt(1, limit);
                stmt.setInt(2, offset);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    // Print fetched data.
                    String s = rs.getString("id") + rs.getString("uuid") + ", " + rs.getString("reason") + ", " + rs.getString("server_origin");
                    System.out.println(s);

                    PunishmentData data = convertResultSetToData(rs);
                    punishmentDataList.add(data);
                }
            } catch (SQLException e) {
                LoggingUtil.error("[BanWarden] Error fetching punishments from LiteBans: " + e.getMessage());
            }

            return punishmentDataList;
        });
    }

    private PunishmentData convertEntryToData(Entry entry) {
        String type = getBanWardenPunishmentType(entry.getType()).name().toLowerCase();
        PunishmentData punishmentData = new PunishmentData();
        punishmentData.plugin_name = BanWardenPluginType.LITEBANS.name().toLowerCase();
        punishmentData.plugin_punishment_id = String.valueOf(entry.getId());
        punishmentData.type = type;
        punishmentData.start_at = entry.getDateStart();
        punishmentData.end_at = entry.getDateEnd();
        punishmentData.reason = entry.getReason();
        punishmentData.is_active = entry.isActive();
        punishmentData.server_scope = entry.getServerScope();
        punishmentData.origin_server_name = entry.getServerOrigin();
        punishmentData.uuid = entry.getUuid();
        punishmentData.ip_address = entry.getIp();
        punishmentData.is_ipban = entry.getType().equalsIgnoreCase("ipban");
        punishmentData.creator_uuid = entry.getExecutorUUID();
        punishmentData.creator_username = entry.getExecutorName();

        punishmentData.removed_at = 0; // TODO
        punishmentData.remover_uuid = entry.getRemovedByUUID();
        punishmentData.remover_username = entry.getRemovedByName();

        return punishmentData;
    }

    private PunishmentData convertResultSetToData(ResultSet rs) throws SQLException {
        PunishmentData punishmentData = new PunishmentData();
        punishmentData.plugin_name = BanWardenPluginType.LITEBANS.name().toLowerCase();
        punishmentData.plugin_punishment_id = rs.getString("id");
        punishmentData.type = rs.getString("type").toLowerCase();
        punishmentData.start_at = rs.getLong("time");
        punishmentData.end_at = rs.getLong("until");
        punishmentData.reason = rs.getString("reason");
        punishmentData.is_active = true;
        punishmentData.server_scope = rs.getString("server_scope");
        punishmentData.uuid = rs.getString("uuid");
        punishmentData.ip_address = rs.getString("ip");
        punishmentData.is_ipban = punishmentData.type.equals("ban") && rs.getBoolean("ipban");
        punishmentData.creator_uuid = rs.getString("banned_by_uuid");
        punishmentData.creator_username = rs.getString("banned_by_name");

        return punishmentData;
    }

    private BanWardenPunishmentType getBanWardenPunishmentType(String type) {
        return switch (type.toLowerCase()) {
            case "ban" -> BanWardenPunishmentType.BAN;
            case "mute" -> BanWardenPunishmentType.MUTE;
            case "warning" -> BanWardenPunishmentType.WARN;
            case "kick" -> BanWardenPunishmentType.KICK;
            default -> BanWardenPunishmentType.UNKNOWN;
        };
    }
}
