local filename = select(1, ...)

local tbl = table.pack(...)
table.remove(tbl, 1)
 
local file = io.open(filename, 'w')

file:write(table.unpack(tbl))

io.close(file)

