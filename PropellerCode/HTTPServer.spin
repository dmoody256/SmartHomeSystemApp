{{
───────────────────────────────────────────────── 
Copyright (c) 2011 AgaveRobotics LLC.
See end of file for terms of use.

File....... HTTPServer.spin 
Author..... Mike Gebhard
Company.... Agave Robotics LLC
Email...... mailto:mike.gebhard@agaverobotics.com
Started.... 11/01/2010
Updated.... 07/16/2011        
───────────────────────────────────────────────── 
}}

{
About:
  HTTPServer is the designed for use with the Spinneret Web server manufactured by Parallax Inc.

Usage:
  HTTPServer is the top level object.
  Required objects:
        • Parallax Serial Terminal.spin
        • W5100_Indirect_Driver.spin
        • S35390A_SD-MMC_FATEngineWrapper.spin
        • Request.spin
        • Response.spin
        • StringMethods.spin
        • S35390A_RTCEngine.spin 

Change Log:
 
}


CON
  _clkmode = xtal1 + pll16x     
  _xinfreq = 5_000_000

  MAX_PACKET_SIZE = $5C0 '1472   '$800 = 2048
  RxTx_BUFFER     = $400         '$600 = 1536
  TEMP_BUFFER     = $400         '$5B4 = 1460 
  TCP_PROTOCOL    = %0001        '$300 = 768 
  UDP_PROTOCOL    = %0010        '$200 = 512 
  TCP_CONNECT     = $04          '$100 = 256                 
  DELAY           = $10
  MAX_TRIES       = $25

  i2cSCLLine = 28                       'SCL is on Propeller pin A13
  i2cSDALine = 29                       'SDA is on Propeller pin A12
  i2cBridge_Addr = $9A                      'LCD03 address (default) at 0xC6
  LCR_REG_Addr = $03
  DLL_REG_Addr = $00
  DLM_REG_Addr = $01
  EFR_REG_Addr = $02
  FCR_REG_Addr = $02
  SPR_REG_Addr = $07

  ' i2c bus contants
  _i2cNAK         = 1
  _i2cACK         = 0

  XB_Rx     = 31    ' XBee DOUT 
  XB_Tx     = 30    ' XBee DIN 
  XB_Baud   = 9600

  #0, DONE, PUT, CD
                                           

DAT
  mac                   byte    $00, $08, $DC, $16, $F3, $39
  subnet                byte    255, 255 ,255, 0
  gateway               byte    192, 168, 1, 1 
  ip                    byte    192, 168, 1, 120
  port                  word    5000
  remoteIp              byte    8, 23, 224, 120 {8.23.224.120}
  remotePort            word    80
  uport                 word    5050 
  emailIp               byte    0, 0, 0, 0
  emailPort             word    25
  status                byte    $00, $00, $00, $00   
  rxdata                byte    $0[RxTx_BUFFER]
  txdata                byte    $0[RxTx_BUFFER]
  udpdata               byte    $0[TEMP_BUFFER]
  'tempBuff              byte    $0[TEMP_BUFFER]  
  fileErrorHandle       long    $0
  debug                 byte    $0
  lastFile              byte    $0[12], 0
  closedState           byte    %0000
  openState             byte    %0000
  listenState           byte    %0000
  establishedState      byte    %0000
  closingState          byte    %0000
  closeWaitState        byte    %0000 
  lastEstblState        byte    %0000
  lastEstblStateDebug   byte    %0000
  udpListen             byte    %0000
  tcpMask               byte    %1111
  udpMask               byte    %1000   
  fifoSocketDepth       byte    $0
  fifoSocket            long    $00_00_00_00
  debugSemId            byte    $00
  debugCounter          long    $00
  stringMethods         long    $00
  closingTimeout        long    $00, $00, $00, $00
  udpLen                long    $00
  time                  byte    "00/00/0000 00:00:00", 0
  httpDate              byte    "Wed, 01 Feb 2000 01:00:00 GMT", 0
  globalCache           byte    $1
  dynamicContentPtr     long    @txdata

  LCRBaudRateChange            byte    $80
  DLL9600Baud                  byte    $60
  DLM9600Buad                  byte    $00
  LCREnhancedFunctionsChange   byte    $BF
  ENFEnabledChange             byte    $10
  DataFlowChange               byte    $03
  ResetFIFO                    byte    $06
  EnableFIFO                   byte    $01

  UserDataBuffer           byte    $0[$220]
  TempUserData             byte    $0[$36]
  TempEditData             byte    $0[$36]

  XbeeStringBuf                 byte $0[$10]

  ModNumBuff             byte $00
                 
VAR
  
  long StackSpace[20]
  'long Timecounter
  'byte Startcount
  
  'byte TestByte
  'byte i2cData
  'byte i2cBIT
  'byte StringArray
  long LEptr
  'long error


  
  byte ModNum                    
  byte ModRelay[$0C]                     
  byte ModManual[$0C]                    
  byte ModMS[$0C]                       
  byte ModSS[$0C]
  byte MStimed[$0C]
  byte MStimer[$0C]
                        

OBJ
  'pst          : "Parallax Serial Terminal"
  Socket        : "W5100_Indirect_Driver"
  SDCard        : "S35390A_SD-MMC_FATEngineWrapper"
  Request       : "Request"
  Response      : "Response"
  str           : "StringMethods"
  rtc           : "S35390A_RTCEngine"
  StringEng     : "ASCII0_STREngine_1"
  XB            : "XBee_Object_2"

PUB Initialize | id, size, st, i

  debug := 1    
  SDCard.Start
  stringMethods := str.Start
  Request.Constructor(stringMethods)
  Response.Constructor(stringMethods, @txdata)

  
  ModNum := 10
  
  
  XB.start(XB_Rx, XB_Tx, 0, XB_Baud)      ' Initialize comms for XBee 
    XB.Delay(200)                      ' One second delay  
' Configure XBee module 
   ' XB.Str(String("Configuring XBee...",13))
    XB.AT_Init
 ' pst.Start(9600)
  pause(200)

  
  'Mount the SD card
 ' pst.str(string("Mount SD Card - ")) 
  SDCard.mount(fileErrorHandle)
 ' pst.str(string("OK",13))
  
 ' pst.str(string("Start RTC: "))
  rtc.RTCEngineStart(29, 28, -1)
  
  pause(200)

  LEptr := \SDCard.openFile(string("USER.TXT"), "A")
  
 ' pst.str(LEptr)
 ' pst.str(string(13))
  
  if(strcomp(LEptr, string("Entry Not Found")))
  
      SDCard.newFile(string("USER.TXT"))
      
     ' pst.str(string("New File Created", 13))

      SDCard.openFile(string("USER.TXT"), "W")

      SDCard.writeData(string("DBinitialized check.", 13), strsize(string("DBinitialized check.", 13)))

      SDCard.closeFile

  LEptr := \SDCard.openFile(string("MODULE.TXT"), "A")
  
 ' pst.str(LEptr)
 ' pst.str(string(13))
  
  if(strcomp(LEptr, string("Entry Not Found")))
  
      SDCard.newFile(string("MODULE.TXT"))
      
     ' pst.str(string("New File Created", 13))

      SDCard.openFile(string("MODULE.TXT"), "W")

      SDCard.writeData(string("DBinitialized check.", 13), strsize(string("DBinitialized check.", 13)))

      SDCard.closeFile
      
 ' pst.str(FillTime)
    
  'Start the W5100 driver
  if(Socket.Start)
   ' pst.str(string(13, "W5100 Driver Started", 13))
   ' pst.str(string(13, "Status Memory Lock ID    : "))
   ' pst.dec(Socket.GetLockId)
   ' pst.char(13) 


  if(debugSemId := locknew) == -1
   ' pst.str(string("Error, no HTTP server locks available", 13))
  else
   ' pst.str(string("HTTP Server Lock ID      : "))
   ' pst.dec(debugSemId)
   ' pst.char(13)
    

  'Set the Socket addresses  
  SetMac(@mac)
  SetGateway(@gateway)
  SetSubnet(@subnet)
  SetIP(@ip)

  ' Initailize TCP sockets (defalut setting; TCP, Port, remote ip and remote port)
  repeat id from 0 to 3
    InitializeSocket(id)
    Request.Release(id)
    pause(50)

  ' Set all TCP sockets to listen
 ' pst.char(13) 
  repeat id from 0 to 3 
    Socket.Listen(id)
  ' pst.str(string("TCP Socket Listener ID    : "))
   ' pst.dec(id)
   ' pst.char(13)
    pause(50)

' pst.Str(string(13,"Started Socket Monitoring Service", 13))
 
  cognew(StatusMonitor, @StackSpace)
  pause(250)


 ' pst.Str(string(13, "Initial Socket States",13))
  StackDump

 ' pst.Str(string(13, "Initial Socket Queue",13))
  QueueDump

 ' pst.str(string(13,"//////////////////////////////////////////////////////////////",13))

  

   
  
  Main

   
PUB Main | packetSize, id, i, reset, j, temp, SlowCount 
  ''HTTP Service

   'XBeeCommands(1, 1)
   'XBeeCommands(2, 1)
  
   'GetMyIp(3)

   
  temp := 1
 
  repeat
                               

                                XBeeCommands(temp, 1)
                                temp := temp + 1
                                if(temp > ModNum)
                                  temp := 1
                                       
                               
         
        
  
    repeat until fifoSocket == 0


                               
    
      bytefill(@rxdata, 0, RxTx_BUFFER)

      if(debug)
        'pst.str(string(13, "----- Start of Request----------------------------",13))
        pause(DELAY)
      else
        pause(DELAY)
        
      ' Pop the next socket handle 
      id := DequeueSocket
      if(id < 0)
        next
      
      if(debug)
        'pst.str(string(13,"ID: "))
        'pst.dec(id)
        'pst.str(string(13, "Request Count     : "))
        'pst.dec(debugCounter)
        'pst.char(13)

      packetSize := Socket.rxTCP(id, @rxdata)

      reset := false
      if ((packetSize < 12) AND (strsize(@rxdata) < 12))
        repeat i from 0 to MAX_TRIES
           'pst.str(string(13,"* Retry *"))
          'Wait for a few moments and try again
                       
                                  
          waitcnt((clkfreq/500) + cnt)
          packetSize := Socket.rxTCP(id, @rxdata)
          if(packetSize > 12)
            quit
          if(i == MAX_TRIES)
            'Clean up resource request   
            Request.Release(id)
            Socket.Disconnect(id)
            reset := true
            if(debug)
              StackDump
              'pst.char(13)
              QueueDump
              'pst.str(string(13,"* Timeout *",13))
            
      if(reset)
        next

      Request.InitializeRequest(id, @rxdata)
      
      if(debug)
        'pst.char(13)
        HeaderLine1(id)
      else
        pause(DELAY)



     
      ' Process router
      Dispatcher(id)

      'Clean up request resource
      Request.Release(id)

      ' This starts the close process -> 0x00
      ' use close to force a close
      Socket.Disconnect(id)

      bytefill(@txdata, 0, RxTx_BUFFER)

      debugCounter++

  GotoMain

PRI GotoMain
  Main



PRI Lights

    if(ina[24])'if no motion
         dira[23] := 1
         outa[23] := 0
    else
         dira[23] := 1
         outa[23] := 1

PRI Dispatcher(id)
''do some processing before sending the response

  
  

  if(strcomp(Request.GetFileName(id), string("aled.htm")) AND Request.GetDepth(id) == 1)     
        SendLedResposne(id)
        return

  if(strcomp(Request.GetFileName(id), string("reguser.htm")) AND Request.GetDepth(id) == 1)     
        UserResponse(id, 1)
        return

  if(strcomp(Request.GetFileName(id), string("edituser.htm")) AND Request.GetDepth(id) == 1)     
        UserResponse(id, 2)
        return

  if(strcomp(Request.GetFileName(id), string("deluser.htm")) AND Request.GetDepth(id) == 1)     
        UserResponse(id, 3)
        return

  if(strcomp(Request.GetFileName(id), string("addmod.htm")) AND Request.GetDepth(id) == 1)
             
        UserResponse(id, 4)
        return

  if(strcomp(Request.GetFileName(id), string("editmod.htm")) AND Request.GetDepth(id) == 1)     
        UserResponse(id, 5)
        return

  if(strcomp(Request.GetFileName(id), string("delmod.htm")) AND Request.GetDepth(id) == 1)     
        UserResponse(id, 6)
        return

  waitcnt((clkfreq/350) + cnt)        
  StaticFileHandler(id)
  return


        
PRI SendLedResposne(id) | headerLen, qstr, ptr, xbeenum
        '' Get the query string value
        qstr :=  Request.Get(id, string("led"))

        
        '' Exit if there is no querystring
        if(qstr == 0)
                return
         

       
        
        
        if(strcomp(string("statusupdate"), qstr ))


                                  
            ModuleDatabase       
              
                 
         
                     
        else
              xbeenum := StringEng.decimalToInteger(qstr)
                     
                     
              XbeeCommands(xbeenum, 0)
         
           
       
        
   '' Build and send the header
        '' Send the value of led= on or off
        headerLen := Response.BuildHeader(Request.GetExtension(id), 200, globalCache)
        Socket.txTCP(id, @txdata, headerLen)
        Socket.txTCP(id, qstr, strsize(qstr))

        

        return

PUB ModuleDatabase | counter, TempString

    counter := 1

    repeat ModNum

        TempString := StringENG.integerToDecimal(counter, 1)
        TempString := StringENG.replaceCharacter(TempString, 43, 30)
        if(counter == 1)
           TempString := StringENG.stringConcatenate(TempString, string(" placeholder.1 1 1 1", 13))
        
         
        EditUser(TempString, 1, string("MODULE.TXT"), 1)
        
        counter := counter + 1
    
        

PRI UserResponse(id, type) | headerLen, qstr, tempstring
        '' Get the query string value
        qstr :=  Request.Get(id, string("user"))

        
        '' Exit if there is no querystring
        if(qstr == 0)
                return

        if(type == 1)
        
          AddUserInfo(qstr,string("USER.TXT"))

        elseif(type == 2)
         
           EditUser(qstr, 1, string("USER.TXT"), 0)

        elseif(type == 3)
          
           EditUser(qstr, 0 ,string("USER.TXT"), 0)

        elseif(type == 4)

        
          AddUserInfo(qstr,string("MODULE.TXT"))

        elseif(type == 5)
         
           EditUser(qstr, 1, string("MODULE.TXT"), 0)

        elseif(type == 6)
          
           EditUser(qstr, 0 ,string("MODULE.TXT"), 0)
  
        
        '' Build and send the header
        '' Send the value of led= on or off
        headerLen := Response.BuildHeader(Request.GetExtension(id), 200, globalCache)
        Socket.txTCP(id, @txdata, headerLen)
        Socket.txTCP(id, qstr, strsize(qstr))

        

        return

PRI LedStatus(state)
        dira[23] := 1
        outa[23] := state

        
        
        return



  
        
PRI StaticFileHandler(id) | fileSize, i, headerLen, temp, j
  ''Serve up static files from the SDCard
  
  'pst.str(string(13,"Static File Handler",13)) 
  SDCard.changeDirectory(@approot)
  'pst.char(13)
  
  'Make sure the directory exists
  ifnot(ChangeDirectory(id))
    'send 404 error
    WriteError(id)
    SDCard.changeDirectory(@approot)
    return
    
  ' Make sure the file exists
  ifnot(FileExists(Request.GetFileName(id)))
    'send 404 error
    WriteError(id)
    SDCard.changeDirectory(@approot)
    return

  ' Open the file for reading
  SDCard.openFile(Request.GetFileName(id), "r")
  fileSize := SDCard.getFileSize

  'WriteResponseHeader(id)
  'BuildHeader(extension, statusCode, expirer)
  headerLen := Response.BuildHeader(Request.GetExtension(id), 200, globalCache)
  Socket.txTCP(id, @txdata, headerLen)
  
  if fileSize < MAX_PACKET_SIZE
    ' send the file in one packet
    SDCard.readFromFile(@txdata, fileSize)
    Socket.txTCP(id, @txdata, fileSize)
  else
    ' send the file in a bunch of packets 
    repeat
      SDCard.readFromFile(@txdata, MAX_PACKET_SIZE)  
      Socket.txTCP(id, @txdata, MAX_PACKET_SIZE)
      fileSize -= MAX_PACKET_SIZE
      ' once the remaining fileSize is less then the max packet size, just send that remaining bit and quit the loop
      if fileSize < MAX_PACKET_SIZE and fileSize > 0
        SDCard.readFromFile(@txdata, fileSize)
        Socket.txTCP(id, @txdata, fileSize)
        quit
   
      ' Bailout
      if(i++ > 1_000_000)
        WriteError(id)
        quit
     
  SDCard.closeFile
  SDCard.changeDirectory(@approot)
  return


  
PRI WriteError(id) | headerOffset
  '' Simple 404 error
  'pst.str(string(13, "Write 404 Error",13 ))
  headerOffset := Response.BuildHeader(Request.GetExtension(id), 404, false)
  Socket.txTCP(id, @txdata, headerOffset)
  return

  
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'' Write data to a buffer
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
PRI PushDynamicContent(content)
  ' Write the content to memory
  ' and update the pointer
  bytemove(dynamicContentPtr, content, strsize(content))
  dynamicContentPtr := dynamicContentPtr + strsize(content)
  return

''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'' directory and file handlers
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''  
PRI ChangeDirectory(id) | i, found
  'Handle directory structure for this Request
  if(Request.GetDepth(id) > 1)
    repeat i from 0 to Request.GetDepth(id)-2
      'Return if the directory is not found 
      ifnot(FileExists(Request.GetPathNode(id, i)))
        return false
      found := SDCard.changeDirectory(Request.GetPathNode(id, i))
  return true 

  
PRI FileExists(fileToCompare) | filenamePtr
'Start file find at the top of the list
  SDCard.startFindFile 
  'Verify that the file exists
  repeat while filenamePtr <> 0
    filenamePtr := SDCard.nextFile 
    if(str.MatchPattern(filenamePtr, fileToCompare, 0, false ) == 0 )
      return true

  return false

''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'' Time Methods and Formats
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
PRI GetTime(id) | ptr, headerOffset
  ptr := @udpdata
  FillHttpDate
  
  bytemove(ptr, string("<p>"),3)
  ptr += 3

  bytemove(ptr, @httpDate, strsize(@httpDate))
  ptr += strsize(@httpDate)
  
  bytemove(ptr, string("</p>"),4)
  ptr += 3  

  headerOffset := Response.BuildHeader(Request.GetExtension(id), 200, false)
  Socket.txTCP(id, @txdata, headerOffset)
  StringSend(id, @udpdata)
  bytefill(@udpdata, 0, TEMP_BUFFER)
  
  return
 
 
PRI FillTime | ptr, num
 'ToString(integerToConvert, destinationPointer)
 '00/00/0000 00:00:00
  ptr := @time
  rtc.readTime
  

  FillTimeHelper(rtc.clockMonth, ptr)
  ptr += 3

  FillTimeHelper(rtc.clockDate, ptr)
  ptr += 3

  FillTimeHelper(rtc.clockYear, ptr)
  ptr += 5

  FillTimeHelper(rtc.clockHour , ptr)
  ptr += 3

  FillTimeHelper(rtc.clockMinute , ptr)
  ptr += 3

  FillTimeHelper(rtc.clockSecond, ptr) 
 
  return @time


PRI FillHttpDate | ptr, num, temp
 'ToString(integerToConvert, destinationPointer)
 'Wed, 01 Feb 2000 01:00:00 GMT
  ptr := @httpDate
  rtc.readTime


  temp := rtc.getDayString
  bytemove(ptr, temp, strsize(temp))
  ptr += strsize(temp) + 2

  FillTimeHelper(rtc.clockDate, ptr)
  ptr += 3

  temp := rtc.getMonthString
  bytemove(ptr, temp, strsize(temp))
  ptr += strsize(temp) + 1

  FillTimeHelper(rtc.clockYear, ptr)
  ptr += 5

  FillTimeHelper(rtc.clockHour , ptr)
  ptr += 3

  FillTimeHelper(rtc.clockMinute , ptr)
  ptr += 3

  FillTimeHelper(rtc.clockSecond, ptr)
  
  return @httpDate
 

PRI FillTimeHelper(number, ptr) | offset
  offset := 0
  if(number < 10)
    offset := 1
     
  str.ToString(@number, @tempNum)
  bytemove(ptr+offset, @tempNum, strsize(@tempNum))
  

''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'' SDCard Logger
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
PRI AppendLog(logToAppend)
  '' logToAppend:  Pointer to a string of test we'd like to log
  SDCard.changeDirectory(@approot) 

  if(FileExists(@logfile))
    SDCard.openFile(@logfile, "A")
  else
    SDCard.newFile(@logfile)

  SDCard.writeData(string(13,10,"----- Start "),14)
  SDCard.writeData(FillTime, 19)
  SDCard.writeData(string(" -----"),6)
  SDCard.writeData(@crlf_crlf, 2)

  SDCard.writeData(logToAppend, strsize(logToAppend))
  SDCard.writeData(@crlf_crlf, 2)
  
  SDCard.writeData(string("----- End "),10)
  SDCard.writeData(FillTime, 19)
  SDCard.writeData(string(" -----"),6)
  SDCard.writeData(@crlf_crlf, 2)

  SDCard.closeFile

''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'' Memory Management
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
PRI Set(DestAddress, SrcAddress, Count)
  bytemove(DestAddress, SrcAddress, Count)
  bytefill(DestAddress+Count, $0, 1)
  

''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'' Socekt helpers
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''

PRI GetTcpSocketMask(id)
  return id & tcpMask

  
PRI DecodeId(value) | tmp
    if(%0001 & value)
      return 0
    if(%0010 & value)
      return 1
    if(%0100 & value)
      return 2 
    if(%1000 & value)
      return 3
  return -1


PRI QueueSocket(id) | tmp
  if(fifoSocketDepth > 4)
    return false

  tmp := |< id
  
  'Unique check
  ifnot(IsUnique(tmp))
    return false
    
  tmp <<= (fifoSocketDepth++) * 8
  
  fifoSocket |= tmp

  return true


PRI IsUnique(encodedId) | tmp
  tmp := encodedId & $0F
  repeat 4
    if(encodedId & fifoSocket)
      return false
    encodedId <<= 8
  return true 
    

PRI DequeueSocket | tmp
  if(fifoSocketDepth == 0)
    return -2
  repeat until not lockset(debugSemId) 
  tmp := fifoSocket & $0F
  fifoSocket >>= 8  
  fifoSocketDepth--
  lockclr(debugSemId)
  return DecodeId(tmp)

  
PRI ResetSocket(id)
  Socket.Disconnect(id)                                                                                                                                 
  Socket.Close(id)
  
PRI IsolateTcpSocketById(id) | tmp
  tmp := |< id
  tcpMask &= tmp


PRI SetTcpSocketMaskById(id, state) | tmp
'' The tcpMask contains the socket the the StatusMonitor monitors
  tmp := |< id
  
  if(state == 1)
    tcpMask |= tmp
  else
    tmp := !tmp
    tcpMask &= tmp 
    
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'' W5100 Helper methods
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
PRI GetCommandRegisterAddress(id)
  return Socket#_S0_CR + (id * $0100)

PRI GetStatusRegisterAddress(id)
  return Socket#_S0_SR + (id * $0100)

    
PRI SetMac(_firstOctet)
  Socket.WriteMACaddress(true, _firstOctet)
  return 


PRI SetGateway(_firstOctet)
  Socket.WriteGatewayAddress(true, _firstOctet)
  return 


PRI SetSubnet(_firstOctet)
  Socket.WriteSubnetMask(true, _firstOctet)
  return 


PRI SetIP(_firstOctet)
  Socket.WriteIPAddress(true, _firstOctet)
  return 



PRI StringSend(id, _dataPtr)
  Socket.txTCP(id, _dataPtr, strsize(_dataPtr))
  return 


PRI SendChar(id, _dataPtr)
  Socket.txTCP(id, _dataPtr, 1)
  return 

 
PRI SendChars(id, _dataPtr, _length)
  Socket.txTCP(id, _dataPtr, _length)
  return 


PRI InitializeSocket(id)
  Socket.Initialize(id, TCP_PROTOCOL, port, remotePort, @remoteIp)
  return

PRI InitializeSocketForEmail(id)
  Socket.Initialize(id, TCP_PROTOCOL, port, emailPort, @emailIp)
  return
  
PRI InitializeUPDSocket(id)
  Socket.Initialize(id, UDP_PROTOCOL, uport, remotePort, @remoteIp)
  return


''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
'' Debug/Display Methods
''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''
PRI QueueDump
  '' Display socket IDs in the queue
  '' ie 00000401 -> socket Zero is next to pop off
  'pst.str(string("FIFO["))
  'pst.dec(fifoSocketDepth)
  'pst.str(string("] "))
  'pst.hex(fifoSocket, 8)

    
PRI StackDump | clsd, open, lstn, estb, clwt, clng, id, ulst
  '' This method is purely for debugging
  '' It displays the status of all socket registers
  repeat until not lockset(debugSemId)
  clsd := closedState
  open := openState
  lstn := listenState
  estb := establishedState
  clwt := closeWaitState
  clng := closingState
  ulst := udpListen
  lockclr(debugSemId)

  'pst.char(13) 
  repeat id from 3 to 0
    'pst.dec(id)
    'pst.str(string("-"))
    'pst.hex(status[id], 2)
   'pst.str(string(" "))
    pause(1)

  'pst.str(string(13,"clsd open lstn estb clwt clng udps", 13))
  'pst.bin(clsd, 4)
  'pst.str(string("-"))
  'pst.bin(open, 4)
  'pst.str(string("-"))
  'pst.bin(lstn, 4)
  'pst.str(string("-"))  
  'pst.bin(estb, 4)
  'pst.str(string("-"))  
  'pst.bin(clwt, 4)
  'pst.str(string("-"))  
  'pst.bin(clng, 4)
  'pst.str(string("-"))  
  'pst.bin(ulst, 4)
  'pst.char(13)
   
PRI HeaderLine1(id) | i
  'pst.str(Request.GetMethod(id))
  'pst.char($20)

  i := 0
  repeat Request.GetDepth(id)
    'pst.char($2F)
    'pst.str(Request.GetPathNode(id, i++))
    
   
PRI Pause(Duration)  
  waitcnt(((clkfreq / 1_000 * Duration - 3932) #> 381) + cnt)
  return


PRI StatusMonitor | id, tmp, value
'' StatusMonitor is the heartbeat of the project
'' Here we monitor the state of the Wiznet 5100's 4 sockets
  repeat

    Socket.GetStatus32(@status[0])

    ' Encode status register states
    repeat until not lockset(debugSemId)

    closedState := openState := listenState := establishedState := {
     } closeWaitState := closingState := 0
     
    repeat id from 0 to 3
      case(status[id])
        $00: closedState               |= |< id
             closedState               &= tcpMask  
        $13: openState                 |= |< id
             openState                 &= tcpMask                   
        $14: listenState               |= |< id
             listenState               &= tcpMask
        $17: establishedState          |= |< id
             establishedState          &= tcpMask
        $18,$1A,$1B: closingState      |= |< id
                     closingState      &= tcpMask
        $1C: closeWaitState            |= |< id
             closeWaitState            &= tcpMask
        $1D: closingState              |= |< id
             closingState              &= tcpMask
        $22: udpListen                 |= |< id
             udpListen                 &= udpMask 

    if(lastEstblState <> establishedState)
      value := establishedState
      repeat while value > 0
        tmp := DecodeId(value)
        if(tmp > -1)
          QueueSocket(tmp)
          tmp := |< tmp
          tmp := !tmp
          value &= tmp
      lastEstblState := establishedState

    lockclr(debugSemId)
    
    ' Initialize a closed socket 
    if(closedState > 0)
      id := DecodeId(closedState)
      if(id > -1)
        InitializeSocket(id & tcpMask)
    
    'Start a listener on an initialized/open socket   
    if(openState > 0)
      id := DecodeId(openState)
      if(id > -1)
        Socket.Listen(id & tcpMask)

    ' Close the socket if the status is close/wait
    ' response processor should close the socket with disconnect
    ' there could be a timeout so we have a forced close.
    ' TODO: CCheck for a port that gets stuck in a closing state
    'if(closeWaitState > 0)
      'id := DecodeId(closeWaitState)
      'if(id > -1)
        'Socket.Close(id & tcpMask)



    'pause(100)
return

{PUB GetMyIp(id) | size, idx, tempMask

    ' Save tcp mask and remove the requested ID from the StatusMonitor
    tempMask := tcpMask
    tcpMask := SetTcpSocketMaskById(id, 0)
    
    Socket.Close(id)
    pause(delay)
    InitializeSocket2(id)
    pause(delay)
    
    ' Connect to the remote server

    'pst.str(string(13, "Getting Assigned IP"))
    Socket.Connect(id)
    pause(delay)
    
    repeat while !Socket.Connected(id)
    
    
    'pst.str(string(13, "Connected... Sending Header"))
    
    ' Build and send the request
    dynamicContentPtr := @tempBuff 
    PushDynamicContent(string("GET /nic/update?hostname=dmoody256.servebeer.com HTTP/1.0", 13, 10))
    PushDynamicContent(string("User-Agent: DmoodySeniorDesign/1.0 dmoody256@gmail.com", 13, 10))
    PushDynamicContent(string("Host: dynupdate.no-ip.com", 13, 10))
    PushDynamicContent(string("Authorization: Basic ZG1vb2R5MjU2QGdtYWlsLmNvbTpwYWludDA2", 13, 10, 13, 10))
    StringSend(id, @tempBuff)
    
    'pst.str(string(13, "Waiting for response ",13))
    
    ' Clear the buffer
    bytefill(@tempBuff, 0, TEMP_BUFFER)
    
    ' Wait for the response
    repeat until size := Socket.rxTCP(id, @rxdata)
    
    ' Find and save message body
    ' Write the message to the terminal
    idx := str.MatchPattern(@rxdata, string(13,10,13,10), 0, true)
    bytemove(@tempBuff, @rxdata+idx+4, strsize(@rxdata+idx+4)) 
    'pst.str(@tempBuff)
    
    'pst.str(string(13, "Disconnect and reset socket: "))
    'pst.dec(id)
    'pst.char(13)
    
    ' Reset the socket
    Socket.Disconnect(id)
    pause(delay)
    
    ' Reset the tcpMask 
    tcpMask := tempMask
    
    InitializeSocket(id)
    pause(delay)
    
    return  }


PRI InitializeSocket2(id)
  Socket.Initialize(id, TCP_PROTOCOL, port2, remotePort, @remoteIp)
  return

PUB readDataToBuffer(DBNameString)  | FileSize, errpr

  SDCard.listEntry(DBNameString)
  FileSize := SDCard.getFileSize
  
  LEptr := \SDCard.openFile(DBNameString, "R")
  
  
  SDCard.readFromFile(@UserDataBuffer, FileSize)
  
  'pst.str(@UserDataBuffer)
   
  SDCard.closeFile

  SDCard.deleteEntry(DBNameString)

  return FileSize
  
PUB AddUserInfo(UserInfoString, DBNameString)

  LEptr := \SDCard.openFile(DBNameString, "A")
 
  SDCard.writeData(UserInfoString, strsize(UserInfoString))

  SDCard.closeFile 

PUB EditUser(UserEditString, EditCode, DBNameString, FindString) | NewBufferptr, TempPtr, BuiltString, ModNumber, ModCounter, MStimedString 

  readDataToBuffer(DBNameString)

  SDCard.newFile(DBNameString)
  
  SDCard.openFile(DBNameString, "W")

  
  
  StringENG.stringCopy(@TempEditData, UserEditString)
  'pst.str(@TempEditData)

  StringENG.replaceCharacter(UserEditString, 32, 0)

  'pst.str(UserEditString)
   
   TempPtr := @UserDataBuffer 

   ModCounter := 0
   
   repeat 10

      ModCounter := ModCounter + 1
   
      NewBufferptr := StringENG.replaceCharacter(TempPtr, 13, 0)
       
       'pst.str(TempPtr)
       
      if(NewBufferptr == 0)
        ModNum := ModCounter
        quit   
     
      StringENG.stringCopy(@TempUserData, TempPtr)
      
      'pst.str(@TempUserData) 

      StringENG.replaceCharacter(TempPtr, 32, 0)

      'pst.str(TempPtr)
       
      'pst.str(TempPtr)
      'pst.str(UserEditString)
      'pst.str(string(13))
       
       
        if(strcomp(TempPtr,UserEditString) and EditCode)
           'pst.str(string("writing edit to disk"))
           'pst.str(@TempEditData)
           'pst.str(string(13))
           if(FindString == 0)
             SDCard.writeData(@TempEditData, strsize(@TempEditData))
             SDCard.writeData(string(13), 1)
           else  
             ModNumber := StringENG.decimalToInteger(TempPtr)


             MStimedString := StringENG.replaceCharacter(@TempUserData, 44, 44)
             StringENG.replaceCharacter(MStimedString, 46, 0)

             MStimed[ModNumber] := StringENG.decimalToInteger(MStimedString)
             
             if(MStimed[ModNumber] == 1)
                 StringENG.buildString(",")
                 StringENG.buildString("1")
             else
                 StringENG.buildString(",")
                 StringENG.buildString("0")
             
             if(ModRelay[ModNumber] == 1)
                 StringENG.buildString(".")
                 StringENG.buildString("1")
             else
                 StringENG.buildString(".")
                 StringENG.buildString("0")

             if(ModManual[ModNumber] == 1)
                 StringENG.buildString(" ")
                 StringENG.buildString("1")
             else
                 StringENG.buildString(" ")
                 StringENG.buildString("0")

             if(ModMS[ModNumber] == 1)
                 StringENG.buildString(" ")
                 StringENG.buildString("1")
             else
                 StringENG.buildString(" ")
                 StringENG.buildString("0")

             if(ModSS[ModNumber] == 1)
                 StringENG.buildString(" ")
                 StringENG.buildString("1")
                 
             else
                 StringENG.buildString(" ")
                 StringENG.buildString("0")

             
                 

             BuiltString := StringENG.builtString(0)

             

             
                      
             StringENG.replaceCharacter(@TempUserData, 44, 0)

             StringENG.stringConcatenate(@TempUserData, BuiltString)
             
             SDCard.writeData(@TempUserData, strsize(@TempUserData))
             SDCard.writeData(string(13), 1)

             BuiltString := StringENG.builtString(1)
             
              
           
        
        elseif(strcomp(TempPtr,UserEditString) and not Editcode)
                                   'pst.str(string("skipping write to disk, deleting"))
                                   'pst.str(@TempUserData)
                                   'pst.str(string(13))
                                   
        
              
        
        elseif(not strcomp(TempPtr,UserEditString))
                                   'pst.str(string("writing current to disk"))
                                   'pst.str(@TempUserData)
                                   'pst.str(string(13))
                                   SDCard.writeData(@TempUserData, strsize(@TempUserData))
                                   SDCard.writeData(string(13), 1) 
    TempPtr :=  NewBufferptr  

    
   SDCard.closeFile

   
   
PUB XbeeCommands(ModNumber, loop)  
                                          ' Configure for fast AT Command Mode 
    
   dira[24] := 1
   dira[23] := 1
   dira[25] := 0
   dira[26] := 0
   dira[27] := 0
   

        if(loop == 0)
                                if(ModRelay[ModNumber] == 1)
                                     ModRelay[ModNumber] := 0
                                     outa[24] := 0
                                     
                                else 
                                     ModRelay[ModNumber] := 1
                                     outa[24]:= 1

        else

        
                        if(ModRelay[ModNumber] == 1)
                                     
                                     outa[24] := 1
                                    
                        else 
                                     
                                     outa[24]:= 0
                                     
        
        XB.AT_ConfigVal(string("ATDL"),ModNumber)
        XB.AT_ConfigVal(string("ATMY"),0)
        XB.AT_ConfigVal(string("ATIA"),ModNumber)
        
        XB.API_RemConfig(ModNumber,string("IA"),$0)
        XB.API_RemConfig(ModNumber,string("DL"),$0)  
        XB.Delay(35)

        if(ina[27])
          ModSS[ModNumber] := 1
          
        else
          ModSS[ModNumber] := 0
            

        if(ina[26])
          ModMS[ModNumber] := 1
             if(MStimed[ModNumber] == 1)
                                ModRelay[ModNumber] := 1
                                outa[24]:= 1
                                MStimer[ModNumber] := 0
          
        else
          ModMS[ModNumber] := 0
            if(MStimed[ModNumber] == 1)
               if(MStimer[ModNumber] < 2 and ModRelay[ModNumber])
                                MStimer[ModNumber] := MStimer[ModNumber] + 1
               else
                                 ModRelay[ModNumber] := 0
                                 outa[24]:= 0
                                 MStimer[ModNumber] := 0
           
            
        
        
        XB.API_RemConfig(ModNumber,string("DL"),$0950)
        XB.API_RemConfig(ModNumber,string("IA"),$0685)
         
        XB.AT_ConfigVal(string("ATIA"),$1320)
        XB.AT_ConfigVal(string("ATDL"),$1000)
        XB.AT_ConfigVal(string("ATMY"),$675)


       { else

        
                        if(ModRelay[ModNumber] == 1)
                                     
                                     outa[24] := 1
                                    
                        else 
                                     
                                     outa[24]:= 0
                                     
          
          XB.Delay(25)
          XB.AT_ConfigVal(string("ATDL"),ModNumber)
          XB.AT_ConfigVal(string("ATMY"),0)
          XB.Delay(25)
          XB.AT_ConfigVal(string("ATIA"),ModNumber)
          XB.Delay(25)

                 if(ModNumber == 1)
                                ifnot(ina[27])
                                   ModSS[ModNumber] := 1
                                 
                                     outa[23] := 1
                                else
                                     outa[23] := 0
                                 
          
          XB.AT_ConfigVal(string("ATIA"),1000)
          XB.Delay(25)
          XB.AT_ConfigVal(string("ATDL"),1000)
          XB.AT_ConfigVal(string("ATMY"),675)    }

        
       
  'Send AT command turn off Association LED


   
    
 {PUB readDataToBuffer | FileSize, errpr

  SDCard.listEntry(string("USER.TXT"))
  FileSize := SDCard.getFileSize
  
  LEptr := \SDCard.openFile(string("USER.TXT"), "R")
  
  
  SDCard.readFromFile(@UserDataBuffer, FileSize)
  
  'pst.str(@UserDataBuffer)
   
  SDCard.closeFile

  SDCard.deleteEntry(string("USER.TXT"))

  return FileSize
  
PUB AddUserInfo(UserInfoString)

  LEptr := \SDCard.openFile(string("USER.TXT"), "A")
 
  SDCard.writeData(UserInfoString, strsize(UserInfoString))

  SDCard.closeFile 

PUB EditUser(UserEditString, EditCode) | NewBufferptr, TempPtr  

  readDataToBuffer

  SDCard.newFile(string("USER.TXT"))
  
  SDCard.openFile(string("USER.TXT"), "W")

  
  StringENG.stringCopy(@TempEditData, UserEditString)
  'pst.str(@TempEditData)

  StringENG.replaceCharacter(UserEditString, 32, 0)

  'pst.str(UserEditString)
   
   TempPtr := @UserDataBuffer 
   
   
   repeat 10
      
      NewBufferptr := StringENG.replaceCharacter(TempPtr, 13, 0)
       
       'pst.str(TempPtr)
       
      if(NewBufferptr == 0)
         
        quit  
     
      StringENG.stringCopy(@TempUserData, TempPtr)
      
      'pst.str(@TempUserData) 

      StringENG.replaceCharacter(TempPtr, 32, 0)

      'pst.str(TempPtr)
       
      'pst.str(TempPtr)
      'pst.str(UserEditString)
      'pst.str(string(13))
       
       
        if(strcomp(TempPtr,UserEditString) and EditCode)
           'pst.str(string("writing edit to disk"))
           'pst.str(@TempEditData)
           'pst.str(string(13))
           SDCard.writeData(@TempEditData, strsize(@TempUserData))
           SDCard.writeData(string(13), 1)
        
        if(strcomp(TempPtr,UserEditString) and not Editcode)
                                   'pst.str(string("skipping write to disk, deleting"))
                                   'pst.str(@TempUserData)
                                   'pst.str(string(13))
                                   

        if(not strcomp(TempPtr,UserEditString))
                                   'pst.str(string("writing current to disk"))
                                   'pst.str(@TempUserData)
                                   'pst.str(string(13))
                                   SDCard.writeData(@TempUserData, strsize(@TempUserData))
                                   SDCard.writeData(string(13), 1) 
    TempPtr :=  NewBufferptr  

    
   SDCard.closeFile   }

    
DAT
  approot               byte    "\", 0 
  defaultpage           byte    "index.htm", 0
  logfile               byte    "log.txt", 0
  'binFile               byte    "filename.bin", 0
  FS                    byte    "/", 0
  fn                    byte    "filename=", 0
  doublequote           byte    $22, 0
  crlf                  byte    13, 10, 0
  crlf_crlf             byte    13, 10, 13, 10, 0
  uploadfile            byte    $0[12], 0
  uploadfolder          byte    "uploads", 0
  tempNum               byte    "0000",0
  multipart             byte    "Content-Type: multipart/form-data; boundary=",0
  boundary              byte    $2D, $2D
  boundary1             byte    $0[64]
  'loadme                file    "TogglePin0.binary"
  port2                 word    5010                 

{{
┌──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                                   TERMS OF USE: MIT License                                                  │                                                            
├──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┤
│Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation    │ 
│files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,    │
│modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software│
│is furnished to do so, subject to the following conditions:                                                                   │
│                                                                                                                              │
│The above copyright notice and this permission notice shall be included in all copies or substantial ions of the Software.│
│                                                                                                                              │
│THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE          │
│WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR         │
│COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,   │
│ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                         │
└──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┘
}}