package de.fu_berlin.inf.dpp.net.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Packet;

import de.fu_berlin.inf.dpp.annotations.Component;
import de.fu_berlin.inf.dpp.net.internal.SarosPacketCollector.CancelHook;

/**
 * Facade for receiving XMPP Packages. Kind of like the GodPacketListener!
 * 
 * XMPPReceiver implements addPacketListener and removePacketListener just
 * like a XMPPConnection but hides the complexity of dealing with new connection
 * objects appearing and old one's disappearing. Users can just register with
 * the XMPPReceiver for the whole application life-cycle.
 * 
 */
@Component(module = "net")
public class XMPPReceiver {

    protected Map<PacketListener, PacketFilter> listeners = Collections
        .synchronizedMap(new HashMap<PacketListener, PacketFilter>());

    /**
     * Adds the given listener to the list of listeners notified when a new
     * packet arrives.
     * 
     * Will only pass those packets to the listener that are accepted by the
     * given filter or all Packets if no filter is given.
     * 
     * @param listener
     *            The listener to pass packets to.
     * @param filter
     *            The filter to use when trying to identify Packets to send to
     *            the listener. may be null, in which case all Packets are sent.
     */
    public void addPacketListener(PacketListener listener, PacketFilter filter) {
        listeners.put(listener, filter);
    }

    public void removePacketListener(PacketListener listener) {
        listeners.remove(listener);
    }

    /**
     * This is called from the XMPPConnection for each incoming Packet and will
     * dispatch these to the registered listeners.
     * 
     * @sarosThread must be called from the Dispatch Thread
     */
    public void processPacket(Packet packet) {
        Map<PacketListener, PacketFilter> copy;

        synchronized (listeners) {
            copy = new HashMap<PacketListener, PacketFilter>(listeners);
        }
        for (Entry<PacketListener, PacketFilter> entry : copy.entrySet()) {
            PacketListener listener = entry.getKey();
            PacketFilter filter = entry.getValue();

            if (filter == null || filter.accept(packet)) {
                listener.processPacket(packet);
            }
        }
    }

    public SarosPacketCollector createCollector(PacketFilter filter) {
        final SarosPacketCollector collector = new SarosPacketCollector(
            new CancelHook() {
                public void cancelPacketCollector(SarosPacketCollector collector) {
                    removePacketListener(collector);
                }
            }, filter);
        addPacketListener(collector, filter);

        return collector;
    }
}