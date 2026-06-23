package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.entity.monster.MimicartEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.gameevent.GameEvent;

public class MimicartItem extends Item {

    // Custom dispenser behavior – based on MinecartItem, but spawns MimicartEntity
    private static final DispenseItemBehavior DISPENSE_BEHAVIOR = new DefaultDispenseItemBehavior() {
        private final DefaultDispenseItemBehavior fallback = new DefaultDispenseItemBehavior();

        @Override
        public ItemStack execute(BlockSource source, ItemStack stack) {
            Direction facing = source.getBlockState().getValue(DispenserBlock.FACING);
            ServerLevel level = source.getLevel();

            double x = source.x() + (double) facing.getStepX() * 1.125D;
            double y = Math.floor(source.y()) + (double) facing.getStepY();
            double z = source.z() + (double) facing.getStepZ() * 1.125D;

            BlockPos railPos = source.getPos().relative(facing);
            BlockState railState = level.getBlockState(railPos);

            double yOffset;
            if (railState.is(BlockTags.RAILS)) {
                RailShape shape = railState.getBlock() instanceof BaseRailBlock
                        ? railState.getValue(((BaseRailBlock) railState.getBlock()).getShapeProperty())
                        : RailShape.NORTH_SOUTH;
                yOffset = shape.isAscending() ? 0.6D : 0.1D;
            } else if (railState.isAir() && level.getBlockState(railPos.below()).is(BlockTags.RAILS)) {
                BlockState belowState = level.getBlockState(railPos.below());
                RailShape shape = belowState.getBlock() instanceof BaseRailBlock
                        ? belowState.getValue(((BaseRailBlock) belowState.getBlock()).getShapeProperty())
                        : RailShape.NORTH_SOUTH;

                if (facing == Direction.DOWN || !shape.isAscending()) {
                    yOffset = -0.9D;
                } else {
                    yOffset = -0.4D;
                }
            } else {
                // no rail in front -> use vanilla fallback (drop item)
                return this.fallback.dispense(source, stack);
            }

            // spawn Mimicart instead of AbstractMinecart.createMinecart(...)
            EntityType<MimicartEntity> type = DNLEntityTypes.MIMICART.get();
            MimicartEntity cart = type.create(level);
            if (cart == null) {
                return this.fallback.dispense(source, stack);
            }

            cart.setPos(x, y + yOffset, z);

            if (stack.hasCustomHoverName()) {
                cart.setCustomName(stack.getHoverName());
            }

            level.addFreshEntity(cart);
            stack.shrink(1);
            return stack;
        }

        @Override
        protected void playSound(BlockSource source) {
            source.getLevel().levelEvent(1000, source.getPos(), 0);
        }
    };

    public MimicartItem(Properties properties) {
        super(properties);
        // Register custom dispenser behavior for THIS item
        DispenserBlock.registerBehavior(this, DISPENSE_BEHAVIOR);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = ctx.getItemInHand();
        if (!level.isClientSide) {
            RailShape shape = state.getBlock() instanceof BaseRailBlock
                    ? state.getValue(((BaseRailBlock) state.getBlock()).getShapeProperty())
                    : RailShape.NORTH_SOUTH;

            double extraY = shape.isAscending() ? 0.5D : 0.0D;

            double x = pos.getX() + 0.5D;
            double y = pos.getY() + 0.0625D + extraY;
            double z = pos.getZ() + 0.5D;

            EntityType<MimicartEntity> type = DNLEntityTypes.MIMICART.get();
            MimicartEntity cart = type.create(level);
            if (cart != null) {
                cart.setPos(x, y, z);

                if (stack.hasCustomHoverName()) {
                    cart.setCustomName(stack.getHoverName());
                }

                level.addFreshEntity(cart);
                level.gameEvent(GameEvent.ENTITY_PLACE, pos,
                        GameEvent.Context.of(ctx.getPlayer(), level.getBlockState(pos.below())));
            }
        }

        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
