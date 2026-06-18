package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.block.client.model.*;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.*;
import dev.hexnowloading.dungeonnowloading.client.model.MendingAuraForgeBakedModel;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.*;
import dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep.CopperCreepButlerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep.CopperCreepModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulChaosSpawnerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulSerpentCallerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.*;
import dev.hexnowloading.dungeonnowloading.item.CopperDetonatorItem;
import dev.hexnowloading.dungeonnowloading.item.RepulsorItem;
import dev.hexnowloading.dungeonnowloading.item.client.model.ScorcherModel;
import dev.hexnowloading.dungeonnowloading.item.client.model.WisplightRodModel;
import dev.hexnowloading.dungeonnowloading.platform.ForgeClientHelper;
import dev.hexnowloading.dungeonnowloading.particle.*;
import dev.hexnowloading.dungeonnowloading.registry.*;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.gui.screens.MenuScreens;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class DNLForgeClientEvents {
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        ForgeClientHelper.ITEM_MODELS.forEach(event::register);
    }

    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll((id, model) -> DungeonNowLoading.MOD_ID.equals(id.getNamespace()) && isMendingAuraModel(id.getPath())
                ? new MendingAuraForgeBakedModel(model)
                : model);
    }

    private static boolean isMendingAuraModel(String path) {
        return path.equals("mending_aura") || path.startsWith("block/mending_aura_");
    }

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
        event.registerLayerDefinition(GarholdModel.LAYER_LOCATION, GarholdModel::createBodyLayer);
        event.registerLayerDefinition(BrokenGarholdModel.LAYER_LOCATION, BrokenGarholdModel::createBodyLayer);
        event.registerLayerDefinition(WispModel.LAYER_LOCATION, WispModel::createBodyLayer);
        event.registerLayerDefinition(LargeWispModel.LAYER_LOCATION, LargeWispModel::createBodyLayer);
        event.registerLayerDefinition(WispLanternModel.LAYER_LOCATION, WispLanternModel::createBodyLayer);
        event.registerLayerDefinition(SilkSpiderModel.LAYER_LOCATION, SilkSpiderModel::createBodyLayer);
        event.registerLayerDefinition(ReaperSpiderModel.LAYER_LOCATION, ReaperSpiderModel::createBodyLayer);

        // Passive
        event.registerLayerDefinition(SealedChaosModel.LAYER_LOCATION, SealedChaosModel::createBodyLayer);
        event.registerLayerDefinition(WhimperModel.LAYER_LOCATION, WhimperModel::createBodyLayer);
        event.registerLayerDefinition(CopperCreepModel.LAYER_LOCATION, CopperCreepModel::createBodyLayer);
        event.registerLayerDefinition(CopperCreepButlerModel.LAYER_LOCATION, CopperCreepButlerModel::createBodyLayer);
        event.registerLayerDefinition(RepulsorModel.LAYER_LOCATION, RepulsorModel::createBodyLayer);
        event.registerLayerDefinition(MimicartModel.LAYER_LOCATION, MimicartModel::createBodyLayer);
        event.registerLayerDefinition(WispwardLanternModel.LAYER_LOCATION, WispwardLanternModel::createBodyLayer);

        // Projectiles
        event.registerLayerDefinition(ChaosSpawnerProjectileModel.LAYER_LOCATION, ChaosSpawnerProjectileModel::createBodyLayer);
        event.registerLayerDefinition(WispProjectileModel.LAYER_LOCATION, WispProjectileModel::createBodyLayer);
        event.registerLayerDefinition(LargeWispProjectileModel.LAYER_LOCATION, LargeWispProjectileModel::createBodyLayer);
        event.registerLayerDefinition(VertexArrowProjectileModel.LAYER_LOCATION, VertexArrowProjectileModel::createBodyLayer);
        event.registerLayerDefinition(VertexPillarProjectileModel.LAYER_LOCATION, VertexPillarProjectileModel::createBodyLayer);
        event.registerLayerDefinition(BallistaArrowModel.LAYER_LOCATION, BallistaArrowModel::createBodyLayer);
        event.registerLayerDefinition(VertexOrbProjectileModel.LAYER_LOCATION, VertexOrbProjectileModel::createBodyLayer);
        event.registerLayerDefinition(VertexDomainProjectileModel.LAYER_LOCATION, VertexDomainProjectileModel::createBodyLayer);
        event.registerLayerDefinition(BorusArrowModel.LAYER_LOCATION, BorusArrowModel::createBodyLayer);
        event.registerLayerDefinition(WebSpitModel.LAYER_LOCATION, WebSpitModel::createBodyLayer);

        // Block
        event.registerLayerDefinition(FairkeeperChestModel.LAYER_LOCATION, FairkeeperChestModel::createBodyLayer);
        event.registerLayerDefinition(DisabledFairkeeperChestModel.LAYER_LOCATION, DisabledFairkeeperChestModel::createBodyLayer);
        event.registerLayerDefinition(PlayerStatueModel.LAYER_LOCATION, PlayerStatueModel::createBodyLayer);
        event.registerLayerDefinition(PlayerStatuePedestalModel.LAYER_LOCATION, PlayerStatuePedestalModel::createBodyLayer);
        event.registerLayerDefinition(DungeonBannerBlockModel.LAYER_LOCATION, DungeonBannerBlockModel::createBodyLayer);
        event.registerLayerDefinition(WispwardChestModel.LAYER_LOCATION, WispwardChestModel::createBodyLayer);
        event.registerLayerDefinition(BurnacleBudModel.LAYER_LOCATION, BurnacleBudModel::createBodyLayer);
        event.registerLayerDefinition(BurnacleJuvenileModel.LAYER_LOCATION, BurnacleJuvenileModel::createBodyLayer);
        event.registerLayerDefinition(BurnacleMatureModel.LAYER_LOCATION, BurnacleMatureModel::createBodyLayer);
        event.registerLayerDefinition(BurnacleElderModel.LAYER_LOCATION, BurnacleElderModel::createBodyLayer);

        // Item
        event.registerLayerDefinition(ScorcherModel.LAYER_LOCATION, ScorcherModel::createBodyLayer);
        event.registerLayerDefinition(WisplightRodModel.LAYER_LOCATION, WisplightRodModel::createBodyLayer);

        // Misc
        event.registerLayerDefinition(SeepingSoulChaosSpawnerModel.LAYER_LOCATION, SeepingSoulChaosSpawnerModel::createBodyLayer);
        event.registerLayerDefinition(SeepingSoulSerpentCallerModel.LAYER_LOCATION, SeepingSoulSerpentCallerModel::createBodyLayer);
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
        event.registerEntityRenderer(DNLEntityTypes.GARHOLD.get(), GarholdRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.BROKEN_GARHOLD.get(), BrokenGarholdRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WISP.get(), WispRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.LARGE_WISP.get(), LargeWispRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WISP_LANTERN.get(), WispLanternRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.SILK_SPIDER.get(), SilkSpiderRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.REAPER_SPIDER.get(), ReaperSpiderRenderer::new);

        // Passive
        event.registerEntityRenderer(DNLEntityTypes.SEALED_CHAOS.get(), SealedChaosRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WHIMPER.get(), WhimperRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.COPPER_CREEP.get(), CopperCreepRenderer::new);

        // Projectiles
        event.registerEntityRenderer(DNLEntityTypes.CHAOS_SPAWNER_PROJECTILE.get(), ChaosSpawnerProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.FLAME_PROJECTILE.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WISP_PROJECTILE.get(), WispProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.LARGE_WISP_PROJECTILE.get(), LargeWispProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.VERTEX_ARROW_PROJECTILE.get(), VertexArrowProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.VERTEX_PILLAR_PROJECTILE.get(), VertexPillarProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.BALLISTA_ARROW.get(), BallistaArrowRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.VERTEX_ORB_PROJECTILE.get(), VertexOrbProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.VERTEX_DOMAIN_PROJECTILE.get(), VertexDomainProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.BORUS_ARROW.get(), BorusArrowRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WEB_SPIT_PROJECTILE.get(), WebSpitProjectileRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.GAS_CLOUD.get(), GasCloudRenderer::new);

        // Misc
        event.registerEntityRenderer(DNLEntityTypes.SPECIAL_ITEM_ENTITY.get(), SpecialItemEntityRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.GREAT_EXPERIENCE_BOTTLE.get(), (context) -> {
            return new ThrownItemRenderer<>(context, 1.25F, false);
        });
        event.registerEntityRenderer(DNLEntityTypes.REPULSOR.get(), RepulsorRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.SEEPING_SOUL.get(), SeepingSoulRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.MIMICART.get(), MimicartRenderer::new);
        event.registerEntityRenderer(DNLEntityTypes.WISPWARD_LANTERN_CART.get(), WispwardLanternCartRenderer::new);
        // Block Entities
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.FAIRKEEPER_CHEST.get(), FairkeeperChestBlockRenderer::new);
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.DISABLED_FAIRKEEPER_CHEST.get(), DisabledFairkeeperChestBlockRenderer::new);
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.PLAYER_STATUE.get(), PlayerStatueRenderer::new);
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.DUNGEON_DIRECTOR.get(), DungeonDirectorRenderer::new);
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.DUNGEON_BANNER.get(), DungeonBannerBlockRenderer::new);
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.MENDING_AURA.get(), MendingAuraBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.WISP_BLOCK.get(), WispBlockRenderer::new);
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.WISPWARD_CHEST.get(), WispwardChestBlockRenderer::new);
        event.registerBlockEntityRenderer(DNLBlockEntityTypes.BURNACLE.get(), BurnacleBlockRenderer::new);

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
        event.registerSpriteSet(DNLParticleTypes.WHITE_SHOCKWAVE_MEDIUM_PARTICLE.get(), WhiteShockwaveParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.ARROW_HAZARD_INDICATOR.get(), ArrowHazardIndicatorParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.MENDING_POP_AND_RUNE_PARTICLE.get(), MendingPopAndRuneParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.MENDING_RUNE_PARTICLE.get(), MendingRuneParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.MENDING_RUNE_SHORT_PARTICLE.get(), MendingRuneShortParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.MENDING_FADE_PARTICLE.get(), MendingFadeParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.WISPWARD_FLAME_TRAVEL_PARTICLE.get(), WispwardFlameTravelParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.MENDING_POP_PARTICLE.get(), MendingPopParticle.Factory::new);
        event.registerSpriteSet(DNLParticleTypes.BURNACLE_GAS_PARTICLE.get(), BurnacleGasParticle.Factory::new);
    }

    public static void onRegisterBlockRenderTypes(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DUNGEON_DIRECTOR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.SPAWN_NODE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.WISP_BLOCK.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.WISPWARD_LANTERN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.TIMED_WISPWARD_LANTERN.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_PLATFORM_FRAME.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_PLATFORM_FLOATING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_PLATFORM_FLOATING_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_PLATFORM_FRAME_TOP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_PLATFORM_FRAME_TOP_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_PLATFORM_SUSPENDED.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_PLATFORM_SUSPENDED_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_SLOPED_PLATFORM_FLOATING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_SLOPED_PLATFORM_FLOATING_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_PLATFORM_ENCLOSED_STAIRS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_MOUNTED_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_MOUNTED_POWERED_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_MOUNTED_DETECTOR_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.DEEPSTEEL_MOUNTED_ACTIVATOR_RAIL.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.WEBBING_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.WEBBING_NEST_BLOCK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(DNLBlocks.SUSPENDED_WEB.get(), RenderType.cutout());
        });
    }

}
