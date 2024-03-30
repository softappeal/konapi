@file:OptIn(ExperimentalUnsignedTypes::class)

package ch.softappeal.kopi

public interface RegisterDevice {
    public suspend fun write(register: UByte, value: UByte)
    public suspend fun write(register: UByte, values: UByteArray)
}

public class RegisterCommand(public val register: UByte, public val value: UByte)

public suspend fun RegisterDevice.write(command: RegisterCommand) {
    write(command.register, command.value)
}

public suspend fun RegisterDevice.write(commands: List<RegisterCommand>) {
    val bytes = UByteArray(commands.size * 2 - 1)
    bytes[0] = commands[0].value
    var i = 1
    for (c in 1..<commands.size) {
        val command = commands[c]
        bytes[i++] = command.register
        bytes[i++] = command.value
    }
    write(commands[0].register, bytes)
}
