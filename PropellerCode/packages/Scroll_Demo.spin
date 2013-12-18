{{ Bitmap, Scroll and TextPositioning Graphicfunctions Demo }}

'  Draws a Propeller Icon together with a text and
'  scrolls the screen around.

CON

  _clkmode = xtal1 + pll16x
  _xinfreq = 5_000_000

  #0, WHITE,CYAN,RED,PINK,BLUE,GREEN,YELLOW,GRAY        'Standard colors


OBJ
  term   :       "PC_Interface"
  

PUB Main | i

  'start the interface
  term.start(31,30)

  setpos(10,4)
  term.setcol(YELLOW)
  term.str(string("Scroll Demo..."))

  'draw the bitmap
  term.setcol(RED)                 
  graf(0, 100,100)                  'position 100,100
  repeat i from 0 to 15 step 2
    bitmap(PIcon[i], PIcon[i+1])
  graf(0, 100,108)                  'second line
  repeat i from 16 to 31 step 2
    bitmap(PIcon[i], PIcon[i+1])
    
  'scroll in all directions
  repeat
    repeat 50
      graf(11, 0, 1)                'up
      waitcnt(clkfreq/25 + cnt)                 
    repeat 50
      graf(11, -1, 0)               'right
      waitcnt(clkfreq/25 + cnt)                 
    repeat 50
      graf(11, 1, -1)               'down & left
      waitcnt(clkfreq/25 + cnt)                 
       

PRI setpos(px, py)

  term.out(5)                       'setpos with new grafic function instead of 10,x 11,y 
  term.out(12)                      '(this works also in NormalTerminal mode)
  term.out(px)
  term.out(py)


PRI  graf(cmd,xp,yp)

  term.out(5)                         'graphic
  term.out(cmd+(xp&1)<<5+(yp&1)<<4)   '0: AT X,Y ... 10: Set BitMap, 11: Scroll X,Y
  term.out(xp>>1)
  term.out(yp>>1)


PRI  bitmap(b1, b2)

  term.out(5)                         'graphic
  term.out(10)                        '10: Set BitMap
  term.out(b1)
  term.out(b2)


DAT

PIcon   byte  $02,$07,$87,$47,$43,$C3,$C3,$7E,$7E,$C2,$C3,$43,$47,$87,$07,$02
        byte  $7C,$E3,$A1,$A0,$B8,$BF,$BF,$A0,$A0,$BF,$BF,$B8,$A0,$A0,$E3,$FC,0
      