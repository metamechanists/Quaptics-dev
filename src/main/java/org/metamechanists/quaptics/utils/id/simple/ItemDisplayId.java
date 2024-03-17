package org.metamechanists.quaptics.utils.id.simple;

import org.bukkit.Bukkit;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.metamechanists.quaptics.utils.id.CustomId;

import java.util.Optional;
import java.util.UUID;

public class ItemDisplayId extends CustomId {
    public ItemDisplayId() {
        super();
    }
    public ItemDisplayId(final CustomId id) {
        super(id);
    }
    public ItemDisplayId(final String uuid) {
        super(uuid);
    }
    public ItemDisplayId(final UUID uuid) {
        super(uuid);
    }
    @Override
    public Optional<ItemDisplay> get() {
        return Bukkit.getEntity(getUUID()) instanceof final ItemDisplay itemDisplay
                ? Optional.of(itemDisplay)
                : Optional.empty();
    }
}
