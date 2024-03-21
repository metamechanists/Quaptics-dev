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
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metamechanists.quaptics.connections.ConnectionPoint;
import org.metamechanists.quaptics.connections.Link;
import org.metamechanists.quaptics.implementation.base.ConnectedBlock;
import org.metamechanists.quaptics.storage.PersistentDataTraverser;
import org.metamechanists.quaptics.utils.PersistentDataUtils;
import org.metamechanists.quaptics.utils.id.complex.ConnectionPointId;

public class PointWandListener implements Listener {
    private static final Color SELECTED_COLOR = Color.fromRGB(0, 255, 255);
    private static final Pair<ItemStack, PersistentDataTraverser> EMPTY = Pair.of(null, null);

    private static @Nullable Vector getLineSphereIntersections(Vector sphereCenter, @NotNull Vector lineOrigin, @NotNull Vector lineDirection, float r) {
        Vector f = lineOrigin.clone().subtract(sphereCenter);
        float discriminant = (float) (Math.pow(lineDirection.clone().dot(f), 2) - (f.toVector3f().absolute().lengthSquared() - Math.pow(r, 2)));
        if (discriminant < 0) {
            return null;
        }

        float d1 = -(float) ((lineDirection.dot(f)) + Math.sqrt(discriminant));
        float d2 = -(float) ((lineDirection.dot(f)) - Math.sqrt(discriminant));

        Vector solution1 = lineOrigin.clone().add(lineDirection.clone().multiply(d1));
        Vector solution2 = lineOrigin.clone().add(lineDirection.clone().multiply(d2));

        // Any negative solutions are invalid
        if (d1 < 0 && d2 < 0) {
            return null;
        }
        if (d1 > 0) {
            return solution1;
        }
        if (d2 > 0) {
            return solution2;
        }

        // If both solutions positive, return shortest one
        if (solution1.length() < solution2.length()) {
            return solution1;
        } else {
            return solution2;
        }
    }

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
        final Player player = event.getPlayer();
        final Pair<ItemStack, PersistentDataTraverser> wand = getPointWand(player);
        if (wand.equals(EMPTY)) {
            return;
        }

        final ConnectionPointId id = wand.value().getConnectionPointId("point");
        if (id == null) {
            return;
        }

        id.get().ifPresent(point -> point.getGroup().ifPresent(group -> {
            final Location newLocation = event.getTo().clone();
            final Location groupLocation = group.getLocation().orElse(null);
            if (groupLocation == null) {
                return;
            }

            if (newLocation.distanceSquared(groupLocation) > 9) {
                PointWand.tryUnSelect(wand.key(), wand.value());
                return;
            }

            final ConnectedBlock block = group.getBlock();
            final Location centerLocation = groupLocation.getBlock().getLocation().toCenterLocation();


            final Vector cameraPosition = newLocation.clone().add(0, player.getEyeHeight(), 0).toVector();
            final Vector unitVectorDirection = newLocation.getDirection().normalize();
            final Vector spherePosition = centerLocation.toVector();
            final float sphereRadius = block.getConnectionRadius();

            final Vector intersect = getLineSphereIntersections(spherePosition, cameraPosition, unitVectorDirection, sphereRadius);
            if (intersect == null) {
                return;
            }

            point.changeLocation(intersect.toLocation(newLocation.getWorld()));
            point.getLink().ifPresent(Link::regenerateBeam);
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
