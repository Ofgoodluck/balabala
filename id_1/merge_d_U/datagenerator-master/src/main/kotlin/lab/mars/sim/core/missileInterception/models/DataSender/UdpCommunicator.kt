package lab.mars.sim.core.missileInterception.models.DataSender

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.*

class UdpCommunicator internal constructor(
    selfPort: Int,
    peerHost: String,
    peerPort: Int,
    private val listener: ((data: ByteArray) -> Unit)?
) : Thread() {

    private val peerAddress: InetSocketAddress
    private val datagramSocket: DatagramSocket

    @Volatile
    private var running = true

    init {
        println("udp sender to ${peerHost}:${peerPort}")
        datagramSocket = DatagramSocket(selfPort)
        datagramSocket.soTimeout = 1000
        peerAddress = InetSocketAddress(peerHost, peerPort)
    }

    @Throws(IOException::class)
    fun send(data: ByteArray) {
        val DpSend = DatagramPacket(data, data.size, peerAddress)
        datagramSocket.send(DpSend)
    }

    override fun run() {
        val buffer = ByteArray(65535)
        while (running) {
            val dpReceive = DatagramPacket(buffer, buffer.size)
            var received = true
            try {
                datagramSocket.receive(dpReceive)
            } catch (e: IOException) {
                received = false
            }
            if (received) {
                listener?.invoke(buffer.copyOfRange(0, dpReceive.length))
            }
        }
    }

    fun terminate() {
        running = false
        datagramSocket.close()
    }
}