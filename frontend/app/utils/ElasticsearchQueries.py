from elasticsearch import Elasticsearch
from elasticsearch_dsl import Search
import Geohash

from secret import ELASTIC_DNS, ELASTIC_PORT


class ElasticsearchQueries:
    
    def __init__(self, lookback):
        hosts = []
        for DNS in ELASTIC_DNS:
            hosts.append({'host': DNS, 'port': ELASTIC_PORT})
        self.client = Elasticsearch(hosts=hosts)
        self.lookback = 'now-%ds'%lookback
        
    def nearMe(self, location, distance, family, name):
        distance = str(distance) + 'm'
        query = Search(using=self.client, index=family) \
            .filter('geo_distance', distance=distance, location=location) \
            .filter('range', timestamp={'gte':self.lookback})
        if name:
            query = query.filter('term', name=name)
        query.aggs.bucket('count', 'terms', field='name', size=5)
        birds = query[0].execute().aggregations['count']['buckets']
        return [(bird['key'], bird['doc_count']) for bird in birds]
        
    def hotspots(self, family, name):
        query = Search(using=self.client, index=family)
        if name:
            query = query.filter('term', name=name)
        query = query.filter('range', timestamp={'gte':self.lookback})
        query.aggs.bucket('hotspot', 'geohash_grid', field='location', precision=7)
        hashes = query[0].execute().aggregations['hotspot']['buckets'][:3]
        return [Geohash.decode_exactly(hash['key'])[:2] for hash in hashes]