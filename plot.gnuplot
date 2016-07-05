# Output to PNG, with Verdana 8pt font
set terminal pngcairo nocrop enhanced font "verdana,8" size 640,300

# Don't show the legend in the chart
# set nokey
set key left top

# Thinner, filled bars
set boxwidth 0.8

set style data histograms
set style fill solid 1.00

# Set a title and Y label. The X label is obviously months, so we don't set it.
set title "Running Time" font ",14" tc rgb "#606060"
set ylabel "Time (ms)"
set xlabel "Data Set"

# Rotate X labels and get rid of the small stripes at the top (nomirror)
# set xtics nomirror rotate by -45

# Show human-readable Y-axis. E.g. "100 k" instead of 100000.
set format y '%.0s %c'

# Replace small stripes on the Y-axis with a horizontal gridlines
set tic scale 0
set grid ytics lc rgb "#505050"

# Remove border around chart
unset border

# Manual set the Y-axis range
# set yrange [100000 to 300000]

set output "6.png"

plot "ihdbscan.dat" using 3:xticlabels(1) lt rgb "#406090" title "IHDBSCAN",\
     "hdbscan.dat" using 3 lt rgb "#40FF00" title "HDBSCAN",\
     'ihdbscan.dat' using 2 with linespoints title "RNG size"
