'' ******************************************************************************
'' * Simplified I2C for Devantech modules                                       *
'' * Chris Clarke Feb 2007                                                      *
'' ******************************************************************************
''
'' adapted from I2C SPIN Object by James Burrows
''
'' for reference look at: www.semiconductors.philips.com/ acrobat/literature/9398/39340011.pdf
''
'' this object provides the PUBLIC functions:
''  -> Init  - sets up the SCL and SDA pin referance 
''  -> readLocation - READ from a single specified register
''  -> writeLocation - WRITE to a single specified register
''  -> i2cStart - performs a bus start
''  -> i2cStop - performs a bus stop
''  -> i2cRead - performs a read of the i2c bus
''  -> i2cWrite - performs a write to the i2c bus

CON
  ' i2c bus contants
  _i2cNAK         = 1
  _i2cACK         = 0
VAR
  byte  i2cSDA, i2cSCL
PUB init(_i2cSDA, _i2cSCL)
    i2cSDA := _i2cSDA  'referances passed from main file for pin numbers
    i2cSCL := _i2cSCL
    outa[i2cSDA] := 0  'as inputs to start
    outa[i2cSCL] := 0

PUB readLocation(deviceAddress, deviceRegister) : i2cData | ackbit
  ' do a standard i2c address, then read
  ' read a device's register
  ackbit := _i2cACK

  i2cStart
  ackbit := (ackbit << 1) | i2cWrite(deviceAddress)   
  ackbit := (ackbit << 1) | i2cWrite(deviceRegister)   
    
  i2cStart
  ackbit := (ackbit << 1) | i2cWrite(deviceAddress | 1)   ' repeat with read bit now set
  i2cData := i2cRead(_i2cNAK)
  i2cStop

  ' return the data      
  return i2cData

    
PUB writeLocation(deviceAddress, deviceRegister, i2cDataValue) : ackbit
  ' do a standard i2c address, then write
  ' return the ACK bit from the device address
  ackbit := _i2cACK
  i2cstart
  ackbit := (ackbit << 1) | i2cWrite(deviceAddress)
  ackbit := (ackbit << 1) | i2cWrite(deviceRegister)
  ackbit := (ackbit << 1) | i2cWrite(i2cDataValue)
  i2cStop
  return ackbit

PUB i2cStop
  ' i2c stop sequence - the SDA goes LOW to HIGH while SCL is HIGH
  dira[i2cSCL] ~
  dira[i2cSDA] ~

    
PUB i2cStart

       dira[i2cSDA] ~  
       dira[i2cSCL] ~
       dira[i2cSDA] ~~       
       
       repeat until ina[i2cSCL] == 1  

PUB i2cWrite(i2cData) : ackbit
    
    i2cData <<=24  

    ' init the clock line                             
    dira[i2cSCL] := 1 
 
    repeat 8
      ' set the SDA while the SCL is LOW 
      dira[i2cSDA] := (!(i2cData <-= 1) & 1)
      ' toggle SCL HIGH
      dira[i2cSCL] := 0
      ' toogle SCL LOW
      dira[i2cSCL] := 1

   ' setup for ACK - pin to input'    
    dira[i2cSDA] := 0
                  
    ' read in the ACK
    dira[i2cSCL] := 0
    ackbit := ina[i2cSDA]
    dira[i2cSCL] := 1

    ' leave the SDA pin LOW 
    dira[i2cSDA] := 1    

   return ackbit  


PUB i2cRead(ackbit): i2cData

    ' set the SCL to output and the SDA to input
    dira[i2cSDA] := 0
    dira[i2cSCL] := 1
     
    ' clock in the byte
    i2cData := 0
    repeat 8
      dira[i2cSCL] := 0
      i2cData := (i2cData << 1) | ina[i2cSDA]
      dira[i2cSCL] := 1
      
    ' send the ACK or NAK
    dira[i2cSDA] ~~
    dira[i2cSCL] := 0
    dira[i2cSDA] := !ackbit
    dira[i2cSCL] := 1

    ' return the data
    return i2cData
