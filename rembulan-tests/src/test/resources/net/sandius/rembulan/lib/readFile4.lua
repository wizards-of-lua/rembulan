local filename = ...
local file = io.open(filename, 'r')

local sum = nil
local text = nil

while true do
  local num = file:read("*n")
  if num then
    if not sum then
      sum = num
    else
      sum = sum + num
    end  
  else
    local txt = file:read("*l")
    if txt then
      if not text then
        text = txt
      else
        text = text .. '\n' .. txt
      end
    else
      break 
    end    
  end 
end
io.close(file)

--print(result)
return sum, text
