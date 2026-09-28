package pl.mikomak.images.image;

import org.bukkit.map.MapPalette;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MapColorMapper {

    // inspiration https://github.com/Andavin/Images
    public static byte[] convert(@NotNull BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int pixelCount = width * height;

        int[] pixels = new int[pixelCount];
        image.getRGB(0, 0, width, height, pixels, 0, width);

        byte[] colors = new byte[pixelCount];

        for (int i = 0; i < pixelCount; i++) {
            colors[i] = MapPalette.matchColor(new Color(pixels[i], true));
        }

        return colors;
    }

}
