<?php
ini_set('display_errors', 1);
error_reporting(E_ALL);

header("Refresh: 120; url=".$_SERVER['PHP_SELF']);
header("Cache-Control: no-cache");
header("Pragma: no-cache");

// Supports locally accesible files and URLs
$files = array(
  "/minecraft-server/plugins/TakeaNumber/Tickets.yml",
  "http://server/Tickets.yml"
);

$template = <<<'TEMPLATE'
<div class="ticket %status$s">
  <div class="title">
    Ticket #<span class="id">%id$s</span> -
    <span class="placed_by">%placedby$s</span>
    @ <span class="location">%location$s</span>
    on <span class="date">%dates$s</span>
  </div>
  <p class="description">%description$s</p>
  <div class="admin"><b>Assigned:</b> <span>%admin$s</span></div>
  <div class="reply"><b>Reply:</b> <span>%reply$s</span></div>
  <div class="resolve"><b>Resolve:</b> [<span>%resolved_on$s</span>] <span>%resolve$s</span></div>
</div>
TEMPLATE;

function sprintfn ($format, array $args = array()) {
  $arg_nums = array_slice(array_flip(array_keys(array(0 => 0) + $args)), 1);

  for ($pos = 0; preg_match('/(?<=%)([a-zA-Z_]\w*)(?=\$)/', $format, $match, PREG_OFFSET_CAPTURE, $pos);) {
    $arg_pos = $match[0][1];
    $arg_len = strlen($match[0][0]);
    $arg_key = $match[1][0];

    if (! array_key_exists($arg_key, $arg_nums)) {
      user_error("sprintfn(): Missing argument '${arg_key}'", E_USER_WARNING);
      return false;
    }

    $format = substr_replace($format, $replace = $arg_nums[$arg_key], $arg_pos, $arg_len);
    $pos = $arg_pos + strlen($replace); // skip to end of replacement for next iteration
  }

  return vsprintf($format, array_values($args));
}

function processTicket ($ticket, $full) {
  global $template;

  if (!isset($full["admin"])      ) { $full["admin"]       = "none"; }
  if (!isset($full["reply"])      ) { $full["reply"]       = "none"; }
  if (!isset($full["resolve"])    ) { $full["resolve"]     = "none"; }
  if (!isset($full["resolved_on"])) { $full["resolved_on"] = ""; }
  $full["id"] = $ticket;
  $full["status"] = $full["reply"] != "none" ? "reply" : $full["resolve"] != "none" ? "resolve" : "open";
  
  $encoded = array_map(function ($value) { return htmlentities($value); }, $full);
  return sprintfn($template, $encoded);
}
?>
<!DOCTYPE html>
<html>
<head>
  <title>Ticket Status</title>
  <style>
    * { margin: 0; padding: 0; }
    
    h1 { margin: 0; padding: 0.5em; color: #090; text-align: center;
      background-color: rgb(60,60,60);
      background-color: rgba(60,60,60, 0.5);
    }
    h2 { margin: 0.5em 0 0.5em; color: #900; text-align: center; }

    body {
      font-family: Verdana, Tahoma, sans-serif;
      font-size: 11px;
      font-weight: normal;
      background-image: url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAIAAAC0Ujn1AAAAjUlEQVRIibXUMQpAIQwD0Hyhi/e/rctfBFGqrW3MHB6Z8tVa4Y6I+Mvlkdta89K3LpyrA66Ljrk2HXYNOuOe6KS7pfOuTlNchWa5K010J5rrDprudvqFC6A8cnF1qleuiHjpWxfO1QHXRcdcmw67Bp1xT3TS3dJ5V6cprkKz3JUmuhPNdQdNdzv9wgXwAy6LV2lsHHA9AAAAAElFTkSuQmCC");
    }

    #dump { margin: 1em; padding: 1em; color: #fff; background-color: #333; border: 1px solid #999; }

    .ticket         { margin: 1em 1em 0; padding: 1em; color: #fff; background-color: #222; border: 1px solid #666; }
    .ticket .title  { padding-bottom: 0.25em; margin-bottom: 0.25em; border-bottom: 1px dotted #666; }
    .ticket.reply .title   { color: #ff0; }
    .ticket.resolve .title { color: #0f0; }
    .ticket.open .title    { color: #f00; }
    
    #copyright { margin-top: 12px; text-align: center; font-size: smaller; color: #fff; }
  </style>
</head>
<body>
  <h1>Ticket Status</h1>
  <?php
    foreach ($files as $file) {
      $yaml = preg_match('/:\/\//', $file) ?
        yaml_parse_url($file) :
        yaml_parse_file($file);
      foreach ($yaml["Tickets"] as $ticket) { echo processTicket($ticket, $yaml[$ticket]); }
    }
  ?>
  <div id="copyright">&copy; Jadedwolf &amp; OldDragon2A 2013</div>
</body>
</html>

