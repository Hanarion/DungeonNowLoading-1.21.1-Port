package dev.hexnowloading.dungeonnowloading.capabilities;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.capabilities.fabric.DNLArmPoseComponent;
import dev.hexnowloading.dungeonnowloading.capabilities.fabric.DNLArmPoseCapabilityHandler;
import dev.hexnowloading.dungeonnowloading.capabilities.fabric.FairkeeperChestPositionsCapabilityHandler;
import dev.hexnowloading.dungeonnowloading.capabilities.fabric.IFairkeeperChestPositionsCapability;
import net.minecraft.resources.ResourceLocation;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;

public class CapabilityList implements EntityComponentInitializer {

    public static final ComponentKey<IFairkeeperChestPositionsCapability> FAIRKEEPER_CHEST_POSITIONS_CAP = ComponentRegistry.getOrCreate(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "fairkeeper_chest_positions"), IFairkeeperChestPositionsCapability.class);
    public static final ComponentKey<DNLArmPoseComponent> DNL_ARM_POSE = ComponentRegistry.getOrCreate(ResourceLocation.fromNamespaceAndPath(DungeonNowLoading.MOD_ID, "dnl_arm_pose"), DNLArmPoseComponent.class);

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        // 1.21 / CCA 6.1.3: handlers implement RespawnableComponent; register with INVENTORY strategy
        // (copies on keepInventory/lossless, matching shouldCopyForRespawn(lossless || keepInventory)).
        registry.registerForPlayers(FAIRKEEPER_CHEST_POSITIONS_CAP, player -> new FairkeeperChestPositionsCapabilityHandler(), RespawnCopyStrategy.INVENTORY);
        registry.registerForPlayers(DNL_ARM_POSE, player -> new DNLArmPoseCapabilityHandler(), RespawnCopyStrategy.ALWAYS_COPY);
    }
}
