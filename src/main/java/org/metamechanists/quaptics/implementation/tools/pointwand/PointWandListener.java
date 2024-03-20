package org.metamechanists.quaptics.implementation.tools.pointwand;

import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
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
import org.metamechanists.quaptics.utils.id.complex.ConnectionPointId;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PointWandListener implements Listener {
    private static final Map<UUID, ConnectionPointId> PLAYERS = new HashMap<>();
    private static final Color SELECTED_COLOR = Color.fromRGB(0, 255, 255);

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void moveEvent(@NotNull final PlayerMoveEvent event) {
        if (!event.hasChangedOrientation()) {
            return;
        }

        final Player player = event.getPlayer();
        final ConnectionPointId pointId = PLAYERS.get(player.getUniqueId());
        if (pointId == null) {
            return;
        }

        pointId.get().ifPresent(point -> point.getGroup().ifPresent(group -> {
            final Location groupLocation = group.getLocation().orElse(null);
            if (groupLocation == null) {
                return;
            }

            final float radius = group.getBlock().getConnectionRadius();
            final Location centerLocation = groupLocation.getBlock().getLocation().toCenterLocation();

            final double yaw = Math.toRadians(event.getTo().getYaw());
            final double pitch = Math.toRadians(event.getTo().getPitch());

            final double x = radius * Math.cos(pitch) * Math.cos(yaw);
            final double y = radius * Math.sin(pitch);
            final double z = radius * Math.cos(pitch) * Math.sin(yaw);

            point.changeLocation(centerLocation.clone().add(x, y, z));
        }));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void interactEvent(@NotNull final PlayerInteractEntityEvent event) {
        final Entity clickedEntity = event.getRightClicked();
        if (clickedEntity.getType() != EntityType.INTERACTION) {
            return;
        }

        final ConnectionPointId pointId = new ConnectionPointId(clickedEntity.getUniqueId());
        if (!pointId.isValid() || pointId.get().isEmpty()) {
            return;
        }

        final Player player = event.getPlayer();
        final ConnectionPoint point = pointId.get().get();
        if (point.isGlowing()) {
            if (point.isGlowing(SELECTED_COLOR)) {
                PLAYERS.remove(player.getUniqueId());
                point.stopGlow();
            }
            return;
        }

        if (SlimefunItem.getByItem(player.getInventory().getItem(event.getHand())) instanceof PointWand) {
            point.glowColor(SELECTED_COLOR);
            PLAYERS.put(player.getUniqueId(), pointId);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void rightClickEvent(@NotNull final PlayerRightClickEvent event) {
        if (event.getSlimefunItem().orElse(null) instanceof PointWand && PLAYERS.containsKey(event.getPlayer().getUniqueId())) {
            PLAYERS.remove(event.getPlayer().getUniqueId()).get().ifPresent(ConnectionPoint::stopGlow);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void scrollEvent(@NotNull final PlayerItemHeldEvent event) {
        final Player player = event.getPlayer();
        final PlayerInventory inventory = player.getInventory();
        final ItemStack heldItem = inventory.getItem(event.getPreviousSlot());
        if (SlimefunItem.getByItem(heldItem) instanceof PointWand && PLAYERS.containsKey(player.getUniqueId())) {
            PLAYERS.remove(player.getUniqueId()).get().ifPresent(ConnectionPoint::stopGlow);
        }
    }
}
