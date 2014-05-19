<?php
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

function setHeaders($url) {
  header("Refresh: 120; url=" . $url);
  header("Cache-Control: no-cache");
  header("Pragma: no-cache");
}

function processTicket($template, $ticket, $full) {
  if (!isset($full["location"])   ) { $full["location"]    = "none"; }
  if (!isset($full["admin"])      ) { $full["admin"]       = "none"; }
  if (!isset($full["reply"])      ) { $full["reply"]       = "none"; }
  if (!isset($full["resolve"])    ) { $full["resolve"]     = "none"; }
  if (!isset($full["resolved_on"])) { $full["resolved_on"] = ""; }
  $full["id"] = $ticket;
  $full["status"] = $full["reply"] != "none" ? "reply" : $full["resolve"] != "none" ? "resolve" : "open";
  $encoded = array_map(function ($value) { return htmlentities($value); }, $full);
  return sprintfn($template, $encoded);
}

function getTickets($config, $template, $empty) {
  $result = '';
  // Load the list of ticket files
  $files = file($config, FILE_IGNORE_NEW_LINES | FILE_SKIP_EMPTY_LINES);
  foreach ($files as $file) {
    // Skip lines that start with a #
    if (preg_match('/^#/', $file)) { continue; }
    // Allow file paths or URLs
    $yaml = preg_match('/:\/\//', $file) ? yaml_parse_url($file) : yaml_parse_file($file);
    // Process all of the tickets found
    foreach ($yaml["Tickets"] as $ticket) {
      $result .= processTicket($template, $ticket, $yaml[$ticket]);
    }
  }
  if ($result === '') { $result = $empty; }
  return $result;
}

class Cache {
  var $dir = "./cache";
  var $file;
  var $time;
  var $caching = false;

  function __construct($file, $time = 86400) {
    $this->file = $this->dir . '/' . $file;
    $this->time = $time;
  }

  function start() {
    $ret = true;
    if (file_exists($this->file) && (time() - filemtime($this->file)) < $this->time) {
      echo "<!-- " . $this->file . " cached on " . date('c', filemtime($this->file)) . " -->\n";
      readfile($this->file);
      echo "<!-- End Cache -->";
      $ret = false;
    } else {
      ob_start();
      $this->caching = true;
    }
    return $ret;
  }

  function end() {
    if ($this->caching) {
      $cached = fopen($this->file, 'w');
      fwrite($cached, ob_get_contents());
      fclose($cached);
      ob_end_flush();
    }
  }
}