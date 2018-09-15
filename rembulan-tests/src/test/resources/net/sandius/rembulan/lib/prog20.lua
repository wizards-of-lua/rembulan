local filename,input = ...
local file = io.open(filename, 'w')

for s in input:gmatch"%d+" do
  local n = tonumber(s)
  file:write(string.char(n))
end

file:close()
