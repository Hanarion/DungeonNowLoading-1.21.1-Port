package dev.hexnowloading.dungeonnowloading.capabilities;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.capabilities.fabric.*;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import net.minecraft.resources.ResourceLocation;

public class CapabilityList implements EntityComponentInitializer {

    public static final ComponentKey<IFairkeeperChestPositionsCapability> FAIRKEEPER_CHEST_POSITIONS_CAP = ComponentRegistry.getOrCreate(new ResourceLocation(DungeonNowLoading.MOD_ID, "fairkeeper_chest_positions"), IFairkeeperChestPositionsCapability.class);
    public static final ComponentKey<IntComponent> SCORCHER_HEAT = ComponentRegistry.getOrCreate(new ResourceLocation(DungeonNowLoading.MOD_ID, "scorcher_heat"), IntComponent.class);
    public static final ComponentKey<DNLArmPoseComponent> DNL_ARM_POSE = ComponentRegistry.getOrCreate(new ResourceLocation(DungeonNowLoading.MOD_ID, "dnl_arm_pose"), DNLArmPoseComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(FAIRKEEPER_CHEST_POSITIONS_CAP, player -> new FairkeeperChestPositionsCapabilityHandler());
        registry.registerForPlayers(SCORCHER_HEAT, player -> new ScorcherHeatCapabilityHandler());
        registry.registerForPlayers(DNL_ARM_POSE, player -> new DNLArmPoseCapabilityHandler());
    }
}