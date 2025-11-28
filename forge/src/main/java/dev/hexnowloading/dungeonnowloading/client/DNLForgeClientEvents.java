package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.client.model.DisabledFairkeeperChestModel;
import dev.hexnowloading.dungeonnowloading.block.client.model.FairkeeperChestModel;
import dev.hexnowloading.dungeonnowloading.block.client.model.PlayerStatueModel;
import dev.hexnowloading.dungeonnowloading.block.client.model.PlayerStatuePedestalModel;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.DisabledFairkeeperChestBlockRenderer;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.FairkeeperChestBlockRenderer;
import dev.hexnowloading.dungeonnowloading.screen.MendingTableScreen;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.PlayerStatueRenderer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.*;
import dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep.CopperCreepButlerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep.CopperCreepModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.*;
import dev.hexnowloading.dungeonnowloading.item.CopperDetonatorItem;
import dev.hexnowloading.dungeonnowloading.item.RepulsorItem;
import dev.hexnowloading.dungeonnowloading.item.client.model.ScorcherModel;
import dev.hexnowloading.dungeonnowloading.particle.*;
import dev.hexnowloading.dungeonnowloading.registry.*;
import net.minecraft.client.gui.screens.MenuScreens;

import net.minecraft.client.renderer.entity.NoopRenderer;
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
        event.registerLayerDefinition(WispModel.LAYER_LOCATION, WispModel::createBodyLayer);
        event.registerLayerDefinition(WispLanternModel.LAYER_LOCATION, WispLanternModel::createBodyLayer);
        event.registerLayerDefinition(ThumperModel.LAYER_LOCATION, ThumperModel::createBodyLayer);
        event.registerLayerDefinition(WebSpitterModel.LAYER_LOCATION, WebSpitterModel::createSpiderBodyLayer);
        event.registerLayerDefinition(ReaperSpiderModel.LAYER_LOCATION, ReaperSpiderModel::createSpiderBodyLayer);

        // Passive
        event.registerLayerDefinition(SealedChaosModel.LAYER_LOCATION, SealedChaosModel::createBodyLayer);
        event.registerLayerDefinition(WhimperModel.LAYER_LOCATION, WhimperModel::createBodyLayer);
        event.registerLayerDefinition(CopperCreepModel.LAYER_LOCATION, CopperCreepModel::createBodyLayer);
        event.registerLayerDefinition(CopperCreepButlerModel.LAYER_LOCATION, CopperCreepButlerModel::createBodyLayer);
        event.registerLayerDefinition(RepulsorModel.LAYER_LOCATION, RepulsorModel::createBodyLayer);
        event.registerLayerDefinition(MimicartModel.LAYER_LOCATION, MimicartModel::createBodyLayer);


        // Projectiles
        event.registerLayerDefinition(ChaosSpawnerProjectileModel.LAYER_LOCATION, ChaosSpawnerProjectileModel::createBodyLayer);
        event.registerLayerDefinition(VertexArrowProjectileModel.LAYER_LOCATION, VertexArrowProjectileModel::createBodyLayer);
        event.registerLayerDefinition(VertexPillarProjectileModel.LAYER_LOCATION, VertexPillarProjectileModel::createBodyLayer);
        event.registerLayerDefinition(BallistaArrowModel.LAYER_LOCATION, BallistaArrowModel::createBodyLayer);
        event.registerLayerDefinition(VertexOrbProjectileModel.LAYER_LOCATION, VertexOrbProjectileModel::createBodyLayer);
        event.registerLayerDefinition(VertexDomainProjectileModel.LAYER_LOCATION, VertexDomainProjectileModel::createBodyLayer);
        event.registerLayerDefinition(BorusArrowModel.LAYER_LOCATION, BorusArrowModel::createBodyLayer);

        // Block
        event.registerLayerDefinition(FairkeeperChestModel.LAYER_LOCATION, FairkeeperChestModel::createBodyLayer);
        event.registerLayerDefinition(DisabledFairkeeperChestModel.LAYER_LOCATION, DisabledFairkeeperChestModel::createBodyLayer);
        event.registerLayerDefinition(PlayerStatueModel.LAYER_LOCATION, PlayerStatueModel::createBodyLayer);
        event.registerLayerDefinition(PlayerStatuePedestalModel.LAYER_LOCATION, PlayerStatuePedestalModel::createBodyLayer);

        // Item
        event.registerLayerDefinition(ScorcherModel.LAYER_LOCATION, ScorcherModel::createBodyLayer);

    }
    public static void onRegisterRenderer(EntityRenderersEvent.RegisterRenderers event) {
        // Bosses
        event.registerEntityRenderer(DNLEntityTypes.CHAOS_SPAWNER.get(), ChaosSpawnerRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FAIRKEEPER_BOROS.get(), FairkeeperBorosRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FAIRKEEPER_BOROS_PART.get(), FairkeeperBorosBodyRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FAIRKEEPER_OUROS.get(), FairkeeperOurosRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FAIRKEEPER_OUROS_PART.get(), FairkeeperOurosBodyRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FAIRKEEPER_SERPENT_CALLER.get(), FairkeeperSerpentCallerRenderer::new);

        // Monsters
        event.registerEntityRenderer(DNLEntityTypes.HOLLOW.get(), HollowRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.SPAWNER_CARRIER.get(), SpawnerCarrierRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.SCUTTLE.get(), ScuttleRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.BALLISTA_GOLEM.get(), BallistaGolemRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WISP.get(), WispRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WISP_LANTERN.get(), WispLanternRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.THUMPER.get(), ThumperRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WEB_SPITTER.get(), WebSpitterRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.REAPER_SPIDER.get(), ReaperSpiderRenderer::new);

        // Passive
        event.registerEntityRenderer(DNLEntityTypes.SEALED_CHAOS.get(), SealedChaosRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WHIMPER.get(), WhimperRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.COPPER_CREEP.get(), CopperCreepRenderer::new);

        // Projectiles
        event.registerEntityRenderer(DNLEntityTypes.CHAOS_SPAWNER_PROJECTILE.get(), ChaosSpawnerProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FLAME_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.VERTEX_ARROW_PROJECTILE.get(), VertexArrowProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.VERTEX_PILLAR_PROJECTILE.get(), VertexPillarProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.BALLISTA_ARROW.get(), BallistaArrowRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.VERTEX_ORB_PROJECTILE.get(), VertexOrbProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.VERTEX_DOMAIN_PROJECTILE.get(), VertexDomainProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.BORUS_ARROW.get(), BorusArrowRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WEB_BALL.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.BULLET.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.GAS_CLOUD.get(), GasCloudRenderer::new);

        // Misc
        event.registerEntityRenderer(DNLEntityTypes.SPECIAL_ITEM_ENTITY.get(), SpecialItemEntityRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.GREAT_EXPERIENCE_BOTTLE.get(), (context) -> {
            return new ThrownItemRenderer<>(context, 1.25F, false);
        });
        event.registerEntityRenderer(DNLEntityTypes.REPULSOR.get(), RepulsorRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.MIMICART.get(), MimicartRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.MOB_SPAWN_EFFECT.get(), NoopRenderer::new);

        // Block Entities
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.FAIRKEEPER_CHEST.get(), FairkeeperChestBlockRenderer::new);
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.DISABLED_FAIRKEEPER_CHEST.get(), DisabledFairkeeperChestBlockRenderer::new);
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.PLAYER_STATUE.get(), PlayerStatueRenderer::new);

        // Item Properties
        ItemProperties.register(DNLItems.VERTEX_BOW.get(), new ResourceLocation("pull"), (stack, level, entity, idk) -> {
            if (entity == null) return 0.0F;
            else
                return entity.getUseItem() != stack ? 0.0F : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 30.0F;
        });

        ItemProperties.register(DNLItems.VERTEX_BOW.get(), new ResourceLocation("pulling"), (stack, level, entity, idk) ->
                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0f : 0.0f);

        ItemProperties.register(DNLItems.COPPER_DETONATOR.get(), new ResourceLocation("mode_switch"), (stack, BlockableEventLoop, entity, idk) -> {
            if (entity == null || entity.getUseItem() != stack) return 0.0F;

            int useTime = stack.getUseDuration() - entity.getUseItemRemainingTicks();
            return useTime > CopperDetonatorItem.MODE_SWITCH_TIMING ? 1.0F : 0.0F;
        });

        // register here to avoid race issues
        try {
            MenuScreens.register(DNLMenuTypes.MENDING_TABLE.get(), MendingTableScreen::new);
        } catch (Exception e) {
            DungeonNowLoading.LOGGER.warn("Failed to register mending table MenuScreen in onRegisterRenderer", e);
        }

        ItemProperties.register(DNLItems.REPULSOR.get(), new ResourceLocation("golden_mode"),
                (stack, level, entity, seed) -> RepulsorItem.isGoldenMode(stack) ? 1.0F : 0.0F);
    }

    public static void onRegisterParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(DNLParticleTypes.LARGE_FLAME_PARTICLE.get(), LargeFlameParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.LARGE_SOUL_FLAME_PARTICLE.get(), LargeSoulFlameParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.SCORCHER_FLAME_PARTICLE.get(), ScorcherFlameParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.VERTEX_SPARK_PARTICLE.get(), VertexSparkParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), FairkeeperBoundaryParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.VERTEX_BOUNDARY_PARTICLE.get(), VertexBoundaryParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(), RedstoneShockwaveParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.REDSTONE_HAZARD_INDICATOR_PARTICLE.get(), RedstoneHazardIndicatorParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.WHITE_SHOCKWAVE_PARTICLE.get(), WhiteShockwaveParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.ARROW_HAZARD_INDICATOR.get(), ArrowHazardIndicatorParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.MENDING_POP_PARTICLE.get(), MendingPopParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.MENDING_RUNE_PARTICLE.get(), MendingRuneParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.MENDING_RUNE_SHORT_PARTICLE.get(), MendingRuneShortParticle.Factory::new);
    }
}
