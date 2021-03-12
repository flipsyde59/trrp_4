<?php
require_once __DIR__."/../vendor/autoload.php";
require_once __DIR__."/config.php";

function getGUID()
{
    if (function_exists('com_create_guid')) {
        return com_create_guid();
    } else {
        mt_srand((double)microtime() * 10000);
        $charid = strtoupper(md5(uniqid(rand(), true)));
        $hyphen = chr(45);// "-"
        $uuid =
            substr($charid, 0, 8) . $hyphen
            . substr($charid, 8, 4) . $hyphen
            . substr($charid, 12, 4) . $hyphen
            . substr($charid, 16, 4) . $hyphen
            . substr($charid, 20, 12);
        return $uuid;
    }
}

function main_f()
{
    header('Content-Type: charset=utf-8');
    set_time_limit(0);
    ob_implicit_flush();
    if (($sock = socket_create(AF_INET, SOCK_STREAM, SOL_TCP)) < 0) {
        echo "An error while creating socket";
    } else {
        echo "The socket is create\n";
    }
    if (($ret = socket_bind($sock, SOCKET_ADDRESS, SOCKET_PORT)) < 0) {
        echo "An error while trying to binding with address and port";
    } else {
        echo "The socket was binding with address and port successfully\n";
    }

    if (($ret = socket_listen($sock, 5)) < 0) {
        echo "An error while try listen client";
    } else {
        echo "Wait client connections...\n";
        do {
            if (($msgsock = socket_accept($sock)) < 0) {
                echo "An error while start connection with socket";
            } else if (!connection_aborted()) {
                echo "Socket ready to get messages\n";

                $success = false;
                while ($success == false) {
                    echo 'Message from client: ';
                    if (false === ($buf = socket_read($msgsock, 1024))) {
                        echo "An error while read message from client";
                    } else {
                        $data = substr($buf, 2);
                        echo $data . PHP_EOL;
                        sleep(1);
                        $data_send = array("data" => $data);
                        $ch = curl_init();
                        curl_setopt($ch, CURLOPT_URL, WS_AUTH_URL);
                        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
                        curl_setopt($ch, CURLOPT_POST, 1);
                        curl_setopt($ch, CURLOPT_POSTFIELDS, $data_send);
                        $output = curl_exec($ch);
                        curl_close($ch);
                        echo "Response from web-service, which will be send to client: " . $output . "\n";
                        socket_write($msgsock, $output, strlen($output));
                        $success = true;
                        echo "Message to client is sent (with result of authorization)\n";
                        if ($output != "false") {
                            $ymq = new Aws\Sqs\SqsClient([
                                'version' => SQS_VERSION,
                                'region' => SQS_REGION,
                                'credentials' => array('key' => AWS_KEY, 'secret' => AWS_SECRET),
                                'endpoint' => SQS_ENDPOINT
                            ]);

                            $message = array(
                                'from' => 'auth',
                                'message' => $data,
                                'id_client' => $output
                            );
                            $ymq->sendMessage([
                                'QueueUrl' => YANDEX_MQ_URL,
                                'MessageBody' => json_encode($message)
                            ]);
                            echo "\nSend! Message to Server-CRUD: " . json_encode($message) . "\n";
                        } else {
                            echo "Message willn't be sent to Server-CRUD, because user not registered or already registered\n";
                        }
                    }
                }

            }
        } while (true);
    }
    if (isset($sock)) {
        socket_close($sock);
        echo "Сокет успешно закрыт";
    }
}

main_f();
?>