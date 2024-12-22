package com.axionspire.axionbridge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class AxionBridge extends JavaPlugin {
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new BridgeListener(this), this);
        BridgeTools.getInstance().setPlugin(this);
        try {
            BridgeTools.getInstance().checkForUpdates();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        getServer().getScheduler().scheduleSyncDelayedTask(this, () -> {
            try {
                testConnection();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }, 40L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    void testConnection() throws JsonProcessingException {
        HttpRequest check = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BridgeTools.getInstance().getAPIURL() + "/healthcheck"))
                .header("Content-Type", "application/json")
                .header("AxionBridge-Version", getDescription().getVersion())
                .build();
        HttpResponse<String> response = null;
        try {
            response = HttpClient.newHttpClient().send(check, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            getLogger().info("Failed to contact the AxionSpire API server '" +  BridgeTools.getInstance().getAPIURL() + "'.");
            e.printStackTrace();
        }
        assert response != null;
        String json = response.body();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);
        String serverVersion = jsonNode.get("version").asText();
        getLogger().info("Connected to the AxionSpire API server '" +  BridgeTools.getInstance().getAPIURL() + "' successfully.");
        getLogger().info("API Server version: " + serverVersion);
    }
}
