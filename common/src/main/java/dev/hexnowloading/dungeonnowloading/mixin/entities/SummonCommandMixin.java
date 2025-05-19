package dev.hexnowloading.dungeonnowloading.mixin.entities;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.hexnowloading.dungeonnowloading.util.SummonFlag;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SummonCommand.class)
public class SummonCommandMixin {

    @Inject(
            method = "createEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;loadEntityRecursive(Lnet/minecraft/nbt/CompoundTag;Lnet/minecraft/world/level/Level;Ljava/util/function/Function;)Lnet/minecraft/world/entity/Entity;")
    )
    private static void markSummoningStart(
            CommandSourceStack source,
            Holder.Reference<EntityType<?>> entityRef,
            Vec3 pos,
            CompoundTag nbt,
            boolean callFinalize,
            CallbackInfoReturnable<Entity> cir
    ) throws CommandSyntaxException {
        SummonFlag.markSummoning();
    }

    @Inject(
            method = "createEntity",
            at = @At("RETURN")
    )
    private static void clearSummoningFlag(
            CommandSourceStack source,
            Holder.Reference<EntityType<?>> entityRef,
            Vec3 pos,
            CompoundTag nbt,
            boolean callFinalize,
            CallbackInfoReturnable<Entity> cir
    ) throws CommandSyntaxException {
        SummonFlag.clear();
    }
}
