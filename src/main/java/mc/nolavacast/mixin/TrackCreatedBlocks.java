package mc.nolavacast.mixin;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import mc.nolavacast.NoLavaCast;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FluidBlock.class)
public class TrackCreatedBlocks {
    @Inject(
        method = "receiveNeighborFluids",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", ordinal = 0),
        cancellable = true,
        locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void trackAndCancelLavaToCobblestone(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir, Block block) {
        if (block == Blocks.COBBLESTONE) {
            long chunkPos = world.getChunk(pos).getPos().toLong();

            if (NoLavaCast.config.ignoreGenInSameSpot) {
                // Add new key if not present
                if (!NoLavaCast.alreadySeenBlocksPerChunk.containsKey(chunkPos)) {
                    NoLavaCast.alreadySeenBlocksPerChunk.put(chunkPos, new LongArrayList());
                }
                
                // Doesn't contain it, so add the pos and increment
                if (!NoLavaCast.alreadySeenBlocksPerChunk.get(chunkPos).contains(pos.asLong())) {
                    NoLavaCast.alreadySeenBlocksPerChunk.get(chunkPos).add(pos.asLong());
                    NoLavaCast.chunk2CountMap.addTo(chunkPos, (short)1);
                }
            } else {
                NoLavaCast.chunk2CountMap.addTo(chunkPos, (short)1);
            }
    
            if (NoLavaCast.chunk2CountMap.get(chunkPos) >= NoLavaCast.config.maxThreshold) {
                cir.setReturnValue(true);
            }
        }
    }
}
