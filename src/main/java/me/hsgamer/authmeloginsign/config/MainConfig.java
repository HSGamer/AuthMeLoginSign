package me.hsgamer.authmeloginsign.config;

import me.hsgamer.hscore.bukkit.config.BukkitConfig;
import me.hsgamer.hscore.common.CollectionUtils;
import me.hsgamer.hscore.config.PathableConfig;
import me.hsgamer.hscore.config.path.BaseConfigPath;
import me.hsgamer.hscore.config.path.ConfigPath;
import me.hsgamer.hscore.config.path.impl.BooleanConfigPath;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;

public class MainConfig extends PathableConfig {
    public static final ConfigPath<List<String>> LINES = new BaseConfigPath<>("lines", Arrays.asList("%password%", "^", "|", "&c&lType password above"), o -> CollectionUtils.createStringListFromObject(o, false));
    public static final BooleanConfigPath FORCE_SIGN = new BooleanConfigPath("force-sign", false);

    public MainConfig(Plugin plugin) {
        super(new BukkitConfig(plugin, "config.yml"));
    }
}
