package pl.mikomak.images.image;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageProcessor {

    public static BufferedImage stretchToGrid(BufferedImage image, GridSize gridSize) {
        int rows = gridSize.rows() * 128;
        int columns = gridSize.columns() * 128;

        BufferedImage stretched = new BufferedImage(columns, rows, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = stretched.createGraphics();

        graphics.drawImage(image, 0, 0, columns, rows, null);
        graphics.dispose();

        return stretched;
    }

    public static BufferedImage[][] slice(BufferedImage stretchedImage, GridSize gridSize) {
        BufferedImage[][] tiles = new BufferedImage[gridSize.rows()][gridSize.columns()];

        for (int row = 0; row < gridSize.rows(); row++) {
            for (int col = 0; col < gridSize.columns(); col++) {
                tiles[row][col] = stretchedImage.getSubimage(col * 128, row * 128, 128, 128);
            }
        }
        return tiles;
    }

}
