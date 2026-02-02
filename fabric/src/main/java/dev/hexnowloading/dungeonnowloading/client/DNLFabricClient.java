package dev.hexnowloading.dungeonnowloading.client;

import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.block.DungeonBannerBlock;
import dev.hexnowloading.dungeonnowloading.block.client.model.*;
import dev.hexnowloading.dungeonnowloading.block.client.renderer.*;
import dev.hexnowloading.dungeonnowloading.client.preview.PreviewOverlayFabric;
import dev.hexnowloading.dungeonnowloading.client.render.PayloadEntityRenderer;
import dev.hexnowloading.dungeonnowloading.entity.client.model.*;
import dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep.CopperCreepButlerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.copper_creep.CopperCreepModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulChaosSpawnerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.seeping_soul.SeepingSoulSerpentCallerModel;
import dev.hexnowloading.dungeonnowloading.entity.client.renderer.*;
import dev.hexnowloading.dungeonnowloading.item.CopperDetonatorItem;
import dev.hexnowloading.dungeonnowloading.item.RepulsorItem;
import dev.hexnowloading.dungeonnowloading.item.client.model.ScorcherModel;
import dev.hexnowloading.dungeonnowloading.item.client.renderer.PlayerStatueItemRenderer;
import dev.hexnowloading.dungeonnowloading.item.client.renderer.ScorcherRenderer;
import dev.hexnowloading.dungeonnowloading.particle.*;
import dev.hexnowloading.dungeonnowloading.registry.*;
import dev.hexnowloading.dungeonnowloading.render.Caltrop;
import dev.hexnowloading.dungeonnowloading.screen.MendingTableScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.List;
import java.util.Map;

public class DNLFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DNLPackets.registerServerbound();
        DNLPackets.registerClientbound();

        MenuScreens.register(DNLMenuTypes.MENDING_TABLE.get(), MendingTableScreen::new);
        registerItemModelLayers();
        registerItemRenderers();
        registerBlockRenderers();
        registerModelLayers();
        registerRenderers();
        registerParticleFactories();
        MendstonePickaxeParticleHandlerFabric.register();
        PreviewOverlayFabric.register();

        final var ID = new ResourceLocation("dungeonnowloading", "serverbound_pedestal_update");
        ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
            System.out.println("[Server] registered receiver hit for " + ID);
            var pkt = dev.hexnowloading.dungeonnowloading.network.packets.C2SPedestalUpdatePacket.decode(buf);
            server.execute(() -> pkt.handle(player));  // hop to main thread, then call your handle()
        });

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> addDnlEnchantmentDescriptions(stack, lines));
    }

    private void registerItemModelLayers() {
        EntityModelLayerRegistry.registerModelLayer(ScorcherModel.LAYER_LOCATION, ScorcherModel::createBodyLayer);
    }

    private void registerItemRenderers() {

        // Block
        BuiltinItemRendererRegistry.INSTANCE.register(DNLItems.FAIRKEEPER_CHEST.get(), FairkeeperChestItemRenderer.getInstance()::renderByItem);
        BuiltinItemRendererRegistry.INSTANCE.register(DNLItems.WISE_FAIRKEEPER_CHEST.get(), WiseFairkeeperChestItemRenderer.getInstance()::renderByItem);
        BuiltinItemRendererRegistry.INSTANCE.register(DNLItems.FIERCE_FAIRKEEPER_CHEST.get(), FierceFairkeeperChestItemRenderer.getInstance()::renderByItem);
        BuiltinItemRendererRegistry.INSTANCE.register(DNLItems.PLAYER_STATUE.get(), PlayerStatueItemRenderer.getInstance()::renderByItem);
        BuiltinItemRendererRegistry.INSTANCE.register(DNLItems.GAUNTLET.get(), GauntletItemRenderer.getInstance()::renderByItem);
        BuiltinItemRendererRegistry.INSTANCE.register(DNLItems.GAUNTLET_VAULT.get(), GauntletVaultItemRenderer.getInstance()::renderByItem);
        for (DungeonBannerBlock.DungeonBannerVariant variant : DungeonBannerBlock.DungeonBannerVariant.values()) {
            BuiltinItemRendererRegistry.INSTANCE.register(
                    DNLItems.getBannerItem(variant).get(),
                    (stack, displayContext, poseStack, buffer, light, overlay) -> {
                        DungeonBannerBlockItemRenderer.getInstance()
                                .renderByItem(stack, displayContext, poseStack, buffer, light, overlay);
                    }
            );
        }


        // Item
        BuiltinItemRendererRegistry.INSTANCE.register(DNLItems.SCORCHER.get(), ScorcherRenderer.getInstance()::renderByItem);
        BuiltinItemRendererRegistry.INSTANCE.register(DNLItems.SOUL_SCORCHER.get(), ScorcherRenderer.getInstance()::renderByItem);
        //BuiltinItemRendererRegistry.INSTANCE.register(DNLItems.SCORCHER.get(), new DifferentProspectiveItemRenderer(DNLClientRegistry.SCORCHER_3D_MODEL, DNLClientRegistry.SCORCHER_3D_MODEL));

    }

    private void registerBlockRenderers() {
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.CHAOS_SPAWNER_BARRIER_CENTER.get()  , RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.CHAOS_SPAWNER_BARRIER_EDGE.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.CHAOS_SPAWNER_BARRIER_VERTEX.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.CHAOS_SPAWNER_DIAMOND_EDGE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.CHAOS_SPAWNER_DIAMOND_VERTEX.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.CHAOS_SPAWNER_BROKEN_DIAMOND_EDGE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.CHAOS_SPAWNER_BROKEN_DIAMOND_VERTEX.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.CHAOS_SPAWNER_EDGE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.CHAOS_SPAWNER_BROKEN_EDGE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.SPIKES.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.MOSS.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.FAIRKEEPER_CHEST.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.WISE_FAIRKEEPER_CHEST.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.FIERCE_FAIRKEEPER_CHEST.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.FAIRKEEEPER_SPAWNER.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.ROTATOR_PRESSURE_PLATE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.SCUTTLE_STATUE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.BALLISTA_GOLEM_STATUE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.BALLISTA_GOLEM_STATUE_PART.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.MENDING_AURA.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.MENDING_AURA_STAIRS.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.MENDING_AURA_SLAB.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.MENDING_AURA_FENCE.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.MENDING_AURA_WALL.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.MENDING_AURA_PATH.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.MENDING_AURA_PANE.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.PLAYER_STATUE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.GAUNTLET.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.AZURO_HANGING_LEAVES.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.AZURO_HANGING_LEAVES_TIP.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.AZURO_OAK_DOOR.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.POTION_BARREL.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.DURITE_CLUSTER.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.LARGE_DURITE_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.MEDIUM_DURITE_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.SMALL_DURITE_BUD.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.MENDING_AURA_CHEST.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.DUNGEON_DIRECTOR.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.SPAWN_NODE.get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(DNLBlocks.DUNGEON_BANNER.get(), RenderType.cutout());
    }


    private void registerRenderers() {
        // Bosses
        EntityRendererRegistry.register(DNLEntityTypes.CHAOS_SPAWNER.get(), ChaosSpawnerRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FAIRKEEPER_BOROS.get(), FairkeeperBorosRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FAIRKEEPER_BOROS_PART.get(), FairkeeperBorosBodyRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FAIRKEEPER_OUROS.get(), FairkeeperOurosRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FAIRKEEPER_OUROS_PART.get(), FairkeeperOurosBodyRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FAIRKEEPER_SERPENT_CALLER.get(), FairkeeperSerpentCallerRenderer::new);

        // Monsters
        EntityRendererRegistry.register(DNLEntityTypes.HOLLOW.get(), HollowRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.SPAWNER_CARRIER.get(), SpawnerCarrierRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.SCUTTLE.get(), ScuttleRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.BALLISTA_GOLEM.get(), BallistaGolemRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.GARHOLD.get(), GarholdRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.BROKEN_GARHOLD.get(), BrokenGarholdRenderer::new);

        // Passive
        EntityRendererRegistry.register(DNLEntityTypes.SEALED_CHAOS.get(), SealedChaosRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.WHIMPER.get(), WhimperRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.COPPER_CREEP.get(), CopperCreepRenderer::new);


        // Projectiles
        EntityRendererRegistry.register(DNLEntityTypes.CHAOS_SPAWNER_PROJECTILE.get(), ChaosSpawnerProjectileRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.FLAME_PROJECTILE.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.VERTEX_ARROW_PROJECTILE.get(), VertexArrowProjectileRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.VERTEX_PILLAR_PROJECTILE.get(), VertexPillarProjectileRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.BALLISTA_ARROW.get(), BallistaArrowRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.VERTEX_ORB_PROJECTILE.get(), VertexOrbProjectileRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.VERTEX_DOMAIN_PROJECTILE.get(), VertexDomainProjectileRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.BORUS_ARROW.get(), BorusArrowRenderer::new);

        // Misc
        EntityRendererRegistry.register(DNLEntityTypes.SPECIAL_ITEM_ENTITY.get(), SpecialItemEntityRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.GREAT_EXPERIENCE_BOTTLE.get(), (context) -> new ThrownItemRenderer<>(context, 1.25F, false));
        EntityRendererRegistry.register(DNLEntityTypes.REPULSOR.get(), RepulsorRenderer::new);
        EntityRendererRegistry.register(DNLEntityTypes.SEEPING_SOUL.get(), SeepingSoulRenderer::new);
        // Render payloads as small tinted cubes instead of items
        EntityRendererRegistry.register(DNLEntityTypes.PAYLOAD.get(), PayloadEntityRenderer::new);


        // Block Entities
        BlockEntityRenderers.register(DNLBlockEntityTypes.FAIRKEEPER_CHEST.get(), FairkeeperChestBlockRenderer::new);
        BlockEntityRenderers.register(DNLBlockEntityTypes.DISABLED_FAIRKEEPER_CHEST.get(), DisabledFairkeeperChestBlockRenderer::new);
        BlockEntityRenderers.register(DNLBlockEntityTypes.PLAYER_STATUE.get(), PlayerStatueRenderer::new);
        BlockEntityRenderers.register(DNLBlockEntityTypes.GAUNTLET.get(), GauntletRenderer::new);
        BlockEntityRenderers.register(DNLBlockEntityTypes.GAUNTLET_VAULT.get(), GauntletVaultRenderer::new);
        BlockEntityRenderers.register(DNLBlockEntityTypes.DUNGEON_DIRECTOR.get(), DungeonDirectorRenderer::new);
        BlockEntityRenderers.register(DNLBlockEntityTypes.DUNGEON_BANNER.get(), DungeonBannerBlockRenderer::new);

        // Item Properties
        ItemProperties.register(DNLItems.VERTEX_BOW.get(), new ResourceLocation("pull"), (stack, level, entity, idk) -> {
            if (entity == null) return 0.0F;
            else
                return entity.getUseItem() != stack ? 0.0F : (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 30.0F;
        });

        ItemProperties.register(DNLItems.VERTEX_BOW.get(), new ResourceLocation("pulling"), (stack, level, entity, idk) ->
                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

        ItemProperties.register(DNLItems.COPPER_DETONATOR.get(), new ResourceLocation("mode_switch"), (stack, level, entity, idk) -> {
            if (entity == null || entity.getUseItem() != stack) return 0.0F;

            int useTime = stack.getUseDuration() - entity.getUseItemRemainingTicks();
            return useTime > CopperDetonatorItem.MODE_SWITCH_TIMING ? 1.0F : 0.0F;
        });

        ItemProperties.register(DNLItems.REPULSOR.get(), new ResourceLocation("golden_mode"),
                (stack, level, entity, seed) -> RepulsorItem.isGoldenMode(stack) ? 1.0F : 0.0F);
    }

    private void registerModelLayers() {
        // Bosses
        EntityModelLayerRegistry.registerModelLayer(ChaosSpawnerModel.LAYER_LOCATION, ChaosSpawnerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(FairkeeperBorosModel.LAYER_LOCATION, FairkeeperBorosModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(FairkeeperBorosBodyModel.LAYER_LOCATION, FairkeeperBorosBodyModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(FairkeeperOurosModel.LAYER_LOCATION, FairkeeperOurosModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(FairkeeperOurosBodyModel.LAYER_LOCATION, FairkeeperOurosBodyModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(FairkeeperSerpentCallerModel.LAYER_LOCATION, FairkeeperSerpentCallerModel::createBodyLayer);

        // Monsters
        EntityModelLayerRegistry.registerModelLayer(HollowModel.LAYER_LOCATION, HollowModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SpawnerCarrierModel.LAYER_LOCATION, SpawnerCarrierModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(ScuttleModel.LAYER_LOCATION, ScuttleModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(BallistaGolemModel.LAYER_LOCATION, BallistaGolemModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(GarholdModel.LAYER_LOCATION, GarholdModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(BrokenGarholdModel.LAYER_LOCATION, BrokenGarholdModel::createBodyLayer);

        // Passive
        EntityModelLayerRegistry.registerModelLayer(SealedChaosModel.LAYER_LOCATION, SealedChaosModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(WhimperModel.LAYER_LOCATION, WhimperModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CopperCreepModel.LAYER_LOCATION, CopperCreepModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(CopperCreepButlerModel.LAYER_LOCATION, CopperCreepButlerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(RepulsorModel.LAYER_LOCATION, RepulsorModel::createBodyLayer);


        //Projectiles
        EntityModelLayerRegistry.registerModelLayer(ChaosSpawnerProjectileModel.LAYER_LOCATION, ChaosSpawnerProjectileModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(VertexArrowProjectileModel.LAYER_LOCATION, VertexArrowProjectileModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(VertexPillarProjectileModel.LAYER_LOCATION, VertexPillarProjectileModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(BallistaArrowModel.LAYER_LOCATION, BallistaArrowModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(VertexOrbProjectileModel.LAYER_LOCATION, VertexOrbProjectileModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(VertexDomainProjectileModel.LAYER_LOCATION, VertexDomainProjectileModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(BorusArrowModel.LAYER_LOCATION, BorusArrowModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(Caltrop.LAYER_LOCATION, Caltrop::createBodyLayer);

        // Block Entities
        EntityModelLayerRegistry.registerModelLayer(FairkeeperChestModel.LAYER_LOCATION, FairkeeperChestModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DisabledFairkeeperChestModel.LAYER_LOCATION, DisabledFairkeeperChestModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(PlayerStatueModel.LAYER_LOCATION, PlayerStatueModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(PlayerStatuePedestalModel.LAYER_LOCATION, PlayerStatuePedestalModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(GauntletModel.LAYER_LOCATION, GauntletModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(GauntletVaultModel.LAYER_LOCATION, GauntletVaultModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(DungeonBannerBlockModel.LAYER_LOCATION, DungeonBannerBlockModel::createBodyLayer);

        // Misc
        EntityModelLayerRegistry.registerModelLayer(SeepingSoulChaosSpawnerModel.LAYER_LOCATION, SeepingSoulChaosSpawnerModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(SeepingSoulSerpentCallerModel.LAYER_LOCATION, SeepingSoulSerpentCallerModel::createBodyLayer);
    }

    private static void registerParticleFactories() {
        ParticleFactoryRegistry registry = ParticleFactoryRegistry.getInstance();
        registry.register(DNLParticleTypes.LARGE_FLAME_PARTICLE.get(), LargeFlameParticle.Factory::new);
        registry.register(DNLParticleTypes.LARGE_SOUL_FLAME_PARTICLE.get(), LargeSoulFlameParticle.Factory::new);
        registry.register(DNLParticleTypes.SCORCHER_FLAME_PARTICLE.get(), ScorcherFlameParticle.Factory::new);
        registry.register(DNLParticleTypes.VERTEX_SPARK_PARTICLE.get(), VertexSparkParticle.Factory::new);
        registry.register(DNLParticleTypes.FAIRKEEPER_BOUNDARY_PARTICLE.get(), FairkeeperBoundaryParticle.Factory::new);
        registry.register(DNLParticleTypes.VERTEX_BOUNDARY_PARTICLE.get(), VertexBoundaryParticle.Factory::new);
        registry.register(DNLParticleTypes.REDSTONE_SHOCKWAVE_PARTICLE.get(), RedstoneShockwaveParticle.Factory::new);
        registry.register(DNLParticleTypes.REDSTONE_HAZARD_INDICATOR_PARTICLE.get(), RedstoneHazardIndicatorParticle.Factory::new);
        registry.register(DNLParticleTypes.WHITE_SHOCKWAVE_PARTICLE.get(), WhiteShockwaveParticle.Factory::new);
        registry.register(DNLParticleTypes.WHITE_SHOCKWAVE_MEDIUM_PARTICLE.get(), WhiteShockwaveParticle.Factory::new);
        registry.register(DNLParticleTypes.ARROW_HAZARD_INDICATOR.get(), ArrowHazardIndicatorParticle.Factory::new);
        registry.register(DNLParticleTypes.MENDING_POP_AND_RUNE_PARTICLE.get(), MendingPopAndRuneParticle.Factory::new);
        registry.register(DNLParticleTypes.MENDING_RUNE_PARTICLE.get(), MendingRuneParticle.Factory::new);
        registry.register(DNLParticleTypes.MENDING_RUNE_SHORT_PARTICLE.get(), MendingRuneShortParticle.Factory::new);
        registry.register(DNLParticleTypes.MENDING_FADE_PARTICLE.get(), MendingFadeParticle.Factory::new);
        registry.register(DNLParticleTypes.MENDING_POP_PARTICLE.get(), MendingPopParticle.Factory::new);
    }

    private static void addDnlEnchantmentDescriptions(ItemStack stack, List<Component> lines) {
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            Enchantment ench = entry.getKey();
            var id = BuiltInRegistries.ENCHANTMENT.getKey(ench);
            if (id != null && DungeonNowLoading.MOD_ID.equals(id.getNamespace())) {
                String key = "enchantment." + id.getNamespace() + "." + id.getPath() + ".desc";
                lines.add(Component.translatable(key).withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

}
