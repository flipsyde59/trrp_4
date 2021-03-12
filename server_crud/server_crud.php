<?php
require_once __DIR__."/config.php";
require_once __DIR__."/../vendor/autoload.php";
use Aws\Sqs\SqsClient;
use Aws\Exception\AwsException;

$ymq = new Aws\Sqs\SqsClient([
    'version' => SQS_VERSION,
    'region' => SQS_REGION,
    'credentials' => array('key'=>AWS_KEY,'secret'=>AWS_SECRET),
    'endpoint' => SQS_ENDPOINT
]);
echo "Server CRUD start to receive!\n";
while (true)
{
    $result = $ymq->receiveMessage([
        'QueueUrl' => YANDEX_MQ_URL,
        'WaitTimeSeconds' => 8
    ]);
    if ($result["Messages"])
        foreach ($result["Messages"] as $msg) {
            echo('Message received: ' .$msg['Body']. PHP_EOL);
            $message = json_decode($msg['Body']);
            if ($message->from == 'auth'){
                $ymq->deleteMessage([
                    'QueueUrl' => YANDEX_MQ_URL,
                    'ReceiptHandle' => $msg['ReceiptHandle'],
                ]);
                $data_crud_read = array(
                    "action" => "read",
                    "ownerId" => $message->id_client
                );
                $send_read = array("data"=>json_encode($data_crud_read));
                $ch = curl_init(WS_CRUD_URL);
                curl_setopt($ch, CURLOPT_URL, WS_CRUD_URL);
                curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
                curl_setopt($ch, CURLOPT_POST, 1);
                curl_setopt($ch, CURLOPT_POSTFIELDS, $send_read);
                $output = curl_exec($ch);
                curl_close($ch);
                echo "Server received message from WS-CRUD. Body of message: " . $output . PHP_EOL;
                $send_to_client = array(
                    'from' => 'server',
                    'message' => $output,
                    'id_client' => $message->id_client
                );
                $ymq->sendMessage([
                    'QueueUrl' => YANDEX_MQ_URL,
                    'MessageBody' => json_encode($send_to_client)
                ]);
                echo "Server sent to client with ID=".$message->id_client." message with next data: \n";
                echo json_encode($send_to_client);
            }
            else if ($message->from == 'client')
            {
                $ymq->deleteMessage([
                    'QueueUrl' => YANDEX_MQ_URL,
                    'ReceiptHandle' => $msg['ReceiptHandle'],
                ]);
                $ch = curl_init(WS_CRUD_URL);
                curl_setopt($ch, CURLOPT_URL, WS_CRUD_URL);
                curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
                curl_setopt($ch, CURLOPT_POST, 1);
                switch ($message->action)
                {
                    case 'read':
                    {
                        $data_crud_read = array(
                            "action" => "read",
                            "ownerId" => $message->id_client
                        );
                        $send_read = array("data"=>json_encode($data_crud_read));
                        curl_setopt($ch, CURLOPT_POSTFIELDS, $send_read);
                        $output = curl_exec($ch);
                        echo "Server received message from WS-CRUD. Body of message: " . $output . PHP_EOL;
                        $send_to_client = array(
                            'from' => 'server',
                            'message' => $output,
                            'id_client' => $message->id_client
                        );
                        $ymq->sendMessage([
                            'QueueUrl' => YANDEX_MQ_URL,
                            'MessageBody' => json_encode($send_to_client)
                        ]);
                        echo "Send to client with ID=".$message->id_client." message with next data: \n";
                        echo json_encode($send_to_client) . PHP_EOL;
                        break;
                    }
                    case 'create':
                    {
                        $data_crud_create = array(
                            "action" => "create",
                            "name" => $message->message->name,
                            "description" => $message->message->description,
                            "ownerId" => $message->id_client,
                            "done" => 0
                        );
                        $send_read = array("data"=>json_encode($data_crud_create));
                        curl_setopt($ch, CURLOPT_POSTFIELDS, $send_read);
                        $output = curl_exec($ch);
                        echo "Server received message from WS-CRUD. Body of message: " . $output . PHP_EOL;
                        $send_to_client = array(
                            'from' => 'server',
                            'message' => $output,
                            'id_client' => $message->id_client
                        );
                        $ymq->sendMessage([
                            'QueueUrl' => YANDEX_MQ_URL,
                            'MessageBody' => json_encode($send_to_client)
                        ]);
                        echo "Send to client with ID=".$message->id_client." message with next data: \n";
                        echo json_encode($send_to_client) . PHP_EOL;
                        break;
                    }
                    case 'update':
                    {
                        $data_crud_update = array(
                            "action" => "update",
                            "name" => $message->message->name,
                            "description" => $message->message->description,
                            "id" => $message->message->id,
                            "done" => $message->message->done
                        );
                        $send_read = array("data"=>json_encode($data_crud_update));
                        curl_setopt($ch, CURLOPT_POSTFIELDS, $send_read);
                        $output = curl_exec($ch);
                        echo "Server received message from WS-CRUD. Body of message: " . $output . PHP_EOL;
                        $send_to_client = array(
                            'from' => 'server',
                            'message' => $output,
                            'id_client' => $message->id_client
                        );
                        $ymq->sendMessage([
                            'QueueUrl' => YANDEX_MQ_URL,
                            'MessageBody' => json_encode($send_to_client)
                        ]);
                        echo "Send to client with ID=".$message->id_client." message with next data: \n";
                        echo json_encode($send_to_client) . PHP_EOL;
                        break;
                    }
                    case 'delete':
                    {
                        $data_crud_delete = array(
                            "action" => "delete",
                            "id" => $message->message->id
                        );
                        $send_read = array("data"=>json_encode($data_crud_delete));
                        curl_setopt($ch, CURLOPT_POSTFIELDS, $send_read);
                        $output = curl_exec($ch);
                        echo "Server received message from WS-CRUD. Body of message: " . $output . PHP_EOL;
                        $send_to_client = array(
                            'from' => 'server',
                            'message' => $output,
                            'id_client' => $message->id_client
                        );
                        $ymq->sendMessage([
                            'QueueUrl' => YANDEX_MQ_URL,
                            'MessageBody' => json_encode($send_to_client)
                        ]);
                        echo "Send to client with ID=".$message->id_client." message with next data: \n";
                        echo json_encode($send_to_client) . PHP_EOL;
                        break;
                    }
                }
                curl_close($ch);
            }
        }
    sleep(1);
}