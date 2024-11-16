package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.entity.misc.CommandPylonEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class CommandPylonItem extends Item{
    public CommandPylonItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos targetPos = context.getClickedPos();
        Direction targetFace = context.getClickedFace();

        CommandPylonEntity pylonEntity = DNLEntityTypes.COMMAND_PYLON.get().create(level);
        if (pylonEntity == null) return InteractionResult.FAIL;

        pylonEntity.moveTo(targetPos.relative(targetFace), 0.0f, 0.0f);
        level.addFreshEntity(pylonEntity);

        return InteractionResult.SUCCESS;
    }

}
