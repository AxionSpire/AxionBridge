package com.axionspire.axionbridge;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public final class AxionBridge extends JavaPlugin {
    @Override
    public void onEnable() {
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new BridgeListener(this), this);
        BridgeTools.getInstance().setPlugin(this);
        BridgeTools.getInstance().checkConfig();
        try {
            BridgeTools.getInstance().checkForUpdates();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        getServer().getScheduler().scheduleSyncDelayedTask(this, this::testConnection, 40L);
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
                .header("AxionBridge-Version", getDescription().getVersion())
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
        if (!Objects.equals(serverName, "AxionSpire API")) {
            getLogger().severe("The server provided (" + BridgeTools.getInstance().getAPIURL() + ") does not appear to be an AxionSpire API server.");
            getLogger().info("Server name (from the API): " + serverName);
            return;
        }
        getLogger().info("Connected to the AxionSpire API server '" +  BridgeTools.getInstance().getAPIURL() + "' successfully.");
        getLogger().info("API Server version: v" + serverVersion);
    }
}
