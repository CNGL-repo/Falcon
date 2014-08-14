<?php

/*
* Alan Meehan
* Globic Log Service - version 1.0
* TCD - CNGL
*/

class RetrieveRequest{
  
  //Class variables declared
  private $_request_array = array();
  private $_actual_activity;
  private $_component_name;
  private $_component_pass;
  private $_component_named_graph;
  private $_prefix_list = array();
  private $_sparql_prefixes = "" ;
  
  /****************************************************
  Constructor 
  *****************************************************/
  public function __construct(){
  
	$this->prefixRetrieval();
	
	$count = count($this->_prefix_list);
	
	// Create the list of prefixes that will appear at the top of the the SPARQL requests
	for($i=0; $i < $count; $i++){
	  $this->_sparql_prefixes = $this->_sparql_prefixes."prefix ".$this->_prefix_list[$i][1]." <".$this->_prefix_list[$i][0].">
";
	}
	
    $this->processRequest();
  }
  
  
  
  /*****************************************************************************************************
  The prefixRetrieval() method is used to read in the prefixes used in the Globic ontology model and 
  store them in an array for further use
  *****************************************************************************************************/
  public function prefixRetrieval(){
  
    $filepath = $_SERVER['DOCUMENT_ROOT']."/globicLog/service/ontology_model/Globic_ontology_model.ttl";
	$file_contents = "";
	
	$lineskip = 0;
	
    $file = fopen($filepath, "r") or exit("Unable to open file!");
    //Output a line of the file until the end is reached
    while(!feof($file)){
	    $file_contents = $file_contents . fgets($file). "<br>";
    }
    fclose($file);
	
	$prefix_array1 = explode('<br>' , $file_contents);
	
	$count1 = count($prefix_array1);
	
	for($i = 0; $i < $count1; $i++){
	  if ($prefix_array1[$i][0] == "@"){
	    $prefix_array2[$i] = $prefix_array1[$i];
	  }
	  else
	    $lineskip++;
	  if ($lineskip > 10 )
	    break;
	}
	
	// Now lines beginning with '@prefix' are seperated from rest of file
	
	$count2 = count($prefix_array2);
	
	for($i = 0; $i < $count2; $i++){
	  $prefix_array3[$i] = explode(' ' , $prefix_array2[$i] , 3); // Explode to get the namespace and the uri
	  $this->_prefix_list[$i][1] = $prefix_array3[$i][1]; // Namespace stored
	  
	  //Now get uri on its own
	  $trimmed1 = trim($prefix_array3[$i][2]); // Remove white space
	  $trimmed2 = trim($trimmed1, "<"); // Remove "<"
	  $trimmed3 = trim($trimmed2, "."); // Remove "."
	  $trimmed4 = trim($trimmed3); // Remove whitespace again
	  $trimmed5 = trim($trimmed4, ">"); // Remove ">"
	  $this->_prefix_list[$i][0] = $trimmed5; // URI stored
	}
	
  }
  
  
 
  /*****************************************************************************************************
  The processRequest() method is used to extract the various bits of information from the request string
  and ensure there are no errors in the request
  *****************************************************************************************************/
  public function processRequest(){
  
    $error_message;
	$component_found = false;
	$comp_key_match = false;
	
	
	// Retrieving the http method from request
	$this->_request_array['http_method'] = $_SERVER['REQUEST_METHOD'];
	
	// Validate Method - ensure it is either POST or GET
	if( strtolower($this->_request_array['http_method']) != "post"){
	  if( strtolower($this->_request_array['http_method']) != "get"){
	    $error_message = "Error in request - HTTP METHOD: '".$this->_request_array['http_method']."' is not allowed, it must be either 'POST' or 'GET'.";
	    echo $error_message;
		$this->errorLog($component_found, $error_message);
	    exit();
      }
	}
	
	
	
	// Retrieving the uri from request
	$this->_request_array['uri'] = $_SERVER['REQUEST_URI'];
	
	// Exploding uri to get individual details
	$uri_explode = explode('/' , $this->_request_array['uri']);
	
	
	// Ensuring that 'globicProv' and 'service' or 'register' is in the URI, if not an error is given
	if( strtolower($uri_explode[1]) != 'globiclog' ){
	  $error_message = "Error in request - URI: it must be 'http://localhost/globicLog/service'.";
	  echo $error_message;
	  $this->errorLog($component_found, $error_message);
	  exit();
	}
	
	$uri_service = false;
	$uri_register = false;
	
	if( strtolower($uri_explode[2]) === 'service' ){ // Check to see if the request contains 'service'
	  $uri_service = true;
	}
	else if( strtolower($uri_explode[2]) === 'register' ){ // Check to see if the request contains 'register'
	  $uri_register = true;
	}
	else {
	  $error_message = "Error in request - URI: it must be 'http://localhost/globicLog/service' or 'http://localhost/globicLog/register'.";
	  echo $error_message;
	  $this->errorLog($component_found, $error_message);
	  exit();
	}
	
	// If request contains register - do the following
	/////////////////////////////////////////////////////////////////////////////////////////////////
	if ( $uri_register == true && $this->_request_array['http_method'] == 'POST' ){
	  
	  // Get componentName from request
	  if ( isset($_REQUEST['componentName']) )
	    $this->_component_name = $_REQUEST['componentName'];
	  else {
	    $error_message = "Error in request - COMPONENT NAME: no component name is set";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
	  }
	  // Ensure componentName is not blank
	  if ($this->_component_name == ""){
	    $error_message = "Error in request - COMPONENT NAME: component name is empty";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
	  }
	  
	  // Get component password from request
	  if ( isset($_REQUEST['password']) )
	    $this->_component_pass = $_REQUEST['password'];
	  else {
	    $error_message = "Error in request - COMPONENT PASSWORD: no component password is set";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
	  }
	  // Ensure password is not blank
	  if ($this->_component_pass == ""){
	    $error_message = "Error in request - COMPONENT PASSWORD: component password is empty";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
	  }
	  
	  // Get component graph (partitionName) from request, if not set, set it to default
	  if ( isset($_REQUEST['partitionName']) )
	    $this->_component_named_graph = $_REQUEST['partitionName'];
	  else {
	    $this->_component_named_graph = "default_graph";
	    //$error_message = "Error in request - GRAPH NAME: no graph name is set";
	    //echo $error_message;
	    //$this->errorLog($component_found, $error_message);
	    //exit();
	  }
	  // Ensure partitionName is not blank
	  if ($this->_component_named_graph == ""){
	    $error_message = "Error in request - PARTITION NAME: partition name is empty";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
	  }
	  
	  //$string_lenghts = 0;
	  
	  // Validate submitted details to ensure they fit within the characters allowed, especially no spaces or commas allowed and less than 20 characters long
	  // Validate Component name
	  //$string_lenghts = strlen($this->_component_name);
	  //if ( $string_lenghts > 20 ){
	  //  $error_message = "Error in request - COMPONENT NAME: component name is grater than 20 characters long, it must be less than or equal to 20";
	  //  echo $error_message;
	  //  $this->errorLog($component_found, $error_message);
	  //  exit();
	  //}
	  
	  // Validate submitted details to ensure they fit within the characters allowed, especially no spaces or commas allowed 
      /*else*/ 
	  if ( preg_match('/^[0-9A-Za-z_-]+$/', $this->_component_name) ){
	    // Component name validated
	  }
      else {
        $error_message = "Error in request - COMPONENT NAME: the component name '".$this->_component_name."' contains illegal characters, it is only allowed to contain 'A-Z', 'a-z', '0-9', '_', '-' and do not use any spaces";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
      }
	  
	  //Ensure that component name does not already exist in the system
	  $filepath1 = $_SERVER['DOCUMENT_ROOT'].'/globicLog/service/component_details/component_details.csv';
	  $file1 =  fopen($filepath1,"a") or exit("Unable to open file!");
	  $component_key_file = file_get_contents($filepath1);
      $component_key_array = str_getcsv($component_key_file);
	
      $length1 = count($component_key_array);
	
      for ($i = 0; $i < $length1-1; $i++){
	    $component_details[$i] = explode(' ' , $component_key_array[$i]); // Puts data from component_keys.csv into $component_details array.
        if($this->_component_name === $component_details[$i][0]){ // Check component ID.
	      $error_message = "Error in registration - COMPONENT NAME: the component name '".$this->_component_name."' already exists, please choose a different name.";
	      echo $error_message;
	      $this->errorLog($component_found, $error_message);
	      exit();
	    }
      }
	  
	  
	  // Validate Component password
	  //$string_lenghts = strlen($this->_component_pass);
	  //if ( $string_lenghts > 20 ){
	  //  $error_message = "Error in request - COMPONENT PASSWORD: component password is grater than 20 characters long, it must be less than or equal to 20";
	  //  echo $error_message;
	  //  $this->errorLog($component_found, $error_message);
	  //  exit();
	  //}
      /*else*/ 
	  if ( preg_match('/^[0-9A-Za-z_-]+$/', $this->_component_pass) ){
	    // Component pass validated
	  }
      else {
        $error_message = "Error in request - COMPONENT PASSWORD: component password: '".$this->_component_pass."' contains illegal characters, it is only allowed to contain 'A-Z', 'a-z', '0-9', '_', '-' and do not use any spaces";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
      }
	  
	  // Validate Component graph name (partition name)
	  //$string_lenghts = strlen($this->_component_named_graph);
	  //if ( $string_lenghts > 20 ){
	  //  $error_message = "Error in request - GRAPH NAME: graph name is grater than 20 characters long, it must be less than or equal to 20";
	  //  echo $error_message;
	  //  $this->errorLog($component_found, $error_message);
	  //  exit();
	  //}
      /*else*/ 
	  if ( preg_match('/^[0-9A-Za-z_-]+$/', $this->_component_named_graph) ){
	    // Component graph name validated
	  }
      else {
        $error_message = "Error in request - PARTITION NAME: partition name: '".$this->_component_named_graph."' contains illegal characters, it is only allowed to contain 'A-Z', 'a-z', '0-9', '_', '-' and do not use any spaces";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
      }
	  
	  // Write component details to file
	  fwrite($file1, $this->_component_name." ".$this->_component_pass." ".$this->_component_named_graph.",");
	  fclose($file1);
	  
	  $this->activityLog(true);
	  echo "Registration Successful";
	  
	}
	
	// Else if request is to use globicLog/service - do the following
	///////////////////////////////////////////////////////////////////////////////////////////
	else if ( $uri_service == true){
	
	  // Getting Component Id and password from request
	  if( isset($uri_explode[3]) == false ){
	    $error_message = "Error in request - COMPONENT ID: no component ID is set";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
	  }
	  else{
	    $this->_request_array['component_id'] = $uri_explode[3]; // Component ID
	  }
	
	  // Retrieve component key from request string
	  if( isset($_REQUEST['password']) )
	    $this->_comp_pass = $_REQUEST['password'];
	  else {
	    $error_message = "Error in request - PASSWORD: no password is set, ensure '?password=' is set in request.";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
	  }
	
	
	  // Validating Component
      $component_key_file = file_get_contents($_SERVER['DOCUMENT_ROOT'].'/globicLog/service/component_details/component_details.csv');
      $component_key_array = str_getcsv($component_key_file);
	
      $length1 = count($component_key_array);
	
      for ($i = 0; $i < $length1-1; $i++){
	    $component_details[$i] = explode(' ' , $component_key_array[$i]); // Puts data from component_keys.csv into $component_details array.
        if($this->_request_array['component_id'] == $component_details[$i][0]){ // Check component ID.
	      $component_found = true;
	      if($this->_comp_pass == $component_details[$i][1]){ // Checks component key matches
		    $this->_component_name = $component_details[$i][0]; // Sets component name
		    $this->_component_named_graph = $component_details[$i][2]; // Sets the named graph component has specified during registration
		    $comp_key_match = true;
	        break;
	      }
        }
	  }
	
	  if($component_found == false){
	    $error_message = "Error in request - COMPONENT ID: '".$this->_request_array['component_id']."' is not recognised, ensure that ID is correct.";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
	  }
	  else if($comp_key_match == false){
	    $error_message = "Error in request - PASSWORD: password is incorrect.";
	    echo $error_message;
	    $this->errorLog($component_found, $error_message);
	    exit();
	  }
	  
	  
	  ///// If method is POST, then extract and validate activity, content consumed and content generated
	  if ($this->_request_array['http_method'] == 'POST'){
	
	
	    // Retrieve activity from request string
	    if( isset($_REQUEST['activity']) )
	      $this->_request_array['activity'] = $_REQUEST['activity'];
	    else {
	      $error_message = "Error in request - ACTIVITY: no activity is set, ensure '&activity=' is set in request.";
	      echo $error_message;
		  $this->errorLog($component_found, $error_message);
	      exit();
	    }
	  
	    // Ensure activity is not blank and does not contain 'NULL'
	    if( $this->_request_array['activity'] == "" ){
	      $error_message = "Error in request - ACTIVITY: activity is empty, it must contain an activity and cannot contain 'NULL'.";
	      echo $error_message;
		  $this->errorLog($component_found, $error_message);
	      exit();
	    }
	    
	  
	  
	    // Validate Activity
	    $activity_validated = false;
	  
	    $sparql_activityretrieval_query = $this->_sparql_prefixes.'

SELECT ?Subject
WHERE
{
  ?Subject rdfs:subClassOf gic:contentProcessingActivity
}'  ;
	
	    // Create fuseki query file to query globic model and retrieve list of activities
	    $filepath1 = $_SERVER['DOCUMENT_ROOT']."/globicLog/service/temp/".$this->_request_array['component_id']."activity_retrieval.rq";
	    $file1 =  fopen($filepath1,"w") or exit("Unable to open file!");;
	    fwrite($file1, $sparql_activityretrieval_query);
	    fclose($file1);
	
	    // Execute request query
        $results1 = shell_exec('@ECHO OFF & cd C:\DB_Server\jena-fuseki-0.2.7 && ruby s-query --service http://localhost:3030/Test/query --query='.$filepath1);
		
	    // Delete temp files
	    unlink($filepath1);
	
	    // Decode request from JSON
	    $decoded1 = json_decode($results1, true);
	  
	    $activity_list_unexploded = array();
	    $activity_list = array();
	  
	    $act_count1 = count($decoded1["results"]["bindings"]);
	  
	    for($i = 0; $i < $act_count1; $i++){
	      $activity_list_unexploded[$i] = $decoded1["results"]["bindings"][$i][$decoded1["head"]["vars"][0]]["value"];
	    }
	  
	    $act_count2 = count($activity_list_unexploded);
	  
	    for($i = 0; $i < $act_count2; $i++){
	      $activity_list[$i] = explode('#' , $activity_list_unexploded[$i]);
	    }
	  
	    // Loop through the array with list of activities and try to find match with activity stated in request
	    for($i = 0; $i < $act_count2; $i++){
          $activity_string = $activity_list[$i][1];
          $found = strripos($this->_request_array['activity'], $activity_string);
	      if($found !== false){
	        $activity_validated = true;
		    $this->_actual_activity = $activity_list[$i][1];
	        break;
	      }
        }
  
        if($activity_validated == false){
	      $error_message = "Error in request - ACTIVITY: '".$this->_request_array['activity']."' is not recognised, ensure that the activities name appears correctly in request.";
	      echo $error_message;
		  $this->errorLog($component_found, $error_message);
	      exit();
        }
		
		
		// Get user from request if it exists
	    if ( isset($_REQUEST['user']) ){
	      $this->_request_array['user'] = $_REQUEST['user'];
		  
		  // Ensure user is not blank
	      if ($this->_request_array['user'] == ""){
	        $error_message = "Error in request - USER: user is empty";
	        echo $error_message;
	        $this->errorLog($component_found, $error_message);
	        exit();
	      }
		}
		
	  
	    /////////////////////////////////////////////////////////////////////////////////////////////////////
	    // Retrieve content consumed from request
	    /////////////////////////////////////////////////////////////////////////////////////////////////////
	    for($i=1; $i<=10; $i++){
	      if( isset($_REQUEST['contentConsumed'.$i]) ){
		    $len = strlen($_REQUEST['contentConsumed'.$i]);
	        if( $_REQUEST['contentConsumed'.$i] == "" ){
		      $error_message = "Error in request - CONTENT: contentConsumed".$i." is empty, ensure it contains content or if it is not needed, remove it from request.";
	          echo $error_message;
			  $this->errorLog($component_found, $error_message);
	          exit();
	        }
			// check to ensure content consumed is stated as a uri in format: <http://...>
			else if ($_REQUEST['contentConsumed'.$i][0] != '<' || $_REQUEST['contentConsumed'.$i][1] != 'h' 
			    || $_REQUEST['contentConsumed'.$i][2] != 't' || $_REQUEST['contentConsumed'.$i][3] != 't' 
				|| $_REQUEST['contentConsumed'.$i][4] != 'p' || $_REQUEST['contentConsumed'.$i][5] != ':' 
				|| $_REQUEST['contentConsumed'.$i][6] != '/' || $_REQUEST['contentConsumed'.$i][7] != '/'
				|| $_REQUEST['contentConsumed'.$i][$len-1] != '>' )
				{
				$error_message = "Error in request - CONTENT: contentConsumed".$i." must be a URI in the form of <http://...> .";
	            echo $error_message;
			    $this->errorLog($component_found, $error_message);
	            exit();
			}
		    else 
			  $this->_request_array['contentConsumed'.$i] = $_REQUEST['contentConsumed'.$i];
		  }
		  
	    }
	  
	    // Retrieve content consumed type from request
	    for($i=1; $i<=10; $i++){
	      if( isset($_REQUEST['contentConsumed'.$i.'Type']) ){
		    if( isset($_REQUEST['contentConsumed'.$i]) ){
	          if( $_REQUEST['contentConsumed'.$i.'Type'] == "" ){
		        $error_message = "Error in request - CONTENT: contentConsumed".$i."Type is empty, please ensure it contains the type of content that relates to 'consumedContent".$i.".";
	            echo $error_message;
			    $this->errorLog($component_found, $error_message);
	            exit();
	          }
		      else
		        $this->_request_array['contentConsumed'.$i.'Type'] = $_REQUEST['contentConsumed'.$i.'Type'];
		    }
		    else{
		      $error_message = "Error in request - CONTENT: 'contentConsumed".$i." does not exist, it must exist before declaring 'contentConsumed".$i."Type'.";
	          echo $error_message;
			  $this->errorLog($component_found, $error_message);
	          exit();
		    }
		  }
	    }
	  
	  
	    // Query triple store to ensure content consumed types are compatible with activity
	    // or if no type is set, retrieve a type and set it as a default type
	  
	    $content_consumed_type_validated = array();
	    $sparql_content_consumed_retrieval_query = $this->_sparql_prefixes.'

	  
SELECT ?Subject

WHERE
{
 gic:'.$this->_actual_activity.'  owl:equivilentClass  ?blank_a .
 ?blank_a  owl:onProperty  gic:contentConsumed .
 ?blank_a  owl:allValuesFrom  ?blank_b .

 ?blank_b  owl:unionOf  ?blank1 .

 ?blank1  rdf:rest*/rdf:first ?Subject .

}'  ;

        // Create fuseki query file to query globic model and retrieve list of possible content an activity can consume
	    $filepath3 = $_SERVER['DOCUMENT_ROOT']."/globicLog/service/temp/".$this->_request_array['component_id']."content_consumed_retrieval.rq";
	    $file3 =  fopen($filepath3,"w") or exit("Unable to open file!");
	    fwrite($file3, $sparql_content_consumed_retrieval_query);
	    fclose($file3);
	
	    // Execute request query
		$results2 = shell_exec('@ECHO OFF & cd C:\DB_Server\jena-fuseki-0.2.7 && ruby s-query --service http://localhost:3030/Test/query --query='.$filepath3);

	    // Delete temp files
	    unlink($filepath3);
	  
	    // Decode request from JSON
	    $decoded2 = json_decode($results2, true);
	  
	    $content_consumed_list_unexploded = array();
	    $content_consumed_list = array();
	  
	    $content_consumed_count1 = count($decoded2["results"]["bindings"]);
	  
	    for($i = 0; $i < $content_consumed_count1; $i++){
	      $content_consumed_list_unexploded[$i] = $decoded2["results"]["bindings"][$i][$decoded1["head"]["vars"][0]]["value"];
	    }
	  
	    $content_consumed_count2 = count($content_consumed_list_unexploded);
	  
	    $content_consumed_list_with_prefixes = array();
	    $z_count = count($this->_prefix_list);
	  
	    // Get a list of content with proper prefixes, eg. 'gic:Content' or 'nif:String' etc
	    for($i = 0; $i < $content_consumed_count2; $i++){
	      $content_consumed_list[$i] = explode('#' , $content_consumed_list_unexploded[$i]);
		  for($a = 0; $a < $z_count; $a++){
		    if ( $content_consumed_list[$i][0] == trim($this->_prefix_list[$a][0], "#")){ // Compare URI inoder to put name space in front of content
		      $content_consumed_list_with_prefixes[$i] = $this->_prefix_list[$a][1].$content_consumed_list[$i][1];
		      break;
		    }
		  }
	    }
	  

	    // Loop through the array of list of content consumed for activity and try to find match with content consumed type stated in request
	    for($a = 1; $a <= 10; $a++){ // Loop through the possible 10 contentConsumed tokens
	      if ( isset($this->_request_array['contentConsumed'.$a.'Type']) ){
	        $content_consumed_type_validated[$a] = false;
		  
		    for($i = 0; $i < $content_consumed_count2; $i++){ // Loop through allowed content consumed by activity
		      if ( $this->_request_array['contentConsumed'.$a.'Type'] === $content_consumed_list_with_prefixes[$i] ){
		        $content_consumed_type_validated[$a] = true;
				break;
		      }
		    }
		  
		    if ($content_consumed_type_validated[$a] == false ){
		      $error_message = "Error in request - CONTENT: '".$this->_request_array['contentConsumed'.$a.'Type']."' assigned to 'contentConsumed".$a."Type' does not match the allowable types of content which can be consumed by activity : '".$this->_actual_activity."'.";
		      echo $error_message;
			  $this->errorLog($component_found, $error_message);
		      exit();
		    }
		  }
	    }
	  
	  
	    // If content consumed is declared but its corresponding type is not declared, then assign it a default type from list of possible types for that activity.
	    for($a = 1; $a <= 10; $a++){
	      if ( isset($this->_request_array['contentConsumed'.$a]) ){
		    if ( isset($this->_request_array['contentConsumed'.$a.'Type']) == false ){
		      $this->_request_array['contentConsumed'.$a.'Type'] = $content_consumed_list_with_prefixes[0];
		    }
		  }
	    }
	  
	  
	  
	    /////////////////////////////////////////////////////////////////////////////////////////////////////
	    // Retrieve content generated from request
	    /////////////////////////////////////////////////////////////////////////////////////////////////////
	    for($i=1; $i<=10; $i++){
	      if( isset($_REQUEST['contentGenerated'.$i]) ){
		  $len = strlen($_REQUEST['contentGenerated'.$i]);
	        if( $_REQUEST['contentGenerated'.$i] == "" ){
		      $error_message = "Error in request - CONTENT: contentGenerated".$i." is empty, ensure it contains content or if it is not needed, remove it from request.";
	          echo $error_message;
			  $this->errorLog($component_found, $error_message);
	          exit();
	        }
			// check to ensure content generated is stated as a uri in format: <http://...>
			else if ($_REQUEST['contentGenerated'.$i][0] != '<' || $_REQUEST['contentGenerated'.$i][1] != 'h' 
			    || $_REQUEST['contentGenerated'.$i][2] != 't' || $_REQUEST['contentGenerated'.$i][3] != 't' 
				|| $_REQUEST['contentGenerated'.$i][4] != 'p' || $_REQUEST['contentGenerated'.$i][5] != ':' 
				|| $_REQUEST['contentGenerated'.$i][6] != '/' || $_REQUEST['contentGenerated'.$i][7] != '/'
				|| $_REQUEST['contentGenerated'.$i][$len-1] != '>' )
				{
				$error_message = "Error in request - CONTENT: contentGenerated".$i." must be a URI in the form of <http://...> .";
	            echo $error_message;
			    $this->errorLog($component_found, $error_message);
	            exit();
			}
		    else
		      $this->_request_array['contentGenerated'.$i] = $_REQUEST['contentGenerated'.$i];
		  }
	    }
	  
	  
	    // Retrieve content generated type from request
	    for($i=1; $i<=10; $i++){
	      if( isset($_REQUEST['contentGenerated'.$i.'Type']) ){
		    if( isset($_REQUEST['contentGenerated'.$i]) ){
	          if( $_REQUEST['contentGenerated'.$i.'Type'] == "" ){
		        $error_message = "Error in request - CONTENT: contentGenerated".$i."Type is empty, please ensure it contains the type of content that relates to 'consumedGenerated".$i.".";
	            echo $error_message;
			    $this->errorLog($component_found, $error_message);
	            exit();
	          }
		      else
		        $this->_request_array['contentGenerated'.$i.'Type'] = $_REQUEST['contentGenerated'.$i.'Type'];
		    }
		    else{
		      $error_message = "Error in request - CONTENT: 'contentGenerated".$i." does not exist, it must exist before declaring 'contentgenerated".$i."Type'.";
	          echo $error_message;
			  $this->errorLog($component_found, $error_message);
	          exit();
		    }
		  }
	    }
	  
	  
	    // Query triple store to ensure content generated types are compatible with activity
	    // or if no type is set, retrieve a type and set it as a default type
	  
	    $content_generated_type_validated = array();
	    $sparql_content_generated_retrieval_query = $this->_sparql_prefixes.'

	  
SELECT ?Subject

WHERE
{
 gic:'.$this->_actual_activity.'  owl:equivilentClass  ?blank_a .
 ?blank_a  owl:onProperty  gic:contentGenerated .
 ?blank_a  owl:allValuesFrom  ?blank_b .

 ?blank_b  owl:unionOf  ?blank1 .

 ?blank1  rdf:rest*/rdf:first ?Subject .

}'  ;

        // Create fuseki query file to query globic model and retrieve list of possible content an activity can generate
	    $filepath5 = $_SERVER['DOCUMENT_ROOT']."/globicLog/service/temp/".$this->_request_array['component_id']."content_generated_retrieval.rq";
	    $file5 =  fopen($filepath5,"w") or exit("Unable to open file!");
	    fwrite($file5, $sparql_content_generated_retrieval_query);
	    fclose($file5);
	
	    
	    // Execute request query
		$results3 = shell_exec('@ECHO OFF & cd C:\DB_Server\jena-fuseki-0.2.7 && ruby s-query --service http://localhost:3030/Test/query --query='.$filepath5);

	    // Delete temp files
	    unlink($filepath5);
	  
	    // Decode request from JSON
	    $decoded3 = json_decode($results3, true);
	  
	    $content_generated_list_unexploded = array();
	    $content_generated_list = array();
	  
	    $content_generated_count1 = count($decoded3["results"]["bindings"]);
	  
	    for($i = 0; $i < $content_generated_count1; $i++){
	      $content_generated_list_unexploded[$i] = $decoded3["results"]["bindings"][$i][$decoded1["head"]["vars"][0]]["value"];
	    }
	  
	    $content_generated_count2 = count($content_generated_list_unexploded);
	  
	    $content_generated_list_with_prefixes = array();
	    $zz_count = count($this->_prefix_list);
	  
	    // Get a list of content with proper prefixes, eg. 'gic:Content' or 'nif:String' etc
	    for($i = 0; $i < $content_generated_count2; $i++){
	      $content_generated_list[$i] = explode('#' , $content_generated_list_unexploded[$i]);
		  for($a = 0; $a < $zz_count; $a++){
		    if ( $content_generated_list[$i][0] == trim($this->_prefix_list[$a][0], "#")){ // Compare URI inoder to put name space in front of content
		      $content_generated_list_with_prefixes[$i] = $this->_prefix_list[$a][1].$content_generated_list[$i][1];
		      break;
		    }
		  }
	    }
	  

	    // Loop through the array of list of content generated for activity and try to find match with content generated type stated in request
	    for($a = 1; $a <= 10; $a++){ // Loop through the possible 10 contentGenerated tokens
	      if ( isset($this->_request_array['contentGenerated'.$a.'Type']) ){
	        $content_generated_type_validated[$a] = false;
		  
		    for($i = 0; $i < $content_generated_count2; $i++){ // Loop through allowed content generated by activity
		          if ( $this->_request_array['contentGenerated'.$a.'Type'] === $content_generated_list_with_prefixes[$i] ){
		            $content_generated_type_validated[$a] = true;
				    break;
		          }
		    }
		  
		    if ($content_generated_type_validated[$a] == false ){
		      $error_message = "Error in request - CONTENT: '".$this->_request_array['contentGenerated'.$a.'Type']."' assigned to 'contentGenerated".$a."Type' does not match the allowable types of content which can be generated by activity : '".$this->_actual_activity."'.";
		      echo $error_message;
			  $this->errorLog($component_found, $error_message);
		      exit();
		    }
		  }
	    }
	  
	    // If content generated is declared but its corresponding type is not declared, then assign it a default type which is gic:Content.
	    for($a = 1; $a <= 10; $a++){
	      if ( isset($this->_request_array['contentGenerated'.$a]) ){
		    if ( isset($this->_request_array['contentGenerated'.$a.'Type']) == false ){
		      $this->_request_array['contentGenerated'.$a.'Type'] = $content_generated_list_with_prefixes[0];
			  //$this->_request_array['contentGenerated'.$a.'Type'] = "gic:Content";
		    }
		  }
	    }
	
	
	
	    // Ensure that there is at least one contentConsumed token or one contentGenerated token in request
	    $exist = false;
	  
	    for ($i=1; $i<=10; $i++){
	      if ( isset($this->_request_array['contentConsumed'.$i]) ){
		    $exist = true;
		    break;
		  }
	      else if ( isset($this->_request_array['contentGenerated'.$i]) ){
		    $exist = true;
		    break;
		  }
	    }
	  
	    if( $exist == false ){
	      $error_message = "Error in request - CONTENT: No 'contentConsumed' tokens or 'contentGenerated' tokens appear in the request. At least one must exist in request ( actual needs vary depending on activity carried out)";
	      echo $error_message;
		  $this->errorLog($component_found, $error_message);
	      exit();
	    }
	  
	  
	    // Once there are no errors and method is POST, prov data can be generated
	    $this->generateProvenance();
	  }

	
	
	  ///// If method is GET, then extract and validate retrieveData
	  else if($this->_request_array['http_method'] == 'GET'){
	
	    // Retrieve requestData from request string
	    if( isset($_REQUEST['retrieveData']) )
	      $this->_request_array['retrieve_data'] = $_REQUEST['retrieveData'];
	    else {
	      $error_message = "Error in request - RETRIEVE DATA: no retrieveData is set, ensure 'retrieveData=' is set in request.";
	      echo $error_message;
		  $this->errorLog($component_found, $error_message);
	      exit();
	    }
	  
	    if( $this->_request_array['retrieve_data'] == "" ){
	      $error_message = "Error in request - RETRIEVE DATA: retrieveData is empty, ensure it contains the name of some data that is to be retrieved from triple store.";
	      echo $error_message;
		  $this->errorLog($component_found, $error_message);
	      exit();
	    }
	  
	    // Once there are no errors and method is GET, prov data can be retrieved
	    $this->retrieveProvenance();
	  }
	}
	else{
	  $error_message = "Error in request - HTTP METHOD: method must be equal to 'POST' when using the registration utility.";
	  echo $error_message;
	  $this->errorLog($component_found, $error_message);
	  exit();
	}
  }

  /*********************************************************************
  The generateProvenance() method is used to generate provenance data about various aspects of data
  declared in a request, and store that provenance in the triple store.
  *********************************************************************/
  public function generateProvenance(){
  
	$sparql_query = $this->_sparql_prefixes.'prefix globiclogservice: <http://localhost/globicLog/service/>
	  
INSERT DATA {
  GRAPH globiclogservice:'.$this->_component_named_graph.' {
  gic:'.$this->_component_name.'  a  gic:ContentProcessingComponent .
  ';
  
    if ( isset($this->_request_array['user']) ){
	  $sparql_query = $sparql_query.'gic:'.$this->_request_array['user'].'  a  prov:Person .
  ';
	}
  
    for ($i=1; $i<=10; $i++){
      if ( isset($this->_request_array['contentConsumed'.$i]) ){
	    $sparql_query = $sparql_query.$this->_request_array['contentConsumed'.$i].'  a  '.$this->_request_array['contentConsumed'.$i.'Type'].' .
  ';
	  }
    }
  
    $sparql_query = $sparql_query.'gic:'.$this->_request_array['activity'].'  a  gic:'.$this->_actual_activity.' ;
       prov:wasAssociatedWith  gic:'.$this->_component_name.' ;
  ';
  
    if ( isset($this->_request_array['user']) ){
	  $sparql_query = $sparql_query.'     prov:wasAssociatedWith  gic:'.$this->_request_array['user'].' ;
	  ';
	}
  
    for ($i=1; $i<=10; $i++){
      if ( isset($this->_request_array['contentConsumed'.$i]) ){
	  $sparql_query = $sparql_query.' gic:contentConsumed  '.$this->_request_array['contentConsumed'.$i].' ;
  ';
	  }
    }
	
	$sparql_query = $sparql_query.'     prov:startedAtTime "'.date('c', time()).'"^^xsd:dateTime .
';
	   
	for ($i=1; $i<=10; $i++){
      if ( isset($this->_request_array['contentGenerated'.$i]) ){
	    $sparql_query = $sparql_query.'  '.$this->_request_array['contentGenerated'.$i].'  a  '.$this->_request_array['contentGenerated'.$i.'Type'].' ;
	   gic:contentGeneratedBy  gic:'.$this->_request_array['activity'].' ; 
	   ';
	   
	     for ($ii=1; $ii<=10; $ii++){
		   if ( isset($this->_request_array['contentConsumed'.$ii]) ){
		     $sparql_query = $sparql_query.'prov:wasDerivedFrom  '.$this->_request_array['contentConsumed'.$ii].' ;
	   ';
		   }
		 }
	   
	   $sparql_query = $sparql_query.'prov:generatedAtTime "'.date('c', time()).'"^^xsd:dateTime .
  ';
	  }
    }
	
	$sparql_query = $sparql_query.'}
}';
  
  
	// Create fuseki update file
	$filepath1 = $_SERVER['DOCUMENT_ROOT']."/globicLog/service/temp/update".$this->_request_array['component_id'].".ru";
	$file1 =  fopen($filepath1,"w") or exit("Unable to open file!");
	fwrite($file1, $sparql_query);
	fclose($file1);
	
	// Execute update query
	shell_exec('@ECHO OFF & cd C:\DB_Server\jena-fuseki-0.2.7 && ruby s-update --service http://localhost:3030/Test/update --update='.$filepath1);

	// Delete temp files
	unlink($filepath1);
	
	// Write to activity log
	$this->activityLog(false);
  }
  
  
  /*********************************************************************
  The retrieveProvenance() method is used to retrieve provenance data that is stored in the triple store.
  *********************************************************************/
  public function retrieveProvenance(){
    
	$sparql_query = $this->_sparql_prefixes.'prefix globiclogservice: <http://localhost/globicLog/service/>
	
SELECT ?predicate ?object
WHERE
{
  GRAPH globiclogservice:'.$this->_component_named_graph.' { '.$this->_request_array['retrieve_data'].' ?predicate ?object }
}'  ;

    // Create fuseki query file
	$filepath3 = $_SERVER['DOCUMENT_ROOT']."/globicLog/service/temp/query".$this->_request_array['component_id'].".rq";
	$file3 =  fopen($filepath3,"w") or exit("Unable to open file!");
	fwrite($file3, $sparql_query);
	fclose($file3);
	
	// Execute request query
	$results = shell_exec('@ECHO OFF & cd C:\DB_Server\jena-fuseki-0.2.7 && ruby s-query --service http://localhost:3030/Test/query --query='.$filepath3);

	// Delete temp files
	unlink($filepath3);
	
	// Checks to see if the request data is present in triple store, displays error if it is not
	$decoded1 = json_decode($results, true);
	if ( isset($decoded1["results"]["bindings"][0][$decoded1["head"]["vars"][0]]["value"]) ){
	  echo $results;
	}
	else{
	  $error_message = "Error in request - REQUEST DATA: '".$this->_request_array['retrieve_data']."' does not exist within the triple store";
	  echo $error_message;
	  $this->errorLog(true, $error_message);
	  exit();
	}
	
	// Write to activity log
	$this->activityLog(false);
	
  }
  
  
  /********************************************************************************************************
  The activityLog() method is used to log when an activity has taken place by the log service.
  A time stamp is provided along with component ID, request type (POST or GET) any content consumed,
  any content generated and the data to be retrieved
  ********************************************************************************************************/
  public function activityLog($registration){
  
	$file =  fopen($_SERVER['DOCUMENT_ROOT']."/globicLog/service/logs/Activity_Log.txt", "a");
	
	// Log registration details
	if($registration == true){
	  
	  $log = "[".date('c', time())."] | Activity: Register | ComponentID: ".$this->_component_name." | GraphName: ".$this->_component_named_graph.".\n"; 
	}
	// Log other activity details
	else {
	
	  $log = "[".date('c', time())."] | ComponentID = ".$this->_request_array['component_id']." | RequestType = ".$this->_request_array['http_method']." | ";
	
	  if($this->_request_array['http_method'] == 'POST'){
	    $log = $log."Activity = ".$this->_actual_activity." | ContentConsumed =";
    
	    for ($i=1; $i<=10; $i++){
          if ( isset($this->_request_array['contentConsumed'.$i]) ){
	        $log = $log." ".$this->_request_array['contentConsumed'.$i];
	      }
        }
	
	    $log = $log." | ContentGenerated = ";
	
	    for ($i=1; $i<=10; $i++){
          if ( isset($this->_request_array['contentGenerated'.$i]) ){
	        $log = $log." ".$this->_request_array['contentGenerated'.$i];
	      }
        }
	
	    $log = $log.".\n";
	  }
	
	  else if($this->_request_array['http_method'] == 'GET'){
	    $log = $log."RetrieveData = ".$this->_request_array['retrieve_data'].".\n";
	  }
	}
	
	fwrite($file, $log);
	fclose($file);
  }
  
  
  /********************************************************************************************************
  The errorLog() method is used to record any errors from requests to the service.
  A time stamp is provided along with component ID (if known - depends at what stage the error occurs
  and the actual error message itself.
  ********************************************************************************************************/
  public function errorLog($comp_found, $errorMessage){
  
    $log = "[".date('c', time())."] | ComponentID = ";
	
	if( $comp_found == true )
	  $log = $log.$this->_request_array['component_id'];
    else
      $log = $log."*UNKNOWN*";
		
	$log = $log." | ErrorMessage = ".$errorMessage.".\n";
  
    error_log($log, 3, $_SERVER['DOCUMENT_ROOT']."/globicLog/service/logs/Error_Log.txt");
  }
  
}

$randomNumber = rand(0, 10000);

$instance = array();

$instance[''.$randomNumber.''] = new RetrieveRequest();

?>