package dev.hexnowloading.dungeonnowloading.block.client.renderer;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.hexnowloading.dungeonnowloading.block.client.StatueSkinCache;
import dev.hexnowloading.dungeonnowloading.block.client.model.PlayerStatueModel;
import dev.hexnowloading.dungeonnowloading.block.entity.PlayerStatueBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;

public class PlayerStatueRenderer implements BlockEntityRenderer<PlayerStatueBlockEntity> {
    private final PlayerStatueModel model;


    public PlayerStatueRenderer(BlockEntityRendererProvider.Context ctx) {
        this.model = new PlayerStatueModel(ctx.bakeLayer(PlayerStatueModel.LAYER_LOCATION));
    }

    @Override
    public void render(PlayerStatueBlockEntity be, float partial, PoseStack pose, MultiBufferSource buf, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 0.0, 0.5);
        pose.scale(-1f, -1f, 1f);

        GameProfile gp = be.getOwner();
        boolean slim = isSlim(gp);
        model.useSlimArms(slim);

        ResourceLocation tex = (gp != null)
                ? StatueSkinCache.get(gp)
                : DefaultPlayerSkin.getDefaultSkin(UUIDUtil.getOrCreatePlayerUUID(new com.mojang.authlib.GameProfile(null, "default")));

        var vc = buf.getBuffer(RenderType.entityTranslucent(tex));
        model.renderToBuffer(pose, vc, light, overlay, 1f, 1f, 1f, 1f);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(PlayerStatueBlockEntity be) { return true; }

    private static boolean isSlim(GameProfile gp) {
        if (gp == null) return false;
        try {
            var sm  = Minecraft.getInstance().getSkinManager();
            var map = sm.getInsecureSkinInformation(gp);
            var tex = map.get(MinecraftProfileTexture.Type.SKIN);
            if (tex != null) {
                String model = tex.getMetadata("model"); // "slim" or null
                if ("slim".equalsIgnoreCase(model)) return true;
                if (model != null) return false;
            }
        } catch (Exception ignored) {}
        var id = gp.getId();
        if (id == null) id = UUIDUtil.getOrCreatePlayerUUID(gp);
        return "slim".equals(DefaultPlayerSkin.getSkinModelName(id));
    }
}
