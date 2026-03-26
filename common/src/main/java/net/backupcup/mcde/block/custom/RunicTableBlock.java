package net.backupcup.mcde.block.custom;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.backupcup.mcde.screen.handler.RunicTableScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ContainerLevelAccess;
import net.minecraft.item.BlockPlaceContext;
import net.minecraft.world.BlockGetter;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.Mirror;
import net.minecraft.block.RenderShape;
import net.minecraft.block.Rotation;
import net.minecraft.block.BlockState;
import net.minecraft.block.StateDefinition;
import net.minecraft.block.properties.BlockStateProperties;
import net.minecraft.block.properties.DirectionProperty;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RunicTableBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public RunicTableBlock(Properties settings) {
        super(settings);
    }

    private static final VoxelShape SHAPE_N = Stream.of(
            Block.box(2, 0, 2, 14, 9, 5),
            Block.box(2, 0, 5, 14, 10, 8),
            Block.box(2, 0, 8, 14, 11, 11),
            Block.box(2, 0, 11, 14, 12, 14)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    private static final VoxelShape SHAPE_E = Stream.of(
            Block.box(11, 0, 2, 14, 9, 14),
            Block.box(8, 0, 2, 11, 10, 14),
            Block.box(5, 0, 2, 8, 11, 14),
            Block.box(2, 0, 2, 5, 12, 14)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    private static final VoxelShape SHAPE_S = Stream.of(
            Block.box(2, 0, 11, 14, 9, 14),
            Block.box(2, 0, 8, 14, 10, 11),
            Block.box(2, 0, 5, 14, 11, 8),
            Block.box(2, 0, 2, 14, 12, 5)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    private static final VoxelShape SHAPE_W = Stream.of(
            Block.box(2, 0, 2, 5, 9, 14),
            Block.box(5, 0, 2, 8, 10, 14),
            Block.box(8, 0, 2, 11, 11, 14),
            Block.box(11, 0, 2, 14, 12, 14)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> SHAPE_N;
            case EAST -> SHAPE_E;
            case SOUTH -> SHAPE_S;
            case WEST -> SHAPE_W;
            default -> SHAPE_N;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx).setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /*FOR BLOCK ENTITY*/
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public ActionResult use(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            MenuProvider screenHandlerFactory = state.getMenuProvider(world, pos);

            if (screenHandlerFactory != null) {
                PlayerEntity.openMenu(screenHandlerFactory);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public MenuProvider getMenuProvider(BlockState state, World world, BlockPos pos) {
        return new SimpleMenuProvider(
            (syncId, inventory, PlayerEntity) -> new RunicTableScreenHandler(
                syncId,
                inventory,
                ContainerLevelAccess.create(world, pos)
            ),
            Text.translatable("block.mcde.runic_table")
        );
    }
}
