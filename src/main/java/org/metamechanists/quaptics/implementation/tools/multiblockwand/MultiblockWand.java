package org.metamechanists.quaptics.implementation.tools.multiblockwand;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.metamechanists.quaptics.storage.PersistentDataTraverser;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MultiblockWand extends SlimefunItem {
    public static final SlimefunItemStack MULTIBLOCK_WAND = new SlimefunItemStack(
            "QP_MULTBLOCK_WAND",
            Material.AMETHYST_SHARD,
            "&bMultiblock Wand",
            "&7● Shows you how to build complex multiblocks",
            "&7● &eRight Click &7a multiblock to see how to build it (works in offhand)",
            "&7● &eRight Click &7a projected block with an empty hand to see what block it is",
            "&7● &eRight Click &7a projected block with the corresponding item to quick place it");

    public MultiblockWand(final ItemGroup itemGroup, final SlimefunItemStack item, final RecipeType recipeType, final ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    // Updates the lore of the item (it changed between versions)
    // TODO: Remove this later
    public static void updateLore(final ItemStack stack) {
        if (!(SlimefunItem.getByItem(stack) instanceof MultiblockWand wand)) {
            return;
        }

        final ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }

        meta.lore(wand.getItem().lore());
        stack.setItemMeta(meta);
    }

    public static void removeProjection(final ItemStack stack) {
        final PersistentDataTraverser traverser = new PersistentDataTraverser(stack);
        final List<UUID> uuids = traverser.getUuidList("uuids");
        if (uuids == null) {
            return;
        }

        traverser.removeIf(key -> key.contains("uuids"));
        traverser.save(stack);
        uuids.stream()
                .map(Bukkit::getEntity)
                .filter(Objects::nonNull)
                .forEach(Entity::remove);
    }
}
