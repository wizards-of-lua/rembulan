local filename = ...
local file = io.open(filename, 'r')

local sum = nil

while true do
  local num1, num2 = file:read("*n","*n")
  if num1 or num2 then
    if not sum then
      sum = 0
    end
    if num1 then
      sum = sum + num1
    end
    if num2 then
      sum = sum + num2
    end
  else
    break
  end
end
io.close(file)

--print(sum)
return sum
