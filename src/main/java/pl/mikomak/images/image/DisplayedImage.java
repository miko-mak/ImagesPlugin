package pl.mikomak.images.image;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import pl.mikomak.images.config.ImagePlacement;
import pl.mikomak.images.packet.FakeIdRegistry;
import pl.mikomak.images.packet.MapPacketSender;

import java.awt.image.BufferedImage;
import java.util.*;

public class DisplayedImage {

    private final Set<UUID> visibleTo = new HashSet<>();
    private final List<MapSection> tiles;
    private final ImagePlacement placement;

    public DisplayedImage(
            ImagePlacement placement,
            BufferedImage loadedImage,
            FakeIdRegistry registry
    ) {
        this.placement = placement;

        GridSize gridSize = calculateGridSize();
        BufferedImage stretched = ImageProcessor.stretchToGrid(loadedImage, gridSize);
        BufferedImage[][] sliced = ImageProcessor.slice(stretched, gridSize);

        this.tiles = createTiles(gridSize, sliced, registry);
    }

    public List<MapSection> createTiles(
            GridSize gridSize,
            BufferedImage[][] sliced,
            FakeIdRegistry registry
    ) {
        List<MapSection> builtTiles = new ArrayList<>();

        for (int row = 0; row < gridSize.rows(); row++) {
            for (int col = 0; col < gridSize.columns(); col++) {
                Location loc = calculateTileLocation(row, col);
                byte[] colors = MapColorMapper.convert(sliced[row][col]);
                int entityId = registry.nextEntityId();
                int mapId = registry.nextMapId();
                builtTiles.add(new MapSection(entityId, mapId, loc, colors));
            }
        }
        return builtTiles;
    }

    private GridSize calculateGridSize() {
        BlockFace facing = this.placement.getFacing();
        Location firstCorner = this.placement.getPos1();
        Location secondCorner = this.placement.getPos2();

        int block1;
        int block2;

        if (facing == BlockFace.EAST || facing == BlockFace.WEST) {
            block1 = firstCorner.getBlockZ();
            block2 = secondCorner.getBlockZ();

        } else {
            block1 = firstCorner.getBlockX();
            block2 = secondCorner.getBlockX();
        }

        int columns = Math.abs(block1 - block2) + 1;
        int rows = Math.abs(this.placement.getPos1().getBlockY() - this.placement.getPos2().getBlockY()) + 1;
        return new GridSize(columns, rows);
    }

    private Location calculateTileLocation(int row, int col) {
        BlockFace facing = this.placement.getFacing();
        Location firstCorner = this.placement.getPos1();
        Location secondCorner = this.placement.getPos2();

        double flatValue;
        double freeValue;
        double height = Math.max(firstCorner.getBlockY(), secondCorner.getBlockY()) - row;

        if (facing == BlockFace.EAST || facing == BlockFace.WEST) {
            flatValue = firstCorner.getBlockX();
            freeValue = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ()) + col;
            return new Location(firstCorner.getWorld(), flatValue, height, freeValue);

        } else {
            flatValue = firstCorner.getBlockZ();
            freeValue = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX()) + col;
            return new Location(firstCorner.getWorld(), freeValue, height, flatValue);
        }
    }

    public void showToPlayer(Player player) {
        this.visibleTo.add(player.getUniqueId());

        for (MapSection tile : this.getTiles()) {
            UUID uuid = UUID.randomUUID();

            org.bukkit.Location bukkitLocation = tile.location();
            com.github.retrooper.packetevents.protocol.world.Location spawnLocation = SpigotConversionUtil.fromBukkitLocation(bukkitLocation);

            WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                    tile.fakeEntityId(), uuid, EntityTypes.ITEM_FRAME, spawnLocation,
                    spawnLocation.getYaw(), 0, null
            );

            PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnPacket);
            MapPacketSender.fillFrame(player, tile, this.getPlacement().getFacing());
            MapPacketSender.sendMapData(player, tile);
        }
    }

    public void hideFromPlayer(Player player) {
        this.visibleTo.remove(player.getUniqueId());
        MapPacketSender.despawn(player, this);
    }

    public void refresh(Player player, int showDistance, int hideDistance) {
        Location playerLocation = player.getLocation();
        Location firstCorner = this.placement.getPos1();
        Location secondCorner = this.placement.getPos2();
        BlockFace facing = this.placement.getFacing();

        double coordinateToCheck;
        double flatValue;
        double freeMin;
        double freeMax;

        if (facing == BlockFace.EAST || facing == BlockFace.WEST) {
            flatValue = firstCorner.getBlockX();
            freeMin = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());
            freeMax = Math.max(firstCorner.getBlockZ(), secondCorner.getBlockZ());
            coordinateToCheck = playerLocation.getBlockX();

        } else {
            flatValue = firstCorner.getBlockZ();
            freeMin = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
            freeMax = Math.max(firstCorner.getBlockX(), secondCorner.getBlockX());
            coordinateToCheck = playerLocation.getBlockZ();
        }

        double yMin = Math.min(firstCorner.getBlockY(), secondCorner.getBlockY());
        double yMax = Math.max(firstCorner.getBlockY(), secondCorner.getBlockY());

        double clampFree = Math.clamp(coordinateToCheck, freeMin, freeMax);
        double clampY = Math.clamp(playerLocation.getBlockY(), yMin, yMax);

        double nearestX = (facing == BlockFace.EAST || facing == BlockFace.WEST) ? flatValue : clampFree;
        double nearestZ = (facing == BlockFace.EAST || facing == BlockFace.WEST) ? clampFree : flatValue;

        double dx = playerLocation.getX() - nearestX;
        double dy = playerLocation.getY() - clampY;
        double dz = playerLocation.getZ() - nearestZ;
        double distanceSquared = dx * dx + dy * dy + dz * dz;

        boolean alreadyVisible = this.visibleTo.contains(player.getUniqueId());

        if (!alreadyVisible && distanceSquared <= (double) showDistance * showDistance) {
            this.showToPlayer(player);
        } else if (alreadyVisible && distanceSquared > (double) hideDistance * hideDistance) {
            this.hideFromPlayer(player);
        }
    }

    public Set<UUID> getVisibleTo() {
        return this.visibleTo;
    }

    public List<MapSection> getTiles() {
        return this.tiles;
    }

    public ImagePlacement getPlacement() {
        return this.placement;
    }

}