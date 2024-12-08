package org.metamechanists.quaptics.panels.config;

import org.metamechanists.displaymodellib.sefilib.entity.display.DisplayGroup;
import io.github.bakedlibs.dough.common.ChatColors;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.metamechanists.quaptics.storage.PersistentDataTraverser;
import org.metamechanists.displaymodellib.builders.InteractionBuilder;
import org.metamechanists.quaptics.utils.id.complex.ConfigPanelAttributeId;
import org.metamechanists.quaptics.utils.id.complex.ConnectionGroupId;
import org.metamechanists.quaptics.utils.id.simple.DisplayGroupId;
import org.metamechanists.quaptics.utils.id.simple.InteractionId;
import org.metamechanists.displaymodellib.models.ModelBuilder;
import org.metamechanists.displaymodellib.models.components.ModelText;

import java.util.Optional;

public class ConfigPanelAttribute {
    private static final float BUTTON_SIZE = 0.08F;
    private static final Vector BUTTON_ADJUSTMENT = new Vector(BUTTON_SIZE/2, BUTTON_SIZE/2, BUTTON_SIZE/2);
    private static final float HIDDEN_VIEW_RANGE = 0;
    private static final float SHOWN_VIEW_RANGE = 1;
    private static final double OFFSET_Y = 0.15;
    private static final double OFFSET_Z = 0.15;
    private final Vector offset;
    private final DisplayGroupId displayGroupId;
    private final InteractionId subButtonId;
    private final InteractionId addButtonId;

    public ConfigPanelAttribute(final ConnectionGroupId groupId, @NotNull final String name, @NotNull final String key,
                                final @NotNull Location location, final Vector offset, final @NotNull Vector3d rotation, final float size) {
        final Vector subButtonLocation = new Vector(0.04, 0, -0.08).add(offset).add(BUTTON_ADJUSTMENT).rotateAroundY(rotation.y);
        final Vector addButtonLocation = new Vector(0.41, 0, -0.08).add(offset).add(BUTTON_ADJUSTMENT).rotateAroundY(rotation.y);

        this.displayGroupId = new DisplayGroupId(new ModelBuilder()
                .add("key", new ModelText()
                        .text(ChatColors.color(key))
                        .brightness(15)
                        .background(Color.fromARGB(0, 0, 0, 0))
                        .size(size)
                        .location(new Vector(-0.06, OFFSET_Y, OFFSET_Z).rotateAroundY(rotation.y).toVector3f())
                        .rotation(rotation))
                .add("value", new ModelText()
                        .brightness(15)
                        .background(Color.fromARGB(0, 0, 0, 0))
                        .size(size)
                        .location(new Vector(0.41, OFFSET_Y, OFFSET_Z).rotateAroundY(rotation.y).toVector3f())
                        .rotation(rotation))
                .add("sub", new ModelText()
                        .text(ChatColors.color("&c-"))
                        .brightness(15)
                        .background(Color.fromARGB(0, 0, 0, 0))
                        .size(size)
                        .location(new Vector(0.22, OFFSET_Y, OFFSET_Z).rotateAroundY(rotation.y).toVector3f())
                        .rotation(rotation))
                .add("add", new ModelText()
                        .text(ChatColors.color("&a+"))
                        .brightness(15)
                        .background(Color.fromARGB(0, 0, 0, 0))
                        .size(size)
                        .location(new Vector(0.59, OFFSET_Y, OFFSET_Z).rotateAroundY(rotation.y).toVector3f())
                        .rotation(rotation))
                .buildAtLocation(location.clone().add(offset))
                .getParentUUID());

        final Interaction subButton = new InteractionBuilder()
                .width(BUTTON_SIZE)
                .height(BUTTON_SIZE)
                .build(location.clone().add(subButtonLocation));
        final Interaction addButton = new InteractionBuilder()
                .width(BUTTON_SIZE)
                .height(BUTTON_SIZE)
                .build(location.clone().add(addButtonLocation));

        final PersistentDataTraverser subButtonTraverser = new PersistentDataTraverser(subButton.getUniqueId());
        subButtonTraverser.set("groupId", groupId);
        subButtonTraverser.set("name", name);
        subButtonTraverser.set("buttonType", "sub");

        final PersistentDataTraverser addButtonTraverser = new PersistentDataTraverser(addButton.getUniqueId());
        addButtonTraverser.set("groupId", groupId);
        addButtonTraverser.set("name", name);
        addButtonTraverser.set("buttonType", "add");

        this.subButtonId = new InteractionId(subButton.getUniqueId());
        this.addButtonId = new InteractionId(addButton.getUniqueId());
        this.offset = offset;

        saveData();
    }
    public ConfigPanelAttribute(final ConfigPanelAttributeId displayGroupId) {
        final PersistentDataTraverser traverser = new PersistentDataTraverser(displayGroupId);

        this.displayGroupId = new DisplayGroupId(displayGroupId);
        this.offset = traverser.getVector("offset");
        this.subButtonId = traverser.getInteractionId("subButtonId");
        this.addButtonId = traverser.getInteractionId("addButtonId");
    }

    private void saveData() {
        final PersistentDataTraverser traverser = new PersistentDataTraverser(displayGroupId);
        traverser.set("offset", offset);
        traverser.set("subButtonId", subButtonId);
        traverser.set("addButtonId", addButtonId);
    }

    public ConfigPanelAttributeId getId() {
        return new ConfigPanelAttributeId(displayGroupId);
    }
    private Optional<DisplayGroup> getDisplayGroup() {
        return displayGroupId.get().isPresent()
                ? displayGroupId.get()
                : Optional.empty();
    }
    private Optional<TextDisplay> getValue() {
        return getDisplayGroup().isPresent() && getDisplayGroup().get().getDisplays().get("value") instanceof final TextDisplay textDisplay
                ? Optional.of(textDisplay)
                : Optional.empty();
    }
    private Optional<Interaction> getAddButton() {
        return addButtonId.get().isPresent()
                ? addButtonId.get()
                : Optional.empty();
    }
    private Optional<Interaction> getSubButton() {
        return subButtonId.get().isPresent()
                ? subButtonId.get()
                : Optional.empty();
    }

    public void setValue(@NotNull final String text) {
        getValue().ifPresent(value -> value.setText(ChatColors.color(text)));
    }

    public void setHidden(final boolean hidden) {
        displayGroupId.get().ifPresent(
                group -> group.getDisplays().values().forEach(
                        display -> display.setViewRange(hidden ? HIDDEN_VIEW_RANGE : SHOWN_VIEW_RANGE)));
    }

    public void remove() {
        displayGroupId.get().ifPresent(group -> {
            group.getDisplays().values().forEach(Entity::remove);
            group.remove();
        });
        getAddButton().ifPresent(Entity::remove);
        getSubButton().ifPresent(Entity::remove);
    }
}
