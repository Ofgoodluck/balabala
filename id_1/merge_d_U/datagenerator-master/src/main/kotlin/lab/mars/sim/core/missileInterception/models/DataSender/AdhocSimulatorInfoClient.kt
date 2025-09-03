package lab.mars.sim.core.missileInterception.models.DataSender

import com.badlogic.gdx.math.Vector3
import lab.mars.sim.core.missileInterception.script.Config
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.SocketChannel
import kotlin.concurrent.thread

object AdhocSimulatorInfoClient {
    private lateinit var socketChannel : SocketChannel
    private lateinit var socketSelector : Selector

    var currentGraph = HashMap<Int, HashSet<Int>>()

    var onNetGraphInfo : ((NetGraph) -> Unit)? = null
    var onMessageLog : ((MessageLog) -> Unit)? = null

    class PositionModify(val id : Int, val posX : Double, val posY : Double, val posZ : Double) {
        companion object {
            const val type = 0
        }
    }

    class DistanceModify(val id : Int, val distance : Double) {
        companion object {
            const val type = 1
        }
    }

    class GraphModify(val graph : HashMap<Int, HashSet<Int>>, val positions : HashMap<Int, Vector3>) {
        companion object {
            const val type = 2
        }
    }

    private fun sendInfo(type : Int, str : String) {
        val bytes = str.toByteArray()
        val bodySize = bytes.size + 5
        val writer = ByteArrayOutputStream()
        writer.write(type)
        writer.writeInt(bodySize)
        writer.writeBytes(bytes)
        val sendData = writer.toByteArray()
        try {
            val size = sendData.size
            val send = ByteBuffer.allocate(size)
            send.put(sendData)
            send.rewind()
            var actualSendSize = 0
            while (actualSendSize != size) {
                val sendSize = socketChannel.write(send)
                if (sendSize < 0) {
                    throw IOException()
                }
                actualSendSize += sendSize
            }
        } catch (exp : Exception) {
            println("cannot send data to adhoc simulator")
        }
    }

    fun updateGraph(g : HashMap<Int, HashSet<Int>>, p : HashMap<Int, Vector3>) {
        this.currentGraph = g
        val msg = GraphModify(g, p)
        sendInfo(GraphModify.type, gson.toJson(msg))
    }

    fun updateDistance(id : Int, distance : Double) {
        val struct = DistanceModify(id, distance)
        sendInfo(DistanceModify.type, gson.toJson(struct))
    }

    fun updatePosition(id : Int, posX : Double, posY : Double) {
        val struct = PositionModify(id, posX, posY, 0.0)
        val jsonStr = gson.toJson(struct)
        sendInfo(PositionModify.type, jsonStr)
    }

    fun onRecv(data : ByteArray) {
        val jsonStr = String(data)
        val info = gson.fromJson(jsonStr, AdhocInfo::class.java)
        when(info.Type) {
            InformationType.MessageLog.value -> onMessageLog?.invoke(info.getValue<MessageLog>())
            InformationType.NetGraph.value -> onNetGraphInfo?.invoke(info.getValue<NetGraph>())
            else -> return
        }
    }

    fun start() {
        try {
            socketChannel = SocketChannel.open()
            socketChannel.connect(InetSocketAddress(Config.adhocIp, 3237))
            socketSelector = Selector.open()
            socketChannel.configureBlocking(false)
            socketChannel.register(socketSelector, SelectionKey.OP_READ)
        } catch (exp : IOException) {
            println("cannot connect to adhoc simulator @ ${Config.adhocIp}:3237")
            return
        }
        println("connected adhoc simulator @ ${Config.adhocIp}:3237")
        thread {
            while (true) {
                try {
                    val num = socketSelector.select()
                    val keys = socketSelector.selectedKeys()
                    for (key in keys) {
                        if (key.readyOps() and SelectionKey.OP_READ == SelectionKey.OP_READ) {
                            val sizeBuffer = ByteBuffer.allocate(4)
                            sizeBuffer.order(ByteOrder.LITTLE_ENDIAN)
                            var actualReadSize = 0
                            while (actualReadSize != 4) {
                                val readSize = socketChannel.read(sizeBuffer)
                                if (readSize < 0) {
                                    throw IOException()
                                }
                                actualReadSize += readSize
                            }
                            sizeBuffer.rewind()
                            val length = sizeBuffer.int //read LEN
                            val actualData = ByteBuffer.allocate(length)
                            actualReadSize = 0
                            while (actualReadSize != length) {
                                val readSize = socketChannel.read(actualData)
                                if (readSize < 0) {
                                    throw IOException()
                                }
                                actualReadSize += readSize
                            }
                            val wholeArray = ByteArrayOutputStream()
                            wholeArray.write(actualData.array())
                            onRecv(wholeArray.toByteArray())
                        }
                    }
                } catch (exp : IOException) {
                    exp.printStackTrace()
                    socketSelector.close()
                    socketChannel.close()
                    break
                }
            }
        }

    }
}