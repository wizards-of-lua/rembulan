local filename,offsetStr = ...
local file = io.open(filename, 'r')
local offset = tonumber(offsetStr)
file:seek("set",offset)

local content = file:read("*a")
file:close()

local output = nil
for i = 1, string.len(content) do
  local s = string.sub(content, i, i)
  local b = string.byte(s)
  if output then
    output = output .. "," .. b
  else
    output = b
  end
end

--print(output)
return output