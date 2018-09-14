local filename,text = ...

local file = io.open(filename, 'w')

file:write(text)

print(text)

