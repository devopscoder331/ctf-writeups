import time
class NoteManager:
    def __init__(self, notes_num, manager_name: bytes):
        self.notes_num = notes_num
        self.manager_name = bytearray(manager_name)
        self.notes_idxs = bytearray(self.notes_num)
        self.notes = list()
        self.counter = 0

    def __setitem__(self, key, value):
        self.notes.insert(key,value)
        self.notes_idxs[key] = value
        self.counter += 1

    def __getitem__(self, index):
        return self.notes[index]

    def reset(self):
        for i in range(0,len(self.notes_idxs)):
            self.notes_idxs[i] = 0x0
        for i in range(0,len(self.notes)):
            self.notes[i] = 0x0

        self.notes_idxs[self.counter] = NullNoteManager(self)
        
    def read_name(self,index,length):   
        return self.manager_name[index:index+length]

    def set_name(self,index,length,value):
        self.manager_name[index:index+length] = value


class NullNoteManager:
    def __init__(self,Manager):
        self.Manager = Manager

    def __index__(self):
        self.Manager.notes_idxs.clear()
        self.Manager.manager_name = bytearray()
        self.Manager.notes_idxs.extend([0]*self.Manager.notes_num)
        return 1
        
class Note:
    def __init__(self, idx, data):
        self.data = bytearray(data)
        self.idx = idx

    def __getitem__(self, index):
        return self.data[index]

    def __setitem__(self, key, value):
        self.data[key:key+len(value)] = value
    
    def __index__(self):
        return self.idx

class User:
    def __init__(self, username, password):
        self.username = username.encode('latin-1')
        self.password = password.encode('latin-1')
        self.main = ''
        self.backup = ''

    def init_notemanagers(self,count):
        self.main = NoteManager(count,self.username)
        self.backup = NoteManager(count,b'')

    def choose_storage(self,where):
        if where:
            return self.main
        else:
            return self.backup
