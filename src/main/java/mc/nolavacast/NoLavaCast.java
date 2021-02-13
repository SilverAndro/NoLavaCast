package mc.nolavacast;

import it.unimi.dsi.fastutil.longs.*;
import mc.microconfig.MicroConfig;
import mc.nolavacast.mixin.LoadedChunksAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.HashMap;

public class NoLavaCast implements ModInitializer {
    public static final LongArraySet isLoadedChunks = new LongArraySet();
    public static final Long2ShortLinkedOpenHashMap chunk2CountMap = new Long2ShortLinkedOpenHashMap();
    public static final HashMap<Long, LongArrayList> alreadySeenBlocksPerChunk = new HashMap<>();
    private int tickCount = 0;
    
    public static final NoLavaCastConfig config = MicroConfig.getOrCreate("no_lava_cast", new NoLavaCastConfig());
    
    @Override
    public void onInitialize() {
        System.out.println("Lavacasting is gone!");
        
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            tickCount++;
            
            if (tickCount % 35 == 0) {
                LongSet loaded = ((LoadedChunksAccessor)world.getChunkManager().threadedAnvilChunkStorage).getLoadedChunks();
                LongIterator iter = loaded.iterator();
                while (iter.hasNext()) {
                    long next = iter.nextLong();
                    ChunkPos pos = new ChunkPos(next);
                    Chunk chunk = world.getChunkManager().getChunk(pos.x, pos.z, ChunkStatus.FULL, false);
                    
                    long posLong = pos.toLong();
                    
                    if (chunk != null) {
                        short amount = chunk2CountMap.get(posLong);
                        if (amount > 0) {
                            amount /= 2;
                            amount -= 2;
                            if (amount <= 0) {
                                amount = 0;
                            } else {
                                isLoadedChunks.add(posLong);
                            }
                            
                            if (amount != 0) {
                                chunk2CountMap.put(posLong, amount);
                            }
                        }
                    }
                }
                
                for (long chunkPos : chunk2CountMap.keySet()) {
                    if (!isLoadedChunks.contains(chunkPos)) {
                        chunk2CountMap.remove(chunkPos);
                    }
                }
                isLoadedChunks.clear();
            }
            
            if (tickCount % 3000 == 0) {
                chunk2CountMap.clear();
                alreadySeenBlocksPerChunk.clear();
                tickCount = 0;
            }
        });
    }
}
