package me.hsgamer.authmeloginsign.listener;

import me.hsgamer.authmeloginsign.AuthMeLoginSign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    private final AuthMeLoginSign plugin;

    public PlayerListener(AuthMeLoginSign plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.openGUI(event.getPlayer());
    }
}
