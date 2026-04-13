local filename, text = ...

local file = io.open(filename, 'r+')
if not file then return end

file:write(text)

io.close(file)
