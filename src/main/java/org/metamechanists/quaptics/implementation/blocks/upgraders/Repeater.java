package org.metamechanists.quaptics.implementation.blocks.upgraders;

import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import io.github.bakedlibs.dough.data.persistent.PersistentDataAPI;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.metamechanists.quaptics.connections.ConnectionGroup;
import org.metamechanists.quaptics.connections.ConnectionPoint;
import org.metamechanists.quaptics.connections.ConnectionPointType;
import org.metamechanists.quaptics.connections.Link;
import org.metamechanists.quaptics.implementation.blocks.Settings;
import org.metamechanists.quaptics.implementation.blocks.attachments.PowerAnimatedBlock;
import org.metamechanists.quaptics.implementation.blocks.attachments.PowerLossBlock;
import org.metamechanists.quaptics.implementation.blocks.base.ConnectedBlock;
import org.metamechanists.quaptics.items.Lore;
import org.metamechanists.quaptics.items.Tier;
import org.metamechanists.quaptics.utils.Keys;
import org.metamechanists.quaptics.utils.Utils;
import org.metamechanists.quaptics.utils.builders.BlockDisplayBuilder;
import org.metamechanists.quaptics.utils.id.complex.ConnectionGroupId;
import org.metamechanists.quaptics.utils.transformations.TransformationMatrixBuilder;
import org.metamechanists.quaptics.utils.transformations.TransformationUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Repeater extends ConnectedBlock implements PowerAnimatedBlock, PowerLossBlock {
    public static final Settings REPEATER_1_SETTINGS = Settings.builder()
            .tier(Tier.BASIC)
            .minPower(15)
            .powerLoss(0.05)
            .minFrequency(0.0)
            .maxFrequency(0.3)
            .frequencyStep(0.1)
            .repeaterDelay(1)
            .build();
    public static final SlimefunItemStack REPEATER_1 = new SlimefunItemStack(
            "QP_REPEATER_1",
            Material.RED_STAINED_GLASS,
            "&cRepeater &4I",
            Lore.create(REPEATER_1_SETTINGS,
                    "&7● Increases the frequency of a quaptic ray"));

    private static final Vector3f GLASS_DISPLAY_SIZE = new Vector3f(0.50F);
    private static final Vector3f REPEATER_DISPLAY_SIZE = new Vector3f(0.25F);
    private static final Vector3f REPEATER_OFFSET = new Vector3f(0.0F, 0.10F, 0.0F);
    private static final Vector3f CONCRETE_DISPLAY_SIZE = new Vector3f(0.26F, 0.075F, 0.26F);
    private static final Vector3f CONCRETE_OFFSET = new Vector3f(0.0F, -0.05F, 0.0F);

    private final Vector inputPointLocation = new Vector(0.0F, 0.0F, -getConnectionRadius());
    private final Vector outputPointLocation = new Vector(0.0F, 0.0F, getConnectionRadius());

    public Repeater(final ItemGroup itemGroup, final SlimefunItemStack item, final RecipeType recipeType, final ItemStack[] recipe, final Settings settings) {
        super(itemGroup, item, recipeType, recipe, settings);
    }

    @Override
    protected float getConnectionRadius() {
        return 0.50F;
    }
    @Override
    protected void initDisplays(@NotNull final DisplayGroup displayGroup, @NotNull final Location location, @NotNull final Player player) {
        final BlockFace face = TransformationUtils.yawToFace(player.getEyeLocation().getYaw());
        displayGroup.addDisplay("main", new BlockDisplayBuilder(location.toCenterLocation())
                .setMaterial(Material.RED_STAINED_GLASS)
                .setTransformation(new TransformationMatrixBuilder()
                        .scale(GLASS_DISPLAY_SIZE)
                        .rotate(TransformationUtils.PRISM_ROTATION)
                        .buildForBlockDisplay())
                .build());
        displayGroup.addDisplay("concrete", new BlockDisplayBuilder(location.toCenterLocation())
                .setMaterial(settings.getTier().concreteMaterial)
                .setBrightness(Utils.BRIGHTNESS_ON)
                .setTransformation(new TransformationMatrixBuilder()
                        .scale(CONCRETE_DISPLAY_SIZE)
                        .translate(CONCRETE_OFFSET)
                        .buildForBlockDisplay())
                .build());
        final BlockDisplay repeater = new BlockDisplayBuilder(location.toCenterLocation())
                .setMaterial(Material.REPEATER)
                .setBlockData(createRepeaterBlockData(face.name().toLowerCase(), false))
                .setTransformation(new TransformationMatrixBuilder()
                        .scale(REPEATER_DISPLAY_SIZE)
                        .translate(REPEATER_OFFSET)
                        .buildForBlockDisplay())
                .build();
        PersistentDataAPI.setString(repeater, Keys.FACING, face.name().toLowerCase());
        displayGroup.addDisplay("repeater", repeater);
    }
    @Override
    protected List<ConnectionPoint> initConnectionPoints(final ConnectionGroupId groupId, final Player player, final Location location) {
        return List.of(
                new ConnectionPoint(ConnectionPointType.INPUT, groupId, "input", formatPointLocation(player, location, inputPointLocation)),
                new ConnectionPoint(ConnectionPointType.OUTPUT, groupId, "output", formatPointLocation(player, location, outputPointLocation)));
    }

    @Override
    public void onInputLinkUpdated(@NotNull final ConnectionGroup group, @NotNull final Location location) {
        if (doBurnoutCheck(group, "input")) {
            return;
        }

        final Optional<Link> inputLink = getLink(location, "input");
        final Optional<Link> outputLink = getLink(location, "output");
        onPoweredAnimation(location, settings.isOperational(inputLink));
        if (outputLink.isEmpty()) {
            return;
        }

        if (inputLink.isEmpty() || !settings.isOperational(inputLink.get())) {
            outputLink.get().disable();
            return;
        }

        outputLink.get().setPowerFrequencyPhase(
                PowerLossBlock.calculatePowerLoss(settings, inputLink.get()),
                calculateFrequency(settings, inputLink.get().getFrequency()),
                inputLink.get().getPhase());
    }
    @Override
    public void onPoweredAnimation(final Location location, final boolean powered) {
        final Optional<BlockDisplay> blockDisplay = getBlockDisplay(location, "repeater");
        blockDisplay.ifPresent(display -> display.setBlock(createRepeaterBlockData(PersistentDataAPI.getString(display, Keys.FACING), powered)));
    }

    private static double calculateFrequency(@NotNull final Settings settings, final double frequency) {
        return frequency + settings.getFrequencyStep();
    }

    private @NotNull BlockData createRepeaterBlockData(@NotNull final String facing, final boolean powered) {
        return Material.REPEATER.createBlockData("[delay=" + settings.getRepeaterDelay() + ",facing=" + facing + ",powered=" + Objects.toString(powered) + "]");
    }
}
