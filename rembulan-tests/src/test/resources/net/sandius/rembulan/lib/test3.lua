local filename = ...
print("filename",filename)
local result = ""
local file = io.open(filename, 'r')
print("file",file)
local lines = file:lines()

for line in lines do
  --print(line)
  result = result..line
end
io.close(file)

return result


