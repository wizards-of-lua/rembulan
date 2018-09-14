local filename,text = ...

local file = io.open(filename, 'w')

file:close()

file:write(text)

