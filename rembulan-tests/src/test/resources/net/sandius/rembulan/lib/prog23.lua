local filename = ...
local file = io.open(filename, 'r')

local result = nil

while true do
  local line = file:read("*L")
  if not line then
    break
  end
  if result then
    result = result..line
  else 
    result = line
  end
end
io.close(file)

return result


