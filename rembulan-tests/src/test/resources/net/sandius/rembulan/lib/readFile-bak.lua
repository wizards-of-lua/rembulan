local filename = ...
local file = io.open(filename, 'r')

local result = nil

local lines = file:lines()
for line in lines do
  if result then
    result = result.."\n"..line
  else 
    result = line
  end
end
io.close(file)

return result


