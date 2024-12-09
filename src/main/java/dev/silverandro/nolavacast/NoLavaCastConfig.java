package dev.silverandro.nolavacast;

import dev.silverandro.microconfig.Comment;
import dev.silverandro.microconfig.ConfigData;

public class NoLavaCastConfig implements ConfigData {
    @Comment("Threshold of count at which point new cobble is disabled")
    public int maxThreshold = 10;
    
    @Comment("Disable this to reduce memory overhead and save some processing time\nWill break cobble generators completely and utterly\nBut also makes the system slightly more accurate")
    public boolean ignoreGenInSameSpot = true;
}
