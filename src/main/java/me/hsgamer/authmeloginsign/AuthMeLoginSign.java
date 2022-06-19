package me.hsgamer.authmeloginsign;

import fr.xephi.authme.api.v3.AuthMeApi;
import me.hsgamer.authmeloginsign.config.MainConfig;
import me.hsgamer.authmeloginsign.listener.PlayerListener;
import me.hsgamer.authmeloginsign.signgui.SignGUIAPI;
import me.hsgamer.hscore.bukkit.simpleplugin.SimplePlugin;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public final class AuthMeLoginSign extends SimplePlugin {
    private final MainConfig mainConfig = new MainConfig(this);
    private AuthMeApi authMeApi;

    @Override
    public void load() {
        mainConfig.setup();
    }

    @Override
    public void enable() {
        authMeApi = AuthMeApi.getInstance();
        registerListener(new PlayerListener(this));
    }

    public void openGUI(Player player) {
        List<String> lines = MainConfig.LINES.getValue();
        lines.replaceAll(MessageUtils::colorize);
        int passwordLineIndex = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("%password%")) {
                passwordLineIndex = i;
                lines.set(i, line.replace("%password%", ""));
                break;
            }
        }

        int finalPasswordLineIndex = passwordLineIndex;
        SignGUIAPI.builder()
                .plugin(this)
                .withLines(lines)
                .action(event -> {
                    String password = ChatColor.stripColor(event.getLines().get(finalPasswordLineIndex));
                    if (authMeApi.isRegistered(player.getName())) {
                        if (authMeApi.checkPassword(player.getName(), password)) {
                            authMeApi.forceLogin(player);
                        } else if (Boolean.TRUE.equals(MainConfig.FORCE_SIGN.getValue())){
                            openGUI(player);
                        }
                    } else {
                        authMeApi.forceRegister(player, password, true);
                    }
                })
                .uuid(player.getUniqueId())
                .build()
                .open();
    }
}
