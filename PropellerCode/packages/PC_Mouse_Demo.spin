{{ Mouse Test }}

'  Receives Mouse-events and shows buttons and position as Hex numbers.
'  Sets pixels, lines or boxes while a mouse button pressed, this shows the
'  Graphic feature of the PropTerminal.

CON

  _clkmode = xtal1 + pll16x
  _xinfreq = 5_000_000


OBJ
  ms     :       "PC_Mouse"
  TV     :       "PC_Text" 
  
VAR


PUB Main | b

  'start the terminal
  TV.start(12)
  TV.str(string("Mouse Demo...",13))

  'start the mouse
  ms.start(24, 25)
  ms.delta_reset

  repeat
      if ms.buttons
        TV.out(1)  'Home   if a button pressed
      else
        TV.out(0)  'cls    if no mouse button pressed

      'echo mouse events in hex
      TV.out("B")
      TV.out(":")
      TV.dec(ms.buttons)
      TV.out(" ")
      TV.out("X")
      TV.out(":")
      TV.dec(ms.abs_x)
      TV.out(" ")
      TV.out("Y")
      TV.out(":")
      TV.dec(ms.abs_y)
      TV.out(" ")

      'draw with the mouse
      TV.out(5)                 'PropTerminal Grafics
      TV.out(ms.buttons)
      TV.out(ms.abs_x >> 1)
      TV.out((230 - ms.abs_y) >> 1)

      waitcnt(clkfreq/10 + cnt)
      