package dev.hexnowloading.dungeonnowloading.platform;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.platform.services.ReloadListenerPlatform;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = DungeonNowLoading.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class ForgeReloadListenerPlatform implements ReloadListenerPlatform {

    public record Entry(ResourceLocation id, PreparableReloadListener listener) {}

    private static final List<Entry> PENDING = new ArrayList<>();

    @Override
    public void registerDataReloadListener(ResourceLocation id, PreparableReloadListener listener) {
        PENDING.add(new Entry(id, listener));
    }

    public static List<Entry> drainPending() {
        List<Entry> out = new ArrayList<>(PENDING);
        PENDING.clear();
        return out;
    }
}
