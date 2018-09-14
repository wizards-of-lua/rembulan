local filename,text = ...

local file = io.open(filename, 'a')

file:seek("set",0)

file:write(text)

io.close(file)

