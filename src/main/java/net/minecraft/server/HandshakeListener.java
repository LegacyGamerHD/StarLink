package net.minecraft.server;

// CraftBukkit start
import java.net.InetAddress;
import java.util.HashMap;
// CraftBukkit end

public class HandshakeListener implements PacketHandshakingInListener {

    // CraftBukkit start - add fields
    private static final HashMap<InetAddress, Long> throttleTracker = new HashMap<InetAddress, Long>();
    private static int throttleCounter = 0;
    // CraftBukkit end

    private final MinecraftServer a;
    private final NetworkManager b;

    public HandshakeListener(MinecraftServer minecraftserver, NetworkManager networkmanager) {
        this.a = minecraftserver;
        this.b = networkmanager;
    }

    @Override
    public void a(PacketHandshakingInSetProtocol packethandshakinginsetprotocol) {
        switch (packethandshakinginsetprotocol.b()) {
            case LOGIN:
                this.b.setProtocol(EnumProtocol.LOGIN);
                ChatMessage chatmessage;

                // CraftBukkit start - Connection throttle
                try {
                    long currentTime = System.currentTimeMillis();
                    long connectionThrottle = MinecraftServer.getServer().server.getConnectionThrottle();
                    InetAddress address = ((java.net.InetSocketAddress) this.b.getSocketAddress()).getAddress();

                    synchronized (throttleTracker) {
                        if (throttleTracker.containsKey(address) && !"127.0.0.1".equals(address.getHostAddress()) && currentTime - throttleTracker.get(address) < connectionThrottle) {
                            throttleTracker.put(address, currentTime);
                            chatmessage = new ChatMessage("Connection throttled! Please wait before reconnecting.");
                            this.b.sendPacket(new PacketLoginOutDisconnect(chatmessage));
                            this.b.close(chatmessage);
                            return;
                        }

                        throttleTracker.put(address, currentTime);
                        throttleCounter++;
                        if (throttleCounter > 200) {
                            throttleCounter = 0;

                            // Cleanup stale entries
                            java.util.Iterator iter = throttleTracker.entrySet().iterator();
                            while (iter.hasNext()) {
                                java.util.Map.Entry<InetAddress, Long> entry = (java.util.Map.Entry) iter.next();
                                if (entry.getValue() > connectionThrottle) {
                                    iter.remove();
                                }
                            }
                        }
                    }
                } catch (Throwable t) {
                    org.apache.logging.log4j.LogManager.getLogger().debug("Failed to check connection throttle", t);
                }
                // CraftBukkit end

                if (packethandshakinginsetprotocol.c() > SharedConstants.getGameVersion().getProtocolVersion()) {
                    chatmessage = new ChatMessage( java.text.MessageFormat.format( org.spigotmc.SpigotConfig.outdatedServerMessage.replaceAll("'", "''"), SharedConstants.getGameVersion().getName() ) ); // Spigot
                    this.b.sendPacket(new PacketLoginOutDisconnect(chatmessage));
                    this.b.close(chatmessage);
                } else if (packethandshakinginsetprotocol.c() < SharedConstants.getGameVersion().getProtocolVersion()) {
                    chatmessage = new ChatMessage( java.text.MessageFormat.format( org.spigotmc.SpigotConfig.outdatedClientMessage.replaceAll("'", "''"), SharedConstants.getGameVersion().getName() ) ); // Spigot
                    this.b.sendPacket(new PacketLoginOutDisconnect(chatmessage));
                    this.b.close(chatmessage);
                } else {
                    this.b.setPacketListener(new LoginListener(this.a, this.b));
                    ((LoginListener) this.b.i()).hostname = packethandshakinginsetprotocol.hostname + ":" + packethandshakinginsetprotocol.port; // CraftBukkit - set hostname
                }
                break;
            case STATUS:
                this.b.setProtocol(EnumProtocol.STATUS);
                this.b.setPacketListener(new PacketStatusListener(this.a, this.b));
                break;
            default:
                throw new UnsupportedOperationException("Invalid intention " + packethandshakinginsetprotocol.b());
        }

    }

    @Override
    public void a(IChatBaseComponent ichatbasecomponent) {}

    @Override
    public NetworkManager a() {
        return this.b;
    }
}
