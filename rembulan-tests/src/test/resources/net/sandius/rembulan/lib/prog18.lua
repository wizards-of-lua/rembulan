local filename,text = ...
print(filename)
local file = io.open(filename, 'w')

file:close()

local _,err = file:write(text)

--print(err)
return err




