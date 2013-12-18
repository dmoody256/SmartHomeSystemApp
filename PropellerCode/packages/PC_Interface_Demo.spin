{{ PC_Interface Demo }}

'  Receives Mouse-events and shows buttons and position as Hex numbers.
'  Draws pixels, lines or boxes while a mouse button pressed.
'  shows the Graphic features of the PropTerminal.

'  Receives Keystrokes and sends it back to the Terminal.
'  Shows a own mouse pointer

CON

  _clkmode = xtal1 + pll16x
  _xinfreq = 5_000_000

  #0, WHITE,CYAN,RED,PINK,BLUE,GREEN,YELLOW,GRAY        'Standard colors

OBJ
  term   :       "PC_Interface"
  
VAR
  long   cx, cy, ch
  long   mx,my,mxo,myo

PUB Main | b


  'start the interface
  term.start(31,30)

  repeat while term.abs_x == 0    'wait for PropTerminal.exe started

  setpos(0,12)
  term.setcol(YELLOW)
  term.str(string("Interface Demo..."))
  'draw some graphics
  term.setcol(BLUE)
  term.box(170,218,10,-50)
  term.setcol(CYAN)
  term.box(190,218,10,-70)
  term.setcol(GRAY)
  term.box(210,218,10,-30)
  term.setcol(GREEN)
  term.plot(240,180)
  term.drawto(260,140)
  term.drawto(300,220)
  term.drawto(319,180)
  term.setcol(YELLOW)
  term.circle(140,100,30)
    
  term.delta_reset
  b := term.buttons
  cy := 1
  cx := 0
  
  term.showpointer(0,0)
  'show mouse events (position, buttons)
  repeat
      mx:=term.abs_x
      my:=term.abs_y
      term.out(1)               'home
      term.setcol(RED)
      term.out("B")
      term.out(":")
      term.dec(term.buttons)
      term.out(" ")
      term.out("X")
      term.out(":")
      term.dec(mx)
      term.out(" ")
      term.out("Y")
      term.out(":")
      term.dec(my)
      term.out(" ")
      setpos(cx,cy)
      term.setcol(3)

      if mx<>mxo or my<>myo
        term.hidepointer

      'show mouse drawing grafics
      msgraf(term.buttons,mx,my)
      setpos(cx,cy)
      term.setcol(WHITE)
      'test and echo keyboard input
      if term.gotkey
        ch := term.getkey
        term.out(ch)
        cx += 1
        if cx > 39 or ch == 13         'Valid key?
          cx := 0
          cy += 1
          if cy > 12
            cy := 12
          setpos(cx,cy)
        if ch == $C8                   'Delete?
          cx -= 2
          setpos(cx,cy)

      if mx<>mxo or my<>myo
        term.showpointer(mx,my)

      waitcnt(clkfreq/25 + cnt)        'Loop all 1/25 seconds                 


PRI setpos(px, py)

  term.out(10)
  term.out(px)
  term.out(11)
  term.out(py)


PRI  msgraf(cmd,xp,yp)

  term.out(5)                   'grafic
  term.out(cmd)                 '1:point 2:line 3:box
  term.out(xp/2)
  term.out(yp/2)
      