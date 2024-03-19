package org.metamechanists.quaptics.implementation.tools.multiblockwand;

import io.github.bakedlibs.dough.blocks.BlockPosition;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.quaptics.implementation.attachments.ComplexMultiblock;
import org.metamechanists.quaptics.utils.Language;
import org.metamechanists.quaptics.utils.id.simple.ItemDisplayId;

public class MultiblockWandListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void placeEvent(@NotNull final BlockPlaceEvent event) {
        Slimefun.runSync(() -> updateProjection(event.getBlock()), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void interactEvent(@NotNull final PlayerInteractEntityEvent event) {
        final Entity clickedEntity = event.getRightClicked();
        if (clickedEntity.getType() != EntityType.INTERACTION) {
            return;
        }

        final Block block = clickedEntity.getLocation().getBlock();
        final ItemDisplayId displayId = ComplexMultiblock.CACHE.get(new BlockPosition(block));
        if (displayId == null || displayId.get().isEmpty()) {
            return;
        }

        final ItemStack itemStack = displayId.get().get().getItemStack();
        final SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
        if (itemStack == null) {
            return;
        }

        final ItemStack mainHandItem = event.getPlayer().getInventory().getItemInMainHand();
        final ItemStack offHandItem = event.getPlayer().getInventory().getItemInOffHand();
        if (block.isEmpty() && (SlimefunUtils.isItemSimilar(mainHandItem, itemStack, true) || SlimefunUtils.isItemSimilar(offHandItem, itemStack, true))) {
            if (Slimefun.getProtectionManager().hasPermission(event.getPlayer(), block, Interaction.PLACE_BLOCK)) {
                block.setType(itemStack.getType());
                final boolean mainHand = SlimefunUtils.isItemSimilar(mainHandItem, itemStack, true);
                if (slimefunItem != null) {
                    final BlockPlaceEvent placeEvent = new BlockPlaceEvent(block, block.getState(), block, itemStack, event.getPlayer(), true, mainHand ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
                    BlockStorage.store(block, slimefunItem.getId());
                    slimefunItem.callItemHandler(BlockPlaceHandler.class, handler -> handler.onPlayerPlace(placeEvent));
                }
                (mainHand ? mainHandItem : offHandItem).subtract();
                Slimefun.runSync(() -> updateProjection(block), 1L);
                return;
            }
        }

        if ((SlimefunItem.getByItem(mainHandItem) instanceof MultiblockWand) || (SlimefunItem.getByItem(offHandItem) instanceof MultiblockWand)) {
            final String blockName = slimefunItem != null ? slimefunItem.getItemName() : ChatUtils.humanize(itemStack.getType().name());
            Language.sendLanguageMessage(event.getPlayer(), "multiblock.block-name", blockName);
        }
        Slimefun.runSync(() -> updateProjection(block), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void breakEvent(@NotNull final BlockBreakEvent event) {
        Slimefun.runSync(() -> updateProjection(event.getBlock()), 1L);
    }

    public static void updateProjection(Block block) {
        final BlockPosition position = new BlockPosition(block);
        final ItemDisplayId id = ComplexMultiblock.CACHE.get(position);
        if (id == null) {
            return;
        }

        id.get().ifPresent(itemDisplay -> {
            boolean correct;
            final ItemStack itemStack = itemDisplay.getItemStack();
            if (itemStack == null) {
                return;
            }

            final SlimefunItem slimefunItem = SlimefunItem.getByItem(itemStack);
            if (slimefunItem != null) {
                correct = BlockStorage.check(block, slimefunItem.getId());
            } else {
                correct = itemStack.getType() == block.getType();
            }

            if (block.isEmpty()) {
                itemDisplay.setGlowColorOverride(ComplexMultiblock.EMPTY_COLOR);
            } else {
                itemDisplay.setGlowColorOverride(correct
                        ? ComplexMultiblock.RIGHT_MATERIAL_COLOR
                        : ComplexMultiblock.WRONG_MATERIAL_COLOR);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public static void scrollEvent(@NotNull final PlayerItemHeldEvent event) {
        final ItemStack heldItem = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
        if (SlimefunItem.getByItem(heldItem) instanceof MultiblockWand) {
            MultiblockWand.removeProjection(heldItem);
        }
    }
}
