package lab.mars.sim.core.missileInterception.models.DataSender

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset

class SNAppChannel : TCPServer.Channel() {

    companion object {
        var activeChannel: SNAppChannel? = null
    }

    override fun doConnected() {
        activeChannel = this
    }

    fun send(data: String) {
        val writer = ByteArrayOutputStream()
        val bytes = data.encodeToByteArray()
        val size = bytes.size
        val bf = ByteBuffer.allocate(Int.SIZE_BYTES).littleEndian()
        bf.putInt(size)
        writer.write(bf.array())
        writer.writeBytes(bytes)
        this.channelSend(writer.toByteArray())
    }

    override fun doSend(): ByteArray {
        return ByteArray(0)
    }

    override fun doRecv(data: ByteArray) {
        println(data.toString(Charset.defaultCharset()))
    }

    override fun doDisconnected() {
        activeChannel = null
    }

}