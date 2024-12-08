package org.metamechanists.quaptics.implementation.multiblocks.beacons.controllers;

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
import org.metamechanists.displaymodellib.sefilib.entity.display.DisplayGroup;
import org.metamechanists.quaptics.implementation.Settings;
import org.metamechanists.quaptics.items.Lore;
import org.metamechanists.quaptics.items.Tier;
import org.metamechanists.quaptics.storage.PersistentDataTraverser;
import org.metamechanists.quaptics.utils.Keys;
import org.metamechanists.quaptics.utils.Utils;
import org.metamechanists.quaptics.utils.id.complex.ConnectionGroupId;
import org.metamechanists.quaptics.utils.id.simple.InteractionId;
import org.metamechanists.displaymodellib.models.ModelBuilder;
import org.metamechanists.displaymodellib.models.components.ModelCuboid;
import org.metamechanists.displaymodellib.models.components.ModelItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.metamechanists.quaptics.implementation.multiblocks.beacons.components.BeaconBeam.BEACON_BEAM;
import static org.metamechanists.quaptics.implementation.multiblocks.beacons.components.BeaconComputer.BEACON_COMPUTER;
import static org.metamechanists.quaptics.implementation.multiblocks.beacons.components.BeaconPanel.BEACON_PANEL;
import static org.metamechanists.quaptics.implementation.multiblocks.beacons.components.BeaconPowerSupply.BEACON_POWER_SUPPLY_1;
import static org.metamechanists.quaptics.implementation.multiblocks.beacons.components.BeaconTransmitter.BEACON_TRANSMITTER;


public class BeaconController1 extends BeaconController {
    public static final Settings BEACON_CONTROLLER_1_SETTINGS = Settings.builder()
            .tier(Tier.INTERMEDIATE)
            .minPower(700)
            .minFrequency(900)
            .range(15)
            .build();

    public static final SlimefunItemStack BEACON_CONTROLLER_1 = new SlimefunItemStack(
            "QP_BEACON_CONTROLLER_1",
            Material.BLUE_CONCRETE,
            "&dBeacon Controller &5I",
            Lore.create(BEACON_CONTROLLER_1_SETTINGS,
                    Lore.multiblock(),
                    "&7● Applies module effects in a range around it",
                    "&7● &eRight Click &7a module slot with a module to insert"));

    private static final Vector COMPUTER_LOCATION = new Vector(0, 2, 0);
    private static final Vector POWER_SUPPLY_LOCATION = new Vector(0, -1, 0);

    private static final Vector3f MODULE_1_LOCATION = new Vector3f(0, -0.15F, 0.23F);
    private static final Vector3f MODULE_2_LOCATION = new Vector3f(0, -0.15F, -0.23F);

    public BeaconController1(final ItemGroup itemGroup, final SlimefunItemStack item, final RecipeType recipeType, final ItemStack[] recipe, final Settings settings) {
        super(itemGroup, item, recipeType, recipe, settings);
    }

    @Override
    protected org.metamechanists.displaymodellib.sefilib.entity.display.DisplayGroup initModel(@NotNull final Location location, @NotNull final Player player) {
        final DisplayGroup displayGroup = new ModelBuilder()
                .add("main", new ModelCuboid()
                        .material(Material.BLUE_CONCRETE)
                        .size(0.5F, 1.0F, 0.5F))

                .add("module1", new ModelItem()
                        .item(getEmptyItemStack())
                        .brightness(Utils.BRIGHTNESS_ON)
                        .size(0.25F)
                        .location(MODULE_1_LOCATION)
                        .rotation(0))
                .add("module2", new ModelItem()
                        .item(getEmptyItemStack())
                        .brightness(Utils.BRIGHTNESS_ON)
                        .size(0.25F)
                        .location(MODULE_2_LOCATION)
                        .rotation(Math.PI))

                .buildAtBlockCenter(location);

        final ConnectionGroupId groupId = new ConnectionGroupId(displayGroup.getParentUUID());
        final List<InteractionId> interactionIds = new ArrayList<>();

        interactionIds.add(createButton(groupId, location, MODULE_1_LOCATION, "module1"));
        interactionIds.add(createButton(groupId, location, MODULE_2_LOCATION, "module2"));

        final PersistentDataTraverser traverser = new PersistentDataTraverser(displayGroup.getParentUUID());
        traverser.setCustomIdList(Keys.BS_INTERACTION_ID_LIST, interactionIds);

        return displayGroup;
    }

    @Override
    protected boolean onRightClick(final @NotNull Location location, final @NotNull Player player) {
        multiblockInteract(location.getBlock(), player);
        return true;
    }
    @Override
    public void onPoweredAnimation(final @NotNull Location location, final boolean powered) {
        brightnessAnimation(location.clone().add(COMPUTER_LOCATION), "main", powered);
        brightnessAnimation(location.clone().add(POWER_SUPPLY_LOCATION), "panel1", powered);
        brightnessAnimation(location.clone().add(POWER_SUPPLY_LOCATION), "panel2", powered);
    }

    @Override
    public Map<Vector, ItemStack> getStructure() {
        final Map<Vector, ItemStack> structure = new HashMap<>();

        structure.put(new Vector(0, -1, 0), BEACON_POWER_SUPPLY_1);

        structure.put(new Vector(0, 1, 0), BEACON_BEAM);
        structure.put(new Vector(1, 1, 0), BEACON_TRANSMITTER);
        structure.put(new Vector(-1, 1, 0), BEACON_TRANSMITTER);
        structure.put(new Vector(0, 1, 1), BEACON_TRANSMITTER);
        structure.put(new Vector(0, 1, -1), BEACON_TRANSMITTER);

        structure.put(new Vector(0, 2, 0), BEACON_COMPUTER);
        structure.put(new Vector(1, 2, 0), BEACON_PANEL);
        structure.put(new Vector(-1, 2, 0), BEACON_PANEL);
        structure.put(new Vector(0, 2, 1), BEACON_PANEL);
        structure.put(new Vector(0, 2, -1), BEACON_PANEL);

        structure.put(new Vector(0, 3, 0), BEACON_BEAM);

        structure.put(new Vector(0, 4, 0), BEACON_TRANSMITTER);

        return structure;
    }
    @Override
    public Vector getPowerSupplyLocation() {
        return POWER_SUPPLY_LOCATION;
    }
    @Override
    protected List<String> getModuleDisplayNames() {
        return List.of("module1", "module2");
    }
}