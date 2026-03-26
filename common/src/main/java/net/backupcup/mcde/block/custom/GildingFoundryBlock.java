package net.backupcup.mcde.block.custom;

import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.backupcup.mcde.block.entity.GildingFoundryBlockEntity;
import net.backupcup.mcde.block.entity.ModBlockEntities;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.util.Hand;
import net.minecraft.util.ActionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockPlaceContext;
import net.minecraft.world.BlockGetter;
import net.minecraft.world.World;
import net.minecraft.block.BaseEntityBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Mirror;
import net.minecraft.block.RenderShape;
import net.minecraft.block.Rotation;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.BlockState;
import net.minecraft.block.StateDefinition;
import net.minecraft.block.properties.BlockStateProperties;
import net.minecraft.block.properties.DirectionProperty;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.block.ShapeContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GildingFoundryBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public GildingFoundryBlock(Properties settings) {
        super(settings);
    }

    private static final VoxelShape SHAPE_N = Stream.of(
            Block.box(0, 0, 2, 16, 9, 16),
            Block.box(3, 9, 10, 13, 16, 15),
            Block.box(2, 0, 0, 14, 2, 2)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    private static final VoxelShape SHAPE_E = Stream.of(
            Block.box(0, 0, 0, 14, 9, 16),
            Block.box(1, 9, 3, 6, 16, 13),
            Block.box(14, 0, 2, 16, 2, 14)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    private static final VoxelShape SHAPE_S = Stream.of(
            Block.box(0, 0, 0, 16, 9, 14),
            Block.box(3, 9, 1, 13, 16, 6),
            Block.box(2, 0, 14, 14, 2, 16)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    private static final VoxelShape SHAPE_W = Stream.of(
            Block.box(2, 0, 0, 16, 9, 16),
            Block.box(10, 9, 3, 15, 16, 13),
            Block.box(0, 0, 2, 2, 2, 14)
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
    public void onRemove(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof GildingFoundryBlockEntity) {
                Containers.dropContents(world, pos, (GildingFoundryBlockEntity)blockEntity);
                world.updateNeighbourForOutputSignal(pos,this);
            }
        }
        super.onRemove(state, world, pos, newState, moved);
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GildingFoundryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.GILDING_FOUNDRY, GildingFoundryBlockEntity::tick);
    }
}
