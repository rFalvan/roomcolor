package me.falvan.roomcolor

import me.falvan.roomcolor.controller.Controller
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.system.exitProcess

lateinit var application: Application

class Application {
    init {
        application = this
        val data = connect("192.168.0.10") ?: run {
            println("Not connected! Exit program...")
            exitProcess(0)
        }
        val status = data.queryStatus()

        println("""
            Successful connection!
            Status:
              Enabled: ${status?.enabled}
              Active color RGB: ${status?.red} ${status?.green} ${status?.blue} 
        """.trimIndent())
    }

    private fun connect(
        address: String,
        port: Int = 48899,
        type: ByteArray = "HF-A11ASSISTHREAD".toByteArray()
    ): Controller? {
        val socket = DatagramSocket(port).apply {
            broadcast = true
            soTimeout = 500
        }
        val buf = ByteArray(512)
        var packet = DatagramPacket(type, type.size, InetAddress.getByName(address), port)
        socket.send(packet)

        val timeout = System.currentTimeMillis() + 500L
        while (timeout - System.currentTimeMillis() > 0L) {
            packet = DatagramPacket(buf, buf.size)

            try {
                socket.receive(packet)
            } catch (var10: Exception) { break }

            val info = String(packet.data, 0, packet.length)
                .split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (info.size == 3) return(Controller(info[0]))
        }
        return null
    }
}

fun main() {
    Application()
}