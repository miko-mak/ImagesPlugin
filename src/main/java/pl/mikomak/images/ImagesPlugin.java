package pl.mikomak.images;

import com.github.retrooper.packetevents.PacketEvents;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;
import pl.mikomak.images.config.ImagePlacement;
import pl.mikomak.images.config.PluginConfig;
import pl.mikomak.images.image.DisplayedImage;
import pl.mikomak.images.listener.PlayerQuitListener;
import pl.mikomak.images.listener.PlayerWorldChangeListener;
import pl.mikomak.images.packet.FakeIdRegistry;
import pl.mikomak.images.scheduler.VisibilityTask;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class ImagesPlugin extends JavaPlugin {

    private final FakeIdRegistry fakeIdRegistry = new FakeIdRegistry();
    private final Map<String, DisplayedImage> activeImages = new HashMap<>();

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        if (!getServer().getPluginManager().isPluginEnabled("packetevents")) {
            this.getLogger().info("PacketEvents not found. Disabling...");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        PacketEvents.getAPI().init();

        PluginConfig config = ConfigManager.create(PluginConfig.class, (it) -> {
            it.withConfigurer(new YamlBukkitConfigurer(), new SerdesBukkit());
            it.withBindFile(this.getDataFolder() + "/config.yml");
            it.saveDefaults();
            it.load(true);
        });

        File imagesDirectory = new File(this.getDataFolder(), "images");
        if (!imagesDirectory.exists()) {
            this.getLogger().info("Creating images directory...");
            imagesDirectory.mkdirs();
        }

        Map<String, BufferedImage> loadedImages = getLoadedImages(imagesDirectory, config);
        this.buildImages(config, loadedImages);

        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerWorldChangeListener(this), this);
    }

    private void buildImages(PluginConfig config, Map<String, BufferedImage> loadedImages) {
        for (Map.Entry<String, ImagePlacement> entry : config.placements.entrySet()) {
            String placementName = entry.getKey();
            ImagePlacement placement = entry.getValue();

            BufferedImage image = loadedImages.get(placement.getImage());
            if (image == null) {
                continue;
            }

            DisplayedImage displayedImage = new DisplayedImage(placement, image, this.fakeIdRegistry);
            this.activeImages.put(placementName, displayedImage);
        }

        int count = this.activeImages.size();
        String form = count == 1 ? "image" : "images";
        this.getLogger().info("Loaded " + count + " " + form);

        new VisibilityTask(this.activeImages, config.showDistance, config.hideDistance).runTaskTimer(this, 0L, 10L);
    }

    private Map<String, BufferedImage> getLoadedImages(File imagesDirectory, PluginConfig config) {
        Map<String, BufferedImage> loadedImages = new HashMap<>();

        for (ImagePlacement placement : config.placements.values()) {
            String imageName = placement.getImage();

            if (loadedImages.containsKey(imageName)) {
                continue;
            }

            File file = new File(imagesDirectory, imageName);
            if (!file.exists()) {
                this.getLogger().warning("Image " + imageName + " not found");
                continue;
            }

            if (!isCorrectExtension(file)) {
                this.getLogger().warning("Image " + imageName + " has an unsupported extension, skipping...");
                continue;
            }

            BufferedImage image = null;
            try {
                image = ImageIO.read(file);

            } catch (IOException e) {
                this.getLogger().warning("Failed to load " + imageName + ": " + e.getMessage());
            }

            if (image != null) {
                loadedImages.put(imageName, image);

            } else {
                this.getLogger().warning("Image " + imageName + " could not be read");
            }
        }

        return loadedImages;
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
    }

    public Map<String, DisplayedImage> getActiveImages() {
        return this.activeImages;
    }

    private boolean isCorrectExtension(File file) {
        return file.isFile() && file.getName().endsWith(".png");
    }

}
