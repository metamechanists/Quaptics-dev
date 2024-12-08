package org.metamechanists.quaptics.implementation.blocks.manipulators;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import org.metamechanists.quaptics.connections.ConnectionGroup;
import org.metamechanists.quaptics.connections.ConnectionPoint;
import org.metamechanists.quaptics.connections.ConnectionPointType;
import org.metamechanists.quaptics.connections.Link;
import org.metamechanists.quaptics.implementation.attachments.PowerAnimatedBlock;
import org.metamechanists.quaptics.implementation.attachments.PowerLossBlock;
import org.metamechanists.quaptics.implementation.base.ConnectedBlock;
import org.metamechanists.quaptics.implementation.Settings;
import org.metamechanists.quaptics.items.Lore;
import org.metamechanists.quaptics.items.Tier;
import org.metamechanists.quaptics.utils.Utils;
import org.metamechanists.quaptics.utils.id.complex.ConnectionGroupId;
import org.metamechanists.displaymodellib.models.ModelBuilder;
import org.metamechanists.displaymodellib.models.components.ModelDiamond;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Combiner extends ConnectedBlock implements PowerAnimatedBlock, PowerLossBlock {
    public static final Settings COMBINER_1_SETTINGS = Settings.builder()
            .tier(Tier.PRIMITIVE)
            .powerLoss(0.2)
            .connections(2)
            .build();
    public static final Settings COMBINER_2_SETTINGS = Settings.builder()
            .tier(Tier.BASIC)
            .powerLoss(0.14)
            .connections(3)
            .build();
    public static final Settings COMBINER_3_SETTINGS = Settings.builder()
            .tier(Tier.INTERMEDIATE)
            .powerLoss(0.08)
            .connections(4)
            .build();
    public static final Settings COMBINER_4_SETTINGS = Settings.builder()
            .tier(Tier.ADVANCED)
            .powerLoss(0.05)
            .connections(5)
            .build();

    public static final SlimefunItemStack COMBINER_1 = new SlimefunItemStack(
            "QP_COMBINER_1",
            Material.GRAY_STAINED_GLASS,
            "&9Combiner &8I",
            Lore.create(COMBINER_1_SETTINGS,
                    "&7● Combines multiple quaptic rays into one"));
    public static final SlimefunItemStack COMBINER_2 = new SlimefunItemStack(
            "QP_COMBINER_2",
            Material.GRAY_STAINED_GLASS,
            "&9Combiner &8II",
            Lore.create(COMBINER_2_SETTINGS,
                    "&7● Combines multiple quaptic rays into one"));
    public static final SlimefunItemStack COMBINER_3 = new SlimefunItemStack(
            "QP_COMBINER_3",
            Material.GRAY_STAINED_GLASS,
            "&9Combiner &8III",
            Lore.create(COMBINER_3_SETTINGS,
                    "&7● Combines multiple quaptic rays into one"));
    public static final SlimefunItemStack COMBINER_4 = new SlimefunItemStack(
            "QP_COMBINER_4",
            Material.GRAY_STAINED_GLASS,
            "&9Combiner &8IV",
            Lore.create(COMBINER_4_SETTINGS,
                    "&7● Combines multiple quaptic rays into one"));

    private static final double CONNECTION_ANGLE = Math.PI * 2/3;

    private final Vector inputStartingLocation = new Vector(0.0F, 0.0F, -getConnectionRadius());
    private final Vector outputLocation = new Vector(0.0F, 0.0F, getConnectionRadius());

    public Combiner(final ItemGroup itemGroup, final SlimefunItemStack item, final RecipeType recipeType, final ItemStack[] recipe, final Settings settings) {
        super(itemGroup, item, recipeType, recipe, settings);
    }

    @Override
    public float getConnectionRadius() {
        return 0.55F;
    }
    @Override
    protected DisplayGroup initModel(final @NotNull Location location, final @NotNull Player player) {
        return new ModelBuilder()
                .add("concrete", new ModelDiamond()
                        .material(settings.getTier().concreteMaterial)
                        .brightness(Utils.BRIGHTNESS_OFF)
                        .size(0.4F))
                .add("glass", new ModelDiamond()
                        .material(Material.GRAY_STAINED_GLASS)
                        .size(0.8F))
                .buildAtBlockCenter(location);
    }
    @Override
    protected List<ConnectionPoint> initConnectionPoints(final ConnectionGroupId groupId, final Player player, final Location location) {
        final List<ConnectionPoint> points = IntStream.range(0,
                settings.getConnections()).mapToObj(i -> new ConnectionPoint(ConnectionPointType.INPUT, groupId, "input " + Objects.toString(i),
                formatPointLocation(player, location, getRelativeInputLocation(i)))).collect(Collectors.toList());
        points.add(new ConnectionPoint(ConnectionPointType.OUTPUT, groupId, "output", formatPointLocation(player, location, outputLocation)));
        return points;
    }

    @Override
    public void onInputLinkUpdated(@NotNull final ConnectionGroup group, @NotNull final Location location) {
        final List<ConnectionPoint> enabledInputs = getEnabledInputs(location);
        if (doBurnoutCheck(group, enabledInputs)) {
            return;
        }

        onPoweredAnimation(location, !enabledInputs.isEmpty());

        final List<Link> incomingLinks = getIncomingLinks(location);
        final Optional<Link> outputLink = getLink(location, "output");
        if (outputLink.isEmpty()) {
            return;
        }

        final double inputPower = incomingLinks.stream().mapToDouble(Link::getPower).sum();
        final double inputFrequency = incomingLinks.stream().mapToDouble(Link::getFrequency).min().orElse(0.0);
        final int inputPhase = incomingLinks.stream().mapToInt(Link::getPhase).min().orElse(0);
        outputLink.get().setPowerFrequencyPhase(
                PowerLossBlock.calculatePowerLoss(settings, inputPower),
                inputFrequency,
                inputPhase);
    }
    @Override
    public void onPoweredAnimation(final @NotNull Location location, final boolean powered) {
        brightnessAnimation(location, "concrete", powered);
    }

    private @NotNull Vector getRelativeInputLocation(final int i) {
        final double angle = (-CONNECTION_ANGLE /2) + CONNECTION_ANGLE *((double)(i) / (settings.getConnections()-1));
        return inputStartingLocation.clone().rotateAroundY(angle);
    }
}
