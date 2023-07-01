package org.metamechanists.quaptics.utils.id;

import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.quaptics.beams.ticker.DirectTicker;

import java.util.UUID;

@SuppressWarnings("unused")
public class TickerId extends CustomId {
    public TickerId() {
        super();
    }
    public TickerId(final CustomId id) {
        super(id);
    }
    public TickerId(final String uuid) {
        super(uuid);
    }
    public TickerId(final UUID uuid) {
        super(uuid);
    }
    @Override
    public @Nullable DirectTicker get() {
        final Entity entity = Bukkit.getEntity(getUUID());
        return (entity instanceof BlockDisplay)
                ? new DirectTicker(this)
                : null;
    }
}