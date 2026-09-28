package pl.mikomak.images.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import pl.mikomak.images.image.DisplayedImage;

import java.util.Map;

public class VisibilityTask extends BukkitRunnable {

    private final Map<String, DisplayedImage> activeImages;
    private final int showDistance;
    private final int hideDistance;

    public VisibilityTask(Map<String, DisplayedImage> activeImages, int showDistance, int hideDistance) {
        this.activeImages = activeImages;
        this.showDistance = showDistance;
        this.hideDistance = hideDistance;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            for (DisplayedImage image : this.activeImages.values()) {
                if (!player.getWorld().equals(image.getPlacement().getWorld())) {
                    continue;
                }
                image.refresh(player, this.showDistance, this.hideDistance);
            }
        }
    }
}
