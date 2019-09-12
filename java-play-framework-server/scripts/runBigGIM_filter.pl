#!/usr/bin/perl -w

use File::Basename;
use JSON -support_by_pp;

die "Usage: $0 maxNumber tisueList geneList" if @ARGV < 3;

$dirname = dirname(__FILE__);
$python  = "python3";

$maxNumber = $ARGV[0];
$tisues    = $ARGV[1];
$genes     = $ARGV[2];

$genes =~ s/NCBIGene://g;

@output = split /\n/, `$python $dirname/runBigGim.py $maxNumber $tisues $genes`;

@outGenes = split /', '/, $output[$#output];

if($outGenes[0] =~ s/^\[\'// && $outGenes[$#outGenes] =~ s/\'\]$// && $outGenes[0]=~ /^\d+$/) {
	foreach(@outGenes) {
		$myGenes{$_} = 1;
	}
	foreach(split /,/, $genes) {
		print STDOUT $_,"\n" if exists $myGenes{$_};
	}
}
else {
	foreach(@output) {
		print STDERR $_,"\n";
	}
}
