package net.pixeldreamstudios.summonerlib.data;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerSummonData {

    private static final String SUMMONS_KEY_PREFIX = "summonerlib_summons_";
    private static final String PERSISTENT_KEY = "summonerlib_persistent_data";

    public static void addSummon(LivingEntity player, String summonType, UUID summonUuid) {
        if (!(player instanceof ServerPlayerEntity)) return;

        NbtCompound nbt = getPersistentData((ServerPlayerEntity) player);
        String key = SUMMONS_KEY_PREFIX + summonType;

        NbtList list;
        if (nbt.contains(key, NbtElement.LIST_TYPE)) {
            list = nbt.getList(key, NbtElement.INT_ARRAY_TYPE);
        } else {
            list = new NbtList();
        }

        list.add(new NbtIntArray(uuidToIntArray(summonUuid)));
        nbt.put(key, list);
    }

    public static void removeSummon(LivingEntity player, String summonType, UUID summonUuid) {
        if (!(player instanceof ServerPlayerEntity)) return;

        NbtCompound nbt = getPersistentData((ServerPlayerEntity) player);
        String key = SUMMONS_KEY_PREFIX + summonType;

        if (nbt.contains(key, NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList(key, NbtElement.INT_ARRAY_TYPE);
            NbtList newList = new NbtList();

            for (int i = 0; i < list.size(); i++) {
                int[] uuidArray = list.getIntArray(i);
                if (uuidArray.length == 4) {
                    UUID uuid = uuidFromIntArray(uuidArray);
                    if (!uuid.equals(summonUuid)) {
                        newList.add(new NbtIntArray(uuidArray));
                    }
                }
            }

            nbt.put(key, newList);
        }
    }

    public static List<UUID> getSummons(LivingEntity player, String summonType) {
        if (!(player instanceof ServerPlayerEntity serverPlayer)) return List.of();

        NbtCompound nbt = getPersistentData(serverPlayer);
        String key = SUMMONS_KEY_PREFIX + summonType;
        List<UUID> uuids = new ArrayList<>();

        if (nbt.contains(key, NbtElement.LIST_TYPE)) {
            NbtList list = nbt.getList(key, NbtElement.INT_ARRAY_TYPE);
            for (int i = 0; i < list.size(); i++) {
                int[] uuidArray = list.getIntArray(i);
                if (uuidArray.length == 4) {
                    UUID uuid = uuidFromIntArray(uuidArray);
                    uuids.add(uuid);
                }
            }
        }

        return uuids;
    }

    public static boolean hasSummon(LivingEntity player, String summonType, UUID summonUuid) {
        return getSummons(player, summonType).contains(summonUuid);
    }

    public static void clearAllSummons(LivingEntity player, String summonType) {
        if (!(player instanceof ServerPlayerEntity)) return;

        NbtCompound nbt = getPersistentData((ServerPlayerEntity) player);
        String key = SUMMONS_KEY_PREFIX + summonType;
        nbt.remove(key);
    }

    public static void clearAllSummons(LivingEntity player) {
        if (!(player instanceof ServerPlayerEntity)) return;

        NbtCompound nbt = getPersistentData((ServerPlayerEntity) player);
        List<String> keysToRemove = new ArrayList<>();

        for (String key : nbt.getKeys()) {
            if (key.startsWith(SUMMONS_KEY_PREFIX)) {
                keysToRemove.add(key);
            }
        }

        keysToRemove.forEach(nbt::remove);
    }

    private static NbtCompound getPersistentData(ServerPlayerEntity player) {

        NbtCompound playerNbt = new NbtCompound();
        player.writeNbt(playerNbt);

        if (!playerNbt.contains(PERSISTENT_KEY, NbtElement.COMPOUND_TYPE)) {
            NbtCompound summonerData = new NbtCompound();
            playerNbt.put(PERSISTENT_KEY, summonerData);
            player.readNbt(playerNbt);
            return summonerData;
        }

        return playerNbt.getCompound(PERSISTENT_KEY);
    }

    private static int[] uuidToIntArray(UUID uuid) {
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();

        return new int[]{
                (int)(mostSigBits >> 32),
                (int)mostSigBits,
                (int)(leastSigBits >> 32),
                (int)leastSigBits
        };
    }

    private static UUID uuidFromIntArray(int[] array) {
        long mostSigBits = (long)array[0] << 32 | (long)array[1] & 0xFFFFFFFFL;
        long leastSigBits = (long)array[2] << 32 | (long)array[3] & 0xFFFFFFFFL;
        return new UUID(mostSigBits, leastSigBits);
    }
}