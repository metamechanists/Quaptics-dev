package org.metamechanists.quaptics.implementation.tools.pointwand;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.attributes.NotPlaceable;
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler;
import io.github.thebusybiscuit.slimefun4.implementation.items.blocks.UnplaceableBlock;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.quaptics.connections.ConnectionPoint;
import org.metamechanists.quaptics.storage.PersistentDataTraverser;
import org.metamechanists.quaptics.utils.id.complex.ConnectionPointId;

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

        addItemHandler(onUse());
    }

    public @NotNull ItemUseHandler onUse() {
        return event -> {
            tryUnSelect(event.getItem());
            event.cancel();
        };
    }

    public static boolean tryUnSelect(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType().isAir()) {
            return false;
        }

        return tryUnSelect(itemStack, new PersistentDataTraverser(itemStack));
    }

    public static boolean tryUnSelect(ItemStack itemStack, PersistentDataTraverser traverser) {
        if (!"QP_POINT_WAND".equals(traverser.getSlimefunId())) {
            return false;
        }

        final ConnectionPointId pointId = traverser.getConnectionPointId("point");
        if (pointId != null) {
            pointId.get().ifPresent(ConnectionPoint::stopGlow);
            traverser.remove("point");
            traverser.save(itemStack);
            return true;
        }
        return false;
    }
}
