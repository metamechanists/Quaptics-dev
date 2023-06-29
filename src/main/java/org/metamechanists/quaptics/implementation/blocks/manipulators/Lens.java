package org.metamechanists.quaptics.implementation.blocks.manipulators;

import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.metamechanists.quaptics.connections.ConnectionGroup;
import org.metamechanists.quaptics.connections.points.ConnectionPoint;
import org.metamechanists.quaptics.connections.points.ConnectionPointInput;
import org.metamechanists.quaptics.connections.points.ConnectionPointOutput;
import org.metamechanists.quaptics.implementation.base.ConnectedBlock;
import org.metamechanists.quaptics.utils.Transformations;
import org.metamechanists.quaptics.utils.builders.BlockDisplayBuilder;
import org.metamechanists.quaptics.utils.id.ConnectionGroupID;

import java.util.ArrayList;
import java.util.List;

public class Lens extends ConnectedBlock {
    private static final int BRIGHTNESS_ON = 13;
    private static final int VIEW_RANGE_ON = 64;
    private final Vector3f glassDisplaySize = new Vector3f(displayRadius*2);
    private final Vector3f concreteDisplaySize = new Vector3f(displayRadius);
    private final Vector3f mainDisplayRotation = new Vector3f((float)(Math.PI/4), (float)(Math.PI/4), 0);
    private final Vector inputPointLocation = new Vector(0.0F, 0.0F, -connectionRadius);
    private final Vector outputPointLocation = new Vector(0.0F, 0.0F, connectionRadius);
    private final double powerLoss;

    public Lens(ItemGroup group, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                float radius, double maxPower, double powerLoss) {
        super(group, item, recipeType, recipe, radius, 2*radius, maxPower);
        this.powerLoss = powerLoss;
    }

    @Override
    protected void addDisplays(@NotNull DisplayGroup displayGroup, @NotNull Location location, Player player) {
        displayGroup.addDisplay("main", new BlockDisplayBuilder(location.clone().add(RELATIVE_CENTER))
                .setMaterial(Material.GLASS)
                .setTransformation(Transformations.adjustedRotateAndScale(glassDisplaySize, mainDisplayRotation))
                .build());
        displayGroup.addDisplay("concrete", new BlockDisplayBuilder(location.clone().add(RELATIVE_CENTER))
                .setMaterial(Material.WHITE_CONCRETE)
                .setTransformation(Transformations.adjustedRotateAndScale(concreteDisplaySize, mainDisplayRotation))
                .setViewRange(0)
                .setBrightness(BRIGHTNESS_ON)
                .build());
    }

    @Override
    protected List<ConnectionPoint> generateConnectionPoints(ConnectionGroupID groupID, Player player, Location location) {
        final List<ConnectionPoint> points = new ArrayList<>();
        points.add(new ConnectionPointInput(groupID, "input", formatPointLocation(player, location, inputPointLocation)));
        points.add(new ConnectionPointOutput(groupID, "output", formatPointLocation(player, location, outputPointLocation)));
        return points;
    }

    @Override
    public void onInputLinkUpdated(@NotNull ConnectionGroup group) {
        final ConnectionPointInput input = group.getInput("input");
        final ConnectionPointOutput output = group.getOutput("output");

        doBurnoutCheck(group, input);

        if (!output.hasLink()) {
            getDisplayGroup(group.getLocation()).getDisplays().get("concrete").setViewRange(0);
            return;
        }

        if (!input.hasLink() || !input.getLink().isEnabled()) {
            output.getLink().setEnabled(false);
            getDisplayGroup(group.getLocation()).getDisplays().get("concrete").setViewRange(0);
            return;
        }

        output.getLink().setPower(powerLoss(input.getLink().getPower(), powerLoss));
        output.getLink().setFrequency(input.getLink().getFrequency());
        output.getLink().setPhase(input.getLink().getPhase());
        output.getLink().setEnabled(true);
        getDisplayGroup(group.getLocation()).getDisplays().get("concrete").setViewRange(VIEW_RANGE_ON);
    }

    @Override
    protected @NotNull Material getBaseMaterial() {
        return Material.STRUCTURE_VOID;
    }
}