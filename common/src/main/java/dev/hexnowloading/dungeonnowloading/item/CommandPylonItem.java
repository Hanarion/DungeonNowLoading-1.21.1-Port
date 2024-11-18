package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.entity.misc.CommandPylonEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class CommandPylonItem extends Item{
    public CommandPylonItem(Item.Properties properties) {
        super(properties);
    }

//    @Override
//    public InteractionResult useOn(UseOnContext context) {
//        Level level = context.getLevel();
//        BlockPos blockPos = context.getClickedPos();
//        Direction targetFace = context.getClickedFace();
//
//        Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
//        AABB aABB = EntityType.ARMOR_STAND.getDimensions().makeBoundingBox(vec3.x(), vec3.y(), vec3.z());
//        if (level.noCollision((Entity)null, aABB) && level.getEntities((Entity)null, aABB).isEmpty()) {
//            if (level instanceof ServerLevel) {
//                CommandPylonEntity pylonEntity = DNLEntityTypes.COMMAND_PYLON.get().create(level);
//                if (pylonEntity == null) return InteractionResult.FAIL;
//
//                pylonEntity.moveTo(blockPos.relative(targetFace), 0.0f, 0.0f);
//                level.addFreshEntity(pylonEntity);
//
//                return InteractionResult.SUCCESS;
//            }
//        }
//        return InteractionResult.FAIL;
//    }

    public InteractionResult useOn(UseOnContext useOnContext) {
        Direction direction = useOnContext.getClickedFace();
        if (direction == Direction.DOWN) {
            return InteractionResult.FAIL;
        } else {
            Level level = useOnContext.getLevel();
            BlockPlaceContext blockPlaceContext = new BlockPlaceContext(useOnContext);
            BlockPos blockPos = blockPlaceContext.getClickedPos();
            ItemStack itemStack = useOnContext.getItemInHand();
            Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
            AABB aABB = DNLEntityTypes.COMMAND_PYLON.get().getDimensions().makeBoundingBox(vec3.x(), vec3.y(), vec3.z());
            if (level.noCollision((Entity)null, aABB) && level.getEntities((Entity)null, aABB).isEmpty()) {
                if (level instanceof ServerLevel) {
                    ServerLevel serverLevel = (ServerLevel)level;
                    Consumer<CommandPylonEntity> consumer = EntityType.createDefaultStackConfig(serverLevel, itemStack, useOnContext.getPlayer());
                    CommandPylonEntity pylonEntity = (CommandPylonEntity)DNLEntityTypes.COMMAND_PYLON.get().create(serverLevel, itemStack.getTag(), consumer, blockPos, MobSpawnType.SPAWN_EGG, true, true);
                    if (pylonEntity == null) {
                        return InteractionResult.FAIL;
                    }

//                    float f = (float) Mth.floor((Mth.wrapDegrees(useOnContext.getRotation() - 180.0F) + 22.5F) / 45.0F) * 45.0F;
                    pylonEntity.moveTo(pylonEntity.getX(), pylonEntity.getY(), pylonEntity.getZ(), 0.0F, 0.0F);
                    serverLevel.addFreshEntityWithPassengers(pylonEntity);
                    level.playSound((Player)null, pylonEntity.getX(), pylonEntity.getY(), pylonEntity.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
                    pylonEntity.gameEvent(GameEvent.ENTITY_PLACE, useOnContext.getPlayer());
                }

                itemStack.shrink(1);
                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.FAIL;
            }
        }
    }
}
