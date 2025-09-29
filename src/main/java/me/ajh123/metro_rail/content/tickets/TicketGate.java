package me.ajh123.metro_rail.content.tickets;

import eu.pb4.polymer.core.api.block.PolymerBlock;
import me.ajh123.metro_rail.foundation.ModItems;
import me.ajh123.metro_rail.utils.TaskScheduler;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Colors;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class TicketGate extends FenceGateBlock implements PolymerBlock {
    public TicketGate(Settings settings) {
        super(WoodType.OAK, settings);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        boolean open = state.get(FenceGateBlock.OPEN);
        Direction facing = state.get(FenceGateBlock.FACING);
        return Blocks.OAK_FENCE_GATE.getDefaultState()
                .with(FenceGateBlock.OPEN, open)
                .with(FenceGateBlock.FACING, facing);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        // don't call super so we don't update when redstone changes and always set powered to false so redstone doesn't affect it
        world.setBlockState(pos, state.with(POWERED, false), Block.NOTIFY_LISTENERS);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        boolean hasTicket = player.getMainHandStack().getItem().equals(ModItems.TICKET);

        if (!hasTicket) {
            player.sendMessage(Text.translatable("info.metro_rail.ticket_gate.ticket_required").withColor(Colors.RED), true);
            return ActionResult.CONSUME;
        }

        // vanilla fence gate behavior now continues ...
        Direction direction = player.getHorizontalFacing();
        if (state.get(FACING) == direction.getOpposite()) {
            state = state.with(FACING, direction);
        }

        state = state.with(OPEN, true);
        world.setBlockState(pos, state, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);


        boolean isOpen = state.get(OPEN);
        world.playSound(
                player, pos, isOpen ? WoodType.OAK.fenceGateOpen() : WoodType.OAK.fenceGateClose(), SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F
        );
        world.emitGameEvent(player, isOpen ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);

        // Schedule the gate to close after 15 ticks (0.75 seconds) just enough time for one person to pass through
        TaskScheduler.runTaskLater(() -> {
            BlockState currentState = world.getBlockState(pos);
            if (!currentState.get(OPEN)) {
                return; // Don't close if it's already closed
            }
            BlockState newState = currentState.with(OPEN, false);
            world.setBlockState(pos, newState, Block.NOTIFY_LISTENERS | Block.REDRAW_ON_MAIN_THREAD);
            world.playSound(
                    null, pos, WoodType.OAK.fenceGateClose(), SoundCategory.BLOCKS, 1.0F, world.getRandom().nextFloat() * 0.1F + 0.9F
            );
            world.emitGameEvent(null, GameEvent.BLOCK_CLOSE, pos);
        }, 15);

        return ActionResult.SUCCESS;
    }
}
