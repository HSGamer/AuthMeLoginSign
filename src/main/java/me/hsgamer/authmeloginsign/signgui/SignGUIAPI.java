package me.hsgamer.authmeloginsign.signgui;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.cryptomorin.xseries.XBlock;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XTag;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SignGUIAPI {
    private final SignCompleteHandler action;
    private final List<String> lines;
    private final Plugin plugin;
    private final UUID uuid;
    private PacketAdapter packetListener;
    private LeaveListener listener;
    private Sign sign;

    @Builder
    public SignGUIAPI(SignCompleteHandler action, List<String> withLines, UUID uuid, Plugin plugin) {
        this.lines = withLines;
        this.plugin = plugin;
        this.action = action;
        this.uuid = uuid;
    }

    public void open() {
        Player player = Bukkit.getPlayer(uuid);

        if (player == null) return;

        this.listener = new LeaveListener();

        int xStart = player.getLocation().getBlockX();

        int yStart = 255;

        int zStart = player.getLocation().getBlockZ();

        while (!XTag.AIR.isTagged(XMaterial.matchXMaterial(player.getWorld().getBlockAt(xStart, yStart, zStart).getType()))
                && !XTag.WALL_SIGNS.isTagged(XMaterial.matchXMaterial(player.getWorld().getBlockAt(xStart, yStart, zStart).getType()))) {
            yStart--;
            if (yStart == 1)
                return;
        }
        XBlock.setType(player.getWorld().getBlockAt(xStart, yStart, zStart), XMaterial.OAK_WALL_SIGN);

        this.sign = (Sign) player.getWorld().getBlockAt(xStart, yStart, zStart).getState();

        int i = 0;
        for (String line : lines) {
            this.sign.setLine(i, line);
            i++;
        }

        this.sign.update(false, false);


        PacketContainer openSign = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.OPEN_SIGN_EDITOR);

        BlockPosition position = new BlockPosition(xStart, yStart, zStart);

        openSign.getBlockPositionModifier().write(0, position);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, openSign);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 3L);

        Bukkit.getPluginManager().registerEvents(this.listener, plugin);
        registerSignUpdateListener();
    }

    private void registerSignUpdateListener() {
        final ProtocolManager manager = ProtocolLibrary.getProtocolManager();
        this.packetListener = new PacketAdapter(plugin, PacketType.Play.Client.UPDATE_SIGN) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                if (event.getPlayer().getUniqueId().equals(SignGUIAPI.this.uuid)) {
                    List<String> signLines = Stream.of(0, 1, 2, 3).map(line -> getLine(event, line)).collect(Collectors.toList());

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        manager.removePacketListener(this);

                        HandlerList.unregisterAll(SignGUIAPI.this.listener);

                        SignGUIAPI.this.sign.getBlock().setType(Material.AIR);

                        SignGUIAPI.this.action.onSignClose(new SignCompletedEvent(event.getPlayer(), signLines));
                    });
                }
            }
        };
        manager.addPacketListener(this.packetListener);
    }

    private String getLine(PacketEvent event, int line) {
        return Bukkit.getVersion().contains("1.8") ?
                (event.getPacket().getChatComponentArrays().read(0))[line].getJson().replace("\"", "") :
                (event.getPacket().getStringArrays().read(0))[line];
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    private class LeaveListener implements Listener {
        @EventHandler
        public void onLeave(PlayerQuitEvent e) {
            if (e.getPlayer().getUniqueId().equals(SignGUIAPI.this.uuid)) {
                ProtocolLibrary.getProtocolManager().removePacketListener(SignGUIAPI.this.packetListener);
                HandlerList.unregisterAll(this);
                SignGUIAPI.this.sign.getBlock().setType(Material.AIR);
            }
        }
    }
}
