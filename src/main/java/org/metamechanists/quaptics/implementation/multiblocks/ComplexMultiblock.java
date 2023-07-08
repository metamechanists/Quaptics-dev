package org.metamechanists.quaptics.implementation.multiblocks;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils;
import org.bukkit.Color;
import org.bukkit.block.Block;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.joml.Vector3f;
import org.metamechanists.quaptics.implementation.tools.multiblockwand.MultiblockWand;
import org.metamechanists.quaptics.storage.PersistentDataTraverser;
import org.metamechanists.quaptics.utils.BlockStorageAPI;
import org.metamechanists.quaptics.utils.Language;
import org.metamechanists.quaptics.utils.Transformations;
import org.metamechanists.quaptics.utils.builders.BlockDisplayBuilder;
import org.metamechanists.quaptics.utils.builders.InteractionBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@FunctionalInterface
public interface ComplexMultiblock {
    Color EMPTY_COLOR = Color.fromARGB(255, 255, 255, 0);
    Color WRONG_MATERIAL_COLOR = Color.fromARGB(255, 255, 0, 0);
    Color RIGHT_MATERIAL_COLOR = Color.fromARGB(255, 0, 255, 0);
    int DISPLAY_BRIGHTNESS = 15;
    float DISPLAY_SCALE = 0.5F;

    private static boolean isStructureBlockValid(final @NotNull Block center, final @NotNull Vector offset, final ItemStack predicted) {
        final Block block = center.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
        final SlimefunItem item = BlockStorageAPI.check(block);
        final ItemStack actual = item != null ? item.getItem() : new ItemStack(block.getType());
        return SlimefunUtils.isItemSimilar(predicted, actual, false);
    }

    private static @NotNull @Unmodifiable List<UUID> visualiseBlock(final @NotNull Block center, final @NotNull Vector offset, final @NotNull ItemStack itemStack) {
        final BlockDisplayBuilder blockDisplayBuilder = new BlockDisplayBuilder(center.getLocation().toCenterLocation())
                .setBrightness(DISPLAY_BRIGHTNESS)
                .setMaterial(itemStack.getType())
                .setTransformation(Transformations.adjustedScale(new Vector3f(DISPLAY_SCALE, DISPLAY_SCALE, DISPLAY_SCALE)));
        final Block block = center.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
        if (block.getType().isEmpty()) {
            blockDisplayBuilder.setGlow(EMPTY_COLOR);
        } else {
            blockDisplayBuilder.setGlow(isStructureBlockValid(center, offset, itemStack) ? RIGHT_MATERIAL_COLOR : WRONG_MATERIAL_COLOR);
        }

        final BlockDisplay blockDisplay = blockDisplayBuilder.build();
        final Interaction interaction = new InteractionBuilder(center.getLocation())
                .setWidth(DISPLAY_SCALE)
                .setHeight(DISPLAY_SCALE)
                .build();

        final SlimefunItem slimefunItem = BlockStorageAPI.check(center);
        final String blockName = slimefunItem != null ? slimefunItem.getItemName() : itemStack.getType().name();
        final PersistentDataTraverser traverser = new PersistentDataTraverser(interaction.getUniqueId());
        traverser.set("blockName", blockName);

        return List.of(blockDisplay.getUniqueId(), interaction.getUniqueId());
    }

    default boolean isStructureValid(final Block center) {
        return getStructure().entrySet().stream().allMatch(entry -> isStructureBlockValid(center, entry.getKey(), entry.getValue()));
    }

    default void visualiseStructure(final ItemStack wand, final Block center) {
        final List<UUID> uuids = new ArrayList<>();
        getStructure().forEach((key, value) -> uuids.addAll(visualiseBlock(center, key, value)));
        final PersistentDataTraverser traverser = new PersistentDataTraverser(wand);
        traverser.set("uuids", uuids);
        traverser.save(wand);
    }

    default void multiblockInteract(final Block center, final Player player, final ItemStack itemStack) {
        if (isStructureValid(center)) {
            Language.sendLanguageMessage(player, "multiblock.valid");
            return;
        }

        visualiseStructure(itemStack, center);
    }

    default boolean multiblockInteract(final Block center, final @NotNull Player player) {
        final ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        final ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (SlimefunItem.getByItem(mainHandItem) instanceof  MultiblockWand) {
            multiblockInteract(center, player, mainHandItem);
            return true;
        }

        if (SlimefunItem.getByItem(offHandItem) instanceof MultiblockWand) {
            multiblockInteract(center, player, offHandItem);
            return true;
        }

        return false;
    }

    Map<Vector, ItemStack> getStructure();
}
