local filename, text = ...

local file, err = io.open(filename, 'w+')
assert(file, tostring(err))

file:write(text)

io.close(file)
