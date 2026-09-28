package pl.mikomak.images.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import pl.mikomak.images.ImagesPlugin;
import pl.mikomak.images.image.DisplayedImage;

public class PlayerQuitListener implements Listener {

    private final ImagesPlugin plugin;

    public PlayerQuitListener(ImagesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        for (DisplayedImage image : this.plugin.getActiveImages().values()) {
            image.getVisibleTo().remove(player.getUniqueId());
        }
    }

}
