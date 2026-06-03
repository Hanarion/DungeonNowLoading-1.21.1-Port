package dev.hexnowloading.dungeonnowloading.item;

import dev.hexnowloading.dungeonnowloading.entity.monster.WispwardLanternCartEntity;
import dev.hexnowloading.dungeonnowloading.registry.DNLEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
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

public class WispwardLanternCartItem extends Item {
    private final boolean timed;
    private final DispenseItemBehavior dispenseBehavior = new DefaultDispenseItemBehavior() {
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
                return this.fallback.dispense(source, stack);
            }

            EntityType<WispwardLanternCartEntity> type = DNLEntityTypes.WISPWARD_LANTERN_CART.get();
            WispwardLanternCartEntity cart = type.create(level);
            if (cart == null) {
                return this.fallback.dispense(source, stack);
            }

            cart.setPos(x, y + yOffset, z);
            cart.setTimed(WispwardLanternCartItem.this.timed);

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

    public WispwardLanternCartItem(Properties properties) {
        this(properties, false);
    }

    public WispwardLanternCartItem(Properties properties, boolean timed) {
        super(properties);
        this.timed = timed;
        DispenserBlock.registerBehavior(this, this.dispenseBehavior);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!state.is(BlockTags.RAILS)) {
            return InteractionResult.FAIL;
        }

        ItemStack stack = context.getItemInHand();
        if (!level.isClientSide) {
            RailShape shape = state.getBlock() instanceof BaseRailBlock
                    ? state.getValue(((BaseRailBlock) state.getBlock()).getShapeProperty())
                    : RailShape.NORTH_SOUTH;

            double extraY = shape.isAscending() ? 0.5D : 0.0D;
            WispwardLanternCartEntity cart = DNLEntityTypes.WISPWARD_LANTERN_CART.get().create(level);
            if (cart != null) {
                cart.setPos(pos.getX() + 0.5D, pos.getY() + 0.0625D + extraY, pos.getZ() + 0.5D);
                cart.setTimed(this.timed);

                if (stack.hasCustomHoverName()) {
                    cart.setCustomName(stack.getHoverName());
                }

                level.addFreshEntity(cart);
                level.gameEvent(GameEvent.ENTITY_PLACE, pos,
                        GameEvent.Context.of(context.getPlayer(), level.getBlockState(pos.below())));
            }
        }

        stack.shrink(1);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
