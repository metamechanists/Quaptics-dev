package org.metamechanists.quaptics.implementation.blocks.concentrators;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.displaymodellib.models.ModelBuilder;
import org.metamechanists.displaymodellib.models.components.ModelCuboid;
import org.metamechanists.displaymodellib.sefilib.entity.display.DisplayGroup;
import org.metamechanists.displaymodellib.transformations.TransformationUtils;
import org.metamechanists.quaptics.connections.ConnectionGroup;
import org.metamechanists.quaptics.connections.ConnectionPoint;
import org.metamechanists.quaptics.connections.ConnectionPointType;
import org.metamechanists.quaptics.connections.Link;
import org.metamechanists.quaptics.implementation.Settings;
import org.metamechanists.quaptics.implementation.attachments.ConfigPanelBlock;
import org.metamechanists.quaptics.implementation.base.ConnectedBlock;
import org.metamechanists.quaptics.items.Lore;
import org.metamechanists.quaptics.panels.config.ConfigPanel;
import org.metamechanists.quaptics.panels.config.implementation.CreativeConcentratorConfigPanel;
import org.metamechanists.quaptics.utils.BlockStorageAPI;
import org.metamechanists.quaptics.utils.Colors;
import org.metamechanists.quaptics.utils.Keys;
import org.metamechanists.quaptics.utils.Utils;
import org.metamechanists.quaptics.utils.id.complex.ConfigPanelId;
import org.metamechanists.quaptics.utils.id.complex.ConnectionGroupId;

import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.List;
import java.util.Optional;

public class CreativeConcentrator extends ConnectedBlock implements ConfigPanelBlock {
    public static final SlimefunItemStack CREATIVE_CONCENTRATOR = new SlimefunItemStack(
            "QP_CREATIVE_CONCENTRATOR",
            Material.PURPLE_GLAZED_TERRACOTTA,
            "&dCreative Concentrator",
            Colors.CREATIVE.getFormattedColor() + "Creative",
            "&7● Concentrates &kwhatlol &7into a quaptic ray",
            "&7● Control the output with the panel (try shifting)",
            Lore.ATTRIBUTE_SYMBOL + Lore.POWER_SYMBOL + "&7Max Power Output &e∞" + Lore.POWER_SUFFIX,
            Lore.ATTRIBUTE_SYMBOL + Lore.FREQUENCY_SYMBOL + "&7Max Frequency Output &e∞" + Lore.FREQUENCY_SUFFIX,
            Lore.ATTRIBUTE_SYMBOL + Lore.PHASE_SYMBOL + "&7Max Phase Output &e∞" + Lore.PHASE_SUFFIX
    );

    private static final Vector RELATIVE_PANEL_LOCATION = new Vector(0, 0, -0.51);

    private final Vector outputLocation = new Vector(0.0F, 0.0F, getConnectionRadius());

    public CreativeConcentrator(final ItemGroup itemGroup, final SlimefunItemStack item) {
        super(itemGroup, item, RecipeType.NULL, new ItemStack[0], Settings.builder().build());
    }

    @Override
    protected float getConnectionRadius() {
        return 0.55F;
    }
    @Override
    protected DisplayGroup initModel(final @NotNull Location location, final @NotNull Player player) {
        return new ModelBuilder()
                .add("center", new ModelCuboid()
                        .material(Material.PURPLE_GLAZED_TERRACOTTA)
                        .brightness(Utils.BRIGHTNESS_ON)
                        .size(0.2F))
                .add("plate", new ModelCuboid()
                        .material(Material.GRAY_CONCRETE)
                        .rotation(Math.PI / 4)
                        .size(0.6F, 0.1F, 0.6F))
                .add("glass", new ModelCuboid()
                        .material(Material.TINTED_GLASS)
                        .rotation(Math.PI / 4)
                        .size(0.4F))
                .buildAtBlockCenter(location);
    }
    @Override
    public ConfigPanel createPanel(final Location location, final Player player, @NotNull final ConnectionGroup group) {
        return new CreativeConcentratorConfigPanel(formatPointLocation(player, location, RELATIVE_PANEL_LOCATION), group.getId(),
                (float) TransformationUtils.yawToCardinalDirection(player.getEyeLocation().getYaw()));
    }
    @Override
    public ConfigPanel getPanel(final ConfigPanelId panelId, final ConnectionGroupId groupId) {
        return new CreativeConcentratorConfigPanel(panelId, groupId);
    }
    @Override
    protected List<ConnectionPoint> initConnectionPoints(final ConnectionGroupId groupId, final Player player, final Location location) {
        return List.of(new ConnectionPoint(ConnectionPointType.OUTPUT, groupId, "output", formatPointLocation(player, location, outputLocation)));
    }
    @Override
    protected void initBlockStorage(@NotNull final Location location) {
        BlockStorageAPI.set(location, Keys.BS_OUTPUT_POWER, 0);
        BlockStorageAPI.set(location, Keys.BS_OUTPUT_FREQUENCY, 0);
        BlockStorageAPI.set(location, Keys.BS_OUTPUT_PHASE, 0);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onPlace(@NotNull final BlockPlaceEvent event) {
        super.onPlace(event);
        onPlaceConfigPanelBlock(event);
    }
    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onBreak(@NotNull final Location location) {
        super.onBreak(location);
        onBreakConfigPanelBlock(location);
    }

    @Override
    protected void onSlimefunTick(@NotNull final Block block, final SlimefunItem item, final Config data) {
        super.onSlimefunTick(block, item, data);
        final Location location = block.getLocation();

        final Optional<Link> linkOptional = getLink(location, "output");
        linkOptional.ifPresent(link -> {
            final double power = BlockStorageAPI.getDouble(location, Keys.BS_OUTPUT_POWER);
            final double frequency = BlockStorageAPI.getDouble(location, Keys.BS_OUTPUT_FREQUENCY);
            final int phase = BlockStorageAPI.getInt(location, Keys.BS_OUTPUT_PHASE);
            link.setPowerFrequencyPhase(power, frequency, phase);
        });
    }
}
