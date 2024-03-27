package ch.softappeal.kopi

import ch.softappeal.kopi.gpio.GpioTest
import ch.softappeal.kopi.i2c.Lcd1602Test
import ch.softappeal.kopi.ic2.Bme280Test
import ch.softappeal.kopi.ic2.Paj7620U2Test

class ConcreteGpioTest : GpioTest()

class ConcreteLcd1602Test : Lcd1602Test()
class ConcreteBme280Test : Bme280Test()
class ConcretePaj7620U2Test : Paj7620U2Test()
