local filename, whence, offsetStr = ...

local offset = tonumber(offsetStr)
local file = io.open(filename, 'r')

local pos1 = file:seek(whence, offset)

local result = file:read('*a')

io.close(file)
--print(result)
return result
