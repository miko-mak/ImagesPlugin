package pl.mikomak.images.listener;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import pl.mikomak.images.ImagesPlugin;
import pl.mikomak.images.image.DisplayedImage;

public class PlayerWorldChangeListener implements Listener {

    private final ImagesPlugin plugin;

    public PlayerWorldChangeListener(ImagesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World world = event.getFrom();

        for (DisplayedImage image : this.plugin.getActiveImages().values()) {
            if (image.getPlacement().getWorld().equals(world)) {
                image.hideFromPlayer(player);
            }
        }
    }
}
