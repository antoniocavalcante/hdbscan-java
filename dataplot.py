import numpy as np
import matplotlib.pyplot as plt
import math

def plotdata(dataset, sep):

    with open(dataset) as f:
        data = f.read()

    data = data.split("\n")

    x = [row.split(s)[0] for row in data if row != '']
    y = [row.split(s)[1] for row in data if row != '']
    z = [row.split(s)[2] for row in data if row != '']

    return x, y, z

def normalize(x, y):
    for i in range(len(x)):
        n = norm(x, y, i)
        x[i] = float(x[i])/n
        y[i] = float(y[i])/n

    return x, y

def norm(x, y, i):
    x[i] = float(x[i])
    y[i] = float(y[i])

    return math.sqrt(x[i]*x[i] + y[i]*y[i])

grid, s = "S.dat", " "
# data, s = "/home/toni/git/HDBSCAN_Star/experiments/data#2/2d-4c-no00.dat", " "
data, s = "/home/toni/git/HDBSCAN_Star/jad.dat", ","
# data, s = "/home/toni/git/HDBSCAN_Star/4.dat", " "


# x, y, z = plotdata(grid, s)
a, b, c = plotdata(data, s)
# a, b = normalize(a, b)

# plt.scatter(0, 0, marker='*', c='r', label='Origin')
# plt.scatter(x, y, marker='o', c=z, label='Grid S')
plt.scatter(a, b, marker='.', c=c, label='Data')
# plt.axes().set_aspect(1)

for i, txt in enumerate(range(len(a))):
    plt.annotate(txt, (a[i],b[i]))

plt.show()
