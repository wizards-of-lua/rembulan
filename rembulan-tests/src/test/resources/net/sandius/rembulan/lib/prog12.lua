local filename, len = ...

local file = io.open(filename, 'r')

local result = {}

while true do
  local chunk = file:read(len)
  if not chunk then
    break
  end
  table.insert(result,chunk)
end

io.close(file)
--print(result)

return table.unpack(result)
