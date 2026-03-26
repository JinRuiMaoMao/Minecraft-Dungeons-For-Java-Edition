package mcd_java.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class FadingObsidianBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public FadingObsidianBlock(Properties settings) {
        super(settings);
    }

    public static BlockState getMeltedState() {
        return Blocks.LAVA.defaultBlockState();
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (world.getBrightness(LightLayer.BLOCK, pos) > 11 - state.getLightBlock(world, pos)) {
            this.melt(world, pos);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if ((random.nextInt(3) == 0 || this.canMelt(world, pos, 4)) && world.getMaxLocalRawBrightness(pos) > 11 - state.getValue(AGE) - state.getLightBlock(world, pos) && this.increaseAge(state, world, pos)) {
            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            for (Direction direction : Direction.values()) {
                mutable.setWithOffset(pos, direction);
                BlockState blockState = world.getBlockState(mutable);
                if (!blockState.is(this) || this.increaseAge(blockState, world, mutable)) continue;
                world.scheduleTick(mutable, this, Mth.nextInt(random, 20, 40));
            }
            return;
        }
        world.scheduleTick(pos, this, Mth.nextInt(random, 20, 40));
    }

    private boolean increaseAge(BlockState state, Level world, BlockPos pos) {
        int i = state.getValue(AGE);
        if (i < 3) {
            world.setBlock(pos, state.setValue(AGE, i + 1), Block.UPDATE_CLIENTS);
            return false;
        }
        this.melt(world, pos);
        return true;
    }

    private boolean canMelt(BlockGetter world, BlockPos pos, int maxNeighbors) {
        int i = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutable.setWithOffset(pos, direction);
            if (!world.getBlockState(mutable).is(this) || ++i < maxNeighbors) continue;
            return false;
        }
        return true;
    }

    protected void melt(Level world, BlockPos pos) {
        world.setBlockAndUpdate(pos, getMeltedState());
        world.neighborChanged(pos, getMeltedState().getBlock(), pos);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

}
