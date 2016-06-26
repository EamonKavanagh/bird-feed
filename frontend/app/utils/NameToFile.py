import json


class NameToFile:
    
    def __init__(self):
        with open('app/static/birds/birds.json') as file:
            self.nameToFile = json.load(file)
    
    def addFile(self, data):
        return map(lambda x: x + (self.nameToFile[x[0]]['file'],), data)