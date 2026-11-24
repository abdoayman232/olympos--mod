package com.olympus.aether.world;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.*;

public class OlympusWorldState extends PersistentState {
    private static final String KEY = "olympus_aether_world_state";

    public int currentStage = 1;
    public final Set<Integer> generatedShrines = new HashSet<>();
    public final Set<Integer> claimedRelics = new HashSet<>();
    public final Map<Integer, BlockPos> shrinePositions = new HashMap<>();
    public final Set<UUID> initializedPlayers = new HashSet<>();
    public final Set<Long> warMarkRegions = new HashSet<>();

    public static class AetherData {
        public boolean unlocked = false;
        public int coreLayer = 1;
        public int aether = 100;
        public int maxAether = 100;
        public int activeRune = 0;
        public boolean destructionActive = false;
        public int destructionTicks = 0;
        public int insightXp = 0;
    }

    public final Map<UUID, AetherData> playerAether = new HashMap<>();

    public static final Type<OlympusWorldState> TYPE = new Type<>(
        OlympusWorldState::new,
        OlympusWorldState::createFromNbt,
        null
    );

    public static OlympusWorldState get(MinecraftServer server) {
        OlympusWorldState state = server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, KEY);
        if (state.shrinePositions.isEmpty()) {
            state.computeShrinePositions(server.getOverworld().getSeed());
        }
        return state;
    }

    public AetherData getOrCreateAether(UUID uuid) {
        return playerAether.computeIfAbsent(uuid, k -> new AetherData());
    }

    public void computeShrinePositions(long seed) {
        Random rng = new Random(seed ^ 0x4F4C594D505553L);
        int[][] configs = {
            {1,  180,  320,  68},
            {2,  350,  520,  72},
            {3,  480,  680,  18},
            {4,  650,  900,  76},
            {5,  850, 1150,  74},
            {6, 1050, 1350,  42},
            {7, 1200, 1550, -45},
            {8,  160,  320,  68},
            {9,  340,  540,  62},
            {10, 1450, 1800, 118},
            {11, 1650, 2000, 240},
            {12,  140,  260,  75}
        };
        for (int[] c : configs) {
            double angle = rng.nextDouble() * Math.PI * 2.0;
            int dist = c[1] + rng.nextInt(c[2] - c[1]);
            shrinePositions.put(c[0], new BlockPos((int) Math.round(Math.cos(angle) * dist), c[3], (int) Math.round(Math.sin(angle) * dist)));
        }
        markDirty();
    }

    public boolean canUnlockStage(int stage) { return stage <= currentStage; }

    public void markRelicClaimed(int stage) {
        if (claimedRelics.add(stage)) {
            if (stage >= currentStage) currentStage = Math.min(13, stage + 1);
            markDirty();
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putInt("currentStage", currentStage);
        nbt.putIntArray("generatedShrines", generatedShrines.stream().mapToInt(Integer::intValue).toArray());
        nbt.putIntArray("claimedRelics", claimedRelics.stream().mapToInt(Integer::intValue).toArray());

        NbtCompound posMap = new NbtCompound();
        for (Map.Entry<Integer, BlockPos> e : shrinePositions.entrySet()) {
            posMap.putLong(String.valueOf(e.getKey()), e.getValue().asLong());
        }
        nbt.put("shrinePositions", posMap);

        NbtCompound initTag = new NbtCompound();
        int idx = 0;
        for (UUID u : initializedPlayers) initTag.putUuid("p_" + (idx++), u);
        nbt.put("initializedPlayers", initTag);

        nbt.putLongArray("warMarkRegions", warMarkRegions.stream().mapToLong(Long::longValue).toArray());

        NbtCompound aetherMap = new NbtCompound();
        for (Map.Entry<UUID, AetherData> e : playerAether.entrySet()) {
            NbtCompound d = new NbtCompound();
            d.putBoolean("unlocked", e.getValue().unlocked);
            d.putInt("coreLayer", e.getValue().coreLayer);
            d.putInt("aether", e.getValue().aether);
            d.putInt("maxAether", e.getValue().maxAether);
            d.putInt("activeRune", e.getValue().activeRune);
            d.putInt("insightXp", e.getValue().insightXp);
            aetherMap.put(e.getKey().toString(), d);
        }
        nbt.put("playerAether", aetherMap);
        return nbt;
    }

    public static OlympusWorldState createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        OlympusWorldState state = new OlympusWorldState();
        state.currentStage = Math.max(1, nbt.getInt("currentStage"));
        for (int s : nbt.getIntArray("generatedShrines")) state.generatedShrines.add(s);
        for (int s : nbt.getIntArray("claimedRelics")) state.claimedRelics.add(s);

        NbtCompound posMap = nbt.getCompound("shrinePositions");
        for (String k : posMap.getKeys()) {
            try { state.shrinePositions.put(Integer.parseInt(k), BlockPos.fromLong(posMap.getLong(k))); } catch (Exception ignored) {}
        }

        NbtCompound initTag = nbt.getCompound("initializedPlayers");
        for (String k : initTag.getKeys()) {
            if (initTag.containsUuid(k)) state.initializedPlayers.add(initTag.getUuid(k));
        }

        for (long r : nbt.getLongArray("warMarkRegions")) state.warMarkRegions.add(r);

        NbtCompound aetherMap = nbt.getCompound("playerAether");
        for (String uuidStr : aetherMap.getKeys()) {
            try {
                UUID u = UUID.fromString(uuidStr);
                NbtCompound d = aetherMap.getCompound(uuidStr);
                AetherData ad = new AetherData();
                ad.unlocked = d.getBoolean("unlocked");
                ad.coreLayer = Math.max(1, d.getInt("coreLayer"));
                ad.aether = d.getInt("aether");
                ad.maxAether = Math.max(100, d.getInt("maxAether"));
                ad.activeRune = d.getInt("activeRune");
                ad.insightXp = d.getInt("insightXp");
                state.playerAether.put(u, ad);
            } catch (Exception ignored) {}
        }
        return state;
    }
}
