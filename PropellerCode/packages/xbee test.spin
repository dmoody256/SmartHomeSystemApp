{{ 
   *************************************** 
   * Config_Getting_dB_Level             * 
   *************************************** 
   *  See end of file for terms of use.  *                
   *************************************** 
   Demonstrates receiving multiple decimal 
   value with start delimiter     
}} 
CON 
  _clkmode = xtal1 + pll16x 
  _xinfreq = 5_000_000 
  ' Set pins and Baud rate for XBee comms   
  XB_Rx     = 31    ' XBee DOUT 
  XB_Tx     = 30    ' XBee DIN 
  XB_Baud   = 9600 
  ' Carriage return value 
  CR = 13 
      
OBJ 
   XB    : "XBee_Object_2"
   str           : "StringMethods"
Pub  Start | DataIn, Val1,Val2 
XB.start(XB_Rx, XB_Tx, 0, XB_Baud)     ' Initialize comms for XBee 
str.Start
XB.Delay(5000)                         ' One second delay  
' Configure XBee module 
'XB.Str(String("Configuring XBee...",13)) 
XB.AT_Init                            ' Configure for fast AT Command Mode 


repeat


  XB.AT_Config(string("ATID 3331"))
                                      
           ' Send AT command turn off Association LED
  XB.Delay(5000)
  XB.AT_Config(string("ATID 3332"))
  XB.Delay(5000)


'XB.str(string("Awaiting Data..."))    ' Notify Base 
'XB.CR  
'Repeat 
 ' DataIn := XB.RxTime(100)            ' Wait for byte with timeout 
  'If DataIn == "!"                    ' Check if delimiter 
   ' Val1 := XB.RxDecTime(3000)        ' Wait for 1st value with timeout 
    'Val2 := XB.RxDecTime(3000)        ' Wait for next value with timeout 
    'If Val2 <> -1                     ' If value not received value is -1 
    '  XB.CR 
    '  XB.Str(string(CR,"Value 1 = ")) ' Display remotely with string 
    '  XB.Dec(Val1)                    ' Decimal value 
    '  XB.Str(string(CR,"Value 2 = ")) ' Display remotely 
    '  XB.Dec(Val2)                    ' Decimal value 
    '  XB.RxFlush                      ' Clear buffer 
    '  XB.AT_Config(string("ATDB"))    ' Request dB Level 
    '  DataIn := XB.RxHexTime(200)     ' Accept returning hex value 
    '  XB.Str(string(13,"dB level = "))' Display remotely 
    '  XB.Dec(-DataIn)                 ' Value as negative decimal 
    '  XB.CR 
  'Else 
  '  XB.Tx(".")                        ' Send dot to show actively waiting 