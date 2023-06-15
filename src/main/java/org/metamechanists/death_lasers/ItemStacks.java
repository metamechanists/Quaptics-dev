package org.metamechanists.death_lasers;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import org.bukkit.Material;

public class ItemStacks {
    public static final SlimefunItemStack LINEAR_TIME_EMITTER = new SlimefunItemStack(
            "LINEAR_TIME_EMITTER",
            Material.GLASS,
            "&4&lLinear Time Emitter");
    public static final SlimefunItemStack TARGETING_WAND = new SlimefunItemStack(
            "TARGETING_WAND",
            Material.BLAZE_ROD,
            "&6Targeting Wand",
            "&eRight Click &7to select a source",
            "&eRight Click &7again to create a link",
            "&eShift Right Click &7to remove a link");
}
