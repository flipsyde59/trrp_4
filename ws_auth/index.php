<?php
$config = __DIR__ . '/config.php';
require_once $config;
$link = mysqli_connect(DB_AUTH_HOST,DB_AUTH_USER,DB_AUTH_PASSWORD,DB_AUTH_NAME);

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

function main() {
    global $link;
    $fp = fopen('log_ws_auth.txt', 'a+');
    $today = date("Y.m.d H:i:s");
    $text = "Received data from Server-AUTH: ".json_encode($_POST['data'])."\n";
    fwrite($fp,$today." :: ".$text);

    $data = json_decode($_POST['data']);
    if($data->action == "auth")
    {
        $text = "User ".$data->user_login." trying to authorization\n";
        $today = date("Y.m.d H:i:s");
        fwrite($fp,$today." :: ".$text);
        $query = mysqli_query($link,"SELECT * FROM users WHERE user_login='".mysqli_real_escape_string($link,$data->user_login)."' LIMIT 1");
        if (mysqli_num_rows($query) == 0) {
            echo "false";
            $text = "access denied for user ".$data->user_login." - this user doesnt created\n";
            $today = date("Y.m.d H:i:s");
            fwrite($fp,$today." :: ".$text);
            fclose($fp);
        }
        else
        {
            $result = mysqli_fetch_assoc($query);
            $hashpassword_come = sha1($data->user_password . $result['salt']);
            if ($hashpassword_come == $result['hashpassword']) {
                echo strval($result['user_id']);
                $text = "access granted for user ".$data->user_login."\n";
                $today = date("Y.m.d H:i:s");
                fwrite($fp,$today." :: ".$text);
                fclose($fp);
            }
            else {
                echo "false";
                $text = "access denied for user ".$data->user_login." - maybe wrong password\n";
                $today = date("Y.m.d H:i:s");
                fwrite($fp,$today." :: ".$text);
                fclose($fp);
            }
        }
        return;
    }
    else
    {
        $text = "Registration of new user - ".$data->user_login."\n";
        $today = date("Y.m.d H:i:s");
        fwrite($fp,$today." :: ".$text);
        $query = mysqli_query($link,"SELECT * FROM users WHERE user_login='".mysqli_real_escape_string($link,$data->user_login)."'");
        if (mysqli_num_rows($query) == 0)
        {
            $guid = getGUID();
            $hashpassword = sha1($data->user_password . $guid);
            mysqli_query($link,"INSERT INTO users SET user_login='".mysqli_real_escape_string($link,$data->user_login)."', salt='".$guid."', hashpassword='".$hashpassword."'");
            $newID = mysqli_insert_id($link);
            echo strval($newID);
            $text = "new user successfully created\n";
            $today = date("Y.m.d H:i:s");
            fwrite($fp,$today." :: ".$text);
            fclose($fp);
        }
        else {
            echo "false";
            $text = "User ".$data->user_login." doesnt created - already registered\n";
            $today = date("Y.m.d H:i:s");
            fwrite($fp,$today." :: ".$text);
            fclose($fp);
        }
        return;
    }

    header('HTTP/1.0 400 Bad Request');
    echo json_encode(array(
        'error' => 'Bad Request'
    ));
}

main();
