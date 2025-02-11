package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.block.client.model.DisabledFairkeeperChestModel;
import dev.hexnowloading.dungeonnowloading.block.client.model.FairkeeperChestModel;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.DisabledFairkeeperChestBlockRenderer;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.FairkeeperChestBlockRenderer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.*;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.*;
import dev.hexnowloading.dungeonnowloading.particle.FairkeeperBoundaryParticle;
import dev.hexnowloading.dungeonnowloading.particle.LargeFlameParticle;
import dev.hexnowloading.dungeonnowloading.particle.RedstoneShockwaveParticle;
import dev.hexnowloading.dungeonnowloading.particle.VertexSparkParticle;
import dev.hexnowloading.dungeonnowloading.registry.*;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

public class DNLForgeClientEvents {
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // Bosses
        event.registerLayerDefinition(ChaosSpawnerModel.LAYER_LOCATION, ChaosSpawnerModel::createBodyLayer);
        event.registerLayerDefinition(FairkeeperBorosModel.LAYER_LOCATION, FairkeeperBorosModel::createBodyLayer);
        event.registerLayerDefinition(FairkeeperBorosBodyModel.LAYER_LOCATION, FairkeeperBorosBodyModel::createBodyLayer);
        event.registerLayerDefinition(FairkeeperOurosModel.LAYER_LOCATION, FairkeeperOurosModel::createBodyLayer);
        event.registerLayerDefinition(FairkeeperOurosBodyModel.LAYER_LOCATION, FairkeeperOurosBodyModel::createBodyLayer);
        event.registerLayerDefinition(FairkeeperSerpentCallerModel.LAYER_LOCATION, FairkeeperSerpentCallerModel::createBodyLayer);

        // Monsters
        event.registerLayerDefinition(HollowModel.LAYER_LOCATION, HollowModel::createBodyLayer);
        event.registerLayerDefinition(SpawnerCarrierModel.LAYER_LOCATION, SpawnerCarrierModel::createBodyLayer);
        event.registerLayerDefinition(ScuttleModel.LAYER_LOCATION, ScuttleModel::createBodyLayer);
        event.registerLayerDefinition(BallistaGolemModel.LAYER_LOCATION, BallistaGolemModel::createBodyLayer);

        // Passive
        event.registerLayerDefinition(SealedChaosModel.LAYER_LOCATION, SealedChaosModel::createBodyLayer);
        event.registerLayerDefinition(WhimperModel.LAYER_LOCATION, WhimperModel::createBodyLayer);
        event.registerLayerDefinition(CopperCreepModel.LAYER_LOCATION, CopperCreepModel::createBodyLayer);

        // Projectiles
        event.registerLayerDefinition(ChaosSpawnerProjectileModel.LAYER_LOCATION, ChaosSpawnerProjectileModel::createBodyLayer);
        event.registerLayerDefinition(StonePillarProjectileModel.LAYER_LOCATION, StonePillarProjectileModel::createBodyLayer);
        event.registerLayerDefinition(VertexArrowProjectileModel.LAYER_LOCATION, VertexArrowProjectileModel::createBodyLayer);
        event.registerLayerDefinition(ShieldingStonePillarProjectileModel.LAYER_LOCATION, ShieldingStonePillarProjectileModel::createBodyLayer);
        event.registerLayerDefinition(BallistaArrowModel.LAYER_LOCATION, BallistaArrowModel::createBodyLayer);
        event.registerLayerDefinition(VertexOrbProjectileModel.LAYER_LOCATION, VertexOrbProjectileModel::createBodyLayer);

        event.registerLayerDefinition(FairkeeperChestModel.LAYER_LOCATION, FairkeeperChestModel::createBodyLayer);
        event.registerLayerDefinition(DisabledFairkeeperChestModel.LAYER_LOCATION, DisabledFairkeeperChestModel::createBodyLayer);
    }
    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        // Bosses
        event.registerEntityRenderer(DNLEntityTypes.CHAOS_SPAWNER.get(), ChaosSpawnerRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FAIRKEEPER.get(), FairkeeperBorosRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FAIRKEEPER_BOROS_PART.get(), FairkeeperBorosBodyRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FAIRKEEPER_OUROS.get(), FairkeeperOurosRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FAIRKEEPER_OUROS_PART.get(), FairkeeperOurosBodyRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FAIRKEEPER_SERPENT_CALLER.get(), FairkeeperSerpentCallerRenderer::new);

        // Monsters
        event.registerEntityRenderer(DNLEntityTypes.HOLLOW.get(), HollowRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.SPAWNER_CARRIER.get(), SpawnerCarrierRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.SCUTTLE.get(), ScuttleRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.BALLISTA_GOLEM.get(), BallistaGolemRenderer::new);

        // Passive
        event.registerEntityRenderer(DNLEntityTypes.SEALED_CHAOS.get(), SealedChaosRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WHIMPER.get(), WhimperRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.COPPER_CREEP.get(), CopperCreepRenderer::new);

        // Projectiles
        event.registerEntityRenderer(DNLEntityTypes.CHAOS_SPAWNER_PROJECTILE.get(), ChaosSpawnerProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FLAME_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.STONE_PILLAR_PROJECTILE.get(), StonePillarProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.VERTEX_ARROW_PROJECTILE.get(), VertexArrowProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.SHIELDING_STONE_PILLAR_PROJECTILE.get(), ShieldingStonePillarProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.BALLISTA_ARROW.get(), BallistaArrowRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.VERTEX_ORB_PROJECTILE.get(), VertexOrbProjectileRenderer::new);

        // Misc
        event.registerEntityRenderer(DNLEntityTypes.SPECIAL_ITEM_ENTITY.get(), SpecialItemEntityRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.GREAT_EXPERIENCE_BOTTLE.get(), (context) -> {
            return new ThrownItemRenderer<>(context, 1.25F, false);
        });

        // Block Entities
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.FAIRKEEPER_CHEST.get(), FairkeeperChestBlockRenderer::new);
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.DISABLED_FAIRKEEPER_CHEST.get(), DisabledFairkeeperChestBlockRenderer::new);

        // Item Properties
        ItemProperties.register(DNLItems.VERTEX_BOW.get(), new ResourceLocation("pull"), (stack, level, entity, idk) -> {
            if (entity == null) return 0.0F;
            else
                return entity.getUseItem() != stack ? 0.0F : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 30.0F;
        });

        ItemProperties.register(DNLItems.VERTEX_BOW.get(), new ResourceLocation("pulling"), (stack, level, entity, idk) ->
                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(DNLParticleTypes.LARGE_FLAME_PARTICLE.get(), LargeFlameParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.VERTEX_SPARK_PARTICLE.get(), VertexSparkParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), FairkeeperBoundaryParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(), RedstoneShockwaveParticle.Factory::new);
    }
}
