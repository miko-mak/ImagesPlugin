package pl.mikomak.images.config;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.HashMap;
import java.util.Map;

public class PluginConfig extends OkaeriConfig {

    public Map<String, ImagePlacement> placements = new HashMap<>();
    public int showDistance = 80;
    public int hideDistance = 128;

    public PluginConfig() {
        this.placements.put("golden",
                new ImagePlacement("golden.png",
                new Location(Bukkit.getWorld("world"), 0, 120, 0, 0, 0),
                new Location(Bukkit.getWorld("world"), 0, 120, 0, 0, 0),
                BlockFace.WEST));
    }

}
