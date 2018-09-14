local filename = ...

local file = io.open(filename, 'r')

file:close()

local line = file:read("*a")

--print(success)
return success



