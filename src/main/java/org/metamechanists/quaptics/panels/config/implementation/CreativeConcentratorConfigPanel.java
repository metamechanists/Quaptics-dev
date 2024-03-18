package org.metamechanists.quaptics.panels.config.implementation;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.quaptics.connections.ConnectionGroup;
import org.metamechanists.quaptics.implementation.blocks.consumers.ItemProjector;
import org.metamechanists.quaptics.items.Lore;
import org.metamechanists.quaptics.panels.config.ConfigPanel;
import org.metamechanists.quaptics.panels.config.ConfigPanelBuilder;
import org.metamechanists.quaptics.panels.config.ConfigPanelContainer;
import org.metamechanists.quaptics.utils.BlockStorageAPI;
import org.metamechanists.quaptics.utils.Keys;
import org.metamechanists.quaptics.utils.Utils;
import org.metamechanists.quaptics.utils.id.complex.ConfigPanelId;
import org.metamechanists.quaptics.utils.id.complex.ConnectionGroupId;

import java.util.Objects;
import java.util.Optional;

public class CreativeConcentratorConfigPanel extends ConfigPanel {

    public CreativeConcentratorConfigPanel(@NotNull final Location location, final ConnectionGroupId groupId, final float rotationY) {
        super(groupId, location, rotationY);
    }

    public CreativeConcentratorConfigPanel(@NotNull final ConfigPanelId id, final ConnectionGroupId groupId) {
        super(id, groupId);
    }

    @Override
    protected ConfigPanelContainer buildPanelContainer(@NotNull final ConnectionGroupId groupId, @NotNull final Location location, final float rotationY) {
        return new ConfigPanelBuilder(groupId, location.clone().add(getOffset()), SIZE, rotationY)
                .addAttribute("power", Lore.POWER_SYMBOL + "&fPower")
                .addAttribute("frequency", Lore.FREQUENCY_SYMBOL + "&fFrequency")
                .addAttribute("phase", Lore.PHASE_SYMBOL + "&fPhase")
                .build();
    }

    @Override
    public void interact(@NotNull final Player player, @NotNull final Location location, final String name, final String type) {
        if ("power".equals(name)) {
            double power = BlockStorageAPI.getDouble(location, Keys.BS_OUTPUT_POWER);
            power += ("add".equals(type) ? 1 : -1) * (player.isSneaking() ? 100 : 1);
            power = Utils.clampToRange(power, 0, Double.MAX_VALUE);
            BlockStorageAPI.set(location, Keys.BS_OUTPUT_POWER, power);
        }

        if ("frequency".equals(name)) {
            double frequency = BlockStorageAPI.getDouble(location, Keys.BS_OUTPUT_FREQUENCY);
            frequency += ("add".equals(type) ? 1 : -1) * (player.isSneaking() ? 100 : 1);
            frequency = Utils.clampToRange(frequency, 0, Double.MAX_VALUE);
            BlockStorageAPI.set(location, Keys.BS_OUTPUT_FREQUENCY, frequency);
        }

        if ("phase".equals(name)) {
            int phase = BlockStorageAPI.getInt(location, Keys.BS_OUTPUT_PHASE);
            phase += ("add".equals(type) ? 1 : -1) * (player.isSneaking() ? 12 : 1);
            phase = Utils.clampToRange(phase, 0, 360);
            BlockStorageAPI.set(location, Keys.BS_OUTPUT_PHASE, phase);
        }

        ItemProjector.onConfigUpdated(location);
        update();
    }

    @Override
    protected void update() {
        if (isPanelHidden()) {
            return;
        }

        final Optional<ConnectionGroup> group = getGroup();
        if (group.isEmpty()) {
            return;
        }

        final Optional<Location> location = group.get().getLocation();
        if (location.isEmpty()) {
            return;
        }

        final double power = BlockStorageAPI.getDouble(location.get(), Keys.BS_OUTPUT_POWER);
        final double frequency = BlockStorageAPI.getDouble(location.get(), Keys.BS_OUTPUT_FREQUENCY);
        final int phase = BlockStorageAPI.getInt(location.get(), Keys.BS_OUTPUT_PHASE);

        container.setValue("power", Objects.toString(power));
        container.setValue("frequency", Objects.toString(frequency));
        container.setValue("phase", Objects.toString(phase));
    }

    @SuppressWarnings("MagicNumber")
    @Override
    protected Vector getOffset() {
        return new Vector(0.0, -0.3, 0.0);
    }
}
