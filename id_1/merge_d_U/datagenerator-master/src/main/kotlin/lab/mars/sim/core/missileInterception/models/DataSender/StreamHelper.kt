package lab.mars.sim.core.missileInterception.models.DataSender

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder


fun ByteBuffer.littleEndian(): ByteBuffer {
    this.order(ByteOrder.LITTLE_ENDIAN)
    return this
}

fun ByteArrayInputStream.readByte(): Byte {
    return this.read().toByte()
}

fun ByteArrayInputStream.readFloat(): Float {
    val array = ByteArray(Int.SIZE_BYTES)
    this.read(array)
    return ByteBuffer.wrap(array).littleEndian().float
}

fun ByteArrayInputStream.readInt(): Int {
    val array = ByteArray(Int.SIZE_BYTES)
    this.read(array)
    return ByteBuffer.wrap(array).littleEndian().int
}

fun ByteArrayInputStream.readLong(): Long {
    val array = ByteArray(Long.SIZE_BYTES)
    this.read(array)
    return ByteBuffer.wrap(array).littleEndian().long
}

fun ByteArrayOutputStream.writeByte(byte: Byte) {
    this.write(byte.toInt())
}

fun ByteArrayOutputStream.writeInt(vararg int: Int) {
    val bf = ByteBuffer.allocate(Int.SIZE_BYTES * int.size).littleEndian()
    int.forEach {
        bf.putInt(it)
    }
    this.write(bf.array())
}

fun ByteArrayOutputStream.writeInt(int: Array<Int>) {
    val bf = ByteBuffer.allocate(Int.SIZE_BYTES * int.size).littleEndian()
    int.forEach {
        bf.putInt(it)
    }
    this.write(bf.array())
}

fun ByteArrayOutputStream.writeLong(long: Long) {
    this.write(ByteBuffer.allocate(Long.SIZE_BYTES).littleEndian().putLong(long).array())
}

fun ByteArrayOutputStream.writeFloat(float: Float) {
    this.write(ByteBuffer.allocate(Float.SIZE_BYTES).littleEndian().putFloat(float).array())
}

fun ByteArrayOutputStream.writeFloat(vararg floats : Float) {
    val bf = ByteBuffer.allocate(Float.SIZE_BYTES * floats.size).littleEndian()
    floats.forEach {
        bf.putFloat(it)
    }
    this.write(bf.array())
}

fun ByteArrayOutputStream.writeFixedString(str : String, fixedLengthByte : Int) {
    val bf = ByteBuffer.allocate(fixedLengthByte).littleEndian()
    bf.put(str.encodeToByteArray())
    this.write(bf.array())
}

fun ByteArrayOutputStream.writeBytes(bytes: ByteArray) {
    this.write(bytes)
}
