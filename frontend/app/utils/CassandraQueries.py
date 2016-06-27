import datetime
import heapq

from cassandra.cluster import Cluster

from secret import CASSANDRA_DNS

class CassandraQueries:
    
    def __init__(self):
        hosts = [CASSANDRA_DNS]
        cluster = Cluster(hosts)
        self.session = cluster.connect()
        self.session.set_keyspace('birdfeed')
        
    def trending(self, family):
        if family:
            filter = "WHERE family=%s"%(self.format(family))
        else:
            filter = ''
        query = """SELECT
                        name, count
                    FROM
                        trending
                    %s"""%(filter)
        results = self.session.execute(query)
        return [(result.name, result.count) for result in heapq.nlargest(5, results, lambda x: x.count)]
        
        
    def historical(self, family, name):
        if family or name:
            if name:
                table = 'names'
                filter = "name = " + self.format(name)
            else:
                table = 'families'
                filter = "family = " + self.format(family)
            query = """SELECT
                            date, count, unique
                        FROM
                            %s
                        WHERE %s
                        ORDER BY
                            date DESC
                        LIMIT 7"""%(table, filter)
        else:
            query = """SELECT
                            date, count, unique
                        FROM
                            overall
                        WHERE
                            year = '%d'
                        ORDER BY
                            date DESC
                        LIMIT 7"""%(datetime.datetime.now().year)
        results = self.session.execute(query)
        return [(str(result.date), result.count, result.unique) for result in results][::-1]
        
    def format(self, string):
        return "'" + string + "'"