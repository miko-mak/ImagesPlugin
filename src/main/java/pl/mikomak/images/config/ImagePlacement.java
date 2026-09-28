package pl.mikomak.images.config;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;

public class ImagePlacement extends OkaeriConfig {

    private String image;
    private Location pos1;
    private Location pos2;
    private BlockFace facing;

    public ImagePlacement() {}

    public ImagePlacement(
            String image,
            Location pos1,
            Location pos2,
            BlockFace facing
    ) {
        this.image = image;
        this.pos1 = pos1;
        this.pos2 = pos2;
        this.facing = facing;
    }

    public String getImage() {
        return this.image;
    }

    public Location getPos1() {
        return this.pos1;
    }

    public Location getPos2() {
        return this.pos2;
    }

    public BlockFace getFacing() {
        return this.facing;
    }

    public World getWorld() {
        return this.pos1.getWorld();
    }

}

