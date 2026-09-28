package pl.mikomak.images.packet;

import java.util.concurrent.atomic.AtomicInteger;

public class FakeIdRegistry {

    private final AtomicInteger entityIdCounter = new AtomicInteger(1000000);
    private final AtomicInteger mapIdCounter = new AtomicInteger(1000000);

    public int nextEntityId() {
        return entityIdCounter.getAndIncrement();
    }

    public int nextMapId() {
        return mapIdCounter.getAndIncrement();
    }

}
