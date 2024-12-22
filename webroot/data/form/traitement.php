<?php
// Dynamically handle GET and POST methods
if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $queryString = $_SERVER['QUERY_STRING'] ?? '';
    parse_str($queryString, $_GET);

    $name = $_GET['name'] ?? '';
    $email = $_GET['email'] ?? '';
    $message = $_GET['message'] ?? '';

    echo "<h1>GET Request</h1>";
    echo "<p>Name: " . htmlspecialchars($name) . "</p>";
    echo "<p>Email: " . htmlspecialchars($email) . "</p>";
    echo "<p>Message: " . nl2br(htmlspecialchars($message)) . "</p>";
} elseif ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Read POST data from stdin if needed
    $rawInput = file_get_contents('php://stdin');
    parse_str($rawInput, $_POST);

    $name = $_POST['name'] ?? '';
    $email = $_POST['email'] ?? '';
    $message = $_POST['message'] ?? '';

    echo "<h1>POST Request</h1>";
    echo "<p>Name: " . htmlspecialchars($name) . "</p>";
    echo "<p>Email: " . htmlspecialchars($email) . "</p>";
    echo "<p>Message: " . nl2br(htmlspecialchars($message)) . "</p>";
} else {
    echo "<h1>Unsupported Request Method</h1>";
    echo "<p>Only GET and POST methods are supported.</p>";
}
?>