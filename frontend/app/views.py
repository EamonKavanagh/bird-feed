from cassandra.cluster import Cluster
from flask import Flask, jsonify, redirect, render_template, request, url_for

from app import app
from utils.CassandraQueries import CassandraQueries
from utils.ElasticsearchQueries import ElasticsearchQueries
from utils.helper import caps, birdmap
from utils.NameToFile import NameToFile
from utils.secret import GOOGLE_MAPS_API_KEY


# 10 second lookback
esQueries = ElasticsearchQueries(10)
csQueries = CassandraQueries()
nameToFile = NameToFile()

@app.route('/')
def home():
	return render_template('home.html')


@app.route('/conway')
def conway():
	return render_template('conway.html')

@app.route('/bird-feed', methods=['GET', 'POST'])
def bird_feed():
    if request.method == 'POST':
        feed = request.form['feed'].lower()
        if feed in birdmap:
            if birdmap[feed] == 'family':
                family = feed
                name = ''
            else:
                family = birdmap[feed]
                name = feed
            return redirect(url_for('track', family=family, name=name))
        elif feed == '':
            return redirect(url_for('track', family='', name=''))
        else:
            return render_template('bird-feed.html', 
                error="Bird or bird family doesn't exist in database")
    else:
        return render_template('bird-feed.html') 
    
@app.route('/bird-feed/track')
def track():
    family = request.args.get('family', '').lower()
    name = request.args.get('name', '').lower()
    if name:
        title = caps(name)
        return render_template('track_bird.html', key=GOOGLE_MAPS_API_KEY, title=title) 
    elif family:
        title = caps(family)
    else:
        title = 'Overall'
    return render_template('track.html', key=GOOGLE_MAPS_API_KEY, title=title)   

# Elasticsearch Query
@app.route('/bird-feed/track/_near_me')
def near_me():
    location = request.args.get('location')
    distance = request.args.get('distance')
    family = request.args.get('family', '')
    family = '*' if not family else family
    name = caps(request.args.get('name', ''))
    result = nameToFile.addFile(esQueries.nearMe(location, distance, family, name))
    return jsonify(result=result)

# Elasticsearch Query    
@app.route('/bird-feed/track/_hotspots')
def hotspots():
    family = request.args.get('family', '')
    family = '*' if not family else family
    name = caps(request.args.get('name', ''))
    return jsonify(result=esQueries.hotspots(family, name))

# Cassandra Query
@app.route('/bird-feed/track/_trending')
def trending():
    family = caps(request.args.get('family'))
    result = nameToFile.addFile(csQueries.trending(family))
    return jsonify(result=result)
    
# Cassandra Query
@app.route('/bird-feed/track/_historical')
def historical():
    family = caps(request.args.get('family'))
    name = caps(request.args.get('name', ''))
    return jsonify(result=csQueries.historical(family, name))