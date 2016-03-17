for i in {1..9}
do
	paste -d' ' q$i.result.out Queries/q$i.epl.out  >  Queries/q$i.times.out
	awk '{$3=$1-$2} 1' Queries/q$i.times.out > Queries/q$i.diff.out 
done
