#/bin/sh

DIR=$1
OUT=$2
THRESHOLD=0
mkdir $OUT
echo "Processing Directory - $DIR"
rm -f "$OUT/$DIR-process"
rm -rf "$OUT/$DIR-process-1"
rm -rf "./$OUT/$DIR-inverted-graph-std"
rm -rf "./$OUT/$DIR-thresholds-max"
rm -rf "./$OUT/$DIR-thresholds-min"

for file in `ls -1 $DIR`
do 
  echo "Processing File $file"
  cat $DIR/$file | awk '($4 != 0) {print int($3 / 1000) "." ($3%1000), $1, $2, $4}' >> "$OUT/$DIR-process-1"
done
cat "$OUT/$DIR-process-1" | sort >> "$OUT/$DIR-process"

for sample in `cat $OUT/$DIR-process-1 | awk '{print $1}' | sort | uniq`
do
  echo "Processing -> $sample"
  AVERAGE=`cat "$OUT/$DIR-process-1" | awk -v sample=$sample -v threshold=$THRESHOLD '($1 == sample  && $4 >= threshold) {sum += $4; count +=1} END {print sum/count}'`
  ROW=$(cat "$OUT/$DIR-process-1" |\
    awk -v avg=$AVERAGE -v sample=$sample -v threshold=$THRESHOLD \
      'BEGIN {
        min = avg; 
        max = avg; 
       }
       ($1 == sample && $4 >= threshold ) {
          if ($4 < min) min = $4; 
          if ($4 > max) max = $4; 
          sum += $4; 
          std += (($4 - avg) * ($4 - avg));
          count +=1
       }
       ($1 == sample && $4 < threshold) {
          threshold_count += 1; 
       } 
       END {print sample, count, min, max, avg, sqrt(std/(count-1)), threshold_count}')

  MIN=`echo $ROW | awk '{print $3}'`
  echo "Finding Samples Which [Min: $MIN]"
  MIN_PS=`cat "$OUT/$DIR-process-1" | awk -v sample=$sample -v min=$MIN '($1 == sample  && $4 == min) {print $2}'`
  for min_p in $MIN_PS
  do
    echo "Sample $min_p contains min [$MIN] @ [$sample]"  >> "./$OUT/$DIR-thresholds-min"
    cat $OUT/$DIR-process-1 | grep "$min_p\ " | grep "$sample" --context=5 >> "./$OUT/$DIR-thresholds-min"
    echo "" >> "./$OUT/$DIR-thresholds-min"
    echo "" >> "./$OUT/$DIR-thresholds-min"
  done

  MAX=`echo $ROW | awk '{print $4}'`
  echo "Finding Samples Which [Max: $MAX]"
  MAX_PS=`cat "$OUT/$DIR-process-1" | awk -v sample=$sample -v max=$MAX '($1 == sample  && $4 == max) {print $2}'`
  for max_p in $MAX_PS
  do
    echo "Sample $max_p contains max [$MAX] @ [$sample]"  >> "./$OUT/$DIR-thresholds-max"
    cat $OUT/$DIR-process-1 | grep "$max_p\ " | grep "$sample" --context=5 >> "./$OUT/$DIR-thresholds-max"
    echo "" >> "./$OUT/$DIR-thresholds-max"
    echo "" >> "./$OUT/$DIR-thresholds-max"
  done

  echo $ROW >> "./$OUT/$DIR-inverted-graph-std"
done

#echo "Generating Plot"
#rm -rf $OUT/$DIR-graph.png
gnuplot -e "name='$DIR';filename='./$OUT/$DIR-inverted-graph-std';raw='./$OUT/$DIR-process'" inverted.gnuplot > $OUT/$DIR-graph.png

