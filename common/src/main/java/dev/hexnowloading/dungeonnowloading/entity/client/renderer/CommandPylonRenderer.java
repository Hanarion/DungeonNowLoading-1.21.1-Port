package dev.hexnowloading.dungeonnowloading.entity.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.hexnowloading.dungeonnowloading.DungeonNowLoading;
import dev.hexnowloading.dungeonnowloading.entity.client.model.CommandPylonModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.CopperCreepModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.SealedChaosModel;
import dev.hexnowloading.dungeonnowloading.entity.client.model.ShieldingStonePillarProjectileModel;
import dev.hexnowloading.dungeonnowloading.entity.misc.CommandPylonEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.CopperCreepEntity;
import dev.hexnowloading.dungeonnowloading.entity.passive.SealedChaosEntity;
import dev.hexnowloading.dungeonnowloading.entity.projectile.VertexArrowProjectileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
//import net.minecraft.client.renderer.entity.layers.CopperCreepPowerLayer;
import net.minecraft.client.renderer.entity.layers.CreeperPowerLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.level.block.Blocks;

public class CommandPylonRenderer<T extends CommandPylonEntity> extends EntityRenderer<CommandPylonEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DungeonNowLoading.MOD_ID, "textures/entity/command_pylon.png");
    private CommandPylonModel model;

    public CommandPylonRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        model = new CommandPylonModel(ctx.bakeLayer(CommandPylonModel.LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(CommandPylonEntity entity) {
        return TEXTURE;
    }
}
