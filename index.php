<?php
require_once("lib/functions.inc");

ini_set('display_errors', 1);
error_reporting(E_ALL);
setHeaders($_SERVER['PHP_SELF']);

$template = <<<'TEMPLATE'
<div class="ticket %status$s">
  <div class="title">
    Ticket #<span class="id">%id$s</span> -
    <span class="placed_by">%placedby$s</span>
    @ <span class="location">%location$s</span>
    on <span class="date">%dates$s</span>
  </div>
  <div class="details">
    <p class="description">%description$s</p>
    <div class="admin"><b>Assigned:</b> <span>%admin$s</span></div>
    <div class="reply"><b>Reply:</b> <span>%reply$s</span></div>
    <div class="resolve"><b>Resolve:</b> [<span>%resolved_on$s</span>] <span>%resolve$s</span></div>
  </div>
</div>
TEMPLATE;

$empty = <<<'TEMPLATE'
<div class="empty">There are no tickets.</div>
TEMPLATE;
?>
<!DOCTYPE html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!--> <html class="no-js"> <!--<![endif]-->
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title>Ticket Status</title>
    <meta name="description" content="Display the help ticket status for our servers.">
    <meta name="viewport" content="width=device-width">
    <link rel="stylesheet" href="css/normalize.css">
    <link rel="stylesheet" href="css/main.css">
    <script src="js/vendor/modernizr-2.6.2.min.js"></script>
  </head>
  <body>
    <!--[if lt IE 7]>
    <p class="chromeframe">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> or <a href="http://www.google.com/chromeframe/?redirect=true">activate Google Chrome Frame</a> to improve your experience.</p>
    <![endif]-->
    <header><h1>Ticket Status</h1></header>
    <section>
      <?php echo getTickets('webstatus.cfg', $template, $empty); ?>
    </section>
    <footer>&copy; Jadedwolf &amp; OldDragon2A 2013</footer>
  </body>
</html>