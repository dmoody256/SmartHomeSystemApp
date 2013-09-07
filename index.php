<?php
/**
 * File to handle all API requests
 * Accepts GET and POST
 *
 * Each request will be identified by TAG
 * Response will be JSON data
 
  /**
 * check for POST request
 */
if (isset($_POST['tag']) && $_POST['tag'] != '') {
    // get tag
    $tag = $_POST['tag'];
    
    $response = array("tag" => $tag, "success" => 0, "error" => 0);
    
    if ($tag == 'Test') {
    	echo json_encode($response);
    	
    }
}
?>
   
    