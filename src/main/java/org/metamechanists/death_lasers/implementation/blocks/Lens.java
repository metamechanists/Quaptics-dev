package org.metamechanists.death_lasers.implementation.blocks;

import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.networks.energy.EnergyNetComponentType;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.metamechanists.death_lasers.connections.ConnectionGroup;
import org.metamechanists.death_lasers.connections.ConnectionPointStorage;
import org.metamechanists.death_lasers.connections.links.Link;
import org.metamechanists.death_lasers.connections.links.LinkProperties;
import org.metamechanists.death_lasers.connections.points.ConnectionPoint;
import org.metamechanists.death_lasers.connections.points.ConnectionPointInput;
import org.metamechanists.death_lasers.connections.points.ConnectionPointOutput;
import org.metamechanists.death_lasers.implementation.abstracts.ConnectedBlock;
import org.metamechanists.death_lasers.utils.DisplayUtils;

import java.util.HashMap;
import java.util.Map;

public class Lens extends ConnectedBlock {
    private final double maxPower;
    private final double powerLoss;
    @Getter
    private final EnergyNetComponentType energyComponentType = EnergyNetComponentType.CONSUMER;

    public Lens(ItemGroup group, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
            int capacity, int consumption, double maxPower, double powerLoss) {
        super(group, item, recipeType, recipe, capacity, consumption);
        this.maxPower = maxPower;
        this.powerLoss = powerLoss;
        // TODO burnout when max power exceeded
    }

    @Override
    protected DisplayGroup generateDisplayGroup(Location location) {
        // Height/width are zero to prevent the large interaction entity from obstructing the player
        final DisplayGroup displayGroup = new DisplayGroup(location, 0, 0);

        displayGroup.addDisplay(
                "main",
                DisplayUtils.spawnBlockDisplay(
                        location.clone().add(0.5, 0.5, 0.5),
                        Material.GLASS,
                        DisplayUtils.rotationTransformation(
                                new Vector3f(0.3F, 0.3F, 0.3F),
                                new Vector3f((float)(Math.PI/4), (float)(Math.PI/4), 0))));

        return displayGroup;
    }

    @Override
    protected Map<String, ConnectionPoint> generateConnectionPoints(Location location) {
        final Map<String, ConnectionPoint> points = new HashMap<>();
        points.put("input", new ConnectionPointInput("input", location.clone().add(new Vector(0.5F, 0.5F, 0.0F))));
        points.put("output", new ConnectionPointOutput("output", location.clone().add(new Vector(0.5F, 0.5F, 0.95F))));
        return points;
    }

    @Override
    public void onLinkUpdated(ConnectionGroup group) {
        final ConnectionPointInput input = (ConnectionPointInput) group.getPoint("input");
        final ConnectionPointOutput output = (ConnectionPointOutput) group.getPoint("output");

        if (!output.hasLink()) {
            return;
        }

        if (!input.hasLink() || !input.getLink().isEnabled()) {
            output.getLink().setEnabled(false);
            return;
        }

        final Link inputLink = input.getLink();
        final Link outputLink = output.getLink();

        outputLink.setPower(LinkProperties.calculatePower(inputLink, maxPower, powerLoss));
        outputLink.setEnabled(true);
    }

    @Override
    protected Location calculateNewLocation(ConnectionPoint from, ConnectionPoint to) {
        final Location fromGroupLocation = ConnectionPointStorage.getGroupLocationFromPointLocation(from.getLocation());
        final Location toGroupLocation = ConnectionPointStorage.getGroupLocationFromPointLocation(to.getLocation());
        final Vector radiusDirection = DisplayUtils.getDirection(fromGroupLocation, toGroupLocation).multiply(0.45F);
        return fromGroupLocation.clone().add(0.5, 0.5, 0.5).add(radiusDirection);
    }

    @Override
    protected @NotNull Material getBaseMaterial() {
        return Material.STRUCTURE_VOID;
    }
}