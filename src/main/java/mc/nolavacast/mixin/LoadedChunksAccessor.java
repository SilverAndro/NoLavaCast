package mc.nolavacast.mixin;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ThreadedAnvilChunkStorage.class)
public interface LoadedChunksAccessor {
    @Accessor
    LongSet getLoadedChunks();
}
