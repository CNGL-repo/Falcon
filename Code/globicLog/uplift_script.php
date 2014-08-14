<?php

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
$xliff_array = array();
$count = 0;
$file1 = fopen($_SERVER['DOCUMENT_ROOT'].'/mock_Components/f109398.csv', "r") or exit("Unable to open file!");

while( !feof($file1) ){
  $xliff_array[$count] = fgets($file1)."<br>";
  $xliff_array[$count] = explode(',' , $xliff_array[$count]);
  $count++;
}
fclose($file1);
/////////////////////////////////////////////////////////////////////////////////////////////////////////////

$component_details = array();

$count1 = count( $xliff_array );

// Components attempt to register with the system
for ($i = 0; $i < $count1; $i++){
  if ( isset($xliff_array[$i][2]) && $xliff_array[$i][2] === "FALSE" ){
    if ( isset($xliff_array[$i][9]) ){
	  $compname = "panacea".$i;
	  $password = "password".$i;
	  $graph = "graph1";
	  
	  if ( isset($component_details[''.$xliff_array[$i][9].''][0]) )
	    continue;
	  else{
	    shell_exec('curl -v -X POST "http://kdeg-vm-24/globicLog/register/?componentName='.$compname.'&password='.$password.'&graphName=graph1"');
		//shell_exec('curl -v -X POST "http://localhost/globicLog/register/?componentName='.$compname.'&password='.$password.'&graphName=graph1"');
		$component_details[''.$xliff_array[$i][9].''][0] = $xliff_array[$i][9];
		$component_details[''.$xliff_array[$i][9].''][1] = $compname;
		$component_details[''.$xliff_array[$i][9].''][2] = $password;
	  }
	}
  }
}

$counting = 0;
// Components attempt to log data to the system
for ($i = 0; $i < $count1; $i++){
  if ( isset($xliff_array[$i][2]) && $xliff_array[$i][2] === "FALSE" ){
    $user = $component_details[''.$xliff_array[$i][9].''][0];
	$compname = $component_details[''.$xliff_array[$i][9].''][1];
	$password = $component_details[''.$xliff_array[$i][9].''][2];
	$unique_id = $xliff_array[$i][3];
	
	// Logging Machine translation
	$results = shell_exec('curl -v -X POST "http://kdeg-vm-24/globicLog/service/'.$compname.'/?password='.$password.'&activity=translate_'.$unique_id.'&contentConsumed1=<http://www.example.com/filestore/source_document'.$unique_id.'>&contentConsumed1Type=nif:String&contentGenerated1=<http://www.example.com/filestore/translated_document'.$unique_id.'>&contentGenerated1Type=gic:Translation"');
	//$results = shell_exec('curl -v -X POST "http://localhost/globicLog/service/'.$compname.'/?password='.$password.'&activity=translate_'.$unique_id.'&contentConsumed1=<http://www.example.com/filestore/source_document'.$unique_id.'>&contentConsumed1Type=nif:String&contentGenerated1=<http://www.example.com/filestore/translated_document'.$unique_id.'>&contentGenerated1Type=gic:Translation"');
	echo $results;
	$counting++;
	
    // Logging Quality Review
	$results = shell_exec('curl -v -X POST "http://kdeg-vm-24/globicLog/service/'.$compname.'/?password='.$password.'&user='.$user.'&activity=qualityReview_'.$unique_id.'&contentConsumed1=<http://www.example.com/filestore/source_document'.$unique_id.'>&contentConsumed1Type=nif:String&contentConsumed2=<http://www.example.com/filestore/translated_document'.$unique_id.'>&contentConsumed2Type=gic:Translation&contentGenerated1=<http://www.example.com/filestore/quality_review_document'.$unique_id.'>&contentGenerated1Type=gic:QualityAssessment"');
	//$results = shell_exec('curl -v -X POST "http://localhost/globicLog/service/'.$compname.'/?password='.$password.'&user='.$user.'&activity=qualityReview_'.$unique_id.'&contentConsumed1=<http://www.example.com/filestore/source_document'.$unique_id.'>&contentConsumed1Type=nif:String&contentConsumed2=<http://www.example.com/filestore/translated_document'.$unique_id.'>&contentConsumed2Type=gic:Translation&contentGenerated1=<http://www.example.com/filestore/quality_review_document'.$unique_id.'>&contentGenerated1Type=gic:QualityAssessment"');	
    echo $results;
	$counting++;
	
	// Logging Revise Translation
	$results = shell_exec('curl -v -X POST "http://kdeg-vm-24/globicLog/service/'.$compname.'/?password='.$password.'&user='.$user.'&activity=reviseTranslation_'.$unique_id.'&contentConsumed1=<http://www.example.com/filestore/source_document'.$unique_id.'>&contentConsumed1Type=nif:String&contentConsumed2=<http://www.example.com/filestore/translated_document'.$unique_id.'>&contentConsumed2Type=gic:Translation&contentGenerated1=<http://www.example.com/filestore/translationRevision_document'.$unique_id.'>&contentGenerated1Type=gic:TranslationRevision"');
    //$results = shell_exec('curl -v -X POST "http://localhost/globicLog/service/'.$compname.'/?password='.$password.'&user='.$user.'&activity=reviseTranslation_'.$unique_id.'&contentConsumed1=<http://www.example.com/filestore/source_document'.$unique_id.'>&contentConsumed1Type=nif:String&contentConsumed2=<http://www.example.com/filestore/translated_document'.$unique_id.'>&contentConsumed2Type=gic:Translation&contentGenerated1=<http://www.example.com/filestore/translationRevision_document'.$unique_id.'>&contentGenerated1Type=gic:TranslationRevision"');	
    echo $results;
	$counting++;
  }
}

echo "<br>Total number of logs = ".$counting;


// Working command line.
//$results = shell_exec('C:\Windows\system32\cmd.exe START /c curl -v -X POST "http://localhost/globicLog/service/component1/?password=password1&activity=translate_1&contentConsumed1=http://www.example.com/datastore/document1&contentConsumed1Type=nif:String&contentGenerated1=http://www.example.com/datastore/document2&contentGenerated1Type=gic:Translation"');
//echo $results;
?>
