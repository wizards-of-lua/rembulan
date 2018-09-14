local filename = ...

local file = io.open(filename, 'r')

local result = file:read('*a')

io.close(file)
--print(result)
return result
