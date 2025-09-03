package lab.mars.sim.core.missileInterception.models.DataSender

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class TCPServer(private val port: Int, private val channelClass: Class<out Channel>) {

    private var server: ServerSocketChannel? = null
    private var socketSelector: Selector? = null

    private var worker: Thread? = null

    private val stop = AtomicBoolean(false)

    private var channelMap: MutableMap<SocketChannel, Channel> = HashMap()

    abstract class Channel {
        internal var socket: SocketChannel? = null

        var errorOccurred = false

        abstract fun doConnected()

        abstract fun doSend(): ByteArray

        abstract fun doRecv(data: ByteArray)

        abstract fun doDisconnected()

        fun channelSend(sendData: ByteArray) {
                try {
                    val size = sendData.size
                    val send = ByteBuffer.allocate(size)
                    send.put(sendData)
                    send.rewind()
                    var actualSendSize = 0
                    while (actualSendSize != size) {
                        val sendSize = socket!!.write(send)
                        if (sendSize < 0) {
                            throw IOException()
                        }
                        actualSendSize += sendSize
                    }
                } catch (exp: Exception) {
                    errorOccurred = true
                }
        }

        @Throws(IOException::class)
        fun canRecv() {
            val sizeBuffer = ByteBuffer.allocate(5)
            sizeBuffer.order(ByteOrder.LITTLE_ENDIAN)
            var actualReadSize = 0
            while (actualReadSize != 5) {
                val readSize = socket!!.read(sizeBuffer)
                if (readSize < 0) {
                    throw IOException()
                }
                actualReadSize += readSize
            }
            sizeBuffer.rewind()
            sizeBuffer.get() //skip first byte
            val length = sizeBuffer.int //read LEN
            sizeBuffer.rewind()
            if (length == 0) {
                throw IOException()
            }
            val actualData = ByteBuffer.allocate(length - 5)
            actualReadSize = 5
            while (actualReadSize != length) {
                val readSize = socket!!.read(actualData)
                if (readSize < 0) {
                    throw IOException()
                }
                actualReadSize += readSize
            }
            val wholeArray = ByteArrayOutputStream()
            wholeArray.write(sizeBuffer.array())
            wholeArray.write(actualData.array())
            doRecv(wholeArray.toByteArray())
        }
    }


    fun open(): Boolean {
        try {
            server = ServerSocketChannel.open()
            socketSelector = Selector.open()
            server!!.bind(InetSocketAddress(port))
            server!!.configureBlocking(false)
            server!!.register(socketSelector, SelectionKey.OP_ACCEPT)
            worker = Thread {
                while (!stop.get()) {
                    try {
                        socketSelector!!.select()
                        val keys = socketSelector!!.selectedKeys()
                        val toClose = HashSet<SocketChannel>()
                        for (key in keys) {
                            if (key.readyOps() and SelectionKey.OP_ACCEPT == SelectionKey.OP_ACCEPT) {
                                val s = server!!.accept()
                                s.configureBlocking(false)
                                s.register(socketSelector, SelectionKey.OP_READ)
                                val newChannel = channelClass.newInstance()
                                newChannel.socket = s
                                newChannel.doConnected()
                                channelMap[s] = newChannel
                            }
                            if (key.readyOps() and SelectionKey.OP_READ == SelectionKey.OP_READ) {
                                val s = key.channel() as SocketChannel
                                val channel = channelMap[s]!!
                                try {
                                    channel.canRecv()
                                } catch (exp: IOException) {
                                    toClose.add(s)
                                }
                            }

                        }
                        keys.clear()
                        channelMap.forEach { s, ch ->
                            if (ch.errorOccurred) {
                                toClose.add(s)
                            }
                        }
                        for (s in toClose) {
                            channelMap[s]!!.doDisconnected()
                            channelMap.remove(s)
                            s.close()
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    } catch (e: InstantiationException) {
                        e.printStackTrace()
                    }

                }
                try {
                    socketSelector!!.close()
                    for (s in channelMap.keys) {
                        s.close()
                    }
                    server!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            worker!!.start()

        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        return true
    }


    fun dispose() {
        try {
            stop.set(true)
            worker!!.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

}
