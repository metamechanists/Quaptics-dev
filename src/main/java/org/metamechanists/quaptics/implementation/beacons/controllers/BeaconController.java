package org.metamechanists.quaptics.implementation.beacons.controllers;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Location;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.metamechanists.quaptics.connections.ConnectionGroup;
import org.metamechanists.quaptics.connections.ConnectionPoint;
import org.metamechanists.quaptics.implementation.attachments.ItemHolderBlock;
import org.metamechanists.quaptics.implementation.base.ConnectedBlock;
import org.metamechanists.quaptics.implementation.beacons.modules.BeaconModule;
import org.metamechanists.quaptics.implementation.blocks.Settings;
import org.metamechanists.quaptics.implementation.tools.QuapticChargeableItem;
import org.metamechanists.quaptics.storage.PersistentDataTraverser;
import org.metamechanists.quaptics.utils.Language;
import org.metamechanists.quaptics.utils.builders.InteractionBuilder;
import org.metamechanists.quaptics.utils.id.complex.ConnectionGroupId;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.List;
import java.util.Optional;


public abstract class BeaconController extends ConnectedBlock implements ItemHolderBlock {
    private static final float MODULE_BUTTON_SIZE = 0.2F;

    protected BeaconController(final ItemGroup itemGroup, final SlimefunItemStack item, final RecipeType recipeType, final ItemStack[] recipe, final Settings settings) {
        super(itemGroup, item, recipeType, recipe, settings);
    }

    @Override
    protected float getConnectionRadius() {
        return 0;
    }
    @Override
    protected List<ConnectionPoint> initConnectionPoints(final ConnectionGroupId groupId, final Player player, final Location location) {
        return List.of();
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onBreak(@NotNull final Location location) {
        super.onBreak(location);
        getModuleDisplayNames().forEach(name -> onBreakItemHolderBlock(location, name));
    }
    @Override
    public boolean onInsert(@NotNull final Location location, @NotNull final String name, @NotNull final ItemStack stack, @NotNull final Player player) {
        if (!(SlimefunItem.getByItem(stack) instanceof BeaconModule)) {
            Language.sendLanguageMessage(player, "beacon.not-module");
            return false;
        }
        return true;
    }
    @Override
    public Optional<ItemStack> onRemove(@NotNull final Location location, @NotNull final String name, @NotNull final ItemStack stack) {
        QuapticChargeableItem.updateLore(stack);
        return Optional.of(stack);
    }

    protected abstract List<String> getModuleDisplayNames();

    protected static void createButton(final ConnectionGroupId groupId, final @NotNull Location location, final Vector3f relativeLocation, final String slot) {
        final Interaction interaction = new InteractionBuilder()
                .width(MODULE_BUTTON_SIZE)
                .height(MODULE_BUTTON_SIZE)
                .build(location.clone().add(Vector.fromJOML(relativeLocation)));

        final PersistentDataTraverser traverser = new PersistentDataTraverser(interaction.getUniqueId());
        traverser.set("groupId", groupId);
        traverser.set("slot", slot);
    }

    public void interact(final Player player, final @NotNull ConnectionGroup group, final String slot) {
        final Optional<Location> location = group.getLocation();
        if (location.isEmpty()) {
            return;
        }

        itemHolderInteract(location.get(), slot, player);
    }
}
