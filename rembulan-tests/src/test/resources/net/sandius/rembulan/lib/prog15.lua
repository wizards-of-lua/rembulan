local filename,text = ...

local file = io.open(filename, 'a')

file:write(text)

io.close(file)

