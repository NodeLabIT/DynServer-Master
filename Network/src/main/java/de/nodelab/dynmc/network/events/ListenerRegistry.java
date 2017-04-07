package de.nodelab.dynmc.network.events;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.nodelab.dynmc.network.packet.Packet;
import io.netty.channel.ChannelHandlerContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ListenerRegistry {

    private Table<Class<?>, PacketListener, Method> packetListener = HashBasedTable.create();

    public void register(PacketListener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            if (method.getAnnotationsByType(PacketListen.class).length == 0) {
                continue;
            }
            packetListener.put(method.getParameterTypes()[1], listener, method);
        }
    }

    public void callEvent(ChannelHandlerContext ctx, Packet packet) {
        System.out.println("Event caught: " + packet.getClass().getName());
        if(!packetListener.containsRow(packet.getClass())) {
            return;
        }
        packetListener.row(packet.getClass()).forEach(((listener, method) -> {
            try {
                method.invoke(listener, ctx, packet);
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }));
    }

}
