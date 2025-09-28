package me.ajh123.metro_rail.content.tickets;

import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DispenserBlock;
import net.minecraft.dialog.AfterAction;
import net.minecraft.dialog.DialogActionButtonData;
import net.minecraft.dialog.DialogButtonData;
import net.minecraft.dialog.DialogCommonData;
import net.minecraft.dialog.action.SimpleDialogAction;
import net.minecraft.dialog.type.MultiActionDialog;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.ajh123.metro_rail.MetroRail.MOD_ID;

public class TicketDispenser extends SimplePolymerBlock {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public TicketDispenser(Settings settings) {
        super(settings, Blocks.DISPENSER);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        var list = new ArrayList<DialogActionButtonData>();

        NbtCompound worldPos = new NbtCompound();
        worldPos.putInt("x", pos.getX());
        worldPos.putInt("y", pos.getY());
        worldPos.putInt("z", pos.getZ());

        for (int i = 0; i < 10; i++) {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("ticket_id", "entry_"+i);
            nbt.put("world_pos", worldPos);
            nbt.putString("player_uuid", player.getUuidAsString());

            list.add(new DialogActionButtonData(new DialogButtonData(Text.literal("Entry "+i), 150),
                    Optional.of(new SimpleDialogAction(new ClickEvent.Custom( // Send a custom packet event when clicked.
                            Identifier.of(MOD_ID, "get_ticket"),
                            Optional.of(nbt)
                    )))));
        }

        player.openDialog(RegistryEntry.of(new MultiActionDialog(new DialogCommonData(
                Text.translatable("gui.metro_rail.ticket_dispenser.title"),
                Optional.empty(),
                true, true,
                AfterAction.CLOSE,
                List.of(),
                List.of()
        ), list, Optional.of(new DialogActionButtonData(new DialogButtonData(ScreenTexts.DONE, 150), Optional.empty())), 1)));
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.DISPENSER.getDefaultState().with(DispenserBlock.FACING, state.get(FACING));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getHorizontalPlayerFacing().getOpposite();
        return this.getDefaultState().with(FACING, direction);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
