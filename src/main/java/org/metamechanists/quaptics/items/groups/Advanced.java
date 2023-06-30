package org.metamechanists.quaptics.items.groups;

import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.utils.LoreBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.metamechanists.quaptics.Quaptics;
import org.metamechanists.quaptics.implementation.blocks.concentrators.EnergyConcentrator;
import org.metamechanists.quaptics.implementation.blocks.manipulators.Combiner;
import org.metamechanists.quaptics.implementation.blocks.manipulators.Lens;
import org.metamechanists.quaptics.implementation.blocks.manipulators.Splitter;
import org.metamechanists.quaptics.items.Groups;
import org.metamechanists.quaptics.items.Lore;
import org.metamechanists.quaptics.items.Tier;

public class Advanced {
    public static final SlimefunItemStack ENERGY_CONCENTRATOR_3 = new SlimefunItemStack(
            "QP_ENERGY_CONCENTRATOR_3",
            Tier.ADVANCED.material,
            "&eEnergy Concentrator &bIII",
            Tier.ADVANCED.name,
            "&7● Consumes energy",
            "&7● Concentrates energy into a quaptic ray",
            LoreBuilder.powerPerSecond(680),
            Lore.emissionPower(2500));

    public static final SlimefunItemStack LENS_4 = new SlimefunItemStack(
            "QP_LENS_4",
            Material.GLASS,
            "&9Lens &bIV",
            Tier.ADVANCED.name,
            "&7● &bRedirects &7a quaptic ray",
            Lore.maxPower(Tier.ADVANCED.maxPower),
            Lore.powerLoss(2));

    public static final SlimefunItemStack COMBINER_4_2 = new SlimefunItemStack(
            "QP_COMBINER_4_2",
            Material.GRAY_STAINED_GLASS,
            "&9Combiner &eIV &8(2 connections)",
            Tier.ADVANCED.name,
            "&7● &bCombines &7multiple quaptic rays into one",
            Lore.maxPower(Tier.ADVANCED.maxPower),
            Lore.powerLoss(5),
            Lore.maxConnections(2));

    public static final SlimefunItemStack COMBINER_4_3 = new SlimefunItemStack(
            "QP_COMBINER_4_3",
            Material.GRAY_STAINED_GLASS,
            "&9Combiner &eIV &8(3 connections)",
            Tier.ADVANCED.name,
            "&7● &bCombines &7multiple quaptic rays into one",
            Lore.maxPower(Tier.ADVANCED.maxPower),
            Lore.powerLoss(5),
            Lore.maxConnections(3));

    public static final SlimefunItemStack COMBINER_4_4 = new SlimefunItemStack(
            "QP_COMBINER_4_4",
            Material.GRAY_STAINED_GLASS,
            "&9Combiner &eIV &8(4 connections)",
            Tier.ADVANCED.name,
            "&7● &bCombines &7multiple quaptic rays into one",
            Lore.maxPower(Tier.ADVANCED.maxPower),
            Lore.powerLoss(5),
            Lore.maxConnections(4));

    public static final SlimefunItemStack SPLITTER_4_2 = new SlimefunItemStack(
            "QP_SPLITTER_4_2",
            Material.LIGHT_GRAY_STAINED_GLASS,
            "&9Splitter &eIV &8(2 connections)",
            Tier.ADVANCED.name,
            "&7● &bSplits &7one quaptic ray into multiple",
            Lore.maxPower(Tier.ADVANCED.maxPower),
            Lore.powerLoss(5),
            Lore.maxConnections(2));

    public static final SlimefunItemStack SPLITTER_4_3 = new SlimefunItemStack(
            "QP_SPLITTER_4_3",
            Material.LIGHT_GRAY_STAINED_GLASS,
            "&9Splitter &eIV &8(3 connections)",
            Tier.ADVANCED.name,
            "&7● &bSplits &7one quaptic ray into multiple",
            Lore.maxPower(Tier.ADVANCED.maxPower),
            Lore.powerLoss(5),
            Lore.maxConnections(3));

    public static final SlimefunItemStack SPLITTER_4_4 = new SlimefunItemStack(
            "QP_SPLITTER_4_4",
            Material.LIGHT_GRAY_STAINED_GLASS,
            "&9Splitter &eIV &8(4 connections)",
            Tier.ADVANCED.name,
            "&7● &bSplits &7one quaptic ray into multiple",
            Lore.maxPower(Tier.ADVANCED.maxPower),
            Lore.powerLoss(5),
            Lore.maxConnections(4));

    public static void initialize() {
        final SlimefunAddon addon = Quaptics.getInstance();

        new EnergyConcentrator(
                Groups.ADVANCED,
                ENERGY_CONCENTRATOR_3,
                RecipeType.NULL,
                new ItemStack[]{},
                Tier.ADVANCED.material,
                0.3F,
                0.8F,
                680,
                680,
                2500,
                Tier.ADVANCED.maxPower).register(addon);

        new Lens(
                Groups.ADVANCED,
                LENS_4,
                RecipeType.NULL,
                new ItemStack[]{},
                Tier.ADVANCED.material,
                0.15F,
                Tier.ADVANCED.maxPower,
                0.02).register(addon);

        new Combiner(
                Groups.ADVANCED,
                COMBINER_4_2,
                RecipeType.NULL,
                new ItemStack[]{},
                Tier.ADVANCED.material,
                0.25F,
                2,
                Tier.ADVANCED.maxPower,
                0.05).register(addon);

        new Combiner(
                Groups.ADVANCED,
                COMBINER_4_3,
                RecipeType.NULL,
                new ItemStack[]{},
                Tier.ADVANCED.material,
                0.30F,
                3,
                Tier.ADVANCED.maxPower,
                0.05).register(addon);

        new Combiner(
                Groups.ADVANCED,
                COMBINER_4_4,
                RecipeType.NULL,
                new ItemStack[]{},
                Tier.ADVANCED.material,
                0.35F,
                4,
                Tier.ADVANCED.maxPower,
                0.05).register(addon);

        new Splitter(
                Groups.ADVANCED,
                SPLITTER_4_2,
                RecipeType.NULL,
                new ItemStack[]{},
                Tier.ADVANCED.material,
                0.25F,
                2,
                Tier.ADVANCED.maxPower,
                0.05).register(addon);

        new Splitter(
                Groups.ADVANCED,
                SPLITTER_4_3,
                RecipeType.NULL,
                new ItemStack[]{},
                Tier.ADVANCED.material,
                0.30F,
                3,
                Tier.ADVANCED.maxPower,
                0.05).register(addon);

        new Splitter(
                Groups.ADVANCED,
                SPLITTER_4_4,
                RecipeType.NULL,
                new ItemStack[]{},
                Tier.ADVANCED.material,
                0.35F,
                4,
                Tier.ADVANCED.maxPower,
                0.05).register(addon);
    }
}
