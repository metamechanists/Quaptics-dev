package org.metamechanists.quaptics.implementation.attachments;

import dev.sefiraat.sefilib.entity.display.builders.ItemDisplayBuilder;
import dev.sefiraat.sefilib.misc.TransformationBuilder;
import io.github.bakedlibs.dough.blocks.BlockPosition;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;
import org.metamechanists.displaymodellib.builders.InteractionBuilder;
import org.metamechanists.quaptics.implementation.tools.multiblockwand.MultiblockWand;
import org.metamechanists.quaptics.storage.PersistentDataTraverser;
import org.metamechanists.quaptics.utils.BlockStorageAPI;
import org.metamechanists.quaptics.utils.Language;
import org.metamechanists.quaptics.utils.id.simple.ItemDisplayId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@FunctionalInterface
public interface ComplexMultiblock {
    Map<BlockPosition, ItemDisplayId> CACHE = new HashMap<>();
    Color EMPTY_COLOR = Color.fromARGB(255, 255, 255, 0);
    Color WRONG_MATERIAL_COLOR = Color.fromARGB(255, 255, 0, 0);
    Color RIGHT_MATERIAL_COLOR = Color.fromARGB(255, 0, 255, 0);
    Display.Brightness DISPLAY_BRIGHTNESS = new Display.Brightness(15, 15);
    float DISPLAY_SIZE = 0.75F;
    Transformation TRANSFORMATION = new TransformationBuilder().scale(DISPLAY_SIZE, DISPLAY_SIZE, DISPLAY_SIZE).build();
    ItemDisplayBuilder GHOST_BLOCK_DISPLAY = new ItemDisplayBuilder().setTransformation(TRANSFORMATION).setBrightness(DISPLAY_BRIGHTNESS);

    private static boolean isStructureBlockValid(final @NotNull Block center, final @NotNull Vector offset, final ItemStack predicted) {
        final Block actual = center.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
        return isStructureBlockValid(actual, predicted);
    }
    private static boolean isStructureBlockValid(final @NotNull Block actual, final ItemStack predicted) {
        final SlimefunItem predictedSlimefunItem = SlimefunItem.getByItem(predicted);
        final SlimefunItem actualSlimefunItem = BlockStorageAPI.check(actual);

        if (actualSlimefunItem != null) {
            return predictedSlimefunItem != null && predictedSlimefunItem.getId().equals(actualSlimefunItem.getId());
        }

        return predicted.getType() == actual.getType();
    }
    default boolean isStructureValid(final Block center) {
        return getStructure().entrySet().stream().allMatch(entry -> isStructureBlockValid(center, entry.getKey(), entry.getValue()));
    }

    private static @NotNull @Unmodifiable List<UUID> projectBlock(final @NotNull Block center, final @NotNull Vector offset, final @NotNull ItemStack stack) {
        final Block block = center.getRelative(offset.getBlockX(), offset.getBlockY(), offset.getBlockZ());
        if (block.getType().isEmpty()) {
            GHOST_BLOCK_DISPLAY.setGlowColorOverride(EMPTY_COLOR);
        } else {
            GHOST_BLOCK_DISPLAY.setGlowColorOverride(isStructureBlockValid(block, stack)
                            ? RIGHT_MATERIAL_COLOR
                            : WRONG_MATERIAL_COLOR);
        }

        final ItemDisplay itemDisplay = GHOST_BLOCK_DISPLAY
                .setItemStack(new ItemStack(stack))
                .setLocation(block.getLocation().toCenterLocation())
                .build();
        itemDisplay.setGlowing(true);
        final Interaction interaction = new InteractionBuilder()
                .width(DISPLAY_SIZE)
                .height(DISPLAY_SIZE)
                .build(block.getLocation().toCenterLocation());

        CACHE.put(new BlockPosition(block), new ItemDisplayId(itemDisplay.getUniqueId()));

        final PersistentDataTraverser traverser = new PersistentDataTraverser(interaction.getUniqueId());
        traverser.set("linked_block", new ItemDisplayId(itemDisplay.getUniqueId()));

        return List.of(itemDisplay.getUniqueId(), interaction.getUniqueId());
    }
    default void visualiseStructure(final ItemStack wand, final Block center) {
        final List<UUID> uuids = new ArrayList<>();
        getStructure().forEach((key, value) -> uuids.addAll(projectBlock(center, key, value)));
        final PersistentDataTraverser traverser = new PersistentDataTraverser(wand);
        traverser.set("uuids", uuids);
        traverser.save(wand);
    }

    default void multiblockInteract(final Block center, final Player player, final ItemStack stack) {
        MultiblockWand.updateLore(stack);
        if (isStructureValid(center)) {
            Language.sendLanguageMessage(player, "multiblock.valid");
            return;
        }

        MultiblockWand.removeProjection(stack);
        visualiseStructure(stack, center);
    }
    default boolean multiblockInteract(final Block center, final @NotNull Player player) {
        final ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        final ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if (SlimefunItem.getByItem(mainHandItem) instanceof MultiblockWand) {
            multiblockInteract(center, player, mainHandItem);
            return true;
        }

        if (SlimefunItem.getByItem(offHandItem) instanceof MultiblockWand) {
            multiblockInteract(center, player, offHandItem);
            return true;
        }

        return false;
    }

    @SuppressWarnings("unused")
    default void tickAnimation(@NotNull final Location centerLocation, final double timeSeconds) {}

    Map<Vector, ItemStack> getStructure();
}
