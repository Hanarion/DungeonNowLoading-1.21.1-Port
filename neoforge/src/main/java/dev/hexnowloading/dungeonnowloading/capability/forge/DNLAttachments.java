package dev.hexnowloading.dungeonnowloading.capability.forge;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

/**
 * 1.21 NeoForge replaced Forge capabilities with data attachments. These attach the
 * arm-pose and fairkeeper-chest-position data to players, serialized via INBTSerializable
 * and carried across death (copyOnDeath).
 */
public final class DNLAttachments {
    private DNLAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DungeonNowLoading.MOD_ID);

    public static final Supplier<AttachmentType<DNLArmPoseCapability>> DNL_ARM_POSE =
            ATTACHMENT_TYPES.register("dnl_arm_pose", () -> AttachmentType
                    .serializable(DNLArmPoseCapability::new)
                    .copyOnDeath()
                    .build());

    public static final Supplier<AttachmentType<FairkeeperChestPositionsCapability>> FAIRKEEPER_CHEST_POSITIONS =
            ATTACHMENT_TYPES.register("fairkeeper_chest_positions", () -> AttachmentType
                    .serializable(FairkeeperChestPositionsCapability::new)
                    .copyOnDeath()
                    .build());
}
