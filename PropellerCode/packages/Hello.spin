CON

  _clkmode = xtal1 + pll16x
  _xinfreq = 5_000_000

OBJ

  term : "PC_Interface"

PUB Main

  term.start(31,30)

  repeat
       term.str(string("Hello World", 13))
    