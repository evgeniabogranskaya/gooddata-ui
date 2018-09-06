import json
import random
import signal
import socket
import string
import sys
import threading
import traceback
from multiprocessing import Value
from optparse import OptionParser
from pymongo import MongoClient
from time import time

""" Simple performance test which connects to fluentd via tcp socket and
 sends records which are then inserted into mongo db """

# socket configuration
distribution = 'flat'
mongo_host = 'localhost'
mongo_port = 27017
fluentd_host = 'localhost'
fluentd_port = 24224


class MongoDBClient(object):
    def __init__(self, host, port):
        self.plugin_name = "mongo"
        self.mongo_host = host
        self.mongo_port = port

    def createIndexes(self, collection_names):
        con = MongoClient(host=self.mongo_host, port=self.mongo_port)
        db = con["admin"]
        db.authenticate("gdc_root", "dummy")

        db = con["cfal"]
        for collection in collection_names:
            print "Creating index for collection %s" % collection
            db[collection].create_index([('userLogin', 1)], background=True)
            db[collection].create_index([('eventdate', 1)], expireAfterSeconds=3600 * 24, background=True)


class FluentDClient(object):
    def __init__(self):
        self.sock = None
        self.tag = None

    def connect(self, host, port):
        # Create a TCP/IP socket
        print 'Creating connection'
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

        # Connect the socket to the port where the server is listening
        server_address = (host, port)
        print 'connecting to %s port %s\n\n' % server_address
        self.sock.connect(server_address)

    def send(self, data):
        message = [self.tag, data]
        msg_json = json.dumps(message)
        self.sock.sendall(msg_json)

    def close(self):
        print 'Closing socket'
        self.sock.close()


class ShuttDownSignalListener(object):
    def __init__(self):
        self.kill_now = False
        signal.signal(signal.SIGINT, self.exit_gracefully)
        signal.signal(signal.SIGTERM, self.exit_gracefully)

    def exit_gracefully(self, signum, frame):
        print "Shutdown signal received"
        self.kill_now = True


def dist(total_domains):
    if distribution == "flat":
        return random.randint(0, total_domains)
    elif distribution == "step":
        return step_dist(rate=40, range=4, total=total_domains)
    else:
        raise Exception('Unsupported distribution type ' + distribution)


def step_dist(rate, range, total):
    value = random.randint(0, 100)
    if value < rate:
        return random.randint(0, range)
    else:
        return random.randint(range + 1, total)


def prepare_record(domainId):
    userLogin = ''.join(random.choice(string.letters) for _ in range(10)) + "@gooddata.com"
    data = ''.join(str(x) for x in range(random.randint(0, 100)))

    record = {
        "type": "LOGIN",
        "userLogin": userLogin,
        "userIp": "127.0.0.1",
        "domainId": domainId,
        "component": "webapp",
        "occurred": "2018-08-09T05:46:25.280Z",
        "success": True,
        "params": {"component": "WEBAPP", "loginType": "USERNAME_PASSWORD",
                   "data": "asibxsaibxsuabxshabsuxvasyxsvsyvaytvxastyVTXavyvaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaayvsyavsyvyavsyavsy",
                   "aaa": "swdewdwedwefderferfrefef",
                   "qqq": "swdewdwedwefderfedsddswef",
                   "data": data
                   }
    }

    return record


class FluentDWorkerThread(threading.Thread):
    def __init__(self, threadID, name, tag, number_of_domains, shuttdown_signal_listener, sent_records):
        threading.Thread.__init__(self)
        self.threadID = threadID
        self.name = name
        self.fluentd_client = FluentDClient()
        self.fluentd_client.tag = tag
        self.sent_records = 0
        self.number_of_domains = number_of_domains
        self.shuttdown_signal_listener = shuttdown_signal_listener
        self.sent_records = sent_records

    def run(self):
        print "Starting " + self.name
        sent_records = 0
        try:
            self.fluentd_client.connect(host=fluentd_host, port=fluentd_port)
            while not self.shuttdown_signal_listener.kill_now:
                number_of_records = 200
                record_set = prepare_record_set(number_of_records=number_of_records,
                                                number_of_domains=self.number_of_domains)
                sent_records += number_of_records
                self.fluentd_client.send(record_set)
        except:
            print("Unexpected error:", sys.exc_info()[0])
            traceback.print_exc()

        self.fluentd_client.close()
        self.sent_records.value += sent_records
        print "Finished " + self.name


def prepare_record_set(number_of_records, number_of_domains):
    record_set = []
    for i in range(number_of_records):
        domain = "domain_" + str(dist(number_of_domains))
        record_set.append((int(time()), prepare_record(domain)))

    return record_set


def main(tag, create_indexes, number_of_domains):
    shuttdown_signal_listener = ShuttDownSignalListener()
    mongodb_client = MongoDBClient(mongo_host, mongo_port)
    sent_records = Value('i', 0)
    init = True
    thread_pool = [
        FluentDWorkerThread(id, "FluentDWorker" + str(id), tag, number_of_domains, shuttdown_signal_listener,
                            sent_records)
        for id in range(3)]
    try:
        for thread_worker in thread_pool:
            thread_worker.start()

        while not shuttdown_signal_listener.kill_now:
            if init and create_indexes:
                mongodb_client.createIndexes(["cfal_domain_" + str(x) for x in range(number_of_domains)])
                init = False

        for thread_worker in thread_pool:
            thread_worker.join()

    finally:
        print "Client stopped. Number of records sent %d" % sent_records.value


if __name__ == "__main__":
    parser = OptionParser()
    parser.add_option("-t", "--tag", default="cfal.test.*", help="fluentd tag", dest="tag")
    parser.add_option("-i", "--indexes", default=False, action="store_true", help="create indexes",
                      dest="create_indexes")
    parser.add_option("-d", "--distribution", default="step",
                      help="distribution of events as function of domain ['step', 'flat']",
                      dest="dist")
    parser.add_option("-n", "--number_domains", type="int", default=100, help="number of domains to be generated",
                      dest="number_of_domains")

    (options, args) = parser.parse_args()

    distribution = options.dist

    main(tag=options.tag, create_indexes=options.create_indexes, number_of_domains=options.number_of_domains)
