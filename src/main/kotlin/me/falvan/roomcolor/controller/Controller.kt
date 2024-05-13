package me.falvan.roomcolor.controller

import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

class Controller(private val ip: String) {
    fun setColor(red: Int, green: Int, blue: Int): Boolean {
        val command = byteArrayOf(49, red.toByte(), green.toByte(), blue.toByte(), 0.toByte(), 0, 15, 0)

        var checksum = 0
        var finalRed = 0
        var finalGreen: Int

        while (finalRed < command.size) {
            finalGreen = command[finalRed].toInt()
            checksum += finalGreen
            ++finalRed
        }

        command[command.size - 1] = checksum.toByte()

        try {
            val socket = Socket(this.ip, 5577).apply { soTimeout = 1000 }
            DataOutputStream(socket.getOutputStream()).apply { write(command) }
            socket.close()
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun queryStatus(): ControllerStatus? {
        val data = this.sendCommand(byteArrayOf(-127, -118, -117)) ?: return null
        if (data[13].toInt() == 0) return null

        return ControllerStatus(
            data[2].toInt() == 35,
            data[6].toInt(),
            data[7].toInt(),
            data[8].toInt()
        )
    }

    fun setPower(on: Boolean): Boolean {
        val byte: Byte = if (on) 35 else 36

        val data = this.sendCommand(byteArrayOf(113, byte, 15))
        return when {
            data == null -> false
            data[2].toInt() == 0 -> false
            else -> data[2] != byte
        }
    }

    private fun sendCommand(buffer: ByteArray): ByteArray? {
        try {
            val socket = Socket(this.ip, 5577).apply { soTimeout = 500 }
            val command = ByteArray(buffer.size + 1)
            var checksum = 0

            for (i in buffer.indices) {
                checksum += buffer[i].toInt()
                command[i] = buffer[i]
            }
            command[command.size - 1] = checksum.toByte()

            DataOutputStream(socket.getOutputStream()).write(command)
            val dIn = DataInputStream(socket.getInputStream())
            val resp = ByteArray(64)

            dIn.read(resp)
            socket.close()
            return resp
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}