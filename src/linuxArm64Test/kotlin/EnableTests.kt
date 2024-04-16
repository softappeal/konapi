package ch.softappeal.konapi

import ch.softappeal.konapi.devices.Bme280Test
import ch.softappeal.konapi.devices.I2cLcd1602Test
import ch.softappeal.konapi.devices.OledTest
import ch.softappeal.konapi.devices.Paj7620U2Test

class ConcreteGpioTest : GpioTest()
class ConcreteSpiTest : SpiTest()

class ConcreteI2cLcd1602Test : I2cLcd1602Test()
class ConcreteBme280Test : Bme280Test()
class ConcretePaj7620U2Test : Paj7620U2Test()
class ConcreteOledTest : OledTest()
