package mc.nolavacast.mixin;

import it.unimi.dsi.fastutil.longs.Long2ShortLinkedOpenHashMap;
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

import java.util.HashMap;

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
    
            Long2ShortLinkedOpenHashMap chunk2CountMapAccess = NoLavaCast.chunk2CountMap;
            if (NoLavaCast.config.ignoreGenInSameSpot) {
                // Add new key if not present
                HashMap<Long, LongArrayList> alreadySeenBlocksPerChunkAccess = NoLavaCast.alreadySeenBlocksPerChunk;
                if (!alreadySeenBlocksPerChunkAccess.containsKey(chunkPos)) {
                    alreadySeenBlocksPerChunkAccess.put(chunkPos, new LongArrayList());
                }
                
                // Doesn't contain it, so add the pos and increment
                if (!alreadySeenBlocksPerChunkAccess.get(chunkPos).contains(pos.asLong())) {
                    alreadySeenBlocksPerChunkAccess.get(chunkPos).add(pos.asLong());
                    chunk2CountMapAccess.addTo(chunkPos, (short)1);
                }
            } else {
                chunk2CountMapAccess.addTo(chunkPos, (short)1);
            }
    
            if (chunk2CountMapAccess.get(chunkPos) >= NoLavaCast.config.maxThreshold) {
                cir.setReturnValue(true);
            }
        }
    }
}
