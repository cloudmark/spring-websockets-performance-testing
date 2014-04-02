# raw using 1:4 with points lc 9 title '',
set datafile separator " "
set terminal png size 4800, 1200
set xlabel "Time" 
set xdata time
set timefmt "%s"
set format x "%M:%S"
set key left top
set xtics 10 rotate by 90 right
set mxtics 1
set grid
set yrange [2800:3200]

set multiplot layout 3, 1;
set title "Latency Log [".name."]"
set ylabel "Latency Log"
set logscale y
set style fill transparent solid 0.5 noborder
plot filename using 1:($5-$6):($5+$6) with filledcurves lc rgb '#ffcccc' title  'StdDev',\
     filename using 1:5 with lines lc rgb 'red' title 'Mean',\
     filename using 1:5 with points lc rgb 'red' title '',\
     filename using 1:3 with lines lw 2 lt 3 lc 1 title 'Min', \
     filename using 1:4 with lines lw 2 lt 3 lc 2 title 'Max'

set title "Latency [".name."]"
set ylabel "Latency "
set nologscale y
set style fill transparent solid 0.5 noborder
plot filename using 1:($5-$6):($5+$6) with filledcurves lc rgb '#ffcccc' title  'StdDev',\
     filename using 1:5 with lines lc rgb 'red' title 'Mean',\
     filename using 1:5 with points lc rgb 'red' title '',\
     filename using 1:3 with lines lw 2 lt 3 lc 1 title 'Min', \
     filename using 1:4 with lines lw 2 lt 3 lc 2 title 'Max'

set title "Connections [".name."]"
set ylabel "Count"
plot filename using 1:2 with lines lw 2 lt 3 title 'Count', \
     filename using 1:2 t '' with points

#set title "Threshold [".name."]"
#set ylabel "Thresholded"
#plot filename using 1:7 with lines lw 2 lt 3 title 'Threshold', \
#     filename using 1:7 t '' with points


