local filename,text1,offset,text2 = ...

local file = io.open(filename, 'w')

file:write(text1)

file:seek("set", offset)

file:write(text2)

io.close(file)

