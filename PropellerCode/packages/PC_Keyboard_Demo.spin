{{ Keyboard Test }}

'  Receives Keystrokes and sends it as Hex numbers to the Terminal (or TV,VGA).
'  You can test the Key-codes of each Key on the Keyboard.

CON

  _clkmode = xtal1 + pll16x
  _xinfreq = 5_000_000


OBJ
  kb     :     "PC_Keyboard"    'You can also use Keyboard object for PS2-Keyboard                          
  TV     :     "PC_Text"        'You can also use TV_Text or VGA_Text objects
  
VAR


PUB Main | k

  'start the terminal
  TV.start(12)
  TV.str(string("PC_Keyboard Demo...",13))

  'start the keyboard
  kb.start(26, 27)

  'echo keystrokes in hex
  repeat
    k := kb.getkey
    if k&255 > $CF
      TV.out(12)                'Change Color if F-Key
      TV.out(k&7)
    TV.hex(k,3)
    TV.out(" ")
    