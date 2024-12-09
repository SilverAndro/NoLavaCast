package dev.silverandro.nolavacast.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.longs.*;
import dev.silverandro.nolavacast.NoLavaCast;
import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public class TrackCreatedBlocks {
    @Inject(
            method = "receiveNeighborFluids",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", ordinal = 0),
            cancellable = true
    )
    public void trackAndCancelLavaToCobblestone(World world, BlockPos pos, BlockState state, CallbackInfoReturnable<Boolean> cir, @Local Block block) {
        if (block == Blocks.COBBLESTONE) {
            long chunkPos = world.getChunk(pos).getPos().toLong();
    
            Long2ShortAVLTreeMap chunk2CountMapAccess = NoLavaCast.chunk2CountMap;
            if (NoLavaCast.config.ignoreGenInSameSpot) {
                // Add new key if not present
                Long2ObjectAVLTreeMap<LongOpenHashSet> alreadySeenBlocksPerChunkAccess = NoLavaCast.alreadySeenBlocksPerChunk;
                if (!alreadySeenBlocksPerChunkAccess.containsKey(chunkPos)) {
                    alreadySeenBlocksPerChunkAccess.put(chunkPos, new LongOpenHashSet());
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
                world.setBlockState(pos, Blocks.STRUCTURE_VOID.getDefaultState());
                NoLavaCast.toRemove.add(pos.asLong());
                cir.setReturnValue(true);
            }
        }
    }
}
