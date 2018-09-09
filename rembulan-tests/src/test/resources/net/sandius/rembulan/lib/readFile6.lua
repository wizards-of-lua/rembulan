local filename, whence1, offsetStr1, whence2, offsetStr2 = ...

local offset1 = tonumber(offsetStr1)
local offset2 = tonumber(offsetStr2)
local file = io.open(filename, 'r')

local pos1 = file:seek(whence1, offset1)
print("pos1",pos1)
local result1 = file:read('*l')

local pos2 = file:seek(whence2, offset2)
print("pos2",pos2)
local result2 = file:read('*l')

io.close(file)
print(result1,result2)
return result1, result2
