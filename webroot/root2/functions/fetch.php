<?php
function fetchQuery($query, $apiKey)
{
    $url = "http://localhost:2020/api/";

    $postData = [
        "query" => $query,
        "api_key" => $apiKey
    ];

    $jsonData = json_encode($postData);

    $options = [
        "http" => [
            "method" => "POST",
            "header" => "Content-Type: application/json\r\n" .
                "Content-Length: " . strlen($jsonData) . "\r\n",
            "content" => $jsonData
        ]
    ];

    $context = stream_context_create($options);


    $response = file_get_contents($url, false, $context);

    return $response;
}

?>
