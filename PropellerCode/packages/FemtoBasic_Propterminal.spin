'' Propterminal FemtoBASIC 2.004
'' Based on FemtoBASIC 2.004 by Mike Green
'' Original Copyright 2006, Radical Eye Software.

'' This version has been modified for use with Propterminal
'' Additional Support has been added for a few graphics commands      
'' supported by PC_Interface.     
''     
'' This version was created with the idea of making it easier      
'' for beginners to get started as well as robotics projects.
     
'' Much of the selectivity for Hydra/Proto/Demo has been left
'' in place, although it has been only tested with the Proto.

'' Modified by Jeff Ledger (See forums for support)
'' 
'' Additional commands have been added. (See below)

'' NEW COMMANDS: {specific to Propterminal Femtobasic}
''
''            HOME        Home cursor
''            CLR         Clear screen
''            COLOR #     # = 1 - 8
''            LOCATE X,Y  Locate cursor 
''            REBOOT      Reboot Propeller
''            PLOT X,Y    Plot a dot at X,Y
''            DRAWTO X,Y  Draw a line from X to Y


obj
   fsrw  : "fsrwFemto"                 ' FAT File System Read/Write
   trm   : "PC_Interface"              ' Propterminal Keyboard / Mouse Driver

con
   Demo      = true                    ' Demo Board
   Proto     = false                   ' Proto Board
   Hydra     = false                   ' Hydra

   version   = 2
   release   = 4
   
   progsize  = 8192                    ' Space reserved for program
   _clkmode  = xtal1 + ((Hydra & pll8x) | ((Demo | Proto) & pll16x))
   _xinfreq  = (Hydra & 10_000_000) | ((Demo | Proto) & 5_000_000)
   _stack    = 100                     ' Roughly 4 nested expressions
   _free = 1000

   trmPins   = 16
   spiDO     = (Hydra & 16) | (Demo & 0) | (Proto & 8)
   spiClk    = (Hydra & 17) | (Demo & 1) | (Proto & 9)
   spiDI     = (Hydra & 18) | (Demo & 2) | (Proto & 10)
   spiCS     = (Hydra & 19) | (Demo & 3) | (Proto & 11)

   bspKey    = $C8                     ' PS/2 keyboard backspace key
   breakKey  = $CB                     ' PS/2 keyboard escape key
   backspace = 8
   fReturn   = 13
   fLinefeed = 10
   fEof      = -1
      
   maxstack  = 20                      ' Maximum stack depth
   linelen   = 256                     ' Maximum input line length
   quote     = 34                      ' Double quote
   caseBit   = !32                     ' Uppercase/Lowercase bit
   userPtr   = $7FEC                   ' Pointer to program memory  ' TV
'   userPtr   = trm#sync - 4            ' Pointer to program memory  ' VGA
   
var
   long sp, tp, eop, nextlineloc, rv, curlineno, pauseTime
   long vars[26], stack[maxstack], control[2]
   long forStep[26], forLimit[26], forLoop[26]
   long ioControl[2]
   word outputs
   byte tline[linelen], tailLine[linelen], inVars[26], fileOpened

dat
   tok0  byte "IF", 0
   tok1  byte "THEN", 0
   tok2  byte "INPUT", 0    ' INPUT {"<prompt>";} <var> {,<var>}
   tok3  byte "PRINT", 0    ' PRINT {USING "<format>";} ...
   tok4  byte "GOTO", 0
   tok5  byte "GOSUB", 0
   tok6  byte "RETURN", 0
   tok7  byte "REM", 0
   tok8  byte "NEW", 0
   tok9  byte "LIST", 0
   tok10 byte "RUN", 0
   tok11 byte "RND", 0
   tok12 byte "OPEN", 0     ' OPEN " <file> ",<mode>
   tok13 byte "READ", 0     ' READ <var> {,<var>}
   tok14 byte "WRITE", 0    ' WRITE {USING "<format>";} ...
   tok15 byte "CLOSE", 0    ' CLOSE
   tok16 byte "DELETE", 0   ' DELETE " <file> "
   tok17 byte "RENAME", 0   ' RENAME " <file> "," <file> "
   tok18 byte "FILES", 0    ' FILES
   tok19 byte "SAVE", 0     ' SAVE { " <file> " }
   tok20 byte "LOAD", 0     ' LOAD { " <file> " }
   tok21 byte "NOT" ,0      ' NOT <logical>
   tok22 byte "AND" ,0      ' <logical> AND <logical>
   tok23 byte "OR", 0       ' <logical> OR <logical>
   tok24 byte "SHL", 0      ' <expr> SHL <expr>
   tok25 byte "SHR", 0      ' <expr> SHR <expr>
   tok26 byte "FOR", 0      ' FOR <var> = <expr> TO <expr>
   tok27 byte "TO", 0
   tok28 byte "STEP", 0     '  optional STEP <expr>
   tok29 byte "NEXT", 0     ' NEXT <var>
   tok30 byte "INA", 0      ' INA [ <expr> ]
   tok31 byte "OUTA", 0     ' OUTA [ <expr> ] = <expr>
   tok32 byte "PAUSE", 0    ' PAUSE <time ms> {,<time us>}
   tok33 byte "USING", 0    ' PRINT USING "<format>"; ...
   tok34 byte "ROL", 0      ' <expr> ROL <expr>
   tok35 byte "ROR", 0      ' <expr> ROR <expr>
   tok36 byte "SAR", 0      ' <expr> SAR <expr>
   tok37 byte "REV", 0      ' <expr> REV <expr>
   tok38 byte "BYTE", 0     ' BYTE [ <expr> ]
   tok39 byte "WORD", 0     ' WORD [ <expr> ]
   tok40 byte "LONG", 0     ' LONG [ <expr> ]
   tok41 byte "CNT", 0
   tok42 byte "PHSA", 0
   tok43 byte "PHSB", 0
   tok44 byte "FRQA", 0
   tok45 byte "FRQB", 0
   tok46 byte "CTRA", 0
   tok47 byte "CTRB", 0
   tok48 byte "DISPLAY", 0  ' DISPLAY <expr> {,<expr>}
   tok49 byte "KEYCODE", 0  ' KEYCODE
   tok50 byte "LET", 0
   tok51 byte "STOP", 0
   tok52 byte "END", 0
   tok53 byte "EEPROM", 0   ' EEPROM[ <expr> ]
   tok54 byte "FILE", 0     ' FILE
   tok55 byte "MEM", 0      ' MEM
   tok56 byte "SPIN", 0     ' SPIN [<expr>] or SPIN "<file>"
   tok57 byte "COPY", 0     ' COPY [<expr>],"<file>" or COPY "<file>",[<expr>] or
                            ' COPY [<expr>],<expr> where <expr> are different
   tok58 byte "DUMP", 0     ' DUMP <expr>,<expr>

   ' Propterminal Specific Tokens by Jeff Ledger

   tok59 byte "REBOOT", 0  ' Reboot Propeller
   tok60 byte "CLR", 0     ' CLRear screen
   tok61 byte "HOME", 0    ' HOME cursor
   tok62 byte "LOCATE", 0  ' LOCATE <exprX>,<exprY> 
   tok63 byte "COLOR", 0   ' COLOR #
   tok64 byte "PLOT", 0    ' PLOT <exprX>,<exprY>
   tok65 byte "DRAWTO" , 0 ' PLOT <exprX>,<exprY>
   

   toks  word @tok0, @tok1, @tok2, @tok3, @tok4, @tok5, @tok6, @tok7
         word @tok8, @tok9, @tok10, @tok11, @tok12, @tok13, @tok14, @tok15
         word @tok16, @tok17, @tok18, @tok19, @tok20, @tok21, @tok22, @tok23
         word @tok24, @tok25, @tok26, @tok27, @tok28, @tok29, @tok30, @tok31
         word @tok32, @tok33, @tok34, @tok35, @tok36, @tok37, @tok38, @tok39
         word @tok40, @tok41, @tok42, @tok43, @tok44, @tok45, @tok46, @tok47
         word @tok48, @tok49, @tok50, @tok51, @tok52, @tok53, @tok54, @tok55
         word @tok56, @tok57, @tok58, @tok59, @tok60, @tok61, @tok62, @tok63
         word @tok64, @tok65
   tokx  word

   syn   byte "Syntax Error", 0
   ln    byte "Invalid Line Number", 0

PUB main | err, s
'' Clear the program space and variables, then read a line and interpret it.
   trm.start(31,30)                            ' Start keyboard driver
   fsrw.start(@ioControl)                        ' Start I2C/SPI driver
   pauseTime := 0
   outputs := 0
   fileOpened := 0
   long[userPtr] := userPtr - progsize           ' Allocate memory
   waitcnt(clkfreq + cnt)
   trm.out($00)
   trm.setcol(6)
   trm.str(string("Propterminal FemtoBasic "))
   trm.dec(version)
   trm.out(".")
   if release < 100
      trm.out("0")
   if release < 10
      trm.out("0")
   trm.dec(release)
   trm.out(fReturn)
   waitcnt(clkfreq + cnt)
   trm.setcol(8)
   clearall
   s := 0
   curlineno := -1
   ifnot \fsrw.mount(spiDO,spiClk,spiDI,spiCS) < 0
      ifnot \fsrw.popen(string("autoexec.bas"),"r")
         if (err := \processLoad) <> fEof
            showError(err)
            newprog
         ifnot \fsrw.pclose < 0
            \fsrw.unmount
         if err == fEof
            s := string("run")
   repeat
      err := \doline(s)
      s := 0
      if err
         showError(err)

PRI showError(err)
   if curlineno => 0
      trm.str(string("IN LINE "))
      trm.dec(curlineno)
      trm.out(" ")
   if err < 0
      trm.str(string("SD card "))
      trm.dec(err)
      trm.out(13)
   else
      putlinet(err)
   nextlineloc := eop - 2

PRI getline | i, c

   i := 0
   repeat
'      c := key.getkey
      c := trm.getkey
      if c == bspKey
         if i > 0
            trm.str(string(backspace," ",backspace))
            i--
      elseif c == fReturn
         trm.out(c)
         tline[i] := 0
         tp := @tline
         return
      elseif i < linelen-1
         trm.out(c)
         tline[i++] := c

pri putlinet(s) | c, ntoks
   ntoks := (@tokx - @toks) / 2
   repeat while c := byte[s++]
      if c => 128
         if (c -= 128) < ntoks
            trm.str(@@toks[c])
            if c <> 7   ' REM
               trm.out(" ")
         else
            trm.out("{")
            trm.dec(c)
            trm.out("}")
      else
         trm.out(c)
   trm.out(fReturn)

pri spaces | c
   repeat
      c := byte[tp]
      if c == 0 or c > " "
         return c
      tp++

pri skipspaces
   if byte[tp]
      tp++
   return spaces

pri parseliteral | r, c
   r := 0
   repeat
      c := byte[tp]
      if c < "0" or c > "9"
         return r
      r := r * 10 + c - "0"
      tp++

pri movprog(at, delta)
   if eop + delta + 2 - long[userPtr] > progsize
      abort string("NO MEMORY")
   bytemove(at+delta, at, eop-at)
   eop += delta

pri fixvar(c)
   if c => "a"
      c -= 32
   return c - "A"

pri isvar(c)
   c := fixvar(c)
   return c => 0 and c < 26

pri tokenize | tok, c, at, put, state, i, j, ntoks
   ntoks := (@tokx - @toks) / 2
   at := tp
   put := tp
   state := 0
   repeat while c := byte[at]
      if c == quote
         if state == "Q"
            state := 0
         elseif state == 0
            state := "Q"
      if state == 0
         repeat i from 0 to ntoks-1
            tok := @@toks[i]
            j := 0
            repeat while byte[tok] and ((byte[tok] ^ byte[j+at]) & caseBit) == 0
               j++
               tok++
            if byte[tok] == 0 and not isvar(byte[j+at])
               byte[put++] := 128 + i
               at += j
               if i == 7
                  state := "R"
               else
                  repeat while byte[at] == " "
                     at++
                  state := "F"
               quit
         if state == "F"
            state := 0
         else
            byte[put++] := byte[at++]
      else
         byte[put++] := byte[at++]
   byte[put] := 0

pri wordat(loc)
   return (byte[loc]<<8)+byte[loc+1]

pri findline(lineno) | at
   at := long[userPtr]
   repeat while wordat(at) < lineno
      at += 3 + strsize(at+2)
   return at

pri insertline | lineno, fc, loc, locat, newlen, oldlen
   lineno := parseliteral
   if lineno < 0 or lineno => 65535
      abort @ln
   tokenize
   fc := spaces
   loc := findline(lineno)
   locat := wordat(loc)
   newlen := 3 + strsize(tp)
   if locat == lineno
      oldlen := 3 + strsize(loc+2)
      if fc == 0
         movprog(loc+oldlen, -oldlen)
      else
         movprog(loc+oldlen, newlen-oldlen)
   elseif fc
      movprog(loc, newlen)
   if fc
      byte[loc] := lineno >> 8
      byte[loc+1] := lineno
      bytemove(loc+2, tp, newlen-2)

pri clearvars
   bytefill(@vars, 0, 26)
   pauseTime := 0
   nextlineloc := long[userPtr]
   sp := 0

pri newprog
   byte[long[userPtr]][0] := 255
   byte[long[userPtr]][1] := 255
   byte[long[userPtr]][2] := 0
   eop := long[userPtr] + 2
   nextlineloc := eop - 2
   sp := 0

pri clearall
   newprog
   clearvars

pri pushstack
   if sp => constant(maxstack-1)
      abort string("RECURSION ERROR")
   stack[sp++] := nextlineloc

pri getAddress(delim) | t
   if spaces <> "["
      abort @syn
   skipspaces
   result := expr
   if delim == "." and (result < 0 or result > 31)
      abort string("Invalid pin number")
   if delim == "." or delim == ","
      if spaces == delim
         if delim == "."             ' Handle the form <expr>..<expr>
            if byte[++tp] <> "."
               abort @syn
            result <<= 8
            skipspaces
            t := expr
            if t < 0 or t > 31
               abort string("Invalid pin number")
            result |= t | $10000
         else                        ' Handle the form <expr>,<expr>
            if result & 1 or result < 0 or result > 31
               abort string("Invalid pin number")
            skipspaces
            result := (result << 18) | (expr & $7FFFF)
      elseif delim == ","
         result := (result & $7FFFF) | fsrw#bootAddr
   if spaces <> "]"
      abort @syn
   tp++

pri factor | tok, t, i
   tok := spaces
   tp++
   case tok
      "(":
         t := expr
         if spaces <> ")"
            abort @syn
         tp++
         return t
      "a".."z","A".."Z":
         return vars[fixvar(tok)]
      158: ' INA [ <expr>{..<expr>} ]
         t := getAddress(".")
         if t > $FFFF
           tok := t & $FF
           t := (t >> 8) & $FF
           repeat i from t to tok
              outputs &= ! |< i
           dira[t..tok]~
           return ina[t..tok]
         else
           outputs &= ! |< t
           dira[t]~
           return ina[t]
      166: ' BYTE [ <expr> ]
         return byte[getAddress(" ")]
      167: ' WORD [ <expr> ]
         return word[getAddress(" ")]
      168: ' LONG [ <expr> ]
         return long[getAddress(" ")]
      181: ' EEPROM [ <expr> ]
         t := getAddress(",")
         if fsrw.readEEPROM(t,@t,1)
            abort string("EEPROM read")
         return t & $FF
      182: ' FILE
         return fsrw.pgetc
      183: ' MEM
         return progsize - (eop - long[userPtr] )
      169: ' CNT
         return CNT
      170: ' PHSA
         return PHSA
      171: ' PHSB
         return PHSB
      172: ' FRQA
         return FRQA
      173: ' FRQB
         return FRQB
      174: ' CTRA
         return CTRA
      175: ' CTRB
         return CTRB
      177: ' KEYCODE
        ' return key.key
      139: ' RND <factor>
         return (rv? >> 1) ** (factor << 1)
      "-":
         return - factor
      "!":
         return ! factor
      "$", "%", quote, "0".."9":
         --tp
         return getAnyNumber
      other:
         abort(@syn)

pri shifts | tok, t
   t := factor
   tok := spaces
   if tok == 152 ' SHL
      tp++
      return t << factor
   elseif tok == 153 ' SHR
      tp++
      return t >> factor
   elseif tok == 162 ' ROL
      tp++
      return t <- factor
   elseif tok == 163 ' ROR
      tp++
      return t -> factor
   elseif tok == 164 ' SAR
      tp++
      return t ~> factor
   elseif tok == 165 ' REV
      tp++
      return t >< factor
   else
      return t

pri bitFactor | tok, t
   t := shifts
   repeat
      tok := spaces
      if tok == "&"
         tp++
         t &= shifts
      else
         return t

pri bitTerm | tok, t
   t := bitFactor
   repeat
      tok := spaces
      if tok == "|"
         tp++
         t |= bitFactor
      elseif tok == "^"
         tp++
         t ^= bitFactor
      else
         return t

pri term | tok, t
   t := bitTerm
   repeat
      tok := spaces
     if tok == "*"
        tp++
        t *= bitTerm
     elseif tok == "/"
        if byte[++tp] == "/"
           tp++
           t //= bitTerm
        else
           t / =bitTerm
     else
        return t

pri arithExpr | tok, t
   t := term
   repeat
      tok := spaces
      if tok == "+"
         tp++
         t += term
      elseif tok == "-"
         tp++
         t -= term
      else
         return t

pri compare | op, a, b, c
   a := arithExpr
   op := 0
   spaces
   repeat
      c := byte[tp]
      case c
         "<": op |= 1
              tp++
         ">": op |= 2
              tp++
         "=": op |= 4
              tp++
         other: quit
   case op
      0: return a
      1: return a < arithExpr
      2: return a > arithExpr 
      3: return a <> arithExpr
      4: return a == arithExpr
      5: return a =< arithExpr
      6: return a => arithExpr
      7: abort string("Invalid comparison")

pri logicNot | tok
   tok := spaces
   if tok == 149 ' NOT
      tp++
      return not compare
   return compare

pri logicAnd | t, tok
   t := logicNot
   repeat
      tok := spaces
      if tok == 150 ' AND
         tp++
         t := t and logicNot
      else
         return t

pri expr | tok, t
   t := logicAnd
   repeat
      tok := spaces
      if tok == 151 ' OR
         tp++
         t := t or logicAnd
      else
         return t

pri specialExpr
   if spaces <> "="
      abort @syn
   skipspaces
   return expr

pri scanFilename(f) | c, chars
   chars := 0
   tp++ ' skip past initial quote
   repeat while (c := byte[tp++]) <> quote
      if chars++ < 31
         byte[f++] := c
   byte[f] := 0

pri texec | ht, nt, restart, thisLine, uS, a,b,c,d, f0,f1,f2,f3,f4,f5,f6,f7
   uS := clkfreq / 1_000_000
   thisLine := tp - 2
   restart := 1
   repeat while restart
      restart := 0
      ht := spaces
      if ht == 0
         return
      nt := skipspaces
      if isvar(ht) and nt == "="
         tp++
         vars[fixvar(ht)] := expr
      elseif ht => 128
         case ht
            128: ' THEN
               a := expr
               if spaces <> 129
                  abort string("MISSING THEN")
               skipspaces
               if not a
                  return
               restart := 1
            130: ' INPUT {"<prompt>";} <var> {, <var>}
               if nt == quote
                  c := byte[++tp]
                  repeat while c <> quote and c
                     trm.out(c)
                     c := byte[++tp]
                  if c <> quote
                     abort @syn
                  if skipspaces <> ";"
                     abort @syn
                  nt := skipspaces
               if not isvar(nt)
                  abort @syn
               b := 0
               inVars[b++] := fixvar(nt)
               repeat while skipspaces == ","
                  nt := skipspaces
                  if not isvar(nt) or b == 26
                     abort @syn
                  inVars[b++] := fixvar(nt)
               getline
               tokenize
               repeat a from 1 to b
                  vars[inVars[a-1]] := expr
                  if a < b
                     if spaces == ","
                        skipspaces
            131: ' PRINT
               a := 0
               repeat
                  nt := spaces
                  if nt == 0 or nt == ":"
                     quit
                  if nt == quote
                     tp++
                     repeat
                        c := byte[tp++]
                        if c == 0 or c == quote
                           quit
                        trm.out(c)
                        a++
                  else
                     d~
                     if (b := expr) < 0
                        -b
                        trm.out("-")
                        a++
                     c := 1_000_000_000
                     repeat 10
                        if b => c
                           trm.out(b / c + "0")
                           a++
                           b //= c
                           d~~
                        elseif d or c == 1
                           trm.out("0")
                           a++
                        c /= 10
                  nt := spaces
                  if nt == ";"
                     tp++
                  elseif nt == ","
                     trm.out(" ")
                     a++
                     repeat while a & 7
                        trm.out(" ")
                        a++
                     tp++
                  elseif nt == 0 or nt == ":"
                     trm.out(fReturn)
                     quit
                  else
                     abort @syn
            132, 133: ' GOTO, GOSUB
               a := expr
               if a < 0 or a => 65535
                  abort @ln
               b := findline(a)
               if wordat(b) <> a
                  abort @ln
               if ht == 133
                  pushstack
               nextlineloc := b 
            134: ' RETURN
               if sp == 0
                  abort string("INVALID RETURN")
               nextlineloc := stack[--sp]
            135: ' REM
               repeat while skipspaces
            136: ' NEW
               clearall
            137: ' LIST {<expr> {,<expr>}}
               trm.out($00)
               b := 0                ' Default line range
               c := 65535
               if spaces <> 0        ' At least one parameter
                  b := c := expr
                  if spaces == ","
                     skipspaces
                     c := expr
               a := long[userPtr]
               repeat while a+2 < eop
                  d := wordat(a)
                  if d => b and d =< c
                     trm.dec(d)
                     trm.out(" ")
                     putlinet(a+2)
                  a += 3 + strsize(a+2)
            138: ' RUN
                  clearvars
            140: ' OPEN " <file> ", R/W/A
               if spaces <> quote
                  abort @syn
               scanFilename(@f0)
               if spaces <> ","
                  abort @syn
               case skipspaces
                  "A", "a": d := "a"
                  "W", "w": d := "w"
                  "R", "r": d := "r"
                  other: abort string("Invalid open file mode")
               tp++
               if \fsrw.mount(spiDO,spiClk,spiDI,spiCS) < 0
                  abort string("Can't mount SD card")
               if \fsrw.popen(@f0,d)
                  abort string("Can't open file")
               fileOpened := true
            141: ' READ <var> {, <var> }
               if not isvar(nt)
                  abort @syn
               d := 0
               inVars[d++] := fixvar(nt)
               repeat while skipspaces == ","
                  nt := skipspaces
                  if not isvar(nt) or d == 26
                     abort @syn
                  inVars[d++] := fixvar(nt)
               a := 0
               repeat
                  c := fsrw.pgetc
                  if c < 0
                     abort string("Can't read file")
                  elseif c == fReturn or c == fEof
                     tline[a] := 0
                     tp := @tline
                     quit
                  elseif c == fLinefeed
                     next
                  elseif a < linelen-1
                     tline[a++] := c
               tokenize
               repeat a from 1 to d
                  vars[inVars[a-1]] := expr
                  if a < d
                     if spaces == ","
                        skipspaces
            142: ' WRITE ...
               d := 0 ' record column
               repeat
                  nt := spaces
                  if nt == 0 or nt == ":"
                     quit
                  if nt == quote
                     tp++
                     repeat
                        c := byte[tp++]
                        if c == 0 or c == quote
                           quit
                        fsrw.pputc(c)
                        d++
                  else
                     a := expr
                     if a < 0
                        -a
                        fsrw.pputc("-")
                     b := 1_000_000_000
                     c := false
                     repeat 10
                        if a => b
                           fsrw.pputc(a / b + "0")
                           a //= b
                           c := true
                        elseif c or b == 1
                           fsrw.pputc("0")
                        b /= 10
                  nt := spaces
                  if nt == ";"
                     tp++
                  elseif nt == ","
                     fsrw.pputc(" ")
                     d++
                     repeat while d & 7
                        fsrw.pputc(" ")
                        d++
                     tp++
                  elseif nt == 0 or nt == ":"
                     fsrw.pputc(fReturn)
                     fsrw.pputc(fLinefeed)
                     quit
                  else
                     abort @syn
            143: ' CLOSE
               fileOpened := false
               if \fsrw.pclose < 0
                  abort string("Error closing file")
               \fsrw.unmount
            144: ' DELETE " <file> "
               if spaces <> quote
                  abort @syn
               scanFilename(@f0)
               if \fsrw.mount(spiDO,spiClk,spiDI,spiCS) < 0
                  abort string("Can't mount SD card")
               if \fsrw.popen(@f0,"d")
                  abort string("Can't delete file")
               if \fsrw.pclose < 0
                  abort string("Error deleting file")
               \fsrw.unmount
            145: ' RENAME " <file> "," <file> "
               if spaces <> quote
                  abort @syn
               scanFilename(@f0)
               if spaces <> ","
                  abort @syn
               if skipspaces <> quote
                  abort @syn
               scanFilename(@f0)
               abort string("Rename not implemented")
            146: ' FILES
               if \fsrw.mount(spiDO,spiClk,spiDI,spiCS)
                  abort string("Can't mount SD card")
               fsrw.opendir
               b := 0
               d := false
               repeat while fsrw.nextfile(@f0) == 0
                  d := true
                  if b == 39
                     trm.out(13)
                     b := 0
                  trm.str(@f0)
                  repeat 13 - strsize(@f0)
                     trm.out(" ")
                  b := b + 13
               if d
                  trm.out(13)
               \fsrw.unmount
            147: ' SAVE or SAVE "<filename>"
               if (nt := spaces) == quote
                  scanFilename(@f0)
                  if \fsrw.mount(spiDO,spiClk,spiDI,spiCS) < 0
                     abort string("Can't mount SD card")
                  if \fsrw.popen(@f0,"w")
                     abort string("Can't create file")
                  processSave
                  if \fsrw.pclose < 0
                     abort string("Error closing file")
                  \fsrw.unmount
               else
                  if nt
                     abort @syn
                  a := ((userPtr - progsize) & $7FE0) | fsrw#bootAddr
                  d := eop - long[userPtr]
                  if fsrw.writeEEPROM(a-4,@d,4)
                     abort string("Save EEPROM write")
                  if fsrw.writeWait(a-4)
                     abort string("Save EEPROM timeout")
                  repeat c from 0 to d step 32   ' Write the program itself
                     if fsrw.writeEEPROM(a+c,long[userPtr]+c,d-c<#32)
                        abort string("Save EEPROM write")
                     if fsrw.writeWait(a+c)
                        abort string("Save EEPROM timeout")
            148: ' LOAD or LOAD "<filename>"
               if (nt := spaces) == quote
                  scanFilename(@f0)
                  if \fsrw.mount(spiDO,spiClk,spiDI,spiCS) < 0
                     abort string("Can't mount SD card")
                  if \fsrw.popen(@f0,"r")        ' Open requested file
                     abort string("Can't open file")
                  bytemove(@tailLine,tp,strsize(tp)+1)
                  newprog
                  processLoad
                  if \fsrw.pclose < 0
                     abort string("Error closing file")
                  \fsrw.unmount
               else
                  if nt <> 0 and nt <> ":"
                     abort @syn                  ' Read program from EEPROM
                  a := ((userPtr - progsize) & $7FE0) | fsrw#bootAddr
                  if fsrw.readEEPROM(a-4,@d,4)
                     abort string("Load EEPROM read")
                  if d < 3 or d > progsize       ' Read program size & check
                     abort string("Invalid program size")
                  bytemove(@tailLine,tp,strsize(tp)+1)
                  if fsrw.readEEPROM(a,long[userPtr],d)
                     abort string("Load EEPROM read")
                  eop := long[userPtr] + d       ' Leave stopped after this line
                  nextlineloc := eop - 2
               tp := @tailLine                   ' Scan command tail after load
            154: ' FOR <var> = <expr> TO <expr> {STEP <expr>}
               ht := spaces
               if ht == 0
                  abort @syn
               nt := skipspaces
               if not isvar(ht) or nt <> "="
                  abort @syn
               a := fixvar(ht)
               skipspaces
               vars[a] := expr
               if spaces <> 155 ' TO             ' Save FOR limit
                  abort @syn
               skipspaces
               forLimit[a] := expr
               if spaces == 156 ' STEP           ' Save step size
                  skipspaces
                  forStep[a] := expr
               else
                  forStep[a] := 1                ' Default step is 1
               if spaces
                  abort @syn
               forLoop[a] := nextlineloc         ' Save address of line
               if forStep[a] < 0                 '  following the FOR
                  b := vars[a] => forLimit[a]
               else                              ' Initially past the limit?
                  b := vars[a] =< forLimit[a]
               if not b                          ' Search for matching NEXT 
                  repeat while nextlineloc < eop-2
                     curlineno := wordat(nextlineloc)
                     tp := nextlineloc + 2
                     nextlineloc := tp + strsize(tp) + 1
                     if spaces == 157            ' NEXT <var>
                        nt := skipspaces         ' Variable has to agree
                        if not isvar(nt)
                           abort @syn
                        if fixvar(nt) == a       ' If match, continue after
                           quit                  '  the matching NEXT
            157: ' NEXT <var>
               nt := spaces
               if not isvar(nt)
                  abort @syn
               a := fixvar(nt)
               vars[a] += forStep[a]             ' Increment or decrement the
               if forStep[a] < 0                 '  FOR variable and check for
                  b := vars[a] => forLimit[a]
               else                              '  the limit value
                  b := vars[a] =< forLimit[a]
               if b                              ' If continuing loop, go to
                  nextlineloc := forLoop[a]      '  statement after FOR
               tp++
            159: ' OUTA [ <expr>{..<expr>} ] = <expr>
               a := getAddress(".")
               if a > $FFFF
                  b := a & $FF
                  a := (a >> 8) & $FF
                  outa[a..b] := specialExpr
                  dira[a..b]~~
                  repeat c from a to b
                     outputs |= |< c
               else
                  outa[a] := specialExpr
                  dira[a]~~
                  outputs |= |< a
            160: ' PAUSE <expr> {,<expr>}
               if pauseTime == 0                 ' If no active pause time, set it
                  spaces                         '  with a minimum time of 50us
                  pauseTime := expr * 1000
                  if spaces == ","               ' First (or only) value is in ms
                     skipspaces
                     pauseTime += expr           ' Second value is in us
                  pauseTime #>= 50
               if pauseTime < 10_050             ' Normally pause at most 10ms at a time,
                  waitcnt(pauseTime * uS + cnt)  '  but, if that would leave < 50us,
                  pauseTime := 0                 '   pause the whole amount now
               else                             
                  a := pauseTime <# 10_000     
                  waitcnt(a * uS + cnt)          ' Otherwise, pause at most 10ms and
                  nextlineloc := thisLine        '  re-execute the PAUSE for the rest
                  pauseTime -= 10_000
            166: ' BYTE [ <expr> ] = <expr>
               a := getAddress(" ")
               byte[a] := specialExpr
            167: ' WORD [ <expr> ] = <expr>
               a := getAddress(" ")
               word[a] := specialExpr
            168: ' LONG [ <expr> ] = <expr>
               a := getAddress(" ")
               long[a] := specialExpr
            170: ' PHSA =
               PHSA := specialExpr
            171: ' PHSB =
               PHSB := specialExpr
            172: ' FRQA =
               FRQA := specialExpr
            173: ' FRQB =
               FRQB := specialExpr
            174: ' CTRA =
               CTRA := specialExpr
            175: ' CTRB =
               CTRB := specialExpr
            176: ' DISPLAY <expr> {,<expr>}
               spaces
               trm.out(expr)
               repeat while spaces == ","
                  skipspaces
                  trm.out(expr)
            178: ' LET <var> = <expr>
               nt := spaces
               if not isvar(nt)
                  abort @syn
               tp++
               vars[fixvar(nt)] := specialExpr
            179: ' STOP
               nextlineloc := eop-2
               return
            180: ' END
               nextlineloc := eop-2
               return
            181: ' EEPROM [ <expr> ] = <expr>
               a := getAddress(",")
               b := specialExpr
               if fsrw.writeEEPROM(a,@b,1)
                  abort string("EEPROM write")
               if fsrw.writeWait(a)
                  abort string("EEPROM timeout")
            182: ' FILE = <expr>
               if fsrw.pputc(specialExpr) < 0
                  abort string("SDCARD write error")
            184: ' SPIN [{<expr>,}<expr>] or "<file>"
               if spaces == quote
                  scanFilename(@f0)
                  if \fsrw.mount(spiDO,spiClk,spiDI,spiCS) < 0
                     abort string("Can't mount SD card")
                  if \fsrw.popen(@f0,"r")
                     abort string("Can't open file")
                  fsrw.bootSDCard
               else
                  a := getAddress(",") & !$7FFF
                  ifnot fsrw.checkPresence(a)
                     abort string("No EEPROM there")
                  fsrw.bootEEPROM(a)
               abort string("SPIN unsuccessful")
            185: ' COPY [<expr>],"<file>" or COPY "<file>",[<expr>] or
                 ' COPY [<expr>],[<expr>] where <expr> are different
               if spaces == quote
                  scanFileName(@f0)
                  if spaces <> ","
                     abort @syn
                  skipspaces
                  b := getAddress(",") & !$7FFF
                  ifnot fsrw.checkPresence(b)
                     abort string("No EEPROM there")
                  if \fsrw.mount(spiDO,spiClk,spiDI,spiCS) < 0
                     abort string("Can't mount SD card")
                  if \fsrw.popen(@f0,"r")
                     abort string("Can't open file")
                  if fsrw.pread(@f0,32) <> 32
                     abort string("Can't read program")
                  if fsrw.writeEEPROM(b,@f0,32)
                     abort string("Copy EEPROM write error")
                  if fsrw.writeWait(b)
                     abort string("Copy EEPROM wait error")
                  a := word[@f0+fsrw#vbase]
                  repeat c from 32 to a - 1 step 32
                     d := (a - c) <# 32
                     if fsrw.pread(@f0,d) <> d
                        abort string("Can't read program")
                     if fsrw.writeEEPROM(b+c,@f0,d)
                        abort string("Copy EEPROM write error")
                     if fsrw.writeWait(b+c)
                        abort string("Copy EEPROM wait error")
                  if \fsrw.pclose < 0
                     abort string("Error closing file")
                  \fsrw.unmount
               else
                  a := getAddress(",") & !$7FFF
                  ifnot fsrw.checkPresence(a)
                     abort string("No EEPROM there")
                  if spaces <> ","
                     abort @syn
                  skipspaces
                  if spaces == quote
                     scanFileName(@f0)
                     if \fsrw.mount(spiDO,spiClk,spiDI,spiCS) < 0
                        abort string("Can't mount SD card")
                     if \fsrw.popen(@f0,"w")
                        abort string("Can't create file")
                     b := 0
                     if fsrw.readEEPROM(a+fsrw#vbase,@b,2)
                        abort string("Copy EEPROM read error")
                     repeat c from 0 to b - 1 step 32
                        d := (b - c) <# 32
                        if fsrw.readEEPROM(a+c,@f0,d)
                           abort string("Copy EEPROM read error")
                        if fsrw.pwrite(@f0,d) <> d
                           abort string("Can't save program")
                     if \fsrw.pclose < 0
                        abort string("Error closing file")
                     \fsrw.unmount
                  else
                     if a == (b := getAddress(",") & !$7FFF)
                        abort string("EEPROM areas same")
                     ifnot fsrw.checkPresence(b)
                        abort string("No EEPROM there")
                     d := 0
                     if fsrw.readEEPROM(a+fsrw#vbase,@d,2)
                        abort string("Copy EEPROM read error")
                     repeat c from 0 to d - 1 step 32
                        if fsrw.readEEPROM(a+c,@f0,32)
                           abort string("Copy EEPROM read error")
                        if fsrw.writeEEPROM(b+c,@f0,32)
                           abort string("Copy EEPROM write error")
                        if fsrw.writeWait(b+c)
                           abort string("Copy EEPROM wait error")
            186: ' DUMP <expr>,<expr>
               a := expr
               if spaces <> ","
                  abort @syn
               skipspaces
               dumpMemory(a,expr)


            187: 'REBOOT
               reboot

            188: 'CLR
               trm.out($00)

            189: 'HOME
               trm.out($01)

            190: 'LOCATE <expr> , <expr>
                c := expr                     ' Evaluate 1st expression
                if spaces <> ","              ' Look for comma
                   abort @syn
                skipspaces                   ' Skip comma and spaces
                trm.locate(c,expr)
                
            191: 'COLOR <expr>
              c := expr
              trm.setcol(c)

            192: 'PLOT <expr> , <expr>
                c := expr                     ' Evaluate 1st expression
                if spaces <> ","              ' Look for comma
                   abort @syn
                skipspaces                   ' Skip comma and spaces
                trm.plot(c,expr)

            193: 'DRAWTO <expr> , <expr>
                c := expr                     ' Evaluate 1st expression
                if spaces <> ","              ' Look for comma
                   abort @syn
                skipspaces                   ' Skip comma and spaces
                trm.drawto(c,expr)

               
               
      else                                   
         abort(@syn)
      if spaces == ":"
         restart := 1
         tp++

pri doline(s) | c, jl                 ' Execute the string in s or wait for input
   curlineno := -1
   if trm.gotkey
     jl := trm.getkey
     if jl == $CB
        nextlineloc := eop-2        ' Stop the program

   if nextlineloc < eop-2
      curlineno := wordat(nextlineloc)
      tp := nextlineloc + 2
      nextlineloc := tp + strsize(tp) + 1
      texec
   else
      if fileOpened
         fileOpened := false
         if fsrw.pclose < 0
            trm.str(string("Error closing open file",fReturn))
         \fsrw.unmount
      pauseTime := 0
      repeat c from 0 to 15
         if outputs & |< c
            dira[c]~
            outa[c]~
      outputs := 0
      if s
         bytemove(tp:=@tline,s,strsize(s)+1)
      else
         putlinet(string("READY."))
         getline
      c := spaces
      if "0" =< c and c =< "9"
         insertline
         nextlineloc := eop - 2
      else
         tokenize
         if spaces
            texec

PRI processLoad : c | a
   repeat
      a := 0
      repeat
         c := fsrw.pgetc
         if c == fReturn or c == fEof
            tline[a] := 0
            tp := @tline
            quit
         elseif c == fLinefeed
            next
         elseif c < 0
            quit
         elseif a < linelen-1
            tline[a++] := c
      if c == fEof and tline[0] == 0
         quit
      if c < 0
         abort string("Error while loading file")
      tp := @tline
      a := spaces
      if "0" =< a and a =< "9"
         insertline
         nextlineloc := eop - 2
      else
         if a <> 0
            abort string("Missing line number in file")

PRI processSave | a, c, d, ntoks
   ntoks := (@tokx - @toks) / 2
   a := long[userPtr]
   repeat while a+2 < eop
      d := wordat(a)
      fsrw.dec(d)
      fsrw.out(" ")
      d := a + 2
      repeat while c := byte[d++]
         if c => 128
            if (c -= 128) < ntoks
               fsrw.str(@@toks[c])
               if c <> 7   ' REM
                  fsrw.out(" ")
            else
               fsrw.out("{")
               fsrw.dec(c)
               fsrw.out("}")
         else
            fsrw.out(c)
      fsrw.out(fReturn)
      fsrw.out(fLinefeed)
      a += 3 + strsize(a+2)

PRI getAnyNumber | c, t
   case c := byte[tp]
      quote:
         if result := byte[++tp]
            if byte[++tp] == quote
              tp++
            else
               abort string("missing closing quote")
         else
            abort string("end of line in string")
      "$":
         c := byte[++tp]
         if (t := hexDigit(c)) < 0
            abort string("invalid hex character")
         result := t
         c := byte[++tp]
         repeat until (t := hexDigit(c)) < 0
            result := result << 4 | t
            c := byte[++tp]
      "%":
         c := byte[++tp]
         if not (c == "0" or c == "1")
            abort string("invalid binary character")
         result := c - "0"
         c := byte[++tp]
         repeat while c == "0" or c == "1"
            result := result << 1 | (c - "0")
            c := byte[++tp]
      "0".."9":
         result := c - "0"
         c := byte[++tp]
         repeat while c => "0" and c =< "9"
            result := result * 10 + c - "0"
            c := byte[++tp]
      other:
        abort string("invalid literal value")

PRI hexDigit(c)
'' Convert hexadecimal character to the corresponding value or -1 if invalid.
   if c => "0" and c =< "9"
      return c - "0"
   if c => "A" and c =< "F"
      return c - "A" + 10
   if c => "a" and c =< "f"
      return c - "a" + 10
   return -1

PUB dumpMemory(addr,size) | i, c, p, first
'' This routine dumps a portion of the RAM to the display.
'' The format is 8 bytes wide with hexadecimal and ASCII.
  addr &= $7FFF
  if addr + size > $7FFF               ' truncate to end of code space
    size := $8000 - addr
  first := true
  p := addr & $7FF8
  repeat while p < (addr + size)
    if first
      trm.hex(addr,4)
      first := false
    else
      trm.hex(p,4)
    repeat i from 0 to 7
      if p => addr and p < (addr + size)
        c := byte[p]
       ' trm.out(trm#MoveX)
        trm.out(3*i+5)
        trm.hex(c,2)
        if c => " " and c =< "~"
        '  trm.out(trm#MoveX)
          trm.out(i+30)
          trm.out(c)
      p++
    trm.str(string(29,"|",38,"|",13))
    