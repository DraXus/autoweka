#!/usr/bin/python
import sqlite3
import matplotlib as mpl

mpl.use('Agg')
from pylab import *
import sys
from collections import OrderedDict

if len(sys.argv) < 3:
    print 'Syntax: python boxplot.py <dataset_name> <type_error>'
    exit(1)

type_error = sys.argv[2]
if type_error not in ['error', 'test_error', 'full_cv_error']:
    raise Exception('Invalid type_error: %s. Options are: error, test_error or full_cv_error' % type_error)

dataset = sys.argv[1]

conn = sqlite3.connect('results.db')
c = conn.cursor()

query = "SELECT strategy,generation,%s FROM results WHERE dataset='%s'" % (type_error, dataset)

results = c.execute(query).fetchall()

conn.close()

if not results:
    raise Exception('No results')

data = dict()
for row in results:
    key = "%s-%s" % (row[0], row[1])
    if key not in data:
        data[key] = []
    try:
        data[key].append(float(row[2]))
    except Exception, e:
        print "[ERROR] ", e, " -- ", row[2]

data = OrderedDict(sorted(data.items(), key=lambda t: t[0]))
figure()
boxplot(data.values(), labels=data.keys())
ylim(ymin=0)
ylabel('Error')
xlabel('Strategy')
title(dataset)
savefig('../plots/boxplot.%s.%s.png' % (type_error, dataset))
# show()
