local filename = ...
local file = io.open(filename, 'r')

local result = nil

local lines = file:lines("*n","*n","*l")
for n1,n2,txt in lines do
  if result then
    result = result .. "\n" .. n1.." "..n2.." "..txt
  else
    result = n1.." "..n2.." "..txt
  end
end
io.close(file)
--print(result)
return result
