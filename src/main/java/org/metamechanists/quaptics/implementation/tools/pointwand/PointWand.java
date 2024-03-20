package org.metamechanists.quaptics.implementation.tools.pointwand;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PointWand extends SlimefunItem implements NotPlaceable {
    public static final SlimefunItemStack POINT_WAND = new SlimefunItemStack(
            "QP_POINT_WAND",
            Material.CYAN_CANDLE,
            "&bPoint Wand",
            "&7● Used to move connection points",
            "&7● &eRight Click &7a point to select it",
            "&7● &eLook Around &7to rotate the point",
            "&7● &eRight Click &7again to deselect it");

    public PointWand(final ItemGroup itemGroup, final SlimefunItemStack item, final RecipeType recipeType, final ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }
}
