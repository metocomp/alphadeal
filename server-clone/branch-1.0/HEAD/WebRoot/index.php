<?php
	$pages = Array(
		'4'	=> Array('id' => '4', 'alias' => 'Home', 'file' => '4.php'),
		'2'	=> Array('id' => '2', 'alias' => 'ACCOUNT', 'file' => '2.php'),
		'3'	=> Array('id' => '3', 'alias' => 'HOW-IT-WORKS', 'file' => '3.php'),
		'5'	=> Array('id' => '5', 'alias' => 'REGISTER', 'file' => '5.php')
	);
	$forms = Array(
		'5'	=> Array(
			'ad5479c8' => Array( 'email' => 'metocomp@gmail.com', 'subject' => 'Register me', 'fields' => Array( Array( 'fidx' => '0', 'name' => 'Name', 'type' => 'input', 'options' => '' ), Array( 'fidx' => '1', 'name' => 'E-mail (This will be used as login name)', 'type' => 'input', 'options' => '' ), Array( 'fidx' => '2', 'name' => 'Address', 'type' => 'input', 'options' => '' ), Array( 'fidx' => '3', 'name' => 'City', 'type' => 'input', 'options' => '' ), Array( 'fidx' => '4', 'name' => 'State', 'type' => 'input', 'options' => '' ), Array( 'fidx' => '5', 'name' => 'Zip', 'type' => 'input', 'options' => '' ), Array( 'fidx' => '6', 'name' => 'DCA License# (Please enter the correct number or system will not accept)', 'type' => 'input', 'options' => '' ) ) )
		)
	);
	$base_dir = dirname(__FILE__);
	$base_url = '/';
	include dirname(__FILE__).'/inc.functions.php';
	$home_page = '4';
	$page_id = parse_uri();
	$user_key = "p96gA7Rq3qvroJZM";
	$user_hash = "0660931715fe784aa138a9176b563aa4";
	$blog_callback = "http://www.zyrobuilder.com/engine/?reset&ts=1325217058315blog.callback.php";
	$preview = false;
	if (isset($_POST["wb_form_id"])) include dirname(__FILE__).'/wb_scripts/wb_email_send.php';
	ob_start();
	if (isset($_REQUEST['view']) && $_REQUEST['view'] == 'news')
		include dirname(__FILE__).'/news.php';
	else if (isset($_REQUEST['view']) && $_REQUEST['view'] == 'blog')
		include dirname(__FILE__).'/blog.php';
	else
		include dirname(__FILE__).'/'.$pages[$page_id]['file'];
	ob_end_flush();

?>