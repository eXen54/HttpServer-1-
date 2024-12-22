<?php
include (realpath(__DIR__ . '/../functions/fetch.php'));


$query = "SELECT * FROM ETUDIANT2";
$jsonResult = fetchQuery($query, "api_key");
// Decode the JSON response
$result = json_decode($jsonResult, true);
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Query Results</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            line-height: 1.6;
            margin: 0;
            padding: 20px;
        }

        .query-info {
            background-color: #f4f4f4;
            padding: 10px;
            margin-bottom: 20px;
            border-radius: 5px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-bottom: 20px;
        }

        table, th, td {
            border: 1px solid #ddd;
        }

        th, td {
            padding: 12px;
            text-align: left;
        }

        th {
            background-color: #f2f2f2;
            font-weight: bold;
        }

        tr:nth-child(even) {
            background-color: #f9f9f9;
        }

        tr:hover {
            background-color: #f5f5f5;
        }

        .no-results, .error {
            color: #666;
            text-align: center;
            padding: 20px;
        }

        .error {
            color: #cc0000;
            background-color: #fff0f0;
        }
    </style>
</head>
<body>
<div class="query-info">
    <strong>Executed Query:</strong> <?php echo htmlspecialchars($query); ?>
</div>

<?php
// Check for JSON decoding errors
if ($result === null && json_last_error() !== JSON_ERROR_NONE) {
    echo "<div class='error'>Error decoding JSON response: " . htmlspecialchars(json_last_error_msg()) . "</div>";
    exit;
}

// Check if result is an array and has data
if (is_array($result) && count($result) > 0) {
    // Get the keys (column names) from the first row
    $columns = array_keys($result[0]);

    // Start the table
    echo "<table>";

    // Table header
    echo "<thead><tr>";
    foreach ($columns as $column) {
        echo "<th>" . htmlspecialchars($column) . "</th>";
    }
    echo "</tr></thead>";

    // Table body
    echo "<tbody>";
    foreach ($result as $row) {
        echo "<tr>";
        foreach ($columns as $column) {
            // Safely output the value, handling potential null or undefined values
            $value = $row[$column] ?? 'N/A';
            echo "<td>" . htmlspecialchars($value) . "</td>";
        }
        echo "</tr>";
    }
    echo "</tbody>";

    echo "</table>";

    // Show total number of rows
    echo "<p>Total rows returned: " . count($result) . "</p>";
} else {
    echo "<div class='no-results'>No results found for the given query.</div>";
}
?>
</body>
</html>