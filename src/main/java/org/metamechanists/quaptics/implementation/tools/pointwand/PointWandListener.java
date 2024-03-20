package org.metamechanists.quaptics.implementation.tools.pointwand;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.quaptics.connections.ConnectionPoint;
import org.metamechanists.quaptics.storage.PersistentDataTraverser;
import org.metamechanists.quaptics.utils.PersistentDataUtils;
import org.metamechanists.quaptics.utils.id.complex.ConnectionPointId;

public class PointWandListener implements Listener {
    private static final Color SELECTED_COLOR = Color.fromRGB(0, 255, 255);
    private static final Pair<ItemStack, PersistentDataTraverser> EMPTY = Pair.of(null, null);

    public static Pair<ItemStack, PersistentDataTraverser> getPointWand(Player player) {
        final PlayerInventory inventory = player.getInventory();
        final boolean mainHand = "QP_POINT_WAND".equals(PersistentDataUtils.getSlimefunId(inventory.getItemInMainHand()));
        final ItemStack stack = mainHand ? inventory.getItemInMainHand() : inventory.getItemInOffHand();
        if (stack == null || stack.getType().isAir() || !stack.hasItemMeta()) {
            return EMPTY;
        }

        final PersistentDataTraverser traverser = new PersistentDataTraverser(stack);
        if (!"QP_POINT_WAND".equals(traverser.getSlimefunId())) {
            return EMPTY;
        }

        return Pair.of(stack, traverser);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void moveEvent(@NotNull final PlayerMoveEvent event) {
        if (!event.hasChangedOrientation()) {
            return;
        }

        final Pair<ItemStack, PersistentDataTraverser> wand = getPointWand(event.getPlayer());
        if (wand.equals(EMPTY)) {
            return;
        }

        final ConnectionPointId id = wand.value().getConnectionPointId("point");
        if (id == null) {
            return;
        }

        id.get().ifPresent(point -> point.getGroup().ifPresent(group -> {
            final Location groupLocation = group.getLocation().orElse(null);
            if (groupLocation == null) {
                return;
            }

            final float radius = group.getBlock().getConnectionRadius();
            final Location centerLocation = groupLocation.getBlock().getLocation().toCenterLocation();

            point.changeLocation(centerLocation.clone().add(event.getTo().getDirection().clone().normalize().multiply(radius)));
        }));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void interactEvent(@NotNull final PlayerInteractEntityEvent event) {
        final Entity clickedEntity = event.getRightClicked();
        if (clickedEntity.getType() != EntityType.INTERACTION) {
            return;
        }

        final Pair<ItemStack, PersistentDataTraverser> wand = getPointWand(event.getPlayer());
        if (wand.equals(EMPTY)) {
            return;
        }

        if (PointWand.tryUnSelect(wand.key(), wand.value())) {
            return;
        }

        final ConnectionPointId id = new ConnectionPointId(clickedEntity.getUniqueId());
        if (!id.isValid() || id.get().isEmpty()) {
            return;
        }

        final ConnectionPoint point = id.get().get();
        if (point.isGlowing()) {
            return;
        }

        wand.value().set("point", id);
        wand.value().save(wand.key());
        point.glowColor(SELECTED_COLOR);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void scrollEvent(@NotNull final PlayerItemHeldEvent event) {
        PointWand.tryUnSelect(event.getPlayer().getInventory().getItem(event.getPreviousSlot()));
    }
}
