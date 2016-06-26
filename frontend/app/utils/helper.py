import csv

birdmap = {}
with open('app/static/birds/birdmap.csv', 'rU') as csvfile:
    reader = csv.DictReader(csvfile)
    for row in reader:
        birdmap[row['feed']] = row['type']
        
def caps(name):
    return ' '.join(map(lambda x: x.capitalize(), name.split(" ")))