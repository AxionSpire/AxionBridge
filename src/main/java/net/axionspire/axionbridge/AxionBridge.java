package net.axionspire.axionbridge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public final class AxionBridge extends JavaPlugin {
    // Extra Files
    YamlConfiguration statsConfig;
    // Actual "File" objects (used for reloading)
    private File statsConfigFile;

    @Override
    public void onEnable() {
        // Configs
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        statsConfigFile = new File(getDataFolder(), "data.yml");
        if (!statsConfigFile.exists()) {
            saveResource("stats.yml", false);
        }
        reloadStatsConfig();

        getServer().getPluginManager().registerEvents(new BridgeListener(this), this);
        BridgeTools.getInstance().setPlugin(this);
        BridgeTools.getInstance().checkConfig();
        // Register base stat providers
        APIStatManager.getInstance().setPlugin(this);
        APIStatManager.getInstance().registerStatProvider(new PlayerEXPStat());

        try {
            BridgeTools.getInstance().checkForUpdates();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        getServer().getScheduler().scheduleSyncDelayedTask(this, this::testConnection, 40L);
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> { APIStatManager.getInstance().loadAPIStats(); }, 60L);

        long statTimer = getConfig().getInt("StatTimer") * 20L;
        getServer().getScheduler().scheduleSyncRepeatingTask(this, () -> { APIStatManager.getInstance().pullStats(); }, statTimer, statTimer);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    void testConnection() {
        HttpRequest check = HttpRequest.newBuilder()
                .GET()
                .uri(BridgeTools.getInstance().getAPIURL())
                .header("Content-Type", "application/json")
                .header("User-Agent", "AxionBridge/" + getDescription().getVersion() + " (Java/" + System.getProperty("java.version") + ")")
                .header("Authorization", "Bearer " + BridgeTools.getInstance().getAPIKey())
                .build();
        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(check, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            getLogger().severe("Failed to contact the AxionSpire API server '" +  BridgeTools.getInstance().getAPIURL() + "'.");
            e.printStackTrace();
            return;
        }
        assert response != null;
        String json = response.body();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = mapper.readTree(json);
        } catch (JsonProcessingException e) {
            getLogger().severe("Failed to parse the response from the API server '" +  BridgeTools.getInstance().getAPIURL() + "'.");
            e.printStackTrace();
            return;
        }
        String serverVersion = jsonNode.get("version").asText();
        String serverName = jsonNode.get("server").asText();
        boolean authValid = jsonNode.get("auth_valid").asBoolean();
        if (!Objects.equals(serverName, "AxionSpire API")) {
            getLogger().severe("The server provided (" + BridgeTools.getInstance().getAPIURL() + ") does not appear to be an AxionSpire API server.");
            getLogger().info("Server name (from the API): " + serverName);
            return;
        }
        getLogger().info("Connected to the AxionSpire API server '" +  BridgeTools.getInstance().getAPIURL() + "' successfully.");
        getLogger().info("API Server version: v" + serverVersion);
        if (authValid) {
            getLogger().info("API Key is valid.");
        } else {
            getLogger().severe("API Key is invalid. Please check your key in the config file.");
        }
    }

    public void reloadStatsConfig() {
        statsConfig = YamlConfiguration.loadConfiguration(statsConfigFile);
    }
    public void saveStatsConfig() {
        try {
            statsConfig.save(new File(getDataFolder(), "stats.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}