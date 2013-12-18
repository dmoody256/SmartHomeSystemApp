con
   _clkfreq = 5_000_000                  '5mhz xtal
   _CLKMODE = xtal1 + pll16x             'phase lock loop to multiply clock to 80mhz (5mhz*16)


   i2cSCLLine = 28                       'SCL is on Propeller pin A13
   i2cSDALine = 29                       'SDA is on Propeller pin A12
   i2cBridge_Addr = $9A                      'LCD03 address (default) at 0xC6
   LCR_REG_Addr = $03
   DLL_REG_Addr = $00
   DLM_REG_Addr = $01
   EFR_REG_Addr = $02
   FCR_REG_Addr = $02
   SPR_REG_Addr = $07
   
OBJ
   i2cObject      : "i2cObject"                
   pst            : "Parallax Serial Terminal"
   
DAT

   LCRBaudRateChange            byte    $80
   DLL9600Baud                  byte    $60
   DLM9600Buad                  byte    $00
   LCREnhancedFunctionsChange   byte    $BF
   ENFEnabledChange             byte    $10
   DataFlowChange               byte    $03
   ResetFIFO                    byte    $06
   EnableFIFO                   byte    $01

VAR

byte ModNum

pub main | temp, SlowCount

   'Num.init                                                         'required to be called before using Num.ToStr function 

   'ModNum := 3
   pst.Start(9600)
   pause(2000)

   pst.str(string("PST Started"))

  { 
  temp := 1
  SlowCount := 0
  repeat
         SlowCount := SlowCount + 1
         if(SlowCount == 10)
                                SlowCount := 0
                                temp := temp + 1
                                'pst.Dec(temp)
                               'pst.str(string(13)) 
                                'XBeeCommands(temp, 1)
                                if(temp == ModNum)
                                  temp := 1

  
                               }

   
   

PRI Pause(Duration)  
  waitcnt(((clkfreq / 1_000 * Duration - 3932) #> 381) + cnt)
  return

     
   
   
                'command of 12 to LCD03 to clear screen
   'i2cObject.Writelocation(LCD03_Addr,LCD03_CMD_REG,4)              'command of 4 to LCD03 to hide cursor
   'i2cObject.Writelocation(LCD03_Addr,LCD03_CMD_REG,19)             'command of 19 to LCD03 to turn backlight on 
   'repeat
    '  i2cObject.Writelocation(LCD03_Addr,0,1)                       'home position for cursor command
     ' outputString(string("LCD03 WITH PROPELLER"))                  'string() makes string, and passes pointer to outputstring()
      'outputString(string("SOFTWARE VERSION"))
      'version := i2cObject.readLocation(LCD03_Addr,LCD03_VER_REG)   'LCD03 version in register 3
      'outputString(Num.ToStr(version, NUM#dec2))                    'write version to lcd03 display
      'keyl := i2cObject.readLocation(LCD03_Addr,LCD03_KEYL_REG)     'LCD03 keyl in register 1
      'keyh := i2cObject.readLocation(LCD03_Addr,LCD03_KEYH_REG)     'LCD03 keyh in register 2
      'outputString(string(" ")) 
      'outputString(string(" KEYSL ="))
      'outputString(Num.ToStr(keyl, NUM#BIN9))                       'print keyl in binary 
      'outputString(string(" "))                                     'just space to finish line
      'outputString(Num.ToStr(keyh, NUM#BIN9))                       'print keyh in binary 

      'waitcnt(5_000_000 + cnt)                                      'just a delay

'PUB OutputString(str_addr)                                          'passed pointer by string() in main to ascii byte string
   'repeat strsize(str_addr)                                         'repeat for string length calculated by strsize
      'i2cObject.Writelocation(LCD03_Addr,LCD03_CMD_REG,byte[str_addr++])       'send string bytes to LCD03 