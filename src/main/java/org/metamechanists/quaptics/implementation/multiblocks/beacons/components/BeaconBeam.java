package org.metamechanists.quaptics.implementation.multiblocks.beacons.components;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.displaymodellib.sefilib.entity.display.DisplayGroup;
import org.metamechanists.quaptics.implementation.base.QuapticBlock;
import org.metamechanists.quaptics.implementation.Settings;
import org.metamechanists.quaptics.items.Lore;
import org.metamechanists.quaptics.items.Tier;
import org.metamechanists.displaymodellib.models.ModelBuilder;


public class BeaconBeam extends QuapticBlock {
    public static final Settings BEACON_BEAM_SETTINGS = Settings.builder()
            .tier(Tier.INTERMEDIATE)
            .operatingPowerHidden(true)
            .build();

    public static final SlimefunItemStack BEACON_BEAM = new SlimefunItemStack(
            "QP_BEACON_BEAM",
            Material.POLISHED_DEEPSLATE_WALL,
            "&dBeacon Beam",
            Lore.create(BEACON_BEAM_SETTINGS,
                    Lore.multiblockComponent()));

    public BeaconBeam(final ItemGroup itemGroup, final SlimefunItemStack item, final RecipeType recipeType, final ItemStack[] recipe, final Settings settings) {
        super(itemGroup, item, recipeType, recipe, settings);
    }

    @Override
    protected DisplayGroup initModel(@NotNull final Location location, @NotNull final Player player) {
        return new ModelBuilder()
                .buildAtBlockCenter(location);
    }
    @Override
    @NotNull
    protected Material getBaseMaterial() {
        return Material.POLISHED_DEEPSLATE_WALL;
    }
}
