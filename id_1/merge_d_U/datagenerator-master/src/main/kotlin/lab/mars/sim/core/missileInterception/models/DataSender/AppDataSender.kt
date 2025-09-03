package lab.mars.sim.core.missileInterception.models.DataSender


import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketOptions
import java.net.StandardSocketOptions
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel

class AppDataSender(val ip: String, val port: Int) {
    private lateinit var socketChannel: SocketChannel
    private lateinit var socketSelector: Selector
    private var connected = false
    init {
        try {
            socketChannel = SocketChannel.open()
            socketChannel.connect(InetSocketAddress(ip, port))
            socketSelector = Selector.open()
            socketChannel.configureBlocking(false)
            socketChannel.register(socketSelector, SelectionKey.OP_READ)
            println("tcp sender to ${ip}:${port}")
            connected = true
        } catch (exp : IOException) {
            println("cannot connect to App@${ip}:${port}")
        }
    }

    fun send(sendData: ByteArray) {
        if (!connected) {
            println("cannot send data to App@${ip}:${port}")
            return
        }
        try {
            var size = sendData.size
            val send = ByteBuffer.allocate(Int.SIZE_BYTES + size)
            send.order(ByteOrder.BIG_ENDIAN)
            send.putInt(size)
            send.order(ByteOrder.LITTLE_ENDIAN)
            send.put(sendData)
            send.rewind()
            var actualSendSize = 0
            size = size + Int.SIZE_BYTES
            while (actualSendSize != size) {
                val sendSize = socketChannel.write(send)
                if (sendSize < 0) {
                    throw IOException()
                }
                actualSendSize += sendSize
            }
        } catch (exp: Exception) {
            println("cannot send data to App@${ip}:${port}")
        }
    }
}