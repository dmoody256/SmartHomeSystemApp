'SD Mount test
CON
  _clkmode = xtal1 + pll16x
  _xinfreq = 5_000_000

   DO  = 0       'Set pins for SD connections on spinneret board
   CLK = 1
   DI  = 2
   CS  = 3

DAT

fileErrorHandle          long    $0
UserDataBuffer           byte    $0[$600]
TempUserData             byte    $0[$60]
TempEditData             byte    $0[$60]

OBJ
   SDCard        : "S35390A_SD-MMC_FATEngineWrapper"  
   pst           : "Parallax Serial Terminal"
   rtc           : "S35390A_RTCEngine"
   StringENG     : "ASCII0_STREngine_1"

VAR

  
  
  long LEptr
  
  long error
   byte ModNum
  
PUB Main | temp, SlowCount

  SDCard.Start
  pst.Start(115_200)
  pause(1000)  'Increase the 4 if more time is required
  ModNum := 3
  'Attempt to mount the SD card.
  'Report a failure to mount if the card is not found.
  'Halt if the card fails to mount.
  pst.str(string("Mount SD Card - ")) 
  SDCard.mount(fileErrorHandle)
  pst.str(string("OK",13))

  LEptr := string("File Found")  


  temp := 1
  SlowCount := 0
  repeat
         SlowCount := SlowCount + 1
         if(SlowCount == 10)
                                SlowCount := 0
                                temp := temp + 1
                                pst.Dec(temp)
                               pst.str(string(13)) 
                                'XBeeCommands(temp, 1)
                                if(temp == ModNum)
                                  temp := 1

  
  'AddUserInfo(string("Dmoody257 paint06 daniel 1", 13))
  
  
  

  
  'EditUser(string("Dmoody253 paint11 daniel 1"), 0)
  
  

  
PUB readDataToBuffer | FileSize

  SDCard.listEntry(string("USER.TXT"))
  FileSize := SDCard.getFileSize
  
  LEptr := \SDCard.openFile(string("USER.TXT"), "R")
  
  pst.str(LEptr)
  pst.str(string(13))
  
  if(strcomp(LEptr, string("Entry Not Found")))
  
      SDCard.newFile(string("USER.TXT"))
      
      pst.str(string("New File Created", 13))
      
      SDCard.openFile(string("USER.TXT"), "R")

      pst.str(string("File opened",13))

      return 0
  
  SDCard.readFromFile(@UserDataBuffer, FileSize)

  pst.str(@UserDataBuffer)
   
  SDCard.closeFile

  SDCard.deleteEntry(string("USER.TXT"))

  return FileSize
  
PUB AddUserInfo(UserInfoString)

  LEptr := \SDCard.openFile(string("USER.TXT"), "A")
  
  pst.str(LEptr)
  pst.str(string(13))
  
  if(strcomp(LEptr, string("Entry Not Found")))
  
      SDCard.newFile(string("USER.TXT"))
      
      pst.str(string("New File Created", 13))

      SDCard.openFile(string("USER.TXT"), "A")

  pst.str(UserInfoString)
  pst.str(string(13))
 
  SDCard.writeData(UserInfoString, strsize(UserInfoString))

  SDCard.closeFile 

PUB EditUser(UserEditString, EditCode) | NewBufferptr, TempPtr 

  readDataToBuffer

  
  SDCard.newFile(string("USER.TXT"))
      
  pst.str(string("New File Created", 13))

  SDCard.openFile(string("USER.TXT"), "W")

  
  StringENG.stringCopy(@TempEditData, UserEditString)
  pst.str(@TempEditData)

  StringENG.replaceCharacter(UserEditString, 32, 0)

   TempPtr := @UserDataBuffer 

   repeat 10

      NewBufferptr := StringENG.replaceCharacter(TempPtr, 13, 0)

      if(NewBufferptr == 0)
        quit  
       
      StringENG.stringCopy(@TempUserData, TempPtr)
       
       
      StringENG.replaceCharacter(TempPtr, 32, 0)
       
       
      pst.str(TempPtr)
      pst.str(UserEditString)
      pst.str(string(13))
       
       
        if(strcomp(TempPtr,UserEditString) and EditCode)
           pst.str(string("writing edit to disk"))
           pst.str(@TempEditData)
           pst.str(string(13)) 
           SDCard.writeData(@TempEditData, strsize(@TempUserData))
           SDCard.writeData(string(13), 1)
        
        if(strcomp(TempPtr,UserEditString) and not Editcode)
                                   pst.str(string("skipping write to disk, deleting"))
                                   pst.str(@TempUserData)
                                   pst.str(string(13)) 
                                   

        if(not strcomp(TempPtr,UserEditString))
                                   pst.str(string("writing current to disk"))
                                   pst.str(@TempUserData)
                                   pst.str(string(13)) 
                                   SDCard.writeData(@TempUserData, strsize(@TempUserData))
                                   SDCard.writeData(string(13), 1) 
    TempPtr :=  NewBufferptr  

    
   SDCard.closeFile   


  
PRI Pause(Duration)  
  waitcnt(((clkfreq / 1_000 * Duration - 3932) #> 381) + cnt)
  return