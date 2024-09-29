package com.xinecraft.minetrax.common.banwarden.hooks;

import com.xinecraft.minetrax.common.MinetraxCommon;
import com.xinecraft.minetrax.common.actions.ReportPlayerPunishment;
import com.xinecraft.minetrax.common.data.PunishmentData;
import com.xinecraft.minetrax.common.enums.BanWardenPluginType;
import com.xinecraft.minetrax.common.enums.BanWardenPunishmentType;
import com.xinecraft.minetrax.common.enums.BanWardenSyncType;
import com.xinecraft.minetrax.common.interfaces.banwarden.BanWardenHook;
import com.xinecraft.minetrax.common.utils.LoggingUtil;
import space.arim.libertybans.api.*;
import space.arim.libertybans.api.punish.Punishment;
import space.arim.libertybans.api.select.SelectionPredicate;
import space.arim.omnibus.Omnibus;
import space.arim.omnibus.OmnibusProvider;
import space.arim.omnibus.util.concurrent.ReactionStage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class LibertybansHook implements BanWardenHook {
    public static final MinetraxCommon common = MinetraxCommon.getInstance();
    private final Omnibus omnibus;
    private final LibertyBans libertyBans;
    private static final int RATE_LIMIT_DELAY_SECONDS = 1; // TODO change to 1 seconds
    private static final int CHUNK_SIZE = 50;

    public LibertybansHook() {
        this.omnibus = OmnibusProvider.getOmnibus();
        this.libertyBans = omnibus.getRegistry().getProvider(LibertyBans.class).orElseThrow();
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
        if (type != BanWardenSyncType.ACTIVE) {
            // info that LibertyBans only supports active bans syncing atm
            LoggingUtil.warning("[BanWarden] Note: LibertyBans integration only supports syncing active bans atm.");
        }
        syncPunishments().thenAccept(aVoid -> {
            LoggingUtil.info("[BanWarden] Syncing of punishments from LibertyBans completed.");
        });
    }

    private CompletableFuture<Void> syncPunishments() {
        AtomicInteger totalPunishments = new AtomicInteger(0);
        AtomicInteger offset = new AtomicInteger(0);

        return CompletableFuture.<Void>completedFuture(null).thenCompose(new Function<Void, ReactionStage<Void>>() {
            @Override
            public ReactionStage<Void> apply(Void unused) {
                return fetchPunishmentChunk(CHUNK_SIZE, offset.get())
                        .thenCompose(punishmentList -> {
                            int fetchedCount = punishmentList.size();
                            totalPunishments.addAndGet(fetchedCount);

                            // Process the current chunk
                            List<PunishmentData> punishmentDataList = new ArrayList<>();
                            punishmentList.forEach(punishment -> {
                                PunishmentData punishmentData = new PunishmentData();
                                punishmentData.plugin_name = BanWardenPluginType.LIBERTYBANS.name().toLowerCase();
                                punishmentData.plugin_punishment_id = String.valueOf(punishment.getIdentifier());
                                punishmentData.type = getBanWardenPunishmentType(punishment.getType()).name().toLowerCase();
                                punishmentData.is_active = true;
                                punishmentData.start_at = punishment.getStartDateSeconds() * 1000;
                                punishmentData.end_at = punishment.getEndDateSeconds() * 1000;
                                punishmentData.reason = punishment.getReason();

                                // Server scope
                                if (punishment.getScope().appliesTo("*")) {
                                    punishmentData.server_scope = "*";
                                } else {
                                    punishmentData.server_scope = "local";
                                }

                                // victim
                                if (punishment.getVictim().getType() == Victim.VictimType.PLAYER) {
                                    PlayerVictim victim = (PlayerVictim) punishment.getVictim();
                                    punishmentData.uuid = victim.getUUID().toString();
                                    punishmentData.is_ipban = false;
                                } else if (punishment.getVictim().getType() == Victim.VictimType.COMPOSITE) {
                                    CompositeVictim victim = (CompositeVictim) punishment.getVictim();
                                    punishmentData.uuid = victim.getUUID().toString();
                                    punishmentData.ip_address = victim.getAddress().toInetAddress().getHostAddress();
                                    punishmentData.is_ipban = true;
                                } else {
                                    AddressVictim victim = (AddressVictim) punishment.getVictim();
                                    punishmentData.ip_address = victim.getAddress().toInetAddress().getHostAddress();
                                    punishmentData.is_ipban = true;
                                }

                                // creator
                                if (punishment.getOperator().getType() == Operator.OperatorType.PLAYER) {
                                    PlayerOperator operator = (PlayerOperator) punishment.getOperator();
                                    punishmentData.creator_uuid = operator.getUUID().toString();
                                    punishmentData.creator_username = null;
                                }

                                // push to list
                                punishmentDataList.add(punishmentData);
                            });

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
                                LoggingUtil.info("[BanWarden] Finished syncing total of " + totalPunishments.get() + " punishments from LibertyBans.");
                                return CompletableFuture.completedFuture(null);
                            }
                        });
            }
        });
    }

    private ReactionStage<List<Punishment>> fetchPunishmentChunk(int limit, int offset) {
        return libertyBans.getSelector()
                .selectionBuilder()
                .selectActiveOnly()
                .types(SelectionPredicate.matchingAnyOf(PunishmentType.BAN, PunishmentType.MUTE, PunishmentType.WARN))
                .skipFirstRetrieved(offset)
                .limitToRetrieve(limit)
                .build()
                .getAllSpecificPunishments();
    }

    private BanWardenPunishmentType getBanWardenPunishmentType(PunishmentType type) {
        return switch (type) {
            case BAN -> BanWardenPunishmentType.BAN;
            case MUTE -> BanWardenPunishmentType.MUTE;
            case WARN -> BanWardenPunishmentType.WARN;
            case KICK -> BanWardenPunishmentType.KICK;
            default -> BanWardenPunishmentType.UNKNOWN;
        };
    }
}
