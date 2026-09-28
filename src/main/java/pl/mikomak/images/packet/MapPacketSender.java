package pl.mikomak.images.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.component.ComponentTypes;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMapData;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import pl.mikomak.images.image.DisplayedImage;
import pl.mikomak.images.image.MapSection;

import java.util.List;

public final class MapPacketSender {

    private MapPacketSender() {}

    public static void fillFrame(@NotNull Player player, @NotNull MapSection section, @NotNull BlockFace facing) {
        ItemStack mapItem = ItemStack.builder()
                .type(ItemTypes.FILLED_MAP)
                .amount(1)
                .component(ComponentTypes.MAP_ID, section.fakeMapId())
                .build();

        com.github.retrooper.packetevents.protocol.world.BlockFace direction =
                com.github.retrooper.packetevents.protocol.world.BlockFace.valueOf(facing.name());

        List<EntityData<?>> metadata = List.of(
                new EntityData(0, EntityDataTypes.BYTE, (byte) 0x20),
                new EntityData(8, EntityDataTypes.BLOCK_FACE, direction),
                new EntityData(9, EntityDataTypes.ITEMSTACK, mapItem),
                new EntityData(10, EntityDataTypes.INT, 0)
        );

        WrapperPlayServerEntityMetadata packet = new WrapperPlayServerEntityMetadata(section.fakeEntityId(), metadata);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public static void sendMapData(@NotNull Player player, @NotNull MapSection section) {
        WrapperPlayServerMapData packet = new WrapperPlayServerMapData(
                section.fakeMapId(),
                (byte) 0,
                false,
                true,
                null,
                128,
                128,
                0,
                0,
                section.pixelColors()
        );
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

    public static void despawn(@NotNull Player player, @NotNull DisplayedImage image) {
        int[] entityIds = image.getTiles().stream()
                .mapToInt(MapSection::fakeEntityId)
                .toArray();

        WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(entityIds);
        PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
    }

}