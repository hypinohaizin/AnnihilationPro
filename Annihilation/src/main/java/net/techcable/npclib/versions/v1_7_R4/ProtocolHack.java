package net.techcable.npclib.versions.v1_7_R4;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.Packet;
import net.techcable.npclib.util.ReflectUtil;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

public class ProtocolHack {
    private ProtocolHack() {}
    
    private static EntityPlayer getHandle(Player bukkitPlayer) {
    	if (!(bukkitPlayer instanceof CraftPlayer)) return null;
    	return ((CraftPlayer)bukkitPlayer).getHandle();
    }

    private static EntityPlayer[] getHandles(Collection<? extends Player> bukkitPlayers)
    {
    	EntityPlayer[] handles = new EntityPlayer[bukkitPlayers.size()];
        Iterator<? extends Player> itt = bukkitPlayers.iterator();
    	//for (int i = 0; i < bukkitPlayers.length; i++)
        int i = 0;
        while(itt.hasNext())
        {
    		handles[i] = getHandle(itt.next());
            i++;
    	}
    	return handles;
    }

    private static EntityPlayer[] getHandles(Player[] bukkitPlayers)
    {
        EntityPlayer[] handles = new EntityPlayer[bukkitPlayers.length];
        for (int i = 0; i < bukkitPlayers.length; i++)
        {
            handles[i] = getHandle(bukkitPlayers[i]);
        }
        return handles;
    }
    
    public static boolean isProtocolHack() {
        try {
            Class.forName("org.spigotmc.ProtocolData");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
    
    public static void notifyOfSpawn(Collection<? extends Player> toNotify, Player... npcs)
    {
        Method addPlayer = ReflectUtil.makeMethod(getPlayerInfoClass(), "addPlayer", EntityPlayer.class);
        EntityPlayer[] handles = getHandles(npcs);
        Packet[] packets = new Packet[handles.length];
        for (int i = 0; i < handles.length; i++)
        {
            EntityPlayer handle = handles[i];
            Packet packet = ReflectUtil.callMethod(addPlayer, null, handle);
            packets[i] = packet;
        }
        sendPacketsTo(toNotify, packets);
    }
    
    public static void notifyOfDespawn(Collection<? extends Player> toNotify, Player... npcs) {
        Method removePlayer = ReflectUtil.makeMethod(getPlayerInfoClass(), "removePlayer", EntityPlayer.class);
        EntityPlayer[] handles = getHandles(npcs);
        Packet[] packets = new Packet[handles.length];
        for (int i = 0; i < handles.length; i++) {
            EntityPlayer handle = handles[i];
            Packet packet = ReflectUtil.callMethod(removePlayer, null, handle);
            packets[i] = packet;
        }
        sendPacketsTo(toNotify, packets);
    }
    
    public static Class <?> getPlayerInfoClass() {
        try {
            return Class.forName("net.minecraft.server.v1_7_R4.PacketPlayOutPlayerInfo");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static void sendPacketsTo(Collection<? extends Player> recipients, Packet... packets) {
	    EntityPlayer[] nmsRecipients = getHandles(recipients);
		for (EntityPlayer recipient : nmsRecipients) {
			if (recipient == null) continue;
			for (Packet packet : packets) {
			    if (packet == null) continue;
			    recipient.playerConnection.sendPacket(packet);
			}
		}
	}
}
