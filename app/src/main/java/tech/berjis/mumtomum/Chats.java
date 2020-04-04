package tech.berjis.mumtomum;

import androidx.annotation.NonNull;

public class Chats {

    public String type, text, sender, receiver, chat_id, date, time, read;

    public Chats(String type, String text, String sender, String receiver, String chat_id, String date, String time, String read) {
        this.type = type;
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
        this.chat_id = chat_id;
        this.date = date;
        this.time = time;
        this.read = read;
    }

    public Chats(){

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRead() {
        return read;
    }

    public void setRead(String read) {
        this.read = read;
    }
}
