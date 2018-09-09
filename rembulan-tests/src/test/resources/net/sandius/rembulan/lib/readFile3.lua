local filename = ...
local file = io.open(filename, 'r')

local result = nil

while true do
  local num = file:read("*n")
  if not num then
    break
  end
  if not result then
    result = num
  else
    result = result + num
  end
end
io.close(file)

--print(result)
return result
