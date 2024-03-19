package org.metamechanists.quaptics.connections;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.metamechanists.quaptics.items.Groups;
import org.metamechanists.quaptics.panels.info.implementation.PointInfoPanel;
import org.metamechanists.quaptics.implementation.base.ConnectedBlock;
import org.metamechanists.quaptics.storage.PersistentDataTraverser;
import org.metamechanists.quaptics.storage.QuapticStorage;
import org.metamechanists.quaptics.utils.id.complex.ConnectionGroupId;
import org.metamechanists.quaptics.utils.id.complex.ConnectionPointId;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ConnectionGroup {
    @Getter
    private final ConnectionGroupId id;
    private final String blockId;
    @Getter
    private final Map<String, ConnectionPointId> points;
    private final boolean isTicker;

    public ConnectionGroup(final ConnectionGroupId id, @NotNull final ConnectedBlock block, @NotNull final Iterable<ConnectionPoint> pointsIn, final boolean isTicker) {
        this.id = id;
        this.blockId = block.getId();
        this.points = new HashMap<>();
        this.isTicker = isTicker;
        pointsIn.forEach(point -> points.put(point.getName(), point.getId()));
        saveData();
    }
    public ConnectionGroup(final ConnectionGroupId id) {
        final PersistentDataTraverser traverser = new PersistentDataTraverser(id);
        this.id = id;
        this.blockId = traverser.getString("blockId");
        this.points = traverser.getPointIdMap("points");
        this.isTicker = traverser.getBoolean("isTicker");
    }

    private void saveData() {
        final PersistentDataTraverser traverser = new PersistentDataTraverser(id);
        traverser.set("blockId", blockId);
        traverser.set("points", points);
        traverser.set("isTicker", isTicker);
    }

    public Optional<ConnectionPoint> getPoint(final String name) {
        return points.get(name).get();
    }
    public ConnectionPoint addPoint(@NotNull final ConnectionPoint point) {
        points.put(point.getName(), point.getId());
        saveData();
        return point;
    }
    public Optional<ConnectionPoint> removePoint(@NotNull final ConnectionPoint point) {
        return removePoint(point.getName());
    }
    public Optional<ConnectionPoint> removePoint(@NotNull final String name) {
        ConnectionPointId point = points.remove(name);
        saveData();
        return point == null ? Optional.empty() : point.get();
    }
    public ConnectedBlock getBlock() {
        return Groups.getBlocks().get(blockId);
    }
    public Optional<Location> getLocation() {
        // The ConnectionGroupId shares the UUID of the main interaction entity
        return Optional.ofNullable(Bukkit.getEntity(id.getUUID())).map(Entity::getLocation);
    }
    public List<ConnectionPoint> getPointList() {
        return points.values().stream()
                .map(ConnectionPointId::get)
                .filter(Optional::isPresent)
                .map(Optional::get).toList();
    }
    public List<PointInfoPanel> getPointPanels() {
        return getPointList().stream()
                .map(ConnectionPoint::getPointPanel)
                .filter(Optional::isPresent)
                .map(Optional::get).toList();
    }

    public void tick2() {
        final Optional<Location> locationOptional = getLocation();
        locationOptional.ifPresent(location -> getBlock().onTick2(this, location));
    }
    public void tick6() {
        final Optional<Location> locationOptional = getLocation();
        locationOptional.ifPresent(location -> getBlock().onTick6(this, location));
    }
    public void tick22() {
        final Optional<Location> locationOptional = getLocation();
        locationOptional.ifPresent(location -> getBlock().onTick22(this, location));
    }
    public void tick102() {
        final Optional<Location> locationOptional = getLocation();
        locationOptional.ifPresent(location -> getBlock().onTick102(this, location));
    }

    public void updatePanels() {
        getPointList().forEach(ConnectionPoint::updatePanel);
    }

    public void remove() {
        getPointList().forEach(ConnectionPoint::remove);
        QuapticStorage.removeGroup(id);
    }
}
