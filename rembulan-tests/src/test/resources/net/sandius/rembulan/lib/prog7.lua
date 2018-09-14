local filename, whence, offsetStr = ...

local offset = tonumber(offsetStr)
local file = io.open(filename, 'r')


file:seek(whence, offset)
  
local result = file:read('*l')

io.close(file)

return result
