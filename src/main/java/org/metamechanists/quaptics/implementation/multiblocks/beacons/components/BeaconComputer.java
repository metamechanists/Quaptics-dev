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
import org.metamechanists.quaptics.utils.Utils;
import org.metamechanists.displaymodellib.models.ModelBuilder;
import org.metamechanists.displaymodellib.models.components.ModelCuboid;


public class BeaconComputer extends QuapticBlock {
    public static final Settings BEACON_COMPUTER_SETTINGS = Settings.builder()
            .tier(Tier.INTERMEDIATE)
            .operatingPowerHidden(true)
            .build();

    public static final SlimefunItemStack BEACON_COMPUTER = new SlimefunItemStack(
            "QP_BEACON_COMPUTER",
            Material.LIGHT_BLUE_STAINED_GLASS,
            "&dBeacon Computer",
            Lore.create(BEACON_COMPUTER_SETTINGS,
                    Lore.multiblockComponent()));

    public BeaconComputer(final ItemGroup itemGroup, final SlimefunItemStack item, final RecipeType recipeType, final ItemStack[] recipe, final Settings settings) {
        super(itemGroup, item, recipeType, recipe, settings);
    }

    @Override
    protected DisplayGroup initModel(@NotNull final Location location, @NotNull final Player player) {
        return new ModelBuilder()
                .add("main", new ModelCuboid()
                        .material(Material.LIGHT_BLUE_STAINED_GLASS)
                        .brightness(Utils.BRIGHTNESS_OFF)
                        .size(0.8F, 1.0F, 0.8F)
                        .rotation(Math.PI / 4))
                .buildAtBlockCenter(location);
    }
}
