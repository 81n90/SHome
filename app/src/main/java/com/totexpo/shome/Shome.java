package com.totexpo.shome;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.totexpo.shome.R;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Shome extends Activity implements TextToSpeech.OnInitListener {//Создание основного
    //  класса с интерфейсом RecognitionListener для обработки событий связанных с распознаванием речи
    // и интерфейсом для инициализации синтезатора голоса
    private String url = "jdbc:mysql://192.168.1.102:3306/smart_home?autoReconnect=true&amp;characterEncoding=UTF8&amp;useUnicode=true";
    private static final String user = "root";
    private static final String pass = "";
    public int r19=10;
    public int upbluetooth = 1;
    public String spout;
    public String sp;
    public TextToSpeech tts;
    private TextView returnedText;
    private TextView ErrText;
    private TextView editTexts;
    private TextView temText;
    private TextView oText;
    public TextView tv;
    public Button Button1;
    public Button Button2;
    public Button buttonDbase;
    public SpeechRecognizer speech = null;
    public Intent recognizerIntent;
    //Socket, с помощью которого будут отправляться данные на bluetooth Arduino
    public BluetoothSocket clientSocket;
    // UUID случае подключения к последовательному bluetooth  устройству
    private UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public HashMap<String, String> params = new HashMap<String, String>();
    private int bluecancel=0;
    public String st;
    public int value=100;
    public int bytes;
    public byte[] buffer = new byte[128];
    public String readMessage;
    public Timer time;
    private Context ma;
    // Вопросы
    public String sp0 = "выключить красный";
    public String sp1 = "включить красный";
    public String sp2 = "выключить зеленый";
    public String sp3 = "включить зеленый";
    public String sp4 = "выключить синий";
    public String sp5 = "включить синий";
    public String sp6 = "температура";
    public String sp10 = "как тебя зовут";
    public String sp11 = "что ты умеешь";
    public String sp12 = "твои команды";
    public String sp13 = "спасибо";
    public String sp14 = "откуда ты";
    public String sp15 = "привет";
    public String sp16 = "маша";
    public String sp19 = "конец связи";
    // Ответы
    public String sp20 = "меня зовут маша простокваша";
    public String sp21 = "я могу включать и выключать свет и определять температуру а так же записывать в базу все данные и говорить о состоянии системы";
    public String sp22 = "Включить зеленый. Выключить зеленый. То же для красного и синего. Температура.";
    public String sp23 = "пожалуйста";
    public String sp24 = "я из жопы";
    public String sp25 = "здравствуйте";
    public String sp26 = "я здесь";
    public String sp29 = "до свидания";
    public String sp30 = "красный выключила";
    public String sp31 = "красный включила";
    public String sp32 = "зеленый выключила";
    public String sp33 = "зеленый включила";
    public String sp34 = "синий выключила";
    public String sp35 = "синий включила";
    public AudioManager myAudioManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shome);
        returnedText = (TextView) findViewById(R.id.textView1);
        ErrText = (TextView) findViewById(R.id.textView2);
        temText = (TextView) findViewById(R.id.textView6);
        oText = (TextView) findViewById(R.id.textView5);
        editTexts = (TextView) findViewById(R.id.editTexts);
        Button1 = (Button) findViewById(R.id.button1);
        Button2 = (Button) findViewById(R.id.button2);
        buttonDbase = (Button) findViewById(R.id.button3);
        tv = (TextView) findViewById(R.id.tv_data);

        utteranceProgressListener TextSpeech = new utteranceProgressListener(); //Экземпляр класса слушателя
        tts = new TextToSpeech(this, this);
        tts.setOnUtteranceProgressListener(TextSpeech); //Установка слушателя синтеза речи
        ma=this;
// Создание экземпляра класса AudioManager
        myAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        buttonDbase.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new ConnectMySql().execute("insert","Пиздато");
            }
        });
    }
    private class ConnectMySql extends AsyncTask<String, Void, String> {
        private String sqlResult="";
        @Override
        protected String doInBackground(String... Arg0) {

            if(Arg0[0] == "select"){
                selectString(Arg0[1]);//"select * from voice_commands"
            }else if(Arg0[0] == "insert"){
                insertString(Arg0[1]);
            }

            return sqlResult;
        }
        @Override
        protected void onPostExecute(String sqlResult){
            super.onPostExecute(sqlResult);
        }

        private void selectString(String dbQuery){
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(dbQuery);
                //ResultSetMetaData rsmd = rs.getMetaData();

                while (rs.next()) {

                    sqlResult += ": " + rs.getString(1) + "\n";

                }
                tv.setText(sqlResult);
                st.close();
                con.close();


            } catch (Exception e) {
                e.printStackTrace();
                tv.setText(e.toString());
            }
        }
        private void insertString(String dbQuery){
            //String utf8String= new String(dbQuery.getBytes('UCS-2'),'UTF-8');
//            Charset forName;
//            forName = (String dbQuery);
            String sqlQuery = "INSERT INTO voice_commands (command_text, time_sad) VALUES ('" + dbQuery + "',NOW())";
            try {
                Class.forName("com.mysql.jdbc.Driver");
                Connection con = DriverManager.getConnection(url, user, pass);
                Statement st = con.createStatement();
                try {
//                    try {
//                        st.executeQuery("SET character_set_client='cp1251'");
//                    }catch (Exception e){
//                        e.printStackTrace();
//                        tv.setText("Ganduras");
//                    }
                    st.executeUpdate(sqlQuery);
                }catch (Exception e){
                    e.printStackTrace();
                    tv.setText("Jopa");
                }
// обойдемся без результата - если исключение не вывалилось, то можно считать, что все хорошо.
// Закрываем сам стейтмент.
// Желательно закрывать руками каждый использованный объект, а не надеяться на Connection.close();
// поскольку в случае использования пула соединение не будет закрыто, и соответственно ресурсы будут теряться
                st.close();
// Закрываем соединение
                con.close();
            } catch (Exception e) {
                e.printStackTrace();
                tv.setText("Manda");
            }

        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        // Остановить синтезатор речи tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (upbluetooth == 0) {
            try {
                clientSocket.close(); // Закрыть соединение с bluetooth
            } catch (IOException e2) {
            }
        }
    }

    @Override
    public void onInit(int status) {   // Инициализация перед синтезом речи
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.getDefault());
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            }
        } else {
            Log.e("TTS", "Init Failed!");
        }
    }

    public void bspeesh() {
        speech = SpeechRecognizer.createSpeechRecognizer(ma); //Создание объекта распознавателя речи
        speech.setRecognitionListener(thiss); //Установить обработчик событий распознавания
        // Передача параметров
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
    }

    RecognitionListener thiss;

    {
        thiss = new RecognitionListener() {
            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
            }

            @Override
            public void onEndOfSpeech() {
                oText.setText("НЕ ГОВОРИ");
            }

            @Override
            public void onError(int errorCode) {
                speak_off();  //Выключить звук в случае любой ошибки
                String errorMessage = getErrorText(errorCode); // Вызов метода расшифровки ошибки
                ErrText.setText(errorMessage + "; Ошибка=№" + errorCode);
                speech.destroy();
                bspeesh();
                oText.setText("ГОВОРИ");
                speech.startListening(recognizerIntent);
            }

            @Override
            public void onEvent(int arg0, Bundle arg1) {
            }

            @Override
            public void onPartialResults(Bundle arg0) {
            }

            @Override
            public void onReadyForSpeech(Bundle arg0) {
            }

            @Override
            public void onResults(Bundle results) {  // Результаты распознавания
                ArrayList data = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                sp = data.get(0).toString();
                value = 100;
                returnedText.setText(sp);
                new ConnectMySql().execute("insert","Фраза: " + sp);
                spout = sp;
                int r0 = sp.compareTo(sp0);
                int r1 = sp.compareTo(sp1);
                int r2 = sp.compareTo(sp2);
                int r3 = sp.compareTo(sp3);
                int r4 = sp.compareTo(sp4);
                int r5 = sp.compareTo(sp5);
                int r6 = sp.compareTo(sp6);

                int r10 = sp.compareTo(sp10);
                int r11 = sp.compareTo(sp11);
                int r12 = sp.compareTo(sp12);
                int r13 = sp.compareTo(sp13);
                int r14 = sp.compareTo(sp14);
                int r15 = sp.compareTo(sp15);
                int r16 = sp.compareTo(sp16);
                int r19 = sp.compareTo(sp19);

                if (r0 == 0) spout = sp30;
                if (r1 == 0) spout = sp31;
                if (r2 == 0) spout = sp32;
                if (r3 == 0) spout = sp33;
                if (r4 == 0) spout = sp34;
                if (r5 == 0) spout = sp35;
                if (r6 == 0) spout = sp6;
                if (r10 == 0) spout = sp20;
                if (r11 == 0) spout = sp21;
                if (r12 == 0) spout = sp22;
                if (r13 == 0) spout = sp23;
                if (r14 == 0) spout = sp24;
                if (r15 == 0) spout = sp25;
                if (r16 == 0) spout = sp26;
                if (r19 == 0) spout = sp29;
                if (upbluetooth == 0) {  // Если подключение к bluetooth существует то
                    // результат сравнения представляем символом
                    // Например, если результат распознавания голоса соответствует строке
                    // "включить зеленый" то на bluetooth посылаем символ 3 (код 51)
                    if (r1 == 0) value = 49;
                    if (r0 == 0) value = 48;
                    if (r3 == 0) value = 51;
                    if (r2 == 0) value = 50;
                    if (r5 == 0) value = 53;
                    if (r4 == 0) value = 52;
                    if (r6 == 0) value = 54;
                    // Посылаем данные
                    if (value != 54) outData(value);
                    if (value == 54) {
                        temText.setText(readMessage);  // Распечатываем температуру
                        spout = readMessage;
                    }
                }
                if (r0 == 0 || r1 == 0 || r2 == 0 || r3 == 0 || r4 == 0 || r5 == 0 || r6 == 0 || r10 == 0 || r11 == 0 || r12 == 0
                        || r13 == 0 || r14 == 0 || r15 == 0 || r16 == 0 || r19 == 0 || value == 54) // Синтез речи
                // выполняется в случае наличия команд и фраз в памяти
                {
                    speak_on(); // Включаем динамики
                    params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "HELLO");
                    tts.speak(spout, TextToSpeech.QUEUE_ADD, params);// Синтезировать речь
                } else {
                    speak_off(); // Если фразы и команды не описаны, выполняется распознавание речи, вывод результата
                    // в виде строки при выключенных динамиках
                    speech.stopListening(); //Прекратить слушать речь
                    speech.destroy();       // Уничтожить объект SpeechRecognizer
                    bspeesh();
                    oText.setText("ГОВОРИ");
                    speech.startListening(recognizerIntent);
                }
            }

            @Override
            public void onRmsChanged(float rmsdB) {
            }
        };
    }

    public void onClick1(View view) { // После нажатия на кнопку начать распознавание речи
        if(upbluetooth == 0) {
//При нажатии на кнопку останавливаю таймер работы программы по передаче и чтению  температуры
            time.cancel();
            time.purge();
// Запускаю таймер работы программы с новыми начальными данными(посылка запроса температуры и прием темпелатуры)
            bluecancel = 0;
            time = new Timer();
            bluetoothInOut bInOut = new bluetoothInOut();
            time.schedule(bInOut, 500, 1000);
        }
        speak_off();
        bspeesh(); // Вызов метода для активизации распознавателя голоса
        oText.setText("ГОВОРИ");
        speech.startListening(recognizerIntent); // Начать прослушивание речи
    }
    public void speak_off() // Метод для выключение внешних динамиков планшета
    {
        myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }
    public void speak_on() // Метод включения внешних динамиков планшета
    {
        myAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 20, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
    }
    public void onClick2(View view) { // После нажатия на кнопку закончить распознавание
        if (speech != null) {
            speech.stopListening(); //Прекратить слушать речь
            speech.destroy();       // Уничтожить объект SpeechRecognizer
            //  останавливаю таймер
            if (upbluetooth == 0) {
                time.cancel();
                time.purge();
// и запускаю таймер работы программы с новыми начальными данными(без посылки запроса температуры но с приемом температуры)
                bluecancel = 1;
                time = new Timer();
                bluetoothInOut bInOut = new bluetoothInOut();
                time.schedule(bInOut, 500, 1000);
            }
        }
    }
    public void onClick3(View view) {
        st = editTexts.getText().toString().toUpperCase(); // Получаем адрес bluetooth с строки ввода
        bluet();
    }
    // Класс, который необходим для фиксации окончания синтеза речи с целью запуска активити
// распознавания голоса
    public class utteranceProgressListener extends UtteranceProgressListener  {

        @Override
        public void onDone(String utteranceId) { // Действия после окончания речи синтезатором
            Shome.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    r19 = sp.compareTo(sp19);
                    if(r19 != 0) { // Если не "конец связи", то активити распознавания голоса запускается вновь
                        oText.setText("ГОВОРИ");
                        speech.startListening(recognizerIntent);
                    }
                    else  {
                        if(upbluetooth==0) { //Если Bluetooth включен то
                            // если произнесено "конец связи" - останавливаю таймер
                            time.cancel();
                            time.purge();
// и запускаю таймер работы программы с новыми начальными данными(без посылки запроса температуры но с приемом температуры)
                            bluecancel = 1;
                            time = new Timer();
                            bluetoothInOut bInOut = new bluetoothInOut();
                            time.schedule(bInOut, 500, 1000);
                        }
                    }
                }
            } );
        }

        @Override
        public void onStart(String utteranceId) {
        }

        @Override
        public void onError(String utteranceId) {
        }
    }
    public void bluet() { // Подключение к bluetooth при нажатии на кнопку
        Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        enableBt.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(enableBt);
        // Используется bluetooth по умолчанию
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

        try {
            // Выбираем bluetooth с конкретным адресом для простоты
            BluetoothDevice device = bluetooth.getRemoteDevice(st);
            // Создание RFCOMM секретного socket - а для входящих и исходящих сообщений
            clientSocket = device.createRfcommSocketToServiceRecord(uuid);
            // Попытка подключения к удаленному bluetooth
            clientSocket.connect();
            // Если попытка удалась, выводится сообщение внизу экрана
            Toast.makeText(getApplicationContext(), "Связь с bluetooth установлена", Toast.LENGTH_LONG).show();
            upbluetooth=0;
            bluecancel=0;
            time = new Timer();
            bluetoothInOut bInOut = new bluetoothInOut();
            time.schedule(bInOut,500,1000); //Через 500 миллисекунд после запуска программы начинать
            // запрашивать температуру каждую секунду

        }
        //В случае появления ошибок сообщаем, что bluetooth не подключен
        catch (IOException e) {
            upbluetooth=1;
            Toast.makeText(getApplicationContext(), "Проверь bluetooth!", Toast.LENGTH_LONG).show();
        }
    }
    public class bluetoothInOut extends TimerTask {

        public void run() {
            try {
                if( bluecancel == 0 ) // Только в этом случае посылаем запрос температуры
                {
                    OutputStream outStream = clientSocket.getOutputStream();
                    outStream.write(54);}
                // Получаем входной поток для приема данных
                InputStream inb = clientSocket.getInputStream();
                // Преобразование входного потока от bluetooth в строку
                DataInputStream in = new DataInputStream(inb);
                bytes = in.read(buffer);
                if (bytes > 10 )  // Если через bluetooth получено (например) больше 10 байт, то
                {
                    // преобразуем байты в строку с нулевого индекса до индекса bytes
                    readMessage = new String(buffer, 0, bytes);
                }
            } catch (IOException e) {
            }
        }
    }
    public void outData(int value) { // Выполняет передачу данных на Bluetooth
        try {
            // Получаем выходной поток для передачи данных
            OutputStream outStream = clientSocket.getOutputStream();
            // Посылаем данные
            outStream.write(value);
        } catch (IOException e) {
        }
    }
    public static String getErrorText(int errorCode) { // Метод возврата ошибки по ее коду
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }








}