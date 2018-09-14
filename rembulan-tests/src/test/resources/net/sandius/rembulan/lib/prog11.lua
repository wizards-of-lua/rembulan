local filename, len = ...

local file = io.open(filename, 'r')

local result = file:read(len)

io.close(file)
--print(result)
return result
