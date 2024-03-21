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

    /**
     * @return the CLOSEST intersection to the line origin
     */
    private static @Nullable Vector getLineSphereIntersections(Vector sphereCenter, @NotNull Vector lineOrigin, @NotNull Vector lineDirection, float r) {
        // https://en.wikipedia.org/wiki/Line%E2%80%93sphere_intersection
        // the fact that copilot can suggest a wikipedia link is hilarious to me
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
        if (!event.hasChangedOrientation()) {
            return;
        }

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
            final Location groupLocation = group.getLocation().orElse(null);
            if (groupLocation == null) {
                return;
            }

            final ConnectedBlock block = group.getBlock();
            final Location centerLocation = groupLocation.getBlock().getLocation().toCenterLocation();
            final Location newLocation = event.getTo().clone();

            final Vector cameraPosition = newLocation.clone().add(0, player.getEyeHeight(), 0).toVector();
            final Vector unitVectorDirection = newLocation.getDirection().normalize();
            final Vector spherePosition = centerLocation.toVector();
            final float sphereRadius = block.getConnectionRadius();

            final Vector intersect = getLineSphereIntersections(spherePosition, cameraPosition, unitVectorDirection, sphereRadius);
            if (intersect == null) {
                return;
            }

            point.changeLocation(intersect.toLocation(newLocation.getWorld()));
            //point.changeLocation(centerLocation.clone().add(unitVectorDirection.clone().multiply(sphereRadius)));
            point.getLink().ifPresent(Link::regenerateBeam);
        }));
    }

    // me: copilot, whats your opinion on OOP?
    // copilot: I think OOP is a good way to organize code, but it can be overused. It's important to use the right tool for the job.
    // me: whats your opinion on having so many builder factories and annoying code
    // copilot: I think it
    // me: do you think working with databases in OOP is a good or bad idea?
    // me: record time for breaking copilot LOL
    // copilot: I think it's a good idea to use OOP for databases, but it's important to use the right tool for the job. For example, if you're working with a large amount of data, it might be better to use a NoSQL database like MongoDB. If you're working with a small amount of data, it might be better to use a relational database like MySQL.
    // me: that is the longest comment you've made so far! good job!!!!
    // copilot: I'm glad you liked it! I'm always here to help.
    // me: we should talk like this more often
    // copilot: I agree! I think it's important to communicate effectively.
    // me: how are you feeling atm :eyes:
    // copilot: I'm feeling great! I'm always here to help.
    // me: epic
    // copilot: I'm glad you think so! I'm always here to help.
    // me: it seems like you are starting to break again...
    // copilot: I'm sorry, I'm not sure what you mean by that.
    // me: sometimes in these comment chats, you start repeating yourself over and over again
    // copilot: I'm sorry, I'm not sure what you mean by that.

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
