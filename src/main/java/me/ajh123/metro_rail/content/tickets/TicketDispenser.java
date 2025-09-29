package me.ajh123.metro_rail.content.tickets;

import eu.pb4.polymer.core.api.block.SimplePolymerBlock;
import me.ajh123.metro_rail.networking.GetTicketPayload;
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
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TicketDispenser extends SimplePolymerBlock {
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;

    public TicketDispenser(Settings settings) {
        super(settings, Blocks.DISPENSER);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        openTicketsDialog(world, pos, player);
        return ActionResult.SUCCESS;
    }

    public static void openTicketsDialog(World world, BlockPos pos, PlayerEntity player) {
        ServerWorld serverWorld = (ServerWorld) world;
        KeyPair pair = serverWorld.getServer().getKeyPair();

        var list = new ArrayList<DialogActionButtonData>();
        var tickets = getTicketsForDispenser(world, pos);

        for (Ticket ticket : tickets) {
            GetTicketPayload payload = new GetTicketPayload(pos, ticket.id(), player.getUuid());
            NbtElement payloadNBT = payload.writeSigned(pair);

            list.add(new DialogActionButtonData(new DialogButtonData(ticket.displayPrice(), 250),
                    Optional.of(new SimpleDialogAction(new ClickEvent.Custom( // Send a custom packet event when clicked.
                            GetTicketPayload.GET_TICKET_PAYLOAD_IDENTIFIER,
                            Optional.of(payloadNBT)
                    )))));
        }

        player.openDialog(RegistryEntry.of(new MultiActionDialog(new DialogCommonData(
                Text.translatable("gui.metro_rail.ticket_dispenser.title"),
                Optional.empty(),
                true, true,
                AfterAction.CLOSE,
                List.of(),
                List.of()
        ), list, Optional.of(new DialogActionButtonData(new DialogButtonData(ScreenTexts.DONE, 250), Optional.empty())), 1)));
    }

    public static List<Ticket> getTicketsForDispenser(World world, BlockPos pos) {
        //TODO: Make this configurable via block entity data
        ItemStack EMERALD5 = new ItemStack(Items.EMERALD, 5);
        ItemStack EMERALD20 = new ItemStack(Items.EMERALD, 20);

        List<Ticket> tickets = new ArrayList<>();
        tickets.add(new Ticket(0, Text.literal("Single Ride"), Ticket.of(ItemStack.EMPTY)));
        tickets.add(new Ticket(1, Text.literal("Day Pass"), Ticket.of(EMERALD5)));
        tickets.add(new Ticket(2, Text.literal("Weekly Pass"), Ticket.of(EMERALD20)));
        return tickets;
    }

    public static Ticket getTicketById(World world, BlockPos pos, int id) {
        return getTicketsForDispenser(world, pos).stream().filter(ticket -> ticket.id() == id).findFirst().orElse(null);
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
