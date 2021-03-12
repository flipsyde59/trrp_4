<?php
require_once 'config.php';
$link = mysqli_connect(DB_CRUD_HOST,DB_CRUD_USER,DB_CRUD_PASSWORD,DB_CRUD_NAME);

function main_func() {
    $fp = fopen('log_ws_crud.txt', 'a+');
    global $link;
    $data = json_decode($_POST['data']);
    $text = "Received data from Server-CRUD: ".json_encode($data)."\n";
    $today = date("Y.m.d H:i:s");
    fwrite($fp,$today." :: ".$text);
    if($data->action == "create")
    {
        $text = "users action - ".$data->action."\n";
        $today = date("Y.m.d H:i:s");
        fwrite($fp,$today." :: ".$text);
        mysqli_query($link,"INSERT INTO tasks SET name='".mysqli_real_escape_string($link,$data->name)."', description='".mysqli_real_escape_string($link,$data->description)."', ownerId='".$data->ownerId."', done='".$data->done."'");
        $newID = mysqli_insert_id($link);
        echo $newID;
        $text = "new data inserted into database; ID of new task - ".$newID." - sent to client\n";
        $today = date("Y.m.d H:i:s");
        fwrite($fp,$today." :: ".$text);
        fclose($fp);
        return;
    }

    if($data->action == "read")
    {
        $text = "users action - ".$data->action."\n";
        $today = date("Y.m.d H:i:s");
        fwrite($fp,$today." :: ".$text);
        $data_db=array();
        $query = mysqli_query($link,"SELECT id, name, description, done FROM tasks WHERE ownerId='".$data->ownerId."'");
        while($row = mysqli_fetch_assoc($query))
            $data_db[] = $row;
        if (count($data_db)>0) {
            echo json_encode($data_db);
            $text = "sent to user next data: ".json_encode($data_db)."\n";
        } else {
            echo "pusto";
            $text = "there is no tasks for user with this ID\n";
        }
        $today = date("Y.m.d H:i:s");
        fwrite($fp,$today." :: ".$text);
        fclose($fp);
        return;
    }

    if($data->action == "update")
    {
        $text = "users action - ".$data->action."\n";
        $today = date("Y.m.d H:i:s");
        fwrite($fp,$today." :: ".$text);
        $query = mysqli_query($link,"UPDATE tasks SET name='".mysqli_real_escape_string($link,$data->name)."', description='".mysqli_real_escape_string($link,$data->description)."', done='".($data->done==true?1:0)."' WHERE id='".$data->id."'");
        echo $query;
        $text = "data updated; sent to user 'true'\n";
        $today = date("Y.m.d H:i:s");
        fwrite($fp,$today." :: ".$text);
        fclose($fp);
        return;
    }

    if($data->action == "delete")
    {
        $text = "users action - ".$data->action."\n";
        $today = date("Y.m.d H:i:s");
        fwrite($fp,$today." :: ".$text);
        $query = mysqli_query($link,"DELETE FROM tasks WHERE id='".$data->id."'");
        echo $query;
        $text = "task with ID=".$data->id." was deleted; sent to user 'true'\n";
        $today = date("Y.m.d H:i:s");
        fwrite($fp,$today." :: ".$text);
        fclose($fp);
        return;
    }

    header('HTTP/1.0 400 Bad Request');
    echo json_encode(array(
        'error' => 'Bad Request'
    ));
}
main_func();
