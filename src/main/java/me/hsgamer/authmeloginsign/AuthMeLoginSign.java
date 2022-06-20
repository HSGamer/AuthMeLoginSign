package me.hsgamer.authmeloginsign;

import fr.xephi.authme.api.v3.AuthMeApi;
import me.hsgamer.authmeloginsign.config.MainConfig;
import me.hsgamer.authmeloginsign.listener.PlayerListener;
import me.hsgamer.authmeloginsign.signgui.SignMenuFactory;
import me.hsgamer.hscore.bukkit.simpleplugin.SimplePlugin;
import me.hsgamer.hscore.bukkit.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public final class AuthMeLoginSign extends SimplePlugin {
    private final MainConfig mainConfig = new MainConfig(this);
    private AuthMeApi authMeApi;
    private SignMenuFactory.Menu menu;

    @Override
    public void load() {
        mainConfig.setup();
    }

    @Override
    public void enable() {
        authMeApi = AuthMeApi.getInstance();
        registerListener(new PlayerListener(this));
        setupMenu();
    }

    private void setupMenu() {
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

        SignMenuFactory factory = new SignMenuFactory(this);
        menu = factory.newMenu(lines)
                .reopenIfFail(Boolean.TRUE.equals(MainConfig.FORCE_SIGN.getValue()))
                .response((player, signLines) -> {
                    String password = ChatColor.stripColor(signLines[finalPasswordLineIndex]);
                    if (authMeApi.isRegistered(player.getName())) {
                        if (authMeApi.checkPassword(player.getName(), password)) {
                            authMeApi.forceLogin(player);
                        } else {
                            return false;
                        }
                    } else {
                        authMeApi.forceRegister(player, password, true);
                    }
                    return true;
                });
    }

    public void openGUI(Player player) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> menu.open(player));
    }
}
