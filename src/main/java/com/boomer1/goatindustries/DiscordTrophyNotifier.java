package com.boomer1.goatindustries;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public final class DiscordTrophyNotifier {

    private final JavaPlugin plugin;
    private final HttpClient httpClient;
    private final URI webhookUri;
    private final String serverName;

    public DiscordTrophyNotifier(JavaPlugin plugin) {
        this.plugin = plugin;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
        this.serverName = plugin.getConfig().getString("discord.server-name", "G.O.A.T. Industries");

        if (!plugin.getConfig().getBoolean("discord.enabled", false)) {
            this.webhookUri = null;
            plugin.getLogger().info("[G.O.A.T. Industries] Discord trophy notifications are disabled (server opt-in required).");
            return;
        }

        String configuredUrl = plugin.getConfig().getString("discord.webhook-url", "").trim();
        URI parsedUri = null;
        try {
            URI candidate = URI.create(configuredUrl);
            if ("https".equalsIgnoreCase(candidate.getScheme())
                && "discord.com".equalsIgnoreCase(candidate.getHost())
                && candidate.getPath() != null
                && candidate.getPath().startsWith("/api/webhooks/")
                && candidate.getUserInfo() == null) {
                parsedUri = candidate;
            }
        } catch (IllegalArgumentException ignored) {
            // The URL is deliberately never included in logs.
        }

        this.webhookUri = parsedUri;
        if (this.webhookUri == null) {
            plugin.getLogger().warning("[G.O.A.T. Industries] Discord notifications are enabled, but the private webhook URL is missing or invalid. Notifications will not be sent.");
        } else {
            plugin.getLogger().info("[G.O.A.T. Industries] Private Discord trophy notifications are enabled.");
        }
    }

    public void notifyTrophyCrafted(String playerName, UUID playerId) {
        if (webhookUri == null || !plugin.isEnabled()) {
            return;
        }

        long timestamp = Instant.now().getEpochSecond();
        String content = "🏆 **G.O.A.T. Trophy successfully crafted!**"
            + "\nPlayer: " + playerName
            + "\nPlayer UUID: " + playerId
            + "\nServer: " + serverName
            + "\nTime: <t:" + timestamp + ":F>";

        String payload = "{\"content\":\"" + escapeJson(content)
            + "\",\"allowed_mentions\":{\"parse\":[]}}";

        HttpRequest request = HttpRequest.newBuilder(webhookUri)
            .timeout(Duration.ofSeconds(15))
            .header("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(payload, StandardCharsets.UTF_8))
            .build();

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
                if (response.statusCode() < 200 || response.statusCode() >= 300) {
                    plugin.getLogger().warning("[G.O.A.T. Industries] Discord trophy notification failed (HTTP "
                        + response.statusCode() + ").");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                plugin.getLogger().log(Level.WARNING,
                    "[G.O.A.T. Industries] Discord trophy notification was interrupted.", exception);
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING,
                    "[G.O.A.T. Industries] Could not send the Discord trophy notification.", exception);
            }
        });
    }

    private String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder(value.length() + 16);
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            switch (character) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (character < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) character));
                    } else {
                        escaped.append(character);
                    }
                }
            }
        }
        return escaped.toString();
    }
}
