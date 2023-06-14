package org.metamechanists.death_lasers.connections;

import org.bukkit.Location;
import org.metamechanists.death_lasers.connections.ConnectionPointGroup;

import java.util.HashMap;
import java.util.Map;

public class ConnectionPointStorage {
    private static final Map<Location, ConnectionPointGroup> blockLocationToGroupMap = new HashMap<>();
    private static final Map<Location, Location> connectionPointLocationToGroupMap = new HashMap<>();

    public static void addConnectionPointGroup(Location blockLocation, ConnectionPointGroup group) {
        blockLocationToGroupMap.put(blockLocation, group);
        for (Location connectionPointLocation : group.getConnectionPointLocations()) {
            connectionPointLocationToGroupMap.put(connectionPointLocation, blockLocation);
        }
    }

    public static void removeConnectionPointGroup(Location blockLocation) {
        final ConnectionPointGroup group = blockLocationToGroupMap.remove(blockLocation);
        for (Location connectionPointLocation : group.getConnectionPointLocations()) {
            group.removeAllConnectionPoints();
            connectionPointLocationToGroupMap.remove(connectionPointLocation);
        }
    }

    public static void removeAllConnectionPoints() {
        for (ConnectionPointGroup group : blockLocationToGroupMap.values()) {
            group.removeAllConnectionPoints();
        }
    }

    public static Location getBlockLocationFromConnectionPointLocation(Location location) {
        return connectionPointLocationToGroupMap.get(location);
    }

    public static ConnectionPointGroup getConnectionGroupFromBlockLocation(Location location) {
        return blockLocationToGroupMap.get(location);
    }

    public static ConnectionPointGroup getConnectionGroupFromConnectionPointLocation(Location location) {
        final Location blockLocation = connectionPointLocationToGroupMap.get(location);
        return getConnectionGroupFromBlockLocation(blockLocation);
    }
}