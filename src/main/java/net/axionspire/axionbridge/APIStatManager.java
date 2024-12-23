package net.axionspire.axionbridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class APIStatManager {
    private static APIStatManager instance;
    private AxionBridge plugin;

    private List<RegisteredStatProvider> registeredProviders = new ArrayList<>();
    private List<APIStat> registeredStats = new ArrayList<>();

    public static APIStatManager getInstance() {
        if (instance == null) {
            instance = new APIStatManager();
        }
        return instance;
    }

    void setPlugin(AxionBridge plugin) {
        this.plugin = plugin;
    }

    public void registerStatProvider(RegisteredStatProvider provider) {
        registeredProviders.add(provider);
        plugin.getLogger().info("Registered stat provider: " + provider.getMethodID());
    }

    // TODO: Verify that attributes of each stat are valid
    void loadAPIStats() {
        Set<String> stats = plugin.statsConfig.getKeys(false);
        for (String statID : stats) {
            String source = plugin.statsConfig.getString(statID + ".source");
            String checkedPlayers = plugin.statsConfig.getString(statID + ".check");
            APIStat stat = null;
            try {
                if (Objects.equals(source, "PLACEHOLDERAPI")) {
                    stat = new APIStat(statID, APIStat.ResolveMethod.PLACEHOLDERAPI, APIStat.CheckedPlayers.valueOf(checkedPlayers), plugin.statsConfig.getString(statID + ".placeholder"), null);
                } else if (Objects.equals(source, "registered")) {
                    String registeredStatProvider = plugin.statsConfig.getString(statID + ".call");
                    for (RegisteredStatProvider provider : registeredProviders) {
                        if (provider.getMethodID().equals(registeredStatProvider)) {
                            stat = new APIStat(statID, APIStat.ResolveMethod.REGISTERED, APIStat.CheckedPlayers.valueOf(checkedPlayers), null, provider);
                        }
                    }
                    if (stat == null) {
                        plugin.getLogger().warning("No registered stat provider matches stat '" + statID + "', which requests '" + registeredStatProvider + "', skipping...");
                        continue;
                    }
                } else  {
                    plugin.getLogger().warning("Unknown stat source '" + source + "' for stat '" + statID + "', skipping...");
                    continue;
                }
                registeredStats.add(stat);
                plugin.getLogger().info("Registered stat '" + statID + "' successfully.");
            } catch (IllegalArgumentException e) {
                plugin.getLogger().severe("Checked players for stat '" + statID + "' is invalid, skipping...");
                e.printStackTrace();
            }
        }
    }

    void pullStats() {
        for (APIStat stat : registeredStats) {
            // TODO: Pull stat data
        }
    }
}
