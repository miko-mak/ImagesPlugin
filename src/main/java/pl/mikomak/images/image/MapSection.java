package pl.mikomak.images.image;

import org.bukkit.Location;

public record MapSection(int fakeEntityId, int fakeMapId, Location location, byte[] pixelColors) {}
