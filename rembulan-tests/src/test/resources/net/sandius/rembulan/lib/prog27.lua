local filename, text = ...

local file, err = io.open(filename, 'a+')
assert(file, tostring(err))

-- Seek to 0 before writing to prove writes still go to EOF, not to position 0
file:seek("set", 0)
file:write(text)

io.close(file)
